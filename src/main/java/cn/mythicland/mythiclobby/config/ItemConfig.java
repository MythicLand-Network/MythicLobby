package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class ItemConfig extends AbstractYamlConfiguration {
    public ItemConfig(Plugin plugin, ConfigManager configManager, PluginLogger logger) {
        super(plugin, configManager, logger, "items.yml");
    }
}
