package cn.mythicland.mythiclobby.gui.model;

import cn.mythicland.mythiclobby.gui.menu.MenuCommandExecutor;

public record MenuCommand(MenuCommandExecutor executor, String command) {
}
