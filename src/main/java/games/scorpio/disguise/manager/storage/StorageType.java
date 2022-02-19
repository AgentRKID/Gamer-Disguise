package games.scorpio.disguise.manager.storage;

import games.scorpio.disguise.manager.storage.impl.json.JsonStorage;
import games.scorpio.disguise.manager.storage.impl.redis.RedisStorage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum StorageType {
    SESSION(null),
    JSON(JsonStorage.class),
    REDIS(RedisStorage.class);

    @Getter private final Class<? extends Storage> clazz;
}
