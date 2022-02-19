package games.scorpio.disguise.command;

import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.manager.disguise.DisguiseManager;
import games.scorpio.disguise.manager.skin.SkinData;
import games.scorpio.disguise.manager.skin.SkinManager;
import games.scorpio.disguise.util.CC;
import games.scorpio.disguise.util.MojangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Configuration config = GamerDisguise.getInstance().getConfig();

        if (!sender.hasPermission(config.getString("Permissions.skin"))) {
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
        DisguiseManager disguiseManager = GamerDisguise.getInstance().getDisguiseManager();

        if (GamerDisguise.getInstance().getConfig().getBoolean("Settings.skin-change-require-disguise", true) && !disguiseManager.isDisguised(player)) {
            player.sendMessage(CC.translate(config.getString("Messages.change-skins-disguise")));
            return false;
        }

        SkinData data;
        UUID disguiseSkinUuid = MojangUtil.getUuidFromName(args[0]);

        if (disguiseSkinUuid != null) {
            data = GamerDisguise.getInstance().getSkinStorage().getSkinData(disguiseSkinUuid);
        } else {
            // Default to the normal skin if no player exists named from output
            data = SkinManager.DEFAULT_SKIN;
        }

        if (GamerDisguise.getInstance().getDisguiseManager().changeSkin(player, data.getTextureValue(), data.getTextureSign())) {
            player.sendMessage(CC.translate(config.getString("Messages.changed-skins")));
        }
        return true;
    }

}
