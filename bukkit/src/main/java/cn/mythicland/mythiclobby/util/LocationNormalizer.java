package cn.mythicland.mythiclobby.util;

import org.bukkit.Location;

public final class LocationNormalizer {
    private LocationNormalizer() {
    }

    public static Location normalizeSpawnLocation(Location location) {
        Location normalized = location.clone();
        normalized.setX(snapToBlockCenter(location.getX()));
        normalized.setY(location.getY());
        normalized.setZ(snapToBlockCenter(location.getZ()));
        normalized.setYaw(snapYawToNearest45(location.getYaw()));
        normalized.setPitch(0.0F);
        return normalized;
    }

    public static double snapToBlockCenter(double coordinate) {
        return Math.floor(coordinate) + 0.5D;
    }

    public static float snapYawToNearest45(float yaw) {
        float normalizedYaw = yaw % 360.0F;
        if (normalizedYaw < 0.0F) {
            normalizedYaw += 360.0F;
        }

        float snappedYaw = Math.round(normalizedYaw / 45.0F) * 45.0F;
        if (snappedYaw >= 360.0F) {
            snappedYaw -= 360.0F;
        }
        if (snappedYaw > 180.0F) {
            snappedYaw -= 360.0F;
        }
        return snappedYaw;
    }
}
