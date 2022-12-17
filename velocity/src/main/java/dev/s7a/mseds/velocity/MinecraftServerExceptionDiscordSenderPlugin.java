package dev.s7a.mseds.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import dev.s7a.mseds.InvalidWebhookUrlException;
import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "minecraft-server-exception-discord-sender", name = "{name}", version = "{version}", authors = {"sya-ri"})
public class MinecraftServerExceptionDiscordSenderPlugin {
    @Inject
    public MinecraftServerExceptionDiscordSenderPlugin(@DataDirectory Path dataFolder) {
        Config config;
        try {
            config = new Config(this, dataFolder.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String url = config.getWebhookUrl();
        if (url != null && !url.isEmpty()) {
            new MinecraftServerExceptionDiscordSender(url).setup();
        } else {
            throw new InvalidWebhookUrlException("webhook_url is empty. Please check config.toml");
        }
    }
}
