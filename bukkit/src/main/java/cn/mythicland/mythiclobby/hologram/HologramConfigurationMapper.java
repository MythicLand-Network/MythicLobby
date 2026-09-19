package cn.mythicland.mythiclobby.hologram;

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
public final class HologramConfigurationMapper {
    private final PluginLogger logger;

    public HologramConfigurationMapper(PluginLogger logger) {
        this.logger = logger;
    }

    public List<StoredHologram> readAll(ConfigurationSection hologramsSection) {
        if (hologramsSection == null) {
            return Collections.emptyList();
        }

        List<StoredHologram> storedHolograms = new ArrayList<>();
        for (String key : hologramsSection.getKeys(false)) {
            readSingle(key, hologramsSection.getConfigurationSection(key)).ifPresent(storedHolograms::add);
        }
        return storedHolograms;
    }

    public void write(ConfigurationSection hologramsSection, StoredHologram storedHologram) {
        String basePath = storedHologram.name();
        hologramsSection.set(basePath + ".display", storedHologram.displayLines());
        writeLocation(hologramsSection, basePath + ".location", storedHologram.location());
    }

    public void delete(ConfigurationSection hologramsSection, String hologramName) {
        hologramsSection.set(hologramName, null);
    }

    private Optional<StoredHologram> readSingle(String key, ConfigurationSection hologramSection) {
        if (hologramSection == null) {
            warn(key, "missing node");
            return Optional.empty();
        }

        String name = trimToNull(key);
        if (name == null) {
            warn(key, "name is empty");
            return Optional.empty();
        }

        Location location = readLocation(key, hologramSection.getConfigurationSection("location"));
        if (location == null) {
            return Optional.empty();
        }

        List<String> displayLines = sanitizeDisplayLines(key, hologramSection);
        if (displayLines.isEmpty()) {
            warn(key, "display is empty");
            return Optional.empty();
        }

        return Optional.of(new StoredHologram(name, displayLines, location));
    }

    private List<String> sanitizeDisplayLines(String key, ConfigurationSection hologramSection) {
        return sanitizeStringList(key, hologramSection, "display", "display must be a list", Collections.emptyList());
    }

    private void writeLocation(ConfigurationSection hologramsSection, String basePath, Location location) {
        writeLocationValue(hologramsSection, basePath, "world", location.getWorld().getName());
        writeLocationValue(hologramsSection, basePath, "x", location.getX());
        writeLocationValue(hologramsSection, basePath, "y", location.getY());
        writeLocationValue(hologramsSection, basePath, "z", location.getZ());
    }

    private Location readLocation(String key, ConfigurationSection locationSection) {
        if (locationSection == null) {
            warn(key, "location section is missing");
            return null;
        }
        if (!hasRequiredLocationFields(locationSection)) {
            warn(key, "location is missing world/x/y/z");
            return null;
        }

        World world = resolveWorld(key, locationSection);
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                locationSection.getDouble("x"),
                locationSection.getDouble("y"),
                locationSection.getDouble("z")
        );
    }

    private List<String> sanitizeStringList(
            String key,
            ConfigurationSection section,
            String path,
            String invalidMessage,
            List<String> fallback
    ) {
        if (section.contains(path) && !section.isList(path)) {
            warn(key, invalidMessage);
            return fallback;
        }

        List<String> sanitizedValues = new ArrayList<>();
        for (String rawValue : section.getStringList(path)) {
            String sanitizedValue = trimToNull(rawValue);
            if (sanitizedValue != null) {
                sanitizedValues.add(sanitizedValue);
            }
        }
        return sanitizedValues;
    }

    private void writeLocationValue(ConfigurationSection section, String basePath, String key, Object value) {
        section.set(basePath + "." + key, value);
    }

    private boolean hasRequiredLocationFields(ConfigurationSection locationSection) {
        return locationSection.contains("world")
                && locationSection.contains("x")
                && locationSection.contains("y")
                && locationSection.contains("z");
    }

    private World resolveWorld(String key, ConfigurationSection locationSection) {
        return WorldResolver.resolveRequiredWorld(locationSection.getString("world"), detail -> warn(key, detail));
    }

    private void warn(String key, String detail) {
        logger.warning(String.format(Locale.ROOT, "Skipping hologram '%s': %s", key, detail));
    }

    private String trimToNull(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
