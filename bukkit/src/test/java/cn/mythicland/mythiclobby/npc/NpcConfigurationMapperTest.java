package cn.mythicland.mythiclobby.npc;

import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NpcConfigurationMapperTest {
    @Test
    void readsAndWritesConfiguredSkinPlayerName() {
        PluginLogger logger = mock(PluginLogger.class);
        ConfigurationSection npcsSection = mock(ConfigurationSection.class);
        ConfigurationSection npcSection = mock(ConfigurationSection.class);
        ConfigurationSection locationSection = mock(ConfigurationSection.class);
        World world = mock(World.class);

        when(npcsSection.getKeys(false)).thenReturn(Set.of("test-npc"));
        when(npcsSection.getConfigurationSection("test-npc")).thenReturn(npcSection);
        when(npcSection.getString("name", "test-npc")).thenReturn("test-npc");
        when(npcSection.getConfigurationSection("location")).thenReturn(locationSection);
        when(npcSection.getStringList("display")).thenReturn(List.of("Test NPC"));
        when(npcSection.getString("skin")).thenReturn(" ShanguanLinG ");
        when(npcSection.getStringList("commands")).thenReturn(List.of());
        when(locationSection.contains("world")).thenReturn(true);
        when(locationSection.contains("x")).thenReturn(true);
        when(locationSection.contains("y")).thenReturn(true);
        when(locationSection.contains("z")).thenReturn(true);
        when(locationSection.getString("world")).thenReturn("world");
        when(world.getName()).thenReturn("world");

        NpcConfigurationMapper mapper = new NpcConfigurationMapper(logger);
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
            StoredNpc storedNpc = mapper.readAll(npcsSection).get(0);

            assertEquals("ShanguanLinG", storedNpc.getSkinPlayerName());
            mapper.write(npcsSection, storedNpc);
            verify(npcsSection).set("test-npc.skin", "ShanguanLinG");
        }
    }
}
