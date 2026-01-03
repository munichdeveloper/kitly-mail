package com.kitly.mail.controller;

import com.kitly.mail.model.Email;
import com.kitly.mail.service.EmailService;
import com.kitly.mail.service.MailProviderException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        log.info("Received request to send email to: {}", request.getToEmail());

        Email email = Email.builder()
                .fromEmail(request.getFromEmail())
                .fromName(request.getFromName())
                .toEmail(request.getToEmail())
                .toName(request.getToName())
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .textContent(request.getTextContent())
                .build();

        try {
            Email sentEmail = emailService.sendEmail(email);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(EmailResponse.fromEmail(sentEmail));
        } catch (MailProviderException e) {
            log.error("Failed to send email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailResponse> getEmail(@PathVariable Long id) {
        Email email = emailService.getEmailById(id);
        return ResponseEntity.ok(EmailResponse.fromEmail(email));
    }

    @GetMapping
    public ResponseEntity<List<EmailResponse>> getAllEmails(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String recipient) {

        List<Email> emails;

        if (status != null) {
            emails = emailService.getEmailsByStatus(Email.EmailStatus.valueOf(status.toUpperCase()));
        } else if (recipient != null) {
            emails = emailService.getEmailsByRecipient(recipient);
        } else {
            emails = emailService.getAllEmails();
        }

        List<EmailResponse> responses = emails.stream()
                .map(EmailResponse::fromEmail)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
