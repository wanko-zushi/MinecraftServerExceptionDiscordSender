package dev.s7a.mseds;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

final class RecordingTransport implements WebhookTransport {
    final List<String> messages = new CopyOnWriteArrayList<>();
    final List<String> threads = new CopyOnWriteArrayList<>();
    final List<Long> times = new CopyOnWriteArrayList<>();
    final CountDownLatch started = new CountDownLatch(1);
    final CountDownLatch release = new CountDownLatch(1);
    final AtomicBoolean closed = new AtomicBoolean();
    volatile boolean blocking;
    volatile Function<Integer, WebhookResponse> response = ignored -> new WebhookResponse(204, 0);

    @Override
    public WebhookResponse send(String content) throws IOException {
        threads.add(Thread.currentThread().getName());
        times.add(System.nanoTime());
        messages.add(content);
        started.countDown();
        if (blocking) {
            try {
                if (!release.await(10, TimeUnit.SECONDS)) throw new IOException("mock server timed out");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("mock request cancelled");
            }
        }
        return response.apply(messages.size());
    }

    @Override public void close() { closed.set(true); release.countDown(); }
}
