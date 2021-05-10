package io.github.agentrkid.gamerdisguise.util;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerUtil {
    public static void updatePlayer(CraftPlayer update) {
        update.getHandle().updateAbilities();
        update.updateInventory();
        update.setExp(update.getExp());
        update.setLevel(update.getLevel());
        update.setHealth(update.getHealth());
        update.setFlying(update.isFlying());

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean canSee = player.canSee(update);

            if (!canSee) {
                player.showPlayer(update);
            }

            player.hidePlayer(update);

            if (canSee) {
                player.showPlayer(update);
            }
        }
    }
}
