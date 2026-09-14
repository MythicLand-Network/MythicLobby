package cn.mythicland.mythiclobby.config.key;

public enum ScoreboardConfigKey implements ConfigKey {
    ENABLED("enabled"),
    UPDATE_INTERVAL_TICKS("update-interval-ticks"),
    TITLE("title"),
    LINES("lines");

    private final String path;

    ScoreboardConfigKey(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
