package io.github.agentrkid.gamerdisguise;

import com.google.gson.JsonParser;
import io.github.agentrkid.gamerdisguise.command.DisguiseCommand;
import io.github.agentrkid.gamerdisguise.command.SkinCommand;
import io.github.agentrkid.gamerdisguise.command.UndisguiseCommand;
import io.github.agentrkid.gamerdisguise.listener.ConnectionListener;
import io.github.agentrkid.gamerdisguise.manager.DisguiseManager;
import io.github.agentrkid.gamerdisguise.manager.skin.SkinStorage;
import io.github.agentrkid.gamerdisguise.manager.skin.jedis.RedisCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class GamerDisguise extends JavaPlugin {
    public static String METADATA = "Disguised";
    public static JsonParser JSON_PARSER = new JsonParser();

    @Getter private static GamerDisguise instance;

    @Getter private SkinStorage skinStorage;
    @Getter private DisguiseManager disguiseManager;

    @Getter private JedisPool localJedisPool;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        Configuration config = getConfig();

        // TODO Move to storage allowing them todo session if they don't have redis.

        try {
            String password = config.getString("Redis.Pass");
            this.localJedisPool = new JedisPool(new JedisPoolConfig(), config.getString("Redis.Host"), config.getInt("Redis.Port"), 20000, password.isEmpty() ? null : password, config.getInt("Redis.DbId", 0));
        } catch (JedisConnectionException ex) {
            ex.printStackTrace();
            Bukkit.shutdown();
        }

        skinStorage = new SkinStorage();
        disguiseManager = new DisguiseManager();

        Bukkit.getPluginCommand("disguise").setExecutor(new DisguiseCommand());
        Bukkit.getPluginCommand("undisguise").setExecutor(new UndisguiseCommand());
        Bukkit.getPluginCommand("skin").setExecutor(new SkinCommand());

        Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);
    }

    public <T> T runRedisCommand(RedisCommand<T> redisCommand) {
        Jedis jedis = this.localJedisPool.getResource();
        T result = null;

        try {
            result = redisCommand.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();

            if (jedis != null) {
                this.localJedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (jedis != null) {
                this.localJedisPool.returnResource(jedis);
            }
        }
        return result;
    }
}
