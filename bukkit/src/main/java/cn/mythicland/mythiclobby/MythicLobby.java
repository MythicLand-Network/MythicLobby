package cn.mythicland.mythiclobby;

import cn.mythicland.framework.runtime.RuntimeBootstrap;
import cn.mythicland.framework.runtime.FrameworkRuntimeClassLoading;
import cn.mythicland.mythiclobby.bootstrap.RuntimeBootstrapFactory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;

public final class MythicLobby extends JavaPlugin {
    private RuntimeBootstrap runtimeBootstrap;

    @Override
    public void onEnable() {
        try {
            this.runtimeBootstrap = RuntimeBootstrapFactory.create(
                    this,
                    "cn.mythicland.mythiclobby.runtime.MainLoader",
                    getDataFolder().toPath(),
                    Set.of("common", "bukkit"),
                    getLogger()::info,
                    getLogger()::warning,
                    FrameworkRuntimeClassLoading.bukkit(
                            List.of(
                                    "cn.mythicland.mythiclobby.",
                                    "cn.mythicland.npclib.",
                                    "org.reflections.",
                                    "javassist.",
                                    "fr.mrmicky.fastboard.",
                                    "it.unimi.dsi.fastutil."
                            ),
                            List.of(MythicLobby.class.getName()),
                            List.of("cn.mythicland.mythiclobby.bootstrap.")
                    )
            );
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
