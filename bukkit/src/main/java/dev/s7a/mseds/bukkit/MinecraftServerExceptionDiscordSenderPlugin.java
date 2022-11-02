package dev.s7a.mseds.bukkit;

import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class MinecraftServerExceptionDiscordSenderPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        MinecraftServerExceptionDiscordSender.setup();
    }
}
