package cn.mythicland.mythiclobby.scoreboard;

import java.util.ArrayList;
import java.util.List;

public record ScoreboardSettings(
        boolean enabled,
        int updateIntervalTicks,
        String title,
        List<String> lines
) {
    public static final int DEFAULT_UPDATE_INTERVAL_TICKS = 20;
    public static final int MAX_LINES = 15;
    public static final String DEFAULT_TITLE = "&6&lMythicLand";

    public ScoreboardSettings(
            boolean enabled,
            int updateIntervalTicks,
            String title,
            List<String> lines
    ) {
        this.enabled = enabled;
        this.updateIntervalTicks = updateIntervalTicks;
        this.title = title;
        this.lines = new ArrayList<>(lines);
    }

    @Override
    public List<String> lines() {
        return new ArrayList<>(lines);
    }
}
