package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.mythiclobby.gui.button.MenuButton;
import cn.mythicland.framework.menu.AbstractButton;
import cn.mythicland.framework.menu.Menu;
import cn.mythicland.mythiclobby.gui.model.MenuSound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigMenu extends AbstractMenu {
    private final boolean glassEnabled;
    private final short glassColor;
    private final MenuRenderer renderer;
    private final MenuSound openSound;
    private final MenuSound clickSound;
    private final Map<Integer, MenuButton> buttons = new LinkedHashMap<>();

    public ConfigMenu(
            String id,
            String title,
            int size,
            String permission,
            MenuSound openSound,
            MenuSound clickSound,
            boolean glassEnabled,
            short glassColor,
            Map<Integer, MenuButton> buttons,
            MenuRenderer renderer
    ) {
        super(
                id,
                title,
                size,
                permission
        );
        this.glassEnabled = glassEnabled;
        this.glassColor = glassColor;
        this.renderer = renderer;
        this.openSound = openSound;
        this.clickSound = clickSound;
        this.buttons.putAll(buttons);
    }

    public boolean isGlassEnabled() {
        return glassEnabled;
    }

    public short getGlassColor() {
        return glassColor;
    }

    @Override
    protected Short getBackgroundGlassColor() {
        return glassEnabled ? glassColor : null;
    }

    @Override
    protected void populateButtons(Player player, Menu menu) {
        for (Map.Entry<Integer, MenuButton> entry : this.buttons.entrySet()) {
            menu.setButton(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected AbstractButton createBackgroundButton(short color) {
        return new cn.mythicland.framework.menu.AbstractDisplayButton() {
            @Override
            public ItemStack buildDisplayItem(Player player) {
                return renderer.createGlassPane(color);
            }
        };
    }

    public MenuSound getOpenSound() {
        return this.openSound;
    }

    public MenuSound getClickSound() {
        return this.clickSound;
    }
}
