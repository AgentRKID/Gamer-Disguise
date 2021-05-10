package io.github.agentrkid.gamerdisguise.listener;

import io.github.agentrkid.gamerdisguise.GamerDisguise;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GamerDisguise.getInstance().getDisguiseManager().disguise(event.getPlayer(), "Gamer");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GamerDisguise.getInstance().getDisguiseManager().getStoredProfile().remove(event.getPlayer().getUniqueId());
    }
}
