package dev.s7a.mseds.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;

import java.io.Serializable;

@Plugin(id = "minecraft-server-exception-discord-sender", name = "{name}", version = "{version}", authors = {"sya-ri"})
public class MinecraftServerExceptionDiscordSenderPlugin {
    @Inject
    public MinecraftServerExceptionDiscordSenderPlugin() {
        LoggerContext context = LoggerContext.getContext(false);
        Appender appender = MinecraftServerExceptionDiscordSenderAppender.createAppender(null, null, false, null);
        appender.start();
        Configuration configuration = context.getConfiguration();
        configuration.getLoggers().values().forEach(loggerConfig -> loggerConfig.addAppender(appender, null, null));
        configuration.getRootLogger().addAppender(appender, null, null);
    }

    static class MinecraftServerExceptionDiscordSenderAppender extends AbstractAppender {
        public static MinecraftServerExceptionDiscordSenderAppender createAppender(Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
            return new MinecraftServerExceptionDiscordSenderAppender(filter, layout, ignoreExceptions, properties);
        }

        protected MinecraftServerExceptionDiscordSenderAppender(Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
            super("MinecraftServerExceptionDiscordSender", filter, layout, ignoreExceptions, properties);
        }

        @Override
        public void append(LogEvent event) {
            Throwable thrown = event.getThrown();
            if (thrown != null) {
                MinecraftServerExceptionDiscordSender.send(thrown);
            }
        }
    }
}
