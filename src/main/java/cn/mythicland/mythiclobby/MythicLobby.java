package cn.mythicland.mythiclobby;

import cn.mythicland.mythiclobby.bootstrap.RuntimeBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public final class MythicLobby extends JavaPlugin {
    private RuntimeBootstrap runtimeBootstrap;

    @Override
    public void onEnable() {
        try {
            this.runtimeBootstrap = new RuntimeBootstrap(this);
            this.runtimeBootstrap.load();
            this.runtimeBootstrap.enable();
        } catch (RuntimeException exception) {
            getLogger().severe("Failed to enable MythicLobby: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (this.runtimeBootstrap == null) return;
        this.runtimeBootstrap.disable();
        this.runtimeBootstrap = null;
    }
}
