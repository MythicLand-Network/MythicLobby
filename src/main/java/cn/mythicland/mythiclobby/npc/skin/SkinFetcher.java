package cn.mythicland.mythiclobby.npc.skin;

import cn.mythicland.mythiclobby.platform.skin.SkinData;

import java.io.IOException;

public interface SkinFetcher {
    SkinData fetchSkin(String playerName) throws IOException;
}
