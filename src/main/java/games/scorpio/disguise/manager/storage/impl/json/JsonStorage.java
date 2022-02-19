package games.scorpio.disguise.manager.storage.impl.json;

import games.scorpio.disguise.manager.skin.SkinData;
import games.scorpio.disguise.manager.storage.Storage;

import java.util.Map;
import java.util.UUID;

public class JsonStorage implements Storage {

    @Override
    public Map<UUID, SkinData> gatherData() {
        return null;
    }

    @Override
    public void update(UUID uuid, SkinData data) {

    }

}
