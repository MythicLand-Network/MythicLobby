package cn.mythicland.mythiclobby.npc.skin;

import cn.mythicland.framework.component.InjectComponent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@InjectComponent
public final class BukkitAsyncExecutor implements AsyncExecutor {
    private final Plugin plugin;

    public BukkitAsyncExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
}
