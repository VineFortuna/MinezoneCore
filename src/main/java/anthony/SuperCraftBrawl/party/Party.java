package anthony.SuperCraftBrawl.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Party {

    private UUID leader;
    private final LinkedHashSet<UUID> members = new LinkedHashSet<>();
    private boolean privateGames = false;
    private boolean partyChatMuted = false;

    public Party(UUID leader) {
        this.leader = leader;
        this.members.add(leader);
    }

    public UUID getLeaderId() {
        return leader;
    }

    public Player getLeader() {
        return Bukkit.getPlayer(leader);
    }

    public void setLeader(UUID leader) {
        if (!members.contains(leader)) {
            members.add(leader);
        }

        this.leader = leader;
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public boolean contains(UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public int size() {
        return members.size();
    }

    public Set<UUID> getMemberIds() {
        return Collections.unmodifiableSet(members);
    }

    public List<Player> getOnlineMembers() {
        List<Player> onlineMembers = new ArrayList<>();

        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null && player.isOnline()) {
                onlineMembers.add(player);
            }
        }

        return onlineMembers;
    }

    public boolean isPrivateGames() {
        return privateGames;
    }

    public void setPrivateGames(boolean privateGames) {
        this.privateGames = privateGames;
    }

    public boolean isPartyChatMuted() {
        return partyChatMuted;
    }

    public void setPartyChatMuted(boolean partyChatMuted) {
        this.partyChatMuted = partyChatMuted;
    }
}