package games.scorpio.disguise.manager.storage;

import games.scorpio.disguise.GamerDisguise;
import games.scorpio.disguise.manager.skin.SkinData;

import java.util.Map;
import java.util.UUID;

public class StorageManager implements Storage {

    private Storage storage = null;

    public StorageManager(StorageType type) {
        try {
            this.storage = type.getClazz().getConstructor(GamerDisguise.class).newInstance(GamerDisguise.getInstance());
        } catch (Exception ex) {
            GamerDisguise.getInstance().getLogger().info("The constructor for the selected storage type does not exist, please try another storage type.");
            GamerDisguise.getInstance().getServer().shutdown();
        }
    }

    public boolean hasStorage() {
        return this.storage != null;
    }

    @Override
    public Map<UUID, SkinData> gatherData() {
        return storage.gatherData();
    }

    @Override
    public void update(UUID uuid, SkinData data) {
        storage.update(uuid, data);
    }

}
