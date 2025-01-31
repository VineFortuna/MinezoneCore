package anthony.SuperCraftBrawl.commands;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.gui.GameStatsGUI;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ChatColorHelper;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.apache.commons.lang.WordUtils;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class Commands implements CommandExecutor, TabCompleter {

	private final Core main;
	public List<Player> players;

	public Commands(Core main) {
		this.main = main;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			switch (cmd.getName().toLowerCase()) {
			case "purchases":
				purchaseCommand(args, player);
				break;

			case "startgame":
				startGameCommand(player);
				break;

			case "fly":
			case "f":
				flyCommand(player);
				break;

			case "items":
				itemsCommand(args, player);
				break;

			case "setlives":
				setLivesCommand(args, player);
				break;

			case "gamestats":
				gameStatsCommand(args, player);
				break;

			case "maps":
				mapsCommand(args, player);
				break;

			case "join":
				joinCommand(args, player);
				break;

			case "spectate":
				spectateCommand(args, player);
				break;

			case "class":
				classCommand(args, player);
				break;

			case "leave":
			case "l":
				this.leaveGame(player);
				break;

			case "players":
				playersCommand(player);
				break;
			case "party":
				partyCommand(args, player);
				break;
			case "color":
				colorCommand(args, player);
				break;
			case "lactate":
				lactateCommand(player);
				break;
			}
		} else
			sender.sendMessage("Hey! You can't use this in the terminal!");
		return true;
	}

	private void lactateCommand(Player player) {
		if (player.hasPermission("scb.lactate")) {
			Location loc = player.getLocation();
			loc.setY(loc.getY() + 0.7);
			player.sendMessage(main.color("&r&l(!) &rYou have &r&lLACTATED!"));
			player.getWorld().playSound(loc, Sound.COW_HURT, 1, 1);
			sendParticle(player, loc, EnumParticle.SNOWBALL, 300, 0.8f, 0.0f, -0.3f, 0.0f);
		} else {
			player.sendMessage(main.color("&c&l(!) &rYou need the rank &5&lSUPREME &rto use this command!"));
		}
	}

	public static void sendParticle(Player player, Location location, EnumParticle particle, int amount, float speed,
			float offsetX, float offsetY, float offsetZ) {
		PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(particle, true, (float) location.getX(),
				(float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, amount);

		for (Player players : Bukkit.getOnlinePlayers())
			((CraftPlayer) players).getHandle().playerConnection.sendPacket(particles);
	}

	public void colorCommand(String[] args, Player player) {
		List<ChatColor> colors = Arrays.asList(
				ChatColor.WHITE,
				ChatColor.YELLOW, ChatColor.GOLD,
				ChatColor.GREEN, ChatColor.DARK_GREEN,
				ChatColor.AQUA, ChatColor.DARK_AQUA,
				ChatColor.BLUE, ChatColor.DARK_BLUE,
				ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE,
				ChatColor.RED, ChatColor.DARK_RED,
				ChatColor.GRAY, ChatColor.DARK_GRAY,
				ChatColor.BLACK,
				ChatColor.RESET
		);

		PlayerData data = main.getDataManager().getPlayerData(player);

		if (player.hasPermission("scb.color")) {
			if (data != null) {
				try {
					if (args.length == 0 || !colors.contains(ChatColor.valueOf(args[0].toUpperCase()))) {
						colorMessage(player, colors);
					} else if (args[0].equalsIgnoreCase("reset")) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Changed your prefix to "
								+ ChatColor.RESET + player.getName());
						data.color = "";
					} else {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Changed your prefix to "
								+ ChatColor.valueOf(args[0].toUpperCase()) + player.getName());
						data.color = args[0].toUpperCase();
					}
				} catch (IllegalArgumentException e) {
					colorMessage(player, colors);
				}
			}
		} else {
			player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You need the rank " + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN " + ChatColor.RESET
					+ "to use this command");
		}
	}

	private void colorMessage(Player player, List<ChatColor> colors) {
		player.sendMessage(main.color("&f&l----------------------------------------"));
		player.sendMessage(main.color("&r&l(!) &rClick on a color below to select it:"));
		TextComponent[] colorText = new TextComponent[colors.size()-1];
		int i = 0;
		for (ChatColor color : colors) {
			if (color != ChatColor.RESET) {
				TextComponent message = new TextComponent(WordUtils.capitalizeFully(color.name().replace('_', ' ')));
				if (i < colorText.length)
					message.addExtra(ChatColor.GRAY + ", ");
				message.setColor(color.asBungee());
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/color " + color.name()));
				colorText[i] = message;
				i++;
			}
		}
		player.spigot().sendMessage(colorText);
		TextComponent message = new TextComponent("Click here to reset color");
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/color RESET"));
		player.spigot().sendMessage(message);
		player.sendMessage(main.color("&f&l----------------------------------------"));
	}

	private void partyCommand(String[] args, Player player) {
		if (args.length == 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing:"));
			player.sendMessage(main.color(ChatColor.STRIKETHROUGH + "&7-------------------------------------"));
			player.sendMessage(main.color("&e&lPARTY COMMANDS:"));
			player.sendMessage(main.color("&e/party invite <player> &7-> &bInvites a player to your party"));
			player.sendMessage(main.color("&e/party accept <player> &7-> &bAccepts a party invite from a player"));
		}
	}

	private void purchaseCommand(String[] args, Player player) {
		if (!player.hasPermission("scb.purchases")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		if (args.length == 0) {
			player.sendMessage("Not enough arguments!");
			return;
		}

		switch (args[0].toLowerCase()) {
		case "get":
			purchaseGetCommand(player);
			break;
		case "buy":
			if (args.length == 1) {
				player.sendMessage("Not enough arguments");
				return;
			}

			purchaseBuyCommand(args, player);
			break;
		}
	}

	private void purchaseGetCommand(Player player) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		player.sendMessage("Listing purchases...");

		int size = 0;
		for (Entry<Integer, ClassDetails> entry : data.playerClasses.entrySet()) {
			player.sendMessage(" - " + entry.getKey() + ": " + entry.getValue().toString());
			size++;
		}

		if (size == 0) {
			player.sendMessage("You have no Class Stats");
		}
	}

	private void purchaseBuyCommand(String[] args, Player player) {
		int classID = Integer.parseInt(args[1]);
		PlayerData playerData = main.getDataManager().getPlayerData(player);
		ClassDetails details = playerData.playerClasses.get(classID);

		if (details == null) {
			details = new ClassDetails();
			playerData.playerClasses.put(classID, details);
		}
		details.setPurchased();
		player.sendMessage("Class Purchased!");
	}

	private void startGameCommand(Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
			return;
		}

		if (!player.hasPermission("scb.startGame")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		if (game.state != GameState.WAITING) {
			if (game.state == GameState.STARTED)
				player.sendMessage(main.color("&c&l(!) &rGame is already in progress!"));
			else if (game.state == GameState.ENDED)
				player.sendMessage(main.color("&c&l(!) &rGame has already ended!"));
			return;
		}

		if (game.players.size() == 0) {
			player.sendMessage(main.color("&c&l(!) &rNot enough players to start!"));
			return;
		}

		game.getGameSettings().forceStartGame(true);
	}

	private void flyCommand(Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
		PlayerData flyData = main.getDataManager().getPlayerData(player);

		if (game != null) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot use this in game!"));
			return;
		}

		if (!player.hasPermission("scb.fly")) {
			player.sendMessage(main.color("&c&l(!) &rYou need the rank &9&lCAPTAIN &rto use this command!"));
			return;
		}

		if (flyData != null) {
			resetDoubleJump(player);
			if (flyData.fly == 0) {
				player.sendMessage(main.color("&e&l(!) &rYou have enabled flight!"));
				flyData.fly = 1;
			} else {
				player.sendMessage(main.color("&e&l(!) &rYou have disabled flight!"));
				flyData.fly = 0;
			}
			main.getDataManager().saveData(flyData); // Save even when server restarts
		}
	}

	private void itemsCommand(String[] args, Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		if (args.length != 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/items"));
			return;
		}

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game"));
			return;
		}

		if (!player.hasPermission("scb.items")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		for (ItemStack itemStack : game.allItemDrops) {
			player.getInventory().addItem(itemStack);
		}
	}

	private void setLivesCommand(String[] args, Player player) {
		if (args.length < 2) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/setlives <player> <num>"));
			return;
		}

		int num;

		try {
			num = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			player.sendMessage(main.color("&r&l(!) &rPlease specify number of lives"));
			return;
		}

		if (num <= 0) {
			player.sendMessage(main.color("&c&l(!) &rNumber of lives must be greater than zero!"));
			return;
		}

		Player target = Bukkit.getServer().getPlayerExact(args[0]);

		if (target == null) {
			player.sendMessage(main.color("&c&l(!) &rPlease specify a player!"));
			return;
		}

		GameInstance game = main.getGameManager().GetInstanceOfPlayer(target);

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rThis player is not in a game!"));
			return;
		}

		if (game.state != GameState.STARTED) {
			player.sendMessage(main.color("&c&l(!) &rGame must be started to use!"));
			return;
		}

		BaseClass baseClass = game.classes.get(target);

		if (baseClass == null) {
			player.sendMessage(main.color("&c&l(!) &rTarget player doesn't have a class assigned."));
			return;
		}

		if (!player.hasPermission("scb.setlives")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		baseClass.lives = num;
		player.sendMessage(main.color("&2&l(!) &rYou set &e" + target.getName() + "&r's lives to &e" + num));
		baseClass.score.setScore(baseClass.lives);
	}

	private void gameStatsCommand(String[] args, Player player) {
		GameInstance currentGame = main.getGameManager().GetInstanceOfPlayer(player);

		// Player is in a game
		if (currentGame != null && currentGame.state == GameState.STARTED) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot use this in a game!"));
			return;
		}

		GameInstance i = main.gameStats.get(player);
		// No stats available
		if (i == null) {
			player.sendMessage(main.color("&c&l(!) &rThis game's stats have expired or are unavailable."));
			return;
		}

		// Game in progress
		if (i.state != GameState.ENDED) {
			player.sendMessage(main.color("&c&l(!) &rThis game is still in progress. No stats available."));
			return;
		}

		// Open stats
		try {
			new GameStatsGUI(main, i).inv.open(player);
		} catch (NullPointerException ex) {
			player.sendMessage(main.color("&c&l(!) &rThis game's stats cannot be viewed. Did a player leave early?"));
		}
	}

	private void mapsCommand(String[] args, Player player) {
		if (args.length != 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/maps"));
			return;
		}

		// Send the message to the player
		player.sendMessage(ChatColorHelper.color(createMapsString()));
	}

	private String createMapsString() {
		StringBuilder stringBuilder = new StringBuilder();
		String HEADERCOLOR = "&e&l";

		List<Maps> classicCuratedMaps = Maps.filterMaps(GameType.CLASSIC, Maps.Category.CURATED, null, null);
		List<Maps> duelCuratedMaps = Maps.filterMaps(GameType.DUEL, Maps.Category.CURATED, null, null);
		List<Maps> curatedMaps = new ArrayList<>(classicCuratedMaps);
		curatedMaps.addAll(duelCuratedMaps);

		List<Maps> classicCasualMaps = Maps.filterMaps(GameType.CLASSIC, Maps.Category.CASUAL, null, null);
		List<Maps> duelCasualMaps = Maps.filterMaps(GameType.DUEL, Maps.Category.CASUAL, null, null);
		List<Maps> casualMaps = new ArrayList<>(classicCasualMaps);
		casualMaps.addAll(duelCasualMaps);

		List<Maps> classicVaultedMaps = Maps.filterMaps(GameType.CLASSIC, Maps.Category.VAULTED, null, null);
		List<Maps> duelVaultedMaps = Maps.filterMaps(GameType.DUEL, Maps.Category.VAULTED, null, null);
		List<Maps> vaultedMaps = new ArrayList<>(classicVaultedMaps);
		vaultedMaps.addAll(duelVaultedMaps);

		stringBuilder.append("&f&l----------------------------------------");
		stringBuilder.append(HEADERCOLOR).append("\nCURATED MAPS: (").append(curatedMaps.size()).append(")\n");

		// Get Classic maps for Curated category
		appendMaps(GameType.CLASSIC, Maps.Category.CURATED, stringBuilder);

		// Get Duels maps for Curated category
		appendMaps(GameType.DUEL, Maps.Category.CURATED, stringBuilder);

		// Now for Casual Maps section
		stringBuilder.append(HEADERCOLOR).append(" \nCASUAL MAPS: (").append(casualMaps.size()).append(")\n");

		// Get Classic maps for Casual category
		appendMaps(GameType.CLASSIC, Maps.Category.CASUAL, stringBuilder);

		// Get Duels maps for Casual category
		appendMaps(GameType.DUEL, Maps.Category.CASUAL, stringBuilder);

		// Now for Vaulted Maps section
		stringBuilder.append(HEADERCOLOR).append(" \nVAULTED MAPS: (").append(vaultedMaps.size()).append(")\n");

		// Get Classic maps for Vaulted category
		appendMaps(GameType.CLASSIC, Maps.Category.VAULTED, stringBuilder);

		// Get Duels maps for Vaulted category
		appendMaps(GameType.DUEL, Maps.Category.VAULTED, stringBuilder);

		stringBuilder.append("&f&l----------------------------------------");

		return stringBuilder.toString();
	}

	private void appendMaps(GameType gameMode, Maps.Category category, StringBuilder stringBuilder) {
		List<Maps> maps = Maps.filterMaps(gameMode, category, null, null);
		Maps.sortMaps(maps, Maps.Sorter.ALPHABETICAL);

		appendColorAndComma(stringBuilder, maps, gameMode);
	}

	private void appendColorAndComma(StringBuilder stringBuilder, List<Maps> mapsList, GameType gameMode) {
		String color = "";

		if (gameMode == GameType.CLASSIC) {
			color = "&a";
		} else if (gameMode == GameType.DUEL)
			color = "&b";

		for (int i = 0; i < mapsList.size(); i++) {
			stringBuilder.append(color).append(mapsList.get(i).getName());

			if (i < mapsList.size() - 1) {
				stringBuilder.append("&7, ");
			}
		}
		stringBuilder.append("\n"); // Newline after each map group
	}

	private void joinCommand(String[] args, Player player) {
		if (args.length == 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/join <map>"));
			return;
		}

		String mapName = args[0];
		GameInstance gameSpectator = main.getGameManager().GetInstanceOfSpectator(player);

		if (gameSpectator != null) {
			player.sendMessage(main.color("&c&l(!) &rYou are currently spectating a game!"));
			return;
		}

		Maps map = null;

		for (Maps maps : Maps.values()) {
			if (maps.toString().equalsIgnoreCase(mapName)) {
				map = maps;
				break;
			}
		}

		if (map == null) {
			player.sendMessage(main.color("&c&l(!) &rThis map does not exist! Use &e/maps &rfor a list of maps"));
			return;
		}

		main.getGameManager().JoinMap(player, map);
	}

	private void spectateCommand(String[] args, Player player) {
		if (args.length == 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/spectate <map>"));
			return;
		}

		String mapName = args[0];
		Maps map = null;

		for (Maps maps : Maps.values()) {
			if (maps.toString().equalsIgnoreCase(mapName)) {
				map = maps;
				break;
			}
		}

		if (map == null) {
			player.sendMessage(main.color("&c&l(!) &rThis map does not exist! Use &e/maps &rfor a list of maps"));
			return;
		}

		main.getGameManager().SpectatorJoinMap(player, map);
	}

	private void playersCommand(Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
			return;
		}

		String players = "";
		for (Player gamePlayer : game.players) {
			if (!players.isEmpty()) {
				players += ", ";
			}

			players += gamePlayer.getName() + "";
		}

		player.sendMessage(main.color("&l(!) &aPlayers in your game (" + game.players.size() + "): "));
		player.sendMessage(main.color("l--> " + players));
	}

	private void classCommand(String[] args, Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
		PlayerData playerData = main.getDataManager().getPlayerData(player);

		if (args.length == 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/class <classname>"));
			return;
		}

		String className = args[0];
		if (className.equalsIgnoreCase("random")) {
			selectRandomClass(player, playerData);
			player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
			return;
		}

		ClassType selectedClassType = getClassType(className);
		if (selectedClassType == null) {
			player.sendMessage(
					main.color("&c&l(!) &rThis class does not exist! Use &e/classes &rfor a list of playable classes"));
			return;
		}

		handleClassSelection(player, game, playerData, selectedClassType);
	}

	private void handleClassSelection(Player player, GameInstance game, PlayerData playerData, ClassType type) {
		ClassDetails classDetails = playerData.playerClasses.get(type.getID());

		if (!isClassUnlocked(player, classDetails, type) || !isLevelUnlocked(player, playerData, type)
				|| !isFishermanClassUnlocked(player, type) || !isRankRequirementMet(player, type)
				|| !isPlayerInGame(player, game) || !isGameStateWaiting(game, player)
				|| !isFrenzyGameType(game, player)) {
			return;
		}

		displayClassSelectionMessage(player, type);
		main.getGameManager().playerSelectClass(player, type);
		player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
	}

	private void displayClassSelectionMessage(Player player, ClassType type) {
		player.sendMessage(main.color("&2&l============================================="));
		player.sendMessage(main.color("&2&l|| "));
		player.sendMessage(main.color("&2&l|| "));
		player.sendMessage(main.color("&2&l|| " + "&e&lSelected Class: " + type.getTag()));
		player.sendMessage(main.color("&2&l|| " + "&e&lClass Desc: &e" + type.getClassDesc()));
		player.sendMessage(main.color("&2&l|| "));
		player.sendMessage(main.color("&2&l|| "));
		player.sendMessage(main.color("&2&l============================================="));
	}

	private void selectRandomClass(Player player, PlayerData playerData) {
		Random random = new Random();
		ClassType classType = ClassType.getAvailableClasses()[random.nextInt(ClassType.getAvailableClasses().length)];

		if (playerData.playerClasses.get(classType.getID()) != null
				&& playerData.playerClasses.get(classType.getID()).purchased || classType.getTokenCost() == 0) {
			Rank donor = classType.getMinRank();

			if (donor == null || player.hasPermission("scb." + donor.toString().toLowerCase())) {
				player.sendMessage(main.color("&2&l(!) " + "&eYou have selected to go a &lRandom class"));
				main.getGameManager().playerSelectClass(player, classType);
				GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
				game.board.updateLine(5, main.color("&6 Random"));
				player.setDisplayName(player.getName());
			}
		}
	}

	private boolean isRankRequirementMet(Player player, ClassType type) {
		Rank donor = type.getMinRank();
		if (donor != null && !player.hasPermission("scb." + donor.toString().toLowerCase())) {
			player.sendMessage(main.color("&c&l(!) &rYou need a rank to use this class"));
			return false;
		}
		return true;
	}

	private boolean isLevelUnlocked(Player player, PlayerData playerData, ClassType type) {
		// Level Classes
		if (type.getLevel() > 0 && playerData.level < type.getLevel()) {
			player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this class yet!"));
			return false;
		}
		return true;
	}

	private boolean isFishermanClassUnlocked(Player player, ClassType type) {
		if (type == ClassType.Fisherman && !main.fishing.hasUnlockedFisherman(player) && !player.isOp()) {
			player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this class yet!"));
			return false;
		}
		return true;
	}

	private boolean isClassUnlocked(Player player, ClassDetails classDetails, ClassType type) {
		boolean isPurchased = classDetails != null && classDetails.purchased;

		if (!isPurchased && type.getTokenCost() != 0) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have this class unlocked"));
			return false;
		}
		return true;
	}

	private boolean isFrenzyGameType(GameInstance game, Player player) {
		if (game.gameType == GameType.FRENZY) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot select a class in a Frenzy game!"));
			return false;
		}
		return true;
	}

	private ClassType getClassType(String className) {
		for (ClassType type : ClassType.getAvailableClasses()) {
			if (className.equalsIgnoreCase(type.toString())) {
				return type;
			}
		}
		return null;
	}

	public void leaveGame(Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfSpectator(player);
		player.spigot().setCollidesWithEntities(true);
		player.setAllowFlight(false);
		player.setAllowFlight(true);
		for (Player p : Bukkit.getOnlinePlayers()) {
			player.showPlayer(p);
			p.showPlayer(player);
		}

		if (game != null && game.state == GameState.ENDED)
			return;
		else if (main.getGameManager().RemovePlayerFromAll(player)) {
			main.ResetPlayer(player);
			/*
			 * main.getScoreboardManager().lobbyBoard(player);
			 * main.sendScoreboardUpdate(player);
			 */
			player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have left your game");

			if (game != null && game.getGameSettings() != null) {
				GameSettings gs = game.getGameSettings();

				if (gs.startVotes.contains(player)) {
					gs.totalStartVotes--;
					gs.startVotes.remove(player);
				}
			}

			for (PotionEffect type : player.getActivePotionEffects())
				player.removePotionEffect(type.getType());

			// main.sendScoreboardUpdate(player);
			removeArmor(player);
		} else if (game != null && game.spectators.contains(player)) {
			String mapName = "";
			if (game.duosMap != null)
				mapName = game.duosMap.toString();
			else
				mapName = game.getMap().toString();

			player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have left " + mapName);
			main.ResetPlayer(player);
			main.getScoreboardManager().lobbyBoard(player);
			main.sendScoreboardUpdate(player);
			game.spectators.remove(player);
			player.setDisplayName("" + player.getName());
		} else
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("join") || cmd.getName().equalsIgnoreCase("spectate")) {
			List<Maps> maps = Arrays.asList(Maps.values());
			List<String> mapsString = Lists.newArrayList();
			if (args.length == 1) {
				for (Maps map : maps) {
					if (map.getName().toLowerCase().startsWith(args[0].toLowerCase()))
						mapsString.add(map.getName());
				}
				return mapsString;
			}
		} else if (cmd.getName().equalsIgnoreCase("class")) {
			List<ClassType> a = Arrays.asList(ClassType.getAvailableClasses());
			List<String> f = Lists.newArrayList();
			if (args.length == 1) {
				for (ClassType s : a) {
					if (s.name().toLowerCase().startsWith(args[0].toLowerCase()))
						f.add(s.name());
				}
				return f;
			}
		} else if (cmd.getName().equalsIgnoreCase("sound")) {
			if (args.length == 1) {
				List<String> soundNames = new ArrayList<>();
				for (Sound sound : Sound.values()) {
					if (sound.name().toLowerCase().startsWith(args[0].toLowerCase())) {
						soundNames.add(sound.name());
					}
				}
				return soundNames;
			}
		}
		return null;
	}

	private boolean isGameStateWaiting(GameInstance game, Player player) {
		if (game.state != GameState.WAITING) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot select a class while in a game!"));
			return false;
		}
		return true;
	}

	private boolean isPlayerInGame(Player player, GameInstance game) {
		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou have to be in a game to select a class!"));
			return false;
		}
		return true;
	}

	private void removeArmor(Player player) {
		ItemStack air = new ItemStack(Material.AIR, 1);
		player.getInventory().setHelmet(air);
		player.getInventory().setChestplate(air);
		player.getInventory().setLeggings(air);
		player.getInventory().setBoots(air);
	}

	private void resetDoubleJump(Player player) {
		player.setAllowFlight(false);
		player.setAllowFlight(true);
	}
}
