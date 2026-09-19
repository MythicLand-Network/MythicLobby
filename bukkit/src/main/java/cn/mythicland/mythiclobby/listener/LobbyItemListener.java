package cn.mythicland.mythiclobby.listener;

import cn.mythicland.framework.bukkit.ListenerComponent;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.item.LobbyItemService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

@InjectComponent
@ListenerComponent
public final class LobbyItemListener implements Listener {
    private final LobbyItemService lobbyItemService;

    public LobbyItemListener(LobbyItemService lobbyItemService) {
        this.lobbyItemService = lobbyItemService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        lobbyItemService.handleJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack itemStack = event.getItem();
        if (!lobbyItemService.isProtectedItem(event.getPlayer(), itemStack)) {
            return;
        }

        event.setCancelled(true);
        lobbyItemService.handleRightClick(event.getPlayer(), event.getPlayer().getInventory().getHeldItemSlot(), itemStack);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!lobbyItemService.isProtectedItem(event.getPlayer(), event.getItemDrop().getItemStack())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() != null
                && event.getClickedInventory().equals(player.getInventory())
                && lobbyItemService.isProtectedSlot(event.getSlot())) {
            event.setCancelled(true);
            return;
        }

        if (lobbyItemService.isProtectedItem(player, event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (lobbyItemService.isProtectedItem(player, hotbarItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {
            int convertedSlot = event.getView().convertSlot(rawSlot);
            if (rawSlot >= topSize && lobbyItemService.isProtectedSlot(convertedSlot)) {
                event.setCancelled(true);
                return;
            }
        }

        if (lobbyItemService.isProtectedItem(player, event.getOldCursor())) {
            event.setCancelled(true);
        }
    }
}
