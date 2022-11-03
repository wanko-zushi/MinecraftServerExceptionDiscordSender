package dev.s7a.mseds;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.io.Serializable;

public class MinecraftServerExceptionDiscordSenderAppender extends AbstractAppender {
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
