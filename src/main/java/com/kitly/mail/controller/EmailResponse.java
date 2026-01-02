package com.kitly.mail.controller;

import com.kitly.mail.model.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {

    private Long id;
    private String fromEmail;
    private String fromName;
    private String toEmail;
    private String toName;
    private String subject;
    private String status;
    private String externalId;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public static EmailResponse fromEmail(Email email) {
        return EmailResponse.builder()
                .id(email.getId())
                .fromEmail(email.getFromEmail())
                .fromName(email.getFromName())
                .toEmail(email.getToEmail())
                .toName(email.getToName())
                .subject(email.getSubject())
                .status(email.getStatus().name())
                .externalId(email.getExternalId())
                .errorMessage(email.getErrorMessage())
                .createdAt(email.getCreatedAt())
                .sentAt(email.getSentAt())
                .build();
    }
}
