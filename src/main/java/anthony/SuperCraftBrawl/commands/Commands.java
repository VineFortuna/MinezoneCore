package anthony.SuperCraftBrawl.commands;

import java.awt.*;
import java.awt.Color;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.DuelsWagerManager;
import anthony.SuperCraftBrawl.Game.GameManager;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.gui.DuelsWagerGUI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.gui.GameStatsGUI;
import anthony.SuperCraftBrawl.gui.ShopCWGUI;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.PropertyType;

public class Commands implements CommandExecutor, TabCompleter {

	private final Core main;
	public List<Player> players;
//	private final DuelsWagerManager duelsWagerManager;

//	public Commands(Core main, DuelsWagerManager duelsWagerManager) {
//		this.main = main;
//		this.duelsWagerManager = duelsWagerManager;
//	}

	public Commands(Core main) {
		this.main = main;
	}

	public void removeArmor(Player player) {
		ItemStack air = new ItemStack(Material.AIR, 1);
		player.getInventory().setHelmet(air);
		player.getInventory().setChestplate(air);
		player.getInventory().setLeggings(air);
		player.getInventory().setBoots(air);
	}

	@SuppressWarnings("unused")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player senderPlayer = (Player) sender;

		if (sender instanceof Player) {
			switch (cmd.getName().toLowerCase()) {
				case "purchases":
					if (args.length > 0) {
						switch (args[0].toLowerCase()) {
							case "get":
								PlayerData data = main.getPlayerDataManager().getPlayerData(senderPlayer);
								senderPlayer.sendMessage("Listing purchases...");

								int size = 0;
								for (Entry<Integer, ClassDetails> entry : data.playerClasses.entrySet()) {
									senderPlayer.sendMessage(" - " + entry.getKey() + ": " + entry.getValue().toString());
									size++;
								}

								if (size == 0)
									senderPlayer.sendMessage("You have no Class Stats");
								break;
							case "buy":
								if (args.length > 1) {
									int classID = Integer.parseInt(args[1]);
									PlayerData playerData = main.getPlayerDataManager().getPlayerData(senderPlayer);
									ClassDetails details = playerData.playerClasses.get(classID);

									if (details == null) {
										details = new ClassDetails();
										playerData.playerClasses.put(classID, details);
									}
									details.setPurchased();
									senderPlayer.sendMessage("Class Purchased!");
								} else
									senderPlayer.sendMessage("Not enough arguments");

						}
					} else {
						senderPlayer.sendMessage("Not enough arguments!");
					}
					break;

				case "startgame":
					GameInstance instance2 = main.getGameManager().GetInstanceOfPlayer(senderPlayer);

					if (instance2 != null) {
						if (senderPlayer.hasPermission("scb.startGame")) {
							if (instance2.state == GameState.WAITING && instance2.players.size() >= 0) {
								instance2.TellAll(
										main.color("&2&l(!) &rGame has been force started by &e" + senderPlayer.getName()));
								instance2.ticksTilStart = 0;
							} else if (instance2.state == GameState.WAITING && !(instance2.players.size() >= 2))
								senderPlayer.sendMessage(main.color("&c&l(!) &rNot enough players to start!"));
							else if (instance2.state == GameState.STARTED)
								sender.sendMessage(main.color("&c&l(!) &rGame is already in progress!"));
						} else
							senderPlayer.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
					} else
						senderPlayer.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));

					break;

				case "setlives":
					if (senderPlayer.getName().equals("chirpinsince02")) {
						if (args.length > 0) {
							Player target = Bukkit.getServer().getPlayerExact(args[0]);
							GameInstance i = main.getGameManager().GetInstanceOfPlayer(target);
							BaseClass baseClass2 = i.classes.get(target);
							int num = Integer.parseInt(args[1]);

							if (i.state == GameState.STARTED) {
								if (i != null) {
									if (target != null) {
										baseClass2.lives = num;
										senderPlayer.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD
												+ "(!) " + ChatColor.RESET + "You have set " + ChatColor.YELLOW
												+ target.getName() + ChatColor.RESET + "'s lives to " + ChatColor.YELLOW
												+ num);
										baseClass2.score.setScore(baseClass2.lives);
									} else
										senderPlayer.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD
												+ "(!) " + ChatColor.RESET + "Please specify a player!");
								} else
									senderPlayer.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
											+ ChatColor.RESET + "This player is not in a game!");
							} else
								senderPlayer.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
										+ ChatColor.RESET + "Game must be started in order to use this!");
						}
					}
					break;

				case "gamestats":
					if (args.length >= 0) {
						if (main.gameStats.containsKey(senderPlayer)) {
							if (main.gameStats.get(senderPlayer) != null) {
								if (main.gameStats.get(senderPlayer).HasPlayer(senderPlayer)) {
									new GameStatsGUI(main, main.gameStats.get(senderPlayer)).inv.open(senderPlayer);
									return true;
								}
							}
						}
						senderPlayer.sendMessage(main.color("&c&l(!) &rThis game's stats have expired"));
					}

					return true;

				case "fav":
					if (args.length >= 0) {
						String className = args[0];

						for (ClassType type : ClassType.values()) {
							if (className != null && className.equalsIgnoreCase(type.toString())) {
								PlayerData playerData = main.getPlayerDataManager().getPlayerData(senderPlayer);

								if (playerData != null) {
									playerData.customIntegers.add(type.getID());
									senderPlayer.sendMessage(main.color("&2&l(!) &rNew favorite class! " + type.getTag()));
									main.getPlayerDataManager().saveData(playerData);
								}
							}
						}
					}
					return true;

				case "effect":
					if (args.length == 0) {
						List<Effect> particleEffects = new ArrayList<>();
						List<Effect> visualEffects = new ArrayList<>();
						List<Effect> soundEffects = new ArrayList<>();

						for (Effect effect : Effect.values()) {
							Effect.Type effectType = effect.getType();

							switch (effectType) {
								case PARTICLE:
									particleEffects.add(effect);
									break;
								case VISUAL:
									visualEffects.add(effect);
									break;
								case SOUND:
									soundEffects.add(effect);
									break;
							}
						}

						int totalEffects = particleEffects.size() + visualEffects.size() + soundEffects.size();

						senderPlayer.sendMessage(color("=================================="));
						senderPlayer.sendMessage(color("&eTotal Effects: &a" + totalEffects));
						senderPlayer.sendMessage(color("&eTotal ParticleType: &a" + particleEffects.size()));
						senderPlayer.sendMessage(color("&eTotal VisualType: &a" + visualEffects.size()));
						senderPlayer.sendMessage(color("&eTotal SoundType: &a" + soundEffects.size()));
						senderPlayer.sendMessage(color("=================================="));
						senderPlayer.sendMessage(color("&6ParticleType Effects:"));
						for (Effect effect : particleEffects) {
							TextComponent message = new TextComponent(
									color("&a" + effect + "&c || &b" + effect.getType()));
							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/effect " + effect));
//							senderPlayer.sendMessage(message.getText());

							Bukkit.spigot().broadcast(message);

						}
						senderPlayer.sendMessage(color("=================================="));
						senderPlayer.sendMessage(color("&6VisualType Effects:"));

						for (Effect effect : visualEffects) {
							TextComponent message = new TextComponent(
									color("&a" + effect + "&c || &d" + effect.getType()));
							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/effect " + effect));
//							senderPlayer.sendMessage(message.getText());

							Bukkit.spigot().broadcast(message);
						}
						senderPlayer.sendMessage(color("=================================="));
						senderPlayer.sendMessage(color("&6SoundType Effects:"));

						for (Effect effect : soundEffects) {
							TextComponent message = new TextComponent(
									color("&a" + effect + "&c || &c" + effect.getType()));
							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/effect " + effect));
//							senderPlayer.sendMessage(message.getText());

							Bukkit.spigot().broadcast(message);

						}
						senderPlayer.sendMessage(color("=================================="));

					} else if (args.length == 1) {
						try {
							Effect effect = Effect.valueOf(args[0].toUpperCase());

							senderPlayer.playEffect(senderPlayer.getLocation(), effect, 1);

						} catch (IllegalArgumentException e) {
							senderPlayer.sendMessage("Invalid effect name.");
						}
					}
					return  true;

				case "particle":
					String usageParticle = "&c&b(!) &r Incorrect usage! Try doing: &a/particle &r or &a/particle particleName";

					Location playerLocation = senderPlayer.getLocation();
					Location particleLocation = playerLocation.add(0,2,0).add(playerLocation.getDirection().multiply(5));

					// Displaying particles list
					if (args.length == 0) {
						displayParticleList(senderPlayer);
					}
					// Displaying particle
					else if (args.length >= 1) {
						ParticleEffect particleEffect;

						try {
							particleEffect = ParticleEffect.valueOf(args[0].toUpperCase());
							displayParticle(senderPlayer, particleEffect, particleLocation, args);
						} catch (IllegalArgumentException e) {
							senderPlayer.sendMessage("Invalid effect name.");
						}
					}
					// Sending usage message
					else
						senderPlayer.sendMessage(color(usageParticle));
					return true;

				case "join":
					if (args.length > 0) {
						String mapName = args[0];
						GameInstance i = main.getGameManager().GetInstanceOfSpectator(senderPlayer);

						if (i != null) {
							senderPlayer.sendMessage(main.color("&c&l(!) &rYou are currently spectating a game!"));
							return true;
						}
						Maps map = null;

						for (Maps maps : anthony.SuperCraftBrawl.Game.map.Maps.values()) {
							if (maps.toString().equalsIgnoreCase(mapName)) {
								map = maps;
								break;
							}
						}
						if (map != null)
							main.getGameManager().JoinMap(senderPlayer, map);
						else
							senderPlayer.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "This map does not exist! Use " + ChatColor.YELLOW + "/maplist " + ChatColor.RESET
									+ "for a list of playable maps");
					} else
						senderPlayer.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Incorrect usage! Try doing: " + ChatColor.RESET + ChatColor.GREEN + "/join <mapname>");

					return true;

				case "duel":
					String usageDuel = "&c&b(!) &r Incorrect usage! Try doing: &a/duel playerName &r or &a/duel playerName tokenAmount";

					senderPlayer.sendMessage("test 1: command executed");

					if (args.length  == 1 || args.length == 2) {
						String targetPlayerName = args[0];
						Player targetPlayer = main.getServer().getPlayer(targetPlayerName);

						senderPlayer.sendMessage("test 2:args == 1 || 2");

						if (targetPlayer != null) {
							senderPlayer.sendMessage("test 3: player not null");

							// Checks if target player is online
							if (!targetPlayer.isOnline()) {
								senderPlayer.sendMessage("test 4: player is not online");
								senderPlayer.sendMessage("Player is not online");
								return true;
							} else {
								GameManager gameManager = new GameManager(main);

								senderPlayer.sendMessage("test 5: player is online");

								for (GameInstance gameInstance : gameManager.gameMap.values()) {
									// Checks if player is in a GameInstance
									if (!gameInstance.players.contains(targetPlayer)) {
										senderPlayer.sendMessage("test 6: player is not in a game");

										// /duel playerName
										if (args.length == 1) {
											new DuelsWagerGUI(main, targetPlayer).inv.open(senderPlayer);

											senderPlayer.sendMessage("test 7: args == 1");

											senderPlayer.sendMessage("the duels gui should have been opened");
											// /duel playerName tokensWageAmount
										} else {
											String tokensWageAmount = args[1];

											senderPlayer.sendMessage("test 8: args == 2 ");

											try {
												Integer.parseInt(tokensWageAmount);

												senderPlayer.sendMessage("test 9: args 2 was an int");

												// Send Duel Invite with token amount
												senderPlayer.sendMessage("duel invite with token amount should have been sent");

											} catch (NumberFormatException e) {
												senderPlayer.sendMessage("test 10: args 2 was not an int");

												senderPlayer.sendMessage(color(usageDuel));
											}
										}
									} else senderPlayer.sendMessage(color("&c&b(!) &r The player is already in a game"));
								}
							}
						}
					} else {
						senderPlayer.sendMessage(color(usageDuel));
					}
					return true;

//				case "duelrequest":
//					String usageDuelRequest = "&c&b(!) &r Incorrect usage! Try doing: &a/duelrequest accept/decline/changebet playerName";
//
//					if (args.length == 2 || args.length == 3) {
//						String response = args[0].toLowerCase();
//						String challengerPlayer = args[1].toLowerCase();
//
//						switch (response) {
//							case "accept":
//
//							case "decline":
//							case "change bet":
//						}
//					} else {
//						senderPlayer.sendMessage(usageDuelRequest);
//					}



//				case "wager":
//					if (args.length > 0) {
//						GameInstance i5 = main.getGameManager().GetInstanceOfPlayer(senderPlayer); String accept = args[0];
//
//						if (accept.equalsIgnoreCase("accept")) {
//							if (main.wagers.containsKey(senderPlayer)) {
//								Player other = main.wagers.get(senderPlayer);
//
//								for (Maps map : Maps.values()) {
//									if (map != null && map.toString().contains("Duel")) {
//										main.getGameManager().JoinMap(senderPlayer, map);
//										main.getGameManager().JoinMap(other, map);
//										senderPlayer.sendMessage(main.color("&2&l(!) &rMatch found on &r&l" + map.toString()));
//										return true;
//									}
//								}
//
//								senderPlayer.sendMessage(main.color("&c&l(!) &rAll wager maps are full! Please try again later."));
//							} else {
//								senderPlayer.sendMessage(main.color("&c&l(!) &rYou do not have any wager requests!"));
//							}
//						} else if (accept.equalsIgnoreCase("list")) {
//							senderPlayer.sendMessage(main.color("&e&l(!) &rWagers list of commands:"));
//							senderPlayer.sendMessage(main.color("     &e- /wager accept"));
//							senderPlayer.sendMessage(main.color("     &e- /wager deny"));
//						} else
//							senderPlayer.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/wager list &rfor a list of commands"));
//					}
//					return true;


				case "shop":
					if (senderPlayer.hasPermission("scb.shop")) {
						new ShopCWGUI(main).inv.open(senderPlayer);
					}

				case "cw":
					if (args.length > 0) {
						String map = args[0];

						for (anthony.CrystalWars.game.Maps maps : anthony.CrystalWars.game.Maps.values()) {
							if (maps.toString().equalsIgnoreCase(map)) {
								main.getCwManager().JoinGame(senderPlayer, maps);
								return true;
							}
						}

						senderPlayer.sendMessage(main.color("&c&l(!) &rThis map does not exist!"));
					}
					return true;

				case "sw":
					if (args.length > 0) {
						String map = args[0];

						for (anthony.skywars.Maps maps : anthony.skywars.Maps.values()) {
							if (maps.toString().equalsIgnoreCase(map)) {
								main.getSWManager().JoinGame(senderPlayer, maps);
								return true;
							}
						}

						senderPlayer.sendMessage(main.color("&c&l(!) &rThis map does not exist!"));
					}
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
								main.getGameManager().SpectatorJoinMap(senderPlayer, map);
							else
								senderPlayer.sendMessage(
										ChatColor.BOLD + "(!) " + ChatColor.RESET + "This map does not exist! Use "
												+ ChatColor.YELLOW + "/maplist " + ChatColor.RESET + "for a list of maps");
						} else {
							senderPlayer.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "Incorrect usage! Try doing: " + ChatColor.RESET + ChatColor.GREEN
									+ "/spectate <mapname>");
						}
					}
					return true;

				case "class":
					PlayerData playerData = main.getPlayerDataManager().getPlayerData(senderPlayer);
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
											senderPlayer.sendMessage(
													main.color("&c&l(!) &rYou have not unlocked this class yet!"));
											return false;
										}
									}

									if (donor == null || sender.hasPermission("scb." + donor.toString().toLowerCase())) {
										GameInstance game = main.getGameManager().GetInstanceOfPlayer((Player) sender);
										if (game != null) {
											if (game.state == GameState.WAITING) {
												if (game.gameType == GameType.FRENZY) {
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
															+ ChatColor.RESET
															+ "You cannot select a class in a Frenzy game");
												} else {
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
															+ "==============================================");
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
															+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
															+ "Selected Class: " + type.getTag());
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
															+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
															+ "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW
															+ type.getClassDesc());
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
													senderPlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
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
										instance20.board.updateLine(2, " " + "Random");
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
					this.leaveGame(senderPlayer);
					return true;

				case "l":
					this.leaveGame(senderPlayer);
					return true;

				case "players":
					GameInstance instance = main.getGameManager().GetInstanceOfPlayer(senderPlayer);
					if (instance != null) {
						String players = "";
						for (Player gamePlayer : instance.players) {
							if (!players.isEmpty())
								players += ", ";
							players += gamePlayer.getName() + "";
						}
						senderPlayer.sendMessage(
								"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "Players in your game ("
										+ instance.players.size() + "): " + ChatColor.RESET + ChatColor.WHITE);
						senderPlayer.sendMessage("" + ChatColor.BOLD + "--> " + ChatColor.RESET + players);
					} else
						senderPlayer.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
					return true;

				case "items":
					senderPlayer.sendMessage("You received one of every item drop.");

					GameInstance gameInstance = main.getGameManager().GetInstanceOfPlayer(senderPlayer);
					for (ItemStack itemStack : gameInstance.allItemDrops) {
						senderPlayer.getInventory().addItem(itemStack);
					}

					senderPlayer.sendMessage(ChatColorHelper.color("&aYou received one of every item drop"));

				case "blockid":
//					Block block = player.getTargetBlock();
//					player.sendMessage(block.getType().getId() + ":" + block.getData());

					}
			} else
			sender.sendMessage("Hey! You can't use this in the terminal!");

			return true;
		}

		private ItemStack enchantments(ItemStack item, Enchantment ench, int level) {
			item.addUnsafeEnchantment(ench, level);
			return item;
		}

		private Material testMaterial(String st) {
			try {
				return Material.getMaterial(st.toUpperCase());
			} catch (Exception e) {
				return null;
			}
		}

		private Enchantment testEnchant(String st) {
			try {
				return Enchantment.getByName(st.toUpperCase());
			} catch (Exception e) {
				return null;
			}
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
				main.LobbyBoard(player);
				player.getInventory().clear();
				main.LobbyItems(player);
				player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have left your game");
				PlayerData data = main.getPlayerDataManager().getPlayerData(player);
				if (data != null && data.votes == 1) {
					if (i != null && i.state == GameState.WAITING) {
						i.totalVotes--;
						data.votes = 0;
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
				main.LobbyBoard(player);
				player.getInventory().clear();
				main.LobbyItems(player);
				i.spectators.remove(player);
				player.setDisplayName("" + player.getName());
			} /*
			 * else if (i2 != null && main.getCwManager().removePlayer(player)) {
			 * main.ResetPlayer(player); player.setGameMode(GameMode.ADVENTURE);
			 * main.LobbyBoard(player); player.getInventory().clear();
			 * main.LobbyItems(player); player.sendMessage("" + ChatColor.RESET +
			 * ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET +
			 * "You have left your game");
			 * 
			 * for (PotionEffect type : player.getActivePotionEffects())
			 * player.removePotionEffect(type.getType()); main.sendScoreboardUpdate(player);
			 * player.setGameMode(GameMode.ADVENTURE); removeArmor(player); }
			 */ else {
				player.sendMessage("" + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You are not in a game!");
			}
		}

		public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
			if (cmd.getName().equalsIgnoreCase("join")) {
				List<Maps> a = Arrays.asList(Maps.values());
				List<String> f = Lists.newArrayList();
				if (args.length == 1) {
					for (Maps s : a) {
						if (s.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
							f.add(s.getName());
						}
					}
					return f;
				}
			} else if (cmd.getName().equalsIgnoreCase("class")) {
				List<ClassType> a = Arrays.asList(ClassType.values());
				List<String> f = Lists.newArrayList();
				if (args.length == 1) {
					for (ClassType s : a) {
						if (s.name().toLowerCase().startsWith(args[0].toLowerCase())) {
							f.add(s.name());
						}
					}
					return f;
				}
			} else if (cmd.getName().equalsIgnoreCase("particle")) {
			}
			return null;
		}

		public static String color(String c) {
			return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', c);
		}

		// Helper method to map color names to color constants
		private static Color getColorByName(String colorName) {
			switch (colorName) {
				case "RED":
					return Color.RED;
				case "YELLOW":
					return Color.YELLOW;
				case "GREEN":
					return Color.GREEN;
				case "GRAY":
					return Color.GRAY;
				case "LIGHT_GRAY":
				case "LIGHTGRAY":
					return Color.LIGHT_GRAY;
				case "DARK_GRAY":
				case "DARKGRAY":
					return Color.DARK_GRAY;
				case "MAGENTA":
					return Color.MAGENTA;
				case "PINK":
					return Color.PINK;
				case "ORANGE":
					return Color.ORANGE;
				case "BLACK":
					return Color.BLACK;
				case "WHITE":
					return Color.WHITE;
				case "BLUE":
					return Color.BLUE;
				case "CYAN":
					return Color.CYAN;

				// Add more color mappings as needed
				default:
					return null;
			}
		}

		private void displayParticleList(Player senderPlayer) {
			Set<ParticleEffect> particles = ParticleEffect.getAvailableEffects();

			// Sorting particles by Colorable and by Name
			List<ParticleEffect> sortedParticles = new ArrayList<>(particles);
			sortedParticles.sort(Comparator.comparing(ParticleEffect::name, String.CASE_INSENSITIVE_ORDER));
			sortedParticles.sort(Comparator.comparing(particleEffect -> particleEffect.hasProperty(PropertyType.COLORABLE) ? 1 : 0));

			senderPlayer.sendMessage(color("=============================="));
			senderPlayer.sendMessage(color("&eTotal ParticleEffects: &a" + sortedParticles.size()));
			senderPlayer.sendMessage(color("=============================="));

			for (ParticleEffect particle : sortedParticles) {
				ComponentBuilder builder;

				if (particle.hasProperty(PropertyType.COLORABLE)) {
					builder = new ComponentBuilder("")
							// Basic display message
							.append(color("&a&l[DISPLAY] "))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/particle " + particle))
							// Suggest command message
							.append(color("&e&l[SUGGEST] "))
							.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/particle " + particle))
							.append(color("&f" + particle))
							.append(color(" &6(Colorable)"));
				} else {
					builder = new ComponentBuilder("")
							// Basic display message
							.append(color("&a&l[DISPLAY] "))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/particle " + particle))
							// Suggest command message
							.append(color("&e&l[SUGGEST] "))
							.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/particle " + particle))
//							.append(color("&f" + String.format("%-" + (25 + particle.getFieldName().length()) + "s", particle)));
							.append(color("&f" + String.format("%-25s", particle)));

					// Add number options dynamically
					int[] particleAmounts = {5, 25, 50, 100, 500, 1000, 10000};
					for (int amount : particleAmounts) {
						String string = " &7[" + amount + "] ";

						builder
								.append(color(" &7[" + amount + "] "))
//								.append(color(String.format("%7s", string)))
								.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/particle " + particle + " " + amount));
					}

					// Send the messages to the player
					senderPlayer.spigot().sendMessage(builder.create());
				}
			}

		}



		private void displayParticle(Player senderPlayer, ParticleEffect particleEffect, Location particleLocation, String[] args) {
			// Displaying particle with no modifications
			if (args.length == 1) {
				ParticleBuilder particleBuilder = new ParticleBuilder(
						particleEffect,
						particleLocation);

				particleBuilder.display(senderPlayer);
			}
			else if (args.length >= 2) {
				int amount;

				try {
					// Parse int
					amount = Integer.parseInt(args[1]);
					// Displaying particles with custom amount
					if (args.length == 2) {
						ParticleBuilder particleBuilder = new ParticleBuilder(
								particleEffect,
								particleLocation)
								.setAmount(amount);

						particleBuilder.display(senderPlayer);
					}
					// Displaying particle with color modification
					else if (args.length == 3) {

						try {
							particleEffect = ParticleEffect.valueOf(args[0].toUpperCase());
							amount = Integer.parseInt(args[1]);

							// Parse color from string or use color constant
							Color color;
							if (args[2].startsWith("#")) {
								// Parse hex color code
								int rgb = Integer.parseInt(args[2].substring(1), 16);
								color = new Color(rgb);
							} else {
								// Use color constant
								color = getColorByName(args[2].toUpperCase());
							}

							ParticleBuilder particleBuilder = new ParticleBuilder(
									particleEffect,
									particleLocation)
									.setAmount(amount)
									.setColor(color);

							particleBuilder.display(senderPlayer);
						} catch (IllegalArgumentException e) {
							senderPlayer.sendMessage("Invalid effect name, color, or format. Use color name or hex color code (e.g., #RRGGBB).");
						}
					}
				} catch (NumberFormatException e) {
					senderPlayer.sendMessage("Invalid integer");
				}
			}
		}

}
