package dev.s7a.mseds.bukkit.test;

import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class ExceptionTestPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getScheduler().runTaskLater(this, () -> {
            throw new RuntimeException("Test exception (bukkit)");
        }, 100L);
    }
}
