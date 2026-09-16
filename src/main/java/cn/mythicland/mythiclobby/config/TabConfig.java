package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.AbstractYamlConfiguration;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class TabConfig extends AbstractYamlConfiguration {
    public TabConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "tab.yml");
        configManager.register(this);
    }
}
