package cn.mythicland.mythiclobby.gui.model;

import cn.mythicland.framework.menu.ClickType;

import java.util.ArrayList;
import java.util.List;

public record MenuClickAction(ClickType clickType, List<MenuCommand> commands) {
    public MenuClickAction(ClickType clickType, List<MenuCommand> commands) {
        this.clickType = clickType;
        this.commands = new ArrayList<>(commands);
    }

    @Override
    public List<MenuCommand> commands() {
        return new ArrayList<>(commands);
    }
}
