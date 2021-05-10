package io.github.agentrkid.gamerdisguise;

import io.github.agentrkid.gamerdisguise.listener.ConnectionListener;
import io.github.agentrkid.gamerdisguise.manager.DisguiseManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class GamerDisguise extends JavaPlugin {
    @Getter private static GamerDisguise instance;

    @Getter private DisguiseManager disguiseManager;

    @Override
    public void onEnable() {
        instance = this;

        disguiseManager = new DisguiseManager();

        Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);
    }
}
