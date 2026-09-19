package cn.mythicland.mythiclobby.listener;

import cn.mythicland.framework.bukkit.ListenerComponent;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.npc.NpcManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

@InjectComponent
@ListenerComponent
public class NpcVisibilityListener implements Listener {
    private final JavaPlugin plugin;
    private final NpcManager npcManager;

    public NpcVisibilityListener(JavaPlugin plugin, NpcManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            npcManager.showAllToPlayer(player);
        });
    }
}
