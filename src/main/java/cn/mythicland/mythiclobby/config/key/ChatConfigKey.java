package cn.mythicland.mythiclobby.config.key;

public enum ChatConfigKey implements ConfigKey {
    ENABLED("enabled"),
    FORMAT("format"),
    MUTE_ALL_ENABLED("mute-all.enabled"),
    MUTE_ALL_BYPASS_PERMISSION("mute-all.bypass-permission"),
    MUTE_ALL_DENIED_MESSAGE("mute-all.denied-message");

    private final String path;

    ChatConfigKey(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
