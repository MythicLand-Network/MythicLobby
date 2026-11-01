package cn.mythicland.mythiclobby;

import cn.mythicland.framework.bukkit.bootstrap.BukkitComponents;
import cn.mythicland.framework.bukkit.bootstrap.BukkitComponents.Capability;
import cn.mythicland.framework.bukkit.task.ScheduledTaskLifecycle;
import cn.mythicland.framework.component.ComponentContainer;
import cn.mythicland.mythiclobby.config.ConfigManager;
import cn.mythicland.mythiclobby.display.DisplayService;
import cn.mythicland.mythiclobby.gui.menu.LobbyMenuService;
import cn.mythicland.mythiclobby.item.LobbyItemService;
import cn.mythicland.mythiclobby.listener.CommandTabBlockService;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import cn.mythicland.mythiclobby.scoreboard.ScoreboardService;
import cn.mythicland.mythiclobby.tab.TabService;
import cn.mythicland.npclib.api.skin.MojangSkinService;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoaderBootstrap {
    private final JavaPlugin plugin;

    public LoaderBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PluginContext prepare() {
        PluginLogger pluginLogger = new PluginLogger(plugin);
        ComponentContainer components = BukkitComponents.create(plugin, Capability.MENUS, Capability.SCHEDULED_TASKS);
        components.bind(pluginLogger);
        components.bind(
                MojangSkinService.class,
                new MojangSkinService(
                        plugin.getDataFolder().toPath().resolve("cache").resolve("npc-skins.properties")
                )
        );
        components.scan(
                "cn.mythicland.mythiclobby",
                type -> !type.getName().startsWith("cn.mythicland.mythiclobby.velocity.")
        );
        components.resolve(ScheduledTaskLifecycle.class).start();

        return new PluginContext(
                plugin,
                pluginLogger,
                components.resolve(ConfigManager.class),
                components,
                components.resolve(PluginReloader.class),
                components.resolve(LobbyMenuService.class),
                components.resolve(BungeeConnectService.class),
                components.resolve(LobbyProtectionService.class),
                components.resolve(CommandTabBlockService.class),
                components.resolve(LobbyItemService.class),
                components.resolve(ScoreboardService.class),
                components.resolve(DisplayService.class),
                components.resolve(TabService.class)
        );
    }
}
