package games.scorpio.disguise.manager.storage;

import games.scorpio.disguise.manager.skin.SkinData;

import java.util.Map;
import java.util.UUID;

public interface Storage {

    Map<UUID, SkinData> gatherData();

    void update(UUID uuid, SkinData data);

}
