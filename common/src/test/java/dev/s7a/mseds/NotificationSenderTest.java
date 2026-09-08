package dev.s7a.mseds;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class NotificationSenderTest {
    private static final String APPENDER_NAME = "MinecraftServerExceptionDiscordSender";

    private static Throwable failure(String message) {
        Throwable failure = new IllegalStateException(message);
        failure.setStackTrace(new StackTraceElement[]{new StackTraceElement("example.Server", "tick", "Server.java", 42)});
        return failure;
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (!condition.getAsBoolean() && System.nanoTime() < end) Thread.sleep(5);
        assertTrue(condition.getAsBoolean(), "condition did not become true");
    }

    private static Appender appender() {
        return LoggerContext.getContext(false).getConfiguration().getRootLogger().getAppenders().get(APPENDER_NAME);
    }

    @Test void loggingThreadReturnsWhileWebhookIsBlockedAndBurstCannotFillUnboundedMemory() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.blocking = true;
        List<String> diagnostics = new CopyOnWriteArrayList<>();
        ExecutorService callers = Executors.newFixedThreadPool(4, task -> new Thread(task, "Netty-test-caller"));
        try (MinecraftServerExceptionDiscordSender sender = new MinecraftServerExceptionDiscordSender(transport, diagnostics::add)) {
            sender.setup();
            Future<?> first = callers.submit(() -> appender().append(Log4jLogEvent.newBuilder().setThrown(failure("first")).build()));
            first.get(1, TimeUnit.SECONDS);
            assertTrue(transport.started.await(1, TimeUnit.SECONDS));
            List<Future<?>> burst = new ArrayList<>();
            for (int i = 0; i < 256; i++) {
                int id = i;
                burst.add(callers.submit(() -> appender().append(Log4jLogEvent.newBuilder().setThrown(failure("burst " + id)).build())));
            }
            for (Future<?> future : burst) future.get(1, TimeUnit.SECONDS);
            assertEquals(128, sender.droppedNotifications());
            assertEquals(1, transport.messages.size());
            assertTrue(transport.threads.get(0).endsWith("-worker"));
            assertFalse(transport.threads.contains("Netty-test-caller"));
            transport.release.countDown();
            await(() -> transport.messages.size() == 129);
            await(() -> diagnostics.stream().anyMatch(value -> value.contains("dropped=128")));
        } finally {
            callers.shutdownNow();
        }
    }

    @Test void sameExceptionIsAggregatedForSixtySecondsAndThenSentAgain() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        AtomicLong clock = new AtomicLong(1);
        try (MinecraftServerExceptionDiscordSender sender = new MinecraftServerExceptionDiscordSender(transport, ignored -> {}, clock::get)) {
            sender.setup();
            sender.enqueue(failure("same"));
            await(() -> transport.messages.size() == 1);
            clock.addAndGet(TimeUnit.SECONDS.toNanos(59));
            for (int i = 0; i < 20; i++) sender.enqueue(failure("same"));
            assertEquals(20, sender.suppressedNotifications());
            clock.addAndGet(TimeUnit.SECONDS.toNanos(1));
            sender.enqueue(failure("same"));
            await(() -> transport.messages.size() == 2);
        }
    }

    @Test void contentIsBoundedAndIsAnImmutableSnapshot() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.blocking = true;
        try (MinecraftServerExceptionDiscordSender sender = new MinecraftServerExceptionDiscordSender(transport, ignored -> {})) {
            sender.setup();
            sender.enqueue(failure("blocker"));
            assertTrue(transport.started.await(1, TimeUnit.SECONDS));
            Throwable snapshot = failure("before mutation");
            sender.enqueue(snapshot);
            snapshot.setStackTrace(new StackTraceElement[]{new StackTraceElement("Changed", "after", "After.java", 9)});
            sender.enqueue(failure(new String(new char[10000]).replace('\0', 'x')));
            transport.release.countDown();
            await(() -> transport.messages.size() == 3);
            assertTrue(transport.messages.get(1).contains("example.Server.tick"));
            assertFalse(transport.messages.get(1).contains("Changed"));
            assertEquals(2000, transport.messages.get(2).length());
            assertTrue(transport.messages.get(2).endsWith("..."));
        }
    }

    @Test void retryAfterIsRespectedAndPersistent429RetriesOnlyOncePerNotification() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.response = ignored -> new WebhookResponse(429, TimeUnit.MILLISECONDS.toNanos(100));
        try (MinecraftServerExceptionDiscordSender sender = new MinecraftServerExceptionDiscordSender(transport, ignored -> {})) {
            sender.setup();
            sender.enqueue(failure("rate limit first"));
            sender.enqueue(failure("rate limit second"));
            await(() -> sender.failedNotifications() == 2);
            assertEquals(4, transport.messages.size());
            for (int i = 1; i < transport.times.size(); i++) {
                assertTrue(transport.times.get(i) - transport.times.get(i - 1) >= TimeUnit.MILLISECONDS.toNanos(90));
            }
        }
    }

    @Test void notificationFailureDoesNotKillWorkerOrExposeSecrets() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.response = count -> {
            if (count == 1) throw new IllegalStateException("secret https://discord.invalid/webhooks/private-token");
            return new WebhookResponse(204, 0);
        };
        List<String> diagnostics = new CopyOnWriteArrayList<>();
        try (MinecraftServerExceptionDiscordSender sender = new MinecraftServerExceptionDiscordSender(transport, diagnostics::add)) {
            sender.setup();
            sender.enqueue(failure("first"));
            sender.enqueue(failure("second"));
            await(() -> transport.messages.size() == 2);
            await(() -> !diagnostics.isEmpty());
            assertEquals(1, sender.failedNotifications());
            assertFalse(diagnostics.toString().contains("private-token"));
            assertFalse(diagnostics.toString().contains("https://"));
        }
    }

    @Test void setupIsIdempotentAndCloseCancelsPendingWorkAndUnregistersOnlyItsAppender() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.blocking = true;
        MinecraftServerExceptionDiscordSender sender = new MinecraftServerExceptionDiscordSender(transport, ignored -> {});
        try {
            sender.setup();
            Appender first = appender();
            sender.setup();
            assertSame(first, appender());
            sender.enqueue(failure("blocked"));
            assertTrue(transport.started.await(1, TimeUnit.SECONDS));
            sender.enqueue(failure("must be discarded"));
            assertTimeoutPreemptively(Duration.ofSeconds(3), sender::close);
            assertNull(appender());
            sender.close();
            sender.enqueue(failure("after close"));
            assertEquals(1, transport.messages.size());
            assertTrue(transport.closed.get());
            assertThrows(IllegalStateException.class, sender::setup);
        } finally { sender.close(); }
        try (MinecraftServerExceptionDiscordSender replacement = new MinecraftServerExceptionDiscordSender(new RecordingTransport(), ignored -> {})) {
            replacement.setup();
            assertNotNull(appender());
        }
    }

    @Test void secondInstanceCannotReplaceActiveAppenderAndItsCloseLeavesFirstActive() {
        try (MinecraftServerExceptionDiscordSender first = new MinecraftServerExceptionDiscordSender(new RecordingTransport(), ignored -> {});
             MinecraftServerExceptionDiscordSender second = new MinecraftServerExceptionDiscordSender(new RecordingTransport(), ignored -> {})) {
            first.setup();
            Appender registered = appender();
            assertThrows(IllegalStateException.class, second::setup);
            second.close();
            assertSame(registered, appender());
        }
    }

    @Test void shutdownInterruptsLongRateLimitCooldown() throws Exception {
        RecordingTransport transport = new RecordingTransport();
        transport.response = ignored -> new WebhookResponse(429, TimeUnit.HOURS.toNanos(1));
        MinecraftServerExceptionDiscordSender sender = new MinecraftServerExceptionDiscordSender(transport, ignored -> {});
        try {
            sender.setup();
            sender.enqueue(failure("rate limited"));
            assertTrue(transport.started.await(1, TimeUnit.SECONDS));
            assertTimeoutPreemptively(Duration.ofSeconds(3), sender::close);
            assertEquals(1, transport.messages.size());
        } finally { sender.close(); }
    }

    @Test void invalidUrlDoesNotExposeItsValue() {
        InvalidWebhookUrlException error = assertThrows(InvalidWebhookUrlException.class,
                () -> new MinecraftServerExceptionDiscordSender("bad://secret-token"));
        assertFalse(error.toString().contains("secret-token"));
        assertNull(error.getCause());
    }
}
