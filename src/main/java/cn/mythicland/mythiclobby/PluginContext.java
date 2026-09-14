package cn.mythicland.mythiclobby;

import cn.mythicland.mythiclobby.config.ConfigManager;
import cn.mythicland.mythiclobby.display.DisplayService;
import cn.mythicland.mythiclobby.gui.menu.MenuManager;
import cn.mythicland.mythiclobby.hologram.HologramService;
import cn.mythicland.mythiclobby.item.LobbyItemService;
import cn.mythicland.mythiclobby.listener.CommandTabBlockService;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.npc.NpcManager;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import cn.mythicland.framework.component.ComponentContainer;
import cn.mythicland.mythiclobby.scoreboard.ScoreboardService;
import cn.mythicland.mythiclobby.tab.TabService;
import org.bukkit.plugin.java.JavaPlugin;

public record PluginContext(
        JavaPlugin plugin,
        PluginLogger pluginLogger,
        ConfigManager configManager,
        ComponentContainer components,
        PluginReloader pluginReloader,
        MenuManager menuManager,
        BungeeConnectService bungeeConnectService,
        HologramService hologramService,
        NpcManager npcManager,
        LobbyProtectionService lobbyProtectionService,
        CommandTabBlockService commandTabBlockService,
        LobbyItemService lobbyItemService,
        ScoreboardService scoreboardService,
        DisplayService displayService,
        TabService tabService
) {
    public PluginLogger getPluginLogger() {
        return pluginLogger;
    }
}
