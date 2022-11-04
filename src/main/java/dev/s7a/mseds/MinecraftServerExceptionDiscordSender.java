package dev.s7a.mseds;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class MinecraftServerExceptionDiscordSender {
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
        Appender appender = new AbstractAppender("MinecraftServerExceptionDiscordSender", null, null, false, null) {
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
        System.out.println("Uncaught exception: " + getStackTrace(throwable));
    }

    private static @NotNull String getStackTrace(@NotNull Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }
}
