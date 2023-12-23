package dev.s7a.mseds.bungee;

import dev.s7a.mseds.MinecraftServerExceptionDiscordSenderConfig;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Config implements MinecraftServerExceptionDiscordSenderConfig {
    private final Configuration configuration;

    public Config(@NotNull Plugin plugin) throws IOException {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            // saveDefaultConfig
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                throw new IOException();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            URL url = this.getClass().getClassLoader().getResource("config.yml");
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            InputStream inputStream = connection.getInputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        }
        configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    @Override
    public @Nullable String getWebhookUrl() {
        return configuration.getString("webhook_url");
    }
}
