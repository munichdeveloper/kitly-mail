package com.kitly.mail.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTest {

    @Test
    void testEmailBuilder() {
        Email email = Email.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender Name")
                .toEmail("recipient@example.com")
                .toName("Recipient Name")
                .subject("Test Subject")
                .htmlContent("<h1>Test</h1>")
                .textContent("Test")
                .status(Email.EmailStatus.PENDING)
                .build();

        assertThat(email.getFromEmail()).isEqualTo("sender@example.com");
        assertThat(email.getFromName()).isEqualTo("Sender Name");
        assertThat(email.getToEmail()).isEqualTo("recipient@example.com");
        assertThat(email.getToName()).isEqualTo("Recipient Name");
        assertThat(email.getSubject()).isEqualTo("Test Subject");
        assertThat(email.getHtmlContent()).isEqualTo("<h1>Test</h1>");
        assertThat(email.getTextContent()).isEqualTo("Test");
        assertThat(email.getStatus()).isEqualTo(Email.EmailStatus.PENDING);
    }

    @Test
    void testPrePersist() {
        Email email = new Email();
        email.onCreate();

        assertThat(email.getCreatedAt()).isNotNull();
        assertThat(email.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(email.getStatus()).isEqualTo(Email.EmailStatus.PENDING);
    }

    @Test
    void testPrePersistDoesNotOverrideStatus() {
        Email email = new Email();
        email.setStatus(Email.EmailStatus.SENT);
        email.onCreate();

        assertThat(email.getStatus()).isEqualTo(Email.EmailStatus.SENT);
    }
}
