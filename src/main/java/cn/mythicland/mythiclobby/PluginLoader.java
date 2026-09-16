package cn.mythicland.mythiclobby;

import cn.mythicland.mythiclobby.config.ConfigManager;
import cn.mythicland.mythiclobby.gui.menu.LobbyMenuService;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.framework.bukkit.BukkitComponentRegistrar;
import cn.mythicland.framework.bukkit.BukkitRegistration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class PluginLoader {
    private static PluginContext context;
    private static BukkitRegistration registration;

    private PluginLoader() {
    }

    public static void load(JavaPlugin plugin) {
        context = prepare(plugin);
        try {
            registration = register(context);
            start(context);
        } catch (RuntimeException | Error exception) {
            try {
                if (registration != null) registration.close();
            } catch (RuntimeException | Error rollbackFailure) {
                exception.addSuppressed(rollbackFailure);
            }
            try {
                context.components().unload();
            } catch (RuntimeException | Error rollbackFailure) {
                exception.addSuppressed(rollbackFailure);
            } finally {
                registration = null;
                context = null;
            }
            throw exception;
        }
    }

    public static void unload() {
        if (context == null) {
            return;
        }

        Throwable failure = null;
        try {
            if (registration != null) registration.close();
        } catch (RuntimeException | Error exception) {
            failure = exception;
        }
        try {
            shutdown(context);
        } catch (RuntimeException | Error exception) {
            if (failure == null) failure = exception;
            else failure.addSuppressed(exception);
        } finally {
            registration = null;
            context = null;
        }
        if (failure instanceof Error error) throw error;
        if (failure instanceof RuntimeException exception) throw exception;
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

    public static LobbyMenuService getMenuManager() {
        return getContext().menuManager();
    }

    private static BukkitRegistration register(PluginContext context) {
        return BukkitComponentRegistrar.register(context.plugin(), context.components());
    }

    private static void start(PluginContext context) {
        List<Runnable> shutdownActions = new ArrayList<>();
        try {
            context.bungeeConnectService().start();
            shutdownActions.add(context.bungeeConnectService()::shutdown);
            context.lobbyProtectionService().start();
            shutdownActions.add(context.lobbyProtectionService()::shutdown);
            context.commandTabBlockService().start();
            shutdownActions.add(context.commandTabBlockService()::shutdown);
            context.lobbyItemService().start();
            context.scoreboardService().start();
            shutdownActions.add(context.scoreboardService()::shutdown);
            context.displayService().start();
            shutdownActions.add(context.displayService()::shutdown);
            context.tabService().start();
            shutdownActions.add(context.tabService()::shutdown);
        } catch (RuntimeException | Error exception) {
            rollback(shutdownActions, exception);
            throw exception;
        }
    }

    private static void shutdown(PluginContext context) {
        List<Runnable> shutdownActions = List.of(
                context.displayService()::shutdown,
                context.tabService()::shutdown,
                context.scoreboardService()::shutdown,
                context.commandTabBlockService()::shutdown,
                context.lobbyProtectionService()::shutdown,
                context.bungeeConnectService()::shutdown,
                context.components()::unload
        );
        Throwable failure = null;
        for (Runnable shutdownAction : shutdownActions) {
            try {
                shutdownAction.run();
            } catch (RuntimeException | Error exception) {
                if (failure == null) failure = exception;
                else failure.addSuppressed(exception);
            }
        }
        if (failure instanceof Error error) throw error;
        if (failure instanceof RuntimeException exception) throw exception;
    }

    private static void rollback(List<Runnable> shutdownActions, Throwable failure) {
        for (int index = shutdownActions.size() - 1; index >= 0; index--) {
            try {
                shutdownActions.get(index).run();
            } catch (RuntimeException | Error rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
        }
    }

    private static PluginContext getContext() {
        if (context == null) {
            throw new IllegalStateException("PluginLoader is not initialized.");
        }
        return context;
    }
}
