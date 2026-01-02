package com.kitly.mail.provider.brevo;

import com.kitly.mail.model.Email;
import com.kitly.mail.service.MailProvider;
import com.kitly.mail.service.MailProviderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "mail.provider", havingValue = "brevo")
@Slf4j
public class BrevoMailProvider implements MailProvider {

    private final WebClient webClient;
    private final String apiKey;

    public BrevoMailProvider(@Value("${brevo.api.key}") String apiKey,
                             @Value("${brevo.api.url:https://api.brevo.com/v3}") String apiUrl) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", apiKey)
                .build();
    }

    @Override
    public String sendEmail(Email email) throws MailProviderException {
        try {
            log.info("Sending email via BREVO to: {}", email.getToEmail());

            Map<String, Object> request = buildBrevoRequest(email);

            BrevoResponse response = webClient.post()
                    .uri("/smtp/email")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(BrevoResponse.class)
                    .block();

            if (response != null && response.getMessageId() != null) {
                log.info("Email sent successfully via BREVO. Message ID: {}", response.getMessageId());
                return response.getMessageId();
            } else {
                throw new MailProviderException("No message ID received from BREVO");
            }
        } catch (WebClientResponseException e) {
            log.error("Error sending email via BREVO: {}", e.getResponseBodyAsString(), e);
            throw new MailProviderException("BREVO API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email via BREVO", e);
            throw new MailProviderException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildBrevoRequest(Email email) {
        Map<String, Object> request = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("email", email.getFromEmail());
        sender.put("name", email.getFromName());
        request.put("sender", sender);

        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", email.getToEmail());
        recipient.put("name", email.getToName());
        request.put("to", List.of(recipient));

        request.put("subject", email.getSubject());

        if (email.getHtmlContent() != null && !email.getHtmlContent().isEmpty()) {
            request.put("htmlContent", email.getHtmlContent());
        }

        if (email.getTextContent() != null && !email.getTextContent().isEmpty()) {
            request.put("textContent", email.getTextContent());
        }

        return request;
    }

    @Override
    public String getProviderName() {
        return "BREVO";
    }
}
