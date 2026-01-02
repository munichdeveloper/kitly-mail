package com.kitly.mail.service;

import com.kitly.mail.model.Email;
import com.kitly.mail.repository.EmailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private MailProvider mailProvider;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(emailRepository, mailProvider);
    }

    @Test
    void testSendEmailSuccess() throws MailProviderException {
        Email email = createTestEmail();
        when(mailProvider.sendEmail(any(Email.class))).thenReturn("external-id-123");
        when(emailRepository.save(any(Email.class))).thenAnswer(i -> i.getArgument(0));

        Email result = emailService.sendEmail(email);

        assertThat(result.getStatus()).isEqualTo(Email.EmailStatus.SENT);
        assertThat(result.getExternalId()).isEqualTo("external-id-123");
        assertThat(result.getSentAt()).isNotNull();
        verify(emailRepository, times(2)).save(any(Email.class));
    }

    @Test
    void testSendEmailFailure() throws MailProviderException {
        Email email = createTestEmail();
        when(mailProvider.sendEmail(any(Email.class)))
                .thenThrow(new MailProviderException("Provider error"));
        when(emailRepository.save(any(Email.class))).thenAnswer(i -> i.getArgument(0));

        Email result = emailService.sendEmail(email);

        assertThat(result.getStatus()).isEqualTo(Email.EmailStatus.FAILED);
        assertThat(result.getErrorMessage()).contains("Provider error");
        assertThat(result.getSentAt()).isNull();
        verify(emailRepository, times(2)).save(any(Email.class));
    }

    @Test
    void testGetEmailById() {
        Email email = createTestEmail();
        email.setId(1L);
        when(emailRepository.findById(1L)).thenReturn(Optional.of(email));

        Email result = emailService.getEmailById(1L);

        assertThat(result).isEqualTo(email);
        verify(emailRepository).findById(1L);
    }

    @Test
    void testGetEmailByIdNotFound() {
        when(emailRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailService.getEmailById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email not found");
    }

    @Test
    void testGetAllEmails() {
        List<Email> emails = Arrays.asList(createTestEmail(), createTestEmail());
        when(emailRepository.findAll()).thenReturn(emails);

        List<Email> result = emailService.getAllEmails();

        assertThat(result).hasSize(2);
        verify(emailRepository).findAll();
    }

    @Test
    void testGetEmailsByStatus() {
        List<Email> emails = Arrays.asList(createTestEmail());
        when(emailRepository.findByStatus(Email.EmailStatus.SENT)).thenReturn(emails);

        List<Email> result = emailService.getEmailsByStatus(Email.EmailStatus.SENT);

        assertThat(result).hasSize(1);
        verify(emailRepository).findByStatus(Email.EmailStatus.SENT);
    }

    @Test
    void testGetEmailsByRecipient() {
        List<Email> emails = Arrays.asList(createTestEmail());
        when(emailRepository.findByToEmail("test@example.com")).thenReturn(emails);

        List<Email> result = emailService.getEmailsByRecipient("test@example.com");

        assertThat(result).hasSize(1);
        verify(emailRepository).findByToEmail("test@example.com");
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
