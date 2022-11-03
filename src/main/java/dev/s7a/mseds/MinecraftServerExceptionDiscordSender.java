package dev.s7a.mseds;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class MinecraftServerExceptionDiscordSender {
    private final String url;

    public MinecraftServerExceptionDiscordSender(@NotNull String url) {
        this.url = url;
    }

    public void setup() {
        LoggerContext context = LoggerContext.getContext(false);
        Appender appender = MinecraftServerExceptionDiscordSenderAppender.createAppender(null, null, false, null);
        appender.start();
        Configuration configuration = context.getConfiguration();
        configuration.getLoggers().values().forEach(loggerConfig -> loggerConfig.addAppender(appender, null, null));
        configuration.getRootLogger().addAppender(appender, null, null);
    }

    static void send(@NotNull Throwable throwable) {
        System.out.println("Uncaught exception: " + throwable);
    }
}
