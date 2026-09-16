package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.AbstractYamlConfiguration;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class ItemConfig extends AbstractYamlConfiguration {
    public ItemConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "items.yml");
        configManager.register(this);
    }
}
