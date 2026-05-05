package com.tawseela.service;

import com.tawseela.config.TawseelaProperties;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sends OTP via Twilio Programmable SMS
 * (<a href="https://www.twilio.com/docs/sms/api/message-resource">Messages API</a>).
 */
@Service
public class TwilioSmsService {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsService.class);

    private final TawseelaProperties props;
    private final RestClient restClient = RestClient.builder().build();

    public TwilioSmsService(TawseelaProperties props) {
        this.props = props;
    }

    public void sendOtp(String e164Phone, String code) {
        if (!props.sms().enabled()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "SMS delivery is disabled (set tawseela.sms.enabled=true and Twilio credentials)");
        }
        TawseelaProperties.Sms.Twilio t = props.sms().twilio();
        if (t.accountSid().isBlank() || t.authToken().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Twilio account-sid and auth-token are required when SMS is enabled");
        }
        boolean useMessagingService = !t.messagingServiceSid().isBlank();
        boolean useFrom = !t.fromNumber().isBlank();
        if (!useMessagingService && !useFrom) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Twilio: set tawseela.sms.twilio.from-number to a number from your Twilio account "
                            + "(Console → Phone Numbers → Active), or set messaging-service-sid (MG…). "
                            + "Your own mobile cannot be used as From — Twilio error 21659.");
        }
        String uri =
                "https://api.twilio.com/2010-04-01/Accounts/" + t.accountSid().trim() + "/Messages.json";
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", e164Phone);
        form.add("Body", "Your Tawseela verification code is: " + code);
        if (useMessagingService) {
            form.add("MessagingServiceSid", t.messagingServiceSid().trim());
        } else {
            form.add("From", t.fromNumber().trim());
        }
        try {
            restClient
                    .post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(h -> h.setBasicAuth(t.accountSid().trim(), t.authToken().trim()))
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            String err = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            log.error("Twilio SMS failed: {} {}", e.getStatusCode(), err);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to send SMS OTP", e);
        }
    }
}
