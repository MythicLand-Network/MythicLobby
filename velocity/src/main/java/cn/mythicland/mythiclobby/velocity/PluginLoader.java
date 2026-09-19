package cn.mythicland.mythiclobby.velocity;

public final class PluginLoader {
    private static LoaderBootstrap bootstrap;

    private PluginLoader() {
    }

    public static void onEnable(MythicLobbyVelocity plugin) {
        bootstrap = LoaderBootstrap.create(plugin);
        bootstrap.register();
    }

    public static void onDisable() {
        if (bootstrap == null) return;
        try {
            bootstrap.unregister();
        } finally {
            bootstrap = null;
        }
    }
}
