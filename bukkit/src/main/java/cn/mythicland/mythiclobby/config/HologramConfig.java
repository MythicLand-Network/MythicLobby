package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.YamlConfig;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class HologramConfig extends YamlConfig {
    public HologramConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "holograms.yml");
        configManager.register(this);
    }
}
