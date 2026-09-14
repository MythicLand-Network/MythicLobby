package cn.mythicland.mythiclobby.config.key;

public enum TabConfigKey implements ConfigKey {
    ENABLED("enabled"),
    UPDATE_INTERVAL_TICKS("update-interval-ticks"),
    HEADER("header"),
    FOOTER("footer"),
    NAME_FORMAT_PREFIX("name-format.prefix"),
    NAME_FORMAT_SUFFIX("name-format.suffix");

    private final String path;

    TabConfigKey(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
