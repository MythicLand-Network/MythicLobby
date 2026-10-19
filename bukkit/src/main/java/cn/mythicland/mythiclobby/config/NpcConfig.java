package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.YamlConfig;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class NpcConfig extends YamlConfig {
    public NpcConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "npcs.yml");
        configManager.register(this);
    }
}
