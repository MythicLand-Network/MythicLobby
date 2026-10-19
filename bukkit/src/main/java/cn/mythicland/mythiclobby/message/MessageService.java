package cn.mythicland.mythiclobby.message;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.key.MessageKey;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.framework.bukkit.message.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@InjectComponent
public final class MessageService {
    private final PlaceholderService placeholderService;

    public MessageService(PlaceholderService placeholderService) {
        this.placeholderService = placeholderService;
    }

    public void send(Player player, MessageKey key, Object... args) {
        player.sendMessage(format(player, key, args));
    }

    public void send(CommandSender sender, MessageKey key, Object... args) {
        if (sender == null) {
            return;
        }
        Player player = sender instanceof Player ? (Player) sender : null;
        sender.sendMessage(format(player, key, args));
    }

    public String format(Player player, MessageKey key, Object... args) {
        String rendered = applyArgs(key.getTemplate(), args);
        rendered = placeholderService.render(player, rendered);
        return CC.translate(rendered);
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
