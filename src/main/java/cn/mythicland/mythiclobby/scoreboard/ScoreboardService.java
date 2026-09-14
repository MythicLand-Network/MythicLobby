package cn.mythicland.mythiclobby.scoreboard;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.ScoreboardConfig;
import cn.mythicland.mythiclobby.config.key.ScoreboardConfigKey;
import cn.mythicland.mythiclobby.log.PluginLogger;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@InjectComponent
public final class ScoreboardService {
    private final JavaPlugin plugin;
    private final ScoreboardConfig scoreboardConfig;
    private final ScoreboardRenderer renderer;
    private final PluginLogger logger;
    private final Map<UUID, FastBoard> boards = new LinkedHashMap<>();

    private ScoreboardSettings settings;
    private BukkitTask refreshTask;

    public ScoreboardService(
            JavaPlugin plugin,
            ScoreboardConfig scoreboardConfig,
            ScoreboardRenderer renderer,
            PluginLogger logger
    ) {
        this.plugin = plugin;
        this.scoreboardConfig = scoreboardConfig;
        this.renderer = renderer;
        this.logger = logger;
        this.settings = loadSettings();
    }

    public void start() {
        settings = loadSettings();
        applySettingsToOnlinePlayers();
        scheduleRefreshTask();
        refreshAll();
    }

    public void reloadAll() {
        settings = loadSettings();
        applySettingsToOnlinePlayers();
        scheduleRefreshTask();
        refreshAll();
    }

    public void shutdown() {
        cancelRefreshTask();
        deleteAllBoards();
    }

    public void handleJoin(Player player) {
        if (!settings.enabled()) {
            removeBoard(player);
            return;
        }

        createBoard(player);
        refreshBoard(player);
    }

    public void handleQuit(Player player) {
        removeBoard(player);
    }

    private void applySettingsToOnlinePlayers() {
        if (!settings.enabled()) {
            deleteAllBoards();
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            createBoard(player);
        }
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            createBoard(player);
            refreshBoard(player);
        }
    }

    private void refreshBoard(Player player) {
        FastBoard board = boards.get(player.getUniqueId());
        if (board == null || board.isDeleted()) {
            return;
        }

        board.updateTitle(renderer.renderTitle(player, settings.title()));
        board.updateLines(renderer.renderLines(player, settings.lines()));
    }

    private void createBoard(Player player) {
        boards.computeIfAbsent(player.getUniqueId(), ignored -> new FastBoard(player));
    }

    private void removeBoard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
    }

    private void deleteAllBoards() {
        for (FastBoard board : new ArrayList<>(boards.values())) {
            if (!board.isDeleted()) {
                board.delete();
            }
        }
        boards.clear();
    }

    private ScoreboardSettings loadSettings() {
        YamlConfiguration yaml = scoreboardConfig.getYamlConfiguration();

        boolean enabled = yaml.getBoolean(ScoreboardConfigKey.ENABLED.getPath(), true);

        int updateIntervalTicks = yaml.getInt(
                ScoreboardConfigKey.UPDATE_INTERVAL_TICKS.getPath(),
                ScoreboardSettings.DEFAULT_UPDATE_INTERVAL_TICKS
        );
        if (updateIntervalTicks <= 0) {
            logger.warning("scoreboard.yml: update-interval-ticks must be > 0, falling back to 20.");
            updateIntervalTicks = ScoreboardSettings.DEFAULT_UPDATE_INTERVAL_TICKS;
        }

        String title = yaml.getString(ScoreboardConfigKey.TITLE.getPath(), ScoreboardSettings.DEFAULT_TITLE);
        if (title == null || title.trim().isEmpty()) {
            logger.warning("scoreboard.yml: title is empty, falling back to default title.");
            title = ScoreboardSettings.DEFAULT_TITLE;
        }

        List<String> lines;
        if (yaml.contains(ScoreboardConfigKey.LINES.getPath()) && !yaml.isList(ScoreboardConfigKey.LINES.getPath())) {
            logger.warning("scoreboard.yml: lines must be a list, falling back to an empty list.");
            lines = new ArrayList<>();
        } else {
            lines = new ArrayList<>(yaml.getStringList(ScoreboardConfigKey.LINES.getPath()));
        }

        if (lines.size() > ScoreboardSettings.MAX_LINES) {
            logger.warning("scoreboard.yml: lines exceed the safe limit of " + ScoreboardSettings.MAX_LINES + ", truncating.");
            lines = new ArrayList<>(lines.subList(0, ScoreboardSettings.MAX_LINES));
        }

        return new ScoreboardSettings(enabled, updateIntervalTicks, title, lines);
    }
}
