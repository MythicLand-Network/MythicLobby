package cn.mythicland.mythiclobby;

import cn.mythicland.framework.bukkit.bootstrap.ManagedRuntimePlugin;
import cn.mythicland.framework.runtime.FrameworkRuntimeClassLoading;
import cn.mythicland.framework.runtime.RuntimeBootstrap;
import cn.mythicland.mythiclobby.bootstrap.RuntimeBootstrapFactory;
import java.util.List;
import java.util.Set;

public final class MythicLobby extends ManagedRuntimePlugin {
    @Override
    protected RuntimeBootstrap createRuntime() {
        return RuntimeBootstrapFactory.create(
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
                                "fr.mrmicky.fastboard.",
                                "it.unimi.dsi.fastutil."
                        ),
                        List.of(MythicLobby.class.getName()),
                        List.of("cn.mythicland.mythiclobby.bootstrap.")
                )
        );
    }
}
