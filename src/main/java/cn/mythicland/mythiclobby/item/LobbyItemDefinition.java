package cn.mythicland.mythiclobby.item;

import cn.mythicland.mythiclobby.gui.model.MenuCommand;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public record LobbyItemDefinition(
        String id, int slot, Material material, short data, int amount, String name,
        List<String> lore, List<LobbyItemEnchantment> enchantments, boolean hideAllFlags,
        List<MenuCommand> commands
) {
    public LobbyItemDefinition(
            String id,
            int slot,
            Material material,
            short data,
            int amount,
            String name,
            List<String> lore,
            List<LobbyItemEnchantment> enchantments,
            boolean hideAllFlags,
            List<MenuCommand> commands
    ) {
        this.id = id;
        this.slot = slot;
        this.material = material;
        this.data = data;
        this.amount = amount;
        this.name = name;
        this.lore = new ArrayList<>(lore);
        this.enchantments = new ArrayList<>(enchantments);
        this.hideAllFlags = hideAllFlags;
        this.commands = new ArrayList<>(commands);
    }

    @Override
    public List<String> lore() {
        return new ArrayList<>(lore);
    }

    @Override
    public List<LobbyItemEnchantment> enchantments() {
        return new ArrayList<>(enchantments);
    }

    @Override
    public List<MenuCommand> commands() {
        return new ArrayList<>(commands);
    }
}
