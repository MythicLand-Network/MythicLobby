package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.config.YamlConfig;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class ChatConfig extends YamlConfig {
    public ChatConfig(Plugin plugin, ConfigManager configManager) {
        super(plugin, "chat.yml");
        configManager.register(this);
    }
}
