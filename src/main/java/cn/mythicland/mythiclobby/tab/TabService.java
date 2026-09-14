package cn.mythicland.mythiclobby.tab;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.TabConfig;
import cn.mythicland.mythiclobby.config.key.TabConfigKey;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.platform.tab.TabPlatform;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
@InjectComponent
public final class TabService {
    private final JavaPlugin plugin;
    private final TabConfig tabConfig;
    private final TabRenderer renderer;
    private final TabPlatform tabPlatform;
    private final PluginLogger logger;

    private TabSettings settings;
    private BukkitTask refreshTask;

    public TabService(
            JavaPlugin plugin,
            TabConfig tabConfig,
            TabRenderer renderer,
            TabPlatform tabPlatform,
            PluginLogger logger
    ) {
        this.plugin = plugin;
        this.tabConfig = tabConfig;
        this.renderer = renderer;
        this.tabPlatform = tabPlatform;
        this.logger = logger;
        this.settings = loadSettings();
    }

    public void start() {
        settings = loadSettings();
        if (!settings.enabled()) {
            cancelRefreshTask();
            resetAllPlayers();
            return;
        }
        scheduleRefreshTask();
        refreshAll();
    }

    public void reloadAll() {
        settings = loadSettings();
        if (!settings.enabled()) {
            cancelRefreshTask();
            resetAllPlayers();
            return;
        }
        scheduleRefreshTask();
        refreshAll();
    }

    public void shutdown() {
        cancelRefreshTask();
        resetAllPlayers();
    }

    public void handleJoin(Player player) {
        if (!settings.enabled()) {
            restorePlayerListName(player);
            clearHeaderFooter(player);
            return;
        }

        refreshPlayer(player);
        // Joining players can receive a later vanilla tab reset, so we re-apply shortly after login.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                refreshPlayer(player);
            }
        }, 2L);
    }

    public void handleQuit(Player player) {
        restorePlayerListName(player);
    }

    private void scheduleRefreshTask() {
        cancelRefreshTask();
        if (!settings.enabled()) {
            return;
        }

        refreshTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::refreshAll,
                0L,
                settings.updateIntervalTicks()
        );
    }

    private void cancelRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    private void refreshAll() {
        if (!settings.enabled()) {
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            sendHeaderFooter(viewer);
        }
        refreshNamesForAllPlayers();
    }

    private void refreshNamesForAllPlayers() {
        for (Player target : Bukkit.getOnlinePlayers()) {
            updatePlayerListName(target);
        }
    }

    private void refreshPlayer(Player player) {
        updatePlayerListName(player);
        sendHeaderFooter(player);
    }

    private void sendHeaderFooter(Player viewer) {
        tabPlatform.sendHeaderFooter(
                viewer,
                renderer.renderHeader(viewer, settings.header()),
                renderer.renderFooter(viewer, settings.footer())
        );
    }

    private void clearHeaderFooter(Player viewer) {
        tabPlatform.clearHeaderFooter(viewer);
    }

    private void updatePlayerListName(Player player) {
        String playerListName = renderer.renderDisplayName(player, settings.prefix(), settings.suffix());
        try {
            player.setPlayerListName(playerListName);
        } catch (IllegalArgumentException exception) {
            String truncated = playerListName.substring(0, Math.min(16, playerListName.length()));
            if (truncated.endsWith("\u00A7")) {
                truncated = truncated.substring(0, truncated.length() - 1);
            }
            logger.warning("tab.yml: player list name for " + player.getName() + " is too long, truncating to 16 characters.");
            player.setPlayerListName(truncated);
        }
    }

    private void resetAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            restorePlayerListName(player);
            clearHeaderFooter(player);
        }
    }

    private void restorePlayerListName(Player player) {
        player.setPlayerListName(null);
    }

    private TabSettings loadSettings() {
        YamlConfiguration yaml = tabConfig.getYamlConfiguration();

        boolean enabled = yaml.getBoolean(TabConfigKey.ENABLED.getPath(), true);

        int updateIntervalTicks = yaml.getInt(
                TabConfigKey.UPDATE_INTERVAL_TICKS.getPath(),
                TabSettings.DEFAULT_UPDATE_INTERVAL_TICKS
        );
        if (updateIntervalTicks <= 0) {
            logger.warning("tab.yml: update-interval-ticks must be > 0, falling back to 20.");
            updateIntervalTicks = TabSettings.DEFAULT_UPDATE_INTERVAL_TICKS;
        }

        List<String> header = readStringList(yaml, TabConfigKey.HEADER, "tab.yml: header must be a list, falling back to an empty list.");
        List<String> footer = readStringList(yaml, TabConfigKey.FOOTER, "tab.yml: footer must be a list, falling back to an empty list.");

        String prefix = yaml.getString(TabConfigKey.NAME_FORMAT_PREFIX.getPath(), "");
        if (prefix == null) {
            prefix = "";
        }

        String suffix = yaml.getString(TabConfigKey.NAME_FORMAT_SUFFIX.getPath(), "");
        if (suffix == null) {
            suffix = "";
        }

        return new TabSettings(enabled, updateIntervalTicks, header, footer, prefix, suffix);
    }

    private List<String> readStringList(YamlConfiguration yaml, TabConfigKey key, String warning) {
        if (yaml.contains(key.getPath()) && !yaml.isList(key.getPath())) {
            logger.warning(warning);
            return new ArrayList<>();
        }
        return new ArrayList<>(yaml.getStringList(key.getPath()));
    }
}
