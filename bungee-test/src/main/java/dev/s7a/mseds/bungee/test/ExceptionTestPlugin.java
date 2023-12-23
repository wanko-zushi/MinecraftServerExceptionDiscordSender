package dev.s7a.mseds.bungee.test;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class ExceptionTestPlugin extends Plugin {
    @Override
    public void onEnable() {
        getProxy().getScheduler().schedule(this, () -> {
            throw new RuntimeException("Test exception (bungee)");
        }, 5, TimeUnit.SECONDS);
    }
}
