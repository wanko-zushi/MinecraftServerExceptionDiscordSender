package dev.s7a.mseds.bungee;

import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("unused")
public class MinecraftServerExceptionDiscordSenderPlugin extends Plugin {
    @Override
    public void onEnable() {
        MinecraftServerExceptionDiscordSender.setup();
    }
}
