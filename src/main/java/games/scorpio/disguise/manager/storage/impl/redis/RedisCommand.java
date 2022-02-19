package games.scorpio.disguise.manager.storage.impl.redis;

import redis.clients.jedis.Jedis;

public interface RedisCommand<T> {
    T execute(Jedis jedis);
}
