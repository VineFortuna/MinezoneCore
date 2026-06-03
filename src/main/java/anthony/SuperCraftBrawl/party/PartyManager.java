package anthony.SuperCraftBrawl.party;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class PartyManager {

    private static final long INVITE_DURATION_MILLIS = 60000L;

    private final Core main;

    private final Map<UUID, Party> partyByLeader = new HashMap<>();
    private final Map<UUID, Party> partyByMember = new HashMap<>();

    // invited player UUID -> inviter UUID -> expiry time
    private final Map<UUID, Map<UUID, Long>> invites = new HashMap<>();

    private final HashSet<UUID> partyChatToggled = new HashSet<>();

    public PartyManager(Core main) {
        this.main = main;
    }

    public Party getParty(Player player) {
        if (player == null) {
            return null;
        }

        return partyByMember.get(player.getUniqueId());
    }

    public boolean isInParty(Player player) {
        return getParty(player) != null;
    }

    public boolean isLeader(Player player) {
        Party party = getParty(player);
        return party != null && party.isLeader(player.getUniqueId());
    }

    public boolean isPartyChatToggled(Player player) {
        return partyChatToggled.contains(player.getUniqueId());
    }

    public void setPartyChatToggled(Player player, boolean enabled) {
        if (enabled) {
            partyChatToggled.add(player.getUniqueId());
        } else {
            partyChatToggled.remove(player.getUniqueId());
        }
    }

    public void togglePartyChat(Player player) {
        boolean enabled = !isPartyChatToggled(player);
        setPartyChatToggled(player, enabled);

        if (enabled) {
            player.sendMessage(main.color("&a&l(!) &rParty chat has been &aenabled&r."));
        } else {
            player.sendMessage(main.color("&a&l(!) &rParty chat has been &cdisabled&r."));
        }
    }

    public void invite(Player inviter, Player target) {
        cleanupExpiredInvites();

        if (target == null || !target.isOnline()) {
            inviter.sendMessage(main.color("&c&l(!) &rThat player is not online."));
            return;
        }

        if (inviter.equals(target)) {
            inviter.sendMessage(main.color("&c&l(!) &rYou cannot invite yourself to a party."));
            return;
        }

        Party inviterParty = getParty(inviter);

        if (inviterParty == null) {
            inviterParty = createParty(inviter);
        }

        if (!inviterParty.isLeader(inviter.getUniqueId())) {
            inviter.sendMessage(main.color("&c&l(!) &rOnly the party leader can invite players."));
            return;
        }

        if (getParty(target) != null) {
            inviter.sendMessage(main.color("&c&l(!) &rThat player is already in a party."));
            return;
        }

        Map<UUID, Long> targetInvites = invites.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>());
        targetInvites.put(inviter.getUniqueId(), System.currentTimeMillis() + INVITE_DURATION_MILLIS);

        inviter.sendMessage(main.color("&a&l(!) &rYou invited &e" + target.getName() + " &rto your party."));

        target.sendMessage(main.color("&e&l(!) &e" + inviter.getName() + " &rhas invited you to their party."));
        target.sendMessage(main.color("&7Type &e/party accept " + inviter.getName() + " &7to join."));
        target.sendMessage(main.color("&7This invite expires in &e60 seconds&7."));

        target.playSound(target.getLocation(), Sound.NOTE_PLING, 1.0F, 1.5F);
    }

    public void accept(Player player, Player inviter) {
        cleanupExpiredInvites();

        if (inviter == null || !inviter.isOnline()) {
            player.sendMessage(main.color("&c&l(!) &rThat player is not online anymore."));
            return;
        }

        Map<UUID, Long> playerInvites = invites.get(player.getUniqueId());

        if (playerInvites == null || !playerInvites.containsKey(inviter.getUniqueId())) {
            player.sendMessage(main.color("&c&l(!) &rYou do not have a party invite from that player."));
            return;
        }

        if (getParty(player) != null) {
            player.sendMessage(main.color("&c&l(!) &rYou are already in a party. Leave it first with &e/party leave&r."));
            return;
        }

        Party party = getParty(inviter);

        if (party == null) {
            party = createParty(inviter);
        }

        party.addMember(player.getUniqueId());
        partyByMember.put(player.getUniqueId(), party);

        playerInvites.remove(inviter.getUniqueId());

        broadcast(party, "&a&l(!) &e" + player.getName() + " &rhas joined the party.");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);

        Player partyLeader = Bukkit.getPlayer(party.getLeaderId());

        if (partyLeader != null && partyLeader.isOnline()) {
            main.getGameManager().sendAcceptedPartyMemberToLeaderGame(partyLeader, player);
        }
    }

    public void deny(Player player, Player inviter) {
        cleanupExpiredInvites();

        if (inviter == null) {
            player.sendMessage(main.color("&c&l(!) &rThat player is not online."));
            return;
        }

        Map<UUID, Long> playerInvites = invites.get(player.getUniqueId());

        if (playerInvites == null || !playerInvites.containsKey(inviter.getUniqueId())) {
            player.sendMessage(main.color("&c&l(!) &rYou do not have a party invite from that player."));
            return;
        }

        playerInvites.remove(inviter.getUniqueId());

        player.sendMessage(main.color("&c&l(!) &rYou denied &e" + inviter.getName() + "&r's party invite."));
        inviter.sendMessage(main.color("&c&l(!) &e" + player.getName() + " &rdenied your party invite."));
    }

    public void leave(Player player) {
        Party party = getParty(player);

        if (party == null) {
            player.sendMessage(main.color("&c&l(!) &rYou are not in a party."));
            return;
        }

        if (party.size() == 1) {
            disband(player, false);
            return;
        }

        if (party.isLeader(player.getUniqueId())) {
            party.removeMember(player.getUniqueId());
            partyByMember.remove(player.getUniqueId());
            partyChatToggled.remove(player.getUniqueId());

            Player newLeader = party.getOnlineMembers().isEmpty() ? null : party.getOnlineMembers().get(0);

            if (newLeader == null) {
                removeParty(party);
                return;
            }

            partyByLeader.remove(player.getUniqueId());
            party.setLeader(newLeader.getUniqueId());
            partyByLeader.put(newLeader.getUniqueId(), party);

            broadcast(party, "&e&l(!) &e" + player.getName() + " &rhas left the party. &e" + newLeader.getName() + " &ris now the party leader.");
        } else {
            party.removeMember(player.getUniqueId());
            partyByMember.remove(player.getUniqueId());
            partyChatToggled.remove(player.getUniqueId());

            broadcast(party, "&e&l(!) &e" + player.getName() + " &rhas left the party.");
            player.sendMessage(main.color("&e&l(!) &rYou left the party."));
        }
    }

    public void kick(Player leader, Player target) {
        Party party = getParty(leader);

        if (!requireLeader(leader, party)) {
            return;
        }

        if (target == null || !party.contains(target.getUniqueId())) {
            leader.sendMessage(main.color("&c&l(!) &rThat player is not in your party."));
            return;
        }

        if (leader.equals(target)) {
            leader.sendMessage(main.color("&c&l(!) &rYou cannot kick yourself. Use &e/party disband &ror &e/party leave&r."));
            return;
        }

        party.removeMember(target.getUniqueId());
        partyByMember.remove(target.getUniqueId());
        partyChatToggled.remove(target.getUniqueId());

        target.sendMessage(main.color("&c&l(!) &rYou were kicked from the party."));
        broadcast(party, "&c&l(!) &e" + target.getName() + " &rwas kicked from the party.");

        if (party.size() <= 1) {
            broadcast(party, "&e&l(!) &rThe party was disbanded because only one player was left.");
            removeParty(party);
        }
    }

    public void promote(Player leader, Player target) {
        Party party = getParty(leader);

        if (!requireLeader(leader, party)) {
            return;
        }

        if (target == null || !party.contains(target.getUniqueId())) {
            leader.sendMessage(main.color("&c&l(!) &rThat player is not in your party."));
            return;
        }

        if (leader.equals(target)) {
            leader.sendMessage(main.color("&c&l(!) &rYou are already the party leader."));
            return;
        }

        GameInstance leaderInstance = main.getGameManager().GetInstanceOfPlayer(leader);
        GameInstance targetInstance = main.getGameManager().GetInstanceOfPlayer(target);

        partyByLeader.remove(leader.getUniqueId());
        party.setLeader(target.getUniqueId());
        partyByLeader.put(target.getUniqueId(), party);

        if (leaderInstance != null && leaderInstance == targetInstance) {
            leaderInstance.transferPrivateGameLeader(leader, target);
        }

        broadcast(party, "&a&l(!) &e" + target.getName() + " &ris now the party leader.");
    }

    public void disband(Player leader, boolean requireLeader) {
        Party party = getParty(leader);

        if (party == null) {
            leader.sendMessage(main.color("&c&l(!) &rYou are not in a party."));
            return;
        }

        if (requireLeader && !party.isLeader(leader.getUniqueId())) {
            leader.sendMessage(main.color("&c&l(!) &rOnly the party leader can disband the party."));
            return;
        }

        broadcast(party, "&c&l(!) &rThe party has been disbanded.");
        removeParty(party);
    }

    public void list(Player player) {
        Party party = getParty(player);

        if (party == null) {
            player.sendMessage(main.color("&c&l(!) &rYou are not in a party."));
            return;
        }

        player.sendMessage(main.color("&8&m--------------------------------"));
        player.sendMessage(main.color("&e&lParty &7(" + party.size() + ")"));

        for (UUID uuid : party.getMemberIds()) {
            Player member = Bukkit.getPlayer(uuid);
            String name = member == null ? "Offline Player" : member.getName();
            String prefix = party.isLeader(uuid) ? "&6&lLEADER &r" : "&7MEMBER &r";

            player.sendMessage(main.color(prefix + "&e" + name));
        }

        player.sendMessage(main.color("&8&m--------------------------------"));
    }

    public void sendPartyMessage(Player sender, String message) {
        Party party = getParty(sender);

        if (party == null) {
            sender.sendMessage(main.color("&c&l(!) &rYou are not in a party."));
            return;
        }

        if (party.isPartyChatMuted() && !party.isLeader(sender.getUniqueId())) {
            sender.sendMessage(main.color("&c&l(!) &rParty chat is currently muted."));
            return;
        }

        String formatted = main.color("&d&lParty &8> &e" + sender.getName() + "&7: &f" + message);

        for (Player member : party.getOnlineMembers()) {
            member.sendMessage(formatted);
        }
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        invites.remove(player.getUniqueId());
        partyChatToggled.remove(player.getUniqueId());

        for (Map<UUID, Long> playerInvites : invites.values()) {
            playerInvites.remove(player.getUniqueId());
        }

        Party party = getParty(player);

        if (party == null) {
            return;
        }

        if (party.size() <= 1) {
            removeParty(party);
            return;
        }

        if (party.isLeader(player.getUniqueId())) {
            party.removeMember(player.getUniqueId());
            partyByMember.remove(player.getUniqueId());
            partyByLeader.remove(player.getUniqueId());

            Player newLeader = party.getOnlineMembers().isEmpty() ? null : party.getOnlineMembers().get(0);

            if (newLeader == null) {
                removeParty(party);
                return;
            }

            party.setLeader(newLeader.getUniqueId());
            partyByLeader.put(newLeader.getUniqueId(), party);

            broadcast(party, "&e&l(!) &e" + player.getName() + " &rleft the server. &e" + newLeader.getName() + " &ris now the party leader.");
        } else {
            party.removeMember(player.getUniqueId());
            partyByMember.remove(player.getUniqueId());

            broadcast(party, "&e&l(!) &e" + player.getName() + " &rleft the server and was removed from the party.");
        }
    }

    private Party createParty(Player leader) {
        Party party = new Party(leader.getUniqueId());

        partyByLeader.put(leader.getUniqueId(), party);
        partyByMember.put(leader.getUniqueId(), party);

        leader.sendMessage(main.color("&a&l(!) &rParty created. Invite players with &e/party invite <player>&r."));

        return party;
    }

    private boolean requireLeader(Player player, Party party) {
        if (party == null) {
            player.sendMessage(main.color("&c&l(!) &rYou are not in a party."));
            return false;
        }

        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(main.color("&c&l(!) &rOnly the party leader can do that."));
            return false;
        }

        return true;
    }

    private void broadcast(Party party, String message) {
        for (Player member : party.getOnlineMembers()) {
            member.sendMessage(main.color(message));
        }
    }

    private void removeParty(Party party) {
        if (party == null) {
            return;
        }

        partyByLeader.remove(party.getLeaderId());

        for (UUID uuid : party.getMemberIds()) {
            partyByMember.remove(uuid);
            partyChatToggled.remove(uuid);
        }
    }

    private void cleanupExpiredInvites() {
        long now = System.currentTimeMillis();
        HashSet<UUID> emptyInviteLists = new HashSet<>();

        for (Map.Entry<UUID, Map<UUID, Long>> entry : invites.entrySet()) {
            entry.getValue().entrySet().removeIf(invite -> invite.getValue() < now);

            if (entry.getValue().isEmpty()) {
                emptyInviteLists.add(entry.getKey());
            }
        }

        for (UUID uuid : emptyInviteLists) {
            invites.remove(uuid);
        }
    }
}