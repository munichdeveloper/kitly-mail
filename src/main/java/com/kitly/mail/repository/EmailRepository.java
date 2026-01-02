package com.kitly.mail.repository;

import com.kitly.mail.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    
    List<Email> findByStatus(Email.EmailStatus status);
    
    List<Email> findByToEmail(String toEmail);
}
