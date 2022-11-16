package dev.s7a.mseds.bungee;

import dev.s7a.mseds.MinecraftServerExceptionDiscordSenderConfig;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class Config implements MinecraftServerExceptionDiscordSenderConfig {
    private final Configuration configuration;

    public Config(@NotNull Plugin plugin) throws IOException {
        File file = new File(plugin.getDataFolder(), "config.yml");
        configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    @Override
    public @Nullable String getWebhookUrl() {
        return configuration.getString("webhook_url");
    }
}
