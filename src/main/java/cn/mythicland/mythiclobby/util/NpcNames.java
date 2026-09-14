package cn.mythicland.mythiclobby.util;

import java.util.Locale;

public final class NpcNames {
    private NpcNames() {
    }

    public static String normalize(String npcName) {
        return npcName.toLowerCase(Locale.ROOT);
    }
}
