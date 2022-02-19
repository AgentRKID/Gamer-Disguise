package games.scorpio.disguise.command;

import com.comphenix.protocol.ProtocolLibrary;
import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.manager.disguise.DisguiseManager;
import games.scorpio.disguise.manager.skin.SkinData;
import games.scorpio.disguise.manager.skin.SkinManager;
import games.scorpio.disguise.util.CC;
import games.scorpio.disguise.util.MojangUtil;
import games.scorpio.disguise.util.Tasks;
import io.github.agentrkid.anvilmenuapi.menu.AnvilMenu;
import io.github.agentrkid.anvilmenuapi.menu.CloseResult;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DisguiseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Configuration config = GamerDisguise.getInstance().getConfig();

        if (!sender.hasPermission(config.getString("Permissions.disguise"))) {
            sender.sendMessage(CC.translate("&cNo permission"));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.translate("&cConsole can't disguise..."));
            return false;
        }

        Player player = (Player) sender;

        if (GamerDisguise.getInstance().getDisguiseManager().isDisguised(player)) {
            sender.sendMessage(CC.translate(config.getString("Messages.already-disguised")));
            return false;
        }

        if (!config.getBoolean("Settings.random-nickname") || player.hasPermission(config.getString("Permissions.bypass-random-nickname"))) {
            new AnvilMenu((result, output) -> {
                if (result == CloseResult.FINISH) {
                    if (output.length() < 3) {
                        player.sendMessage(CC.translate("&cYou need to have more then 3 characters in your name to disguise."));
                        return;
                    }

                    if (output.length() > 16) {
                        player.sendMessage(CC.translate("&cYou cannot have more then 16 characters in your name to disguise."));
                        return;
                    }

                    String[] spaceSplit = output.split(" ");

                    if (spaceSplit.length > 1) {
                        player.sendMessage(CC.translate(config.getString("Messages.spaces-not-allowed")));
                        return;
                    }

                    Player checkPlayer = Bukkit.getPlayer(output);

                    if (checkPlayer != null) {
                        player.sendMessage(CC.translate("&cThere is someone already online with the name " + output + "."));
                        return;
                    }
                    disguise(player, output);
                } else {
                    player.sendMessage(CC.translate("&cCancelled disguising process."));
                }
            }).open(player, "Enter a name");
        } else {
            List<String> randomNicknames = config.getStringList("Settings.random-nicknames");

            if (randomNicknames == null || randomNicknames.size() < 1) {
                player.sendMessage(CC.translate("&cThere are currently no random nicknames setup."));
                return false;
            }

            String randomName = randomNicknames.get(ThreadLocalRandom.current().nextInt(randomNicknames.size()));
            disguise(player, randomName);
        }

        return true;
    }

    private void disguise(Player player, String disguisedName) {
        DisguiseManager disguiseManager = GamerDisguise.getInstance().getDisguiseManager();

        Tasks.runAsync(() -> {
            SkinData data;
            UUID disguiseSkinUuid = MojangUtil.getUuidFromName(disguisedName);

            if (disguiseSkinUuid != null) {
                data = GamerDisguise.getInstance().getSkinStorage().getSkinData(disguiseSkinUuid);
            } else {
                // Default to the normal skin if no player exists named from output
                data = SkinManager.DEFAULT_SKIN;
            }

            boolean disguise = disguiseManager.disguise(player, disguisedName, data.getTextureValue(), data.getTextureSign());

            if (disguise) {
                // We warn them that if a player with the name comes online they'll be kicked.
                player.sendMessage(CC.translate(

                        "&aYou have disguised as " + disguisedName + ".",
                        "&4&lWARNING&4: &cIf someone with the name \"" + disguisedName + "\" logs in, you will be kicked."
                ));
                player.setMetadata(GamerDisguise.METADATA, new FixedMetadataValue(GamerDisguise.getInstance(), "10010"));
            } else {
                player.sendMessage(CC.translate("&cFailed to disguise."));
            }
        });
    }

}
