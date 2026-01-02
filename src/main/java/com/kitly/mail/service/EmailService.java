package com.kitly.mail.service;

import com.kitly.mail.model.Email;
import com.kitly.mail.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailRepository emailRepository;
    private final MailProvider mailProvider;

    @Transactional
    public Email sendEmail(Email email) {
        email.setStatus(Email.EmailStatus.PENDING);
        email = emailRepository.save(email);

        try {
            String externalId = mailProvider.sendEmail(email);
            email.setExternalId(externalId);
            email.setStatus(Email.EmailStatus.SENT);
            email.setSentAt(LocalDateTime.now());
            log.info("Email sent successfully. ID: {}, External ID: {}", email.getId(), externalId);
        } catch (MailProviderException e) {
            email.setStatus(Email.EmailStatus.FAILED);
            email.setErrorMessage(e.getMessage());
            log.error("Failed to send email. ID: {}", email.getId(), e);
        }

        return emailRepository.save(email);
    }

    public Email getEmailById(Long id) {
        return emailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Email not found with id: " + id));
    }

    public List<Email> getAllEmails() {
        return emailRepository.findAll();
    }

    public List<Email> getEmailsByStatus(Email.EmailStatus status) {
        return emailRepository.findByStatus(status);
    }

    public List<Email> getEmailsByRecipient(String toEmail) {
        return emailRepository.findByToEmail(toEmail);
    }
}
