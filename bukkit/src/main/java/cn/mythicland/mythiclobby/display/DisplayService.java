package cn.mythicland.mythiclobby.display;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.DisplayConfig;
import cn.mythicland.mythiclobby.config.LobbyConfig;
import cn.mythicland.mythiclobby.config.key.DisplayConfigKey;
import cn.mythicland.mythiclobby.config.key.LobbyConfigKey;
import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
@InjectComponent
public final class DisplayService {
    private static final String TEAM_PREFIX = "mld-";
    private static final String LEGACY_TEAM_PREFIX = "mldisplay-";
    private static final int TEAM_ID_LENGTH = 16 - TEAM_PREFIX.length();

    private final JavaPlugin plugin;
    private final DisplayConfig displayConfig;
    private final LobbyConfig lobbyConfig;
    private final DisplayRenderer renderer;
    private final PluginLogger logger;

    private DisplaySettings settings;
    private BukkitTask refreshTask;

    public DisplayService(
            JavaPlugin plugin,
            DisplayConfig displayConfig,
            LobbyConfig lobbyConfig,
            DisplayRenderer renderer,
            PluginLogger logger
    ) {
        this.plugin = plugin;
        this.displayConfig = displayConfig;
        this.lobbyConfig = lobbyConfig;
        this.renderer = renderer;
        this.logger = logger;
        this.settings = loadSettings();
    }

    public void start() {
        settings = loadSettings();
        if (!settings.enabled()) {
            cleanupPluginTeams();
            return;
        }
        ensureAllTeams();
        scheduleRefreshTask();
        refreshAll();
    }

    public void reloadAll() {
        settings = loadSettings();
        cleanupPluginTeams();
        if (!settings.enabled()) {
            cancelRefreshTask();
            return;
        }
        ensureAllTeams();
        scheduleRefreshTask();
        refreshAll();
    }

    public void shutdown() {
        cancelRefreshTask();
        cleanupPluginTeams();
    }

    public void handleJoin(Player player) {
        if (!settings.enabled()) {
            return;
        }
        ensureTeam(player);
        refreshPlayer(player);
    }

    public void handleQuit(Player player) {
        removePlayerTeam(player);
    }

    public String renderJoinMessage(Player player) {
        return renderBroadcastMessage(player, settings.joinMessage());
    }

    public String renderQuitMessage(Player player) {
        return renderBroadcastMessage(player, settings.quitMessage());
    }

    public boolean shouldUseCustomJoinMessage() {
        return settings.customJoinMessageEnabled();
    }

    public boolean shouldUseCustomQuitMessage() {
        return settings.customQuitMessageEnabled();
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
            ensureTeam(player);
            refreshPlayer(player);
        }
    }

    private void ensureAllTeams() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ensureTeam(player);
        }
    }

    private void refreshPlayer(Player player) {
        Team team = ensureTeam(player);
        String prefix = truncate(
                renderer.renderPrefix(player, settings.prefix()),
                "prefix",
                player.getName()
        );
        String suffix = truncate(
                renderer.renderSuffix(player, settings.suffix()),
                "suffix",
                player.getName()
        );

        if (!Objects.equals(team.getPrefix(), prefix)) {
            team.setPrefix(prefix);
        }
        if (!Objects.equals(team.getSuffix(), suffix)) {
            team.setSuffix(suffix);
        }
    }

    private Team ensureTeam(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = getTeamName(player.getUniqueId());
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        if (!team.hasEntry(player.getName())) {
            removePlayerFromPluginTeams(player.getName(), teamName);
            team.addEntry(player.getName());
        }
        return team;
    }

    private void removePlayerTeam(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(getTeamName(player.getUniqueId()));
        if (team == null) {
            return;
        }
        if (team.hasEntry(player.getName())) {
            team.removeEntry(player.getName());
        }
        if (team.getEntries().isEmpty()) {
            team.unregister();
        }
    }

    private void cleanupPluginTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        List<Team> teamsToRemove = new ArrayList<>();
        for (Team team : scoreboard.getTeams()) {
            if (isPluginTeam(team)) {
                teamsToRemove.add(team);
            }
        }
        for (Team team : teamsToRemove) {
            team.unregister();
        }
    }

    private void removePlayerFromPluginTeams(String playerName, String currentTeamName) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team team : scoreboard.getTeams()) {
            if (!isPluginTeam(team) || team.getName().equals(currentTeamName)) {
                continue;
            }
            if (team.hasEntry(playerName)) {
                team.removeEntry(playerName);
                if (team.getEntries().isEmpty()) {
                    team.unregister();
                }
            }
        }
    }

    private boolean isPluginTeam(Team team) {
        String name = team.getName();
        return name.startsWith(TEAM_PREFIX) || name.startsWith(LEGACY_TEAM_PREFIX);
    }

    private String getTeamName(UUID uuid) {
        String compactUuid = uuid.toString().replace("-", "");
        return TEAM_PREFIX + compactUuid.substring(0, TEAM_ID_LENGTH);
    }

    private String truncate(String text, String kind, String playerName) {
        if (text.length() <= DisplaySettings.TEAM_TEXT_LIMIT) {
            return text;
        }

        String truncated = text.substring(0, DisplaySettings.TEAM_TEXT_LIMIT);
        if (truncated.endsWith("\u00A7")) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }

        logger.warning(String.format(
                Locale.ROOT,
                "display.yml: %s for player %s exceeds %d characters after rendering, truncating.",
                kind,
                playerName,
                DisplaySettings.TEAM_TEXT_LIMIT
        ));
        return truncated;
    }

    private DisplaySettings loadSettings() {
        YamlConfiguration yaml = displayConfig.data();

        boolean enabled = yaml.getBoolean(DisplayConfigKey.ENABLED.getPath(), true);

        int updateIntervalTicks = yaml.getInt(
                DisplayConfigKey.UPDATE_INTERVAL_TICKS.getPath(),
                DisplaySettings.DEFAULT_UPDATE_INTERVAL_TICKS
        );
        if (updateIntervalTicks <= 0) {
            logger.warning("display.yml: update-interval-ticks must be > 0, falling back to 20.");
            updateIntervalTicks = DisplaySettings.DEFAULT_UPDATE_INTERVAL_TICKS;
        }

        String prefix = yaml.getString(DisplayConfigKey.NAME_FORMAT_PREFIX.getPath(), "");
        if (prefix == null) {
            prefix = "";
        }

        String suffix = yaml.getString(DisplayConfigKey.NAME_FORMAT_SUFFIX.getPath(), "");
        if (suffix == null) {
            suffix = "";
        }

        YamlConfiguration lobbyYaml = lobbyConfig.data();

        boolean customJoinMessageEnabled = hasConfiguredMessage(lobbyYaml, LobbyConfigKey.MESSAGES_JOIN_MESSAGE);
        String joinMessage = lobbyYaml.getString(LobbyConfigKey.MESSAGES_JOIN_MESSAGE.getPath());
        if (joinMessage == null) {
            joinMessage = DisplaySettings.DEFAULT_JOIN_MESSAGE;
        }

        boolean customQuitMessageEnabled = hasConfiguredMessage(lobbyYaml, LobbyConfigKey.MESSAGES_QUIT_MESSAGE);
        String quitMessage = lobbyYaml.getString(LobbyConfigKey.MESSAGES_QUIT_MESSAGE.getPath());
        if (quitMessage == null) {
            quitMessage = DisplaySettings.DEFAULT_QUIT_MESSAGE;
        }

        return new DisplaySettings(
                enabled,
                updateIntervalTicks,
                prefix,
                suffix,
                customJoinMessageEnabled,
                joinMessage,
                customQuitMessageEnabled,
                quitMessage
        );
    }

    private String renderBroadcastMessage(Player player, String message) {
        if (message == null) {
            return null;
        }

        String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return renderer.renderMessage(player, message);
    }

    private boolean hasConfiguredMessage(YamlConfiguration yaml, LobbyConfigKey key) {
        String value = yaml.getString(key.getPath());
        return value != null && !value.trim().isEmpty();
    }
}
