package cn.mythicland.mythiclobby.tab;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.mythiclobby.util.CC;
import org.bukkit.entity.Player;

import java.util.List;

@InjectComponent
public final class TabRenderer {
    private final PlaceholderService formatter;

    public TabRenderer(PlaceholderService formatter) {
        this.formatter = formatter;
    }

    public String renderHeader(Player viewer, List<String> lines) {
        return renderMultiline(viewer, lines);
    }

    public String renderFooter(Player viewer, List<String> lines) {
        return renderMultiline(viewer, lines);
    }

    public String renderPrefix(Player target, String prefix) {
        return CC.translate(formatter.render(target, prefix));
    }

    public String renderSuffix(Player target, String suffix) {
        return CC.translate(formatter.render(target, suffix));
    }

    public String renderDisplayName(Player target, String prefix, String suffix) {
        return renderPrefix(target, prefix) + target.getName() + renderSuffix(target, suffix);
    }

    private String renderMultiline(Player viewer, List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(CC.translate(formatter.render(viewer, lines.get(i))));
        }
        return builder.toString();
    }
}
