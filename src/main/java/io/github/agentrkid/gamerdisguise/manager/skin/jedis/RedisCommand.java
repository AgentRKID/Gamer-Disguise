package io.github.agentrkid.gamerdisguise.manager.skin.jedis;

import redis.clients.jedis.Jedis;

public interface RedisCommand<T> {
    T execute(Jedis jedis);
}
