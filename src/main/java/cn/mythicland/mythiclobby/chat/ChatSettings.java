package cn.mythicland.mythiclobby.chat;

public record ChatSettings(
        boolean enabled,
        String format,
        boolean muteAllEnabled
) {
    public static final String DEFAULT_FORMAT = "<luckperms_prefix><player_name>&7: &f%message%";
    public static final String DEFAULT_MUTE_DENIED_MESSAGE = "&c你不能在这个服务器上发言。";
}
