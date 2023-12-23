package dev.s7a.mseds.velocity.test;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@Plugin(id = "exception-test", name = BuildConstants.NAME, version = BuildConstants.VERSION, authors = {"sya-ri"})
public class ExceptionTestPlugin {
    private final ProxyServer proxy;

    @Inject
    public ExceptionTestPlugin(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        proxy.getScheduler().buildTask(this, () -> {
            throw new RuntimeException("Test exception (velocity)");
        }).delay(Duration.ofSeconds(5)).schedule();
    }
}
