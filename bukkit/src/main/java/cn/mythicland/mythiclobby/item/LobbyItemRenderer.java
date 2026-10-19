package cn.mythicland.mythiclobby.item;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.framework.bukkit.message.CC;
import cn.mythicland.framework.bukkit.item.MaterialCompat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@InjectComponent
public final class LobbyItemRenderer {
    private final PlaceholderService formatter;

    public LobbyItemRenderer(PlaceholderService formatter) {
        this.formatter = formatter;
    }

    public ItemStack renderItem(Player player, LobbyItemDefinition definition) {
        ItemStack itemStack = MaterialCompat.createItemStack(definition.material(), definition.amount(), definition.data());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(renderText(player, definition.name()));
            itemMeta.setLore(renderLore(player, definition.lore()));
            if (definition.hideAllFlags()) {
                itemMeta.addItemFlags(ItemFlag.values());
            }
            itemStack.setItemMeta(itemMeta);
        }

        for (LobbyItemEnchantment enchantment : definition.enchantments()) {
            itemStack.addUnsafeEnchantment(enchantment.enchantment(), enchantment.level());
        }
        return itemStack;
    }

    public String renderCommand(Player player, String command) {
        return formatter.render(player, command == null ? "" : command);
    }

    public String renderMessage(Player player, String message) {
        return renderText(player, message);
    }

    private List<String> renderLore(Player player, List<String> lore) {
        List<String> renderedLore = new ArrayList<>(lore.size());
        for (String line : lore) {
            renderedLore.add(renderText(player, line));
        }
        return renderedLore;
    }

    private String renderText(Player player, String text) {
        return CC.translate(formatter.render(player, text == null ? "" : text));
    }
}
