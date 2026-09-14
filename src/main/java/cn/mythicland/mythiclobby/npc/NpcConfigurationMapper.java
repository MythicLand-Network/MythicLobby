package cn.mythicland.mythiclobby.npc;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.util.WorldResolver;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
@InjectComponent
public final class NpcConfigurationMapper {
    private final PluginLogger logger;

    public NpcConfigurationMapper(PluginLogger logger) {
        this.logger = logger;
    }

    public List<StoredNpc> readAll(ConfigurationSection npcsSection) {
        if (npcsSection == null) {
            return Collections.emptyList();
        }

        List<StoredNpc> storedNpcs = new ArrayList<>();
        for (String key : npcsSection.getKeys(false)) {
            readSingle(key, npcsSection.getConfigurationSection(key)).ifPresent(storedNpcs::add);
        }
        return storedNpcs;
    }

    public void write(ConfigurationSection npcsSection, StoredNpc storedNpc) {
        String basePath = storedNpc.getName();
        npcsSection.set(basePath + ".name", storedNpc.getName());
        npcsSection.set(basePath + ".display", new ArrayList<>(storedNpc.getDisplayLines()));
        writeLocation(npcsSection, basePath + ".location", storedNpc.getLocation());
        npcsSection.set(basePath + ".skin", storedNpc.getSkinPlayerName());
        npcsSection.set(basePath + ".commands", new ArrayList<>(storedNpc.getCommands()));
    }

    public void delete(ConfigurationSection npcsSection, String npcName) {
        npcsSection.set(npcName, null);
    }

    private Optional<StoredNpc> readSingle(String key, ConfigurationSection npcSection) {
        if (npcSection == null) {
            warn(key, "missing node");
            return Optional.empty();
        }

        String name = trimToNull(npcSection.getString("name", key));
        if (name == null) {
            warn(key, "name is empty");
            return Optional.empty();
        }

        Location location = readLocation(key, npcSection.getConfigurationSection("location"));
        if (location == null) {
            return Optional.empty();
        }

        List<String> displayLines = sanitizeDisplayLines(key, npcSection);
        if (displayLines.isEmpty()) {
            warn(key, "display is empty, falling back to name");
            displayLines = Collections.singletonList(name);
        }

        return Optional.of(new StoredNpc(
                name,
                displayLines,
                location,
                trimToNull(npcSection.getString("skin")),
                sanitizeCommands(key, npcSection)
        ));
    }

    private List<String> sanitizeDisplayLines(String key, ConfigurationSection npcSection) {
        if (npcSection.contains("display") && !npcSection.isList("display")) {
            warn(key, "display must be a list");
            return Collections.emptyList();
        }

        List<String> displayLines = new ArrayList<>();
        for (String line : npcSection.getStringList("display")) {
            String sanitized = trimToNull(line);
            if (sanitized != null) {
                displayLines.add(sanitized);
            }
        }
        return displayLines;
    }

    private List<String> sanitizeCommands(String key, ConfigurationSection npcSection) {
        if (npcSection.contains("commands") && !npcSection.isList("commands")) {
            warn(key, "commands must be a list");
            return new ArrayList<>();
        }

        List<String> commands = new ArrayList<>();
        for (String command : npcSection.getStringList("commands")) {
            String sanitized = trimToNull(command);
            if (sanitized != null) {
                commands.add(sanitized);
            }
        }
        return commands;
    }

    private void writeLocation(ConfigurationSection npcsSection, String basePath, Location location) {
        npcsSection.set(basePath + ".world", location.getWorld().getName());
        npcsSection.set(basePath + ".x", location.getX());
        npcsSection.set(basePath + ".y", location.getY());
        npcsSection.set(basePath + ".z", location.getZ());
        npcsSection.set(basePath + ".yaw", location.getYaw());
        npcsSection.set(basePath + ".pitch", location.getPitch());
    }

    private Location readLocation(String key, ConfigurationSection locationSection) {
        if (locationSection == null) {
            warn(key, "location section is missing");
            return null;
        }
        if (!locationSection.contains("world") || !locationSection.contains("x") || !locationSection.contains("y") || !locationSection.contains("z")) {
            warn(key, "location is missing world/x/y/z");
            return null;
        }

        World world = WorldResolver.resolveRequiredWorld(locationSection.getString("world"), detail -> warn(key, detail));
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                locationSection.getDouble("x"),
                locationSection.getDouble("y"),
                locationSection.getDouble("z"),
                (float) locationSection.getDouble("yaw"),
                (float) locationSection.getDouble("pitch")
        );
    }

    private void warn(String key, String detail) {
        logger.warning(String.format(Locale.ROOT, "Skipping NPC '%s': %s", key, detail));
    }

    private String trimToNull(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
