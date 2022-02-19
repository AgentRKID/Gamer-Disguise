package games.scorpio.disguise.listener;

import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.manager.disguise.DisguiseManager;
import games.scorpio.disguise.util.CC;
import games.scorpio.disguise.util.Tasks;
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
                Tasks.run(() -> player.kickPlayer(CC.translate("&cSomeone has logged in with the name " + player.getName() + ".")));
            } else {
                // This shouldn't of happened... but it did, so lets remove it.
                disguiseManager.getPlayersByName().remove(event.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DisguiseManager disguiseManager = GamerDisguise.getInstance().getDisguiseManager();

        if (disguiseManager.isDisguised(player)) {
            // They logged out lets just undisguise them because they're disguised.
            GamerDisguise.getInstance().getDisguiseManager().undisguise(player);
            player.removeMetadata(GamerDisguise.METADATA, GamerDisguise.getInstance());
        }
    }

}
