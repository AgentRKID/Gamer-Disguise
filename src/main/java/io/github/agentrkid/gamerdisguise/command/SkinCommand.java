package io.github.agentrkid.gamerdisguise.command;

import io.github.agentrkid.gamerdisguise.GamerDisguise;
import io.github.agentrkid.gamerdisguise.manager.skin.SkinData;
import io.github.agentrkid.gamerdisguise.manager.skin.SkinStorage;
import io.github.agentrkid.gamerdisguise.util.CC;
import io.github.agentrkid.gamerdisguise.util.MojangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkinCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gamer.skin.command")) {
            sender.sendMessage(CC.translate("&cNo permission"));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.translate("&cConsole can't disguise..."));
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage(CC.translate("&cUsage: /skin <skin owner>"));
            return false;
        }

        Player player = (Player) sender;

        SkinData data;
        UUID disguiseSkinUuid = MojangUtil.getUuidFromName(args[0]);

        if (disguiseSkinUuid != null) {
            data = GamerDisguise.getInstance().getSkinStorage().getSkinData(disguiseSkinUuid);
        } else {
            // Default to the normal skin if no player exists named from output
            data = SkinStorage.DEFAULT_SKIN;
        }

        GamerDisguise.getInstance().getDisguiseManager().changeSkin(player, data.getTextureValue(), data.getTextureSign());
        player.sendMessage(CC.translate("&aYou have changed skins!"));

        return true;
    }
}
