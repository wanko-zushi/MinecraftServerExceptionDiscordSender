package dev.s7a.mseds;

import org.jetbrains.annotations.NotNull;

public class MinecraftServerExceptionDiscordSender {
    public static void send(@NotNull Throwable throwable) {
        System.out.println("Uncaught exception: " + throwable);
    }
}
