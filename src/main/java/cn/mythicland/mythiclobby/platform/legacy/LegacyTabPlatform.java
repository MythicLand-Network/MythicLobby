package cn.mythicland.mythiclobby.platform.legacy;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.platform.tab.TabPlatform;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

@InjectComponent
public final class LegacyTabPlatform implements TabPlatform {
    private static final Field FOOTER_FIELD = resolveFooterField();

    @Override
    public void sendHeaderFooter(Player viewer, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(toComponent(header));
        setFooter(packet, toComponent(footer));
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void clearHeaderFooter(Player viewer) {
        sendHeaderFooter(viewer, "", "");
    }

    private static IChatBaseComponent toComponent(String text) {
        return new ChatComponentText(text == null ? "" : text);
    }

    private static void setFooter(PacketPlayOutPlayerListHeaderFooter packet, IChatBaseComponent footer) {
        try {
            FOOTER_FIELD.set(packet, footer);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Failed to set legacy tab footer", exception);
        }
    }

    private static Field resolveFooterField() {
        try {
            Field field = PacketPlayOutPlayerListHeaderFooter.class.getDeclaredField("b");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("Failed to resolve legacy tab footer field", exception);
        }
    }
}
