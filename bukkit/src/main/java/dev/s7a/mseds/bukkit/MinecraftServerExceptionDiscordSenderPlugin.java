package dev.s7a.mseds.bukkit;

import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

@SuppressWarnings("unused")
public class MinecraftServerExceptionDiscordSenderPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getLogger().addHandler(new Handler() {
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
