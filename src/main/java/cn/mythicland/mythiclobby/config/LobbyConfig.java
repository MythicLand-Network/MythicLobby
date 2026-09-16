package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.AbstractYamlConfiguration;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class LobbyConfig extends AbstractYamlConfiguration {
    public LobbyConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "config.yml");
        configManager.register(this);
    }
}
