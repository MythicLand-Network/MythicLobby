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
    void sendsUsageHintForRootCommandWithoutArguments() {
        Player player = mock(Player.class);
        MainCommand command = command();

        command.root(player, "mythiclobby");

        ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
        verify(player, times(2)).sendMessage(messages.capture());
        assertEquals(
                List.of(
                        "§c用法: /mythiclobby <子命令>",
                        "§c使用 /mythiclobby help 查看所有命令。"
                ),
                messages.getAllValues()
        );
    }

    @Test
    void sendsThePitStyleHelpForHelpCommand() {
        Player player = mock(Player.class);
        MainCommand command = command();

        command.info(player);

        ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
        verify(player, times(7)).sendMessage(messages.capture());
        assertEquals(
                List.of(
                        "§4> §c/spawn",
                        "§4> §c/mlb help",
                        "§4> §c/mlb reload",
                        "§4> §c/mlb setspawn",
                        "§4> §c/mlbnpc",
                        "§4> §c/mlbholo",
                        "§4> §c/mlbmenu open <menuId>"
                ),
                messages.getAllValues()
        );
    }

    private MainCommand command() {
        return new MainCommand(
                mock(PluginReloader.class),
                mock(LobbyProtectionService.class),
                mock(MessageService.class)
        );
    }
}
