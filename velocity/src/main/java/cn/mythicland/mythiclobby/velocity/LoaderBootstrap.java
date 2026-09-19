package cn.mythicland.mythiclobby.velocity;

import cn.mythicland.framework.component.ComponentContainer;
import cn.mythicland.mythiclobby.velocity.command.HubCommand;
import cn.mythicland.mythiclobby.velocity.config.HubConfig;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.ProxyServer;

final class LoaderBootstrap {
    private final ProxyServer proxyServer;
    private final HubCommand hubCommand;
    private boolean registered;
    private CommandMeta commandMeta;

    private LoaderBootstrap(
            ProxyServer proxyServer,
            HubCommand hubCommand
    ) {
        this.proxyServer = proxyServer;
        this.hubCommand = hubCommand;
    }

    static LoaderBootstrap create(MythicLobbyVelocity plugin) {
        HubConfig hubConfig = new HubConfig(
                plugin.getDataDirectory(),
                plugin.getClass().getClassLoader()
        );
        ComponentContainer components = new ComponentContainer();
        components.bind(plugin.getProxyServer());
        components.bind(hubConfig);
        components.scan("cn.mythicland.mythiclobby.velocity");
        return new LoaderBootstrap(
                plugin.getProxyServer(),
                components.resolve(HubCommand.class)
        );
    }

    void register() {
        CommandManager commandManager = this.proxyServer.getCommandManager();
        this.commandMeta = commandManager.metaBuilder("hub")
                .aliases("lobby")
                .build();
        commandManager.register(this.commandMeta, this.hubCommand);
        this.registered = true;
    }

    void unregister() {
        if (!this.registered) return;
        this.proxyServer.getCommandManager().unregister(this.commandMeta);
        this.commandMeta = null;
        this.registered = false;
    }
}
