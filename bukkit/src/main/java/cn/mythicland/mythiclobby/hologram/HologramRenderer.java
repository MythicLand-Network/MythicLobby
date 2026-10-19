package cn.mythicland.mythiclobby.hologram;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.framework.bukkit.message.CC;
import org.bukkit.entity.Player;

@InjectComponent
public final class HologramRenderer {
    private final PlaceholderService formatter;

    public HologramRenderer(PlaceholderService formatter) {
        this.formatter = formatter;
    }

    public String renderLine(Player viewer, String line) {
        return CC.translate(formatter.render(viewer, line == null ? "" : line));
    }
}
