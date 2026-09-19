package cn.mythicland.mythiclobby.velocity.command;

import cn.mythicland.mythiclobby.velocity.config.HubConfig;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class HubCommandTest {
    @TempDir
    Path dataDirectory;

    private ProxyServer proxyServer;
    private Player player;
    private RegisteredServer targetServer;
    private HubCommand command;

    @BeforeEach
    void setUp() {
        this.proxyServer = mock(ProxyServer.class);
        this.player = mock(Player.class);
        this.targetServer = mock(RegisteredServer.class);
        this.command = new HubCommand(
                this.proxyServer,
                new HubConfig(dataDirectory, getClass().getClassLoader())
        );
    }

    @Test
    void connectsPlayerToTheConfiguredServer() {
        ConnectionRequestBuilder request = mock(ConnectionRequestBuilder.class);
        ConnectionRequestBuilder.Result result = mock(ConnectionRequestBuilder.Result.class);
        SimpleCommand.Invocation invocation = mock(SimpleCommand.Invocation.class);
        when(invocation.source()).thenReturn(this.player);
        when(this.proxyServer.getServer("lobby")).thenReturn(Optional.of(this.targetServer));
        when(this.player.getCurrentServer()).thenReturn(Optional.empty());
        when(this.player.createConnectionRequest(this.targetServer)).thenReturn(request);
        when(request.connect()).thenReturn(CompletableFuture.completedFuture(result));
        when(result.isSuccessful()).thenReturn(true);

        this.command.execute(invocation);

        verify(this.player).createConnectionRequest(this.targetServer);
    }

    @Test
    void doesNotReconnectPlayerAlreadyInTheLobby() {
        SimpleCommand.Invocation invocation = mock(SimpleCommand.Invocation.class);
        ServerInfo serverInfo = mock(ServerInfo.class);
        ServerConnection currentServer = mock(ServerConnection.class);
        when(invocation.source()).thenReturn(this.player);
        when(this.proxyServer.getServer("lobby")).thenReturn(Optional.of(this.targetServer));
        when(this.targetServer.getServerInfo()).thenReturn(serverInfo);
        when(serverInfo.getName()).thenReturn("lobby");
        when(this.player.getCurrentServer()).thenReturn(Optional.of(currentServer));
        when(currentServer.getServerInfo()).thenReturn(serverInfo);

        this.command.execute(invocation);

        verify(this.player, never()).createConnectionRequest(any(RegisteredServer.class));
        verify(this.player).sendMessage(any());
    }

    @Test
    void rejectsNonPlayerCommandSources() {
        CommandSource source = mock(CommandSource.class);
        SimpleCommand.Invocation invocation = mock(SimpleCommand.Invocation.class);
        when(invocation.source()).thenReturn(source);

        this.command.execute(invocation);

        verify(source).sendMessage(any());
        verify(this.proxyServer, never()).getServer("lobby");
    }

    @Test
    void convertsAmpersandColorCodesBeforeSending() {
        CommandSource source = mock(CommandSource.class);
        SimpleCommand.Invocation invocation = mock(SimpleCommand.Invocation.class);
        when(invocation.source()).thenReturn(source);

        this.command.execute(invocation);

        var message = forClass(Component.class);
        verify(source).sendMessage(message.capture());
        org.junit.jupiter.api.Assertions.assertEquals(NamedTextColor.RED, message.getValue().color());
    }
}
