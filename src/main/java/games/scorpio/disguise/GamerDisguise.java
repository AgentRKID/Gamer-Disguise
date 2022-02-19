package games.scorpio.disguise;

import com.google.gson.JsonParser;
import games.scorpio.disguise.manager.disguise.DisguiseManager;
import games.scorpio.disguise.command.DisguiseCommand;
import games.scorpio.disguise.command.SkinCommand;
import games.scorpio.disguise.command.UndisguiseCommand;
import games.scorpio.disguise.listener.ConnectionListener;
import games.scorpio.disguise.manager.skin.SkinManager;
import games.scorpio.disguise.manager.storage.StorageManager;
import games.scorpio.disguise.manager.storage.StorageType;
import io.github.agentrkid.anvilmenuapi.AnvilMenuAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

public class GamerDisguise extends JavaPlugin {

    public static String METADATA = "Disguised";
    public static JsonParser JSON_PARSER = new JsonParser();

    @Getter private static GamerDisguise instance;

    @Getter private StorageManager storageManager;
    @Getter private SkinManager skinStorage;
    @Getter private DisguiseManager disguiseManager;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        Configuration config = getConfig();

        try {
            StorageType type = StorageType.valueOf(config.getString("Settings.skin-cache-type", "REDIS"));
            storageManager = new StorageManager(type);

            if (!storageManager.hasStorage()) {
                return;
            }
        } catch (Exception ex) {
            getLogger().info("Storage type \"" + config.getString("Settings.skin-cache-type") + "\" does not exist.");
            return;
        }

        skinStorage = new SkinManager();
        disguiseManager = new DisguiseManager();

        Bukkit.getPluginCommand("disguise").setExecutor(new DisguiseCommand());
        Bukkit.getPluginCommand("undisguise").setExecutor(new UndisguiseCommand());
        Bukkit.getPluginCommand("skin").setExecutor(new SkinCommand());

        Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);

        try {
            AnvilMenuAPI.register(this);
        } catch (Exception ignored) {} // API is already enabled...
    }

}
