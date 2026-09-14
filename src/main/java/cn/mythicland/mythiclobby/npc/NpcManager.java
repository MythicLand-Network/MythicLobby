package cn.mythicland.mythiclobby.npc;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.component.LoadOrder;
import cn.mythicland.framework.component.LoadPhase;
import cn.mythicland.framework.component.ManagedLifecycle;
import cn.mythicland.mythiclobby.util.CC;
import cn.mythicland.mythiclobby.util.LocationNormalizer;
import cn.mythicland.mythiclobby.util.NpcNames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@InjectComponent
@LoadOrder(phase = LoadPhase.ADAPTERS)
public final class NpcManager implements ManagedLifecycle {
    private static final Pattern MINECRAFT_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    private final JavaPlugin plugin;
    private final NpcRepository repository;
    private final NpcRegistry registry;
    private final Map<UUID, String> selectedNpcByPlayer = new LinkedHashMap<>();

    public NpcManager(JavaPlugin plugin, NpcRepository repository, NpcRegistry registry) {
        this.plugin = plugin;
        this.repository = repository;
        this.registry = registry;
    }

    @Override
    public void load() {
        this.loadAll();
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
        registry.clear();
        selectedNpcByPlayer.clear();
        for (StoredNpc storedNpc : repository.loadAll()) {
            registry.spawn(storedNpc);
        }
        registry.restartDisplayRefreshTask();
    }

    public void reloadAll() {
        repository.reload();
        loadAll();
    }

    public void shutdown() {
        selectedNpcByPlayer.clear();
        registry.clear();
    }

    public boolean createNpc(String npcName, Location location, Player creator) {
        return createNpc(npcName, Collections.singletonList(npcName), location, creator);
    }

    public boolean createNpc(List<String> displayLines, Location location, Player creator) {
        return createNpc(generateNpcId(), displayLines, location, creator);
    }

    public boolean createNpc(String npcName, List<String> displayLines, Location location, Player creator) {
        if (registry.containsName(npcName)) {
            return false;
        }

        StoredNpc storedNpc = new StoredNpc(
                npcName,
                displayLines,
                LocationNormalizer.normalizeSpawnLocation(location),
                resolveSkinPlayerName(displayLines, creator),
                new ArrayList<>()
        );

        ManagedNpc managedNpc = registry.spawn(storedNpc);
        if (managedNpc == null) {
            return false;
        }

        repository.save(storedNpc);
        selectedNpcByPlayer.put(creator.getUniqueId(), storedNpc.getName());
        return true;
    }

    public boolean removeSelectedNpc(Player player) {
        ManagedNpc selectedNpc = getSelectedNpc(player);
        return selectedNpc != null && removeNpc(selectedNpc.getStoredNpc().getName());
    }

    public boolean removeNpc(String npcName) {
        ManagedNpc removedNpc = registry.remove(npcName);
        if (removedNpc == null) {
            return false;
        }

        repository.delete(removedNpc.getStoredNpc().getName());
        selectedNpcByPlayer.entrySet().removeIf(entry -> NpcNames.normalize(entry.getValue())
                .equals(NpcNames.normalize(npcName)));
        return true;
    }

    public boolean selectNpc(Player player, String npcName) {
        ManagedNpc managedNpc = registry.getByName(npcName);
        if (managedNpc == null) {
            return false;
        }
        selectedNpcByPlayer.put(player.getUniqueId(), managedNpc.getStoredNpc().getName());
        return true;
    }

    public ManagedNpc selectNpcInSight(Player player) {
        ManagedNpc managedNpc = registry.findNpcInSight(player);
        if (managedNpc == null) {
            return null;
        }
        selectedNpcByPlayer.put(player.getUniqueId(), managedNpc.getStoredNpc().getName());
        return managedNpc;
    }

    public ManagedNpc getSelectedNpc(Player player) {
        String selectedNpc = selectedNpcByPlayer.get(player.getUniqueId());
        return selectedNpc == null ? null : registry.getByName(selectedNpc);
    }

    public ManagedNpc getNpcByUniqueId(UUID uniqueId) {
        return registry.getByUniqueId(uniqueId);
    }

    public Collection<ManagedNpc> getAllNpcs() {
        return registry.getAll();
    }

    public boolean moveSelectedNpc(Player player, Location newLocation) {
        ManagedNpc selectedNpc = getSelectedNpc(player);
        if (selectedNpc == null) {
            return false;
        }

        StoredNpc updatedNpc = selectedNpc.getStoredNpc()
                .withLocation(LocationNormalizer.normalizeSpawnLocation(newLocation));
        updateSelectedNpc(selectedNpc, updatedNpc, true);
        return true;
    }

    public boolean addCommand(Player player, String command) {
        ManagedNpc selectedNpc = getSelectedNpc(player);
        if (selectedNpc == null) {
            return false;
        }

        StoredNpc updatedNpc = selectedNpc.getStoredNpc().addCommand(command);
        updateSelectedNpc(selectedNpc, updatedNpc, false);
        return true;
    }

    public List<String> getCommands(Player player) {
        ManagedNpc selectedNpc = getSelectedNpc(player);
        if (selectedNpc == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(selectedNpc.getStoredNpc().getCommands());
    }

    public boolean removeCommand(Player player, int index) {
        ManagedNpc selectedNpc = getSelectedNpc(player);
        if (selectedNpc == null) {
            return false;
        }

        List<String> commands = selectedNpc.getStoredNpc().getCommands();
        if (index < 0 || index >= commands.size()) {
            return false;
        }

        StoredNpc updatedNpc = selectedNpc.getStoredNpc().removeCommand(index);
        updateSelectedNpc(selectedNpc, updatedNpc, false);
        return true;
    }

    public int clearCommands(Player player) {
        ManagedNpc selectedNpc = getSelectedNpc(player);
        if (selectedNpc == null) {
            return 0;
        }

        List<String> commands = selectedNpc.getStoredNpc().getCommands();
        int removed = commands.size();
        if (removed == 0) {
            return 0;
        }

        StoredNpc updatedNpc = selectedNpc.getStoredNpc().clearCommands();
        updateSelectedNpc(selectedNpc, updatedNpc, false);
        return removed;
    }

    public void executeCommands(ManagedNpc managedNpc, Player player) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> executeCommands(managedNpc, player));
            return;
        }

        for (String rawCommand : managedNpc.getStoredNpc().getCommands()) {
            Bukkit.dispatchCommand(player, rawCommand.replace("{player}", player.getName()));
        }
    }

    public void showAllToPlayer(Player player) {
        registry.showAllToPlayer(player);
    }

    private void updateSelectedNpc(ManagedNpc managedNpc, StoredNpc updatedNpc, boolean respawn) {
        managedNpc.setStoredNpc(updatedNpc);
        if (respawn) {
            registry.respawn(managedNpc);
        }
        repository.save(updatedNpc);
    }

    private String generateNpcId() {
        String generatedId;
        do {
            generatedId = "npc-" + Long.toUnsignedString(ThreadLocalRandom.current().nextLong(), 36);
        } while (registry.containsName(generatedId));
        return generatedId;
    }

    private String resolveSkinPlayerName(List<String> displayLines, Player creator) {
        if (!displayLines.isEmpty()) {
            String firstLine = ChatColor.stripColor(CC.translate(displayLines.get(0)));
            if (firstLine != null) {
                String candidate = firstLine.trim();
                if (MINECRAFT_NAME_PATTERN.matcher(candidate).matches()) return candidate;
            }
        }
        return creator.getName();
    }

}
