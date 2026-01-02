package com.kitly.mail.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "From email is required")
    @Email(message = "From email must be valid")
    private String fromEmail;

    @NotBlank(message = "From name is required")
    private String fromName;

    @NotBlank(message = "To email is required")
    @Email(message = "To email must be valid")
    private String toEmail;

    @NotBlank(message = "To name is required")
    private String toName;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String htmlContent;

    private String textContent;
}
