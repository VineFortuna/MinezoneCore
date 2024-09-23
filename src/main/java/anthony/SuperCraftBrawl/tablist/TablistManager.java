package anthony.SuperCraftBrawl.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;

public class TablistManager {

	private Core main;
	public ScoreboardManager scoreManager = Bukkit.getScoreboardManager();
	public Scoreboard c;	
	private Team captain;
	private Team owner;
	
	public TablistManager(Core main) {
		this.main = main;
		this.c = scoreManager.getNewScoreboard();
		registerTeams();
	}
	
	private void registerTeams() {
		this.captain = c.registerNewTeam("b_captain");
		this.owner = c.registerNewTeam("a_owner");
		setTeamPrefix();
	}
	
	private void setTeamPrefix() {
		this.captain.setPrefix(Rank.CAPTAIN.getTagWithSpace());
		this.owner.setPrefix(Rank.OWNER.getTagWithSpace());
	}
	
	@SuppressWarnings("deprecation")
	public void setPlayerTeam(Player player) {
		if (main.getRankManager().getRank(player) == Rank.OWNER)
			this.owner.addPlayer(player);
		else if (main.getRankManager().getRank(player) == Rank.CAPTAIN)
			this.captain.addPlayer(player);
		
		player.setScoreboard(c);
	}
	
}
