package cn.mythicland.mythiclobby.bootstrap;

import cn.mythicland.framework.runtime.RuntimeBootstrap;
import cn.mythicland.mythiclobby.MythicLobby;

import java.util.List;
import java.util.Set;

public final class RuntimeBootstrapFactory {
    private static final String MAIN_LOADER = "cn.mythicland.mythiclobby.runtime.MainLoader";

    private RuntimeBootstrapFactory() {
    }

    public static RuntimeBootstrap create(MythicLobby plugin) {
        return RuntimeBootstrap.builder(plugin, MAIN_LOADER)
                .runtimeName("MythicLobby")
                .libraries(
                        plugin.getDataFolder().toPath(),
                        "META-INF/mythiclobby-libraries.properties",
                        Set.of("bukkit")
                )
                .loggers(plugin.getLogger()::info, plugin.getLogger()::warning)
                .childFirstPackages(List.of(
                        "cn.mythicland.mythiclobby.",
                        "cn.mythicland.npclib.",
                        "org.reflections.",
                        "javassist.",
                        "fr.mrmicky.fastboard.",
                        "it.unimi.dsi.fastutil."
                ))
                .parentFirstClasses(List.of(MythicLobby.class.getName()))
                .parentFirstPackages(List.of("cn.mythicland.mythiclobby.bootstrap."))
                .build();
    }
}
