package cn.mythicland.mythiclobby;

import cn.mythicland.mythiclobby.config.ConfigManager;
import cn.mythicland.mythiclobby.gui.menu.MenuManager;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.framework.bukkit.BukkitComponentRegistrar;
import cn.mythicland.framework.command.BukkitCommandRegistrar;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginLoader {
    private static PluginContext context;

    private PluginLoader() {
    }

    public static void load(JavaPlugin plugin) {
        context = prepare(plugin);
        register(context);
        start(context);
    }

    public static void unload() {
        if (context == null) {
            return;
        }

        HandlerList.unregisterAll(context.plugin());
        shutdown(context);
        context = null;
    }

    public static JavaPlugin getInstance() {
        return getContext().plugin();
    }

    public static PluginLogger getPluginLogger() {
        return getContext().getPluginLogger();
    }

    public static PluginContext prepare(JavaPlugin plugin) {
        return new LoaderBootstrap(plugin).prepare();
    }

    public static ConfigManager getConfigManager() {
        return getContext().configManager();
    }

    public static MenuManager getMenuManager() {
        return getContext().menuManager();
    }

    private static void register(PluginContext context) {
        BukkitCommandRegistrar.register(context.plugin(), context.components());
        BukkitComponentRegistrar.registerListeners(context.plugin(), context.components());
    }

    private static void start(PluginContext context) {
        context.bungeeConnectService().start();
        context.lobbyProtectionService().start();
        context.commandTabBlockService().start();
        context.lobbyItemService().start();
        context.scoreboardService().start();
        context.displayService().start();
        context.tabService().start();
    }

    private static void shutdown(PluginContext context) {
        context.displayService().shutdown();
        context.tabService().shutdown();
        context.scoreboardService().shutdown();
        context.commandTabBlockService().shutdown();
        context.lobbyProtectionService().shutdown();
        context.bungeeConnectService().shutdown();
        context.components().unload();
    }

    private static PluginContext getContext() {
        if (context == null) {
            throw new IllegalStateException("PluginLoader is not initialized.");
        }
        return context;
    }
}
