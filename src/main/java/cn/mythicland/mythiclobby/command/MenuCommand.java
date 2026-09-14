package cn.mythicland.mythiclobby.command;

import cn.mythicland.framework.bukkit.CommandComponent;
import cn.mythicland.framework.command.AbstractCommand;
import cn.mythicland.framework.command.CommandContext;
import cn.mythicland.framework.command.CommandRoute;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.key.MessageKey;
import cn.mythicland.mythiclobby.gui.menu.MenuManager;
import cn.mythicland.mythiclobby.message.MessageService;
import cn.mythicland.mythiclobby.util.CommandHelpTheme;
import org.bukkit.entity.Player;

@InjectComponent
@CommandComponent
public final class MenuCommand extends AbstractCommand {
    private final MenuManager menuManager;
    private final MessageService messageService;

    public MenuCommand(MenuManager menuManager, MessageService messageService) {
        this.menuManager = menuManager;
        this.messageService = messageService;
    }

    @CommandRoute(path = {"mlbmenu"}, usages = "/mlbmenu")
    private void executeInfo(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.info(player);
    }

    @CommandRoute(path = {"mlbmenu", "open"}, usages = "/mlbmenu open <menuId>", minArguments = 1, maxArguments = 1)
    private void executeOpen(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.open(player, context.argument(0));
    }

    public void info(Player player) {
        CommandHelpTheme.sendDivider(player);
        CommandHelpTheme.sendCommand(player, "/mlbmenu", "open <menuId>", "- 打开指定菜单");
        CommandHelpTheme.sendDivider(player);
    }

    public void open(Player player, String menuId) {
        MenuManager.OpenResult result = menuManager.openMenu(player, menuId);
        if (result == MenuManager.OpenResult.OPENED) {
            return;
        }
        if (result == MenuManager.OpenResult.NO_PERMISSION) {
            messageService.send(player, MessageKey.MENU_OPEN_NO_PERMISSION);
            return;
        }
        messageService.send(player, MessageKey.MENU_OPEN_NOT_FOUND, menuId);
    }
}
