package cn.mythicland.mythiclobby.display;

public record DisplaySettings(
        boolean enabled,
        int updateIntervalTicks,
        String prefix,
        String suffix,
        boolean customJoinMessageEnabled,
        String joinMessage,
        boolean customQuitMessageEnabled,
        String quitMessage
) {
    public static final int DEFAULT_UPDATE_INTERVAL_TICKS = 20;
    public static final int TEAM_TEXT_LIMIT = 16;
    public static final String DEFAULT_JOIN_MESSAGE = "&a<player_name> joined the game.";
    public static final String DEFAULT_QUIT_MESSAGE = "&c<player_name> left the game.";

}
