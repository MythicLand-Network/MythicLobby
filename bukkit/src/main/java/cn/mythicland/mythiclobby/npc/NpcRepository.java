package cn.mythicland.mythiclobby.npc;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.NpcConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@InjectComponent
public final class NpcRepository {
    private final NpcConfig config;
    private final NpcConfigurationMapper mapper;

    public NpcRepository(NpcConfig config, NpcConfigurationMapper mapper) {
        this.config = config;
        this.mapper = mapper;
    }

    public List<StoredNpc> loadAll() {
        return mapper.readAll(config.getYamlConfiguration().getConfigurationSection("npcs"));
    }

    public void reload() {
        // ConfigManager keeps YAML state fresh before repositories rebuild runtime data.
    }

    public void save(StoredNpc storedNpc) {
        mapper.write(ensureNpcsSection(), storedNpc);
        config.saveYamlConfiguration();
    }

    public void delete(String npcName) {
        mapper.delete(ensureNpcsSection(), npcName);
        config.saveYamlConfiguration();
    }

    private ConfigurationSection ensureNpcsSection() {
        ConfigurationSection section = config.getYamlConfiguration().getConfigurationSection("npcs");
        return section != null ? section : config.getYamlConfiguration().createSection("npcs");
    }
}
