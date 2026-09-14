package cn.mythicland.mythiclobby.platform.legacy;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.hologram.HologramService;
import cn.mythicland.mythiclobby.platform.hologram.HologramPlatform;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@InjectComponent
public final class LegacyHologramPlatform implements HologramPlatform {
    @Override
    public Object spawn(Player viewer, Location baseLocation, List<String> renderedLines) {
        List<LegacyLineState> lines = new ArrayList<>(renderedLines.size());
        for (int i = 0; i < renderedLines.size(); i++) {
            Location lineLocation = getLineLocation(baseLocation, i);
            EntityArmorStand armorStand = createArmorStand(lineLocation, renderedLines.get(i));
            sendPacket(viewer, new PacketPlayOutSpawnEntityLiving(armorStand));
            sendPacket(viewer, new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
            lines.add(new LegacyLineState(armorStand, lineLocation, renderedLines.get(i)));
        }
        return new LegacyViewerState(lines);
    }

    @Override
    public void update(Player viewer, Object viewerState, Location baseLocation, List<String> renderedLines) {
        LegacyViewerState state = (LegacyViewerState) viewerState;
        for (int i = 0; i < state.lines.size(); i++) {
            LegacyLineState lineState = state.lines.get(i);
            Location lineLocation = getLineLocation(baseLocation, i);
            if (!sameLocation(lineState.location, lineLocation)) {
                lineState.armorStand.setLocation(lineLocation.getX(), lineLocation.getY(), lineLocation.getZ(), lineLocation.getYaw(), lineLocation.getPitch());
                sendPacket(viewer, new PacketPlayOutEntityTeleport(lineState.armorStand));
                lineState.location = lineLocation.clone();
            }

            String renderedLine = renderedLines.get(i);
            if (!Objects.equals(lineState.renderedLine, renderedLine)) {
                lineState.armorStand.setCustomName(renderedLine);
                sendPacket(viewer, new PacketPlayOutEntityMetadata(lineState.armorStand.getId(), lineState.armorStand.getDataWatcher(), true));
                lineState.renderedLine = renderedLine;
            }
        }
    }

    @Override
    public void destroy(Player viewer, Object viewerState) {
        LegacyViewerState state = (LegacyViewerState) viewerState;
        int[] entityIds = new int[state.lines.size()];
        for (int i = 0; i < state.lines.size(); i++) {
            entityIds[i] = state.lines.get(i).armorStand.getId();
        }
        sendPacket(viewer, new PacketPlayOutEntityDestroy(entityIds));
    }

    @Override
    public int getLineCount(Object viewerState) {
        return ((LegacyViewerState) viewerState).lines.size();
    }

    private EntityArmorStand createArmorStand(Location location, String renderedLine) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(world, location.getX(), location.getY(), location.getZ());
        armorStand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setSmall(true);
        armorStand.setBasePlate(false);
        armorStand.n(true);
        armorStand.setCustomName(renderedLine);
        armorStand.setCustomNameVisible(true);
        return armorStand;
    }

    private Location getLineLocation(Location baseLocation, int index) {
        return baseLocation.clone().add(0.0D, -HologramService.LINE_SPACING * index, 0.0D);
    }

    private boolean sameLocation(Location first, Location second) {
        return first.getWorld() == second.getWorld()
                && Double.compare(first.getX(), second.getX()) == 0
                && Double.compare(first.getY(), second.getY()) == 0
                && Double.compare(first.getZ(), second.getZ()) == 0
                && Float.compare(first.getYaw(), second.getYaw()) == 0
                && Float.compare(first.getPitch(), second.getPitch()) == 0;
    }

    private void sendPacket(Player viewer, Packet<?> packet) {
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }

    private static final class LegacyViewerState {
        private final List<LegacyLineState> lines;

        private LegacyViewerState(List<LegacyLineState> lines) {
            this.lines = lines;
        }
    }

    private static final class LegacyLineState {
        private final EntityArmorStand armorStand;
        private Location location;
        private String renderedLine;

        private LegacyLineState(EntityArmorStand armorStand, Location location, String renderedLine) {
            this.armorStand = armorStand;
            this.location = location.clone();
            this.renderedLine = renderedLine;
        }
    }
}
