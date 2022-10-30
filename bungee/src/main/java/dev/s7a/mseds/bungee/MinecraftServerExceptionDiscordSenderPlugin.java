package dev.s7a.mseds.bungee;

import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

@SuppressWarnings("unused")
public class MinecraftServerExceptionDiscordSenderPlugin extends Plugin {
    @Override
    public void onEnable() {
        ProxyServer.getInstance().getLogger().addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                Throwable thrown = record.getThrown();
                if (thrown != null) {
                    MinecraftServerExceptionDiscordSender.send(thrown);
                }
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });
    }
}
