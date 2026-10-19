package cn.mythicland.mythiclobby.gui.menu;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.gui.button.MenuButton;
import cn.mythicland.mythiclobby.gui.model.MenuItemEnchantment;
import cn.mythicland.mythiclobby.placeholder.PlaceholderService;
import cn.mythicland.framework.bukkit.message.CC;
import cn.mythicland.framework.bukkit.item.MaterialCompat;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@InjectComponent
public final class MenuRenderer {
    private final PlaceholderService formatter;

    public MenuRenderer(PlaceholderService formatter) {
        this.formatter = formatter;
    }

    public String renderTitle(Player player, String title) {
        return renderText(player, title);
    }

    public ItemStack renderItem(Player player, MenuButton button) {
        ItemStack itemStack = MaterialCompat.createItemStack(button.getMaterial(), button.getAmount(), button.getData());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (button.getName() != null) {
                itemMeta.setDisplayName(renderText(player, button.getName()));
            }
            if (!button.getLore().isEmpty()) {
                itemMeta.setLore(renderLines(player, button.getLore()));
            }
            if (button.isHideNbt()) {
                itemMeta.addItemFlags(ItemFlag.values());
            }
            itemStack.setItemMeta(itemMeta);
        }

        for (MenuItemEnchantment enchantment : button.getEnchantments()) {
            itemStack.addUnsafeEnchantment(enchantment.enchantment(), enchantment.level());
        }
        if (button.isGlow() && button.getEnchantments().isEmpty()) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }
        return itemStack;
    }

    public ItemStack createGlassPane(short color) {
        ItemStack pane = MaterialCompat.createGlassPane(color);
        ItemMeta itemMeta = pane.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            pane.setItemMeta(itemMeta);
        }
        return pane;
    }

    public String renderCommand(Player player, String command) {
        return formatter.render(player, command == null ? "" : command);
    }

    public String renderMessage(Player player, String message) {
        return renderText(player, message);
    }

    private List<String> renderLines(Player player, List<String> lines) {
        List<String> renderedLines = new ArrayList<>(lines.size());
        for (String line : lines) {
            renderedLines.add(renderText(player, line));
        }
        return renderedLines;
    }

    private String renderText(Player player, String text) {
        return CC.translate(formatter.render(player, text == null ? "" : text));
    }
}
