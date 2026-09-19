package cn.mythicland.mythiclobby.velocity;

import cn.mythicland.framework.runtime.FrameworkRuntimeClassLoading;
import cn.mythicland.framework.runtime.RuntimeBootstrap;
import cn.mythicland.mythiclobby.bootstrap.RuntimeBootstrapFactory;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Plugin(
        id = "mythiclobby",
        name = "MythicLobbyVelocity",
        version = "1.0.0",
        authors = {"ShanguanLinG"}
)
public final class MythicLobbyVelocity {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private RuntimeBootstrap runtimeBootstrap;

    @Inject
    public MythicLobbyVelocity(
            ProxyServer proxyServer,
            Logger logger,
            @DataDirectory Path dataDirectory
    ) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.runtimeBootstrap = RuntimeBootstrapFactory.create(
                this,
                "cn.mythicland.mythiclobby.velocity.runtime.VelocityMainLoader",
                this.dataDirectory,
                Set.of("common", "velocity"),
                this.logger::info,
                this.logger::warn,
                FrameworkRuntimeClassLoading.velocity(
                        List.of(
                                "cn.mythicland.mythiclobby.",
                                "org.reflections.",
                                "javassist.",
                                "org.yaml.snakeyaml."
                        ),
                        List.of(MythicLobbyVelocity.class.getName()),
                        List.of("cn.mythicland.mythiclobby.bootstrap.")
                )
        );
        this.runtimeBootstrap.load();
        this.runtimeBootstrap.enable();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (this.runtimeBootstrap == null) return;
        this.runtimeBootstrap.disable();
        this.runtimeBootstrap = null;
    }

    public ProxyServer getProxyServer() {
        return this.proxyServer;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Path getDataDirectory() {
        return this.dataDirectory;
    }
}
