package cn.mythicland.mythiclobby.tab;

import java.util.ArrayList;
import java.util.List;

public record TabSettings(
        boolean enabled,
        int updateIntervalTicks,
        List<String> header,
        List<String> footer,
        String prefix, String suffix
) {
    public static final int DEFAULT_UPDATE_INTERVAL_TICKS = 20;

    public TabSettings(
            boolean enabled,
            int updateIntervalTicks,
            List<String> header,
            List<String> footer,
            String prefix,
            String suffix
    ) {
        this.enabled = enabled;
        this.updateIntervalTicks = updateIntervalTicks;
        this.header = new ArrayList<>(header);
        this.footer = new ArrayList<>(footer);
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public List<String> header() {
        return new ArrayList<>(header);
    }

    @Override
    public List<String> footer() {
        return new ArrayList<>(footer);
    }
}
