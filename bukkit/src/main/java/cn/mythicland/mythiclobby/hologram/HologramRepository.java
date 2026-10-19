package cn.mythicland.mythiclobby.hologram;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.HologramConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@InjectComponent
public final class HologramRepository {
    private final HologramConfig config;
    private final HologramConfigurationMapper mapper;

    public HologramRepository(HologramConfig config, HologramConfigurationMapper mapper) {
        this.config = config;
        this.mapper = mapper;
    }

    public List<StoredHologram> loadAll() {
        return mapper.readAll(config.data().getConfigurationSection("holograms"));
    }

    public void reload() {
        // ConfigManager keeps YAML state fresh before repositories rebuild runtime data.
    }

    public void save(StoredHologram storedHologram) {
        mapper.write(ensureHologramsSection(), storedHologram);
        config.save();
    }

    public void delete(String hologramName) {
        mapper.delete(ensureHologramsSection(), hologramName);
        config.save();
    }

    private ConfigurationSection ensureHologramsSection() {
        ConfigurationSection section = config.data().getConfigurationSection("holograms");
        return section != null ? section : config.data().createSection("holograms");
    }
}
