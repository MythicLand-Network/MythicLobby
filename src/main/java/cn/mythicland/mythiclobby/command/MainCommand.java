package cn.mythicland.mythiclobby.command;

import cn.mythicland.framework.bukkit.CommandComponent;
import cn.mythicland.framework.command.AbstractCommand;
import cn.mythicland.framework.command.CommandContext;
import cn.mythicland.framework.command.CommandRoute;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.PluginReloader;
import cn.mythicland.mythiclobby.config.key.MessageKey;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import cn.mythicland.mythiclobby.message.MessageService;
import cn.mythicland.mythiclobby.util.CommandHelpTheme;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@InjectComponent
@CommandComponent
public class MainCommand extends AbstractCommand {
    private final PluginReloader pluginReloader;
    private final LobbyProtectionService lobbyProtectionService;
    private final MessageService messageService;

    public MainCommand(PluginReloader pluginReloader, LobbyProtectionService lobbyProtectionService, MessageService messageService) {
        this.pluginReloader = pluginReloader;
        this.lobbyProtectionService = lobbyProtectionService;
        this.messageService = messageService;
    }

    @CommandRoute(path = {"mythiclobby"}, usages = "/mythiclobby")
    private void executeInfo(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.info(player);
    }

    @CommandRoute(
            path = {"mythiclobby", "reload"},
            permission = "mythiclobby.reload",
            usages = "/mythiclobby reload"
    )
    private void executeReload(CommandContext context) {
        this.reload(context.sender());
    }

    @CommandRoute(
            path = {"mythiclobby", "setspawn"},
            permission = "mythiclobby.setspawn",
            usages = "/mythiclobby setspawn"
    )
    private void executeSetSpawn(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.setSpawn(player);
    }

    public void info(Player player) {
        CommandHelpTheme.sendDivider(player);
        CommandHelpTheme.sendCommand(player, "/spawn", "", "- 传送到大厅出生点");
        CommandHelpTheme.sendCommand(player, "/mlb", "reload", "- 重载插件配置与运行模块");
        CommandHelpTheme.sendCommand(player, "/mlb", "setspawn", "- 设置大厅出生点");
        CommandHelpTheme.sendCommand(player, "/mlbnpc", "", "- 查看 NPC 管理命令");
        CommandHelpTheme.sendCommand(player, "/mlbholo", "", "- 查看悬浮字管理命令");
        CommandHelpTheme.sendCommand(player, "/mlbmenu", "open <menuId>", "- 打开指定菜单");
        CommandHelpTheme.sendDivider(player);
    }

    public void reload(CommandSender sender) {
        pluginReloader.reloadAll();
        messageService.send(sender, MessageKey.COMMAND_RELOAD_SUCCESS);
    }

    public void setSpawn(Player player) {
        if (!lobbyProtectionService.setSpawn(player.getLocation())) {
            messageService.send(player, MessageKey.COMMAND_SETSPAWN_FAILED);
            return;
        }
        messageService.send(player, MessageKey.COMMAND_SETSPAWN_SUCCESS);
    }
}
