package cn.mythicland.mythiclobby.hologram;

import java.util.Locale;

public final class HologramNames {
    private HologramNames() {
    }

    public static String normalize(String hologramName) {
        return hologramName.toLowerCase(Locale.ROOT);
    }
}
