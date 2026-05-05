package com.tawseela.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tawseela")
public record TawseelaProperties(Jwt jwt, Otp otp, Sms sms) {

    public TawseelaProperties {
        if (sms == null) {
            sms = new Sms(false, new Sms.Twilio("", "", "", ""));
        } else if (sms.twilio() == null) {
            sms = new Sms(sms.enabled(), new Sms.Twilio("", "", "", ""));
        }
        if (otp == null) {
            otp = new Otp(5);
        }
    }

    public record Jwt(String secret, long accessExpirationMs, long refreshExpirationMs) {}

    /** OTP lifetime only — always a real random code. */
    public record Otp(int ttlMinutes) {}

    /**
     * Twilio SMS. When {@code enabled=false} the OTP is still printed to the log for local testing.
     * Set {@code enabled=true} with credentials (prefer env vars) to deliver OTP to the phone.
     */
    public record Sms(boolean enabled, Twilio twilio) {

        /**
         * Use either {@code fromNumber} (a number bought on Twilio) or {@code messagingServiceSid} (MG…),
         * not your personal mobile as {@code From} — see Twilio error 21659.
         */
        public record Twilio(String accountSid, String authToken, String fromNumber, String messagingServiceSid) {
            public Twilio {
                accountSid = accountSid != null ? accountSid : "";
                authToken = authToken != null ? authToken : "";
                fromNumber = fromNumber != null ? fromNumber : "";
                messagingServiceSid = messagingServiceSid != null ? messagingServiceSid : "";
            }
        }
    }
}
