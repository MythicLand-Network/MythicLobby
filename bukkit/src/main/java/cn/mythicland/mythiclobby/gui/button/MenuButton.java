package cn.mythicland.mythiclobby.gui.button;

import cn.mythicland.framework.menu.ClickType;
import cn.mythicland.framework.menu.MenuManager;
import cn.mythicland.mythiclobby.gui.menu.MenuRenderer;
import cn.mythicland.mythiclobby.gui.model.MenuCommand;
import cn.mythicland.mythiclobby.gui.model.MenuItemEnchantment;
import cn.mythicland.mythiclobby.gui.model.MenuSound;
import cn.mythicland.mythiclobby.gui.menu.LobbyMenuCommandExecutor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MenuButton extends cn.mythicland.framework.menu.AbstractButton {
    private final int slot;
    private final String id;
    private final boolean keepOpen;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int amount;
    private final short data;
    private final boolean glow;
    private final List<MenuItemEnchantment> enchantments;
    private final boolean hideNbt;
    private final List<MenuCommand> commands;
    private final List<MenuCommand> leftClickCommands;
    private final List<MenuCommand> rightClickCommands;
    private final MenuSound clickSound;
    private final LobbyMenuCommandExecutor commandExecutor;

    private MenuButton(Builder builder) {
        this.slot = builder.requireSlot();
        this.id = builder.resolveId();
        this.keepOpen = builder.keepOpen;
        this.material = builder.requireMaterial();
        this.name = builder.resolveName();
        this.lore = builder.copyLines();
        this.amount = builder.resolveAmount();
        this.data = builder.resolveData();
        this.glow = builder.glow;
        this.enchantments = builder.copyEnchantments();
        this.hideNbt = builder.hideNbt;
        this.commands = builder.copyCommands(builder.commands);
        this.leftClickCommands = builder.copyCommands(builder.leftClickCommands);
        this.rightClickCommands = builder.copyCommands(builder.rightClickCommands);
        this.clickSound = builder.clickSound;
        this.commandExecutor = Objects.requireNonNull(builder.commandExecutor, "commandExecutor");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(int slot, Material material) {
        return builder().slot(slot).material(material);
    }

    public String getId() {
        return id;
    }

    public int getSlot() {
        return this.slot;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    public int getAmount() {
        return amount;
    }

    public short getData() {
        return data;
    }

    public boolean isGlow() {
        return glow;
    }

    public List<MenuItemEnchantment> getEnchantments() {
        return new ArrayList<>(enchantments);
    }

    public boolean isHideNbt() {
        return hideNbt;
    }

    public List<MenuCommand> getCommands() {
        return new ArrayList<>(commands);
    }

    public List<MenuCommand> getLeftClickCommands() {
        return new ArrayList<>(leftClickCommands);
    }

    public List<MenuCommand> getRightClickCommands() {
        return new ArrayList<>(rightClickCommands);
    }

    @Override
    public ItemStack buildDisplayItem(Player player) {
        return this.commandExecutor.renderer().renderItem(player, this);
    }

    @Override
    public void onClick(Player player, ClickType clickType, MenuManager menuManager) {
        if (this.clickSound != null) this.clickSound.play(player);
        this.commandExecutor.execute(player, this.getCommands(clickType), menuManager);
    }

    @Override
    public boolean shouldClose(Player player, ClickType clickType) {
        return !this.keepOpen;
    }

    public List<MenuCommand> getCommands(ClickType clickType) {
        if (clickType.isLeft() && !leftClickCommands.isEmpty()) {
            return getLeftClickCommands();
        }
        if (clickType.isRight() && !rightClickCommands.isEmpty()) {
            return getRightClickCommands();
        }
        return getCommands();
    }

    public static final class Builder {
        private Integer slot;
        private Material material;
        private String id;
        private String name;
        private List<String> lore = Collections.emptyList();
        private Integer amount;
        private Short data;
        private boolean keepOpen;
        private boolean glow;
        private List<MenuItemEnchantment> enchantments = Collections.emptyList();
        private boolean hideNbt;
        private List<MenuCommand> commands = Collections.emptyList();
        private List<MenuCommand> leftClickCommands = Collections.emptyList();
        private List<MenuCommand> rightClickCommands = Collections.emptyList();
        private MenuSound clickSound;
        private LobbyMenuCommandExecutor commandExecutor;

        private Builder() {
        }

        public Builder slot(int slot) {
            this.slot = slot;
            return this;
        }

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder lore(Collection<String> lore) {
            this.lore = lore == null ? Collections.emptyList() : new ArrayList<>(lore);
            return this;
        }

        public Builder addLore(String line) {
            if (lore.isEmpty()) {
                lore = new ArrayList<>();
            }
            lore.add(line == null ? "" : line);
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder data(short data) {
            this.data = data;
            return this;
        }

        public Builder keepOpen(boolean keepOpen) {
            this.keepOpen = keepOpen;
            return this;
        }

        public Builder glow(boolean glow) {
            this.glow = glow;
            return this;
        }

        public Builder enchantments(Collection<MenuItemEnchantment> enchantments) {
            this.enchantments = enchantments == null ? Collections.emptyList() : new ArrayList<>(enchantments);
            return this;
        }

        public Builder addEnchantment(MenuItemEnchantment enchantment) {
            if (enchantment == null) {
                return this;
            }
            if (enchantments.isEmpty()) {
                enchantments = new ArrayList<>();
            }
            enchantments.add(enchantment);
            return this;
        }

        public Builder hideNbt(boolean hideNbt) {
            this.hideNbt = hideNbt;
            return this;
        }

        public Builder commands(Collection<MenuCommand> commands) {
            this.commands = commands == null ? Collections.emptyList() : new ArrayList<>(commands);
            return this;
        }

        public Builder addCommand(MenuCommand command) {
            if (command == null) {
                return this;
            }
            if (commands.isEmpty()) {
                commands = new ArrayList<>();
            }
            commands.add(command);
            return this;
        }

        public Builder leftClick(Collection<MenuCommand> commands) {
            this.leftClickCommands = commands == null ? Collections.emptyList() : new ArrayList<>(commands);
            return this;
        }

        public Builder addLeftClick(MenuCommand command) {
            if (command == null) {
                return this;
            }
            if (leftClickCommands.isEmpty()) {
                leftClickCommands = new ArrayList<>();
            }
            leftClickCommands.add(command);
            return this;
        }

        public Builder rightClick(Collection<MenuCommand> commands) {
            this.rightClickCommands = commands == null ? Collections.emptyList() : new ArrayList<>(commands);
            return this;
        }

        public Builder addRightClick(MenuCommand command) {
            if (command == null) {
                return this;
            }
            if (rightClickCommands.isEmpty()) {
                rightClickCommands = new ArrayList<>();
            }
            rightClickCommands.add(command);
            return this;
        }

        public Builder clickSound(MenuSound clickSound) {
            this.clickSound = clickSound;
            return this;
        }

        public Builder commandExecutor(LobbyMenuCommandExecutor commandExecutor) {
            this.commandExecutor = commandExecutor;
            return this;
        }

        public MenuButton build() {
            return new MenuButton(this);
        }

        private int requireSlot() {
            if (slot == null || slot < 0) {
                throw new IllegalStateException("MenuButton slot must be >= 0.");
            }
            return slot;
        }

        private Material requireMaterial() {
            if (material == null) {
                throw new IllegalStateException("MenuButton material cannot be null.");
            }
            return material;
        }

        private String resolveId() {
            String normalizedId = trimToNull(id);
            return normalizedId == null ? "button-" + requireSlot() : normalizedId;
        }

        private String resolveName() {
            return hasText(name) ? name : null;
        }

        private int resolveAmount() {
            if (amount == null || amount < 1 || amount > requireMaterial().getMaxStackSize()) {
                return 1;
            }
            return amount;
        }

        private short resolveData() {
            return data == null ? 0 : data;
        }

        private List<String> copyLines() {
            List<String> resolvedLore = new ArrayList<>();
            for (String line : lore) {
                resolvedLore.add(line == null ? "" : line);
            }
            return resolvedLore;
        }

        private List<MenuItemEnchantment> copyEnchantments() {
            List<MenuItemEnchantment> resolvedEnchantments = new ArrayList<>();
            for (MenuItemEnchantment enchantment : enchantments) {
                if (enchantment != null) {
                    resolvedEnchantments.add(enchantment);
                }
            }
            return resolvedEnchantments;
        }

        private List<MenuCommand> copyCommands(Collection<MenuCommand> commands) {
            List<MenuCommand> resolvedCommands = new ArrayList<>();
            for (MenuCommand command : commands) {
                if (command != null) {
                    resolvedCommands.add(command);
                }
            }
            return resolvedCommands;
        }

        private String trimToNull(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }

        private boolean hasText(String value) {
            return value != null && !value.trim().isEmpty();
        }
    }
}
