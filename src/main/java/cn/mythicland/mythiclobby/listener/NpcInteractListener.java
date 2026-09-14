package cn.mythicland.mythiclobby.listener;

import cn.mythicland.framework.bukkit.ListenerComponent;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.npc.ManagedNpc;
import cn.mythicland.mythiclobby.npc.NpcManager;
import cn.mythicland.npclib.api.events.NPCInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@InjectComponent
@ListenerComponent
public final class NpcInteractListener implements Listener {
    private final NpcManager npcManager;

    public NpcInteractListener(NpcManager npcManager) {
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onNpcInteract(NPCInteractEvent event) {
        ManagedNpc managedNpc = npcManager.getNpcByUniqueId(event.getNPC().getUniqueId());
        if (managedNpc == null) return;
        npcManager.executeCommands(managedNpc, event.getWhoClicked());
    }
}
