package cn.mythicland.mythiclobby.platform.tab;

import org.bukkit.entity.Player;

public interface TabPlatform {
    void sendHeaderFooter(Player viewer, String header, String footer);

    void clearHeaderFooter(Player viewer);
}
