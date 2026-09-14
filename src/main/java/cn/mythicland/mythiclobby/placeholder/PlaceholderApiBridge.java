package cn.mythicland.mythiclobby.placeholder;

import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class PlaceholderApiBridge {
    private final PluginLogger logger;
    private volatile Method setPlaceholdersMethod;
    private volatile boolean resolutionAttempted;

    PlaceholderApiBridge(PluginLogger logger) {
        this.logger = logger;
    }

    void reload() {
        setPlaceholdersMethod = null;
        resolutionAttempted = false;
    }

    String apply(Player player, String text) {
        Method method = resolveMethod();
        if (method == null) {
            return text;
        }

        try {
            Object result = method.invoke(null, player, text);
            return result instanceof String ? (String) result : text;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return text;
        }
    }

    private Method resolveMethod() {
        Method cached = setPlaceholdersMethod;
        if (cached != null || resolutionAttempted) return cached;

        Plugin placeholderApiPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderApiPlugin == null || !placeholderApiPlugin.isEnabled()) {
            resolutionAttempted = true;
            return null;
        }

        try {
            Class<?> apiClass = Class.forName(
                    "me.clip.placeholderapi.PlaceholderAPI",
                    true,
                    placeholderApiPlugin.getClass().getClassLoader()
            );
            Method method = CompatibleMethodResolver.find(
                    apiClass,
                    "setPlaceholders",
                    true,
                    Player.class,
                    String.class
            );
            setPlaceholdersMethod = method;
            return method;
        } catch (ReflectiveOperationException exception) {
            logger.warning("Failed to hook PlaceholderAPI, falling back to built-in placeholders only.");
            return null;
        } finally {
            resolutionAttempted = true;
        }
    }
}
