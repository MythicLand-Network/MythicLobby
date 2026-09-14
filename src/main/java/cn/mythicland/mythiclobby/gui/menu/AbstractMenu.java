package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.mythiclobby.gui.button.AbstractButton;
import cn.mythicland.mythiclobby.gui.model.MenuHolder;
import cn.mythicland.mythiclobby.gui.model.MenuSound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collection;

public abstract class AbstractMenu {
    private final String id;
    private final String title;
    private final int size;
    private final String permission;
    private final MenuSound openSound;
    private final MenuSound clickSound;

    protected AbstractMenu(
            String id,
            String title,
            int size,
            String permission,
            MenuSound openSound,
            MenuSound clickSound
    ) {
        this.id = id;
        this.title = title;
        this.size = size;
        this.permission = permission;
        this.openSound = openSound;
        this.clickSound = clickSound;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return size / 9;
    }

    public int getSize() {
        return size;
    }

    public String getPermission() {
        return permission;
    }

    public MenuSound getOpenSound() {
        return openSound;
    }

    public MenuSound getClickSound() {
        return clickSound;
    }

    public Inventory createInventory(Player player, MenuRenderer renderer) {
        MenuHolder holder = new MenuHolder(this);
        Inventory inventory = Bukkit.createInventory(holder, size, renderer.renderTitle(player, title));
        holder.setInventory(inventory);
        for (AbstractButton button : getButtons()) {
            inventory.setItem(button.getSlot(), button.render(player, renderer));
        }
        loadBackgroundGlass(inventory, renderer);
        return inventory;
    }

    public AbstractButton getButton(int slot) {
        for (AbstractButton button : getButtons()) {
            if (button.getSlot() == slot) {
                return button;
            }
        }
        return null;
    }

    protected Short getBackgroundGlassColor() {
        return null;
    }

    private void loadBackgroundGlass(Inventory inventory, MenuRenderer renderer) {
        Short glassColor = getBackgroundGlassColor();
        if (glassColor == null) {
            return;
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) != null && inventory.getItem(slot).getType() != Material.AIR) {
                continue;
            }
            inventory.setItem(slot, renderer.createGlassPane(glassColor));
        }
    }

    protected abstract Collection<? extends AbstractButton> getButtons();
}
