package com.kitly.mail.service;

public class MailProviderException extends Exception {

    public MailProviderException(String message) {
        super(message);
    }

    public MailProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
