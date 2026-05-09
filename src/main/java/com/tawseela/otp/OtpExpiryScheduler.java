package com.tawseela.otp;

import com.tawseela.security.AccessTokenBlacklist;
import com.tawseela.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OtpExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(OtpExpiryScheduler.class);

    private final OtpService otpService;
    private final AccessTokenBlacklist accessTokenBlacklist;

    public OtpExpiryScheduler(OtpService otpService, AccessTokenBlacklist accessTokenBlacklist) {
        this.otpService = otpService;
        this.accessTokenBlacklist = accessTokenBlacklist;
    }

    @Scheduled(fixedDelayString = "${tawseela.otp.expire-scan-ms:300000}")
    public void expireOtps() {
        int updated = otpService.expireStale();
        if (updated > 0) {
            log.debug("Marked {} OTP rows as EXPIRED", updated);
        }
        accessTokenBlacklist.purgeExpired();
    }
}
