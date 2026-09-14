package cn.mythicland.mythiclobby.command;

import cn.mythicland.mythiclobby.PluginReloader;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import cn.mythicland.mythiclobby.message.MessageService;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

final class MainCommandHelpTest {
    @Test
    void sendsThePitStyleHelpForRootCommand() {
        Player player = mock(Player.class);
        MainCommand command = new MainCommand(
                mock(PluginReloader.class),
                mock(LobbyProtectionService.class),
                mock(MessageService.class)
        );

        command.info(player);

        ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
        verify(player, times(6)).sendMessage(messages.capture());
        assertEquals(
                List.of(
                        "§4> §c/spawn",
                        "§4> §c/mlb reload",
                        "§4> §c/mlb setspawn",
                        "§4> §c/mlbnpc",
                        "§4> §c/mlbholo",
                        "§4> §c/mlbmenu open <menuId>"
                ),
                messages.getAllValues()
        );
    }
}
