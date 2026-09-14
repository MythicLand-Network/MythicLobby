package cn.mythicland.mythiclobby.npc;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.platform.npc.NpcPlatform;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.mythiclobby.util.CC;
import cn.mythicland.mythiclobby.util.NpcNames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@InjectComponent
public final class NpcRegistry {
    private static final long DISPLAY_REFRESH_INTERVAL_TICKS = 20L;

    private final Plugin plugin;
    private final PluginLogger logger;
    private final NpcPlatform npcPlatform;
    private final PlaceholderService placeholderService;
    private final Map<String, ManagedNpc> npcsByName = new LinkedHashMap<>();
    private final Map<UUID, ManagedNpc> npcsByUniqueId = new LinkedHashMap<>();
    private BukkitTask displayRefreshTask;

    public NpcRegistry(
            Plugin plugin,
            PluginLogger logger,
            NpcPlatform npcPlatform,
            PlaceholderService placeholderService
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.npcPlatform = npcPlatform;
        this.placeholderService = placeholderService;
    }

    public ManagedNpc spawn(StoredNpc storedNpc) {
        Location location = storedNpc.getLocation();
        if (location.getWorld() == null) {
            logger.warning("Skipping NPC " + storedNpc.getName() + " because its world is missing.");
            return null;
        }

        List<String> renderedDisplayLines = renderDisplayLines(storedNpc);
        Object runtimeNpc = createNpcInstance(storedNpc, renderedDisplayLines);
        ManagedNpc managedNpc = new ManagedNpc(
                storedNpc,
                runtimeNpc,
                renderedDisplayLines,
                hasPlaceholderTokens(storedNpc.getDisplayLines())
        );
        register(managedNpc);
        showToAllPlayers(runtimeNpc);
        return managedNpc;
    }

    public ManagedNpc remove(String npcName) {
        ManagedNpc managedNpc = npcsByName.remove(NpcNames.normalize(npcName));
        if (managedNpc == null) {
            return null;
        }

        npcsByUniqueId.remove(npcPlatform.getUniqueId(managedNpc.getRuntimeNpc()));
        npcPlatform.destroy(managedNpc.getRuntimeNpc());
        return managedNpc;
    }

    public void respawn(ManagedNpc managedNpc) {
        Object previousNpc = managedNpc.getRuntimeNpc();
        npcsByUniqueId.remove(npcPlatform.getUniqueId(previousNpc));
        npcPlatform.destroy(previousNpc);

        List<String> renderedDisplayLines = renderDisplayLines(managedNpc.getStoredNpc());
        Object replacementNpc = createNpcInstance(managedNpc.getStoredNpc(), renderedDisplayLines);
        managedNpc.setRuntimeNpc(replacementNpc);
        managedNpc.setRenderedDisplayLines(renderedDisplayLines);
        npcsByUniqueId.put(npcPlatform.getUniqueId(replacementNpc), managedNpc);
        showToAllPlayers(replacementNpc);
    }

    public ManagedNpc getByName(String npcName) {
        return npcsByName.get(NpcNames.normalize(npcName));
    }

    public ManagedNpc getByUniqueId(UUID uniqueId) {
        return npcsByUniqueId.get(uniqueId);
    }

    public boolean containsName(String npcName) {
        return npcsByName.containsKey(NpcNames.normalize(npcName));
    }

    public Collection<ManagedNpc> getAll() {
        return new ArrayList<>(npcsByName.values());
    }

    public ManagedNpc findNpcInSight(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();
        double maxDistance = 10.0D;
        double hitRadius = 1.5D;
        ManagedNpc bestMatch = null;
        double bestDistance = Double.MAX_VALUE;

        for (ManagedNpc managedNpc : npcsByName.values()) {
            Location npcLocation = npcPlatform.getLocation(managedNpc.getRuntimeNpc());
            if (npcLocation == null || npcLocation.getWorld() == null || !npcLocation.getWorld().equals(player.getWorld())) {
                continue;
            }

            Vector toNpc = npcLocation.clone().add(0.0D, 1.6D, 0.0D).toVector().subtract(eyeLocation.toVector());
            double alongSight = toNpc.dot(direction);
            if (alongSight < 0.0D || alongSight > maxDistance) {
                continue;
            }

            Vector closestPoint = direction.clone().multiply(alongSight);
            double distanceFromSight = toNpc.subtract(closestPoint).length();
            if (distanceFromSight > hitRadius || alongSight >= bestDistance) {
                continue;
            }

            bestDistance = alongSight;
            bestMatch = managedNpc;
        }

        return bestMatch;
    }

    public void showAllToPlayer(Player player) {
        for (ManagedNpc managedNpc : npcsByName.values()) {
            Object runtimeNpc = managedNpc.getRuntimeNpc();
            if (!npcPlatform.isShown(runtimeNpc, player)) {
                npcPlatform.show(runtimeNpc, player);
            }
        }
    }

    public void clear() {
        cancelDisplayRefreshTask();
        for (ManagedNpc managedNpc : new ArrayList<>(npcsByName.values())) {
            npcPlatform.destroy(managedNpc.getRuntimeNpc());
        }
        npcsByName.clear();
        npcsByUniqueId.clear();
    }

    private void register(ManagedNpc managedNpc) {
        npcsByName.put(NpcNames.normalize(managedNpc.getStoredNpc().getName()), managedNpc);
        npcsByUniqueId.put(npcPlatform.getUniqueId(managedNpc.getRuntimeNpc()), managedNpc);
    }

    private void showToAllPlayers(Object runtimeNpc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!npcPlatform.isShown(runtimeNpc, player)) {
                npcPlatform.show(runtimeNpc, player);
            }
        }
    }

    public void restartDisplayRefreshTask() {
        cancelDisplayRefreshTask();
        if (!hasDynamicDisplays()) {
            return;
        }

        displayRefreshTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::refreshDynamicDisplays,
                DISPLAY_REFRESH_INTERVAL_TICKS,
                DISPLAY_REFRESH_INTERVAL_TICKS
        );
    }

    private void cancelDisplayRefreshTask() {
        if (displayRefreshTask != null) {
            displayRefreshTask.cancel();
            displayRefreshTask = null;
        }
    }

    private boolean hasDynamicDisplays() {
        for (ManagedNpc managedNpc : npcsByName.values()) {
            if (managedNpc.hasDynamicDisplay()) {
                return true;
            }
        }
        return false;
    }

    private void refreshDynamicDisplays() {
        for (ManagedNpc managedNpc : new ArrayList<>(npcsByName.values())) {
            if (!managedNpc.hasDynamicDisplay()) {
                continue;
            }

            List<String> renderedDisplayLines = renderDisplayLines(managedNpc.getStoredNpc());
            if (Objects.equals(managedNpc.getRenderedDisplayLines(), renderedDisplayLines)) {
                continue;
            }

            respawn(managedNpc, renderedDisplayLines);
        }
    }

    private void respawn(ManagedNpc managedNpc, List<String> renderedDisplayLines) {
        Object previousNpc = managedNpc.getRuntimeNpc();
        npcsByUniqueId.remove(npcPlatform.getUniqueId(previousNpc));
        npcPlatform.destroy(previousNpc);

        Object replacementNpc = createNpcInstance(managedNpc.getStoredNpc(), renderedDisplayLines);
        managedNpc.setRuntimeNpc(replacementNpc);
        managedNpc.setRenderedDisplayLines(renderedDisplayLines);
        npcsByUniqueId.put(npcPlatform.getUniqueId(replacementNpc), managedNpc);
        showToAllPlayers(replacementNpc);
    }

    private Object createNpcInstance(StoredNpc storedNpc, List<String> renderedDisplayLines) {
        return npcPlatform.spawn(storedNpc.getLocation(), renderedDisplayLines);
    }

    private List<String> renderDisplayLines(StoredNpc storedNpc) {
        List<String> rendered = new ArrayList<>(storedNpc.getDisplayLines().size());
        for (String line : storedNpc.getDisplayLines()) {
            // NPC text is shared between viewers, so placeholder rendering uses a global context.
            String resolved = CC.translate(placeholderService.render(null, line));
            rendered.add(resolved.isEmpty() ? " " : resolved);
        }
        return rendered;
    }

    private boolean hasPlaceholderTokens(List<String> lines) {
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            if ((line.indexOf('<') >= 0 && line.indexOf('>') > line.indexOf('<')) || hasPercentPlaceholder(line)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPercentPlaceholder(String line) {
        int firstPercent = line.indexOf('%');
        if (firstPercent < 0) {
            return false;
        }
        int secondPercent = line.indexOf('%', firstPercent + 1);
        return secondPercent > firstPercent + 1;
    }

}
