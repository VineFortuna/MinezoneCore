package anthony.SuperCraftBrawl.friends;

import java.util.UUID;

public class FriendProfile {
    private final UUID uuid;
    private final String name;
    private final boolean online;

    public FriendProfile(UUID uuid, String name, boolean online) {
        this.uuid = uuid;
        this.name = name;
        this.online = online;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return online;
    }
}