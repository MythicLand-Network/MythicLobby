package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.AbstractYamlConfiguration;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class ScoreboardConfig extends AbstractYamlConfiguration {
    public ScoreboardConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "scoreboard.yml");
        configManager.register(this);
    }
}
