package cn.mythicland.mythiclobby.command;

import cn.mythicland.framework.bukkit.CommandComponent;
import cn.mythicland.framework.command.AbstractCommand;
import cn.mythicland.framework.command.CommandContext;
import cn.mythicland.framework.command.CommandRoute;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.key.MessageKey;
import cn.mythicland.mythiclobby.hologram.HologramService;
import cn.mythicland.mythiclobby.message.MessageService;
import cn.mythicland.mythiclobby.util.CommandHelpTheme;
import cn.mythicland.mythiclobby.util.NpcDisplayParser;
import org.bukkit.entity.Player;

import java.util.List;

@InjectComponent
@CommandComponent
public final class HologramCommand extends AbstractCommand {
    private final HologramService hologramService;
    private final MessageService messageService;

    public HologramCommand(HologramService hologramService, MessageService messageService) {
        this.hologramService = hologramService;
        this.messageService = messageService;
    }

    @CommandRoute(path = {"mlbholo"}, usages = "/mlbholo")
    private void executeInfo(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.info(player);
    }

    @CommandRoute(
            path = {"mlbholo", "create"},
            permission = "mythiclobby.hologram.create",
            usages = "/mlbholo create <name> <display>",
            minArguments = 2,
            maxArguments = Integer.MAX_VALUE
    )
    private void executeCreate(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player == null) return;
        this.create(player, context.argument(0), String.join(" ", context.arguments().subList(1, context.argumentCount())));
    }

    @CommandRoute(
            path = {"mlbholo", "remove"},
            permission = "mythiclobby.hologram.remove",
            usages = "/mlbholo remove <name>",
            minArguments = 1,
            maxArguments = 1
    )
    private void executeRemove(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.remove(player, context.argument(0));
    }

    public void info(Player player) {
        CommandHelpTheme.sendDivider(player);
        CommandHelpTheme.sendCommand(player, "/mlbholo", "create <name> <display>", "- 创建一个新的悬浮字");
        CommandHelpTheme.sendCommand(player, "/mlbholo", "remove <name>", "- 删除指定悬浮字");
        CommandHelpTheme.sendDivider(player);
    }

    public void create(Player player, String hologramName, String display) {
        if (hologramName == null || hologramName.trim().isEmpty() || hologramName.contains(".")) {
            messageService.send(player, MessageKey.HOLOGRAM_CREATE_INVALID_NAME);
            return;
        }

        if (display == null || display.trim().isEmpty()) {
            messageService.send(player, MessageKey.HOLOGRAM_CREATE_EMPTY_DISPLAY);
            return;
        }

        List<String> displayLines = NpcDisplayParser.parse(display);
        if (!hologramService.createHologram(hologramName, displayLines, player.getLocation().clone())) {
            messageService.send(player, MessageKey.HOLOGRAM_CREATE_DUPLICATE_OR_INVALID);
            return;
        }

        messageService.send(player, MessageKey.HOLOGRAM_CREATE_SUCCESS, hologramName);
    }

    public void remove(Player player, String hologramName) {
        if (!hologramService.removeHologram(hologramName)) {
            messageService.send(player, MessageKey.HOLOGRAM_REMOVE_NOT_FOUND, hologramName);
            return;
        }

        messageService.send(player, MessageKey.HOLOGRAM_REMOVE_SUCCESS, hologramName);
    }
}
