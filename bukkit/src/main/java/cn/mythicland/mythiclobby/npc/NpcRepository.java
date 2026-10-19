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
        return mapper.readAll(config.data().getConfigurationSection("npcs"));
    }

    public void reload() {
        // ConfigManager keeps YAML state fresh before repositories rebuild runtime data.
    }

    public void save(StoredNpc storedNpc) {
        mapper.write(ensureNpcsSection(), storedNpc);
        config.save();
    }

    public void delete(String npcName) {
        mapper.delete(ensureNpcsSection(), npcName);
        config.save();
    }

    private ConfigurationSection ensureNpcsSection() {
        ConfigurationSection section = config.data().getConfigurationSection("npcs");
        return section != null ? section : config.data().createSection("npcs");
    }
}
