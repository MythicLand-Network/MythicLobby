package cn.mythicland.mythiclobby.npc.skin;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.framework.component.ManagedLifecycle;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.platform.skin.SkinData;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@InjectComponent
public final class SkinService implements ManagedLifecycle {
    private final PluginLogger logger;
    private final SkinFetcher skinFetcher;
    private final AsyncExecutor asyncExecutor;
    private final Map<String, SkinData> skinCacheByPlayer = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<SkinData>> inFlightRequests = new ConcurrentHashMap<>();
    private final AtomicBoolean active = new AtomicBoolean(true);

    public SkinService(PluginLogger logger, SkinFetcher skinFetcher, AsyncExecutor asyncExecutor) {
        this.logger = logger;
        this.skinFetcher = skinFetcher;
        this.asyncExecutor = asyncExecutor;
    }

    public SkinData getCachedSkin(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) return null;
        return skinCacheByPlayer.get(playerName.toLowerCase(Locale.ROOT));
    }

    public CompletableFuture<SkinData> fetchSkin(String playerName) {
        if (!active.get()) return CompletableFuture.completedFuture(null);

        String cacheKey = playerName.toLowerCase(Locale.ROOT);
        SkinData cachedSkin = skinCacheByPlayer.get(cacheKey);
        if (cachedSkin != null) return CompletableFuture.completedFuture(cachedSkin);

        return inFlightRequests.computeIfAbsent(cacheKey, ignored -> {
            CompletableFuture<SkinData> future = new CompletableFuture<>();
            asyncExecutor.execute(() -> loadSkin(playerName, cacheKey, future));
            return future;
        });
    }

    public void cacheSkin(String playerName, SkinData skin) {
        skinCacheByPlayer.put(playerName.toLowerCase(Locale.ROOT), skin);
    }

    public boolean isActive() {
        return active.get();
    }

    @Override
    public void unload() {
        if (!active.compareAndSet(true, false)) return;
        inFlightRequests.values().forEach(future -> future.complete(null));
        inFlightRequests.clear();
        skinCacheByPlayer.clear();
    }

    private void loadSkin(String playerName, String cacheKey, CompletableFuture<SkinData> future) {
        try {
            SkinData skin = skinFetcher.fetchSkin(playerName);
            if (skin != null && active.get()) {
                skinCacheByPlayer.put(cacheKey, skin);
            }
            future.complete(active.get() ? skin : null);
        } catch (IOException exception) {
            logger.warning(String.format(
                    Locale.ROOT,
                    "Failed to fetch Mojang skin for player %s: %s",
                    playerName,
                    exception.getMessage()
            ));
            future.complete(null);
        } finally {
            inFlightRequests.remove(cacheKey, future);
        }
    }
}
