package anthony.SuperCraftBrawl.playerdata;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerDataManager implements Listener {

	private final Core main;
	private final DatabaseManager manager;
	private HashMap<Player, PlayerData> playerData = new HashMap<>();
	private final HashMap<Player, PermissionAttachment> perms = new HashMap<>();

	public PlayerDataManager(Core main) {
		this.main = main;
		this.manager = this.main.getDatabaseManager();
		main.getServer().getPluginManager().registerEvents(this, main);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		if (!(loadPlayer(event.getPlayer()))) {
			event.getPlayer().kickPlayer("Player Data has not been loaded! Please relog");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event) {
		unloadPlayer(event.getPlayer());
	}

	public PlayerData getPlayerData(Player player) {
		return playerData.get(player);
	}

	public PlayerData getOffPlayerData(OfflinePlayer player) {
		return playerData.get(player);
	}

	public boolean loadPlayer(Player player) {
		try {
			System.out.print("Loading data for " + player.getName());
			PlayerData data = getSavedData(player);
			if (data == null)
				data = loadNewData(player);

			playerData.put(player, data);

			PermissionAttachment a = player.addAttachment(main);
			perms.put(player, a);
			a.setPermission("scb." + data.getRank().toString().toLowerCase(), true);

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void unloadPlayer(Player player) {
		PlayerData data = playerData.remove(player);
		saveData(data);
		perms.remove(player);
	}

	public PlayerData getSavedData(Player player) throws SQLException {
		System.out.print("Getting saved data for " + player.getName());
		PlayerData data = null;
		Connection c = manager.getConnection();
		if (c.isClosed()) {
			System.out.println("Connection is closed!");
		}
		Statement stmt = c.createStatement();
		ResultSet set = null;
		try {
			set = stmt.executeQuery("SELECT * FROM PlayerData WHERE UUID = '" + player.getUniqueId().toString() + "'");
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (set.next()) {
			UUID uuid = UUID.fromString(set.getString("UUID"));
			String lastIp = player.getAddress().getAddress().getHostAddress();
			int roleID = set.getInt("RoleID");
			int tokens = set.getInt("Tokens");
			int wins = set.getInt("Wins");
			int kills = set.getInt("Kills");
			int deaths = set.getInt("Deaths");
			int flawlessWins = set.getInt("FlawlessWins");
			int losses = set.getInt("Losses");
			int winstreak = set.getInt("Winstreak");
			int cwm = set.getInt("Cwm");
			int melon = set.getInt("MelonCosmetic");
			int astronaut = set.getInt("AstronautCosmetic");
			int pm = set.getInt("PrivateMessages");
			int votes = set.getInt("Votes");
			int mysteryChests = set.getInt("MysteryChests");
			int blue = set.getInt("Blue");
			int red = set.getInt("Red");
			int green = set.getInt("Green");
			int yellow = set.getInt("Yellow");
			int muted = set.getInt("Muted");
			int exp = set.getInt("Exp");
			int level = set.getInt("Level");
			int bestTime = set.getInt("BestTime");
			int magicbroom = set.getInt("MagicBroom");
			int points = set.getInt("Points");
			int withersk = set.getInt("WitherSk");
			int bonusTokens = set.getInt("BonusTokens");
			int bonusLevels = set.getInt("BonusLevels");
			int paintball = set.getInt("Paintball");
			int santaoutfit = set.getInt("SantaOutfit");
			int elf = set.getInt("Elf");
			int killMsgs = set.getInt("KillMsgs");
			int gingerbreadman = set.getInt("GingerBreadMan");
			int challenge1 = set.getInt("Challenge1");
			int challenge2 = set.getInt("Challenge2");
			int challenge3 = set.getInt("Challenge3");
			int goldApple = set.getInt("GoldApple");
			int glowstone = set.getInt("Glowstone");
			int redstone = set.getInt("Redstone");
			int web = set.getInt("Web");
			int bottleEXP = set.getInt("BottleEXP");
			int broomWinEffect = set.getInt("BroomWinEffect");
			int enderDragonEffect = set.getInt("EnderDragonEffect");
			int santaEffect = set.getInt("SantaEffect");
			int fireParticlesEffect = set.getInt("FireParticlesEffect");
			int fishRainEffect = set.getInt("FishRainEffect");
			int challenge100 = set.getInt("Challenge100");
			int challenge101 = set.getInt("Challenge101");
			int challenge102 = set.getInt("Challenge102");
			int challenge103 = set.getInt("Challenge103");
			int matchMvps = set.getInt("MatchMvps");
			int fly = set.getInt("Fly");
			int totalcaught = set.getInt("TotalCaught");
			int caught = set.getInt("Caught");
			int rewardLevel = set.getInt("RewardLevel");
			int lureLevel = set.getInt("LureLevel");
			int lure = set.getInt("Lure");
			int friendshipLevel = set.getInt("FriendshipLevel");
			int friendship = set.getInt("Friendship");
			int bestWinstreak = set.getInt("BestWinstreak");
			int december15 = set.getInt("December15");
			int december16 = set.getInt("December16");
			int december17 = set.getInt("December17");
			int december18 = set.getInt("December18");
			int december19 = set.getInt("December19");
			int december20 = set.getInt("December20");
			int december21 = set.getInt("December21");
			int december22 = set.getInt("December22");
			int december23 = set.getInt("December23");
			int december24 = set.getInt("December24");
			int december25 = set.getInt("December25");
			int snowParticles = set.getInt("SnowParticles");
			int snowballDeathEffect = set.getInt("SnowballDeathEffect");
			int elfCosmetic = set.getInt("ElfCosmetic");

			data = new PlayerData(uuid, player.getName(), lastIp, roleID, tokens, wins, kills, deaths, flawlessWins,
					losses, winstreak, cwm, melon, astronaut, pm, votes, mysteryChests, blue, red, green, yellow, muted,
					exp, level, bestTime, magicbroom, points, withersk, bonusTokens, bonusLevels, paintball,
					santaoutfit, elf, gingerbreadman, killMsgs, challenge1, challenge2, challenge3, goldApple,
					glowstone, redstone, web, bottleEXP, broomWinEffect, enderDragonEffect, santaEffect,
					fireParticlesEffect, fishRainEffect, challenge100, challenge101, challenge102, challenge103,
					matchMvps, fly, totalcaught, caught, rewardLevel, lureLevel, lure, friendshipLevel, friendship,
					bestWinstreak, december15, december16, december17, december18, december19, december20, december21,
					december22, december23, december24, december25, snowParticles, snowballDeathEffect, elfCosmetic);
		}
		set.close();
		stmt.close();
		Statement classState = c.createStatement();
		ResultSet classSet = classState
				.executeQuery("SELECT * FROM PlayerClasses WHERE UUID = '" + player.getUniqueId().toString() + "'");
		while (classSet.next()) {
			int classID = classSet.getInt("ClassID");
			boolean purchased = classSet.getInt("Purchased") == 1;
			int timePurchased = classSet.getInt("TimePurchased");
			int gamesPlayed = classSet.getInt("GamesPlayed");
			int gamesWon = classSet.getInt("GamesWon");
			boolean reward1 = classSet.getInt("Reward1") == 1;
			boolean reward2 = classSet.getInt("Reward2") == 1;
			boolean reward3 = classSet.getInt("Reward3") == 1;
			boolean reward4 = classSet.getInt("Reward4") == 1;
			boolean reward5 = classSet.getInt("Reward5") == 1;
			data.playerClasses.put(classID,
					new ClassDetails(purchased, timePurchased, gamesPlayed, gamesWon, reward1, reward2, reward3, reward4, reward5));
		}
		classSet.close();

		Statement fishingState = c.createStatement();
		ResultSet fishingSet = fishingState
				.executeQuery("SELECT * FROM PlayerFishing WHERE UUID = '" + player.getUniqueId().toString() + "'");
		while (fishingSet.next()) {
			int fishID = fishingSet.getInt("FishID");
			int timesCaught = fishingSet.getInt("TimesCaught");
			data.playerFishing.put(fishID, new FishingDetails(timesCaught));
		}
		fishingSet.close();

		Statement favClassesState = c.createStatement();
		ResultSet favClassesSet = favClassesState.executeQuery(
				"SELECT * FROM PlayerCustomIntegers WHERE UUID = '" + player.getUniqueId().toString() + "'");

		while (favClassesSet.next()) {
			int classID = favClassesSet.getInt("CustomInteger");
			data.customIntegers.add(classID);
		}

		favClassesSet.close();
		favClassesState.close();

		return data;
	}

	public PlayerData loadNewData(Player player) {
		System.out.print("Loading new saved data for " + player.getName());
		String lastIp = player.getAddress().getAddress().getHostAddress();
		PlayerData newData = new PlayerData(player.getUniqueId(), player.getName(), lastIp);

		manager.executeUpdateCommand("INSERT INTO PlayerData (`UUID`, `LastPlayerName`, `LastIP`) VALUES ('"
				+ player.getUniqueId().toString() + "', '" + player.getName() + "', '" + lastIp + "');");

		return newData;
	}

	public void resetPoints() {
		manager.executeUpdateCommand("UPDATE PlayerData SET Points = 0");
	}

	public void saveData(PlayerData data) {
		System.out.print("Saving data for " + data.playerName);
		manager.executeUpdateCommand("UPDATE PlayerData SET LastPlayerName = '" + data.playerName + "', LastIP = '"
				+ data.playerIP + "', RoleID = " + data.roleID + ", Tokens = " + data.tokens + ", Kills = " + data.kills
				+ ", MatchMvps = " + data.matchMvps + ", Challenge100 = " + data.challenge100 + ", Challenge101 = "
				+ data.challenge101 + ", Challenge102 = " + data.challenge102 + ", Challenge103 = " + data.challenge103
				+ ", EnderDragonEffect = " + data.enderDragonEffect + ", SantaEffect = " + data.santaEffect
				+ ", FireParticlesEffect = " + data.fireParticlesEffect + ", Losses = " + data.losses + ", Votes = "
				+ data.votes + ", FlawlessWins = " + data.flawlessWins + ", BonusTokens = " + data.bonusTokens
				+ ", BonusLevels = " + data.bonusLevels + ", WitherSk = " + data.withersk + ", Points = " + data.points
				+ ", MagicBroom = " + data.magicbroom + ", Cwm = " + data.cwm + ", Blue = " + data.blue + ", Red = "
				+ data.red + ", Green = " + data.green + ", Yellow = " + data.yellow + ", MelonCosmetic = " + data.melon
				+ ", PrivateMessages = " + data.pm + ", Muted = " + data.muted + ", GoldApple = " + data.goldApple
				+ ", Fly = " + data.fly + ", Glowstone = " + data.glowstone + ", Redstone = " + data.redstone
				+ ", Web = " + data.web + ", BottleEXP = " + data.bottleEXP + ", MysteryChests = " + data.mysteryChests
				+ ", AstronautCosmetic = " + data.astronaut + ", SantaOutfit = " + data.santaoutfit
				+ ", BestWinstreak = " + data.bestWinstreak + ", BroomWinEffect = " + data.broomWinEffect
				+ ", BestTime = " + data.bestTime + ", Exp = " + data.exp + ", Winstreak = " + data.winstreak
				+ ", ElfCosmetic = " + data.elfCosmetic + ", SnowballDeathEffect = " + data.snowballDeathEffect
				+ ", GingerBreadMan = " + data.gingerbreadman + ", Elf = " + data.elf + ", Challenge1 = "
				+ data.challenge1 + ", Challenge2 = " + data.challenge2 + ", Challenge3 = " + data.challenge3
				+ ", December15 = " + data.december15 + ", December16 = " + data.december16 + ", December17 = "
				+ data.december17 + ", December18 = " + data.december18 + ", December19 = " + data.december19
				+ ", December20 = " + data.december20 + ", December21 = " + data.december21 + ", December22 = "
				+ data.december22 + ", December23 = " + data.december23 + ", December24 = " + data.december24
				+ ", SnowParticles = " + data.snowParticles + ", December25 = " + data.december25 + ", KillMsgs = "
				+ data.killMsgs + ", Level = " + data.level + ", Deaths = " + data.deaths + ", Paintball = "
				+ data.paintball + ", Wins = " + data.wins + ", TotalCaught = " + data.totalcaught + ", Caught = "
				+ data.caught + ", RewardLevel = " + data.rewardLevel + ", LureLevel = " + data.lureLevel + ", Lure = "
				+ data.lure + ", FriendshipLevel = " + data.friendshipLevel + ", Friendship = " + data.friendship
				+ ", FishRainEffect = " + data.fishRainEffect + " WHERE UUID = '" + data.playerUUID.toString() + "';");
		String updateCMD = "INSERT INTO PlayerClasses (UUID, ClassID, TimePurchased, Purchased, GamesPlayed, GamesWon,"
				+ "Reward1, Reward2) VALUES ";
		int index = 0;

		for (Entry<Integer, ClassDetails> entry : data.playerClasses.entrySet()) {
			if (entry.getValue().hasUpdated) {
				if (index != 0)
					updateCMD += ", ";
				updateCMD += "('" + data.playerUUID.toString() + "', " + entry.getKey() + ", "
						+ entry.getValue().timePurchased + ", " + (entry.getValue().purchased ? 1 : 0) + ", "
						+ entry.getValue().gamesPlayed + ", " + entry.getValue().gamesWon + ", "
						+ (entry.getValue().reward1 ? 1 : 0) + ", " + (entry.getValue().reward2 ? 1 : 0) + ")";
				index++;
			}
		}

		if (index > 0) {
			updateCMD += " ON DUPLICATE KEY UPDATE TimePurchased = VALUES (TimePurchased), Purchased = VALUES (Purchased), GamesPlayed = VALUES (GamesPlayed),"
					+ "GamesWon = VALUES (GamesWon), Reward1 = VALUES (Reward1), Reward2 = VALUES (Reward2);";
			System.out.print("Executing " + updateCMD);
			manager.executeUpdateCommand(updateCMD);
		}

		updateCMD = "INSERT INTO PlayerFishing (UUID, FishID, TimesCaught) VALUES ";
		index = 0;

		for (Entry<Integer, FishingDetails> entry : data.playerFishing.entrySet()) {
			if (entry.getValue().hasUpdated) {
				if (index != 0)
					updateCMD += ", ";
				updateCMD += "('" + data.playerUUID.toString() + "', " + entry.getKey() + ", "
						+ entry.getValue().timesCaught + ")";
				index++;
			}
		}

		if (index > 0) {
			updateCMD += " ON DUPLICATE KEY UPDATE TimesCaught = VALUES (TimesCaught);";
			System.out.print("Executing " + updateCMD);
			manager.executeUpdateCommand(updateCMD);
		}

		saveCustomIntegersToDatabase(data);
	}

	private void saveCustomIntegersToDatabase(PlayerData data) {
		String s = "DELETE FROM PlayerCustomIntegers WHERE UUID = '" + data.playerUUID.toString() + "'";
		StringBuilder updateCMD = new StringBuilder("INSERT INTO PlayerCustomIntegers (UUID, CustomInteger) VALUES ");
		int index = 0;

		for (Integer customInteger : data.customIntegers) {
			if (index != 0)
				updateCMD.append(", ");
			updateCMD.append("('").append(data.playerUUID.toString()).append("', ").append(customInteger).append(")");
			index++;
		}

		if (index > 0) {
			updateCMD.append(" ON DUPLICATE KEY UPDATE CustomInteger = VALUES (CustomInteger);");
			System.out.print("Executing " + updateCMD);
			manager.multiExecuteUpdateCommand(s, updateCMD.toString());
		} else {
			manager.multiExecuteUpdateCommand(s);
		}
	}

}
