package games.scorpio.disguise.manager.storage.impl.session;

import games.scorpio.disguise.manager.skin.SkinData;
import games.scorpio.disguise.manager.storage.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionStorage implements Storage {

    @Override
    public Map<UUID, SkinData> gatherData() {
        return new HashMap<>();
    }

    @Override
    public void update(UUID uuid, SkinData data) {}

}
