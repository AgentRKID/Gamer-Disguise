package io.github.agentrkid.gamerdisguise.util;

import com.google.gson.JsonObject;
import io.github.agentrkid.gamerdisguise.GamerDisguise;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MojangUtil {
    public static final String GET_UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";

    private static final Pattern DASHLESS_PATTERN = Pattern.compile("^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");
    private static final Map<String, UUID> nameToUuid = new HashMap<>();

    public static UUID getUuidFromName(String name) {
        if (nameToUuid.containsKey(name)) {
            return nameToUuid.get(name);
        }

        try {
            URL url = new URL(GET_UUID_URL + name);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonObject object = GamerDisguise.JSON_PARSER.parse(reader).getAsJsonObject();

            Matcher matcher = DASHLESS_PATTERN.matcher(object.get("id").getAsString());
            if (!matcher.matches()) {
                return null;
            }

            UUID uuidFound = UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
            nameToUuid.put(name, uuidFound);

            return uuidFound;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
