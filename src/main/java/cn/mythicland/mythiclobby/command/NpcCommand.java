package cn.mythicland.mythiclobby.command;

import cn.mythicland.framework.bukkit.CommandComponent;
import cn.mythicland.framework.command.AbstractCommand;
import cn.mythicland.framework.command.CommandContext;
import cn.mythicland.framework.command.CommandRoute;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.key.MessageKey;
import cn.mythicland.mythiclobby.message.MessageService;
import cn.mythicland.mythiclobby.npc.ManagedNpc;
import cn.mythicland.mythiclobby.npc.NpcManager;
import cn.mythicland.mythiclobby.util.CommandHelpTheme;
import cn.mythicland.mythiclobby.util.NpcDisplayParser;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

@InjectComponent
@CommandComponent
public class NpcCommand extends AbstractCommand {
    private final NpcManager npcManager;
    private final MessageService messageService;

    public NpcCommand(NpcManager npcManager, MessageService messageService) {
        this.npcManager = npcManager;
        this.messageService = messageService;
    }

    @CommandRoute(path = {"mlbnpc"}, usages = "/mlbnpc")
    private void executeInfo(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.info(player);
    }

    @CommandRoute(path = {"mlbnpc", "create"}, permission = "mythiclobby.npc.create", usages = "/mlbnpc create <display>", minArguments = 1, maxArguments = Integer.MAX_VALUE)
    private void executeCreate(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.create(player, String.join(" ", context.arguments()));
    }

    @CommandRoute(path = {"mlbnpc", "remove"}, permission = "mythiclobby.npc.remove", usages = "/mlbnpc remove")
    private void executeRemove(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.remove(player);
    }

    @CommandRoute(path = {"mlbnpc", "sel"}, permission = "mythiclobby.npc.select", usages = "/mlbnpc sel [name]", minArguments = 0, maxArguments = 1)
    private void executeSelect(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.selectInSight(player, context.optionalArgument(0));
    }

    @CommandRoute(path = {"mlbnpc", "tph"}, permission = "mythiclobby.npc.tph", usages = "/mlbnpc tph")
    private void executeTeleportHere(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.tph(player);
    }

    @CommandRoute(path = {"mlbnpc", "cmd", "add"}, permission = "mythiclobby.npc.cmd", usages = "/mlbnpc cmd add <command>", minArguments = 1, maxArguments = Integer.MAX_VALUE)
    private void executeAddCommand(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.addCommand(player, String.join(" ", context.arguments()));
    }

    @CommandRoute(path = {"mlbnpc", "cmd", "remove"}, permission = "mythiclobby.npc.cmd", usages = "/mlbnpc cmd remove <index>", minArguments = 1, maxArguments = 1)
    private void executeRemoveCommand(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player == null) return;
        try {
            this.removeCommand(player, Integer.parseInt(context.argument(0)));
        } catch (NumberFormatException exception) {
            this.messageService.send(player, MessageKey.NPC_CMD_REMOVE_OUT_OF_RANGE);
        }
    }

    @CommandRoute(path = {"mlbnpc", "cmd", "list"}, permission = "mythiclobby.npc.cmd", usages = "/mlbnpc cmd list")
    private void executeListCommands(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.listCommands(player);
    }

    @CommandRoute(path = {"mlbnpc", "cmd", "clear"}, permission = "mythiclobby.npc.cmd", usages = "/mlbnpc cmd clear")
    private void executeClearCommands(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.clearCommands(player);
    }

    public void info(Player player) {
        CommandHelpTheme.sendUsage(player, "/mlbnpc create <display>");
        CommandHelpTheme.sendUsage(player, "/mlbnpc sel [name]");
        CommandHelpTheme.sendUsage(player, "/mlbnpc remove");
        CommandHelpTheme.sendUsage(player, "/mlbnpc tph");
        CommandHelpTheme.sendUsage(player, "/mlbnpc cmd add <command>");
        CommandHelpTheme.sendUsage(player, "/mlbnpc cmd remove <index>");
        CommandHelpTheme.sendUsage(player, "/mlbnpc cmd list");
        CommandHelpTheme.sendUsage(player, "/mlbnpc cmd clear");
    }

    public void create(Player player, String display) {
        List<String> displayLines = NpcDisplayParser.parse(display);
        if (!npcManager.createNpc(displayLines, getCreateLocation(player), player)) {
            messageService.send(player, MessageKey.NPC_CREATE_FAILED);
            return;
        }
        messageService.send(player, MessageKey.NPC_CREATE_SUCCESS);
        sendSelectionMessage(player);
    }

    public void remove(Player player) {
        ManagedNpc selectedNpc = requireSelectedNpc(player);
        if (selectedNpc == null) {
            return;
        }
        npcManager.removeSelectedNpc(player);
        messageService.send(player, MessageKey.NPC_REMOVE_SUCCESS, selectedNpc.getStoredNpc().getDisplayName());
    }

    public void selectInSight(Player player, String npcName) {
        if (npcName != null && !npcName.trim().isEmpty()) {
            if (!npcManager.selectNpc(player, npcName)) {
                messageService.send(player, MessageKey.NPC_SELECT_NOT_FOUND, npcName);
                return;
            }
            sendSelectionMessage(player);
            return;
        }

        ManagedNpc managedNpc = npcManager.selectNpcInSight(player);
        if (managedNpc == null) {
            messageService.send(player, MessageKey.NPC_SELECT_NONE_IN_SIGHT);
            return;
        }
        sendSelectionMessage(player);
    }

    public void tph(Player player) {
        ManagedNpc selectedNpc = requireSelectedNpc(player);
        if (selectedNpc == null) {
            return;
        }
        npcManager.moveSelectedNpc(player, player.getLocation());
        messageService.send(player, MessageKey.NPC_TPH_SUCCESS, selectedNpc.getStoredNpc().getDisplayName());
    }

    public void addCommand(Player player, String command) {
        ManagedNpc selectedNpc = requireSelectedNpc(player);
        if (selectedNpc == null) {
            return;
        }
        npcManager.addCommand(player, command);
        messageService.send(player, MessageKey.NPC_CMD_ADD_SUCCESS, selectedNpc.getStoredNpc().getDisplayName(), command);
    }

    public void removeCommand(Player player, int index) {
        ManagedNpc selectedNpc = requireSelectedNpc(player);
        if (selectedNpc == null) {
            return;
        }
        if (!npcManager.removeCommand(player, index - 1)) {
            messageService.send(player, MessageKey.NPC_CMD_REMOVE_OUT_OF_RANGE);
            return;
        }
        messageService.send(player, MessageKey.NPC_CMD_REMOVE_SUCCESS, selectedNpc.getStoredNpc().getDisplayName(), index);
    }

    public void listCommands(Player player) {
        ManagedNpc selectedNpc = requireSelectedNpc(player);
        if (selectedNpc == null) {
            return;
        }

        List<String> commands = npcManager.getCommands(player);
        messageService.send(player, MessageKey.NPC_CMD_LIST_HEADER, selectedNpc.getStoredNpc().getDisplayName());
        if (commands.isEmpty()) {
            messageService.send(player, MessageKey.NPC_CMD_LIST_EMPTY);
            return;
        }

        for (int i = 0; i < commands.size(); i++) {
            messageService.send(player, MessageKey.NPC_CMD_LIST_ENTRY, i + 1, commands.get(i));
        }
    }

    public void clearCommands(Player player) {
        ManagedNpc selectedNpc = requireSelectedNpc(player);
        if (selectedNpc == null) {
            return;
        }
        int removed = npcManager.clearCommands(player);
        messageService.send(player, MessageKey.NPC_CMD_CLEAR_SUCCESS, selectedNpc.getStoredNpc().getDisplayName(), removed);
    }

    private ManagedNpc requireSelectedNpc(Player player) {
        ManagedNpc selectedNpc = npcManager.getSelectedNpc(player);
        if (selectedNpc == null) {
            messageService.send(player, MessageKey.NPC_SELECT_REQUIRED);
        }
        return selectedNpc;
    }

    private org.bukkit.Location getCreateLocation(Player player) {
        org.bukkit.Location location = player.getLocation().clone();
        Vector direction = location.getDirection().setY(0.0D);
        if (direction.lengthSquared() < 1.0E-6D) {
            direction = new Vector(0.0D, 0.0D, 1.0D);
        }
        direction.normalize().multiply(1.5D);
        location.add(direction);
        return location;
    }

    private void sendSelectionMessage(Player player) {
        ManagedNpc selectedNpc = npcManager.getSelectedNpc(player);
        if (selectedNpc == null) {
            messageService.send(player, MessageKey.NPC_SELECT_REQUIRED);
            return;
        }

        messageService.send(player, MessageKey.NPC_SELECT_SUCCESS, selectedNpc.getStoredNpc().getDisplayName());
    }
}
