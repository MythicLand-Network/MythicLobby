package cn.mythicland.mythiclobby;

import cn.mythicland.mythiclobby.config.ConfigManager;
import cn.mythicland.mythiclobby.display.DisplayService;
import cn.mythicland.mythiclobby.gui.menu.LobbyMenuService;
import cn.mythicland.mythiclobby.item.LobbyItemService;
import cn.mythicland.mythiclobby.listener.CommandTabBlockService;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import cn.mythicland.framework.component.ComponentContainer;
import cn.mythicland.framework.component.ComponentResolver;
import cn.mythicland.framework.bukkit.task.BukkitTaskScheduler;
import cn.mythicland.framework.bukkit.task.ScheduledTaskLifecycle;
import cn.mythicland.framework.bukkit.task.TaskScheduler;
import cn.mythicland.framework.menu.MenuManager;
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
        ComponentContainer components = new ComponentContainer();
        components.bind(plugin);
        components.bind(pluginLogger);
        components.bind(
                MojangSkinService.class,
                new MojangSkinService(
                        plugin.getDataFolder().toPath().resolve("cache").resolve("npc-skins.properties")
                )
        );
        components.bind(components);
        components.bind(ComponentResolver.class, components);
        TaskScheduler taskScheduler = new BukkitTaskScheduler(plugin);
        components.bind(TaskScheduler.class, taskScheduler);
        MenuManager menuManager = new MenuManager(plugin, components, taskScheduler);
        components.bind(menuManager);
        ScheduledTaskLifecycle scheduledTaskLifecycle = new ScheduledTaskLifecycle(components, taskScheduler);
        components.bind(scheduledTaskLifecycle);
        components.scan(
                "cn.mythicland.mythiclobby",
                type -> !type.getName().startsWith("cn.mythicland.mythiclobby.velocity.")
        );
        scheduledTaskLifecycle.start();

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
