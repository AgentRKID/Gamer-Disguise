package games.scorpio.disguise.manager.storage.impl.redis;

import com.google.common.collect.ImmutableMap;
import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.manager.skin.SkinData;
import games.scorpio.disguise.manager.storage.Storage;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class RedisStorage implements Storage {

    private JedisPool jedisPool;

    public RedisStorage(GamerDisguise plugin) {
        try {
            jedisPool = new JedisPool(new URI(plugin.getConfig().getString("Settings.redis-uri", "redis://localhost:6379?db=0")), 5000);
        } catch (Exception ex) {
            plugin.getLogger().info("Couldn't connect to redis server...");
            plugin.getServer().shutdown();
        }
    }

    @Override
    public Map<UUID, SkinData> gatherData() {
        return returnValueRedisCommand(redis -> {
            Map<UUID, SkinData> map = new HashMap<>();

            Set<String> keys = redis.keys("DisguiseSkinCache:*");

            for (String key : keys) {
                Map<String, String> searchedData = redis.hgetAll(key);
                map.put(UUID.fromString(key.split(":")[1]), new SkinData(searchedData.get("textureValue"), searchedData.get("textureSign")));
            }
            return map;
        });
    }

    @Override
    public void update(UUID uuid, SkinData data) {
        runRedisCommand(redis ->
                redis.hmset(
                        "DisguiseSkinCache:" + uuid.toString(),
                        ImmutableMap.of("textureValue", data.getTextureValue(), "textureSign", data.getTextureSign())
                )
        );
    }

    public void runRedisCommand(Consumer<Jedis> consumer) {
        if (jedisPool == null || jedisPool.isClosed()) {
            throw new IllegalStateException("A connection to the redis server couldn't be established or has been forcefully closed");
        }

        Jedis jedis = jedisPool.getResource();

        try {
            consumer.accept(jedis);
        } catch (Exception ex) {
            this.jedisPool.returnBrokenResource(jedis);
            jedis = null;
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    public <T> T returnValueRedisCommand(Function<Jedis, T> command) {
        if (jedisPool == null || jedisPool.isClosed()) {
            throw new IllegalStateException("A connection to the redis server couldn't be established or has been forcefully closed");
        }

        T result = null;

        Jedis jedis = this.jedisPool.getResource();

        try {
            result = command.apply(jedis);
        } catch (Exception ex) {
            this.jedisPool.returnBrokenResource(jedis);
            jedis = null;
            ex.printStackTrace();
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
        return result;
    }

}
