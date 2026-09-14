package cn.mythicland.mythiclobby.placeholder;

import cn.mythicland.mythiclobby.placeholder.LuckPermsBridge.LuckPermsData;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class BuiltinPlaceholderResolver {
    private static final Pattern NETWORK_SERVER_PLACEHOLDER_PATTERN =
            Pattern.compile("<network_online:([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern NETWORK_SERVER_SUM_PLACEHOLDER_PATTERN =
            Pattern.compile("<network_online_sum:([^>]+)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEGACY_NETWORK_SERVER_PLACEHOLDER_PATTERN =
            Pattern.compile("%network_online:([^%]+)%", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEGACY_NETWORK_SERVER_SUM_PLACEHOLDER_PATTERN =
            Pattern.compile("%network_online_sum:([^%]+)%", Pattern.CASE_INSENSITIVE);
    private static final Map<String, String> LEGACY_PLACEHOLDER_ALIASES = createLegacyPlaceholderAliases();

    private final BungeeConnectService bungeeConnectService;
    private final LuckPermsBridge luckPermsBridge;

    BuiltinPlaceholderResolver(BungeeConnectService bungeeConnectService, LuckPermsBridge luckPermsBridge) {
        this.bungeeConnectService = bungeeConnectService;
        this.luckPermsBridge = luckPermsBridge;
    }

    void reload() {
        luckPermsBridge.reload();
    }

    Map<String, String> resolveValues(Player player) {
        Map<String, String> values = new LinkedHashMap<>();
        LuckPermsData luckPermsData = luckPermsBridge.resolve(player);
        Location location = player == null ? null : player.getLocation();
        World world = location == null ? null : location.getWorld();

        values.put("player_name", player == null ? "" : player.getName());
        values.put("player_display_name", player == null ? "" : nullToEmpty(player.getDisplayName()));
        values.put("player_list_name", resolvePlayerListName(player));
        values.put("player_uuid", player == null ? "" : player.getUniqueId().toString());
        values.put("player_ping", Integer.toString(resolvePing(player)));
        values.put("player_world", world == null ? "" : world.getName());
        values.put("player_x", formatDouble(location == null ? 0.0D : location.getX()));
        values.put("player_y", formatDouble(location == null ? 0.0D : location.getY()));
        values.put("player_z", formatDouble(location == null ? 0.0D : location.getZ()));
        values.put("player_block_x", Integer.toString(location == null ? 0 : location.getBlockX()));
        values.put("player_block_y", Integer.toString(location == null ? 0 : location.getBlockY()));
        values.put("player_block_z", Integer.toString(location == null ? 0 : location.getBlockZ()));
        values.put("player_yaw", formatDouble(location == null ? 0.0F : location.getYaw()));
        values.put("player_pitch", formatDouble(location == null ? 0.0F : location.getPitch()));
        values.put("player_health", formatDouble(player == null ? 0.0D : player.getHealth()));
        values.put("player_max_health", formatDouble(player == null ? 0.0D : player.getMaxHealth()));
        values.put("player_food", Integer.toString(player == null ? 0 : player.getFoodLevel()));
        values.put("player_level", Integer.toString(player == null ? 0 : player.getLevel()));
        values.put("player_exp", formatDouble(player == null ? 0.0F : player.getExp()));
        values.put("player_total_exp", Integer.toString(player == null ? 0 : player.getTotalExperience()));
        values.put(
                "player_gamemode",
                player == null || player.getGameMode() == null ? "" : player.getGameMode().name()
        );
        values.put("player_allow_flight", Boolean.toString(player != null && player.getAllowFlight()));
        values.put("player_walk_speed", formatDouble(player == null ? 0.0F : player.getWalkSpeed()));
        values.put("player_fly_speed", formatDouble(player == null ? 0.0F : player.getFlySpeed()));
        values.put("luckperms_prefix", luckPermsData.prefix());
        values.put("luckperms_suffix", luckPermsData.suffix());
        values.put("luckperms_primary_group_name", luckPermsData.primaryGroup());
        values.put("network_online", Integer.toString(bungeeConnectService.getNetworkOnlineCount()));
        values.put("server_online", Integer.toString(Bukkit.getOnlinePlayers().size()));
        values.put(
                "world_online",
                Integer.toString(world == null ? Bukkit.getOnlinePlayers().size() : world.getPlayers().size())
        );
        values.put("server_max_players", Integer.toString(Bukkit.getMaxPlayers()));
        return values;
    }

    String applyLegacyAliasFallbacks(String input, Map<String, String> values) {
        String rendered = applyNetworkServerPlaceholders(input);
        for (Map.Entry<String, String> entry : LEGACY_PLACEHOLDER_ALIASES.entrySet()) {
            String replacement = values.get(entry.getValue());
            if (replacement != null) {
                rendered = rendered.replace(entry.getKey(), replacement);
            }
        }
        return rendered;
    }

    String applyNetworkServerPlaceholders(String input) {
        String rendered = replaceNetworkServerPlaceholder(input, NETWORK_SERVER_SUM_PLACEHOLDER_PATTERN, true);
        rendered = replaceNetworkServerPlaceholder(rendered, NETWORK_SERVER_PLACEHOLDER_PATTERN, false);
        rendered = replaceNetworkServerPlaceholder(rendered, LEGACY_NETWORK_SERVER_SUM_PLACEHOLDER_PATTERN, true);
        return replaceNetworkServerPlaceholder(rendered, LEGACY_NETWORK_SERVER_PLACEHOLDER_PATTERN, false);
    }

    private String replaceNetworkServerPlaceholder(String input, Pattern pattern, boolean sumMode) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            List<String> serverNames = parseServerNames(matcher.group(1));
            int onlineCount = sumMode
                    ? bungeeConnectService.getServersOnlineCount(serverNames)
                    : bungeeConnectService.getServerOnlineCount(serverNames.isEmpty() ? "" : serverNames.get(0));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(Integer.toString(onlineCount)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String resolvePlayerListName(Player player) {
        if (player == null) {
            return "";
        }
        String playerListName = player.getPlayerListName();
        if (playerListName == null || playerListName.trim().isEmpty()) {
            return player.getName();
        }
        return playerListName;
    }

    private int resolvePing(Player player) {
        if (player == null) {
            return 0;
        }

        try {
            Object spigot = player.spigot();
            Method getPing = spigot.getClass().getMethod("getPing");
            return ((Number) getPing.invoke(spigot)).intValue();
        } catch (ReflectiveOperationException ignored) {
            // Fall through to NMS lookup.
        }

        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            Field pingField = handle.getClass().getField("ping");
            return ((Number) pingField.get(handle)).intValue();
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    private static List<String> parseServerNames(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return Arrays.stream(rawInput.split(","))
                .map(String::trim)
                .filter(serverName -> !serverName.isEmpty())
                .collect(Collectors.toList());
    }

    private static Map<String, String> createLegacyPlaceholderAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("%player_name%", "player_name");
        aliases.put("%player_display_name%", "player_display_name");
        aliases.put("%player_displayname%", "player_display_name");
        aliases.put("%player_list_name%", "player_list_name");
        aliases.put("%player_uuid%", "player_uuid");
        aliases.put("%player_ping%", "player_ping");
        aliases.put("%player_world%", "player_world");
        aliases.put("%player_x%", "player_x");
        aliases.put("%player_y%", "player_y");
        aliases.put("%player_z%", "player_z");
        aliases.put("%player_health%", "player_health");
        aliases.put("%player_food%", "player_food");
        aliases.put("%player_level%", "player_level");
        aliases.put("%player_gamemode%", "player_gamemode");
        aliases.put("%luckperms_prefix%", "luckperms_prefix");
        aliases.put("%luckperms_suffix%", "luckperms_suffix");
        aliases.put("%luckperms_primary_group_name%", "luckperms_primary_group_name");
        aliases.put("%bungee_total%", "network_online");
        aliases.put("%server_online%", "server_online");
        aliases.put("%world_online%", "world_online");
        aliases.put("%server_max_players%", "server_max_players");
        return aliases;
    }

    private static String formatDouble(double value) {
        String formatted = String.format(Locale.ROOT, "%.2f", value);
        if (!formatted.contains(".")) {
            return formatted;
        }
        int end = formatted.length();
        while (end > 0 && formatted.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && formatted.charAt(end - 1) == '.') {
            end--;
        }
        return formatted.substring(0, end);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
