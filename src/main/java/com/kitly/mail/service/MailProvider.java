package com.kitly.mail.service;

import com.kitly.mail.model.Email;

/**
 * Mail provider interface for different email service implementations.
 * This allows for easy swapping between providers like BREVO, MAILGUN, etc.
 */
public interface MailProvider {

    /**
     * Send an email using the provider's API.
     *
     * @param email The email to send
     * @return The external ID assigned by the provider
     * @throws MailProviderException if sending fails
     */
    String sendEmail(Email email) throws MailProviderException;

    /**
     * Get the name of this provider.
     *
     * @return The provider name
     */
    String getProviderName();
}
