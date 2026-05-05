package com.tawseela.service;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.domain.Profile;
import com.tawseela.domain.Role;
import com.tawseela.dto.MeResponse;
import com.tawseela.dto.OtpVerifyRequest;
import com.tawseela.dto.RefreshTokenRequest;
import com.tawseela.dto.TokenResponse;
import com.tawseela.dto.TokenResponse.UserSummary;
import com.tawseela.repository.ProfileRepository;
import com.tawseela.security.JwtPrincipal;
import com.tawseela.security.JwtService;
import com.tawseela.service.support.OtpStore;
import com.tawseela.util.PhoneNormalizer;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final TawseelaProperties props;
    private final OtpStore otpStore;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final JwtService jwtService;
    private final TwilioSmsService twilioSmsService;
    private final SecureRandom random = new SecureRandom();

    public AuthService(
            TawseelaProperties props,
            OtpStore otpStore,
            ProfileService profileService,
            ProfileRepository profileRepository,
            JwtService jwtService,
            TwilioSmsService twilioSmsService) {
        this.props = props;
        this.otpStore = otpStore;
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.jwtService = jwtService;
        this.twilioSmsService = twilioSmsService;
    }

    public void requestOtp(String phone) {
        String normalized = PhoneNormalizer.normalize(phone);
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone number");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expires = Instant.now().plus(props.otp().ttlMinutes(), ChronoUnit.MINUTES);
        otpStore.put(normalized, code, expires);

        // Always printed so you can verify locally without SMS configured.
        log.info("OTP for {} → {} (expires in {} min)", normalized, code, props.otp().ttlMinutes());

        if (props.sms().enabled()) {
            twilioSmsService.sendOtp(normalized, code);
        }
    }

    @Transactional
    public TokenResponse verifyOtp(OtpVerifyRequest body) {
        String normalized = PhoneNormalizer.normalize(body.phone());
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone number");
        }
        if (!otpStore.verifyAndRemove(normalized, body.otp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        return profileService
                .findByPhone(normalized)
                .map(p -> {
                    mergeRegistrationOnVerify(p, body, false);
                    return buildTokenResponse(profileService.save(p));
                })
                .orElseGet(() -> {
                    Profile p = newProfile(normalized, resolveSignupRole(body.role()));
                    mergeRegistrationOnVerify(p, body, true);
                    return buildTokenResponse(profileService.save(p));
                });
    }

    private static Role resolveSignupRole(String requested) {
        if (requested == null || requested.isBlank()) {
            return Role.customer;
        }
        String r = requested.trim().toLowerCase();
        try {
            Role role = Role.valueOf(r);
            if (role == Role.admin) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "admin role cannot be set via OTP signup");
            }
            return role;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: use customer or driver");
        }
    }

    private static Profile newProfile(String normalizedPhone, Role role) {
        Profile p = new Profile();
        p.setPhone(normalizedPhone);
        p.setRole(role);
        return p;
    }

    private static String syntheticEmailForPhone(String normalizedPhone) {
        return normalizedPhone.replace("+", "") + "@phone.tawseela.local";
    }

    private void mergeRegistrationOnVerify(Profile p, OtpVerifyRequest body, boolean isNew) {
        if (body.fullName() != null && !body.fullName().isBlank()) {
            p.setFullName(body.fullName().trim());
        }
        if (body.fcmToken() != null && !body.fcmToken().isBlank()) {
            p.setFcmToken(body.fcmToken().trim());
        }
        if (body.email() != null && !body.email().isBlank()) {
            setEmailIfAvailable(p, body.email().trim().toLowerCase());
        } else if (isNew && (p.getEmail() == null || p.getEmail().isBlank())) {
            p.setEmail(syntheticEmailForPhone(p.getPhone()));
        }
    }

    private void setEmailIfAvailable(Profile p, String normalizedEmail) {
        profileRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .filter(other -> !other.getId().equals(p.getId()))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                });
        p.setEmail(normalizedEmail);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshTokenRequest body) {
        UUID userId;
        try {
            userId = jwtService.parseRefreshTokenUserId(body.refreshToken());
        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        Profile profile = profileService
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return buildTokenResponse(profile);
    }

    @Transactional(readOnly = true)
    public MeResponse me(JwtPrincipal principal) {
        Profile p = profileService
                .findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return new MeResponse(
                p.getId(),
                p.getPhone(),
                p.getEmail(),
                p.getFullName(),
                p.getRole().name(),
                p.getFcmToken(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }

    private TokenResponse buildTokenResponse(Profile p) {
        long expiresInSeconds = props.jwt().accessExpirationMs() / 1000L;
        return new TokenResponse(
                jwtService.createAccessToken(p),
                jwtService.createRefreshToken(p.getId()),
                "Bearer",
                expiresInSeconds,
                new UserSummary(p.getId(), p.getPhone(), p.getEmail(), p.getFullName(), p.getRole().name()));
    }
}
