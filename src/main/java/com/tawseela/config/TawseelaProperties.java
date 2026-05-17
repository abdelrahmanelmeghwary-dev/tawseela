package com.tawseela.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tawseela")
public class TawseelaProperties {

    private Jwt jwt = new Jwt();
    private Otp otp = new Otp();
    private Sms sms = new Sms();
    private AdminBootstrap adminBootstrap = new AdminBootstrap();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt != null ? jwt : new Jwt();
    }

    public Otp getOtp() {
        return otp;
    }

    public void setOtp(Otp otp) {
        this.otp = otp != null ? otp : new Otp();
    }

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms != null ? sms : new Sms();
    }

    public AdminBootstrap getAdminBootstrap() {
        return adminBootstrap;
    }

    public void setAdminBootstrap(AdminBootstrap adminBootstrap) {
        this.adminBootstrap = adminBootstrap != null ? adminBootstrap : new AdminBootstrap();
    }

    @PostConstruct
    public void applySmsEnvOverride() {
        if (sms.getTwilio() == null) {
            sms.setTwilio(new Sms.Twilio());
        }
        String smsEnv = System.getenv("TAWSEELA_SMS_ENABLED");
        if (StringUtils.hasText(smsEnv)) {
            sms.setEnabled(Boolean.parseBoolean(smsEnv.trim()));
        }
    }

    /** True when a fixed OTP is configured; Twilio must not be called in this mode. */
    public boolean isFixedOtpActive() {
        return StringUtils.hasText(otp.getFixedCode());
    }

    public static class Jwt {
        private String secret = "";
        private long accessExpirationMs = 900_000L;
        private long refreshExpirationMs = 1_209_600_000L;
        private boolean blacklistAccessTokenOnLogout = true;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret != null ? secret : "";
        }

        public long getAccessExpirationMs() {
            return accessExpirationMs;
        }

        public void setAccessExpirationMs(long accessExpirationMs) {
            this.accessExpirationMs = accessExpirationMs;
        }

        public long getRefreshExpirationMs() {
            return refreshExpirationMs;
        }

        public void setRefreshExpirationMs(long refreshExpirationMs) {
            this.refreshExpirationMs = refreshExpirationMs;
        }

        public boolean isBlacklistAccessTokenOnLogout() {
            return blacklistAccessTokenOnLogout;
        }

        public void setBlacklistAccessTokenOnLogout(boolean blacklistAccessTokenOnLogout) {
            this.blacklistAccessTokenOnLogout = blacklistAccessTokenOnLogout;
        }
    }

    public static class Otp {
        private int ttlMinutes = 5;
        private int maxAttempts = 5;
        /** When set, every issued OTP uses this value instead of a random code. Clear for production. */
        private String fixedCode = "1234";

        public String getFixedCode() {
            return fixedCode;
        }

        public void setFixedCode(String fixedCode) {
            this.fixedCode = fixedCode;
        }

        public int getTtlMinutes() {
            return ttlMinutes;
        }

        public void setTtlMinutes(int ttlMinutes) {
            this.ttlMinutes = ttlMinutes;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
    }

    public static class Sms {
        private boolean enabled;
        private Twilio twilio = new Twilio();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Twilio getTwilio() {
            return twilio;
        }

        public void setTwilio(Twilio twilio) {
            this.twilio = twilio != null ? twilio : new Twilio();
        }

        public static class Twilio {
            private String accountSid = "";
            private String authToken = "";
            private String fromNumber = "";
            private String messagingServiceSid = "";

            public String getAccountSid() {
                return accountSid;
            }

            public void setAccountSid(String accountSid) {
                this.accountSid = accountSid != null ? accountSid : "";
            }

            public String getAuthToken() {
                return authToken;
            }

            public void setAuthToken(String authToken) {
                this.authToken = authToken != null ? authToken : "";
            }

            public String getFromNumber() {
                return fromNumber;
            }

            public void setFromNumber(String fromNumber) {
                this.fromNumber = fromNumber != null ? fromNumber : "";
            }

            public String getMessagingServiceSid() {
                return messagingServiceSid;
            }

            public void setMessagingServiceSid(String messagingServiceSid) {
                this.messagingServiceSid = messagingServiceSid != null ? messagingServiceSid : "";
            }
        }
    }

    public static class AdminBootstrap {
        private String mobile = "admin";
        private String defaultPassword = "ChangeMe1!Strong";

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile != null ? mobile : "admin";
        }

        public String getDefaultPassword() {
            return defaultPassword;
        }

        public void setDefaultPassword(String defaultPassword) {
            this.defaultPassword = defaultPassword != null ? defaultPassword : "ChangeMe1!Strong";
        }
    }
}

