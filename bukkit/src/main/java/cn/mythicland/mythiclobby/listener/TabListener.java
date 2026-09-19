package cn.mythicland.mythiclobby.listener;

import cn.mythicland.framework.bukkit.ListenerComponent;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.tab.TabService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@InjectComponent
@ListenerComponent
public final class TabListener implements Listener {
    private final TabService tabService;

    public TabListener(TabService tabService) {
        this.tabService = tabService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        tabService.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        tabService.handleQuit(event.getPlayer());
    }
}
