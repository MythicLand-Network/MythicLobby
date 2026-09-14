package cn.mythicland.mythiclobby.npc.skin;

import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.platform.skin.SkinData;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class SkinServiceTest {
    @Test
    void cachesFetchedSkinWithoutCaseSensitivity() {
        AtomicInteger fetchCount = new AtomicInteger();
        SkinData skin = new SkinData("value", "signature");
        SkinFetcher fetcher = playerName -> {
            fetchCount.incrementAndGet();
            return skin;
        };
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            SkinService service = new SkinService(mock(PluginLogger.class), fetcher, executor::execute);

            assertSame(skin, service.fetchSkin("ShanguanLinG").join());
            assertSame(skin, service.fetchSkin("shanguanling").join());
            assertEquals(1, fetchCount.get());
        }
    }
}
