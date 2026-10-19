package cn.mythicland.mythiclobby.hologram;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.component.LoadOrder;
import cn.mythicland.framework.component.LoadPhase;
import cn.mythicland.framework.component.ManagedLifecycle;
import cn.mythicland.mythiclobby.platform.hologram.HologramPlatform;
import cn.mythicland.framework.bukkit.location.LocationSnapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@InjectComponent
@LoadOrder(phase = LoadPhase.ADAPTERS)
public final class HologramService implements ManagedLifecycle {
    public static final double LINE_SPACING = 0.25D;

    private final JavaPlugin plugin;
    private final HologramRepository repository;
    private final HologramRenderer renderer;
    private final HologramPlatform hologramPlatform;

    private final Map<String, StoredHologram> hologramsByName = new LinkedHashMap<>();
    private final Map<UUID, Map<String, ViewerHologramState>> viewerStatesByPlayer = new LinkedHashMap<>();

    public HologramService(
            JavaPlugin plugin,
            HologramRepository repository,
            HologramRenderer renderer,
            HologramPlatform hologramPlatform
    ) {
        this.plugin = plugin;
        this.repository = repository;
        this.renderer = renderer;
        this.hologramPlatform = hologramPlatform;
    }

    @Override
    public void load() {
        this.loadAll();
        this.start();
    }

    @Override
    public void reload() {
        this.reloadAll();
    }

    @Override
    public void unload() {
        this.shutdown();
    }

    public void loadAll() {
        hologramsByName.clear();
        for (StoredHologram storedHologram : repository.loadAll()) {
            hologramsByName.put(HologramNames.normalize(storedHologram.name()), storedHologram);
        }
    }

    public void start() {
        refreshAll();
    }

    public void reloadAll() {
        repository.reload();
        destroyAllViewerStates();
        loadAll();
        refreshAll();
    }

    public void shutdown() {
        destroyAllViewerStates();
        hologramsByName.clear();
    }

    public void handleJoin(Player player) {
        syncViewer(player);
    }

    public void handleQuit(Player player) {
        viewerStatesByPlayer.remove(player.getUniqueId());
    }

    public void handleWorldChange(Player player) {
        destroyViewerStates(player);
        syncViewer(player);
    }

    public void handleChunkChange(Player player) {
        syncViewer(player);
    }

    public boolean createHologram(String name, List<String> displayLines, Location location) {
        String sanitizedName = sanitizeName(name);
        if (sanitizedName == null || displayLines.isEmpty() || location.getWorld() == null) {
            return false;
        }

        String normalizedName = HologramNames.normalize(sanitizedName);
        if (hologramsByName.containsKey(normalizedName)) {
            return false;
        }

        List<String> sanitizedLines = sanitizeDisplayLines(displayLines);
        if (sanitizedLines.isEmpty()) {
            return false;
        }

        StoredHologram storedHologram = new StoredHologram(
                sanitizedName,
                sanitizedLines,
                normalizeCreationLocation(location)
        );
        hologramsByName.put(normalizedName, storedHologram);
        repository.save(storedHologram);
        refreshAll();
        return true;
    }

    public boolean removeHologram(String name) {
        String sanitizedName = sanitizeName(name);
        if (sanitizedName == null) {
            return false;
        }

        StoredHologram removed = hologramsByName.remove(HologramNames.normalize(sanitizedName));
        if (removed == null) {
            return false;
        }

        repository.delete(removed.name());
        destroyHologramForAllViewers(removed.name());
        return true;
    }

    private void refreshAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            syncViewer(viewer);
        }
    }

    private void syncViewer(Player viewer) {
        if (!viewer.isOnline() || viewer.getWorld() == null) {
            destroyViewerStates(viewer);
            return;
        }

        Map<String, ViewerHologramState> viewerStates = viewerStatesByPlayer.get(viewer.getUniqueId());
        if (viewerStates == null) {
            viewerStates = new LinkedHashMap<>();
            viewerStatesByPlayer.put(viewer.getUniqueId(), viewerStates);
        }

        List<String> hiddenHolograms = new ArrayList<>();
        for (Map.Entry<String, ViewerHologramState> entry : viewerStates.entrySet()) {
            StoredHologram storedHologram = hologramsByName.get(entry.getKey());
            if (storedHologram == null || !isVisibleTo(viewer, storedHologram)) {
                destroyState(viewer, entry.getValue());
                hiddenHolograms.add(entry.getKey());
            }
        }
        for (String hiddenHologram : hiddenHolograms) {
            viewerStates.remove(hiddenHologram);
        }

        for (Map.Entry<String, StoredHologram> entry : hologramsByName.entrySet()) {
            StoredHologram storedHologram = entry.getValue();
            if (!isVisibleTo(viewer, storedHologram)) {
                continue;
            }

            List<String> renderedLines = renderLines(viewer, storedHologram);
            ViewerHologramState state = viewerStates.get(entry.getKey());
            if (state == null) {
                viewerStates.put(entry.getKey(), spawnState(viewer, storedHologram, renderedLines));
                continue;
            }

            if (hologramPlatform.getLineCount(state.getViewerState()) != renderedLines.size()) {
                destroyState(viewer, state);
                viewerStates.put(entry.getKey(), spawnState(viewer, storedHologram, renderedLines));
                continue;
            }

            refreshState(viewer, storedHologram, state, renderedLines);
        }

        if (viewerStates.isEmpty()) {
            viewerStatesByPlayer.remove(viewer.getUniqueId());
        }
    }

    private boolean isVisibleTo(Player viewer, StoredHologram storedHologram) {
        World hologramWorld = storedHologram.location().getWorld();
        return hologramWorld != null && hologramWorld.equals(viewer.getWorld());
    }

    private ViewerHologramState spawnState(Player viewer, StoredHologram storedHologram, List<String> renderedLines) {
        Object viewerState = hologramPlatform.spawn(viewer, storedHologram.location(), renderedLines);
        return new ViewerHologramState(storedHologram.name(), viewerState, storedHologram.location(), renderedLines);
    }

    private void refreshState(
            Player viewer,
            StoredHologram storedHologram,
            ViewerHologramState state,
            List<String> renderedLines
    ) {
        Location location = storedHologram.location();
        if (sameLocation(state.getBaseLocation(), location)
                && Objects.equals(state.getRenderedLines(), renderedLines)) {
            return;
        }

        hologramPlatform.update(viewer, state.getViewerState(), location, renderedLines);
        state.setBaseLocation(location);
        state.setRenderedLines(renderedLines);
    }

    private List<String> renderLines(Player viewer, StoredHologram storedHologram) {
        List<String> displayLines = storedHologram.displayLines();
        List<String> renderedLines = new ArrayList<>(displayLines.size());
        for (String displayLine : displayLines) {
            renderedLines.add(renderer.renderLine(viewer, displayLine));
        }
        return renderedLines;
    }

    private void destroyAllViewerStates() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            destroyViewerStates(viewer);
        }
        viewerStatesByPlayer.clear();
    }

    private void destroyViewerStates(Player viewer) {
        Map<String, ViewerHologramState> viewerStates = viewerStatesByPlayer.remove(viewer.getUniqueId());
        if (viewerStates == null) {
            return;
        }
        for (ViewerHologramState state : viewerStates.values()) {
            destroyState(viewer, state);
        }
    }

    private void destroyHologramForAllViewers(String hologramName) {
        String normalizedName = HologramNames.normalize(hologramName);
        List<UUID> emptyViewers = new ArrayList<>();
        for (Map.Entry<UUID, Map<String, ViewerHologramState>> entry : viewerStatesByPlayer.entrySet()) {
            Player viewer = Bukkit.getPlayer(entry.getKey());
            ViewerHologramState state = entry.getValue().remove(normalizedName);
            if (viewer != null && state != null) {
                destroyState(viewer, state);
            }
            if (entry.getValue().isEmpty()) {
                emptyViewers.add(entry.getKey());
            }
        }
        for (UUID emptyViewer : emptyViewers) {
            viewerStatesByPlayer.remove(emptyViewer);
        }
    }

    private void destroyState(Player viewer, ViewerHologramState state) {
        if (!viewer.isOnline()) {
            return;
        }
        hologramPlatform.destroy(viewer, state.getViewerState());
    }

    private String sanitizeName(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty() || trimmed.contains(".")) {
            return null;
        }
        return trimmed;
    }

    private Location normalizeCreationLocation(Location location) {
        return LocationSnapper.snapBlockAndYaw(location);
    }

    private List<String> sanitizeDisplayLines(List<String> input) {
        List<String> lines = new ArrayList<>();
        for (String line : input) {
            if (line == null) {
                continue;
            }
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        return lines;
    }

    private boolean sameLocation(Location first, Location second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.getWorld() == second.getWorld()
                && Double.compare(first.getX(), second.getX()) == 0
                && Double.compare(first.getY(), second.getY()) == 0
                && Double.compare(first.getZ(), second.getZ()) == 0
                && Float.compare(first.getYaw(), second.getYaw()) == 0
                && Float.compare(first.getPitch(), second.getPitch()) == 0;
    }

    private static final class ViewerHologramState {
        private final String hologramName;
        private final Object viewerState;
        private Location baseLocation;
        private List<String> renderedLines;

        private ViewerHologramState(
                String hologramName,
                Object viewerState,
                Location baseLocation,
                List<String> renderedLines
        ) {
            this.hologramName = hologramName;
            this.viewerState = viewerState;
            this.baseLocation = baseLocation.clone();
            this.renderedLines = new ArrayList<>(renderedLines);
        }

        public String getHologramName() {
            return hologramName;
        }

        public Object getViewerState() {
            return viewerState;
        }

        public Location getBaseLocation() {
            return baseLocation.clone();
        }

        public void setBaseLocation(Location baseLocation) {
            this.baseLocation = baseLocation.clone();
        }

        public List<String> getRenderedLines() {
            return Collections.unmodifiableList(renderedLines);
        }

        public void setRenderedLines(List<String> renderedLines) {
            this.renderedLines = new ArrayList<>(renderedLines);
        }
    }
}
