package cn.mythicland.mythiclobby.config.key;

public enum LobbyConfigKey implements ConfigKey {
    MESSAGES_JOIN_MESSAGE("messages.join-message"),
    MESSAGES_QUIT_MESSAGE("messages.quit-message");

    private final String path;

    LobbyConfigKey(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}
