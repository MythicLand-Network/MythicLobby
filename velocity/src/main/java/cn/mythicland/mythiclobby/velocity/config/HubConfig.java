package cn.mythicland.mythiclobby.velocity.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public final class HubConfig {
    private final Path file;
    private final ClassLoader classLoader;
    private String hubServer;

    public HubConfig(Path dataDirectory, ClassLoader classLoader) {
        this.file = dataDirectory.resolve("config.yml");
        this.classLoader = classLoader;
        this.saveDefaultConfig();
        this.reload();
    }

    public String hubServer() {
        return this.hubServer;
    }

    private void reload() {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
        try (Reader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
            Object loaded = yaml.load(reader);
            this.hubServer = parseHubServer(loaded);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load " + this.file.getFileName(), exception);
        }
    }

    private String parseHubServer(Object loaded) {
        if (!(loaded instanceof Map<?, ?> values)) {
            throw new IllegalArgumentException("config.yml must contain a hub-server value.");
        }
        Object value = values.get("hub-server");
        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("hub-server must not be blank.");
        }
        return value.toString().trim();
    }

    private void saveDefaultConfig() {
        try {
            Files.createDirectories(this.file.getParent());
            if (Files.exists(this.file)) return;
            try (InputStream inputStream = this.classLoader.getResourceAsStream("velocity/config.yml")) {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing bundled resource config.yml");
                }
                Files.copy(inputStream, this.file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save config.yml", exception);
        }
    }
}
