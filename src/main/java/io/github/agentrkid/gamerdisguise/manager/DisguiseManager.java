package io.github.agentrkid.gamerdisguise.manager;

import com.mojang.authlib.GameProfile;
import io.github.agentrkid.gamerdisguise.event.PlayerDisguiseEvent;
import io.github.agentrkid.gamerdisguise.event.PlayerUnDisguiseEvent;
import io.github.agentrkid.gamerdisguise.util.ModifierUtil;
import io.github.agentrkid.gamerdisguise.util.PlayerUtil;
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
    @Getter private Map<String, EntityPlayer> playersByName = new HashMap<>();
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

    public boolean disguise(Player player, String disguiseName) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer handle = craftPlayer.getHandle();

        PlayerDisguiseEvent event = new PlayerDisguiseEvent(player, disguiseName);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

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

            if (connection != null) {
                connection.sendPacket(removeInfoPacket);
                connection.sendPacket(addInfoPacket);
                connection.sendPacket(respawnPacket);
                connection.sendPacket(positionPacket);
                connection.sendPacket(slotPacket);
            }

            // Add them back to the map under the new name.
            playersByName.put(player.getName(), handle);

            PlayerUtil.updatePlayer(craftPlayer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean undisguise(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer handle = craftPlayer.getHandle();

        DisguiseData disguiseData = this.storedDisguiseData.get(player.getUniqueId());

        if (disguiseData == null) {
            return false;
        }

        PlayerUnDisguiseEvent event = new PlayerUnDisguiseEvent(player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        try {
            // Remove their old disguise data (which is unneeded now.)
            this.storedDisguiseData.remove(player.getUniqueId());

            // Change the GameProfile back to the original
            GAME_PROFILE_FIELD.set(handle, disguiseData.getOriginalGameProfile());

            // Remove their current disguise information from the map to be readded under their original name.
            playersByName.remove(player.getName());

            // Remove the disguise information client side.
            PacketPlayOutPlayerInfo removeInfoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, handle);

            // Revert the changes made when disguising & update client side.
            player.setPlayerListName(disguiseData.getOriginalPlayerListName());
            player.setDisplayName(disguiseData.getOriginalPlayerDisplayName());
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

            // Add them back to the map under their original name.
            playersByName.put(player.getName(), handle);

            PlayerUtil.updatePlayer(craftPlayer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    static {
        try {
            GAME_PROFILE_FIELD = ModifierUtil.changeModifiers(EntityHuman.class.getDeclaredField("bH"), Modifier.FINAL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
