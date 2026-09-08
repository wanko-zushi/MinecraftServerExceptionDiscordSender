package dev.s7a.mseds;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

final class HttpWebhookTransport implements WebhookTransport {
    private static final int CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int READ_TIMEOUT_MILLIS = 5000;
    private static final int RESPONSE_LIMIT = 8192;
    private final URL url;
    private volatile HttpURLConnection active;
    private volatile boolean closed;

    HttpWebhookTransport(URL url) { this.url = url; }

    @Override
    public WebhookResponse send(String content) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        synchronized (this) {
            if (closed) throw new IOException("Notification transport is closed");
            active = connection;
        }
        try {
            JsonObject json = new JsonObject();
            json.addProperty("content", content);
            byte[] body = json.toString().getBytes(StandardCharsets.UTF_8);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("User-Agent", "MinecraftServerExceptionDiscordSender");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream stream = connection.getOutputStream()) { stream.write(body); }
            int status = connection.getResponseCode();
            long retryNanos = 0;
            if (status == 429) {
                String response = "";
                try {
                    response = readError(connection.getErrorStream());
                } catch (IOException ignored) {
                    // use Retry-After when the response body is incomplete
                }
                retryNanos = retryAfterNanos(connection.getHeaderField("Retry-After"), response);
            }
            return new WebhookResponse(status, retryNanos);
        } finally {
            active = null;
            connection.disconnect();
        }
    }

    private static String readError(InputStream input) throws IOException {
        if (input == null) return "";
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while (output.size() < RESPONSE_LIMIT
                    && (length = stream.read(buffer, 0, Math.min(buffer.length, RESPONSE_LIMIT - output.size()))) != -1) {
                output.write(buffer, 0, length);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    static long retryAfterNanos(String header, String body) {
        double seconds = parseSeconds(header);
        if (seconds < 0 && header != null) {
            try {
                seconds = Math.max(0, (ZonedDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME)
                        .toInstant().toEpochMilli() - System.currentTimeMillis()) / 1000.0);
            } catch (DateTimeParseException ignored) { }
        }
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (json.has("retry_after")) seconds = Math.max(seconds, parseSeconds(json.get("retry_after").getAsString()));
        } catch (RuntimeException ignored) { }
        if (seconds < 0) seconds = 1;
        // avoid overflow in cooldown calculations
        return (long) Math.min(seconds * TimeUnit.SECONDS.toNanos(1), Long.MAX_VALUE / 4.0);
    }

    private static double parseSeconds(String value) {
        if (value == null) return -1;
        try {
            double seconds = Double.parseDouble(value);
            return Double.isFinite(seconds) && seconds >= 0 ? seconds : -1;
        } catch (NumberFormatException ignored) { return -1; }
    }

    @Override
    public void close() {
        HttpURLConnection connection;
        synchronized (this) {
            closed = true;
            connection = active;
        }
        if (connection != null) connection.disconnect();
    }
}
