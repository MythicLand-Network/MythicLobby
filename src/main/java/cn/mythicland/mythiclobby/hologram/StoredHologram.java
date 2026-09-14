package cn.mythicland.mythiclobby.hologram;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public record StoredHologram(
        String name,
        List<String> displayLines,
        Location location
) {
    public StoredHologram(String name, List<String> displayLines, Location location) {
        this.name = name;
        this.displayLines = new ArrayList<>(displayLines);
        this.location = location.clone();
    }

    @Override
    public List<String> displayLines() {
        return new ArrayList<>(displayLines);
    }

    @Override
    public Location location() {
        return location.clone();
    }
}
