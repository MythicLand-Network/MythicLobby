package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.menu.MenuManager;
import cn.mythicland.mythiclobby.gui.model.MenuCommand;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@InjectComponent
public final class LobbyMenuCommandExecutor {
    private final MenuRenderer renderer;
    private final BungeeConnectService bungeeConnectService;
    private final PluginLogger logger;

    public LobbyMenuCommandExecutor(
            MenuRenderer renderer,
            BungeeConnectService bungeeConnectService,
            PluginLogger logger
    ) {
        this.renderer = renderer;
        this.bungeeConnectService = bungeeConnectService;
        this.logger = logger;
    }

    public MenuRenderer renderer() {
        return this.renderer;
    }

    public void execute(
            Player player,
            List<MenuCommand> commands,
            MenuManager menuManager
    ) {
        for (MenuCommand command : commands) {
            if (command.executor() == MenuCommandExecutor.CLOSE) {
                menuManager.close(player);
                continue;
            }

            String value = this.renderedValue(player, command);
            if (value.isEmpty()) {
                this.logger.warning("Configured menu command became empty after placeholder rendering");
                continue;
            }
            if (command.executor() == MenuCommandExecutor.CONNECT) {
                this.bungeeConnectService.connect(player, value);
                continue;
            }
            if (command.executor() == MenuCommandExecutor.MESSAGE) {
                player.sendMessage(value);
                continue;
            }
            if (command.executor() == MenuCommandExecutor.CONSOLE) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
                continue;
            }
            Bukkit.dispatchCommand(player, value);
        }
    }

    private String renderedValue(Player player, MenuCommand command) {
        String rendered = command.executor() == MenuCommandExecutor.MESSAGE
                ? this.renderer.renderMessage(player, command.command())
                : this.renderer.renderCommand(player, command.command());
        String trimmed = rendered == null ? "" : rendered.trim();
        if (command.executor() == MenuCommandExecutor.CONNECT
                || command.executor() == MenuCommandExecutor.MESSAGE) {
            return trimmed;
        }
        return trimmed.startsWith("/") ? trimmed.substring(1).trim() : trimmed;
    }
}
