package dev.s7a.mseds.bungee;

import dev.s7a.mseds.InvalidWebhookUrlException;
import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

@SuppressWarnings("unused")
public class MinecraftServerExceptionDiscordSenderPlugin extends Plugin {
    private MinecraftServerExceptionDiscordSender sender;

    @Override
    public void onEnable() {
        Config config;
        try {
            config = new Config(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String url = config.getWebhookUrl();
        if (url != null && !url.isEmpty()) {
            sender = new MinecraftServerExceptionDiscordSender(url);
            sender.setup();
        } else {
            throw new InvalidWebhookUrlException("webhook_url is empty. Please check config.yml");
        }
    }

    @Override
    public void onDisable() {
        if (sender != null) sender.close();
    }
}
