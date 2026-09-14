package cn.mythicland.mythiclobby.platform.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface HologramPlatform {
    Object spawn(Player viewer, Location baseLocation, List<String> renderedLines);

    void update(Player viewer, Object viewerState, Location baseLocation, List<String> renderedLines);

    void destroy(Player viewer, Object viewerState);

    int getLineCount(Object viewerState);
}
