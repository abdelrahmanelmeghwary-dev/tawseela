package com.tawseela.service;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.dto.AuthTokensResponse;
import com.tawseela.dto.ForgotPasswordResetRequest;
import com.tawseela.dto.ForgotPasswordSendOtpRequest;
import com.tawseela.dto.ForgotPasswordVerifyOtpRequest;
import com.tawseela.dto.ForgotPasswordVerifyResponse;
import com.tawseela.dto.LoginRequest;
import com.tawseela.dto.OtpSendPublicRequest;
import com.tawseela.dto.OtpVerifyApiResponse;
import com.tawseela.dto.OtpVerifyPublicRequest;
import com.tawseela.dto.RegisterRequest;
import com.tawseela.dto.RegisterVerifyRequest;
import com.tawseela.dto.RegisterVerifyResponse;
import com.tawseela.entity.DriverProfile;
import com.tawseela.entity.OtpEntity;
import com.tawseela.entity.OtpPurpose;
import com.tawseela.entity.OtpStatus;
import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.SystemRole;
import com.tawseela.entity.User;
import com.tawseela.exception.BusinessException;
import com.tawseela.repository.DriverProfileRepository;
import com.tawseela.repository.OtpRepository;
import com.tawseela.repository.RoleRepository;
import com.tawseela.repository.UserRepository;
import com.tawseela.security.AccessTokenBlacklist;
import com.tawseela.security.JwtService;
import com.tawseela.util.PhoneNormalizer;
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
public class AuthService {

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

    public AuthService(
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
            AccessTokenBlacklist accessTokenBlacklist) {
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
    }

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
        if (requestedRole == SystemRole.CUSTOMER) {
            user.setEnabled(true);
        } else {
            user.setEnabled(false);
        }

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
        boolean isDriver = hasRole(user, SystemRole.DRIVER);
        if (isDriver) {
            return new RegisterVerifyResponse(
                    null,
                    "Phone verified. Your driver account is pending admin approval; you will be able to login after approval.");
        }
        return new RegisterVerifyResponse(tokenService.issueForUser(user), "Phone verified. Registration complete.");
    }

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

    @Transactional(readOnly = true)
    public AuthTokensResponse refresh(String refreshToken) {
        return tokenService.rotateRefresh(refreshToken);
    }

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
            // ignore malformed access token; refresh is still revoked
        }
    }

    @Transactional
    public void sendOtpPublic(OtpSendPublicRequest request) {
        OtpPurpose purpose = OtpPurpose.valueOf(request.getPurpose().trim());
        String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
        if (mobile.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        switch (purpose) {
            case REGISTER:
                User regUser = userRepository
                        .findByMobileNumberEagerRoles(mobile)
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
                if (regUser.isPhoneVerified()) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "Mobile number is already verified");
                }
                String regCode = otpService.createAndPersistOtp(regUser, OtpPurpose.REGISTER);
                sendSmsIfConfigured(mobile, regCode);
                break;
            case FORGET_PASSWORD:
                ForgotPasswordSendOtpRequest forgotReq = new ForgotPasswordSendOtpRequest();
                forgotReq.setMobileNumber(request.getMobileNumber());
                forgotSendOtp(forgotReq);
                break;
            case LOGIN:
                User loginUser = userRepository
                        .findByMobileNumberEagerRoles(mobile)
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
                if (!isLoginOtpAllowed(loginUser)) {
                    throw new BusinessException(
                            HttpStatus.FORBIDDEN, "Account cannot receive a login OTP in its current state");
                }
                String loginCode = otpService.createAndPersistOtp(loginUser, OtpPurpose.LOGIN);
                sendSmsIfConfigured(mobile, loginCode);
                break;
        }
    }

    @Transactional
    public OtpVerifyApiResponse verifyOtpPublic(OtpVerifyPublicRequest request) {
        OtpPurpose purpose = OtpPurpose.valueOf(request.getPurpose().trim());
        OtpVerifyApiResponse body = new OtpVerifyApiResponse();
        body.setPurpose(request.getPurpose().trim());
        switch (purpose) {
            case REGISTER:
                RegisterVerifyRequest rv = new RegisterVerifyRequest();
                rv.setMobileNumber(request.getMobileNumber());
                rv.setOtpCode(request.getOtpCode());
                body.setRegistration(verifyRegistration(rv));
                break;
            case FORGET_PASSWORD:
                ForgotPasswordVerifyOtpRequest fv = new ForgotPasswordVerifyOtpRequest();
                fv.setMobileNumber(request.getMobileNumber());
                fv.setOtpCode(request.getOtpCode());
                body.setForgotPassword(forgotVerifyOtp(fv));
                break;
            case LOGIN:
                String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
                if (mobile.isEmpty()) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
                }
                User loginUser = userRepository
                        .findByMobileNumberEagerRoles(mobile)
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
                otpService.verifyCode(loginUser, OtpPurpose.LOGIN, request.getOtpCode());
                body.setLoginOtpVerified(Boolean.TRUE);
                break;
        }
        return body;
    }

    private boolean isLoginOtpAllowed(User user) {
        if (!user.isPhoneVerified() || !user.isEnabled()) {
            return false;
        }
        if (hasRole(user, SystemRole.DRIVER)) {
            return driverProfileRepository
                    .findByUser_Id(user.getId())
                    .map(DriverProfile::isApproved)
                    .orElse(false);
        }
        return true;
    }

    @Transactional
    public void forgotSendOtp(ForgotPasswordSendOtpRequest request) {
        String mobile = PhoneNormalizer.normalize(request.getMobileNumber());
        if (mobile.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        User user = userRepository
                .findByMobileNumberEagerRoles(mobile)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        String code = otpService.createAndPersistOtp(user, OtpPurpose.FORGET_PASSWORD);
        sendSmsIfConfigured(mobile, code);
    }

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

    private void sendSmsIfConfigured(String mobile, String code) {
        if (tawseelaProperties.getSms().isEnabled()) {
            twilioSmsService.sendOtp(mobile, code);
        }
    }

    private static boolean hasRole(User user, SystemRole role) {
        for (RoleEntity r : user.getRoles()) {
            if (r.getName() == role) {
                return true;
            }
        }
        return false;
    }
}
