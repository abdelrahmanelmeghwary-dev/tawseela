package com.tawseela.service.support;

import com.tawseela.domain.OtpCode;
import com.tawseela.repository.OtpCodeRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database-backed OTP store.
 * <p>
 * Each {@code put} inserts a new row in {@code otp_codes}; previous codes for the same phone are
 * invalidated naturally (only the latest non-expired row is checked on verify). All codes for a
 * phone are removed on successful verify. Expired rows are cleaned up every 10 minutes.
 */
@Component
public class OtpStore {

    private static final Logger log = LoggerFactory.getLogger(OtpStore.class);

    private final OtpCodeRepository repo;

    public OtpStore(OtpCodeRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void put(String normalizedPhone, String code, Instant expiresAt) {
        repo.save(new OtpCode(normalizedPhone, code, expiresAt));
    }

    /**
     * Returns {@code true} if a valid, non-expired code exists for the phone and matches,
     * then deletes all codes for that phone.
     */
    @Transactional
    public boolean verifyAndRemove(String normalizedPhone, String code) {
        return repo.findLatestValid(normalizedPhone, Instant.now())
                .filter(otp -> otp.getCode().equals(code))
                .map(otp -> {
                    repo.deleteAllByPhone(normalizedPhone);
                    return true;
                })
                .orElse(false);
    }

    /** Removes expired OTP rows every 10 minutes to keep the table small. */
    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void cleanupExpired() {
        int deleted = repo.deleteExpired(Instant.now());
        if (deleted > 0) {
            log.debug("Cleaned up {} expired OTP row(s)", deleted);
        }
    }
}
