package cn.mythicland.mythiclobby.platform.legacy;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.component.ManagedLifecycle;
import cn.mythicland.mythiclobby.platform.npc.NpcPlatform;
import cn.mythicland.mythiclobby.platform.skin.SkinData;
import cn.mythicland.npclib.NPCLib;
import cn.mythicland.npclib.api.NPC;
import cn.mythicland.npclib.api.skin.Skin;
import com.mojang.authlib.properties.Property;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

@InjectComponent
public final class LegacyNpcPlatform implements NpcPlatform, ManagedLifecycle {
    private final NPCLib npcLib;

    public LegacyNpcPlatform(JavaPlugin plugin) {
        this.npcLib = new NPCLib(plugin);
    }

    @Override
    public Object spawn(Location location, List<String> displayLines, SkinData skin) {
        NPC npc = npcLib.createNPC(displayLines);
        npc.setLocation(location);
        if (skin != null) {
            npc.setSkin(new Skin(skin.value(), skin.signature()));
        }
        npc.create();
        return npc;
    }

    @Override
    public void destroy(Object runtimeNpc) {
        asNpc(runtimeNpc).destroy();
    }

    @Override
    public UUID getUniqueId(Object runtimeNpc) {
        return asNpc(runtimeNpc).getUniqueId();
    }

    @Override
    public Location getLocation(Object runtimeNpc) {
        return asNpc(runtimeNpc).getLocation();
    }

    @Override
    public boolean isShown(Object runtimeNpc, Player player) {
        return asNpc(runtimeNpc).isShown(player);
    }

    @Override
    public void show(Object runtimeNpc, Player player) {
        asNpc(runtimeNpc).show(player);
    }

    @Override
    public SkinData getSkin(Player player) {
        Property texture = ((CraftPlayer) player).getProfile()
                .getProperties()
                .get("textures")
                .stream()
                .findFirst()
                .orElse(null);
        if (texture == null) return null;
        return new SkinData(texture.getValue(), texture.getSignature());
    }

    @Override
    public void updateSkin(Object runtimeNpc, SkinData skin) {
        asNpc(runtimeNpc).updateSkin(new Skin(skin.value(), skin.signature()));
    }

    @Override
    public void unload() {
        this.npcLib.shutdown();
    }

    private NPC asNpc(Object runtimeNpc) {
        return (NPC) runtimeNpc;
    }
}
