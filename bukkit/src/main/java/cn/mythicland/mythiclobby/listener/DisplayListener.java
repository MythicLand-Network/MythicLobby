package cn.mythicland.mythiclobby.listener;

import cn.mythicland.framework.bukkit.ListenerComponent;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.display.DisplayService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@InjectComponent
@ListenerComponent
public final class DisplayListener implements Listener {
    private final DisplayService displayService;

    public DisplayListener(DisplayService displayService) {
        this.displayService = displayService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        displayService.handleJoin(event.getPlayer());
        if (!displayService.shouldUseCustomJoinMessage()) {
            return;
        }

        event.setJoinMessage(null);
        String joinMessage = displayService.renderJoinMessage(event.getPlayer());
        if (joinMessage != null) {
            Bukkit.broadcastMessage(joinMessage);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (displayService.shouldUseCustomQuitMessage()) {
            event.setQuitMessage(null);
            String quitMessage = displayService.renderQuitMessage(event.getPlayer());
            if (quitMessage != null) {
                Bukkit.broadcastMessage(quitMessage);
            }
        }
        displayService.handleQuit(event.getPlayer());
    }
}
