package cn.mythicland.mythiclobby.npc.skin;

import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.platform.skin.SkinData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@InjectComponent
public final class MojangSkinFetcher implements SkinFetcher {
    private static final Pattern UUID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([a-fA-F0-9]{32})\"");
    private static final Pattern VALUE_PATTERN = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("\"signature\"\\s*:\\s*\"([^\"]+)\"");

    @Override
    public SkinData fetchSkin(String playerName) throws IOException {
        String encodedPlayerName = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
        String profileResponse = readHttp("https://api.mojang.com/users/profiles/minecraft/" + encodedPlayerName);
        String uuid = extract(profileResponse, UUID_PATTERN);
        if (uuid == null) return null;

        String sessionResponse = readHttp(
                "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false"
        );
        String value = extract(sessionResponse, VALUE_PATTERN);
        String signature = extract(sessionResponse, SIGNATURE_PATTERN);
        return value != null && signature != null ? new SkinData(value, signature) : null;
    }

    private String readHttp(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "MythicLobby/1.1");
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == 404 || responseCode == 204) return null;
            if (responseCode != 200) throw new IOException("HTTP " + responseCode + " for " + url);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } finally {
            connection.disconnect();
        }
    }

    private String extract(String input, Pattern pattern) {
        if (input == null || input.trim().isEmpty()) return null;
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }
}
