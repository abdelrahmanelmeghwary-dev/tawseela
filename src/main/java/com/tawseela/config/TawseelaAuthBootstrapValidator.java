package com.tawseela.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TawseelaAuthBootstrapValidator {

    private static final Logger log = LoggerFactory.getLogger(TawseelaAuthBootstrapValidator.class);

    private final TawseelaProperties props;

    public TawseelaAuthBootstrapValidator(TawseelaProperties props) {
        this.props = props;
    }

    @PostConstruct
    void validate() {
        byte[] secret = props.getJwt().getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("tawseela.jwt.secret must be at least 32 bytes for HS256");
        }
        if (!props.getSms().isEnabled()) {
            log.warn(
                    "SMS disabled: OTP codes are logged for local testing. Configure Twilio and TAWSEELA_SMS_ENABLED for production.");
            return;
        }
        TawseelaProperties.Sms.Twilio t = props.getSms().getTwilio();
        if (!StringUtils.hasText(t.getAccountSid()) || !StringUtils.hasText(t.getAuthToken())) {
            throw new IllegalStateException("SMS enabled but Twilio account-sid or auth-token is missing.");
        }
        if (!StringUtils.hasText(t.getMessagingServiceSid()) && !StringUtils.hasText(t.getFromNumber())) {
            log.warn("Twilio: set from-number or messaging-service-sid or SMS delivery may fail.");
        } else {
            log.info("Twilio SMS enabled.");
        }
    }
}

