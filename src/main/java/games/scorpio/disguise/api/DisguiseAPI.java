package games.scorpio.disguise.api;

import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.manager.rank.Rank;
import games.scorpio.disguise.manager.skin.SkinData;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DisguiseAPI {

    public static boolean isDisguised(Player player) {
        return GamerDisguise.getInstance().getDisguiseManager().isDisguised(player);
    }

    public static SkinData getSkinData(UUID uuid) {
        return GamerDisguise.getInstance().getSkinStorage().getSkinData(uuid);
    }

    public static Rank getDisguiseRank(Player player) { return null; }

}
