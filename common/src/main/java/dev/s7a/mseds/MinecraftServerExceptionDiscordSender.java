package dev.s7a.mseds;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public class MinecraftServerExceptionDiscordSender implements AutoCloseable {
    private static final String SENDER_NAME = "MinecraftServerExceptionDiscordSender";
    private static final int QUEUE_CAPACITY = 128;
    private static final int RECENT_CAPACITY = 1024;
    private static final long DEDUPLICATION_NANOS = TimeUnit.SECONDS.toNanos(60);
    private static final long DIAGNOSTIC_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(60);

    private final WebhookTransport transport;
    private final Consumer<String> diagnostic;
    private final LongSupplier clock;
    private final ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final Map<String, Long> recent = new LinkedHashMap<>();
    private final List<LoggerConfig> registrations = new ArrayList<>();
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong suppressed = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private volatile boolean running;
    private boolean closed;
    private ExecutorService worker;
    private Appender appender;
    private long nextAttemptNanos;
    private boolean coolingDown;
    private long lastDiagnosticNanos;
    private long reportedDropped;
    private long reportedSuppressed;
    private long reportedFailed;

    public MinecraftServerExceptionDiscordSender(@NotNull String url) throws InvalidWebhookUrlException {
        this(createTransport(url), System.err::println);
    }

    MinecraftServerExceptionDiscordSender(WebhookTransport transport, Consumer<String> diagnostic) {
        this(transport, diagnostic, System::nanoTime);
    }

    MinecraftServerExceptionDiscordSender(WebhookTransport transport, Consumer<String> diagnostic, LongSupplier clock) {
        this.transport = transport;
        this.diagnostic = diagnostic;
        this.clock = clock;
    }

    private static WebhookTransport createTransport(String value) {
        try {
            URL url = new URL(value);
            if (!"https".equalsIgnoreCase(url.getProtocol())) {
                throw new InvalidWebhookUrlException("webhook_url must use HTTPS");
            }
            return new HttpWebhookTransport(url);
        } catch (MalformedURLException e) {
            // hide webhook URL
            throw new InvalidWebhookUrlException("webhook_url is not a valid HTTPS URL");
        }
    }

    public synchronized void setup() {
        if (closed) throw new IllegalStateException("The notification sender is closed");
        if (running) return;
        Configuration configuration = LoggerContext.getContext(false).getConfiguration();
        registrations.addAll(configuration.getLoggers().values());
        if (!registrations.contains(configuration.getRootLogger())) {
            registrations.add(configuration.getRootLogger());
        }
        for (LoggerConfig logger : registrations) {
            if (logger.getAppenders().containsKey(SENDER_NAME)) {
                registrations.clear();
                throw new IllegalStateException("An exception notification sender is already registered");
            }
        }
        appender = new AbstractAppender(SENDER_NAME, null, null, true, null) {
            @Override
            public void append(LogEvent event) {
                Throwable thrown = event.getThrown();
                if (thrown != null) enqueue(thrown);
            }
        };
        worker = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, SENDER_NAME + "-worker");
            thread.setDaemon(true);
            return thread;
        });
        running = true;
        try {
            worker.execute(this::runWorker);
            appender.start();
            for (LoggerConfig logger : registrations) logger.addAppender(appender, null, null);
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    void enqueue(Throwable throwable) {
        if (!running) return;
        String content = BoundedStackTrace.format(throwable);
        if (content.contains(SENDER_NAME)) return;
        long now = clock.getAsLong();
        synchronized (recent) {
            if (!running) return;
            Long previous = recent.get(content);
            if (previous != null && now - previous < DEDUPLICATION_NANOS) {
                suppressed.incrementAndGet();
                return;
            }
            if (!queue.offer(content)) {
                dropped.incrementAndGet();
                return;
            }
            recent.remove(content);
            recent.put(content, now);
            if (recent.size() > RECENT_CAPACITY) recent.remove(recent.keySet().iterator().next());
        }
    }

    private void runWorker() {
        try {
            while (running) {
                String content = queue.poll(1, TimeUnit.SECONDS);
                if (content != null && running) deliver(content);
                reportCounters();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void deliver(String content) throws InterruptedException {
        for (int attempt = 0; attempt < 2 && running; attempt++) {
            awaitCooldown();
            if (!running) return;
            try {
                WebhookResponse response = transport.send(content);
                if (response.getStatus() >= 200 && response.getStatus() < 300) return;
                if (response.getStatus() == 429) {
                    nextAttemptNanos = System.nanoTime() + response.getRetryAfterNanos();
                    coolingDown = true;
                    if (attempt == 0) continue;
                }
                failed.incrementAndGet();
                return;
            } catch (IOException | RuntimeException ignored) {
                // hide webhook URL
                if (running) failed.incrementAndGet();
                return;
            }
        }
    }

    private void awaitCooldown() throws InterruptedException {
        long remaining;
        while (running && coolingDown && (remaining = nextAttemptNanos - System.nanoTime()) > 0) {
            TimeUnit.NANOSECONDS.sleep(remaining);
        }
        coolingDown = false;
    }

    private void reportCounters() {
        long now = System.nanoTime();
        long droppedCount = dropped.get();
        long suppressedCount = suppressed.get();
        long failedCount = failed.get();
        if (droppedCount == reportedDropped && suppressedCount == reportedSuppressed && failedCount == reportedFailed) return;
        if (lastDiagnosticNanos != 0 && now - lastDiagnosticNanos < DIAGNOSTIC_INTERVAL_NANOS) return;
        lastDiagnosticNanos = now;
        reportedDropped = droppedCount;
        reportedSuppressed = suppressedCount;
        reportedFailed = failedCount;
        try {
            // avoid recursive notifications
            diagnostic.accept(SENDER_NAME + ": notifications dropped=" + droppedCount
                    + ", duplicates=" + suppressedCount + ", failed=" + failedCount);
        } catch (RuntimeException ignored) {
            // ignore diagnostic errors
        }
    }

    long droppedNotifications() { return dropped.get(); }
    long suppressedNotifications() { return suppressed.get(); }
    long failedNotifications() { return failed.get(); }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        running = false;
        for (LoggerConfig logger : registrations) {
            if (logger.getAppenders().get(SENDER_NAME) == appender) logger.removeAppender(SENDER_NAME);
        }
        registrations.clear();
        if (appender != null) appender.stop();
        synchronized (recent) {
            queue.clear();
            recent.clear();
        }
        if (worker != null) worker.shutdownNow();
        transport.close();
        if (worker != null) {
            try {
                worker.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        reportCounters();
    }
}
