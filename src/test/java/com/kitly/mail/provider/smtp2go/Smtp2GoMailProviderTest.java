package com.kitly.mail.provider.smtp2go;

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

class Smtp2GoMailProviderTest {

    private MockWebServer mockWebServer;
    private Smtp2GoMailProvider smtp2GoMailProvider;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        smtp2GoMailProvider = new Smtp2GoMailProvider("test-api-key", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testSendEmailSuccess() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"request_id\":\"e8ba23d0-e21e-11ed-8b17-d692668ad678\"," +
                        "\"data\":{\"succeeded\":true,\"message_id\":\"msg-123\"}}")
                .addHeader("Content-Type", "application/json"));

        Email email = createTestEmail();
        String messageId = smtp2GoMailProvider.sendEmail(email);

        assertThat(messageId).isEqualTo("msg-123");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/email/send");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("X-Smtp2go-Api-Key")).isEqualTo("test-api-key");
    }

    @Test
    void testSendEmailApiError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"error\":\"invalid_parameter\",\"message\":\"Invalid email\"}")
                .addHeader("Content-Type", "application/json"));

        Email email = createTestEmail();

        assertThatThrownBy(() -> smtp2GoMailProvider.sendEmail(email))
                .isInstanceOf(MailProviderException.class)
                .hasMessageContaining("SMTP2GO API error");
    }

    @Test
    void testSendEmailNoMessageId() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"request_id\":\"test-id\",\"data\":{\"succeeded\":true}}")
                .addHeader("Content-Type", "application/json"));

        Email email = createTestEmail();

        assertThatThrownBy(() -> smtp2GoMailProvider.sendEmail(email))
                .isInstanceOf(MailProviderException.class)
                .hasMessageContaining("No message ID");
    }

    @Test
    void testSendEmailFailedSend() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"request_id\":\"test-id\",\"data\":{\"succeeded\":false,\"message_id\":\"msg-123\"}}")
                .addHeader("Content-Type", "application/json"));

        Email email = createTestEmail();

        assertThatThrownBy(() -> smtp2GoMailProvider.sendEmail(email))
                .isInstanceOf(MailProviderException.class)
                .hasMessageContaining("send failed");
    }

    @Test
    void testSendEmailWithHtmlAndTextContent() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"request_id\":\"test-id\"," +
                        "\"data\":{\"succeeded\":true,\"message_id\":\"msg-456\"}}")
                .addHeader("Content-Type", "application/json"));

        Email email = Email.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender Name")
                .toEmail("recipient@example.com")
                .toName("Recipient Name")
                .subject("Test Subject")
                .htmlContent("<h1>Test Email</h1>")
                .textContent("Test Email")
                .build();

        String messageId = smtp2GoMailProvider.sendEmail(email);

        assertThat(messageId).isEqualTo("msg-456");

        RecordedRequest request = mockWebServer.takeRequest();
        String requestBody = request.getBody().readUtf8();
        assertThat(requestBody).contains("html_body");
        assertThat(requestBody).contains("text_body");
    }

    @Test
    void testSendEmailWithHtmlContentOnly() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"request_id\":\"test-id\"," +
                        "\"data\":{\"succeeded\":true,\"message_id\":\"msg-789\"}}")
                .addHeader("Content-Type", "application/json"));

        Email email = Email.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender Name")
                .toEmail("recipient@example.com")
                .toName("Recipient Name")
                .subject("Test Subject")
                .htmlContent("<h1>Test Email</h1>")
                .build();

        String messageId = smtp2GoMailProvider.sendEmail(email);

        assertThat(messageId).isEqualTo("msg-789");

        RecordedRequest request = mockWebServer.takeRequest();
        String requestBody = request.getBody().readUtf8();
        assertThat(requestBody).contains("html_body");
        assertThat(requestBody).doesNotContain("text_body");
    }

    @Test
    void testGetProviderName() {
        assertThat(smtp2GoMailProvider.getProviderName()).isEqualTo("SMTP2GO");
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
