package cn.mythicland.mythiclobby.proxy;

import cn.mythicland.framework.component.InjectComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@InjectComponent
public final class BungeeConnectService implements PluginMessageListener {
    private static final String CHANNEL = "BungeeCord";
    private static final long PLAYER_COUNT_UPDATE_INTERVAL_TICKS = 40L;

    private final JavaPlugin plugin;
    private BukkitTask playerCountTask;
    private volatile int networkOnlineCount = -1;
    private final Map<String, Integer> serverOnlineCounts = new ConcurrentHashMap<>();
    private final Set<String> trackedServers = ConcurrentHashMap.newKeySet();
    private boolean channelsRegistered;

    public BungeeConnectService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        registerChannels();
        schedulePlayerCountUpdates();
        requestNetworkPlayerCount();
    }

    public void reloadAll() {
        registerChannels();
        networkOnlineCount = -1;
        serverOnlineCounts.clear();
        schedulePlayerCountUpdates();
        requestNetworkPlayerCount();
    }

    public void shutdown() {
        if (playerCountTask != null) {
            playerCountTask.cancel();
            playerCountTask = null;
        }
        if (!channelsRegistered) {
            return;
        }

        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL, this);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL);
        channelsRegistered = false;
    }

    public int getNetworkOnlineCount() {
        int cached = networkOnlineCount;
        if (cached >= 0) {
            return cached;
        }

        requestNetworkPlayerCount();
        return Bukkit.getOnlinePlayers().size();
    }

    public int getServerOnlineCount(String serverName) {
        String normalizedServerName = normalizeServerName(serverName);
        if (normalizedServerName.isEmpty()) {
            return 0;
        }

        trackedServers.add(normalizedServerName);
        Integer cached = serverOnlineCounts.get(normalizedServerName);
        if (cached != null) {
            return Math.max(cached, 0);
        }

        requestPlayerCount(normalizedServerName);
        return 0;
    }

    public int getServersOnlineCount(Collection<String> serverNames) {
        if (serverNames == null || serverNames.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (String serverName : serverNames) {
            total += getServerOnlineCount(serverName);
        }
        return total;
    }

    public void connect(Player player, String serverName) {
        try {
            sendPluginMessage(player, output -> {
                output.writeUTF("Connect");
                output.writeUTF(serverName);
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to send BungeeCord connect message", exception);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CHANNEL.equals(channel)) {
            return;
        }

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = input.readUTF();
            if (!"PlayerCount".equalsIgnoreCase(subChannel)) {
                return;
            }

            String serverName = input.readUTF();
            int onlineCount = input.readInt();
            if ("ALL".equalsIgnoreCase(serverName)) {
                networkOnlineCount = onlineCount;
                return;
            }

            String normalizedServerName = normalizeServerName(serverName);
            if (!normalizedServerName.isEmpty()) {
                serverOnlineCounts.put(normalizedServerName, onlineCount);
            }
        } catch (IOException ignored) {
            // Ignore malformed plugin messages and keep the last cached value.
        }
    }

    private void schedulePlayerCountUpdates() {
        if (playerCountTask != null) {
            playerCountTask.cancel();
        }

        playerCountTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::requestTrackedPlayerCounts,
                0L,
                PLAYER_COUNT_UPDATE_INTERVAL_TICKS
        );
    }

    private void requestTrackedPlayerCounts() {
        requestNetworkPlayerCount();
        for (String serverName : trackedServers) {
            requestPlayerCount(serverName);
        }
    }

    private void requestNetworkPlayerCount() {
        requestPlayerCount("ALL");
    }

    private void requestPlayerCount(String targetServer) {
        Player sender = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (sender == null) {
            return;
        }

        String normalizedTarget = "ALL".equalsIgnoreCase(targetServer) ? "ALL" : normalizeServerName(targetServer);
        if (normalizedTarget.isEmpty()) {
            return;
        }

        try {
            sendPluginMessage(sender, output -> {
                output.writeUTF("PlayerCount");
                output.writeUTF(normalizedTarget);
            });
        } catch (IOException ignored) {
            // Keep the previous cached value and fall back to local online count.
        }
    }

    private String normalizeServerName(String serverName) {
        return serverName == null ? "" : serverName.trim();
    }

    private void sendPluginMessage(Player player, PluginMessageWriter writer) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(byteStream)) {
            writer.write(output);
        }
        player.sendPluginMessage(plugin, CHANNEL, byteStream.toByteArray());
    }

    private void registerChannels() {
        if (channelsRegistered) {
            return;
        }

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
        channelsRegistered = true;
    }

    @FunctionalInterface
    private interface PluginMessageWriter {
        void write(DataOutputStream output) throws IOException;
    }
}
