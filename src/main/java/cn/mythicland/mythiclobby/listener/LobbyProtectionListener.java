package cn.mythicland.mythiclobby.listener;

import cn.mythicland.framework.bukkit.ListenerComponent;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.menu.MenuManager;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@InjectComponent
@ListenerComponent
public final class LobbyProtectionListener implements Listener {
    private final LobbyProtectionService lobbyProtectionService;
    private final MenuManager menuManager;

    public LobbyProtectionListener(
            LobbyProtectionService lobbyProtectionService,
            MenuManager menuManager
    ) {
        this.lobbyProtectionService = lobbyProtectionService;
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        lobbyProtectionService.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        org.bukkit.Location respawnLocation = lobbyProtectionService.getRespawnLocation(event.getPlayer());
        if (respawnLocation != null) {
            event.setRespawnLocation(respawnLocation);
        }
        lobbyProtectionService.handleRespawn(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!lobbyProtectionService.shouldHandleDeathRespawn(player)) {
            return;
        }

        event.setDeathMessage(null);
        lobbyProtectionService.handleDeath(player);
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        lobbyProtectionService.handleWorldChange(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (lobbyProtectionService.shouldProtect(event.getPlayer(), LobbyProtectionService.ProtectionFlag.BLOCK_BREAK)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (lobbyProtectionService.shouldProtect(event.getPlayer(), LobbyProtectionService.ProtectionFlag.BLOCK_PLACE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (lobbyProtectionService.shouldProtect(event.getPlayer(), LobbyProtectionService.ProtectionFlag.BUCKET_FILL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (lobbyProtectionService.shouldProtect(event.getPlayer(), LobbyProtectionService.ProtectionFlag.BUCKET_EMPTY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.PHYSICAL) {
            return;
        }
        if (lobbyProtectionService.shouldProtect(event.getPlayer(), LobbyProtectionService.ProtectionFlag.BLOCK_INTERACT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (lobbyProtectionService.shouldProtect(event.getPlayer(), LobbyProtectionService.ProtectionFlag.ITEM_DROP)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        if (lobbyProtectionService.shouldProtect(event.getPlayer(), LobbyProtectionService.ProtectionFlag.ITEM_PICKUP)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClick() == ClickType.NUMBER_KEY
                && lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.INVENTORY_NUMBER_KEY)) {
            event.setCancelled(true);
            return;
        }
        if (!lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.INVENTORY_MOVE)) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        if (this.menuManager.isManagedInventory(topInventory)) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
                event.setCancelled(true);
                return;
            }
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            if (hasItem(event.getCursor())) {
                event.setCancelled(true);
                return;
            }
            if (event.getClick() == ClickType.NUMBER_KEY
                    || event.getClick() == ClickType.DOUBLE_CLICK
                    || event.getClick() == ClickType.DROP
                    || event.getClick() == ClickType.CONTROL_DROP) {
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.INVENTORY_DRAG)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (lobbyProtectionService.shouldProtect(event.getPlayer(), LobbyProtectionService.ProtectionFlag.CONSUME_ITEMS)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.HUNGER_CHANGE)) {
            return;
        }

        event.setCancelled(true);
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!lobbyProtectionService.isLobbyWorld(player.getWorld())) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (isFireDamage(cause) && lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.FIRE_DAMAGE)) {
            event.setCancelled(true);
            player.setFireTicks(0);
            return;
        }
        if (cause == EntityDamageEvent.DamageCause.FALL
                && lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.FALL_DAMAGE)) {
            event.setCancelled(true);
            return;
        }
        if (cause == EntityDamageEvent.DamageCause.VOID
                && lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.VOID_DAMAGE)) {
            event.setCancelled(true);
            return;
        }
        if (isExplosionDamage(cause)
                && lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.EXPLOSION_DAMAGE)) {
            event.setCancelled(true);
            return;
        }
        if (!(event instanceof EntityDamageByEntityEvent byEntityEvent)) {
            return;
        }
        Entity damager = byEntityEvent.getDamager();
        if (damager instanceof Projectile
                && lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.PROJECTILE_DAMAGE)) {
            event.setCancelled(true);
            return;
        }
        if (damager instanceof Player
                && lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.PVP_DAMAGE)) {
            event.setCancelled(true);
            return;
        }
        if (damager instanceof LivingEntity
                && !(damager instanceof Player)
                && lobbyProtectionService.shouldProtect(player, LobbyProtectionService.ProtectionFlag.MOB_DAMAGE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (lobbyProtectionService.shouldApplyWorldRule(event.getWorld(), LobbyProtectionService.WorldRuleFlag.DISABLE_WEATHER_CHANGE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (lobbyProtectionService.shouldApplyWorldRule(event.getLocation().getWorld(), LobbyProtectionService.WorldRuleFlag.DISABLE_MOB_SPAWN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (lobbyProtectionService.shouldApplyWorldRule(event.getLocation().getWorld(), LobbyProtectionService.WorldRuleFlag.DISABLE_ITEM_SPAWN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (lobbyProtectionService.shouldApplyWorldRule(event.getBlock().getWorld(), LobbyProtectionService.WorldRuleFlag.DISABLE_LEAVES_DECAY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (lobbyProtectionService.shouldApplyWorldRule(event.getBlock().getWorld(), LobbyProtectionService.WorldRuleFlag.DISABLE_BLOCK_EXPLODE)) {
            event.setCancelled(true);
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (lobbyProtectionService.shouldApplyWorldRule(event.getLocation().getWorld(), LobbyProtectionService.WorldRuleFlag.DISABLE_ENTITY_EXPLODE)) {
            event.setCancelled(true);
            event.blockList().clear();
        }
    }

    private boolean hasItem(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() != Material.AIR;
    }

    private boolean isFireDamage(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA;
    }

    private boolean isExplosionDamage(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
    }
}
