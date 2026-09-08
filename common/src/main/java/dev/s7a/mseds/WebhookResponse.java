package dev.s7a.mseds;

final class WebhookResponse {
    private final int status;
    private final long retryAfterNanos;

    WebhookResponse(int status, long retryAfterNanos) {
        this.status = status;
        this.retryAfterNanos = retryAfterNanos;
    }

    int getStatus() { return status; }
    long getRetryAfterNanos() { return retryAfterNanos; }
}
