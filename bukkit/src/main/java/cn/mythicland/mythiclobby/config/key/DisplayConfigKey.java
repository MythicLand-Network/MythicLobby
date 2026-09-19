package cn.mythicland.mythiclobby.config.key;

public enum DisplayConfigKey implements ConfigKey {
    ENABLED("enabled"),
    UPDATE_INTERVAL_TICKS("update-interval-ticks"),
    NAME_FORMAT_PREFIX("name-format.prefix"),
    NAME_FORMAT_SUFFIX("name-format.suffix");

    private final String path;

    DisplayConfigKey(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
