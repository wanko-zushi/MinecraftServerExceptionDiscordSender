package dev.s7a.mseds.bukkit;

import dev.s7a.mseds.MinecraftServerExceptionDiscordSenderConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class Config implements MinecraftServerExceptionDiscordSenderConfig {
    public FileConfiguration config;

    public Config(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    @Override
    public @Nullable String getWebhookUrl() {
        return config.getString("webhook_url");
    }
}
