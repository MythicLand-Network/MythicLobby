package cn.mythicland.mythiclobby.placeholder;

import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class LuckPermsBridge {
    private final PluginLogger logger;
    private volatile Object api;
    private volatile boolean resolutionAttempted;

    LuckPermsBridge(PluginLogger logger) {
        this.logger = logger;
    }

    private static Object invokeMethod(Object target, String methodName, Object... arguments)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = CompatibleMethodResolver.findForArguments(
                target.getClass(),
                methodName,
                false,
                arguments
        );
        return method.invoke(target, arguments);
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    void reload() {
        api = null;
        resolutionAttempted = false;
    }

    LuckPermsData resolve(Player player) {
        if (player == null) {
            return LuckPermsData.EMPTY;
        }

        Object resolvedApi = resolveApi();
        if (resolvedApi == null) {
            return LuckPermsData.EMPTY;
        }

        try {
            Object playerAdapter = invokeMethod(resolvedApi, "getPlayerAdapter", Player.class);
            Object metaData = invokeMethod(playerAdapter, "getMetaData", player);
            Object user = invokeMethod(playerAdapter, "getUser", player);
            if (user == null) {
                return LuckPermsData.EMPTY;
            }

            return getLuckPermsData(metaData, user);
        } catch (ReflectiveOperationException exception) {
            return resolveUsingCachedDataFallback(resolvedApi, player);
        }
    }

    @NotNull
    private LuckPermsBridge.LuckPermsData getLuckPermsData(Object metaData, Object user) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String prefix = metaData == null ? "" : stringValue(invokeMethod(metaData, "getPrefix"));
        String suffix = metaData == null ? "" : stringValue(invokeMethod(metaData, "getSuffix"));
        String primaryGroup = stringValue(invokeMethod(user, "getPrimaryGroup"));
        return new LuckPermsData(prefix, suffix, primaryGroup);
    }

    private Object resolveApi() {
        Object cached = api;
        if (cached != null || resolutionAttempted) return cached;

        Plugin luckPermsPlugin = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (luckPermsPlugin == null || !luckPermsPlugin.isEnabled()) {
            resolutionAttempted = true;
            return null;
        }

        try {
            Class<?> providerClass = Class.forName(
                    "net.luckperms.api.LuckPermsProvider",
                    true,
                    luckPermsPlugin.getClass().getClassLoader()
            );
            Method getMethod = CompatibleMethodResolver.find(providerClass, "get", true);
            Object resolvedApi = getMethod.invoke(null);
            api = resolvedApi;
            return resolvedApi;
        } catch (ReflectiveOperationException exception) {
            logger.warning("Failed to hook LuckPerms, prefix and group placeholders will stay empty.");
            return null;
        } finally {
            resolutionAttempted = true;
        }
    }

    private LuckPermsData resolveUsingCachedDataFallback(Object resolvedApi, Player player) {
        try {
            Object playerAdapter = invokeMethod(resolvedApi, "getPlayerAdapter", Player.class);
            Object user = invokeMethod(playerAdapter, "getUser", player);
            if (user == null) {
                return LuckPermsData.EMPTY;
            }

            Object queryOptions = invokeMethod(playerAdapter, "getQueryOptions", player);
            Object cachedData = invokeMethod(user, "getCachedData");
            Object metaData = invokeMethod(cachedData, "getMetaData", queryOptions);

            return getLuckPermsData(metaData, user);
        } catch (ReflectiveOperationException ignored) {
            return LuckPermsData.EMPTY;
        }
    }

    record LuckPermsData(String prefix, String suffix, String primaryGroup) {
        private static final LuckPermsData EMPTY = new LuckPermsData("", "", "");
    }
}
