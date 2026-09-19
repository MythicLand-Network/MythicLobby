package cn.mythicland.mythiclobby.util;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.function.Consumer;

public final class WorldResolver {
    private WorldResolver() {
    }

    public static World resolveRequiredWorld(String rawWorldName, Consumer<String> warningConsumer) {
        String worldName = trimToNull(rawWorldName);
        if (worldName == null) {
            warningConsumer.accept("location.world is empty");
            return null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            warningConsumer.accept("world '" + worldName + "' does not exist");
            return null;
        }
        return world;
    }

    private static String trimToNull(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

