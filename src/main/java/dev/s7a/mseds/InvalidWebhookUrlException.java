package dev.s7a.mseds;

public class InvalidWebhookUrlException extends IllegalArgumentException {
    public InvalidWebhookUrlException(String message) {
        super(message);
    }

    public InvalidWebhookUrlException(Throwable cause) {
        super(cause);
    }
}
