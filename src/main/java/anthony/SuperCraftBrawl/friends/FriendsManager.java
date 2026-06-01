package anthony.SuperCraftBrawl.friends;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class FriendsManager {

    private final Core main;

    public FriendsManager(Core main) {
        this.main = main;
    }

    public void ensureTables() {
        String friendsTable =
                "CREATE TABLE IF NOT EXISTS scb_friends (" +
                        "player_uuid VARCHAR(36) NOT NULL," +
                        "friend_uuid VARCHAR(36) NOT NULL," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "PRIMARY KEY (player_uuid, friend_uuid)," +
                        "INDEX (friend_uuid)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

        String requestsTable =
                "CREATE TABLE IF NOT EXISTS scb_friend_requests (" +
                        "sender_uuid VARCHAR(36) NOT NULL," +
                        "receiver_uuid VARCHAR(36) NOT NULL," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "PRIMARY KEY (sender_uuid, receiver_uuid)," +
                        "INDEX (receiver_uuid)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

        main.getDatabaseManager().multiExecuteUpdateCommand(friendsTable, requestsTable);
    }

    public List<FriendProfile> getFriends(UUID playerUuid) {
        List<FriendProfile> friends = new ArrayList<>();

        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "SELECT f.friend_uuid, COALESCE(pd.LastPlayerName, 'Unknown') AS LastPlayerName " +
                            "FROM scb_friends f " +
                            "LEFT JOIN PlayerData pd ON pd.UUID = f.friend_uuid " +
                            "WHERE f.player_uuid = ? " +
                            "ORDER BY pd.LastPlayerName ASC"
            );

            stmt.setString(1, playerUuid.toString());

            ResultSet set = stmt.executeQuery();

            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("friend_uuid"));
                String name = set.getString("LastPlayerName");
                friends.add(new FriendProfile(uuid, name, Bukkit.getPlayer(uuid) != null));
            }

            set.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friends;
    }

    public List<FriendProfile> getPendingRequests(UUID receiverUuid) {
        List<FriendProfile> requests = new ArrayList<>();

        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "SELECT r.sender_uuid, COALESCE(pd.LastPlayerName, 'Unknown') AS LastPlayerName " +
                            "FROM scb_friend_requests r " +
                            "LEFT JOIN PlayerData pd ON pd.UUID = r.sender_uuid " +
                            "WHERE r.receiver_uuid = ? " +
                            "ORDER BY r.created_at DESC"
            );

            stmt.setString(1, receiverUuid.toString());

            ResultSet set = stmt.executeQuery();

            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("sender_uuid"));
                String name = set.getString("LastPlayerName");
                requests.add(new FriendProfile(uuid, name, Bukkit.getPlayer(uuid) != null));
            }

            set.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public List<UUID> getFriendUuids(UUID playerUuid) {
        List<UUID> friends = new ArrayList<>();

        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "SELECT friend_uuid FROM scb_friends WHERE player_uuid = ?"
            );

            stmt.setString(1, playerUuid.toString());

            ResultSet set = stmt.executeQuery();

            while (set.next()) {
                friends.add(UUID.fromString(set.getString("friend_uuid")));
            }

            set.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friends;
    }

    public int getOnlineFriendsCount(UUID playerUuid) {
        int count = 0;

        for (FriendProfile friend : getFriends(playerUuid)) {
            if (friend.isOnline()) {
                count++;
            }
        }

        return count;
    }

    public int getPendingRequestCount(UUID receiverUuid) {
        int count = 0;

        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "SELECT COUNT(*) AS total FROM scb_friend_requests WHERE receiver_uuid = ?"
            );

            stmt.setString(1, receiverUuid.toString());

            ResultSet set = stmt.executeQuery();

            if (set.next()) {
                count = set.getInt("total");
            }

            set.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    public UUID findPlayerUuidByName(String name) {
        Player online = Bukkit.getPlayerExact(name);

        if (online != null) {
            return online.getUniqueId();
        }

        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "SELECT UUID FROM PlayerData WHERE LOWER(LastPlayerName) = LOWER(?) LIMIT 1"
            );

            stmt.setString(1, name);

            ResultSet set = stmt.executeQuery();

            UUID uuid = null;

            if (set.next()) {
                uuid = UUID.fromString(set.getString("UUID"));
            }

            set.close();
            stmt.close();

            return uuid;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public FriendResult sendRequest(Player sender, String targetName) {
        UUID targetUuid = findPlayerUuidByName(targetName);

        if (targetUuid == null) {
            return FriendResult.PLAYER_NOT_FOUND;
        }

        if (targetUuid.equals(sender.getUniqueId())) {
            return FriendResult.CANNOT_ADD_SELF;
        }

        if (areFriends(sender.getUniqueId(), targetUuid)) {
            return FriendResult.ALREADY_FRIENDS;
        }

        if (hasPendingRequest(sender.getUniqueId(), targetUuid)) {
            return FriendResult.REQUEST_ALREADY_SENT;
        }

        if (hasPendingRequest(targetUuid, sender.getUniqueId())) {
            acceptRequest(sender.getUniqueId(), targetUuid);
            return FriendResult.REQUEST_ACCEPTED;
        }

        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "INSERT INTO scb_friend_requests (sender_uuid, receiver_uuid) VALUES (?, ?)"
            );

            stmt.setString(1, sender.getUniqueId().toString());
            stmt.setString(2, targetUuid.toString());

            stmt.executeUpdate();
            stmt.close();

            Player target = Bukkit.getPlayer(targetUuid);

            if (target != null) {
                target.sendMessage(main.color("&a&l(!) &r" + sender.getName() + " sent you a friend request."));

                TextComponent prefix = new TextComponent(main.color("&7["));

                TextComponent accept = new TextComponent(main.color("&a&lACCEPT"));
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends accept " + sender.getName()));
                accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(main.color("&aClick to accept " + sender.getName() + "'s friend request")).create()));

                TextComponent middle = new TextComponent(main.color("&7] ["));

                TextComponent reject = new TextComponent(main.color("&c&lREJECT"));
                reject.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friends reject " + sender.getName()));
                reject.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(main.color("&cClick to reject " + sender.getName() + "'s friend request")).create()));

                TextComponent suffix = new TextComponent(main.color("&7]"));

                prefix.addExtra(accept);
                prefix.addExtra(middle);
                prefix.addExtra(reject);
                prefix.addExtra(suffix);

                target.spigot().sendMessage(prefix);
            }

            return FriendResult.REQUEST_SENT;
        } catch (SQLException e) {
            e.printStackTrace();
            return FriendResult.ERROR;
        }
    }

    public FriendResult acceptRequest(UUID accepterUuid, UUID senderUuid) {
        if (!hasPendingRequest(senderUuid, accepterUuid)) {
            return FriendResult.NO_REQUEST;
        }

        try {
            Connection c = main.getDatabaseManager().getConnection();
            boolean oldAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);

            try {
                PreparedStatement insertOne = c.prepareStatement(
                        "INSERT IGNORE INTO scb_friends (player_uuid, friend_uuid) VALUES (?, ?)"
                );
                insertOne.setString(1, accepterUuid.toString());
                insertOne.setString(2, senderUuid.toString());
                insertOne.executeUpdate();
                insertOne.close();

                PreparedStatement insertTwo = c.prepareStatement(
                        "INSERT IGNORE INTO scb_friends (player_uuid, friend_uuid) VALUES (?, ?)"
                );
                insertTwo.setString(1, senderUuid.toString());
                insertTwo.setString(2, accepterUuid.toString());
                insertTwo.executeUpdate();
                insertTwo.close();

                PreparedStatement delete = c.prepareStatement(
                        "DELETE FROM scb_friend_requests WHERE sender_uuid = ? AND receiver_uuid = ?"
                );
                delete.setString(1, senderUuid.toString());
                delete.setString(2, accepterUuid.toString());
                delete.executeUpdate();
                delete.close();

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(oldAutoCommit);
            }

            Player sender = Bukkit.getPlayer(senderUuid);
            Player accepter = Bukkit.getPlayer(accepterUuid);

            if (sender != null && accepter != null) {
                sender.sendMessage(main.color("&a&l(!) &r" + accepter.getName() + " accepted your friend request!"));
            }

            return FriendResult.REQUEST_ACCEPTED;
        } catch (SQLException e) {
            e.printStackTrace();
            return FriendResult.ERROR;
        }
    }

    public FriendResult rejectRequest(UUID receiverUuid, UUID senderUuid) {
        if (!hasPendingRequest(senderUuid, receiverUuid)) {
            return FriendResult.NO_REQUEST;
        }

        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "DELETE FROM scb_friend_requests WHERE sender_uuid = ? AND receiver_uuid = ?"
            );

            stmt.setString(1, senderUuid.toString());
            stmt.setString(2, receiverUuid.toString());

            stmt.executeUpdate();
            stmt.close();

            return FriendResult.REQUEST_REJECTED;
        } catch (SQLException e) {
            e.printStackTrace();
            return FriendResult.ERROR;
        }
    }

    public FriendResult removeFriend(UUID playerUuid, UUID friendUuid) {
        if (!areFriends(playerUuid, friendUuid)) {
            return FriendResult.NOT_FRIENDS;
        }

        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "DELETE FROM scb_friends WHERE (player_uuid = ? AND friend_uuid = ?) OR (player_uuid = ? AND friend_uuid = ?)"
            );

            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, friendUuid.toString());
            stmt.setString(3, friendUuid.toString());
            stmt.setString(4, playerUuid.toString());

            stmt.executeUpdate();
            stmt.close();

            return FriendResult.FRIEND_REMOVED;
        } catch (SQLException e) {
            e.printStackTrace();
            return FriendResult.ERROR;
        }
    }

    public boolean areFriends(UUID first, UUID second) {
        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "SELECT 1 FROM scb_friends WHERE player_uuid = ? AND friend_uuid = ? LIMIT 1"
            );

            stmt.setString(1, first.toString());
            stmt.setString(2, second.toString());

            ResultSet set = stmt.executeQuery();
            boolean found = set.next();

            set.close();
            stmt.close();

            return found;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasPendingRequest(UUID senderUuid, UUID receiverUuid) {
        try {
            Connection c = main.getDatabaseManager().getConnection();

            PreparedStatement stmt = c.prepareStatement(
                    "SELECT 1 FROM scb_friend_requests WHERE sender_uuid = ? AND receiver_uuid = ? LIMIT 1"
            );

            stmt.setString(1, senderUuid.toString());
            stmt.setString(2, receiverUuid.toString());

            ResultSet set = stmt.executeQuery();
            boolean found = set.next();

            set.close();
            stmt.close();

            return found;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public enum FriendResult {
        REQUEST_SENT,
        REQUEST_ACCEPTED,
        REQUEST_REJECTED,
        FRIEND_REMOVED,
        PLAYER_NOT_FOUND,
        CANNOT_ADD_SELF,
        ALREADY_FRIENDS,
        REQUEST_ALREADY_SENT,
        NO_REQUEST,
        NOT_FRIENDS,
        ERROR
    }
}