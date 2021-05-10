package io.github.agentrkid.gamerdisguise.manager;

import com.mojang.authlib.GameProfile;
import io.github.agentrkid.gamerdisguise.util.ModifierUtil;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisguiseManager {
    private Map<String, EntityPlayer> playersByName = new HashMap<>();
    @Getter private final Map<UUID, DisguiseData> storedDisguiseData = new HashMap<>();

    private static Field GAME_PROFILE_FIELD;

    // Suppress unchecked warnings, its useless.
    @SuppressWarnings("unchecked")
    public DisguiseManager() {
        try {
            PlayerList playerList = (PlayerList) ModifierUtil.changeModifiers(CraftServer.class.getDeclaredField("playerList"),
                    Modifier.PROTECTED, Modifier.FINAL).get(Bukkit.getServer());
            this.playersByName = (Map<String, EntityPlayer>) ModifierUtil.changeModifiers(PlayerList.class.getDeclaredField("playersByName"),
                    Modifier.FINAL).get(playerList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void disguise(Player player, String disguiseName) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer handle = craftPlayer.getHandle();

        GameProfile newGameProfile = new GameProfile(player.getUniqueId(), disguiseName);

        try {
            storedDisguiseData.put(craftPlayer.getUniqueId(), new DisguiseData(handle.getProfile(), player.getPlayerListName(), handle.displayName));

            // Change the GameProfile in HumanEntity.
            GAME_PROFILE_FIELD.set(handle, newGameProfile);

            // Remove their current info from the map to be readded under the new name.
            playersByName.remove(player.getName());

            // Remove the old information from tab/client side.
            PacketPlayOutPlayerInfo removeInfoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, handle);

            // Add the new information to the tab/client side.
            player.setPlayerListName(disguiseName);
            player.setDisplayName(disguiseName);
            PacketPlayOutPlayerInfo addInfoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, handle);

            WorldServer world = (WorldServer) handle.getWorld();
            PacketPlayOutRespawn respawnPacket = new PacketPlayOutRespawn(world.dimension, world.getDifficulty(),
                    world.worldData.getType(), handle.playerInteractManager.getGameMode());

            Location location = player.getLocation();
            PacketPlayOutPosition positionPacket = new PacketPlayOutPosition(location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch(), Collections.emptySet());

            PacketPlayOutHeldItemSlot slotPacket = new PacketPlayOutHeldItemSlot(player.getInventory().getHeldItemSlot());

            PlayerConnection connection = handle.playerConnection;

            connection.sendPacket(removeInfoPacket);
            connection.sendPacket(addInfoPacket);
            connection.sendPacket(respawnPacket);
            connection.sendPacket(positionPacket);
            connection.sendPacket(slotPacket);

            // Add them back to the map under the new name.
            playersByName.put(player.getName(), handle);

            updatePlayer(craftPlayer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updatePlayer(CraftPlayer update) {
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

    static {
        try {
            GAME_PROFILE_FIELD = ModifierUtil.changeModifiers(EntityHuman.class.getDeclaredField("bH"), Modifier.FINAL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
