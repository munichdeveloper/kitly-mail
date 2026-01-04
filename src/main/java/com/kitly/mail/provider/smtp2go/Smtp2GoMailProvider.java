package com.kitly.mail.provider.smtp2go;

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
@ConditionalOnProperty(name = "mail.provider", havingValue = "smtp2go")
@Slf4j
public class Smtp2GoMailProvider implements MailProvider {

    private final WebClient webClient;
    private final String apiKey;

    public Smtp2GoMailProvider(@Value("${smtp2go.api.key}") String apiKey,
                               @Value("${smtp2go.api.url:https://api.smtp2go.com/v3}") String apiUrl) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Smtp2go-Api-Key", apiKey)
                .build();
    }

    @Override
    public String sendEmail(Email email) throws MailProviderException {
        // Validate that at least one content type is provided
        if ((email.getHtmlContent() == null || email.getHtmlContent().isEmpty()) &&
            (email.getTextContent() == null || email.getTextContent().isEmpty())) {
            throw new MailProviderException("Email must have either HTML or text content");
        }

        try {
            log.info("Sending email via SMTP2GO to: {}", email.getToEmail());

            Map<String, Object> request = buildSmtp2GoRequest(email);

            Smtp2GoResponse response = webClient.post()
                    .uri("/email/send")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Smtp2GoResponse.class)
                    .block();

            if (response != null && response.getData() != null 
                    && Boolean.TRUE.equals(response.getData().getSucceeded())) {
                String messageId = response.getData().getMessageId();
                if (messageId == null) {
                    messageId = response.getRequestId();
                }
                if (messageId == null) {
                    messageId = "UNKNOWN";
                }
                log.info("Email sent successfully via SMTP2GO. Message ID: {}", messageId);
                return messageId;
            } else {
                throw new MailProviderException("No success status received from SMTP2GO or send failed");
            }
        } catch (WebClientResponseException e) {
            log.error("Error sending email via SMTP2GO: {}", e.getResponseBodyAsString(), e);
            throw new MailProviderException("SMTP2GO API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email via SMTP2GO", e);
            throw new MailProviderException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildSmtp2GoRequest(Email email) {
        Map<String, Object> request = new HashMap<>();

        // Sender with name and email in format "Name <email>"
        String sender = formatEmailAddress(email.getFromName(), email.getFromEmail());
        request.put("sender", sender);

        // Recipient with name and email in format "Name <email>"
        String recipient = formatEmailAddress(email.getToName(), email.getToEmail());
        request.put("to", List.of(recipient));

        // Subject
        request.put("subject", email.getSubject());

        // HTML content (if present)
        if (email.getHtmlContent() != null && !email.getHtmlContent().isEmpty()) {
            request.put("html_body", email.getHtmlContent());
        }

        // Text content (if present)
        if (email.getTextContent() != null && !email.getTextContent().isEmpty()) {
            request.put("text_body", email.getTextContent());
        }

        return request;
    }

    private String formatEmailAddress(String name, String email) {
        if (name != null && !name.isEmpty()) {
            return name + " <" + email + ">";
        }
        return email;
    }

    @Override
    public String getProviderName() {
        return "SMTP2GO";
    }
}
