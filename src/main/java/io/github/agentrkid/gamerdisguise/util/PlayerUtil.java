package io.github.agentrkid.gamerdisguise.util;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerUtil {
    public static void updatePlayer(CraftPlayer update) {
        // Update all attributes so the player can see the correct values.
        update.getHandle().updateAbilities();
        update.updateInventory();
        update.setExp(update.getExp());
        update.setLevel(update.getLevel());
        update.setHealth(update.getHealth());
        update.setFlying(update.isFlying());

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean canSee = player.canSee(update);

            // Show if player can't already see the updating player.
            if (!canSee) {
                player.showPlayer(update);
            }

            // hide the updating player from the looped player.
            player.hidePlayer(update);

            // Reshow if they previously seen the player.
            if (canSee) {
                player.showPlayer(update);
            }
        }
    }
}
