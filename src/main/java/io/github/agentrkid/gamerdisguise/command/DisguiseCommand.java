package io.github.agentrkid.gamerdisguise.command;

import com.comphenix.protocol.ProtocolLibrary;
import io.github.agentrkid.anvilmenuapi.menu.AnvilMenu;
import io.github.agentrkid.anvilmenuapi.menu.CloseResult;
import io.github.agentrkid.gamerdisguise.GamerDisguise;
import io.github.agentrkid.gamerdisguise.manager.DisguiseManager;
import io.github.agentrkid.gamerdisguise.manager.skin.SkinData;
import io.github.agentrkid.gamerdisguise.manager.skin.SkinStorage;
import io.github.agentrkid.gamerdisguise.util.CC;
import io.github.agentrkid.gamerdisguise.util.MojangUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class DisguiseCommand implements CommandExecutor {
    public static FixedMetadataValue FIXED_METADATA = new FixedMetadataValue(GamerDisguise.getInstance(), "10010");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gamer.disguise.command")) {
            sender.sendMessage(CC.translate("&cNo permission"));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.translate("&cConsole can't disguise..."));
            return false;
        }

        Player player = (Player) sender;

        if (ProtocolLibrary.getProtocolManager().getProtocolVersion(player) > 47) {
            new AnvilMenu((result, output) -> {
                if (result == CloseResult.FINISH) {
                    if (output.length() < 3) {
                        player.sendMessage(CC.translate("&cYou need to have more then 3 characters in your name to disguise."));
                        return;
                    }

                    String[] spaceSplit = output.split(" ");
                    if (spaceSplit.length > 1) {
                        player.sendMessage(CC.translate("&cYou cannot have spaces in your name."));
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
            return true;
        } else {
            if (args.length < 1) {
                player.sendMessage(CC.translate("&cUsage: /disguise <name>"));
                return false;
            }
            disguise(player, args[0]);
        }
        return true;
    }

    private void disguise(Player player, String disguisedName) {
        DisguiseManager disguiseManager = GamerDisguise.getInstance().getDisguiseManager();

        Bukkit.getScheduler().runTaskAsynchronously(GamerDisguise.getInstance(), () -> {
            SkinData data;
            UUID disguiseSkinUuid = MojangUtil.getUuidFromName(disguisedName);

            if (disguiseSkinUuid != null) {
                data = GamerDisguise.getInstance().getSkinStorage().getSkinData(disguiseSkinUuid);
            } else {
                // Default to the normal skin if no player exists named from output
                data = SkinStorage.DEFAULT_SKIN;
            }

            boolean disguise = disguiseManager.disguise(player, disguisedName, data.getTextureValue(), data.getTextureSign());

            if (disguise) {
                // We warn them that if a player with the name comes online they'll be kicked.
                player.sendMessage(CC.translate("&aYou have disguised as " + disguisedName + ".",
                        "&4&lWARNING&4: &cIf someone with the name \"" + disguisedName + "\" logs in, you will be kicked."));
                player.setMetadata(GamerDisguise.METADATA, FIXED_METADATA);
            } else {
                player.sendMessage(CC.translate("&cFailed to disguise."));
            }
        });
    }
}
