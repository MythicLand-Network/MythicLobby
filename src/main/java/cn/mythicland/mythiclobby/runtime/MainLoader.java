package cn.mythicland.mythiclobby.runtime;

import cn.mythicland.mythiclobby.MythicLobby;
import cn.mythicland.mythiclobby.PluginLoader;

public final class MainLoader {
    private final MythicLobby plugin;

    public MainLoader(MythicLobby plugin) {
        this.plugin = plugin;
    }

    public void load() {
    }

    public void enable() {
        PluginLoader.load(this.plugin);
    }

    public void unload() {
        PluginLoader.unload();
    }
}
