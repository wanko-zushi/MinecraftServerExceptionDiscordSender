package dev.s7a.mseds.bukkit;

import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class MinecraftServerExceptionDiscordSenderPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Config config = new Config(this);
        String url = config.getWebhookUrl();
        if (url != null && !url.isEmpty()) {
            new MinecraftServerExceptionDiscordSender(url).setup();
        } else {
            getLogger().severe("webhook_url is empty. Please check config.yml");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
