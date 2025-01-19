package anthony.SuperCraftBrawl.party;

import java.util.List;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class PartyManager {

	private Player leader;
	private List<Player> members;

	public PartyManager(Player leader) {
		this.leader = leader;
	}

	// This returns the leader of the player's party
	public Player getLeader() {
		return this.leader;
	}

	// Adds players to the party
	public void addMember(Player member) {
		leader.sendMessage(color("&e&l(!) &e" + member.getName() + " &2has joined the party"));
		this.members.add(member);
	}

	public void removeMember(Player member) {
		leader.sendMessage(color("&e&l(!) &e" + member.getName() + " &chas left the party"));
		this.members.remove(member);
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
