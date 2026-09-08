package dev.s7a.mseds;

final class TraceLimitReached extends RuntimeException {
    static final TraceLimitReached INSTANCE = new TraceLimitReached();

    private TraceLimitReached() { super(null, null, false, false); }
}
