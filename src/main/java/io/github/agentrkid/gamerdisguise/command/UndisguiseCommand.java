package io.github.agentrkid.gamerdisguise.command;

import io.github.agentrkid.gamerdisguise.GamerDisguise;
import io.github.agentrkid.gamerdisguise.manager.DisguiseManager;
import io.github.agentrkid.gamerdisguise.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UndisguiseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gamer.disguise.command")) {
            sender.sendMessage(CC.translate("&cNo permission"));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.translate("&cConsole can't disguise or undisguise..."));
            return false;
        }

        Player player = (Player) sender;

        DisguiseManager disguiseManager = GamerDisguise.getInstance().getDisguiseManager();
        boolean undisguise = disguiseManager.undisguise(player);

        if (undisguise) {
            player.sendMessage(CC.translate("&aYou have undisguised."));
        } else {
            player.sendMessage(CC.translate("&cFailed to undisguise."));
        }
        return true;
    }
}
