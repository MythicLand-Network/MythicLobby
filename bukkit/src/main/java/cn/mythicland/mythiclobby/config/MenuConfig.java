package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.YamlConfig;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class MenuConfig extends YamlConfig {
    public MenuConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "menus.yml");
        configManager.register(this);
    }
}
