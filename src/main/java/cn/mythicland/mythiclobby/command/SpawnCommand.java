package cn.mythicland.mythiclobby.command;

import cn.mythicland.framework.bukkit.CommandComponent;
import cn.mythicland.framework.command.AbstractCommand;
import cn.mythicland.framework.command.CommandContext;
import cn.mythicland.framework.command.CommandRoute;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.key.MessageKey;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import cn.mythicland.mythiclobby.message.MessageService;
import org.bukkit.entity.Player;

@InjectComponent
@CommandComponent
public final class SpawnCommand extends AbstractCommand {
    private final LobbyProtectionService lobbyProtectionService;
    private final MessageService messageService;

    public SpawnCommand(LobbyProtectionService lobbyProtectionService, MessageService messageService) {
        this.lobbyProtectionService = lobbyProtectionService;
        this.messageService = messageService;
    }

    @CommandRoute(path = {"spawn"}, usages = "/spawn")
    private void execute(CommandContext context) {
        Player player = CommandPlayers.require(context);
        if (player != null) this.spawn(player);
    }

    public void spawn(Player player) {
        LobbyProtectionService.TeleportResult result = lobbyProtectionService.teleportPlayerToSpawn(player);
        switch (result) {
            case SUCCESS:
                break;
            case DISABLED:
                messageService.send(player, MessageKey.COMMAND_SPAWN_DISABLED);
                break;
            case SPAWN_DISABLED:
                messageService.send(player, MessageKey.COMMAND_SPAWN_SPAWN_DISABLED);
                break;
            case SPAWN_NOT_SET:
                messageService.send(player, MessageKey.COMMAND_SPAWN_SPAWN_NOT_SET);
                break;
            default:
                break;
        }
    }
}
