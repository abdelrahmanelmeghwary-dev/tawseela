package com.tawseela.service.impl;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.dto.request.DriverProfileRequest;
import com.tawseela.dto.request.ForgotPasswordResetRequest;
import com.tawseela.dto.request.ForgotPasswordSendOtpRequest;
import com.tawseela.dto.request.ForgotPasswordVerifyOtpRequest;
import com.tawseela.dto.request.LoginRequest;
import com.tawseela.dto.request.RegisterRequest;
import com.tawseela.dto.request.RegisterVerifyRequest;
import com.tawseela.dto.response.AuthMeResponse;
import com.tawseela.dto.response.AuthTokensResponse;
import com.tawseela.dto.response.ForgotPasswordVerifyResponse;
import com.tawseela.dto.response.RegisterVerifyResponse;
import com.tawseela.entity.DriverProfile;
import com.tawseela.entity.OtpEntity;
import com.tawseela.entity.Profile;
import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.User;
import com.tawseela.enums.OtpPurpose;
import com.tawseela.enums.OtpStatus;
import com.tawseela.enums.SystemRole;
import com.tawseela.exception.BusinessException;
import com.tawseela.mapper.UserMapper;
import com.tawseela.repository.DriverProfileRepository;
import com.tawseela.repository.OtpRepository;
import com.tawseela.repository.RoleRepository;
import com.tawseela.repository.UserRepository;
import com.tawseela.security.AccessTokenBlacklist;
import com.tawseela.security.JwtService;
import com.tawseela.security.SecurityUtils;
import com.tawseela.service.AuthService;
import com.tawseela.service.OtpService;
import com.tawseela.service.ProfileService;
import com.tawseela.service.TokenService;
import com.tawseela.service.TwilioSmsService;
import com.tawseela.util.PhoneNormalizer;
import com.tawseela.util.RoleNames;
import io.jsonwebtoken.JwtException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final TwilioSmsService twilioSmsService;
    private final TawseelaProperties tawseelaProperties;
    private final AuthenticationManager authenticationManager;
    private final OtpRepository otpRepository;
    private final JwtService jwtService;
    private final AccessTokenBlacklist accessTokenBlacklist;
    private final UserMapper userMapper;
    private final ProfileService profileService;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            DriverProfileRepository driverProfileRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService,
            TokenService tokenService,
            TwilioSmsService twilioSmsService,
            TawseelaProperties tawseelaProperties,
            AuthenticationManager authenticationManager,
            OtpRepository otpRepository,
            JwtService jwtService,
            AccessTokenBlacklist accessTokenBlacklist,
            UserMapper userMapper,
            ProfileService profileService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.twilioSmsService = twilioSmsService;
        this.tawseelaProperties = tawseelaProperties;
        this.authenticationManager = authenticationManager;
        this.otpRepository = otpRepository;
        this.jwtService = jwtService;
        this.accessTokenBlacklist = accessTokenBlacklist;
        this.userMapper = userMapper;
        this.profileService = profileService;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Password and confirm password do not match");
        }
        String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
        if (mobile.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        if (userRepository.existsByMobileNumber(mobile)) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "This mobile number is already registered. Please login instead.");
        }
        SystemRole requestedRole = SystemRole.valueOf(request.getRole());
        if (requestedRole == SystemRole.ADMIN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid registration role");
        }
        if (requestedRole == SystemRole.DRIVER) {
            if (!StringUtils.hasText(request.getVehicleType())
                    || !StringUtils.hasText(request.getVehicleNumber())
                    || !StringUtils.hasText(request.getLicenseNumber())) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST, "vehicleType, vehicleNumber, and licenseNumber are required for drivers");
            }
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setMobileNumber(mobile);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneVerified(false);
        user.setEnabled(requestedRole == SystemRole.CUSTOMER);

        RoleEntity roleEntity = roleRepository
                .findByName(requestedRole)
                .orElseThrow(() -> new IllegalStateException("Role not seeded: " + requestedRole));
        user.getRoles().add(roleEntity);
        userRepository.save(user);

        if (requestedRole == SystemRole.DRIVER) {
            DriverProfile dp = new DriverProfile();
            dp.setUser(user);
            dp.setVehicleType(request.getVehicleType().trim());
            dp.setVehicleNumber(request.getVehicleNumber().trim());
            dp.setLicenseNumber(request.getLicenseNumber().trim());
            dp.setApproved(false);
            driverProfileRepository.save(dp);
        }

        String code = otpService.createAndPersistOtp(user, OtpPurpose.REGISTER);
        sendSmsIfConfigured(mobile, code);
    }

    @Override
    @Transactional
    public RegisterVerifyResponse verifyRegistration(RegisterVerifyRequest request) {
        String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
        if (mobile.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        User user = userRepository
                .findByMobileNumberEagerRoles(mobile)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        otpService.verifyCode(user, OtpPurpose.REGISTER, request.getOtpCode());
        user.setPhoneVerified(true);
        userRepository.save(user);
        user = userRepository.findByIdEagerRoles(user.getId()).orElse(user);
        if (RoleNames.hasRole(user, SystemRole.DRIVER)) {
            return new RegisterVerifyResponse(
                    null,
                    "Phone verified. Your driver account is pending admin approval; you will be able to login after approval.");
        }
        return new RegisterVerifyResponse(tokenService.issueForUser(user), "Phone verified. Registration complete.");
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokensResponse login(LoginRequest request) {
        String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
        if (mobile.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(mobile, request.getPassword()));
        } catch (BadCredentialsException | DisabledException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials or account not ready");
        }
        User user = userRepository
                .findByMobileNumberEagerRoles(mobile)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return tokenService.issueForUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokensResponse refresh(String refreshToken) {
        return tokenService.rotateRefresh(refreshToken);
    }

    @Override
    @Transactional
    public void logout(UUID userId, String refreshToken, String authorizationHeader) {
        tokenService.revokeRefreshIfOwned(userId, refreshToken);
        if (!tawseelaProperties.getJwt().isBlacklistAccessTokenOnLogout()) {
            return;
        }
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }
        String access = authorizationHeader.substring(7).trim();
        if (access.isEmpty()) {
            return;
        }
        try {
            JwtService.ParsedAccessToken parsed = jwtService.parseAccessToken(access);
            if (parsed.getJti() != null && parsed.getExpiresAt() != null) {
                accessTokenBlacklist.denyUntil(parsed.getJti(), parsed.getExpiresAt());
            }
        } catch (JwtException ignored) {
            // refresh token is still revoked
        }
    }

    @Override
    @Transactional
    public void forgotSendOtp(ForgotPasswordSendOtpRequest request) {
        String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
        if (mobile.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        User user = userRepository
                .findByMobileNumberEagerRoles(mobile)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        sendSmsIfConfigured(mobile, otpService.createAndPersistOtp(user, OtpPurpose.FORGET_PASSWORD));
    }

    @Override
    @Transactional
    public ForgotPasswordVerifyResponse forgotVerifyOtp(ForgotPasswordVerifyOtpRequest request) {
        String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
        if (mobile.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        User user = userRepository
                .findByMobileNumberEagerRoles(mobile)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        OtpEntity otp = otpService.verifyCodeReturningEntity(user, OtpPurpose.FORGET_PASSWORD, request.getOtpCode());
        return new ForgotPasswordVerifyResponse("OTP verified", otp.getId().toString());
    }

    @Override
    @Transactional
    public void forgotReset(ForgotPasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Password and confirm password do not match");
        }
        String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
        if (mobile.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        User user = userRepository
                .findByMobileNumberEagerRoles(mobile)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        UUID resetId;
        try {
            resetId = UUID.fromString(request.getResetToken());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid reset token");
        }
        OtpEntity otp = otpRepository
                .findByIdAndUser_Id(resetId, user.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Invalid reset token"));
        if (otp.getPurpose() != OtpPurpose.FORGET_PASSWORD || otp.getStatus() != OtpStatus.VERIFIED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Reset token is not valid for this step");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otp.setStatus(OtpStatus.EXPIRED);
        otpRepository.save(otp);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthMeResponse getCurrentUser() {
        UUID userId = SecurityUtils.requireUserId();
        User user = userRepository
                .findByIdEagerRoles(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        AuthMeResponse response = userMapper.toAuthMe(user);

        Profile profile = profileService.ensureProfile(userId);
        response = userMapper.enrichWithProfile(response, profile);

        if (RoleNames.hasRole(user, SystemRole.DRIVER)) {
            DriverProfile driverProfile = driverProfileRepository
                    .findByUser_Id(userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Driver profile not found"));
            response = userMapper.enrichWithDriverProfile(response, driverProfile);
        }
        return response;
    }

    @Override
    @Transactional
    public AuthMeResponse updateDriverProfile(DriverProfileRequest request) {
        UUID userId = SecurityUtils.requireUserId();
        User user = userRepository
                .findByIdEagerRoles(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        if (!RoleNames.hasRole(user, SystemRole.DRIVER)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Driver role required");
        }
        DriverProfile driverProfile = driverProfileRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Driver profile not found"));
        driverProfile.setVehicleType(request.getVehicleType().trim());
        driverProfile.setVehicleNumber(request.getVehicleNumber().trim());
        driverProfile.setLicenseNumber(request.getLicenseNumber().trim());
        driverProfileRepository.save(driverProfile);
        return getCurrentUser();
    }

    private void sendSmsIfConfigured(String mobile, String code) {
        if (tawseelaProperties.isFixedOtpActive() || !tawseelaProperties.getSms().isEnabled()) {
            return;
        }
        twilioSmsService.sendOtp(mobile, code);
    }
}
