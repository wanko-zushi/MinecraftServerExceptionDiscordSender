package dev.s7a.mseds.velocity;

import com.moandjiezana.toml.Toml;
import dev.s7a.mseds.MinecraftServerExceptionDiscordSenderConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Config implements MinecraftServerExceptionDiscordSenderConfig {
    private final Toml config;

    public Config(@NotNull MinecraftServerExceptionDiscordSenderPlugin plugin, @NotNull File dataFolder) throws IOException {
        String fileName = "config.toml";
        File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            dataFolder.mkdirs();
            Files.copy(plugin.getClass().getClassLoader().getResourceAsStream(fileName), file.toPath());
        }
        config = new Toml().read(file);
    }

    @Override
    public @Nullable String getWebhookUrl() {
        return config.getString("webhook_url");
    }
}
