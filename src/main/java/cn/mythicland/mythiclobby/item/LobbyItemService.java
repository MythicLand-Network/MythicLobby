package cn.mythicland.mythiclobby.item;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.gui.model.MenuCommand;
import cn.mythicland.mythiclobby.gui.menu.MenuCommandExecutor;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
@InjectComponent
public final class LobbyItemService {
    private final JavaPlugin plugin;
    private final LobbyItemRepository repository;
    private final LobbyItemRenderer renderer;
    private final BungeeConnectService bungeeConnectService;
    private final PluginLogger logger;

    private final Map<Integer, LobbyItemDefinition> itemsBySlot = new LinkedHashMap<>();

    public LobbyItemService(
            JavaPlugin plugin,
            LobbyItemRepository repository,
            LobbyItemRenderer renderer,
            BungeeConnectService bungeeConnectService,
            PluginLogger logger
    ) {
        this.plugin = plugin;
        this.repository = repository;
        this.renderer = renderer;
        this.bungeeConnectService = bungeeConnectService;
        this.logger = logger;
        loadItems();
    }

    public void start() {
        grantToOnlinePlayers();
    }

    public void reloadAll() {
        repository.reload();
        loadItems();
        grantToOnlinePlayers();
    }

    public void handleJoin(Player player) {
        giveItems(player);
    }

    public boolean isProtectedInventoryAction(Player player, int slot, ItemStack itemStack) {
        if (isProtectedSlot(slot)) {
            return true;
        }

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }

        return isProtectedItem(player, itemStack);
    }

    public boolean isProtectedSlot(int slot) {
        return itemsBySlot.containsKey(slot);
    }

    public boolean isProtectedItem(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }

        for (LobbyItemDefinition definition : itemsBySlot.values()) {
            if (matches(player, definition, itemStack)) {
                return true;
            }
        }
        return false;
    }

    public void handleRightClick(Player player, int heldSlot, ItemStack itemStack) {
        LobbyItemDefinition definition = itemsBySlot.get(heldSlot);
        if (definition == null || !matches(player, definition, itemStack)) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> executeCommands(player, definition));
    }

    private void loadItems() {
        itemsBySlot.clear();
        for (LobbyItemDefinition definition : repository.loadAll()) {
            itemsBySlot.put(definition.slot(), definition);
        }
    }

    private void giveItems(Player player) {
        for (LobbyItemDefinition definition : itemsBySlot.values()) {
            player.getInventory().setItem(definition.slot(), renderer.renderItem(player, definition));
        }
        player.updateInventory();
    }

    private void grantToOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveItems(player);
        }
    }

    private boolean matches(Player player, LobbyItemDefinition definition, ItemStack itemStack) {
        return renderer.renderItem(player, definition).isSimilar(itemStack);
    }

    private void executeCommands(Player player, LobbyItemDefinition definition) {
        for (MenuCommand commandDefinition : definition.commands()) {
            String renderedValue = renderCommandValue(player, commandDefinition);
            if (renderedValue.isEmpty()) {
                logger.warning(String.format(
                        Locale.ROOT,
                        "items.yml: item '%s' produced an empty command after placeholder rendering",
                        definition.id()
                ));
                continue;
            }

            if (commandDefinition.executor() == MenuCommandExecutor.CONNECT) {
                bungeeConnectService.connect(player, renderedValue);
                continue;
            }
            if (commandDefinition.executor() == MenuCommandExecutor.MESSAGE) {
                player.sendMessage(renderedValue);
                continue;
            }
            if (commandDefinition.executor() == MenuCommandExecutor.CONSOLE) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), renderedValue);
                continue;
            }
            Bukkit.dispatchCommand(player, renderedValue);
        }
    }

    private String renderCommandValue(Player player, MenuCommand commandDefinition) {
        if (commandDefinition.executor() == MenuCommandExecutor.MESSAGE) {
            return sanitizeValue(commandDefinition, renderer.renderMessage(player, commandDefinition.command()));
        }
        return sanitizeValue(commandDefinition, renderer.renderCommand(player, commandDefinition.command()));
    }

    private String sanitizeValue(MenuCommand commandDefinition, String command) {
        String trimmed = command == null ? "" : command.trim();
        if (commandDefinition.executor() == MenuCommandExecutor.CONNECT
                || commandDefinition.executor() == MenuCommandExecutor.MESSAGE) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return trimmed.substring(1).trim();
        }
        return trimmed;
    }
}
