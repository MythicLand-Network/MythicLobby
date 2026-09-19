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
        if (player != null) this.root(player, context.label());
    }

    @CommandRoute(path = {"mythiclobby", "help"}, usages = "/mythiclobby help")
    private void executeHelp(CommandContext context) {
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
        CommandHelpTheme.sendUsage(player, "/spawn");
        CommandHelpTheme.sendUsage(player, "/mlb help");
        CommandHelpTheme.sendUsage(player, "/mlb reload");
        CommandHelpTheme.sendUsage(player, "/mlb setspawn");
        CommandHelpTheme.sendUsage(player, "/mlbnpc");
        CommandHelpTheme.sendUsage(player, "/mlbholo");
        CommandHelpTheme.sendUsage(player, "/mlbmenu open <menuId>");
    }

    public void root(Player player, String label) {
        player.sendMessage("§c用法: /" + label + " <子命令>");
        player.sendMessage("§c使用 /" + label + " help 查看所有命令。");
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
