package cn.mythicland.mythiclobby.config.key;

public enum ChatConfigKey implements ConfigKey {
    ENABLED("enabled"),
    FORMAT("format"),
    MUTE_ALL_ENABLED("mute-all.enabled");

    private final String path;

    ChatConfigKey(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
