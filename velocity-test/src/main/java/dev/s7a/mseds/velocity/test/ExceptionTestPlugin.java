package dev.s7a.mseds.velocity.test;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;

@Plugin(id = "exception-test", name = BuildConstants.NAME, version = BuildConstants.VERSION, authors = {"sya-ri"})
public class ExceptionTestPlugin {
    @Inject
    public ExceptionTestPlugin() {
    }
}
