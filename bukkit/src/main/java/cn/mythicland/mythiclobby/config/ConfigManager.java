package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.bukkit.config.YamlConfig;
import cn.mythicland.framework.component.InjectComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@InjectComponent
public final class ConfigManager {
    private final Map<Class<? extends YamlConfig>, YamlConfig> configs = new LinkedHashMap<>();

    public synchronized void register(YamlConfig configuration) {
        configuration.load();
        Class<? extends YamlConfig> type =
                configuration.getClass();
        configs.put(type, configuration);
    }

    public synchronized void reloadAll() {
        for (YamlConfig configuration : configs.values()) {
            configuration.load();
        }
    }

    public synchronized <T extends YamlConfig> T get(Class<T> type) {
        YamlConfig configuration = configs.get(type);
        if (configuration == null) {
            throw new IllegalStateException("Missing configuration: " + type.getName() + '.');
        }
        return type.cast(configuration);
    }

    public synchronized Collection<YamlConfig> getAll() {
        return Collections.unmodifiableCollection(configs.values());
    }
}
