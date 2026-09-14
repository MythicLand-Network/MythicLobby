package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@InjectComponent
public final class ConfigManager {
    private final Map<Class<? extends AbstractYamlConfiguration>, AbstractYamlConfiguration> configs = new LinkedHashMap<>();

    public synchronized void register(AbstractYamlConfiguration configuration) {
        Class<? extends AbstractYamlConfiguration> type =
                configuration.getClass();
        configs.put(type, configuration);
    }

    public synchronized void reloadAll() {
        for (AbstractYamlConfiguration configuration : configs.values()) {
            configuration.reload();
        }
    }

    public synchronized <T extends AbstractYamlConfiguration> T get(Class<T> type) {
        AbstractYamlConfiguration configuration = configs.get(type);
        if (configuration == null) {
            throw new IllegalStateException("Missing configuration: " + type.getName() + '.');
        }
        return type.cast(configuration);
    }

    public synchronized Collection<AbstractYamlConfiguration> getAll() {
        return Collections.unmodifiableCollection(configs.values());
    }
}
