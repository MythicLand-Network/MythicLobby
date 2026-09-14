package cn.mythicland.mythiclobby.config;

import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public abstract class AbstractYamlConfiguration {

    private final Plugin plugin;
    private final PluginLogger logger;
    private final String fileName;
    private final File file;
    private YamlConfiguration yamlConfiguration;

    protected AbstractYamlConfiguration(Plugin plugin, ConfigManager configManager, PluginLogger logger, String fileName) {
        this.plugin = plugin;
        this.logger = logger;
        this.fileName = fileName;
        saveDefaultConfig();
        this.file = new File(plugin.getDataFolder(), fileName);
        loadYamlConfiguration();
        configManager.register(this);
    }

    public void saveDefaultConfig() {
        File dataFolder = plugin.getDataFolder();
        File file = new File(dataFolder, fileName);
        if (file.exists()) return;
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            String message = "Failed to create plugin data folder for " + fileName;
            logger.error(message);
            throw new IllegalStateException(message);
        }

        try {
            plugin.saveResource(fileName, false);
        } catch (IllegalArgumentException exception) {
            logger.error("Failed to copy default config resource " + fileName, exception);
            throw new IllegalStateException("Failed to copy default config resource " + fileName, exception);
        }
    }

    public void loadYamlConfiguration() {
        yamlConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        loadYamlConfiguration();
    }

    public void reloadYamlConfiguration() {
        reload();
    }

    public void saveYamlConfiguration() {
        try {
            yamlConfiguration.save(file);
        } catch (IOException exception) {
            logger.error("Failed to save " + getFileName(), exception);
            throw new IllegalStateException("Failed to save " + getFileName(), exception);
        }
    }

    public YamlConfiguration getYamlConfiguration() {
        return yamlConfiguration;
    }

    public String getFileName() {
        return fileName;
    }
}
