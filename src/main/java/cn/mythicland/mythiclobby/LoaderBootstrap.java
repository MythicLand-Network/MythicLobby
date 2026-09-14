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
import cn.mythicland.framework.component.ComponentResolver;
import cn.mythicland.mythiclobby.scoreboard.ScoreboardService;
import cn.mythicland.mythiclobby.tab.TabService;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoaderBootstrap {
    private final JavaPlugin plugin;

    public LoaderBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PluginContext prepare() {
        PluginLogger pluginLogger = new PluginLogger(plugin);
        ComponentContainer components = new ComponentContainer();
        components.bind(plugin);
        components.bind(pluginLogger);
        components.bind(components);
        components.bind(ComponentResolver.class, components);
        components.scan("cn.mythicland.mythiclobby");

        return new PluginContext(
                plugin,
                pluginLogger,
                components.resolve(ConfigManager.class),
                components,
                components.resolve(PluginReloader.class),
                components.resolve(MenuManager.class),
                components.resolve(BungeeConnectService.class),
                components.resolve(HologramService.class),
                components.resolve(NpcManager.class),
                components.resolve(LobbyProtectionService.class),
                components.resolve(CommandTabBlockService.class),
                components.resolve(LobbyItemService.class),
                components.resolve(ScoreboardService.class),
                components.resolve(DisplayService.class),
                components.resolve(TabService.class)
        );
    }
}
