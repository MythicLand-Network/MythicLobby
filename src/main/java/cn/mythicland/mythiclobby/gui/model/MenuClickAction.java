package cn.mythicland.mythiclobby.gui.model;

import cn.mythicland.mythiclobby.gui.menu.MenuClickType;

import java.util.ArrayList;
import java.util.List;

public record MenuClickAction(MenuClickType clickType, List<MenuCommand> commands) {
    public MenuClickAction(MenuClickType clickType, List<MenuCommand> commands) {
        this.clickType = clickType;
        this.commands = new ArrayList<>(commands);
    }

    @Override
    public List<MenuCommand> commands() {
        return new ArrayList<>(commands);
    }
}
