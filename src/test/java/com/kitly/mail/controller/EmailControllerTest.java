package com.kitly.mail.controller;

import com.kitly.mail.model.Email;
import com.kitly.mail.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailController.class)
@ActiveProfiles("test")
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    @Test
    @WithMockUser
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.fromEmail").value("sender@example.com"));
    }

    @Test
    @WithMockUser
    void testSendEmailValidationError() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .fromEmail("invalid-email")
                .fromName("Sender")
                .toEmail("recipient@example.com")
                .toName("Recipient")
                .subject("Test Subject")
                .build();

        mockMvc.perform(post("/api/emails")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
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
    @WithMockUser
    void testGetAllEmails() throws Exception {
        List<Email> emails = Arrays.asList(createTestEmail(), createTestEmail());
        when(emailService.getAllEmails()).thenReturn(emails);

        mockMvc.perform(get("/api/emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void testGetEmailsByStatus() throws Exception {
        List<Email> emails = Arrays.asList(createTestEmail());
        when(emailService.getEmailsByStatus(Email.EmailStatus.SENT)).thenReturn(emails);

        mockMvc.perform(get("/api/emails")
                        .param("status", "SENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void testGetEmailsByRecipient() throws Exception {
        List<Email> emails = Arrays.asList(createTestEmail());
        when(emailService.getEmailsByRecipient("test@example.com")).thenReturn(emails);

        mockMvc.perform(get("/api/emails")
                        .param("recipient", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/emails"))
                .andExpect(status().isUnauthorized());
    }

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
