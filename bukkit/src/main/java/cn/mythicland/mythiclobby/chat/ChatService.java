package cn.mythicland.mythiclobby.chat;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.ChatConfig;
import cn.mythicland.mythiclobby.config.key.ChatConfigKey;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.framework.bukkit.message.CC;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

@InjectComponent
public final class ChatService {
    private final ChatConfig chatConfig;
    private final ChatRenderer renderer;
    private final PluginLogger logger;

    private ChatSettings settings;

    public ChatService(ChatConfig chatConfig, ChatRenderer renderer, PluginLogger logger) {
        this.chatConfig = chatConfig;
        this.renderer = renderer;
        this.logger = logger;
        this.settings = loadSettings();
    }

    public void reloadAll() {
        settings = loadSettings();
    }

    public boolean isEnabled() {
        return settings.enabled();
    }

    public boolean shouldBlockChat(Player player) {
        return settings.muteAllEnabled();
    }

    public String getMuteDeniedMessage() {
        return CC.translate(ChatSettings.DEFAULT_MUTE_DENIED_MESSAGE);
    }

    public void notifyMuteDenied(Player player) {
        String message = getMuteDeniedMessage();
        if (message.isEmpty()) {
            return;
        }
        player.sendMessage(message);
    }

    public String renderFormat(Player player) {
        return renderer.renderFormat(player, settings.format());
    }

    private ChatSettings loadSettings() {
        YamlConfiguration yaml = chatConfig.data();
        boolean enabled = yaml.getBoolean(ChatConfigKey.ENABLED.getPath(), true);
        String format = yaml.getString(ChatConfigKey.FORMAT.getPath(), ChatSettings.DEFAULT_FORMAT);
        boolean muteAllEnabled = yaml.getBoolean(ChatConfigKey.MUTE_ALL_ENABLED.getPath(), false);
        if (format == null || format.trim().isEmpty()) {
            logger.warning("chat.yml: format is empty, falling back to the default format.");
            format = ChatSettings.DEFAULT_FORMAT;
        }
        return new ChatSettings(enabled, format, muteAllEnabled);
    }
}
