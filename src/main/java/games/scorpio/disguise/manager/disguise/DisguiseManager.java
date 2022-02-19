package games.scorpio.disguise.manager.disguise;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import games.scorpio.disguise.event.PlayerDisguiseEvent;
import games.scorpio.disguise.event.PlayerSkinChangeEvent;
import games.scorpio.disguise.event.PlayerUnDisguiseEvent;
import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.util.ReflectionUtil;
import games.scorpio.disguise.util.PlayerUtil;
import games.scorpio.disguise.util.Tasks;
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
            PlayerList playerList = (PlayerList) ReflectionUtil.setModifiers(CraftServer.class.getDeclaredField("playerList"),
                    Modifier.PROTECTED, Modifier.FINAL).get(Bukkit.getServer());
            this.playersByName = (Map<String, EntityPlayer>) ReflectionUtil.setModifiers(PlayerList.class.getDeclaredField("playersByName"),
                    Modifier.FINAL).get(playerList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isDisguised(Player player) {
        return isDisguised(player.getUniqueId());
    }

    public boolean isDisguised(UUID playerId) {
        return this.storedDisguiseData.containsKey(playerId);
    }

    public boolean disguise(Player player, String disguiseName, String disguiseTextureValue, String disguiseTextureSign) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer handle = craftPlayer.getHandle();

        // Call event to allow hooked plugins to
        // either cancel or do something within that plugin.
        PlayerDisguiseEvent event = new PlayerDisguiseEvent(player, disguiseName);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        GameProfile newGameProfile = new GameProfile(player.getUniqueId(), disguiseName);

        if (disguiseTextureValue != null
                && disguiseTextureSign != null) {
            // For skins we need to add the
            // texture property to the GameProfile
            newGameProfile.getProperties().put("texture", new Property("textures", disguiseTextureValue, disguiseTextureSign));
        }

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

            // Just incase we update async
            Tasks.safeRunSync(() -> PlayerUtil.updatePlayer(craftPlayer));
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

        // Call event to allow hooked plugins to
        // either cancel or do something within that plugin.
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

            // Just incase we update async
            Tasks.safeRunSync(() -> PlayerUtil.updatePlayer(craftPlayer));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean changeSkin(Player player, String textureValue, String textureSign) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer handle = craftPlayer.getHandle();

        PlayerSkinChangeEvent event = new PlayerSkinChangeEvent(player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        GameProfile gameProfile = craftPlayer.getProfile();

        gameProfile.getProperties().clear();
        gameProfile.getProperties().put("texture", new Property("textures", textureValue, textureSign));

        PacketPlayOutPlayerInfo removeInfoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, handle);
        PacketPlayOutPlayerInfo addInfoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, handle);

        WorldServer world = (WorldServer) handle.getWorld();
        PacketPlayOutRespawn respawnPacket = new PacketPlayOutRespawn(
                world.dimension,
                world.getDifficulty(),
                world.worldData.getType(),
                handle.playerInteractManager.getGameMode()
        );

        Location location = player.getLocation();
        PacketPlayOutPosition positionPacket = new PacketPlayOutPosition(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                Collections.emptySet()
        );

        PacketPlayOutHeldItemSlot slotPacket = new PacketPlayOutHeldItemSlot(player.getInventory().getHeldItemSlot());

        PlayerConnection connection = handle.playerConnection;

        connection.sendPacket(removeInfoPacket);
        connection.sendPacket(addInfoPacket);
        connection.sendPacket(respawnPacket);
        connection.sendPacket(positionPacket);
        connection.sendPacket(slotPacket);

        // Just incase we update async
        Tasks.safeRunSync(() -> PlayerUtil.updatePlayer(craftPlayer));

        return true;
    }

    static {
        try {
            GAME_PROFILE_FIELD = ReflectionUtil.setModifiers(EntityHuman.class.getDeclaredField("bH"), Modifier.FINAL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
