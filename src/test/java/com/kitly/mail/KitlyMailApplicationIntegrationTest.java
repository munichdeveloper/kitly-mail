package com.kitly.mail;

import com.kitly.mail.controller.EmailRequest;
import com.kitly.mail.controller.EmailResponse;
import com.kitly.mail.model.Email;
import com.kitly.mail.repository.EmailRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class KitlyMailApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        emailRepository.deleteAll();
        mockWebServer = new MockWebServer();
        mockWebServer.start(8888);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testSendEmailEndToEnd() throws Exception {
        // Mock BREVO API response
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"messageId\":\"msg-123\"}")
                .addHeader("Content-Type", "application/json"));

        EmailRequest request = EmailRequest.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender Name")
                .toEmail("recipient@example.com")
                .toName("Recipient Name")
                .subject("Integration Test Email")
                .htmlContent("<h1>Test Content</h1>")
                .textContent("Test Content")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<EmailResponse> response = restTemplate
                .withBasicAuth("testuser", "testpass")
                .postForEntity("/api/emails", entity, EmailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("SENT");
        assertThat(response.getBody().getExternalId()).isEqualTo("msg-123");

        // Verify data in database
        List<Email> emails = emailRepository.findAll();
        assertThat(emails).hasSize(1);
        assertThat(emails.get(0).getStatus()).isEqualTo(Email.EmailStatus.SENT);
    }

    @Test
    void testGetAllEmailsEndToEnd() {
        Email email = Email.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender")
                .toEmail("recipient@example.com")
                .toName("Recipient")
                .subject("Test Subject")
                .status(Email.EmailStatus.SENT)
                .build();
        emailRepository.save(email);

        ResponseEntity<EmailResponse[]> response = restTemplate
                .withBasicAuth("testuser", "testpass")
                .getForEntity("/api/emails", EmailResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);
    }

    @Test
    void testUnauthorizedAccess() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/emails", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
