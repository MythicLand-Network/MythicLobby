package cn.mythicland.mythiclobby.item;

import cn.mythicland.framework.bukkit.item.MaterialNames;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.config.ItemConfig;
import cn.mythicland.mythiclobby.gui.model.MenuCommand;
import cn.mythicland.mythiclobby.gui.menu.MenuCommandExecutor;
import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
@InjectComponent
public final class LobbyItemRepository {
    private static final String PLAYER_PREFIX = "[player]";
    private static final String CONSOLE_PREFIX = "[console]";
    private static final String CONNECT_PREFIX = "[connect]";
    private static final String MESSAGE_PREFIX = "[message]";

    private final ItemConfig itemConfig;
    private final PluginLogger logger;

    public LobbyItemRepository(ItemConfig itemConfig, PluginLogger logger) {
        this.itemConfig = itemConfig;
        this.logger = logger;
    }

    public void reload() {
        // ConfigManager keeps YAML state fresh before repositories rebuild runtime data.
    }

    public List<LobbyItemDefinition> loadAll() {
        YamlConfiguration yaml = itemConfig.data();
        ConfigurationSection itemsSection = yaml.getConfigurationSection("items");
        if (itemsSection == null) {
            return Collections.emptyList();
        }

        List<LobbyItemDefinition> items = new ArrayList<>();
        Map<Integer, String> slots = new LinkedHashMap<>();
        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
            if (itemSection == null) {
                warn(itemId, "item node is missing");
                continue;
            }

            LobbyItemDefinition definition = readItem(itemId, itemSection);
            if (definition == null) {
                continue;
            }
            if (slots.containsKey(definition.slot())) {
                warn(itemId, "uses duplicate slot " + definition.slot() + " already occupied by '" + slots.get(definition.slot()) + "'");
                continue;
            }
            slots.put(definition.slot(), itemId);
            items.add(definition);
        }
        return items;
    }

    private LobbyItemDefinition readItem(String itemId, ConfigurationSection section) {
        int slot = section.getInt("slot", -1);
        if (slot < 0 || slot > 35) {
            warn(itemId, "slot must be between 0 and 35");
            return null;
        }

        Material material = parseMaterial(itemId, section.getString("material"));
        if (material == null) {
            return null;
        }

        int data = section.getInt("data", 0);
        if (data < Short.MIN_VALUE || data > Short.MAX_VALUE) {
            warn(itemId, "data value is out of range");
            return null;
        }

        int amount = section.getInt("amount", 1);
        if (amount < 1 || amount > material.getMaxStackSize()) {
            warn(itemId, "amount must be between 1 and " + material.getMaxStackSize());
            return null;
        }

        String name = section.getString("name");
        if (name == null) {
            warn(itemId, "name is missing");
            return null;
        }

        List<String> lore = readLore(itemId, section);
        List<LobbyItemEnchantment> enchantments = readEnchantments(itemId, section);
        boolean hideAllFlags = section.getBoolean("hide-all-flags", false);
        List<MenuCommand> commands = readCommands(itemId, section);

        return new LobbyItemDefinition(
                itemId,
                slot,
                material,
                (short) data,
                amount,
                name,
                lore,
                enchantments,
                hideAllFlags,
                commands
        );
    }

    private Material parseMaterial(String itemId, String rawMaterial) {
        String materialName = trimToEmpty(rawMaterial);
        if (materialName.isEmpty()) {
            warn(itemId, "material is missing");
            return null;
        }

        Material material = MaterialNames.match(materialName);
        if (material == null) {
            warn(itemId, "uses invalid material '" + materialName + "'");
        }
        return material;
    }

    private List<String> readLore(String itemId, ConfigurationSection section) {
        if (section.contains("lore") && !section.isList("lore")) {
            warn(itemId, "lore must be a list");
            return Collections.emptyList();
        }

        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(line == null ? "" : line);
        }
        return lore;
    }

    private List<LobbyItemEnchantment> readEnchantments(String itemId, ConfigurationSection section) {
        if (section.contains("enchantments") && !section.isList("enchantments")) {
            warn(itemId, "enchantments must be a list");
            return Collections.emptyList();
        }

        List<LobbyItemEnchantment> enchantments = new ArrayList<>();
        for (String rawEntry : section.getStringList("enchantments")) {
            LobbyItemEnchantment enchantment = parseEnchantment(itemId, rawEntry);
            if (enchantment != null) {
                enchantments.add(enchantment);
            }
        }
        return enchantments;
    }

    private LobbyItemEnchantment parseEnchantment(String itemId, String rawEntry) {
        String entry = trimToEmpty(rawEntry);
        if (entry.isEmpty()) {
            return null;
        }

        String[] parts = entry.split(":", 2);
        if (parts.length != 2) {
            warn(itemId, "enchantment '" + entry + "' must use format NAME:LEVEL");
            return null;
        }

        Enchantment enchantment = Enchantment.getByName(parts[0].trim().toUpperCase(Locale.ROOT));
        if (enchantment == null) {
            warn(itemId, "uses invalid enchantment '" + parts[0].trim() + "'");
            return null;
        }

        int level;
        try {
            level = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException exception) {
            warn(itemId, "enchantment '" + entry + "' has invalid level");
            return null;
        }

        if (level <= 0) {
            warn(itemId, "enchantment '" + entry + "' level must be > 0");
            return null;
        }
        return new LobbyItemEnchantment(enchantment, level);
    }

    private List<MenuCommand> readCommands(String itemId, ConfigurationSection section) {
        if (section.contains("commands") && !section.isList("commands")) {
            warn(itemId, "commands must be a list");
            return Collections.emptyList();
        }

        List<MenuCommand> commands = new ArrayList<>();
        for (String rawCommand : section.getStringList("commands")) {
            MenuCommand commandDefinition = parseCommand(itemId, rawCommand);
            if (commandDefinition != null) {
                commands.add(commandDefinition);
            }
        }
        return commands;
    }

    private MenuCommand parseCommand(String itemId, String rawCommand) {
        String command = trimToEmpty(rawCommand);
        if (command.isEmpty()) {
            return null;
        }

        String lowerCase = command.toLowerCase(Locale.ROOT);
        if (lowerCase.startsWith(PLAYER_PREFIX)) {
            return createCommand(itemId, command.substring(PLAYER_PREFIX.length()).trim(), MenuCommandExecutor.PLAYER);
        }
        if (lowerCase.startsWith(CONSOLE_PREFIX)) {
            return createCommand(itemId, command.substring(CONSOLE_PREFIX.length()).trim(), MenuCommandExecutor.CONSOLE);
        }
        if (lowerCase.startsWith(CONNECT_PREFIX)) {
            return createCommand(itemId, command.substring(CONNECT_PREFIX.length()).trim(), MenuCommandExecutor.CONNECT);
        }
        if (lowerCase.startsWith(MESSAGE_PREFIX)) {
            return createCommand(itemId, command.substring(MESSAGE_PREFIX.length()).trim(), MenuCommandExecutor.MESSAGE);
        }

        warn(itemId, "has command without prefix: " + command);
        return null;
    }

    private MenuCommand createCommand(String itemId, String command, MenuCommandExecutor executor) {
        if (command.isEmpty()) {
            warn(itemId, "has an empty " + executor.name().toLowerCase(Locale.ROOT) + " command");
            return null;
        }
        return new MenuCommand(executor, command);
    }

    private void warn(String itemId, String detail) {
        logger.warning(String.format(Locale.ROOT, "items.yml: item '%s' %s", itemId, detail));
    }

    private String trimToEmpty(String input) {
        return input == null ? "" : input.trim();
    }
}
