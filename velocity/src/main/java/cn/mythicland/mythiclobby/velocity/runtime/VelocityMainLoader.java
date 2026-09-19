package cn.mythicland.mythiclobby.velocity.runtime;

import cn.mythicland.mythiclobby.velocity.MythicLobbyVelocity;
import cn.mythicland.mythiclobby.velocity.PluginLoader;

public final class VelocityMainLoader {
    private final MythicLobbyVelocity plugin;

    public VelocityMainLoader(MythicLobbyVelocity plugin) {
        this.plugin = plugin;
    }

    public void load() {
    }

    public void enable() {
        PluginLoader.onEnable(this.plugin);
    }

    public void unload() {
        PluginLoader.onDisable();
    }
}
