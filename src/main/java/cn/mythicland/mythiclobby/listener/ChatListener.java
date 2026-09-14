package cn.mythicland.mythiclobby.listener;

import cn.mythicland.framework.bukkit.ListenerComponent;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.chat.ChatService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutionException;

@InjectComponent
@ListenerComponent
public final class ChatListener implements Listener {
    private final JavaPlugin plugin;
    private final ChatService chatService;

    public ChatListener(JavaPlugin plugin, ChatService chatService) {
        this.plugin = plugin;
        this.chatService = chatService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.isAsynchronous()) {
            if (chatService.shouldBlockChat(event.getPlayer())) {
                event.setCancelled(true);
                chatService.notifyMuteDenied(event.getPlayer());
                return;
            }

            if (!chatService.isEnabled()) return;
            event.setFormat(chatService.renderFormat(event.getPlayer()));
            return;
        }

        try {
            ChatHandlingResult result = Bukkit.getScheduler().callSyncMethod(
                    plugin,
                    () -> {
                        if (chatService.shouldBlockChat(event.getPlayer())) {
                            chatService.notifyMuteDenied(event.getPlayer());
                            return ChatHandlingResult.blocked();
                        }
                        if (!chatService.isEnabled()) {
                            return ChatHandlingResult.noFormat();
                        }
                        return ChatHandlingResult.formatted(chatService.renderFormat(event.getPlayer()));
                    }
            ).get();

            if (result.cancelled()) {
                event.setCancelled(true);
                return;
            }
            if (result.format() != null) {
                event.setFormat(result.format());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to render chat format for " + event.getPlayer().getName(), e);
        }
    }

    private record ChatHandlingResult(boolean cancelled, String format) {

        private static ChatHandlingResult blocked() {
            return new ChatHandlingResult(true, null);
        }

        private static ChatHandlingResult noFormat() {
            return new ChatHandlingResult(false, null);
        }

        private static ChatHandlingResult formatted(String format) {
            return new ChatHandlingResult(false, format);
        }
    }
}
