package cn.mythicland.mythiclobby.util;

import java.util.ArrayList;
import java.util.List;

public final class NpcDisplayParser {
    private NpcDisplayParser() {
    }

    public static List<String> parse(String rawDisplay) {
        String normalized = rawDisplay.replace("\\n", "\n");
        List<String> lines = new ArrayList<>();
        for (String line : normalized.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        if (lines.isEmpty()) {
            lines.add(rawDisplay);
        }
        return lines;
    }
}
