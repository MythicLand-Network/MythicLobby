package cn.mythicland.mythiclobby.display;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.mythiclobby.util.CC;
import org.bukkit.entity.Player;

@InjectComponent
public final class DisplayRenderer {
    private final PlaceholderService formatter;

    public DisplayRenderer(PlaceholderService formatter) {
        this.formatter = formatter;
    }

    public String renderPrefix(Player player, String prefix) {
        return CC.translate(formatter.render(player, prefix));
    }

    public String renderSuffix(Player player, String suffix) {
        return CC.translate(formatter.render(player, suffix));
    }

    public String renderMessage(Player player, String message) {
        return CC.translate(formatter.render(player, message));
    }
}
