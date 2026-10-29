package cn.mythicland.mythiclobby.platform.legacy;

import cn.mythicland.framework.bukkit.message.v1_8_R3.TabHeaderFooter;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.platform.tab.TabPlatform;
import org.bukkit.entity.Player;

@InjectComponent
public final class LegacyTabPlatform implements TabPlatform {
    @Override
    public void sendHeaderFooter(Player viewer, String header, String footer) {
        TabHeaderFooter.send(viewer, header, footer);
    }

    @Override
    public void clearHeaderFooter(Player viewer) {
        TabHeaderFooter.send(viewer, "", "");
    }
}
