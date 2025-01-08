package anthony.SuperCraftBrawl.ranks;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;

public class RankManager {
	
	private final Core main;
	
	public RankManager(Core main) {
		this.main = main;
	}
	
	public void setRank(Player player, Rank rank) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		data.roleID = rank.getRoleID();
		updateRank(player, data);
	}
	
	public Rank getRank(Player player) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		
		if (data != null)
			return Rank.getRankFromID(data.roleID);
		else
			return null;
	}
	
	public void updateRank(Player player, PlayerData data) {
		main.getListener().setPlayerOnTablist(player);
		main.sendScoreboardUpdate(player); // This sets the rank next to player name above their head
		main.getScoreboardManager().lobbyBoard(player);
		main.getDataManager().reloadPerms(player, data);
	}
	
}
