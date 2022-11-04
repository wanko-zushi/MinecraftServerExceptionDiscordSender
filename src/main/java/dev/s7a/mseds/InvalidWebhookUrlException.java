package dev.s7a.mseds;

public class InvalidWebhookUrlException extends IllegalArgumentException {
    public InvalidWebhookUrlException(Throwable cause) {
        super(cause);
    }
}
