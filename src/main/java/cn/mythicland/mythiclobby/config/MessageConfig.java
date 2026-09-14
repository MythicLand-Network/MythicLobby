package cn.mythicland.mythiclobby.config;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@InjectComponent
public final class MessageConfig extends AbstractYamlConfiguration {
    private static final String FILE_NAME = "messages.yml";
    private static final String LEGACY_FILE_NAME = "message.yml";

    public MessageConfig(Plugin plugin, ConfigManager configManager, PluginLogger logger) {
        super(plugin, configManager, logger, prepareMessagesFile(plugin, logger));
    }

    private static String prepareMessagesFile(Plugin plugin, PluginLogger logger) {
        File dataFolder = plugin.getDataFolder();
        File currentFile = new File(dataFolder, FILE_NAME);
        File legacyFile = new File(dataFolder, LEGACY_FILE_NAME);
        if (currentFile.exists() || !legacyFile.exists()) {
            return FILE_NAME;
        }

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            String message = "Failed to create plugin data folder for " + FILE_NAME;
            logger.error(message);
            throw new IllegalStateException(message);
        }

        try {
            Files.move(legacyFile.toPath(), currentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Migrated legacy message.yml to messages.yml.");
        } catch (IOException exception) {
            logger.error("Failed to migrate legacy message.yml to messages.yml", exception);
            throw new IllegalStateException("Failed to migrate legacy message.yml to messages.yml", exception);
        }
        return FILE_NAME;
    }
}
