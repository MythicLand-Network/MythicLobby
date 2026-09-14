package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.bukkit.MenuComponent;
import cn.mythicland.mythiclobby.gui.button.AbstractButton;
import cn.mythicland.mythiclobby.gui.model.MenuCommand;
import cn.mythicland.mythiclobby.gui.model.MenuHolder;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import cn.mythicland.framework.component.ComponentContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
@InjectComponent
public final class MenuManager {
    private final JavaPlugin plugin;
    private final MenuRepository repository;
    private final MenuRenderer renderer;
    private final BungeeConnectService bungeeConnectService;
    private final PluginLogger logger;
    private final ComponentContainer components;

    private final Map<String, AbstractMenu> menusById = new LinkedHashMap<>();
    private final Map<UUID, String> openMenuIds = new LinkedHashMap<>();

    public MenuManager(
            JavaPlugin plugin,
            MenuRepository repository,
            MenuRenderer renderer,
            BungeeConnectService bungeeConnectService,
            PluginLogger logger,
            ComponentContainer components
    ) {
        this.plugin = plugin;
        this.repository = repository;
        this.renderer = renderer;
        this.bungeeConnectService = bungeeConnectService;
        this.logger = logger;
        this.components = components;
        loadMenus();
    }

    public void reloadAll() {
        closeOpenMenus();
        repository.reload();
        loadMenus();
    }

    public OpenResult openMenu(Player player, String menuId) {
        String normalizedMenuId = normalizeMenuId(menuId);
        if (normalizedMenuId == null) {
            return OpenResult.NOT_FOUND;
        }

        AbstractMenu menu = menusById.get(normalizedMenuId);
        if (menu == null) {
            return OpenResult.NOT_FOUND;
        }
        if (!menu.getPermission().isEmpty() && !player.hasPermission(menu.getPermission())) {
            return OpenResult.NO_PERMISSION;
        }

        Inventory inventory = menu.createInventory(player, renderer);
        player.openInventory(inventory);
        openMenuIds.put(player.getUniqueId(), normalizedMenuId);
        if (menu.getOpenSound() != null) {
            menu.getOpenSound().play(player);
        }
        return OpenResult.OPENED;
    }

    public void handleInventoryClick(Player player, MenuHolder holder, int rawSlot, MenuClickType clickType) {
        AbstractMenu menu = holder.getMenu();
        if (menu == null) {
            return;
        }

        AbstractButton button = menu.getButton(rawSlot);
        if (button == null) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!button.isKeepOpen()) {
                player.closeInventory();
            }
            if (menu.getClickSound() != null) {
                menu.getClickSound().play(player);
            }
            button.click(player, menu, this, clickType);
        });
    }

    public void handleInventoryClose(Player player, MenuHolder holder) {
        String normalizedMenuId = normalizeMenuId(holder.getMenuId());
        if (normalizedMenuId == null) {
            return;
        }

        String openMenuId = openMenuIds.get(player.getUniqueId());
        if (normalizedMenuId.equals(openMenuId)) {
            openMenuIds.remove(player.getUniqueId());
        }
    }

    private void loadMenus() {
        menusById.clear();
        for (ConfigMenu menu : repository.loadAll()) {
            registerMenu(menu);
        }
        for (AbstractMenu menu : this.components.componentsAnnotatedWith(MenuComponent.class, AbstractMenu.class)) {
            registerMenu(menu);
        }
    }

    private void registerMenu(AbstractMenu menu) {
        String normalizedMenuId = normalizeMenuId(menu.getId());
        if (normalizedMenuId == null) {
            logger.warning("Skipped menu registration because menu id is empty.");
            return;
        }
        if (menusById.containsKey(normalizedMenuId)) {
            logger.warning(String.format(
                    Locale.ROOT,
                    "Skipped menu registration because menu id '%s' is duplicated by %s",
                    menu.getId(),
                    menu.getClass().getName()
            ));
            return;
        }
        menusById.put(normalizedMenuId, menu);
    }

    public void executeButtonCommands(Player player, AbstractMenu menu, List<MenuCommand> commands) {
        for (MenuCommand commandDefinition : commands) {
            if (commandDefinition.executor() == MenuCommandExecutor.CLOSE) {
                player.closeInventory();
                continue;
            }

            String renderedValue = renderCommandValue(player, commandDefinition);
            if (renderedValue.isEmpty()) {
                logger.warning(String.format(
                        Locale.ROOT,
                        "menus.yml: menu '%s' produced an empty command after placeholder rendering",
                        menu.getId()
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

    private void closeOpenMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) {
                player.closeInventory();
            }
        }
        openMenuIds.clear();
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

    private String normalizeMenuId(String menuId) {
        if (menuId == null) {
            return null;
        }
        String trimmed = menuId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    public enum OpenResult {
        OPENED,
        NOT_FOUND,
        NO_PERMISSION
    }
}
