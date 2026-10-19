package cn.mythicland.mythiclobby.scoreboard;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.framework.bukkit.message.CC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@InjectComponent
public final class ScoreboardRenderer {
    private static final String[] UNIQUE_SUFFIXES = {
            String.valueOf(ChatColor.RESET),
            String.valueOf(ChatColor.BLACK),
            String.valueOf(ChatColor.DARK_BLUE),
            String.valueOf(ChatColor.DARK_GREEN),
            String.valueOf(ChatColor.DARK_AQUA),
            String.valueOf(ChatColor.DARK_RED),
            String.valueOf(ChatColor.DARK_PURPLE),
            String.valueOf(ChatColor.GOLD),
            String.valueOf(ChatColor.GRAY),
            String.valueOf(ChatColor.DARK_GRAY),
            String.valueOf(ChatColor.BLUE),
            String.valueOf(ChatColor.GREEN),
            String.valueOf(ChatColor.AQUA),
            String.valueOf(ChatColor.RED),
            String.valueOf(ChatColor.LIGHT_PURPLE),
            String.valueOf(ChatColor.YELLOW),
            String.valueOf(ChatColor.WHITE)
    };

    private final PlaceholderService formatter;

    public ScoreboardRenderer(PlaceholderService formatter) {
        this.formatter = formatter;
    }

    public String renderTitle(Player player, String title) {
        return CC.translate(formatter.render(player, title));
    }

    public List<String> renderLines(Player player, List<String> lines) {
        List<String> renderedLines = new ArrayList<>(lines.size());
        Map<String, Integer> counts = new LinkedHashMap<>();

        for (String line : lines) {
            String rendered = CC.translate(formatter.render(player, line));
            rendered = rendered.isEmpty() ? " " : rendered;

            int duplicateCount = counts.getOrDefault(rendered, 0);
            counts.put(rendered, duplicateCount + 1);
            renderedLines.add(makeUnique(rendered, duplicateCount));
        }

        return renderedLines;
    }
    private String makeUnique(String line, int duplicateCount) {
        if (duplicateCount <= 0) {
            return line;
        }

        StringBuilder suffixBuilder = new StringBuilder();
        int remaining = duplicateCount;
        do {
            suffixBuilder.append(UNIQUE_SUFFIXES[remaining % UNIQUE_SUFFIXES.length]);
            remaining = (remaining / UNIQUE_SUFFIXES.length) - 1;
        } while (remaining >= 0);
        return line + suffixBuilder;
    }
}
