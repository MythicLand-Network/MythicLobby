package cn.mythicland.mythiclobby.gui.button;

import cn.mythicland.mythiclobby.gui.menu.AbstractMenu;
import cn.mythicland.mythiclobby.gui.menu.MenuClickType;
import cn.mythicland.mythiclobby.gui.menu.MenuManager;
import cn.mythicland.mythiclobby.gui.menu.MenuRenderer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractButton {
    private final int slot;
    private final boolean keepOpen;

    protected AbstractButton(int slot, boolean keepOpen) {
        this.slot = slot;
        this.keepOpen = keepOpen;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isKeepOpen() {
        return keepOpen;
    }

    public abstract ItemStack render(Player player, MenuRenderer renderer);

    public abstract void click(Player player, AbstractMenu menu, MenuManager menuManager, MenuClickType clickType);
}
