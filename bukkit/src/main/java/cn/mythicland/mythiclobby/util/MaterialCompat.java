package cn.mythicland.mythiclobby.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public final class MaterialCompat {
    private static final String[] STAINED_GLASS_PANE_MATERIALS = {
            "WHITE_STAINED_GLASS_PANE",
            "ORANGE_STAINED_GLASS_PANE",
            "MAGENTA_STAINED_GLASS_PANE",
            "LIGHT_BLUE_STAINED_GLASS_PANE",
            "YELLOW_STAINED_GLASS_PANE",
            "LIME_STAINED_GLASS_PANE",
            "PINK_STAINED_GLASS_PANE",
            "GRAY_STAINED_GLASS_PANE",
            "LIGHT_GRAY_STAINED_GLASS_PANE",
            "CYAN_STAINED_GLASS_PANE",
            "PURPLE_STAINED_GLASS_PANE",
            "BLUE_STAINED_GLASS_PANE",
            "BROWN_STAINED_GLASS_PANE",
            "GREEN_STAINED_GLASS_PANE",
            "RED_STAINED_GLASS_PANE",
            "BLACK_STAINED_GLASS_PANE"
    };

    private MaterialCompat() {
    }

    public static Material matchMaterial(String rawName) {
        String materialName = trimToEmpty(rawName);
        if (materialName.isEmpty()) {
            return null;
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.matchMaterial(materialName.toUpperCase(Locale.ROOT));
        }
        return material;
    }

    public static ItemStack createItemStack(Material material, int amount, short data) {
        if (material == null) {
            throw new IllegalArgumentException("material cannot be null");
        }
        if (data <= 0) {
            return new ItemStack(material, amount);
        }
        return new ItemStack(material, amount, data);
    }

    public static ItemStack createGlassPane(short color) {
        int normalizedColor = Math.max(0, Math.min(15, color));

        Material modernPane = matchMaterial(STAINED_GLASS_PANE_MATERIALS[normalizedColor]);
        if (modernPane != null) {
            return new ItemStack(modernPane, 1);
        }

        Material legacyPane = matchMaterial("STAINED_GLASS_PANE");
        if (legacyPane != null) {
            return new ItemStack(legacyPane, 1, (short) normalizedColor);
        }

        Material fallbackPane = matchMaterial("GLASS_PANE");
        if (fallbackPane != null) {
            return new ItemStack(fallbackPane, 1);
        }

        return new ItemStack(Material.AIR);
    }

    private static String trimToEmpty(String input) {
        return input == null ? "" : input.trim();
    }
}
