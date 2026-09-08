package dev.s7a.mseds;

import java.io.PrintWriter;
import java.io.Writer;

/** Stops stack-trace formatting as soon as Discord's content limit is reached. */
final class BoundedStackTrace extends Writer {
    private static final int CONTENT_LIMIT = 2000;
    private final StringBuilder text = new StringBuilder();

    static String format(Throwable throwable) {
        BoundedStackTrace writer = new BoundedStackTrace();
        try {
            throwable.printStackTrace(new PrintWriter(writer));
            return writer.text.toString();
        } catch (TraceLimitReached ignored) {
            int end = CONTENT_LIMIT - 3;
            if (Character.isHighSurrogate(writer.text.charAt(end - 1))) end--;
            return writer.text.substring(0, end) + "...";
        }
    }

    @Override
    public void write(char[] value, int offset, int length) {
        int accepted = Math.min(length, CONTENT_LIMIT - text.length());
        text.append(value, offset, accepted);
        if (accepted < length) throw TraceLimitReached.INSTANCE;
    }

    @Override public void flush() { }
    @Override public void close() { }
}
