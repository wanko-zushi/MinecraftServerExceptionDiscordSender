package dev.s7a.mseds;

import java.io.IOException;

interface WebhookTransport extends AutoCloseable {
    WebhookResponse send(String content) throws IOException;
    @Override void close();
}
