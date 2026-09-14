package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.mythiclobby.gui.button.AbstractButton;
import cn.mythicland.mythiclobby.gui.button.MenuButton;
import cn.mythicland.mythiclobby.gui.model.MenuSound;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigMenu extends AbstractMenu {
    private final boolean glassEnabled;
    private final short glassColor;
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
            Map<Integer, MenuButton> buttons
    ) {
        super(
                id,
                title,
                size,
                permission,
                openSound,
                clickSound
        );
        this.glassEnabled = glassEnabled;
        this.glassColor = glassColor;
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
    protected Collection<? extends AbstractButton> getButtons() {
        return buttons.values();
    }
}
