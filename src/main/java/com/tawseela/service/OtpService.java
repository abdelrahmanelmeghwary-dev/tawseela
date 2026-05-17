package com.tawseela.service;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.entity.OtpEntity;
import com.tawseela.entity.OtpPurpose;
import com.tawseela.entity.OtpStatus;
import com.tawseela.entity.User;
import com.tawseela.exception.BusinessException;
import com.tawseela.repository.OtpRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpRepository otpRepository;
    private final TawseelaProperties props;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpRepository otpRepository, TawseelaProperties props) {
        this.otpRepository = otpRepository;
        this.props = props;
    }

    @Transactional
    public String createAndPersistOtp(User user, OtpPurpose purpose) {
        expirePreviousPending(user.getId(), purpose);
        String code = resolveOtpCode();
        OtpEntity otp = new OtpEntity();
        otp.setUser(user);
        otp.setOtpCode(code);
        otp.setPurpose(purpose);
        otp.setStatus(OtpStatus.PENDING);
        otp.setAttempts(0);
        otp.setExpiresAt(Instant.now().plus(props.getOtp().getTtlMinutes(), ChronoUnit.MINUTES));
        otpRepository.save(otp);
        if (!props.getSms().isEnabled()) {
            log.warn("OTP (SMS disabled) for {} purpose {}: {}", user.getMobileNumber(), purpose, code);
        } else {
            log.info("OTP issued for {} purpose {}", user.getMobileNumber(), purpose);
        }
        return code;
    }

    private String resolveOtpCode() {
        String fixed = props.getOtp().getFixedCode();
        if (StringUtils.hasText(fixed)) {
            return fixed.trim();
        }
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private void expirePreviousPending(java.util.UUID userId, OtpPurpose purpose) {
        List<OtpEntity> pending =
                otpRepository.findLatestForUserPurposeStatus(userId, purpose, OtpStatus.PENDING);
        Instant now = Instant.now();
        for (OtpEntity o : pending) {
            o.setStatus(OtpStatus.EXPIRED);
        }
        otpRepository.saveAll(pending);
    }

    @Transactional
    public void verifyCode(User user, OtpPurpose purpose, String submittedCode) {
        OtpEntity otp = otpRepository
                .findLatestPending(user.getId(), purpose)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "No active OTP for this step"));

        if (Instant.now().isAfter(otp.getExpiresAt())) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        if (otp.getAttempts() >= props.getOtp().getMaxAttempts()) {
            otp.setStatus(OtpStatus.FAILED);
            otpRepository.save(otp);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Maximum OTP attempts exceeded");
        }

        if (!otp.getOtpCode().equals(submittedCode.trim())) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= props.getOtp().getMaxAttempts()) {
                otp.setStatus(OtpStatus.FAILED);
            }
            otpRepository.save(otp);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        otp.setStatus(OtpStatus.VERIFIED);
        otpRepository.save(otp);
    }

    @Transactional
    public OtpEntity verifyCodeReturningEntity(User user, OtpPurpose purpose, String submittedCode) {
        OtpEntity otp = otpRepository
                .findLatestPending(user.getId(), purpose)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "No active OTP for this step"));

        if (Instant.now().isAfter(otp.getExpiresAt())) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        if (otp.getAttempts() >= props.getOtp().getMaxAttempts()) {
            otp.setStatus(OtpStatus.FAILED);
            otpRepository.save(otp);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Maximum OTP attempts exceeded");
        }

        if (!otp.getOtpCode().equals(submittedCode.trim())) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= props.getOtp().getMaxAttempts()) {
                otp.setStatus(OtpStatus.FAILED);
            }
            otpRepository.save(otp);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        otp.setStatus(OtpStatus.VERIFIED);
        otpRepository.save(otp);
        return otp;
    }

    @Transactional
    public int expireStale() {
        return otpRepository.expirePendingPastDue(OtpStatus.EXPIRED, OtpStatus.PENDING, Instant.now());
    }
}
