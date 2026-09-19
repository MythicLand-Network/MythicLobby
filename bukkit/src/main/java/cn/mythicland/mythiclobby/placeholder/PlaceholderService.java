package cn.mythicland.mythiclobby.placeholder;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.log.PluginLogger;
import cn.mythicland.mythiclobby.proxy.BungeeConnectService;
import org.bukkit.entity.Player;

import java.util.Map;

@InjectComponent
public final class PlaceholderService {
    private final BuiltinPlaceholderResolver builtinResolver;
    private final PlaceholderApiBridge placeholderApiBridge;

    public PlaceholderService(BungeeConnectService bungeeConnectService, PluginLogger logger) {
        LuckPermsBridge luckPermsBridge = new LuckPermsBridge(logger);
        this.builtinResolver = new BuiltinPlaceholderResolver(bungeeConnectService, luckPermsBridge);
        this.placeholderApiBridge = new PlaceholderApiBridge(logger);
    }

    public void reloadAll() {
        builtinResolver.reload();
        placeholderApiBridge.reload();
    }

    public String render(Player player, String input) {
        String rendered = input == null ? "" : input;
        Map<String, String> values = builtinResolver.resolveValues(player);

        for (Map.Entry<String, String> entry : values.entrySet()) {
            rendered = rendered.replace('<' + entry.getKey() + '>', entry.getValue());
        }

        rendered = builtinResolver.applyNetworkServerPlaceholders(rendered);
        rendered = placeholderApiBridge.apply(player, rendered);
        return builtinResolver.applyLegacyAliasFallbacks(rendered, values);
    }
}
