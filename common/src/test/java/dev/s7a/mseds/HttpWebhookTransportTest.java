package dev.s7a.mseds;

import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class HttpWebhookTransportTest {
    private static URL url(HttpServer server) throws Exception {
        return new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/webhook");
    }

    private static String read(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int count;
        while ((count = stream.read(bytes)) != -1) output.write(bytes, 0, count);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test void sendsUtf8JsonAndUsesNoRedirects() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> received = new AtomicReference<>();
        AtomicReference<String> contentType = new AtomicReference<>();
        server.createContext("/webhook", exchange -> {
            received.set(read(exchange.getRequestBody()));
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            exchange.getResponseHeaders().set("Location", "/must-not-follow");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
        try (HttpWebhookTransport transport = new HttpWebhookTransport(url(server))) {
            assertEquals(302, transport.send("例外の通知").getStatus());
            assertEquals("例外の通知", JsonParser.parseString(received.get()).getAsJsonObject().get("content").getAsString());
            assertEquals("application/json; charset=UTF-8", contentType.get());
        } finally { server.stop(0); }
    }

    @Test void readsDiscordRetryAfterFromReal429Response() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/webhook", exchange -> {
            read(exchange.getRequestBody());
            byte[] body = "{\"retry_after\":0.25}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Retry-After", "0.1");
            exchange.sendResponseHeaders(429, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try (HttpWebhookTransport transport = new HttpWebhookTransport(url(server))) {
            WebhookResponse response = transport.send("test");
            assertEquals(429, response.getStatus());
            assertEquals(TimeUnit.MILLISECONDS.toNanos(250), response.getRetryAfterNanos());
        } finally { server.stop(0); }
    }

    @Test void incomplete429BodyStillPreservesRetryAfterHeader() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/webhook", exchange -> {
            read(exchange.getRequestBody());
            exchange.getResponseHeaders().set("Retry-After", "0.15");
            exchange.sendResponseHeaders(429, 100);
            exchange.getResponseBody().write('{');
            exchange.close();
        });
        server.start();
        try (HttpWebhookTransport transport = new HttpWebhookTransport(url(server))) {
            assertEquals(TimeUnit.MILLISECONDS.toNanos(150), transport.send("incomplete").getRetryAfterNanos());
        } finally { server.stop(0); }
    }

    @Test void noResponseTimesOutWithoutHoldingTheLoggingCaller() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService handlers = Executors.newSingleThreadExecutor();
        server.setExecutor(handlers);
        server.createContext("/webhook", exchange -> {
            read(exchange.getRequestBody());
            entered.countDown();
            try { release.await(10, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            exchange.close();
        });
        server.start();
        try (MinecraftServerExceptionDiscordSender sender = new MinecraftServerExceptionDiscordSender(new HttpWebhookTransport(url(server)), ignored -> {})) {
            sender.setup();
            Throwable error = new IOException("slow webhook");
            error.setStackTrace(new StackTraceElement[0]);
            long start = System.nanoTime();
            sender.enqueue(error);
            assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(1));
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(7);
            while (sender.failedNotifications() == 0 && System.nanoTime() < deadline) Thread.sleep(20);
            assertEquals(1, sender.failedNotifications());
            assertTrue(System.nanoTime() - start >= TimeUnit.SECONDS.toNanos(4));
        } finally {
            release.countDown();
            server.stop(0);
            handlers.shutdownNow();
        }
    }

    @Test void closesConnectionDuringPendingResponse() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService tasks = Executors.newFixedThreadPool(3);
        server.setExecutor(tasks);
        server.createContext("/webhook", exchange -> {
            read(exchange.getRequestBody());
            entered.countDown();
            try { release.await(10, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            exchange.close();
        });
        server.start();
        HttpWebhookTransport transport = new HttpWebhookTransport(url(server));
        try {
            Future<?> send = tasks.submit(() -> assertThrows(IOException.class, () -> transport.send("cancel")));
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            Future<?> close = tasks.submit(transport::close);
            close.get(7, TimeUnit.SECONDS);
            send.get(2, TimeUnit.SECONDS);
            assertThrows(IOException.class, () -> transport.send("after close"));
        } finally {
            release.countDown();
            transport.close();
            server.stop(0);
            tasks.shutdownNow();
        }
    }

    @Test void connectionFailureIsReported() throws Exception {
        int port;
        try (ServerSocket reservation = new ServerSocket(0)) { port = reservation.getLocalPort(); }
        try (HttpWebhookTransport transport = new HttpWebhookTransport(new URL("http://127.0.0.1:" + port))) {
            assertThrows(IOException.class, () -> transport.send("refused"));
        }
    }

    @Test void malformedRetryAfterFallsBackAndLargeValuesRemainFinite() {
        assertEquals(TimeUnit.SECONDS.toNanos(1), HttpWebhookTransport.retryAfterNanos("NaN", "not json"));
        assertEquals(TimeUnit.SECONDS.toNanos(1), HttpWebhookTransport.retryAfterNanos("-1", "{}"));
        assertTrue(HttpWebhookTransport.retryAfterNanos("1e100", "{}") > 0);
        assertEquals(TimeUnit.SECONDS.toNanos(2), HttpWebhookTransport.retryAfterNanos("2", "{}"));
    }
}
