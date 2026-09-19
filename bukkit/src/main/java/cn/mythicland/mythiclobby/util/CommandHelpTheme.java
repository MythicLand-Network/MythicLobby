package cn.mythicland.mythiclobby.util;

import org.bukkit.entity.Player;

public final class CommandHelpTheme {
    private static final String HELP_PREFIX = "§4> §c";

    private CommandHelpTheme() {
    }

    public static void sendUsage(Player player, String usage) {
        player.sendMessage(HELP_PREFIX + usage);
    }
}
