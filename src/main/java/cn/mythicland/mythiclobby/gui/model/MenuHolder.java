package cn.mythicland.mythiclobby.gui.model;

import cn.mythicland.mythiclobby.gui.menu.AbstractMenu;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder {
    private final AbstractMenu menu;
    private Inventory inventory;

    public MenuHolder(AbstractMenu menu) {
        this.menu = menu;
    }

    public String getMenuId() {
        return menu.getId();
    }

    public AbstractMenu getMenu() {
        return menu;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
