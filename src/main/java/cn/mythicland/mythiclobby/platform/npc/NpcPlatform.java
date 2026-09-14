package cn.mythicland.mythiclobby.platform.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface NpcPlatform {
    Object spawn(Location location, List<String> displayLines);

    void destroy(Object runtimeNpc);

    UUID getUniqueId(Object runtimeNpc);

    Location getLocation(Object runtimeNpc);

    boolean isShown(Object runtimeNpc, Player player);

    void show(Object runtimeNpc, Player player);
}
