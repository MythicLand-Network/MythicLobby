package cn.mythicland.mythiclobby.chat;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.framework.bukkit.message.CC;
import org.bukkit.entity.Player;

@InjectComponent
public final class ChatRenderer {
    private static final String MESSAGE_TOKEN = "%message%";
    private static final String MESSAGE_SENTINEL = "__ML_CHAT_MESSAGE__";

    private final PlaceholderService formatter;

    public ChatRenderer(PlaceholderService formatter) {
        this.formatter = formatter;
    }

    public String renderFormat(Player player, String format) {
        String rendered = formatter.render(player, format == null ? "" : format);
        rendered = CC.translate(rendered);
        rendered = rendered.replace(MESSAGE_TOKEN, MESSAGE_SENTINEL);
        rendered = rendered.replace("%", "%%");
        return rendered.replace(MESSAGE_SENTINEL, "%2$s");
    }
}
