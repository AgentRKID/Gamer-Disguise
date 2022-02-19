package games.scorpio.disguise.command;

import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.manager.disguise.DisguiseManager;
import games.scorpio.disguise.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class UndisguiseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Configuration config = GamerDisguise.getInstance().getConfig();

        if (!sender.hasPermission(config.getString("Permissions.disguise"))) {
            sender.sendMessage(CC.translate("&cNo permission"));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.translate("&cConsole can't disguise or undisguise..."));
            return false;
        }

        Player player = (Player) sender;
        DisguiseManager disguiseManager = GamerDisguise.getInstance().getDisguiseManager();

        if (!disguiseManager.isDisguised(player)) {
            player.sendMessage(CC.translate(config.getString("Messages.not-disguised")));
            return false;
        }

        boolean undisguise = disguiseManager.undisguise(player);

        if (undisguise) {
            player.sendMessage(CC.translate(config.getString("Messages.disguised")));
            player.removeMetadata(GamerDisguise.METADATA, GamerDisguise.getInstance());
        } else {
            player.sendMessage(CC.translate("&cFailed to undisguise."));
        }
        return true;
    }

}
