package com.kitly.mail.repository;

import com.kitly.mail.model.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EmailRepositoryTest {

    @Autowired
    private EmailRepository emailRepository;

    @Test
    void testSaveAndFindEmail() {
        Email email = createTestEmail();
        Email saved = emailRepository.save(email);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFromEmail()).isEqualTo("sender@example.com");
    }

    @Test
    void testFindByStatus() {
        Email email1 = createTestEmail();
        email1.setStatus(Email.EmailStatus.SENT);
        emailRepository.save(email1);

        Email email2 = createTestEmail();
        email2.setStatus(Email.EmailStatus.FAILED);
        emailRepository.save(email2);

        List<Email> sentEmails = emailRepository.findByStatus(Email.EmailStatus.SENT);
        assertThat(sentEmails).hasSize(1);
        assertThat(sentEmails.get(0).getStatus()).isEqualTo(Email.EmailStatus.SENT);
    }

    @Test
    void testFindByToEmail() {
        Email email1 = createTestEmail();
        email1.setToEmail("user1@example.com");
        emailRepository.save(email1);

        Email email2 = createTestEmail();
        email2.setToEmail("user2@example.com");
        emailRepository.save(email2);

        Email email3 = createTestEmail();
        email3.setToEmail("user1@example.com");
        emailRepository.save(email3);

        List<Email> user1Emails = emailRepository.findByToEmail("user1@example.com");
        assertThat(user1Emails).hasSize(2);
    }

    private Email createTestEmail() {
        return Email.builder()
                .fromEmail("sender@example.com")
                .fromName("Sender")
                .toEmail("recipient@example.com")
                .toName("Recipient")
                .subject("Test")
                .status(Email.EmailStatus.PENDING)
                .build();
    }
}
