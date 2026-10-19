package cn.mythicland.mythiclobby.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigManagerTest {
    @TempDir
    Path directory;

    @Test
    void constructingConfig_shouldLoadItBeforeDependentServicesCanReadIt() throws Exception {
        Files.writeString(this.directory.resolve("scoreboard.yml"), "title: saved\n");
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(this.directory.toFile());
        ConfigManager manager = new ConfigManager();

        ScoreboardConfig config = new ScoreboardConfig(plugin, manager);

        assertEquals("saved", config.data().getString("title"));
        assertSame(config, manager.get(ScoreboardConfig.class));
    }
}
