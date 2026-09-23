package cn.mythicland.mythiclobby.npc.skin;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.npclib.api.skin.MojangSkinService;
import cn.mythicland.npclib.api.skin.Skin;
import cn.mythicland.mythiclobby.platform.skin.SkinData;

import java.io.IOException;
import java.util.Optional;

@InjectComponent
public final class MojangSkinFetcher implements SkinFetcher {
    private final MojangSkinService skinService;

    public MojangSkinFetcher(MojangSkinService skinService) {
        this.skinService = skinService;
    }

    @Override
    public SkinData fetchSkin(String playerName) throws IOException {
        Optional<Skin> skin = this.skinService.fetchSkinByPlayerName(playerName);
        return skin.map(value -> new SkinData(value.value(), value.signature())).orElse(null);
    }
}
