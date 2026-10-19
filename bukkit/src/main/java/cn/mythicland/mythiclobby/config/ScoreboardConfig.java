package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.YamlConfig;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class ScoreboardConfig extends YamlConfig {
    public ScoreboardConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "scoreboard.yml");
        configManager.register(this);
    }
}
