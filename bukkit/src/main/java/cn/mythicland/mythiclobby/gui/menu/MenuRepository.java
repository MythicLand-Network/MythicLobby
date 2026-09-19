package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.menu.ClickType;
import cn.mythicland.mythiclobby.gui.button.MenuButton;
import cn.mythicland.mythiclobby.config.MenuConfig;
import cn.mythicland.mythiclobby.gui.model.MenuClickAction;
import cn.mythicland.mythiclobby.gui.model.MenuCommand;
import cn.mythicland.mythiclobby.gui.model.MenuItemEnchantment;
import cn.mythicland.mythiclobby.gui.model.MenuSound;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.util.MaterialCompat;
import cn.mythicland.mythiclobby.util.SoundCompat;
import org.bukkit.Material;
import org.bukkit.Sound;
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
public final class MenuRepository {
    private static final int DEFAULT_SIZE = 9;
    private static final short DEFAULT_GLASS_COLOR = 15;
    private static final short DEFAULT_ITEM_DATA = 0;
    private static final String PLAYER_PREFIX = "[player]";
    private static final String CONSOLE_PREFIX = "[console]";
    private static final String CONNECT_PREFIX = "[connect]";
    private static final String MESSAGE_PREFIX = "[message]";
    private static final String CLOSE_PREFIX = "[close]";

    private final MenuConfig menuConfig;
    private final PluginLogger logger;
    private final LobbyMenuCommandExecutor commandExecutor;

    public MenuRepository(
            MenuConfig menuConfig,
            PluginLogger logger,
            LobbyMenuCommandExecutor commandExecutor
    ) {
        this.menuConfig = menuConfig;
        this.logger = logger;
        this.commandExecutor = commandExecutor;
    }

    public void reload() {
        // ConfigManager keeps YAML state fresh before repositories rebuild runtime data.
    }

    public List<ConfigMenu> loadAll() {
        YamlConfiguration yaml = menuConfig.getYamlConfiguration();
        List<ConfigMenu> menus = new ArrayList<>();
        for (String menuId : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(menuId);
            ConfigMenu menu = readMenu(menuId, section);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    private ConfigMenu readMenu(String menuId, ConfigurationSection section) {
        if (section == null) {
            warn(menuId, "menu node is missing");
            return null;
        }
        if (section.contains("tittle")) {
            warn(menuId, "field 'tittle' is unsupported, use 'title' instead");
        }

        String resolvedMenuId = trimToEmpty(section.getString("id"));
        if (resolvedMenuId.isEmpty()) {
            resolvedMenuId = menuId;
        }

        String title = section.getString("title");
        if (title == null) {
            warn(menuId, "title is missing");
            return null;
        }

        int size = readMenuSize(menuId, section);
        if (size < 0) {
            return null;
        }

        String permission = trimToEmpty(section.getString("permission"));
        MenuSound openSound = parseSound(menuId, section, "open-sound");
        MenuSound clickSound = parseSound(menuId, section, "click-sound");
        boolean glassEnabled = section.getBoolean("glass-enable", false);
        short glassColor = parseGlassColor(menuId, section.getInt("glass-color", DEFAULT_GLASS_COLOR));
        Map<Integer, MenuButton> items = readItems(
                menuId,
                section.getConfigurationSection("items"),
                size,
                clickSound
        );

        return new ConfigMenu(
                resolvedMenuId,
                title,
                size,
                permission,
                openSound,
                clickSound,
                glassEnabled,
                glassColor,
                items,
                commandExecutor.renderer()
        );
    }

    private Map<Integer, MenuButton> readItems(
            String menuId,
            ConfigurationSection itemsSection,
            int inventorySize,
            MenuSound clickSound
    ) {
        Map<Integer, MenuButton> itemsBySlot = new LinkedHashMap<>();
        if (itemsSection == null) {
            return itemsBySlot;
        }

        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
            if (itemSection == null) {
                warn(menuId, "item '" + itemId + "' is missing");
                continue;
            }

            MenuButton button = readItem(menuId, itemId, itemSection, inventorySize, clickSound);
            if (button == null) {
                continue;
            }
            if (itemsBySlot.containsKey(button.getSlot())) {
                warn(menuId, "item '" + itemId + "' uses duplicate slot " + button.getSlot());
                continue;
            }
            itemsBySlot.put(button.getSlot(), button);
        }
        return itemsBySlot;
    }

    private MenuButton readItem(
            String menuId,
            String itemId,
            ConfigurationSection itemSection,
            int inventorySize,
            MenuSound clickSound
    ) {
        Material material = parseMaterial(menuId, itemId, itemSection.getString("material"));
        if (material == null) {
            return null;
        }

        int slot = itemSection.getInt("slot", -1);
        if (slot < 0 || slot >= inventorySize) {
            warn(menuId, "item '" + itemId + "' has invalid slot " + slot);
            return null;
        }

        List<String> lore = readLore(menuId, itemId, itemSection);
        Short data = parseItemData(menuId, itemId, itemSection);
        Integer amount = parseAmount(menuId, itemId, itemSection, material);
        boolean keepOpen = itemSection.getBoolean("keep-open", false);
        boolean glow = itemSection.getBoolean("glow", false);
        List<MenuItemEnchantment> enchantments = readEnchantments(menuId, itemId, itemSection);
        boolean hideNbt = itemSection.getBoolean("hide-nbt", itemSection.getBoolean("hide-all-flags", false));
        List<MenuCommand> commands = readCommands(menuId, itemId, itemSection);
        List<MenuClickAction> clickActions = readClickActions(menuId, itemId, itemSection);

        MenuButton.Builder builder = MenuButton.builder(slot, material)
                .id(itemId)
                .name(itemSection.getString("name"))
                .lore(lore)
                .keepOpen(keepOpen)
                .glow(glow)
                .enchantments(enchantments)
                .hideNbt(hideNbt)
                .commands(commands)
                .clickSound(clickSound)
                .commandExecutor(commandExecutor);
        if (amount != null) {
            builder.amount(amount);
        }
        if (data != null) {
            builder.data(data);
        }

        for (MenuClickAction clickAction : clickActions) {
            if (clickAction.clickType() == ClickType.LEFT) {
                builder.leftClick(clickAction.commands());
                continue;
            }
            if (clickAction.clickType() == ClickType.RIGHT) {
                builder.rightClick(clickAction.commands());
            }
        }
        return builder.build();
    }

    private int readMenuSize(String menuId, ConfigurationSection section) {
        if (section.contains("size")) {
            int size = section.getInt("size", -1);
            if (size < 9 || size > 54 || size % 9 != 0) {
                warn(menuId, "size must be a multiple of 9 between 9 and 54");
                return -1;
            }
            return size;
        }

        int rows = section.getInt("rows", DEFAULT_SIZE / 9);
        if (rows < 1 || rows > 6) {
            warn(menuId, "rows must be between 1 and 6, falling back to 1");
            rows = DEFAULT_SIZE / 9;
        }
        return rows * 9;
    }

    private List<String> readLore(String menuId, String itemId, ConfigurationSection itemSection) {
        if (itemSection.contains("lore") && !itemSection.isList("lore")) {
            warn(menuId, "item '" + itemId + "' lore must be a list");
            return Collections.emptyList();
        }

        List<String> lore = new ArrayList<>();
        for (String line : itemSection.getStringList("lore")) {
            lore.add(line == null ? "" : line);
        }
        return lore;
    }

    private Integer parseAmount(String menuId, String itemId, ConfigurationSection itemSection, Material material) {
        if (!itemSection.contains("amount")) {
            return null;
        }

        int amount = itemSection.getInt("amount", 1);
        if (amount < 1 || amount > material.getMaxStackSize()) {
            warn(menuId, "item '" + itemId + "' amount must be between 1 and " + material.getMaxStackSize());
            return null;
        }
        return amount;
    }

    private List<MenuItemEnchantment> readEnchantments(String menuId, String itemId, ConfigurationSection itemSection) {
        String path = itemSection.contains("enchants") ? "enchants" : "enchantments";
        if (itemSection.contains(path) && !itemSection.isList(path)) {
            warn(menuId, "item '" + itemId + "' " + path + " must be a list");
            return Collections.emptyList();
        }

        List<MenuItemEnchantment> enchantments = new ArrayList<>();
        for (String rawEntry : itemSection.getStringList(path)) {
            MenuItemEnchantment enchantment = parseEnchantment(menuId, itemId, rawEntry);
            if (enchantment != null) {
                enchantments.add(enchantment);
            }
        }
        return enchantments;
    }

    private MenuItemEnchantment parseEnchantment(String menuId, String itemId, String rawEntry) {
        String entry = trimToEmpty(rawEntry);
        if (entry.isEmpty()) {
            return null;
        }

        String[] parts = entry.split(":", 2);
        if (parts.length != 2) {
            warn(menuId, "item '" + itemId + "' enchant '" + entry + "' must use format NAME:LEVEL");
            return null;
        }

        Enchantment enchantment = Enchantment.getByName(parts[0].trim().toUpperCase(Locale.ROOT));
        if (enchantment == null) {
            warn(menuId, "item '" + itemId + "' uses invalid enchantment '" + parts[0].trim() + "'");
            return null;
        }

        int level;
        try {
            level = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException exception) {
            warn(menuId, "item '" + itemId + "' enchant '" + entry + "' has invalid level");
            return null;
        }

        if (level <= 0) {
            warn(menuId, "item '" + itemId + "' enchant '" + entry + "' level must be > 0");
            return null;
        }
        return new MenuItemEnchantment(enchantment, level);
    }

    private List<MenuCommand> readCommands(String menuId, String itemId, ConfigurationSection itemSection) {
        if (itemSection.contains("commands") && !itemSection.isList("commands")) {
            warn(menuId, "item '" + itemId + "' commands must be a list");
            return Collections.emptyList();
        }

        List<MenuCommand> commands = new ArrayList<>();
        for (String rawCommand : itemSection.getStringList("commands")) {
            MenuCommand commandDefinition = parseCommand(menuId, itemId, rawCommand);
            if (commandDefinition != null) {
                commands.add(commandDefinition);
            }
        }
        return commands;
    }

    private List<MenuClickAction> readClickActions(String menuId, String itemId, ConfigurationSection itemSection) {
        List<MenuClickAction> clickActions = new ArrayList<>(2);
        addClickAction(clickActions, menuId, itemId, itemSection, "left-click", ClickType.LEFT);
        addClickAction(clickActions, menuId, itemId, itemSection, "right-click", ClickType.RIGHT);
        return clickActions;
    }

    private void addClickAction(
            List<MenuClickAction> clickActions,
            String menuId,
            String itemId,
            ConfigurationSection itemSection,
            String path,
            ClickType clickType
    ) {
        if (itemSection.contains(path) && !itemSection.isList(path)) {
            warn(menuId, "item '" + itemId + "' " + path + " must be a list");
            return;
        }

        List<MenuCommand> commands = new ArrayList<>();
        for (String rawCommand : itemSection.getStringList(path)) {
            MenuCommand commandDefinition = parseCommand(menuId, itemId, rawCommand);
            if (commandDefinition != null) {
                commands.add(commandDefinition);
            }
        }
        if (!commands.isEmpty()) {
            clickActions.add(new MenuClickAction(clickType, commands));
        }
    }

    private MenuCommand parseCommand(String menuId, String itemId, String rawCommand) {
        String command = trimToEmpty(rawCommand);
        if (command.isEmpty()) {
            return null;
        }

        String lowerCase = command.toLowerCase(Locale.ROOT);
        if (lowerCase.startsWith(PLAYER_PREFIX)) {
            return createCommand(menuId, itemId, command.substring(PLAYER_PREFIX.length()).trim(), MenuCommandExecutor.PLAYER);
        }
        if (lowerCase.startsWith(CONSOLE_PREFIX)) {
            return createCommand(menuId, itemId, command.substring(CONSOLE_PREFIX.length()).trim(), MenuCommandExecutor.CONSOLE);
        }
        if (lowerCase.startsWith(CONNECT_PREFIX)) {
            return createCommand(menuId, itemId, command.substring(CONNECT_PREFIX.length()).trim(), MenuCommandExecutor.CONNECT);
        }
        if (lowerCase.startsWith(MESSAGE_PREFIX)) {
            return createCommand(menuId, itemId, command.substring(MESSAGE_PREFIX.length()).trim(), MenuCommandExecutor.MESSAGE);
        }
        if (lowerCase.startsWith(CLOSE_PREFIX)) {
            return createCloseCommand(menuId, itemId, command.substring(CLOSE_PREFIX.length()).trim());
        }

        warn(menuId, "item '" + itemId + "' has command without prefix: " + command);
        return null;
    }

    private MenuCommand createCommand(String menuId, String itemId, String command, MenuCommandExecutor executor) {
        if (command.isEmpty()) {
            warn(menuId, "item '" + itemId + "' has an empty " + executor.name().toLowerCase(Locale.ROOT) + " command");
            return null;
        }
        return new MenuCommand(executor, command);
    }

    private MenuCommand createCloseCommand(String menuId, String itemId, String command) {
        if (!command.isEmpty()) {
            warn(menuId, "item '" + itemId + "' close command ignores extra content: " + command);
        }
        return new MenuCommand(MenuCommandExecutor.CLOSE, "");
    }

    private Material parseMaterial(String menuId, String itemId, String rawMaterial) {
        String materialName = trimToEmpty(rawMaterial);
        if (materialName.isEmpty()) {
            warn(menuId, "item '" + itemId + "' is missing material");
            return null;
        }

        Material material = MaterialCompat.matchMaterial(materialName);
        if (material == null) {
            warn(menuId, "item '" + itemId + "' uses invalid material '" + materialName + "'");
        }
        return material;
    }

    private MenuSound parseSound(String menuId, ConfigurationSection section, String path) {
        if (!section.contains(path)) {
            return null;
        }

        ConfigurationSection soundSection = section.getConfigurationSection(path);
        if (soundSection != null) {
            return parseSoundSection(menuId, path, soundSection);
        }
        return parseSoundValue(menuId, path, section.getString(path), 1.0F, 1.0F);
    }

    private MenuSound parseSoundSection(String menuId, String path, ConfigurationSection section) {
        String soundName = trimToEmpty(section.getString("sound", section.getString("name")));
        if (soundName.isEmpty() || soundName.equalsIgnoreCase("NONE")) {
            return null;
        }

        Sound sound = SoundCompat.matchSound(soundName);
        if (sound == null) {
            warn(menuId, path + " '" + soundName + "' is invalid, skipping sound");
            return null;
        }

        float volume = parseFloat(menuId, path + ".volume", section.get("volume"), 1.0F);
        float pitch = parseFloat(menuId, path + ".pitch", section.get("pitch"), 1.0F);
        return new MenuSound(sound, volume, pitch);
    }

    private MenuSound parseSoundValue(String menuId, String path, String rawSound, float volume, float pitch) {
        String soundName = trimToEmpty(rawSound);
        if (soundName.isEmpty() || soundName.equalsIgnoreCase("NONE")) {
            return null;
        }

        Sound sound = SoundCompat.matchSound(soundName);
        if (sound == null) {
            warn(menuId, path + " '" + soundName + "' is invalid, skipping sound");
            return null;
        }
        return new MenuSound(sound, volume, pitch);
    }

    private float parseFloat(String menuId, String path, Object rawValue, float defaultValue) {
        if (rawValue == null) {
            return defaultValue;
        }

        float value;
        if (rawValue instanceof Number number) {
            value = number.floatValue();
        } else {
            try {
                value = Float.parseFloat(rawValue.toString().trim());
            } catch (NumberFormatException exception) {
                warn(menuId, path + " must be a number, falling back to " + defaultValue);
                return defaultValue;
            }
        }

        if (value < 0.0F) {
            warn(menuId, path + " must be >= 0.0, falling back to " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    private Short parseItemData(String menuId, String itemId, ConfigurationSection itemSection) {
        if (!itemSection.contains("data")) {
            return null;
        }

        int data = itemSection.getInt("data", DEFAULT_ITEM_DATA);
        if (data < Short.MIN_VALUE || data > Short.MAX_VALUE) {
            warn(menuId, "item '" + itemId + "' data value is out of range, falling back to 0");
            return null;
        }
        return (short) data;
    }

    private short parseGlassColor(String menuId, int color) {
        if (color < 0 || color > 15) {
            warn(menuId, "glass-color must be between 0 and 15, falling back to 15");
            return DEFAULT_GLASS_COLOR;
        }
        return (short) color;
    }

    private void warn(String menuId, String detail) {
        logger.warning(String.format(Locale.ROOT, "menus.yml: menu '%s' %s", menuId, detail));
    }

    private String trimToEmpty(String input) {
        return input == null ? "" : input.trim();
    }
}
