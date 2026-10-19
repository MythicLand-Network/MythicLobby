package cn.mythicland.mythiclobby.lobby;

import cn.mythicland.mythiclobby.config.LobbyConfig;
import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class LobbyProtectionServiceTest {
    @Test
    void commandTabBypassShouldUseHardcodedPermission() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("enabled", false);
        configuration.set("block-command-tab.enabled", true);
        LobbyConfig lobbyConfig = mock(LobbyConfig.class);
        when(lobbyConfig.data()).thenReturn(configuration);
        LobbyProtectionService service = new LobbyProtectionService(
                mock(JavaPlugin.class),
                lobbyConfig,
                mock(PluginLogger.class)
        );
        Player regularPlayer = mock(Player.class);
        Player permittedPlayer = mock(Player.class);
        when(permittedPlayer.hasPermission("mythiclobby.commandtab.bypass")).thenReturn(true);

        assertTrue(service.shouldBlockCommandTabComplete(regularPlayer));
        assertFalse(service.shouldBlockCommandTabComplete(permittedPlayer));
    }
}
