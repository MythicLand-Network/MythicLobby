package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.YamlConfig;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class DisplayConfig extends YamlConfig {
    public DisplayConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "display.yml");
        configManager.register(this);
    }
}
