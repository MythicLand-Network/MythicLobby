package cn.mythicland.mythiclobby.platform.npc;

import cn.mythicland.mythiclobby.platform.skin.SkinData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface NpcPlatform {
    Object spawn(Location location, List<String> displayLines, SkinData skin);

    void destroy(Object runtimeNpc);

    UUID getUniqueId(Object runtimeNpc);

    Location getLocation(Object runtimeNpc);

    boolean isShown(Object runtimeNpc, Player player);

    void show(Object runtimeNpc, Player player);

    SkinData getSkin(Player player);

    void updateSkin(Object runtimeNpc, SkinData skin);
}
