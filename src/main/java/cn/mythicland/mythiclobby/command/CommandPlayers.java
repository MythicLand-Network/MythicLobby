package cn.mythicland.mythiclobby.command;

import cn.mythicland.framework.command.CommandContext;
import org.bukkit.entity.Player;

final class CommandPlayers {
    private CommandPlayers() {
    }

    static Player require(CommandContext context) {
        if (context.sender() instanceof Player player) return player;
        context.sender().sendMessage("§c只有玩家可以使用这个命令。");
        return null;
    }
}
