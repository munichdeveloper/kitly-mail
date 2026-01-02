package com.kitly.mail.provider.brevo;

import com.kitly.mail.model.Email;
import com.kitly.mail.service.MailProviderException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrevoMailProviderTest {

    private MockWebServer mockWebServer;
    private BrevoMailProvider brevoMailProvider;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        brevoMailProvider = new BrevoMailProvider("test-api-key", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testSendEmailSuccess() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"messageId\":\"msg-123\"}")
                .addHeader("Content-Type", "application/json"));

        Email email = createTestEmail();
        String messageId = brevoMailProvider.sendEmail(email);

        assertThat(messageId).isEqualTo("msg-123");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/smtp/email");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("api-key")).isEqualTo("test-api-key");
    }

    @Test
    void testSendEmailApiError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"code\":\"invalid_parameter\",\"message\":\"Invalid email\"}")
                .addHeader("Content-Type", "application/json"));

        Email email = createTestEmail();

        assertThatThrownBy(() -> brevoMailProvider.sendEmail(email))
                .isInstanceOf(MailProviderException.class)
                .hasMessageContaining("BREVO API error");
    }

    @Test
    void testSendEmailNoMessageId() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        Email email = createTestEmail();

        assertThatThrownBy(() -> brevoMailProvider.sendEmail(email))
                .isInstanceOf(MailProviderException.class)
                .hasMessageContaining("No message ID");
    }

    @Test
    void testGetProviderName() {
        assertThat(brevoMailProvider.getProviderName()).isEqualTo("BREVO");
    }

    private Email createTestEmail() {
        return Email.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender Name")
                .toEmail("recipient@example.com")
                .toName("Recipient Name")
                .subject("Test Subject")
                .htmlContent("<h1>Test Email</h1>")
                .textContent("Test Email")
                .build();
    }
}
