package dev.s7a.mseds;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class MinecraftServerExceptionDiscordSender {
    private static final String SENDER_NAME = "MinecraftServerExceptionDiscordSender";
    private final URL url;

    public MinecraftServerExceptionDiscordSender(@NotNull String url) throws InvalidWebhookUrlException {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new InvalidWebhookUrlException(e);
        }
    }

    public void setup() {
        LoggerContext context = LoggerContext.getContext(false);
        Appender appender = new AbstractAppender(SENDER_NAME, null, null, false, null) {
            @Override
            public void append(LogEvent event) {
                Throwable thrown = event.getThrown();
                if (thrown != null) {
                    send(thrown);
                }
            }
        };
        appender.start();
        Configuration configuration = context.getConfiguration();
        configuration.getLoggers().values().forEach(loggerConfig -> loggerConfig.addAppender(appender, null, null));
        configuration.getRootLogger().addAppender(appender, null, null);
    }

    private void send(@NotNull Throwable throwable) {
        String stackTrace = getStackTrace(throwable);
        if (stackTrace.contains(SENDER_NAME)) {
            // ignore self exception
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("username", "MinecraftServerException");
        int contentLengthLimit = 1997; // https://birdie0.github.io/discord-webhooks-guide/other/field_limits.html
        if (contentLengthLimit < stackTrace.length()) {
            json.addProperty("content", stackTrace.substring(0, contentLengthLimit) + "...");
        } else {
            json.addProperty("content", stackTrace);
        }
        send(json);
    }

    private void send(@NotNull JsonObject json) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", SENDER_NAME);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStream stream = connection.getOutputStream();
            stream.write(json.toString().getBytes());
            stream.flush();
            stream.close();
            connection.getInputStream().close();
            connection.disconnect();
        } catch (IOException e) {
            throw new WebhookSendException(e);
        }
    }

    private static @NotNull String getStackTrace(@NotNull Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }
}
