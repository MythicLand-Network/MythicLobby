package cn.mythicland.mythiclobby.lobby;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.LobbyConfig;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.util.LocationNormalizer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
@InjectComponent
public final class LobbyProtectionService {
    private static final double SAME_LOCATION_THRESHOLD = 0.0001D;
    private static final float DEFAULT_WALK_SPEED = 0.2F;
    private static final float DEFAULT_FLY_SPEED = 0.1F;
    private static final long DEFAULT_LOCK_TIME = 6000L;
    private static final String COMMAND_TAB_BYPASS_PERMISSION = "mythiclobby.commandtab.bypass";

    private final JavaPlugin plugin;
    private final LobbyConfig lobbyConfig;
    private final PluginLogger logger;

    private LobbySettings settings;
    private BukkitTask timeLockTask;

    public LobbyProtectionService(JavaPlugin plugin, LobbyConfig lobbyConfig, PluginLogger logger) {
        this.plugin = plugin;
        this.lobbyConfig = lobbyConfig;
        this.logger = logger;
        this.settings = loadSettings();
    }

    public void start() {
        restartTimeLockTask();
        applyToOnlinePlayers();
    }

    public void shutdown() {
        cancelTimeLockTask();
    }

    public void reloadAll() {
        settings = loadSettings();
        restartTimeLockTask();
        applyToOnlinePlayers();
    }

    public boolean isEnabled() {
        return settings.enabled();
    }

    public boolean shouldBlockCommandTabComplete(Player player) {
        if (player == null) {
            return false;
        }

        return settings.commandTabComplete().enabled()
                && !player.hasPermission(COMMAND_TAB_BYPASS_PERMISSION);
    }

    public boolean shouldHandleDeathRespawn(Player player) {
        return player != null
                && settings.enabled()
                && settings.spawn().enabled()
                && settings.spawn().teleportOnRespawn()
                && settings.spawn().teleportOnDeath()
                && settings.spawn().location() != null;
    }

    public void handleDeath(Player player) {
        if (!shouldHandleDeathRespawn(player)) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> forceRespawn(player));
    }

    public boolean isLobbyWorld(World world) {
        if (!settings.enabled() || world == null) {
            return false;
        }
        return settings.lobbyWorlds().contains(normalizeWorldName(world.getName()));
    }

    public boolean shouldProtect(Player player, ProtectionFlag flag) {
        return player != null
                && isLobbyWorld(player.getWorld())
                && settings.protections().isEnabled(flag);
    }

    public boolean shouldApplyWorldRule(World world, WorldRuleFlag flag) {
        return isLobbyWorld(world) && settings.worldRules().isEnabled(flag);
    }

    public void handleJoin(Player player) {
        if (!settings.enabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            if (settings.spawn().enabled() && settings.spawn().teleportOnJoin()) {
                teleportToSpawn(player, Trigger.JOIN, true);
                return;
            }

            applyStateForTrigger(player, Trigger.JOIN);
        });
    }

    public Location getRespawnLocation(Player player) {
        if (player == null
                || !settings.enabled()
                || !settings.spawn().enabled()
                || !settings.spawn().teleportOnRespawn()) {
            return null;
        }

        return getConfiguredSpawnLocation(true);
    }

    public void handleRespawn(Player player) {
        if (!settings.enabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            applyStateForTrigger(player, Trigger.RESPAWN);
        });
    }

    public void handleWorldChange(Player player) {
        if (!settings.enabled() || !isLobbyWorld(player.getWorld())) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || !isLobbyWorld(player.getWorld())) {
                return;
            }

            if (settings.spawn().enabled() && settings.spawn().teleportOnReturnToLobby()) {
                teleportToSpawn(player, Trigger.WORLD_CHANGE, true);
                return;
            }

            applyStateForTrigger(player, Trigger.WORLD_CHANGE);
        });
    }

    public TeleportResult teleportPlayerToSpawn(Player player) {
        if (!settings.enabled()) {
            return TeleportResult.DISABLED;
        }
        if (!settings.spawn().enabled()) {
            return TeleportResult.SPAWN_DISABLED;
        }
        return teleportToSpawn(player, Trigger.COMMAND, false);
    }

    public boolean setSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        Location normalizedLocation = LocationNormalizer.normalizeSpawnLocation(location);

        YamlConfiguration yaml = lobbyConfig.getYamlConfiguration();
        yaml.set("spawn.enabled", true);
        yaml.set("spawn.location.world", normalizedLocation.getWorld().getName());
        yaml.set("spawn.location.x", normalizedLocation.getX());
        yaml.set("spawn.location.y", normalizedLocation.getY());
        yaml.set("spawn.location.z", normalizedLocation.getZ());
        yaml.set("spawn.location.yaw", normalizedLocation.getYaw());
        yaml.set("spawn.location.pitch", normalizedLocation.getPitch());

        List<String> lobbyWorlds = new ArrayList<>(yaml.getStringList("lobby-worlds"));
        if (!containsWorld(lobbyWorlds, normalizedLocation.getWorld().getName())) {
            lobbyWorlds.add(normalizedLocation.getWorld().getName());
            yaml.set("lobby-worlds", lobbyWorlds);
        }

        lobbyConfig.saveYamlConfiguration();
        settings = loadSettings();
        restartTimeLockTask();
        return true;
    }

    private TeleportResult teleportToSpawn(Player player, Trigger trigger, boolean silent) {
        Location spawnLocation = getConfiguredSpawnLocation(silent);
        if (spawnLocation == null) {
            return TeleportResult.SPAWN_NOT_SET;
        }

        if (!isSameLocation(player.getLocation(), spawnLocation)) {
            player.teleport(spawnLocation);
        }
        if (trigger == Trigger.COMMAND || settings.playerState().applyOn().isEnabled(trigger)) {
            applyState(player);
        }
        return TeleportResult.SUCCESS;
    }

    private void applyToOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isLobbyWorld(player.getWorld())) {
                applyState(player);
            }
        }
    }

    private void applyStateForTrigger(Player player, Trigger trigger) {
        if (!isLobbyWorld(player.getWorld())) {
            return;
        }
        if (!settings.playerState().applyOn().isEnabled(trigger)) {
            return;
        }
        applyState(player);
    }

    private void applyState(Player player) {
        PlayerStateSettings playerState = settings.playerState();
        player.setGameMode(playerState.gamemode());
        player.setHealth(Math.min(player.getMaxHealth(), playerState.health()));
        player.setFoodLevel(playerState.foodLevel());
        player.setSaturation(playerState.saturation());
        player.setAllowFlight(playerState.allowFlight());
        if (!playerState.allowFlight() && player.isFlying()) {
            player.setFlying(false);
        }
        player.setWalkSpeed(playerState.walkSpeed());
        player.setFlySpeed(playerState.flySpeed());
        player.setFallDistance(0.0F);
        if (playerState.extinguish()) {
            player.setFireTicks(0);
        }
        if (playerState.clearActivePotions()) {
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                player.removePotionEffect(potionEffect.getType());
            }
        }
    }

    private void forceRespawn(Player player) {
        if (!player.isOnline() || !player.isDead()) {
            return;
        }

        try {
            player.spigot().respawn();
        } catch (Throwable ignored) {
            // Ignore servers that do not expose the respawn bridge.
        }
    }

    private void restartTimeLockTask() {
        cancelTimeLockTask();
        if (!settings.enabled() || !settings.worldRules().lockTimeEnabled()) {
            return;
        }

        timeLockTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (String worldName : settings.lobbyWorlds()) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    world.setTime(settings.worldRules().lockedTime());
                }
            }
        }, 1L, 20L);
    }

    private void cancelTimeLockTask() {
        if (timeLockTask != null) {
            timeLockTask.cancel();
            timeLockTask = null;
        }
    }

    private Location getConfiguredSpawnLocation(boolean silent) {
        SpawnSettings spawn = settings.spawn();
        if (!spawn.enabled()) {
            if (!silent) {
                logger.warning("config.yml: spawn is disabled.");
            }
            return null;
        }

        if (spawn.location() == null) {
            if (!silent) {
                logger.warning("config.yml: spawn.location is missing or invalid.");
            }
            return null;
        }
        return spawn.location().clone();
    }

    private LobbySettings loadSettings() {
        YamlConfiguration yaml = lobbyConfig.getYamlConfiguration();
        boolean enabled = yaml.getBoolean("enabled", true);
        CommandTabCompleteSettings commandTabCompleteSettings = readCommandTabCompleteSettings(yaml);
        Set<String> lobbyWorlds = readLobbyWorlds(yaml);
        SpawnSettings spawnSettings = readSpawnSettings(yaml);
        PlayerStateSettings playerStateSettings = readPlayerStateSettings(yaml);
        ProtectionSettings protectionSettings = readProtectionSettings(yaml);
        WorldRuleSettings worldRuleSettings = readWorldRuleSettings(yaml);

        if (enabled && lobbyWorlds.isEmpty()) {
            logger.warning("config.yml: lobby-worlds is empty, lobby protections are effectively disabled.");
        }

        return new LobbySettings(
                enabled,
                commandTabCompleteSettings,
                lobbyWorlds,
                spawnSettings,
                playerStateSettings,
                protectionSettings,
                worldRuleSettings
        );
    }

    private CommandTabCompleteSettings readCommandTabCompleteSettings(YamlConfiguration yaml) {
        boolean enabled = yaml.getBoolean("block-command-tab.enabled", false);
        return new CommandTabCompleteSettings(enabled);
    }

    private Set<String> readLobbyWorlds(YamlConfiguration yaml) {
        Set<String> lobbyWorlds = new LinkedHashSet<>();
        for (String worldName : yaml.getStringList("lobby-worlds")) {
            String normalized = normalizeWorldName(worldName);
            if (!normalized.isEmpty()) {
                lobbyWorlds.add(normalized);
            }
        }
        return lobbyWorlds;
    }

    private SpawnSettings readSpawnSettings(YamlConfiguration yaml) {
        boolean enabled = yaml.getBoolean("spawn.enabled", true);
        boolean join = yaml.getBoolean("spawn.teleport-on.join", true);
        boolean respawn = yaml.getBoolean("spawn.teleport-on.respawn", true);
        boolean teleportOnDeath = yaml.getBoolean(
                "spawn.teleport-on.death",
                yaml.getBoolean("death-respawn.enabled", false)
        );
        boolean returnToLobby = yaml.getBoolean("spawn.teleport-on.return-to-lobby", true);
        String worldName = trimToEmpty(yaml.getString("spawn.location.world"));
        if (worldName.isEmpty()) {
            return new SpawnSettings(enabled, join, respawn, teleportOnDeath, returnToLobby, null);
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            logger.warning("config.yml: spawn.location.world '" + worldName + "' does not exist.");
            return new SpawnSettings(enabled, join, respawn, teleportOnDeath, returnToLobby, null);
        }

        Location location = new Location(
                world,
                yaml.getDouble("spawn.location.x", world.getSpawnLocation().getX()),
                yaml.getDouble("spawn.location.y", world.getSpawnLocation().getY()),
                yaml.getDouble("spawn.location.z", world.getSpawnLocation().getZ()),
                (float) yaml.getDouble("spawn.location.yaw", world.getSpawnLocation().getYaw()),
                (float) yaml.getDouble("spawn.location.pitch", world.getSpawnLocation().getPitch())
        );
        return new SpawnSettings(enabled, join, respawn, teleportOnDeath, returnToLobby, location);
    }

    private PlayerStateSettings readPlayerStateSettings(YamlConfiguration yaml) {
        ApplyOnSettings applyOnSettings = new ApplyOnSettings(
                yaml.getBoolean("player-state.apply-on.join", true),
                yaml.getBoolean("player-state.apply-on.respawn", true),
                yaml.getBoolean("player-state.apply-on.world-change", true)
        );

        int rawGameMode = yaml.getInt("player-state.gamemode", 2);
        GameMode gameMode = parseGameMode(rawGameMode);

        double health = yaml.getDouble("player-state.health", 20.0D);
        if (health <= 0.0D) {
            logger.warning("config.yml: player-state.health must be > 0, falling back to 20.0.");
            health = 20.0D;
        }

        int foodLevel = Math.max(0, Math.min(20, yaml.getInt("player-state.food-level", 20)));
        float saturation = Math.max(
                0.0F,
                Math.min(20.0F, (float) yaml.getDouble("player-state.saturation", 20.0D))
        );
        float walkSpeed = clampSpeed(
                "player-state.walk-speed",
                (float) yaml.getDouble("player-state.walk-speed", DEFAULT_WALK_SPEED),
                DEFAULT_WALK_SPEED
        );
        float flySpeed = clampSpeed(
                "player-state.fly-speed",
                (float) yaml.getDouble("player-state.fly-speed", DEFAULT_FLY_SPEED),
                DEFAULT_FLY_SPEED
        );

        return new PlayerStateSettings(
                applyOnSettings,
                gameMode,
                health,
                foodLevel,
                saturation,
                yaml.getBoolean("player-state.extinguish", true),
                yaml.getBoolean("player-state.clear-active-potions", true),
                yaml.getBoolean("player-state.allow-flight", false),
                walkSpeed,
                flySpeed
        );
    }

    private ProtectionSettings readProtectionSettings(YamlConfiguration yaml) {
        return new ProtectionSettings(
                yaml.getBoolean("protections.block-break", true),
                yaml.getBoolean("protections.block-place", true),
                yaml.getBoolean("protections.bucket-fill", true),
                yaml.getBoolean("protections.bucket-empty", true),
                yaml.getBoolean("protections.block-interact", true),
                yaml.getBoolean("protections.item-drop", true),
                yaml.getBoolean("protections.item-pickup", true),
                yaml.getBoolean("protections.inventory-move", true),
                yaml.getBoolean("protections.inventory-drag", true),
                yaml.getBoolean("protections.inventory-number-key", true),
                yaml.getBoolean("protections.consume-items", true),
                yaml.getBoolean("protections.hunger-change", true),
                yaml.getBoolean("protections.pvp-damage", true),
                yaml.getBoolean("protections.mob-damage", true),
                yaml.getBoolean("protections.fall-damage", true),
                yaml.getBoolean("protections.fire-damage", true),
                yaml.getBoolean("protections.void-damage", true),
                yaml.getBoolean("protections.projectile-damage", true),
                yaml.getBoolean("protections.explosion-damage", true)
        );
    }

    private WorldRuleSettings readWorldRuleSettings(YamlConfiguration yaml) {
        long lockedTime = yaml.getLong("world-rules.lock-time.value", DEFAULT_LOCK_TIME);
        lockedTime %= 24000L;
        if (lockedTime < 0L) {
            lockedTime += 24000L;
        }

        return new WorldRuleSettings(
                yaml.getBoolean("world-rules.disable-weather-change", true),
                yaml.getBoolean("world-rules.disable-mob-spawn", true),
                yaml.getBoolean("world-rules.disable-item-spawn", true),
                yaml.getBoolean("world-rules.disable-leaves-decay", true),
                yaml.getBoolean("world-rules.disable-block-explode", true),
                yaml.getBoolean("world-rules.disable-entity-explode", true),
                yaml.getBoolean("world-rules.lock-time.enabled", false),
                lockedTime
        );
    }

    private float clampSpeed(String path, float value, float fallback) {
        if (value < -1.0F || value > 1.0F) {
            logger.warning("config.yml: " + path + " must be between -1.0 and 1.0, falling back to " + fallback + ".");
            return fallback;
        }
        return value;
    }

    private GameMode parseGameMode(int rawGameMode) {
        switch (rawGameMode) {
            case 0:
                return GameMode.SURVIVAL;
            case 1:
                return GameMode.CREATIVE;
            case 2:
                return GameMode.ADVENTURE;
            default:
                logger.warning(
                        "config.yml: player-state.gamemode '" + rawGameMode
                                + "' is invalid, falling back to 2 (ADVENTURE)."
                );
                return GameMode.ADVENTURE;
        }
    }

    private boolean containsWorld(List<String> worldNames, String targetWorld) {
        String normalizedTarget = normalizeWorldName(targetWorld);
        for (String worldName : worldNames) {
            if (normalizeWorldName(worldName).equals(normalizedTarget)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeWorldName(String worldName) {
        return trimToEmpty(worldName).toLowerCase(Locale.ROOT);
    }

    private String trimToEmpty(String input) {
        return input == null ? "" : input.trim();
    }

    private boolean isSameLocation(Location first, Location second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.getWorld() == null || second.getWorld() == null) {
            return false;
        }
        if (!first.getWorld().equals(second.getWorld())) {
            return false;
        }
        if (first.distanceSquared(second) > SAME_LOCATION_THRESHOLD) {
            return false;
        }
        return Math.abs(first.getYaw() - second.getYaw()) < 0.01F
                && Math.abs(first.getPitch() - second.getPitch()) < 0.01F;
    }

    public enum ProtectionFlag {
        BLOCK_BREAK,
        BLOCK_PLACE,
        BUCKET_FILL,
        BUCKET_EMPTY,
        BLOCK_INTERACT,
        ITEM_DROP,
        ITEM_PICKUP,
        INVENTORY_MOVE,
        INVENTORY_DRAG,
        INVENTORY_NUMBER_KEY,
        CONSUME_ITEMS,
        HUNGER_CHANGE,
        PVP_DAMAGE,
        MOB_DAMAGE,
        FALL_DAMAGE,
        FIRE_DAMAGE,
        VOID_DAMAGE,
        PROJECTILE_DAMAGE,
        EXPLOSION_DAMAGE
    }

    public enum WorldRuleFlag {
        DISABLE_WEATHER_CHANGE,
        DISABLE_MOB_SPAWN,
        DISABLE_ITEM_SPAWN,
        DISABLE_LEAVES_DECAY,
        DISABLE_BLOCK_EXPLODE,
        DISABLE_ENTITY_EXPLODE
    }

    private enum Trigger {
        JOIN,
        RESPAWN,
        WORLD_CHANGE,
        COMMAND
    }

    public enum TeleportResult {
        SUCCESS,
        DISABLED,
        SPAWN_DISABLED,
        SPAWN_NOT_SET
    }

    private record LobbySettings(
            boolean enabled,
            CommandTabCompleteSettings commandTabComplete,
            Set<String> lobbyWorlds,
            SpawnSettings spawn,
            PlayerStateSettings playerState,
            ProtectionSettings protections,
            WorldRuleSettings worldRules
    ) {
        private LobbySettings(
                boolean enabled,
                CommandTabCompleteSettings commandTabComplete,
                Set<String> lobbyWorlds,
                SpawnSettings spawn,
                PlayerStateSettings playerState,
                ProtectionSettings protections,
                WorldRuleSettings worldRules
        ) {
            this.enabled = enabled;
            this.commandTabComplete = commandTabComplete;
            this.lobbyWorlds = new LinkedHashSet<>(lobbyWorlds);
            this.spawn = spawn;
            this.playerState = playerState;
            this.protections = protections;
            this.worldRules = worldRules;
        }

        @Override
        public Set<String> lobbyWorlds() {
            return new LinkedHashSet<>(lobbyWorlds);
        }
    }

    private record CommandTabCompleteSettings(boolean enabled) {
    }

    private record SpawnSettings(
            boolean enabled,
            boolean teleportOnJoin,
            boolean teleportOnRespawn,
            boolean teleportOnDeath,
            boolean teleportOnReturnToLobby,
            Location location
    ) {
        private SpawnSettings(
                boolean enabled,
                boolean teleportOnJoin,
                boolean teleportOnRespawn,
                boolean teleportOnDeath,
                boolean teleportOnReturnToLobby,
                Location location
        ) {
            this.enabled = enabled;
            this.teleportOnJoin = teleportOnJoin;
            this.teleportOnRespawn = teleportOnRespawn;
            this.teleportOnDeath = teleportOnDeath;
            this.teleportOnReturnToLobby = teleportOnReturnToLobby;
            this.location = location == null ? null : location.clone();
        }

        @Override
        public Location location() {
            return location == null ? null : location.clone();
        }
    }

    private record PlayerStateSettings(
            ApplyOnSettings applyOn,
            GameMode gamemode,
            double health,
            int foodLevel,
            float saturation,
            boolean extinguish,
            boolean clearActivePotions,
            boolean allowFlight,
            float walkSpeed,
            float flySpeed
    ) {
    }

    private record ApplyOnSettings(boolean join, boolean respawn, boolean worldChange) {
        private boolean isEnabled(Trigger trigger) {
            switch (trigger) {
                case JOIN:
                    return join;
                case RESPAWN:
                    return respawn;
                case WORLD_CHANGE:
                    return worldChange;
                case COMMAND:
                    return true;
                default:
                    return false;
            }
        }
    }

    private record ProtectionSettings(
            boolean blockBreak,
            boolean blockPlace,
            boolean bucketFill,
            boolean bucketEmpty,
            boolean blockInteract,
            boolean itemDrop,
            boolean itemPickup,
            boolean inventoryMove,
            boolean inventoryDrag,
            boolean inventoryNumberKey,
            boolean consumeItems,
            boolean hungerChange,
            boolean pvpDamage,
            boolean mobDamage,
            boolean fallDamage,
            boolean fireDamage,
            boolean voidDamage,
            boolean projectileDamage,
            boolean explosionDamage
    ) {
        private boolean isEnabled(ProtectionFlag flag) {
            switch (flag) {
                case BLOCK_BREAK:
                    return blockBreak;
                case BLOCK_PLACE:
                    return blockPlace;
                case BUCKET_FILL:
                    return bucketFill;
                case BUCKET_EMPTY:
                    return bucketEmpty;
                case BLOCK_INTERACT:
                    return blockInteract;
                case ITEM_DROP:
                    return itemDrop;
                case ITEM_PICKUP:
                    return itemPickup;
                case INVENTORY_MOVE:
                    return inventoryMove;
                case INVENTORY_DRAG:
                    return inventoryDrag;
                case INVENTORY_NUMBER_KEY:
                    return inventoryNumberKey;
                case CONSUME_ITEMS:
                    return consumeItems;
                case HUNGER_CHANGE:
                    return hungerChange;
                case PVP_DAMAGE:
                    return pvpDamage;
                case MOB_DAMAGE:
                    return mobDamage;
                case FALL_DAMAGE:
                    return fallDamage;
                case FIRE_DAMAGE:
                    return fireDamage;
                case VOID_DAMAGE:
                    return voidDamage;
                case PROJECTILE_DAMAGE:
                    return projectileDamage;
                case EXPLOSION_DAMAGE:
                    return explosionDamage;
                default:
                    return false;
            }
        }
    }

    private record WorldRuleSettings(
            boolean disableWeatherChange,
            boolean disableMobSpawn,
            boolean disableItemSpawn,
            boolean disableLeavesDecay,
            boolean disableBlockExplode,
            boolean disableEntityExplode,
            boolean lockTimeEnabled,
            long lockedTime
    ) {
        private boolean isEnabled(WorldRuleFlag flag) {
            switch (flag) {
                case DISABLE_WEATHER_CHANGE:
                    return disableWeatherChange;
                case DISABLE_MOB_SPAWN:
                    return disableMobSpawn;
                case DISABLE_ITEM_SPAWN:
                    return disableItemSpawn;
                case DISABLE_LEAVES_DECAY:
                    return disableLeavesDecay;
                case DISABLE_BLOCK_EXPLODE:
                    return disableBlockExplode;
                case DISABLE_ENTITY_EXPLODE:
                    return disableEntityExplode;
                default:
                    return false;
            }
        }
    }
}
