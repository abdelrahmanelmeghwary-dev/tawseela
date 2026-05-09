package com.tawseela.service;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class TwilioSmsService {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsService.class);

    private final TawseelaProperties props;
    private final RestTemplate restTemplate;

    public TwilioSmsService(TawseelaProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    private static String otpSmsBody(String code) {
        return "Tawseela OTP is : " + code;
    }

    public void sendOtp(String e164Phone, String code) {
        if (!props.getSms().isEnabled()) {
            throw new BusinessException(
                    HttpStatus.SERVICE_UNAVAILABLE, "SMS delivery is disabled (configure Twilio and enable SMS)");
        }
        TawseelaProperties.Sms.Twilio t = props.getSms().getTwilio();
        if (!org.springframework.util.StringUtils.hasText(t.getAccountSid())
                || !org.springframework.util.StringUtils.hasText(t.getAuthToken())) {
            throw new BusinessException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Twilio account-sid and auth-token are required when SMS is enabled");
        }
        boolean useMessagingService = org.springframework.util.StringUtils.hasText(t.getMessagingServiceSid());
        boolean useFrom = org.springframework.util.StringUtils.hasText(t.getFromNumber());
        String uri =
                "https://api.twilio.com/2010-04-01/Accounts/" + t.getAccountSid().trim() + "/Messages.json";
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("To", e164Phone);
        form.add("Body", otpSmsBody(code));
        if (useMessagingService) {
            form.add("MessagingServiceSid", t.getMessagingServiceSid().trim());
        } else if (useFrom) {
            form.add("From", t.getFromNumber().trim());
        } else {
            throw new BusinessException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Twilio: set from-number or messaging-service-sid for SMS delivery");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(t.getAccountSid().trim(), t.getAuthToken().trim(), StandardCharsets.UTF_8);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
        try {
            ResponseEntity<Void> resp =
                    restTemplate.postForEntity(uri, entity, Void.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to send SMS OTP");
            }
            log.info("Twilio SMS queued for OTP delivery to {}", e164Phone);
        } catch (HttpStatusCodeException e) {
            String err = e.getResponseBodyAsString();
            log.error("Twilio SMS failed: {} {}", e.getStatusCode(), err);
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to send SMS OTP");
        }
    }
}
