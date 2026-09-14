package cn.mythicland.mythiclobby.listener;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.plugin.java.JavaPlugin;

@InjectComponent
public final class CommandTabBlockService {
    private final JavaPlugin plugin;
    private final LobbyProtectionService lobbyProtectionService;
    private final ProtocolManager protocolManager;

    private PacketListener packetListener;

    public CommandTabBlockService(JavaPlugin plugin, LobbyProtectionService lobbyProtectionService) {
        this.plugin = plugin;
        this.lobbyProtectionService = lobbyProtectionService;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void start() {
        if (packetListener != null) return;
        packetListener = new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.TAB_COMPLETE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                String input = event.getPacket().getStrings().read(0);
                if (input == null || !input.startsWith("/")) return;
                if (!lobbyProtectionService.shouldBlockCommandTabComplete(event.getPlayer())) return;
                event.setCancelled(true);
            }
        };
        protocolManager.addPacketListener(packetListener);
    }

    public void reloadAll() {
        shutdown();
        start();
    }

    public void shutdown() {
        if (packetListener == null) {
            return;
        }

        protocolManager.removePacketListener(packetListener);
        packetListener = null;
    }
}
