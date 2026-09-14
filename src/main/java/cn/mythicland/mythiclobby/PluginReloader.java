package cn.mythicland.mythiclobby;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.chat.ChatService;
import cn.mythicland.mythiclobby.config.ConfigManager;
import cn.mythicland.mythiclobby.display.DisplayService;
import cn.mythicland.mythiclobby.gui.menu.MenuManager;
import cn.mythicland.mythiclobby.hologram.HologramService;
import cn.mythicland.mythiclobby.item.LobbyItemService;
import cn.mythicland.mythiclobby.listener.CommandTabBlockService;
import cn.mythicland.mythiclobby.lobby.LobbyProtectionService;
import cn.mythicland.mythiclobby.npc.NpcManager;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import cn.mythicland.mythiclobby.scoreboard.ScoreboardService;
import cn.mythicland.mythiclobby.tab.TabService;

@InjectComponent
public final class PluginReloader {
    private final ConfigManager configManager;
    private final NpcManager npcManager;
    private final BungeeConnectService bungeeConnectService;
    private final ScoreboardService scoreboardService;
    private final DisplayService displayService;
    private final ChatService chatService;
    private final LobbyProtectionService lobbyProtectionService;
    private final CommandTabBlockService commandTabBlockService;
    private final PlaceholderService placeholderService;
    private final HologramService hologramService;
    private final LobbyItemService lobbyItemService;
    private final MenuManager menuManager;
    private final TabService tabService;

    public PluginReloader(
            ConfigManager configManager,
            NpcManager npcManager,
            BungeeConnectService bungeeConnectService,
            ScoreboardService scoreboardService,
            DisplayService displayService,
            ChatService chatService,
            LobbyProtectionService lobbyProtectionService,
            CommandTabBlockService commandTabBlockService,
            PlaceholderService placeholderService,
            HologramService hologramService,
            LobbyItemService lobbyItemService,
            MenuManager menuManager,
            TabService tabService
    ) {
        this.configManager = configManager;
        this.npcManager = npcManager;
        this.bungeeConnectService = bungeeConnectService;
        this.scoreboardService = scoreboardService;
        this.displayService = displayService;
        this.chatService = chatService;
        this.lobbyProtectionService = lobbyProtectionService;
        this.commandTabBlockService = commandTabBlockService;
        this.placeholderService = placeholderService;
        this.hologramService = hologramService;
        this.lobbyItemService = lobbyItemService;
        this.menuManager = menuManager;
        this.tabService = tabService;
    }

    public void reloadAll() {
        configManager.reloadAll();
        placeholderService.reloadAll();
        bungeeConnectService.reloadAll();
        npcManager.reloadAll();
        hologramService.reloadAll();
        lobbyItemService.reloadAll();
        lobbyProtectionService.reloadAll();
        commandTabBlockService.reloadAll();
        scoreboardService.reloadAll();
        displayService.reloadAll();
        chatService.reloadAll();
        menuManager.reloadAll();
        tabService.reloadAll();
    }
}
