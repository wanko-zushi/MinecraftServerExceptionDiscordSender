package dev.s7a.mseds;

import java.io.IOException;

public class WebhookSendException extends RuntimeException {
    public WebhookSendException(IOException exception) {
        super(exception);
    }
}
