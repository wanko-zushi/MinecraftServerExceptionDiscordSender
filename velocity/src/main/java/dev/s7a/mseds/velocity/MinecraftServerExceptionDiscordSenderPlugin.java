package dev.s7a.mseds.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import dev.s7a.mseds.MinecraftServerExceptionDiscordSender;

@Plugin(id = "minecraft-server-exception-discord-sender", name = "{name}", version = "{version}", authors = {"sya-ri"})
public class MinecraftServerExceptionDiscordSenderPlugin {
    @Inject
    public MinecraftServerExceptionDiscordSenderPlugin() {
        MinecraftServerExceptionDiscordSender.setup();
    }
}
