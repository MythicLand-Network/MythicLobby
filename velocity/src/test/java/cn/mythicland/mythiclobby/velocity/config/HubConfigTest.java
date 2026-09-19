package cn.mythicland.mythiclobby.velocity.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class HubConfigTest {
    @TempDir
    Path dataDirectory;

    @Test
    void usesLobbyAsTheBundledDefaultServer() {
        HubConfig config = new HubConfig(dataDirectory, getClass().getClassLoader());

        assertEquals("lobby", config.hubServer());
    }

    @Test
    void rejectsBlankServerName() throws IOException {
        Files.writeString(dataDirectory.resolve("config.yml"), "hub-server: '  '");

        assertThrows(
                IllegalArgumentException.class,
                () -> new HubConfig(dataDirectory, getClass().getClassLoader())
        );
    }
}
