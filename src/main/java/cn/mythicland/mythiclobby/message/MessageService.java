package cn.mythicland.mythiclobby.message;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.MessageConfig;
import cn.mythicland.mythiclobby.config.key.MessageKey;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.mythiclobby.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

@InjectComponent
public final class MessageService {
    private final MessageConfig messageConfig;
    private final PlaceholderService placeholderService;
    private final PluginLogger logger;

    public MessageService(MessageConfig messageConfig, PlaceholderService placeholderService, PluginLogger logger) {
        this.messageConfig = messageConfig;
        this.placeholderService = placeholderService;
        this.logger = logger;
    }

    public void reloadAll() {
        // ConfigManager keeps YAML state fresh before services rebuild runtime data.
    }

    public void send(Player player, String key, Object... args) {
        player.sendMessage(format(player, key, args));
    }

    public void send(Player player, MessageKey key, Object... args) {
        send(player, key.getPath(), args);
    }

    public void send(CommandSender sender, String key, Object... args) {
        if (sender == null) {
            return;
        }

        Player player = sender instanceof Player ? (Player) sender : null;
        sender.sendMessage(format(player, key, args));
    }

    public void send(CommandSender sender, MessageKey key, Object... args) {
        send(sender, key.getPath(), args);
    }

    public String format(Player player, String key, Object... args) {
        String template = resolveTemplate(key);
        String rendered = applyBuiltInPlaceholders(template);
        rendered = applyArgs(rendered, args);
        rendered = placeholderService.render(player, rendered);
        return CC.translate(rendered);
    }

    public String format(Player player, MessageKey key, Object... args) {
        return format(player, key.getPath(), args);
    }

    private String resolveTemplate(String key) {
        if (key == null || key.trim().isEmpty()) {
            logger.warning("messages.yml: missing message key reference");
            return "<empty-key>";
        }

        YamlConfiguration yaml = messageConfig.getYamlConfiguration();
        String value = yaml.getString(key);
        if (value == null) {
            logger.warning("messages.yml: missing key '" + key + "'");
            return key;
        }
        if (value.trim().isEmpty()) {
            logger.warning("messages.yml: key '" + key + "' is empty");
            return key;
        }
        return value;
    }

    private String applyBuiltInPlaceholders(String message) {
        String resolved = message == null ? "" : message;
        YamlConfiguration yaml = messageConfig.getYamlConfiguration();
        String prefix = yaml.getString("prefix", "");
        if (prefix == null) {
            prefix = "";
        }
        return resolved.replace("{prefix}", prefix);
    }

    private String applyArgs(String template, Object... args) {
        String resolved = template == null ? "" : template;
        if (args == null || args.length == 0) {
            return resolved;
        }

        for (int i = 0; i < args.length; i++) {
            String token = "{" + i + "}";
            String value = args[i] == null ? "" : String.valueOf(args[i]);
            resolved = resolved.replace(token, value);
        }
        return resolved;
    }
}
