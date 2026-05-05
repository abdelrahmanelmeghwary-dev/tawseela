package com.tawseela.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * On startup: if SMS is enabled, verifies Twilio credentials are all present (fails fast).
 * If SMS is disabled the app still works — the OTP is printed to the log (console) so you can
 * copy it locally.
 */
@Component
public class TawseelaAuthBootstrapValidator {

    private static final Logger log = LoggerFactory.getLogger(TawseelaAuthBootstrapValidator.class);

    private final TawseelaProperties props;

    public TawseelaAuthBootstrapValidator(TawseelaProperties props) {
        this.props = props;
    }

    @PostConstruct
    void validate() {
        if (!props.sms().enabled()) {
            log.warn(
                    "SMS is disabled (tawseela.sms.enabled=false). "
                            + "OTP codes will be printed to the application log — suitable for local testing only. "
                            + "Enable Twilio SMS and set TWILIO_* env vars to deliver OTP to phones.");
            return;
        }
        TawseelaProperties.Sms.Twilio t = props.sms().twilio();
        if (t.accountSid().isBlank() || t.authToken().isBlank()) {
            throw new IllegalStateException(
                    "tawseela.sms.enabled=true but Twilio account-sid or auth-token is missing in tawseela.sms.twilio.");
        }
        if (t.messagingServiceSid().isBlank() && t.fromNumber().isBlank()) {
            log.warn(
                    "Twilio: set from-number (a number you bought on Twilio) or messaging-service-sid (MG…), "
                            + "otherwise SMS will fail (Twilio error 21659 if you use a non-Twilio number as From).");
        } else {
            log.info("Twilio SMS enabled — OTP will be sent to users' phones.");
        }
    }
}
