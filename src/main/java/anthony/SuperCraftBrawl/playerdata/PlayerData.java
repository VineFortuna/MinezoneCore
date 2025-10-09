package anthony.SuperCraftBrawl.playerdata;

import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ranks.Rank;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

public class PlayerData {
	public UUID playerUUID;

	public String playerName, playerIP;

	public int roleID = 0, tokens = 0, wins = 0, kills = 0, deaths = 0, flawlessWins = 0, losses = 0, winstreak = 0,
			votes = 0, mysteryChests = 0, cwm = 0, melon = 0, astronaut = 0, pm = 0, blue = 0, red = 0, green = 0,
			yellow = 0, muted = 0, exp = 0, level = 0, bestTime = 0, magicbroom = 0, points = 0, withersk = 0,
			bonusTokens = 0, bonusLevels = 0, paintball = 0, santaoutfit, elf, gingerbreadman, killMsgs = 0,
			challenge1 = 0, challenge2 = 0, challenge3 = 0, goldApple = 0, glowstone = 0, redstone = 0, web = 0,
			bottleEXP = 0, broomWinEffect = 0, enderDragonEffect = 0, santaEffect = 0, fireParticlesEffect = 0,
			fishRainEffect = 0, challenge100 = 0, challenge101 = 0, challenge102 = 0, challenge103 = 0, matchMvps = 0,
			fly = 0, totalcaught = 0, caught = 0, rewardLevel = 0, lureLevel = 0, lure = 0, friendshipLevel = 0,
			friendship = 0, bestWinstreak = 0, december15 = 0, december16 = 0, december17 = 0, december18 = 0,
			december19 = 0, december20 = 0, december21 = 0, december22 = 0, december23 = 0, december24 = 0,
			december25 = 0, snowParticles = 0, snowballDeathEffect = 0, elfCosmetic = 0, snowmanPet = 0,
			candycaneParticles = 0, snowball = 0, floodEffect = 0, treasureEffect = 0, treasureOpened = 0,
			pumpkinPie = 0, ritualEffect = 0;
	public String color = "", fishingWarps = "", treasureLoc = "";

	public HashMap<Integer, ClassDetails> playerClasses = new HashMap<>();
	public HashMap<Integer, FishingDetails> playerFishing = new HashMap<>();
	public HashMap<Integer, ParkourDetails> playerParkour = new HashMap<>();
	public ArrayList<Integer> customIntegers = new ArrayList<>();

	public PlayerData(UUID playerUUID, String playerName, String playerIP, int roleID, int tokens, int wins, int kills,
			int deaths, int flawlessWins, int losses, int winstreak, int cwm, int melon, int astronaut, int pm,
			int votes, int mysteryChests, int blue, int red, int green, int yellow, int muted, int exp, int level,
			int bestTime, int magicbroom, int points, int withersk, int bonusTokens, int bonusLevels, int paintball,
			int santaoutfit, int elf, int gingerbreadman, int killMsgs, int challenge1, int challenge2, int challenge3,
			int goldApple, int glowstone, int redstone, int web, int bottleEXP, int broomWWinEffect,
			int enderDragonEffect, int santaEffect, int fireParticlesEffect, int fishRainEffect, int challenge100,
			int challenge101, int challenge102, int challenge103, int matchMvps, int fly, int totalcaught, int caught,
			int rewardLevel, int lureLevel, int lure, int friendshipLevel, int friendship, int bestWinstreak,
			int december15, int december16, int december17, int december18, int december19, int december20,
			int december21, int december22, int december23, int december24, int december25, int snowParticles,
			int snowballDeathEffect, int elfCosmetic, int snowmanPet, int candycaneParticles, int snowball,
			int floodEffect, int treasureEffect, int treasureOpened, String color, String fishingWarps,
			String treasureLoc, int pumpkinPie, int ritualEffect) {
		this(playerUUID, playerName, playerIP);
		this.roleID = roleID;
		this.tokens = tokens;
		this.wins = wins;
		this.kills = kills;
		this.deaths = deaths;
		this.flawlessWins = flawlessWins;
		this.losses = losses;
		this.winstreak = winstreak;
		this.cwm = cwm;
		this.melon = melon;
		this.astronaut = astronaut;
		this.pm = pm;
		this.votes = votes;
		this.mysteryChests = mysteryChests;
		this.blue = blue;
		this.red = red;
		this.green = green;
		this.yellow = yellow;
		this.muted = muted;
		this.exp = exp;
		this.level = level;
		this.bestTime = bestTime;
		this.magicbroom = magicbroom;
		this.points = points;
		this.withersk = withersk;
		this.bonusTokens = bonusTokens;
		this.bonusLevels = bonusLevels;
		this.paintball = paintball;
		this.santaoutfit = santaoutfit;
		this.elf = elf;
		this.gingerbreadman = gingerbreadman;
		this.killMsgs = killMsgs;
		this.challenge1 = challenge1;
		this.challenge2 = challenge2;
		this.challenge3 = challenge3;
		this.goldApple = goldApple;
		this.glowstone = glowstone;
		this.redstone = redstone;
		this.web = web;
		this.bottleEXP = bottleEXP;
		this.broomWinEffect = broomWWinEffect;
		this.enderDragonEffect = enderDragonEffect;
		this.santaEffect = santaEffect;
		this.fireParticlesEffect = fireParticlesEffect;
		this.fishRainEffect = fishRainEffect;
		this.challenge100 = challenge100;
		this.challenge101 = challenge101;
		this.challenge102 = challenge102;
		this.challenge103 = challenge103;
		this.matchMvps = matchMvps;
		this.fly = fly;
		this.totalcaught = totalcaught;
		this.caught = caught;
		this.rewardLevel = rewardLevel;
		this.lureLevel = lureLevel;
		this.lure = lure;
		this.friendshipLevel = friendshipLevel;
		this.friendship = friendship;
		this.bestWinstreak = bestWinstreak;
		this.december15 = december15;
		this.december16 = december16;
		this.december17 = december17;
		this.december18 = december18;
		this.december19 = december19;
		this.december20 = december20;
		this.december21 = december21;
		this.december22 = december22;
		this.december23 = december23;
		this.december24 = december24;
		this.december25 = december25;
		this.snowParticles = snowParticles;
		this.snowballDeathEffect = snowballDeathEffect;
		this.elfCosmetic = elfCosmetic;
		this.snowmanPet = snowmanPet;
		this.candycaneParticles = candycaneParticles;
		this.snowball = snowball;
		this.floodEffect = floodEffect;
		this.treasureEffect = treasureEffect;
		this.treasureOpened = treasureOpened;
		this.color = color;
		this.fishingWarps = (fishingWarps == null || fishingWarps.equals("null")) ? "" : fishingWarps;
		this.treasureLoc = (treasureLoc == null || treasureLoc.equals("null")) ? "" : treasureLoc;
		this.pumpkinPie = pumpkinPie;
		this.ritualEffect = ritualEffect;
	}

	public boolean isPurchased(ClassType type) {
		return this.playerClasses.get(type.getID()) != null && this.playerClasses.get(type.getID()).purchased
				|| type.getTokenCost() == 0;
	}

	public PlayerData(UUID playerUUID, String playerName, String playerIP) {
		this.playerUUID = playerUUID;
		this.playerName = playerName;
		this.playerIP = playerIP;
	}

	public Rank getRank() {
		return Rank.getRankFromID(roleID);
	}

	public List<Integer> getFishingWarps() {
		if (this.fishingWarps == null || this.fishingWarps.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.stream(this.fishingWarps.split(",")).map(Integer::parseInt).collect(Collectors.toList());
	}

	public void addFishingWarp(int i) {
		if (getFishingWarps() == null || getFishingWarps().isEmpty()) {
			this.fishingWarps = Integer.toString(i);
		} else if (!getFishingWarps().contains(i)) {
			this.fishingWarps += "," + i;
		}
	}

	public String checkPlayerLevel(Player player, PlayerData data) {
		String color = "&7";
		String rainbowPrestige = "";
		String[] rainbow = { "&c", "&6", "&e", "&a", "&b", "&d", "&5" };

		if (data.level >= 25)
			color = "&f";
		if (data.level >= 50)
			color = "&6";
		if (data.level >= 75)
			color = "&b";
		if (data.level >= 100)
			color = "&2";
		if (data.level >= 125)
			color = "&3";
		if (data.level >= 150)
			color = "&4";
		if (data.level >= 175)
			color = "&d";
		if (data.level >= 200)
			color = "&9";
		if (data.level >= 225)
			color = "&5";
		if (data.level >= 250) {
			String[] rainbowString = ("[" + data.level + "✫]").split("");
			for (int i = 0; i < rainbowString.length; i++) {
				rainbowPrestige += rainbow[i] + rainbowString[i];
			}
		}

		return color;
	}
}
