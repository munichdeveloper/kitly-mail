package com.kitly.mail.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitly.mail.config.TestSecurityConfig;
import com.kitly.mail.filter.ApiKeyAuthFilter;
import com.kitly.mail.model.Email;
import com.kitly.mail.service.EmailService;
import com.kitly.mail.service.MailProviderException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmailController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class EmailControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private ApiKeyAuthFilter apiKeyAuthFilter;

    @Test
    void testSendEmail() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender")
                .toEmail("recipient@example.com")
                .toName("Recipient")
                .subject("Test Subject")
                .htmlContent("<h1>Test</h1>")
                .build();

        Email savedEmail = createTestEmail();
        savedEmail.setId(1L);
        savedEmail.setStatus(Email.EmailStatus.SENT);

        when(emailService.sendEmail(any(Email.class))).thenReturn(savedEmail);

        mockMvc.perform(post("/api/emails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.fromEmail").value("sender@example.com"));
    }

    @Test
    void testSendEmailValidationError() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .fromEmail("invalid-email")
                .fromName("Sender")
                .toEmail("recipient@example.com")
                .toName("Recipient")
                .subject("Test Subject")
                .build();

        mockMvc.perform(post("/api/emails")
                        .header("X-API-Key", "test-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSendEmailProviderFailure() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender")
                .toEmail("recipient@example.com")
                .toName("Recipient")
                .subject("Test Subject")
                .htmlContent("<h1>Test</h1>")
                .build();

        when(emailService.sendEmail(any(Email.class)))
                .thenThrow(new MailProviderException("Provider error"));

        mockMvc.perform(post("/api/emails")
                        .header("X-API-Key", "test-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetEmailById() throws Exception {
        Email email = createTestEmail();
        email.setId(1L);

        when(emailService.getEmailById(1L)).thenReturn(email);

        mockMvc.perform(get("/api/emails/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromEmail").value("sender@example.com"));
    }

    @Test
    void testGetAllEmails() throws Exception {
        Email email1 = createTestEmail();
        email1.setId(1L);
        Email email2 = createTestEmail();
        email2.setId(2L);
        List<Email> emails = Arrays.asList(email1, email2);
        when(emailService.getAllEmails()).thenReturn(emails);

        mockMvc.perform(get("/api/emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetEmailsByStatus() throws Exception {
        Email email = createTestEmail();
        email.setId(1L);
        email.setStatus(Email.EmailStatus.SENT);
        List<Email> emails = Arrays.asList(email);
        when(emailService.getEmailsByStatus(Email.EmailStatus.SENT)).thenReturn(emails);

        mockMvc.perform(get("/api/emails")
                        .param("status", "SENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetEmailsByRecipient() throws Exception {
        Email email = createTestEmail();
        email.setId(1L);
        List<Email> emails = Arrays.asList(email);
        when(emailService.getEmailsByRecipient("test@example.com")).thenReturn(emails);

        mockMvc.perform(get("/api/emails")
                        .param("recipient", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // Note: Unauthorized access should be tested in integration tests
    // since we've disabled security filters for unit tests

    private Email createTestEmail() {
        return Email.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender")
                .toEmail("recipient@example.com")
                .toName("Recipient")
                .subject("Test Subject")
                .htmlContent("<h1>Test</h1>")
                .status(Email.EmailStatus.PENDING)
                .build();
    }
}
