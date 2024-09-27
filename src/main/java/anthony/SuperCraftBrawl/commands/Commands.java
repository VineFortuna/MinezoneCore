package anthony.SuperCraftBrawl.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.fishing.FishType;
import anthony.SuperCraftBrawl.gui.GameStatsGUI;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;

public class Commands implements CommandExecutor, TabCompleter {

	private final Core main;
	public List<Player> players;

	public Commands(Core main) {
		this.main = main;
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

	@SuppressWarnings("unused")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;

		if (sender instanceof Player) {
			GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

			switch (cmd.getName().toLowerCase()) {
			case "purchases":
				if (args.length > 0) {
					switch (args[0].toLowerCase()) {
					case "get":
						PlayerData data = main.getDataManager().getPlayerData(player);
						player.sendMessage("Listing purchases...");

						int size = 0;
						for (Entry<Integer, ClassDetails> entry : data.playerClasses.entrySet()) {
							player.sendMessage(" - " + entry.getKey() + ": " + entry.getValue().toString());
							size++;
						}

						if (size == 0)
							player.sendMessage("You have no Class Stats");
						break;
					case "buy":
						if (args.length > 1) {
							int classID = Integer.parseInt(args[1]);
							PlayerData playerData = main.getDataManager().getPlayerData(player);
							ClassDetails details = playerData.playerClasses.get(classID);

							if (details == null) {
								details = new ClassDetails();
								playerData.playerClasses.put(classID, details);
							}
							details.setPurchased();
							player.sendMessage("Class Purchased!");
						} else
							player.sendMessage("Not enough arguments");

					}
				} else
					player.sendMessage("Not enough arguments!");

				break;
			case "startgame":
				if (game != null) {
					if (player.hasPermission("scb.startGame")) {
						if (game.state == GameState.WAITING && game.players.size() >= 1)
							game.getGameSettings().forceStartGame();
						else if (game.state == GameState.WAITING)
							player.sendMessage(main.color("&c&l(!) &rNot enough players to start!"));
						else if (game.state == GameState.STARTED)
							sender.sendMessage(main.color("&c&l(!) &rGame is already in progress!"));
					} else
						player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
				} else
					player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));

				break;

			case "fly":
				game = main.getGameManager().GetInstanceOfPlayer(player);
				PlayerData flyData = main.getDataManager().getPlayerData(player);

				if (game == null) {
					if (player.hasPermission("scb.fly")) {
						if (flyData != null) {
							if (flyData.fly == 0) {
								player.sendMessage(main.color("&e&l(!) &rYou have enabled flight!"));
								resetDoubleJump(player);
								flyData.fly = 1;
							} else {
								player.sendMessage(main.color("&e&l(!) &rYou have disabled flight!"));
								resetDoubleJump(player);
								flyData.fly = 0;
							}
							main.getDataManager().saveData(flyData); // Save even when server restarts
						}
					} else
						player.sendMessage(main.color("&c&l(!) &rYou need the rank " + ChatColor.BLUE + ChatColor.BOLD
								+ "CAPTAIN &rto use this command!"));
				} else
					player.sendMessage(main.color("&c&l(!) &rYou cannot use this in game!"));

				break;

			case "setlives":
				if (args.length >= 2) {
					Player target = Bukkit.getServer().getPlayerExact(args[0]);
					int num = 0;

					try {
						num = Integer.parseInt(args[1]);
					} catch (NumberFormatException ex) {
						player.sendMessage(main.color("&r&l(!) &rPlease specify number of lives"));
						return false;
					}

					if (target != null) {
						GameInstance i = main.getGameManager().GetInstanceOfPlayer(target);
						if (i != null) {
							if (i.state == GameState.STARTED) {
								BaseClass baseClass2 = i.classes.get(target);
								baseClass2.lives = num;
								player.sendMessage(main
										.color("&2&l(!) &rYou set &e" + target.getName() + "&r's lives to &e" + num));
								baseClass2.score.setScore(baseClass2.lives);
							} else
								player.sendMessage(main.color("&c&l(!) &rGame must be started to use!"));
						} else
							player.sendMessage(main.color("&c&l(!) &rThis player is not in a game!"));
					} else
						player.sendMessage(main.color("&c&l(!) &rPlease specify a player!"));
				} else
					player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/setlives <player> <num>"));

				break;

			case "gamestats":
				if (args.length >= 0) {
					if (main.gameStats.containsKey(player)) {
						if (main.gameStats.get(player) != null) {
							if (main.gameStats.get(player).HasPlayer(player)) {
								try {
									new GameStatsGUI(main, main.gameStats.get(player)).inv.open(player);
								} catch (NullPointerException ex) {
									player.sendMessage(main.color(
											"&c&l(!) &rThis game's stats cannot be viewed. Did a player leave early?"));
								}
								return true;
							}
						}
					}
					player.sendMessage(main.color("&c&l(!) &rThis game's stats have expired"));
				}

				return true;

			case "join":
				if (args.length > 0) {
					String mapName = args[0];
					GameInstance i = main.getGameManager().GetInstanceOfSpectator(player);

					if (i != null) {
						player.sendMessage(main.color("&c&l(!) &rYou are currently spectating a game!"));
						return true;
					}
					Maps map = null;

					for (Maps maps : Maps.values()) {
						if (maps.toString().equalsIgnoreCase(mapName)) {
							map = maps;
							break;
						}
					}
					if (map != null)
						main.getGameManager().JoinMap(player, map);
					else
						player.sendMessage(
								main.color("&c&l(!) &rThis map does not exist! Use &e/maplist &rfor a list of maps"));
				} else
					player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/join <map>"));

				return true;

			case "spectate":
				if (sender instanceof Player) {
					if (args.length > 0) {
						String mapName = args[0];
						Maps map = null;

						for (Maps maps : Maps.values()) {
							if (maps.toString().equalsIgnoreCase(mapName)) {
								map = maps;
								break;
							}
						}

						if (map != null)
							main.getGameManager().SpectatorJoinMap(player, map);
						else
							player.sendMessage(main
									.color("&c&l(!) &rThis map does not exist! Use &e/maplist &rfor a list of maps"));
					} else
						player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/spectate <map>"));
				}
				return true;

			case "class":
				PlayerData playerData = main.getDataManager().getPlayerData(player);
				if (args.length > 0) {
					String className = args[0];
					for (ClassType type : ClassType.values())
						if (className.equalsIgnoreCase(type.toString())) {
							if (playerData.playerClasses.get(type.getID()) != null
									&& playerData.playerClasses.get(type.getID()).purchased
									|| type.getTokenCost() == 0) {
								Rank donor = type.getMinRank();

								if (type.getLevel() > 0) {
									if (playerData.level < type.getLevel()) {
										player.sendMessage(
												main.color("&c&l(!) &rYou have not unlocked this class yet!"));
										return false;
									}
								}
								if (type == ClassType.Fisherman) {
									if (main.getTotalFish(player) < FishType.values().length && !player.isOp()) {
										player.sendMessage(
												main.color("&c&l(!) &rYou have not unlocked this class yet!"));
										return false;
									}
								}

								if (donor == null || sender.hasPermission("scb." + donor.toString().toLowerCase())) {
									if (game != null) {
										if (game.state == GameState.WAITING) {
											if (game.gameType == GameType.FRENZY) {
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
														+ ChatColor.RESET
														+ "You cannot select a class in a Frenzy game");
											} else {
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
														+ "==============================================");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
														+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
														+ "Selected Class: " + type.getTag());
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
														+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
														+ "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW
														+ type.getClassDesc());
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
														+ "==============================================");
												main.getGameManager().playerSelectClass((Player) sender, type);
											}
										} else {
											sender.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN
													+ ChatColor.BOLD + "(!) " + ChatColor.RESET
													+ "You cannot select a class while in a game!");
										}
									} else {
										sender.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD
												+ "(!) " + ChatColor.RESET
												+ "You have to be in a game to select a class!");
									}
									return true;
								} else {
									sender.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD
											+ "(!) " + ChatColor.RESET
											+ "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
									return true;
								}
							} else {
								sender.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
										+ ChatColor.RESET + "You do not have this class unlocked");
								return true;
							}
						} else if (className.equalsIgnoreCase("random")) {
							GameInstance instance20 = main.getGameManager().GetInstanceOfPlayer((Player) sender);
							Random random = new Random();
							ClassType classType = ClassType.values()[random.nextInt(ClassType.values().length)];

							if (playerData.playerClasses.get(classType.getID()) != null
									&& playerData.playerClasses.get(classType.getID()).purchased
									|| classType.getTokenCost() == 0) {
								Rank donor = type.getMinRank();

								if (donor == null || sender.hasPermission("scb." + donor.toString().toLowerCase())) {

									sender.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD
											+ "(!) " + ChatColor.RESET + "You have selected to go a Random class");
									main.getGameManager().playerSelectClass((Player) sender, classType);
									instance20.board.updateLine(5, " " + ChatColor.GRAY + "Random");
									((Player) sender).setDisplayName("" + sender.getName());
									return true;
								}
							}
						}

					sender.sendMessage(main.color(
							"&c&l(!) &rThis class does not exist! Use &e/classes &rfor a list of playable classes"));
				} else
					sender.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/class <classname>"));
				return true;

			case "leave":
			case "l":
				this.leaveGame(player);
				return true;

			case "players":
				GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);
				if (instance != null) {
					String players = "";
					for (Player gamePlayer : instance.players) {
						if (!players.isEmpty())
							players += ", ";
						players += gamePlayer.getName() + "";
					}
					player.sendMessage(
							"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "Players in your game ("
									+ instance.players.size() + "): " + ChatColor.RESET + ChatColor.WHITE);
					player.sendMessage("" + ChatColor.BOLD + "--> " + ChatColor.RESET + players);
				} else
					player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
				return true;
			}
		} else
			sender.sendMessage("Hey! You can't use this in the terminal!");

		return true;
	}

	public void leaveGame(Player player) {
		GameInstance i = main.getGameManager().GetInstanceOfSpectator(player);
		// anthony.CrystalWars.game.GameInstance i2 =
		// main.getCwManager().getInstanceOfPlayer(player);
		player.spigot().setCollidesWithEntities(true);
		player.setAllowFlight(false);
		player.setAllowFlight(true);
		for (Player p : Bukkit.getOnlinePlayers()) {
			player.showPlayer(p);
			p.showPlayer(player);
		}

		if (i != null && i.state == GameState.ENDED)
			return;
		else if (main.getGameManager().RemovePlayerFromAll(player)) {
			main.ResetPlayer(player);
			player.setGameMode(GameMode.ADVENTURE);
			main.getScoreboardManager().lobbyBoard(player);
			player.getInventory().clear();
			main.LobbyItems(player);
			player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have left your game");

			if (i != null && i.getGameSettings() != null) {
				GameSettings gs = i.getGameSettings();

				if (gs.startVotes.contains(player)) {
					gs.totalStartVotes--;
					gs.startVotes.remove(player);
				}
			}

			for (PotionEffect type : player.getActivePotionEffects())
				player.removePotionEffect(type.getType());

			main.sendScoreboardUpdate(player);
			player.setGameMode(GameMode.ADVENTURE);
			removeArmor(player);
		} else if (i != null && i.spectators.contains(player)) {
			String mapName = "";
			if (i.duosMap != null)
				mapName = i.duosMap.toString();
			else
				mapName = i.getMap().toString();

			player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have left " + mapName);
			main.ResetPlayer(player);
			player.setGameMode(GameMode.ADVENTURE);
			main.getScoreboardManager().lobbyBoard(player);
			player.getInventory().clear();
			main.LobbyItems(player);
			i.spectators.remove(player);
			player.setDisplayName("" + player.getName());
		} else
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("join")) {
			List<Maps> a = Arrays.asList(Maps.values());
			List<String> f = Lists.newArrayList();
			if (args.length == 1) {
				for (Maps s : a) {
					if (s.getName().toLowerCase().startsWith(args[0].toLowerCase()))
						f.add(s.getName());
				}
				return f;
			}
		} else if (cmd.getName().equalsIgnoreCase("class")) {
			List<ClassType> a = Arrays.asList(ClassType.values());
			List<String> f = Lists.newArrayList();
			if (args.length == 1) {
				for (ClassType s : a) {
					if (s.name().toLowerCase().startsWith(args[0].toLowerCase()))
						f.add(s.name());
				}
				return f;
			}
		}
		return null;
	}
}
