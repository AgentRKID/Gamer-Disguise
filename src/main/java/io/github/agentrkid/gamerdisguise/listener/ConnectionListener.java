package io.github.agentrkid.gamerdisguise.listener;

import io.github.agentrkid.gamerdisguise.GamerDisguise;
import io.github.agentrkid.gamerdisguise.manager.DisguiseManager;
import io.github.agentrkid.gamerdisguise.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {
    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        DisguiseManager disguiseManager = GamerDisguise.getInstance().getDisguiseManager();

        if (disguiseManager.getPlayersByName().get(event.getName()) != null) {
            Player player = Bukkit.getPlayer(event.getName());

            if (player != null && player.isOnline()) {
                // We can't kick async lol.
                Bukkit.getScheduler().runTask(GamerDisguise.getInstance(),
                        () -> player.kickPlayer(CC.translate("&cSomeone has logged in with the name " + player.getName() + ".")));
            } else {
                // TODO: Make sure we can remove here?
                disguiseManager.getPlayersByName().remove(event.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GamerDisguise.getInstance().getDisguiseManager().undisguise(event.getPlayer());
    }
}
