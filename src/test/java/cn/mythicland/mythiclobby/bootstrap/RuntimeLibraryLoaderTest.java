package cn.mythicland.mythiclobby.bootstrap;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.InetSocketAddress;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeLibraryLoaderTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void repairsCorruptCacheAndLeavesUnknownLibraryUntouched() throws Exception {
        byte[] libraryBytes = "lobby-library".getBytes(StandardCharsets.UTF_8);
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/example.jar", exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(200, libraryBytes.length);
            exchange.getResponseBody().write(libraryBytes);
            exchange.close();
        });
        server.start();

        Path resourcesDirectory = this.temporaryDirectory.resolve("resources");
        Path manifest = resourcesDirectory.resolve("META-INF/mythiclobby-libraries.properties");
        Files.createDirectories(manifest.getParent());
        Files.writeString(
                manifest,
                "library.count=1\n"
                        + "library.0.coordinate=test:example:1.0\n"
                        + "library.0.fileName=example-1.0.jar\n"
                        + "library.0.downloadUrls=http://127.0.0.1:" + server.getAddress().getPort() + "/example.jar\n"
                        + "library.0.sha256=" + sha256(libraryBytes) + "\n"
                        + "library.0.platform=bukkit\n"
        );

        try (URLClassLoader resources = new URLClassLoader(
                new java.net.URL[]{resourcesDirectory.toUri().toURL()},
                null
        )) {
            Path dataDirectory = this.temporaryDirectory.resolve("data");
            Path unknown = dataDirectory.resolve("libs/unknown.jar");
            Files.createDirectories(unknown.getParent());
            Files.writeString(unknown, "unmanaged");
            RuntimeLibraryLoader loader = new RuntimeLibraryLoader(dataDirectory, resources, ignored -> {
            });

            loader.resolveLibraries();
            Path library = dataDirectory.resolve("libs/example-1.0.jar");
            Files.writeString(library, "corrupt");
            loader.resolveLibraries();

            assertArrayEquals(libraryBytes, Files.readAllBytes(library));
            assertEquals(2, requests.get());
            assertTrue(Files.exists(unknown));
        } finally {
            server.stop(0);
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder hash = new StringBuilder();
        for (byte value : digest) hash.append(String.format("%02x", value));
        return hash.toString();
    }
}
