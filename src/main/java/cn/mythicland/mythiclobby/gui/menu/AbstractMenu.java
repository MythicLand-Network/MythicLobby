package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.framework.menu.Menu;
import cn.mythicland.framework.menu.AbstractButton;
import org.bukkit.entity.Player;

public abstract class AbstractMenu extends cn.mythicland.framework.menu.AbstractMenu {
    private final String id;
    private final String title;
    private final int size;
    private final String permission;

    protected AbstractMenu(String id, String title, int size, String permission) {
        this.id = id;
        this.title = title;
        this.size = size;
        this.permission = permission;
    }

    public final String getId() {
        return this.id;
    }

    @Override
    public final String buildTitle(Player player) {
        return this.title;
    }

    @Override
    protected final int resolveRowCount(Player player) {
        return this.size / 9;
    }

    public final int getRows() {
        return this.size / 9;
    }

    public final int getSize() {
        return this.size;
    }

    public final String getPermission() {
        return this.permission;
    }

    protected abstract void populateButtons(Player player, Menu menu);

    @Override
    protected final void populateMenu(Player player, Menu menu) {
        this.populateButtons(player, menu);
        this.populateBackground(menu);
    }

    protected Short getBackgroundGlassColor() {
        return null;
    }

    protected abstract AbstractButton createBackgroundButton(short color);

    private void populateBackground(Menu menu) {
        Short glassColor = this.getBackgroundGlassColor();
        if (glassColor == null) return;
        AbstractButton background = this.createBackgroundButton(glassColor);
        for (int slot = 0; slot < menu.getSize(); slot++) {
            if (!menu.hasButton(slot)) menu.setButton(slot, background);
        }
    }
}
