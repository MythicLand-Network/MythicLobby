package cn.mythicland.mythiclobby.velocity.command;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.velocity.config.HubConfig;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;

@InjectComponent
public final class HubCommand implements SimpleCommand {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final Component PLAYER_ONLY_MESSAGE = LEGACY_SERIALIZER.deserialize("&c该命令只能由玩家执行。");
    private static final Component ALREADY_CONNECTED_MESSAGE = LEGACY_SERIALIZER.deserialize("&c你已经在大厅服务器了!");
    private static final Component SERVER_UNAVAILABLE_MESSAGE = LEGACY_SERIALIZER.deserialize("&c大厅服务器当前不可用。");
    private static final Component CONNECTION_FAILED_MESSAGE = LEGACY_SERIALIZER.deserialize("&c连接大厅服务器失败，请稍后再试。");

    private final ProxyServer proxyServer;
    private final HubConfig hubConfig;

    public HubCommand(
            ProxyServer proxyServer,
            HubConfig hubConfig
    ) {
        this.proxyServer = proxyServer;
        this.hubConfig = hubConfig;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(PLAYER_ONLY_MESSAGE);
            return;
        }

        Optional<RegisteredServer> target = this.proxyServer.getServer(this.hubConfig.hubServer());
        if (target.isEmpty()) {
            player.sendMessage(SERVER_UNAVAILABLE_MESSAGE);
            return;
        }

        if (isConnectedTo(player, target.get())) {
            player.sendMessage(ALREADY_CONNECTED_MESSAGE);
            return;
        }

        player.createConnectionRequest(target.get())
                .connect()
                .thenAccept(result -> {
                    if (!result.isSuccessful()) {
                        player.sendMessage(CONNECTION_FAILED_MESSAGE);
                    }
                })
                .exceptionally(exception -> {
                    player.sendMessage(CONNECTION_FAILED_MESSAGE);
                    return null;
                });
    }

    private boolean isConnectedTo(Player player, RegisteredServer target) {
        Optional<ServerConnection> current = player.getCurrentServer();
        return current.isPresent()
                && current.get().getServerInfo().getName().equalsIgnoreCase(target.getServerInfo().getName());
    }
}
