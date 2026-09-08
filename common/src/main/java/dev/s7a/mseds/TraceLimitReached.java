package dev.s7a.mseds;

/** Internal control flow: bounds formatting work as well as the retained string. */
final class TraceLimitReached extends RuntimeException {
    static final TraceLimitReached INSTANCE = new TraceLimitReached();

    private TraceLimitReached() { super(null, null, false, false); }
}
