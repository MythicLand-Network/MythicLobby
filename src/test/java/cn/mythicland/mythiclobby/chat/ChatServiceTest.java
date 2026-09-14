package cn.mythicland.mythiclobby.chat;

import cn.mythicland.mythiclobby.config.ChatConfig;
import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class ChatServiceTest {
    @Test
    void muteAllShouldNotAllowPermissionBypass() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("mute-all.enabled", true);
        ChatConfig chatConfig = mock(ChatConfig.class);
        when(chatConfig.getYamlConfiguration()).thenReturn(configuration);
        ChatService service = new ChatService(
                chatConfig,
                mock(ChatRenderer.class),
                mock(PluginLogger.class)
        );

        assertTrue(service.shouldBlockChat(mock(Player.class)));
        assertEquals("§c你不能在这个服务器上发言。", service.getMuteDeniedMessage());
    }
}
