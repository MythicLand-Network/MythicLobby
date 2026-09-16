package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.menu.MenuManager;
import cn.mythicland.mythiclobby.gui.model.MenuSound;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@InjectComponent
public final class LobbyMenuService {
    private final MenuRepository repository;
    private final MenuManager frameworkMenuManager;
    private final Map<String, ConfigMenu> menusById = new LinkedHashMap<>();

    public LobbyMenuService(
            MenuRepository repository,
            MenuManager frameworkMenuManager
    ) {
        this.repository = repository;
        this.frameworkMenuManager = frameworkMenuManager;
        this.loadMenus();
    }

    public void reloadAll() {
        this.frameworkMenuManager.clearSessions();
        this.repository.reload();
        this.loadMenus();
    }

    public OpenResult openMenu(Player player, String menuId) {
        String normalizedMenuId = this.normalizeMenuId(menuId);
        if (normalizedMenuId == null) return OpenResult.NOT_FOUND;
        ConfigMenu menu = this.menusById.get(normalizedMenuId);
        if (menu == null) return OpenResult.NOT_FOUND;
        if (!menu.getPermission().isEmpty() && !player.hasPermission(menu.getPermission())) {
            return OpenResult.NO_PERMISSION;
        }
        this.frameworkMenuManager.open(player, menu);
        this.playSound(menu.getOpenSound(), player);
        return OpenResult.OPENED;
    }

    private void loadMenus() {
        this.menusById.clear();
        for (ConfigMenu menu : this.repository.loadAll()) {
            String normalizedMenuId = this.normalizeMenuId(menu.getId());
            if (normalizedMenuId == null || this.menusById.containsKey(normalizedMenuId)) continue;
            this.menusById.put(normalizedMenuId, menu);
        }
    }

    private void playSound(MenuSound sound, Player player) {
        if (sound != null) sound.play(player);
    }

    private String normalizeMenuId(String menuId) {
        if (menuId == null) return null;
        String trimmed = menuId.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    public enum OpenResult {
        OPENED,
        NOT_FOUND,
        NO_PERMISSION
    }
}
