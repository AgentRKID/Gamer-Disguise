package games.scorpio.disguise.manager.skin;

import com.google.gson.JsonObject;
import com.mojang.authlib.properties.Property;
import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.util.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkinManager implements Listener {

    public static final String GET_SKIN_TEXTURE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static final String DEFAULT_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTYyMDY4NTE0Mzc2MywKICAicHJvZmlsZUlkIiA6ICI2MGIyOTY5OWIzYWM0MjEzOGQxMDI1ZDM2Njk0ZDlhOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBZ2VudFJLSUQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE0YWY3MTg0NTVkNGFhYjUyOGU3YTYxZjg2ZmEyNWU2YTM2OWQxNzY4ZGNiMTNmN2RmMzE5YTcxM2ViODEwYiIKICAgIH0KICB9Cn0=";
    public static final String DEFAULT_SKIN_SIGN = "RtTDjx1RB8A6Mlh40p5CJcQwaltPmtykHqfiLjU8LrV/rTk/yt15UNrsrTm/snAAQNZqjnBGABv5Fov3GMlK49eAD3O0TYqidhRsTKtwRhZrsuQvMIMN57G4Zp8fQiH3kzszLtBgYy2qt3TzkXiXQhRS1ovJ5Ab12i1raSkEzNdu5pIgApeKAoJg3EPMrQjjIi59q0vxLseINmFQhqKBzp17YZJU8RZRTcECx/+lIK3U3a5ARxmRLveSa5gObEQ6v8VIRlv9jffk/BsqH8t024oymNYYNh1g+Q1jtQz0kRxIhFx5eBaR4xPkB1bxV2PpPG0J5UrT9tnV6fBwXzsEKfzBEswr6XMICW4B3PisRsrx1613/Aw3XSxivIGtVrOo0sDCe+iPpSQ6QwGp461drP281XnGsIKlU/1IdPmnkmFQQwClvFzy8k3IDZ0i47z0TigQyQ+rx9OApcJMZthP4wRBhGzDj7333SXQwToXYuavSzOzGeZsVr5S8iMXW1L7+LXPwNOmnBrEc5bXGM4qwiY7SGFqE9IpW4qQJIjriqHFtYjnC9XddaT234NKU5NwQkBjs5m6Hzlg5nKIzwxtJAHbc+lZkmRcUf1BpJf19hrMI0O0uOmemu8FWbpj4O6phAeuXJXcCCmRsFkHQ7p1GFjntwcol1NNHHXlzvjx3Ts=";

    public final static SkinData DEFAULT_SKIN = new SkinData(DEFAULT_SKIN_VALUE, DEFAULT_SKIN_SIGN);

    private final Map<UUID, SkinData> skins = new ConcurrentHashMap<>();

    public SkinManager() {
        this.skins.putAll(GamerDisguise.getInstance().getStorageManager().gatherData());
        Bukkit.getPluginManager().registerEvents(this, GamerDisguise.getInstance());
    }

    public SkinData getSkinData(UUID playerId) {
        if (skins.containsKey(playerId)) {
            return skins.get(playerId);
        }

        try {
            URL url = new URL(GET_SKIN_TEXTURE_URL + playerId.toString() + "?unsigned=false");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonObject object = GamerDisguise.JSON_PARSER.parse(reader).getAsJsonObject();

            if (object.has("error") || !object.isJsonObject()) {
                // Default to the default skin as the
                // player that was searched doesn't exist.
                return DEFAULT_SKIN;
            }

            JsonObject properties = object.getAsJsonArray("properties").get(0).getAsJsonObject();

            SkinData data = new SkinData(properties.get("value").getAsString(), properties.get("signature").getAsString());
            GamerDisguise.getInstance().getStorageManager().update(playerId, data);
            skins.put(playerId, data);

            return data;
        } catch (Exception ex) {
            return DEFAULT_SKIN;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Tasks.runLater(() -> {
            CraftPlayer player = (CraftPlayer) event.getPlayer();

            List<Property> properties = new ArrayList<>(player.getProfile().getProperties().get("textures"));

            if (!properties.isEmpty()) {
                Property property = properties.get(0);

                if (property != null && property.hasSignature()) {
                    SkinData data = skins.put(player.getUniqueId(), new SkinData(property.getValue(), property.getSignature()));
                    Tasks.runAsync(() -> GamerDisguise.getInstance().getStorageManager().update(player.getUniqueId(), data));
                }
            }
        }, 10L);
    }

}
