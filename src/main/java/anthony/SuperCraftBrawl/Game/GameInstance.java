package anthony.SuperCraftBrawl.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Timer;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.all.DarkSethBlingClass;
import anthony.SuperCraftBrawl.Game.map.DuosMaps;
import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;

public class GameInstance {

	// Variables
	private GameManager gameManager;
	public Objective livesObjective;
	public GameType gameType;
	private Random random = new Random();
	private Maps map;
	public DuosMaps duosMap;
	private World mapWorld;
	public int gameTicks = -1;
	public GameState state;
	public List<Player> players;
	public List<Player> spectators;
	public Location recentDrop = null;
	public HashMap<Player, BaseClass> classes;
	public HashMap<Player, BaseClass> oldClasses;
	public List<Player> playerPosition = new ArrayList<>();
	public HashMap<Player, FastBoard> boards = new HashMap();
	private HashMap<Player, ClassType> classSelection = new HashMap<>();
	public HashMap<Player, Timer> cooldowns = new HashMap<Player, Timer>();
	private List<Player> winnerList;
	public BukkitRunnable gameStartTime;
	public int ticksTilStart = 30;
	List<BukkitRunnable> runnables = new ArrayList<>();
	public int totalVotes = 0;
	public int blindness = 0;
	public ItemStack paper = ItemHelper.setDetails(new ItemStack(Material.PAPER),
			"" + ChatColor.YELLOW + ChatColor.BOLD + "Ready");
	public ItemStack paper2 = ItemHelper.setDetails(new ItemStack(Material.PAPER),
			"" + ChatColor.YELLOW + ChatColor.BOLD + "Unready");
	public int alivePlayers = 0;
	public int aliveTeams = 0;
	public Sign s;
	public HashMap<Player, String> team;
	public List<Player> redTeam;
	public List<Player> blueTeam;
	public List<Player> blackTeam;
	public int tokensBet = 0;
	public boolean wagers = false;

	private int gameTime = 0;

	// Constructors:
	public GameInstance(GameManager gameManager, Maps map) {
		this.gameManager = gameManager;
		this.map = map;
		this.state = GameState.WAITING; // Default game state
		this.gameType = map.GetInstance().gameType;
		this.players = new ArrayList<Player>();
		this.winnerList = new ArrayList<Player>();
		this.spectators = new ArrayList<Player>();
		classes = new HashMap<>();
		oldClasses = new HashMap<>();
		InitialiseMap();
	}

	public GameInstance(GameManager gameManager, DuosMaps map) {
		this.gameManager = gameManager;
		this.duosMap = map;
		this.state = GameState.WAITING; // Default game state
		this.gameType = GameType.NORMAL;
		this.players = new ArrayList<Player>();
		this.winnerList = new ArrayList<Player>();
		this.spectators = new ArrayList<Player>();
		classes = new HashMap<>();
		oldClasses = new HashMap<>();
		team = new HashMap<Player, String>();
		redTeam = new ArrayList<Player>();
		blueTeam = new ArrayList<Player>();
		blackTeam = new ArrayList<Player>();
		InitialiseMap();
	}

	public GameManager getManager() {
		return gameManager;
	}

	public Maps getMap() {
		return this.map;
	}

	public DuosMaps getDuosMap() {
		return this.duosMap;
	}

	public World getMapWorld() {
		return mapWorld;
	}

	public void setSign(Sign s) {
		this.s = s;
	}

	public void InitialiseMap() {
		WorldCreator w = null;
		if (map != null)
			w = new WorldCreator(map.GetInstance().worldName).environment(World.Environment.NORMAL);
		else
			w = new WorldCreator(duosMap.GetInstance().worldName).environment(World.Environment.NORMAL);
		w.generator(new VoidGenerator());
		mapWorld = Bukkit.getServer().createWorld(w);
	}

	public Location GetLobbyLoc() {
		MapInstance mapInstance = null;
		if (map != null)
			mapInstance = map.GetInstance();
		else
			mapInstance = duosMap.GetInstance();
		Vector v = mapInstance.lobbyLoc;

		return new Location(mapWorld, v.getX(), v.getY(), v.getZ());
	}

	public boolean isOpen() {
		return state == GameState.WAITING && this.players.size() < gameType.getMaxPlayers();
	}

	@EventHandler
	public void SendPlayerToMap(Player player) {
		player.teleport(GetLobbyLoc());
	}

	public GameReason AddSpectator(Player player) {
		if (state == GameState.STARTED) {
			if (!players.contains(player)) {
				spectators.add(player);
				for (Player gamePlayer : players) {
					gamePlayer.hidePlayer(player);
				}
				player.getInventory().clear();
				player.setAllowFlight(true);
				player.teleport(GetSpecLoc());
				gameManager.getMain().board.get(player).delete();
				setGameScore(player);
				player.setDisplayName("" + player.getName() + " " + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC
						+ "Spectator" + ChatColor.RESET);
				return GameReason.SPECTATOR;
			} else
				return GameReason.ALREADY_IN;
		} else
			return GameReason.FAIL;
	}

	public void SetWaitingScoreboard(Player player) {
		FastBoard board = new FastBoard(player);
		boards.put(player, board);

		if (map != null) {
			board.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + map.toString()
					+ (map.GetInstance().gameType == GameType.FRENZY
							? "" + ChatColor.GRAY + ChatColor.ITALIC + " (frenzy)"
							: ""));
			board.updateLines("", "" + ChatColor.RESET + ChatColor.BOLD + "Class:", " " + ChatColor.RESET + "Random",
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Players:",
					" " + ChatColor.RESET
							+ (map.GetInstance().gameType == GameType.FRENZY
									? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers()
									: "")
							+ (map.GetInstance().gameType == GameType.NORMAL
									? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers()
									: "")
							+ (map.GetInstance().gameType == GameType.DUEL
									? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers()
									: ""),
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Status:",
					"" + ChatColor.RESET + ChatColor.ITALIC + " Waiting..");

			boards.get(player)
					.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + map.toString()
							+ (map.GetInstance().gameType == GameType.FRENZY
									? "" + ChatColor.GRAY + ChatColor.ITALIC + " (frenzy)"
									: ""));
		} else {
			board.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
			board.updateLines("", "" + ChatColor.RESET + ChatColor.BOLD + "Class:", " " + ChatColor.RESET + "Random",
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Players:",
					" " + ChatColor.RESET + players.size() + "/6", "",
					"" + ChatColor.RESET + ChatColor.BOLD + "Status:",
					"" + ChatColor.RESET + ChatColor.ITALIC + " Waiting..");
		}

	}

	public void removeArmor(Player player) {
		player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
		player.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
		player.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
		player.getInventory().setBoots(new ItemStack(Material.AIR, 1));
	}

	public GameReason AddPlayer(Player player) {
		if (state == GameState.WAITING) {
			if (!players.contains(player)) {
				if (this.map != null) {
					if (gameType == GameType.DUEL && players.size() >= 2) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "This game is full!");
						return GameReason.FULL;
					}
					if (!(player.hasPermission("scb.bypassFull"))) {
						if (gameType == GameType.NORMAL && players.size() >= 5) {
							player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "This game is full!");
							return GameReason.FULL;
						}
					}
					players.add(player);
					player.sendMessage(
							this.gameManager.getMain().color("&2&l(!) &rYou have joined &r&l" + map.toString()));
				} else if (this.duosMap != null) {
					if (players.size() >= 6) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "This game is full!");
						return GameReason.FULL;
					}
					players.add(player);
					player.sendMessage(
							this.gameManager.getMain().color("&2&l(!) &rYou have joined &r&l" + duosMap.toString()));
					player.sendMessage(this.gameManager.getMain().color("&2&l(!) &rSelect a team in your 2nd slot!"));
				}

				for (Player gamePlayer : players) {
					if (gamePlayer.getWorld() != mapWorld) {
						SendPlayerToMap(gamePlayer);
						CheckForGameStart(gamePlayer);
						SetWaitingScoreboard(gamePlayer);
					}

					if (gamePlayer != player) {
						if (map != null) {
							boards.get(gamePlayer)
									.updateLine(5, " "
											+ (map.GetInstance().gameType == GameType.FRENZY
													? "" + ChatColor.RESET + players.size() + "/"
															+ gameType.getMaxPlayers()
													: "")
											+ (map.GetInstance().gameType == GameType.NORMAL
													? "" + ChatColor.RESET + players.size() + "/"
															+ gameType.getMaxPlayers()
													: "")
											+ (map.GetInstance().gameType == GameType.DUEL
													? "" + ChatColor.RESET + players.size() + "/"
															+ gameType.getMaxPlayers()
													: ""));
							boards.get(gamePlayer)
									.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + map.toString()
											+ (map.GetInstance().gameType == GameType.FRENZY
													? "" + ChatColor.GRAY + ChatColor.ITALIC + " (frenzy)"
													: ""));
						} else
							boards.get(gamePlayer).updateLine(5, " " + ChatColor.RESET + players.size() + "/6");
					}
					removeArmor(player);

					if (map != null) {
						gamePlayer.sendMessage(
								"" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + player.getName()
										+ ChatColor.GREEN + " joined " + ChatColor.RED + "(" + ChatColor.GREEN
										+ (map.GetInstance().gameType == GameType.FRENZY
												? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers()
												: "")
										+ (map.GetInstance().gameType == GameType.NORMAL
												? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers()
												: "")
										+ (map.GetInstance().gameType == GameType.DUEL
												? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers()
												: "")
										+ ChatColor.RED + ")");
						if (gameType == GameType.FRENZY) {
							player.sendTitle("" + ChatColor.YELLOW + ChatColor.BOLD + map.toString(),
									"" + ChatColor.GREEN + "Your class will be randomly selected!");
						} else {
							player.sendTitle("" + ChatColor.YELLOW + ChatColor.BOLD + map.toString(),
									"" + ChatColor.GREEN + "Choose your class!");
						}
					} else {
						player.sendTitle("" + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString(),
								"" + ChatColor.GREEN + "Choose your class!");
						gamePlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ player.getName() + ChatColor.GREEN + " joined " + ChatColor.RED + "("
								+ ChatColor.GREEN + players.size() + "/6" + ChatColor.RED + ")");
					}

				}
				return GameReason.SUCCESS;
			} else
				return GameReason.ALREADY_IN;

		} else
			return GameReason.ALREADYPLAYING;
	}

	public FastBoard board;

	public void setClass(Player player, ClassType type) {
		classSelection.put(player, type);
		if (gameType != GameType.FRENZY) {
			board = boards.get(player);
			board.updateLine(2, " " + type.getTag());

			if (player.hasPermission("scb.chat"))
				player.setDisplayName("" + player.getName() + " " + type.getTag());
			else
				player.setDisplayName("" + player.getName() + " " + type.getTag() + ChatColor.GRAY);
		}
	}

	public void CheckForGameStart(Player player) {
		if (map != null) {
			if (players.size() == 2)
				StartGameTimer(player);
		} else {
			if (players.size() == 4)
				StartGameTimer(player);
		}
	}

	public int getSecondsUntilStart() {
		if (gameManager.getMain().tournament == false)
			return ticksTilStart = 30;
		return ticksTilStart = 60;
	}

	@EventHandler
	public void StartGameTimer(Player player) {
		if (gameStartTime == null) {
			ticksTilStart = getSecondsUntilStart();
			gameStartTime = new BukkitRunnable() {

				@Override
				public void run() {
					if (s != null) {
						s.setLine(3, getManager().getMain().color("&0" + ticksTilStart + "s"));
						s.update();
					}
					int ticks = ticksTilStart;
					if (ticks == 0) {
						StartGame();
						GameScoreboard();
						gameStartTime = null;
						this.cancel();
					} else if (ticks == 60) {
						if (gameManager.getMain().tournament == true) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "The game is now starting..");
						}
					} else if (ticks == 30) {
						for (Player gamePlayer : players) {
							if (!(gamePlayer.getInventory().contains(paper)))
								gamePlayer.getInventory().addItem(paper);
						}

						String mapName = "";

						if (map != null)
							mapName = map.toString();
						else
							mapName = duosMap.toString();

						if (gameType == GameType.FRENZY) {
							Bukkit.broadcastMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
									+ ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + "A " + ChatColor.RESET
									+ ChatColor.GRAY + ChatColor.ITALIC + "Frenzy " + ChatColor.GREEN + ChatColor.BOLD
									+ "game on " + ChatColor.RESET + ChatColor.BOLD + mapName + ChatColor.RESET
									+ ChatColor.GREEN + ChatColor.BOLD + " is starting in 30 seconds.");
							Bukkit.broadcastMessage(
									"" + "    " + ChatColor.GREEN + ChatColor.BOLD + " Use " + ChatColor.RESET
											+ "/join " + mapName + ChatColor.GREEN + ChatColor.BOLD + " to join!");
						} else if (gameType == GameType.NORMAL) {
							Bukkit.broadcastMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
									+ ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + "A game on "
									+ ChatColor.RESET + ChatColor.BOLD + mapName + ChatColor.RESET + ChatColor.GREEN
									+ ChatColor.BOLD + " is starting in 30 seconds.");
							Bukkit.broadcastMessage(
									"" + "    " + ChatColor.GREEN + ChatColor.BOLD + " Use " + ChatColor.RESET
											+ "/join " + mapName + ChatColor.GREEN + ChatColor.BOLD + " to join!");
						}
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "All players have joined. Game is now starting!");

						for (Player player : players)
							player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
					} else if (ticks == 25 || ticks == 20 || ticks == 15 || ticks == 10)
						player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);

					else if (ticks == 5 || ticks == 4 || ticks == 3 || ticks == 2) {
						for (Player player : players)
							player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
					} else if (ticks == 1)
						for (Player player : players)
							player.playSound(player.getLocation(), Sound.NOTE_PLING, 5, 7);

					else if (ticks == 18) {
						Random rand = new Random();
						int chance = rand.nextInt(2);

						if (chance == 0) {
							TellAll("" + ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Tip" + ChatColor.DARK_GREEN
									+ "] " + ChatColor.RESET + ChatColor.LIGHT_PURPLE
									+ "Execute double jump by tapping the space bar twice!");
						} else if (chance == 1) {
							TellAll("" + ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Tip" + ChatColor.DARK_GREEN
									+ "] " + ChatColor.RESET + ChatColor.LIGHT_PURPLE
									+ "Consider purchasing a rank at our /store for more SCB features!");
						} else if (chance == 2) {
							TellAll("" + ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Tip" + ChatColor.DARK_GREEN
									+ "] " + ChatColor.RESET + ChatColor.LIGHT_PURPLE
									+ "Be sure to select a class by using the sign or compass!");
						}
						for (Player player : players)
							player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
					}

					for (Player player : players)
						player.setLevel(ticks);
					if (ticks <= 5 && ticks >= 1)
						player.sendTitle("" + ChatColor.GREEN + ticks, "");
					if (ticks <= 60 && ticks >= 1) {
						if (players.size() >= 2) {
							for (Player player : players) {
								FastBoard board = boards.get(player);
								board.updateLine(8, " " + ticksTilStart + "s");
								board.updateLine(7, "" + ChatColor.RESET + ChatColor.BOLD + "Starting In:");
								if (players.size() >= 2)
									if (!(player.getInventory().contains(paper))
											&& !(player.getInventory().contains(paper2)))
										player.getInventory().addItem(paper);
							}
						}
					}

					for (Player gamePlayer : players) {
						if (ticks <= 60) {
							if (totalVotes == players.size()) {
								this.cancel();
								StartGame();
								gamePlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
										+ ChatColor.RESET + "Game is now starting");
							}
						}
					}

					ticksTilStart--;
				}
			};
			gameStartTime.runTaskTimer(gameManager.getMain(), 0, 20);
		}
	}

	public Location GetRespawnLoc() {
		// Respawn location for each map
		MapInstance mapInstance = null;

		if (map != null)
			mapInstance = map.GetInstance();
		else
			mapInstance = duosMap.GetInstance();

		if (mapInstance.spawnPos.size() == 0)
			return GetLobbyLoc().add(new Vector(42, 2, 2.5));
		else {
			Vector spawnPos = mapInstance.spawnPos.get(random.nextInt(mapInstance.spawnPos.size()));
			return new Location(mapWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
		}
	}

	public Location GetSpecLoc() {
		// This is the spectator location for each map
		MapInstance mapInstance = null;

		if (map != null)
			mapInstance = map.GetInstance();
		else
			mapInstance = duosMap.GetInstance();
		Vector v = mapInstance.specLoc;

		return new Location(mapWorld, v.getX(), v.getY(), v.getZ());
	}

	public double boundsX, boundsZ;
	public BukkitRunnable moveBar;
	public int resetBoundsX = 0;
	public int resetBoundsZ = 0;
	public int barrierTicks = 0;

	/*
	 * public void moveBarrier() { if (moveBar == null) { moveBar = new
	 * BukkitRunnable() {
	 * 
	 * @Override public void run() { if (state == GameState.ENDED) { MapInstance
	 * instance = map.GetInstance(); if (resetBoundsX != 0) instance.boundsX =
	 * resetBoundsX + instance.boundsX; if (resetBoundsZ != 0) instance.boundsZ =
	 * resetBoundsZ + instance.boundsZ; moveBar = null; this.cancel(); }
	 * 
	 * if (barrierTicks == 600) { for (Player gamePlayer : players) { int minutes =
	 * (barrierTicks / 120); gamePlayer.sendTitle(
	 * gameManager.getMain().color("&9Barrier will begin moving in &r" + minutes +
	 * "m"), ""); gamePlayer.sendMessage(
	 * gameManager.getMain().color("&9Barrier will begin moving in &r" + minutes +
	 * "m")); String msg =
	 * getManager().getMain().color("&9&l(!) &eBarrier will begin moving in &r");
	 * PacketPlayOutChat packet = new PacketPlayOutChat(
	 * ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2); CraftPlayer craft
	 * = (CraftPlayer) gamePlayer;
	 * craft.getHandle().playerConnection.sendPacket(packet); } } else if
	 * (barrierTicks == 840) { for (Player gamePlayer : players) { int seconds =
	 * (barrierTicks + 20) - 800; gamePlayer.sendTitle(
	 * gameManager.getMain().color("&9Barrier will begin moving in &r" + seconds +
	 * "s"), ""); gamePlayer.sendMessage(
	 * gameManager.getMain().color("&9Barrier will begin moving in &r" + seconds +
	 * "s")); } } else if (barrierTicks == 900) { for (Player gamePlayer : players)
	 * { gamePlayer.sendTitle(gameManager.getMain().
	 * color("&9Barrier moving is now active!"), "");
	 * gamePlayer.sendMessage(gameManager.getMain().
	 * color("&9Barrier moving is now active!")); MapInstance mapInstance =
	 * map.GetInstance(); mapInstance.boundsX -= 3; resetBoundsX += 3;
	 * mapInstance.boundsZ -= 3; resetBoundsZ += 3; } } else if (barrierTicks ==
	 * 915) { for (Player gamePlayer : players) { barrierTicks = 480; int seconds =
	 * (barrierTicks / 60); // isInBounds(gamePlayer.getLocation());
	 * gamePlayer.sendTitle(gameManager.getMain().
	 * color("&9Barrier has been moved &r3 blocks"),
	 * gameManager.getMain().color("&eNext barrier move will be in &r2m"));
	 * gamePlayer.sendMessage(gameManager.getMain().
	 * color("&9Barrier has been moved &r3 blocks"));
	 * gamePlayer.sendMessage(gameManager.getMain().
	 * color("&eNext barrier move will be in &r2m")); } }
	 * 
	 * barrierTicks++; } }; moveBar.runTaskTimer(gameManager.getMain(), 0, 20); } }
	 */

	public boolean isInBounds(Location loc) {
		MapInstance mapInstance = null;

		if (map != null)
			mapInstance = map.GetInstance();
		else
			mapInstance = duosMap.GetInstance();
		Vector v = mapInstance.center;
		Location centre = new Location(mapWorld, v.getX(), v.getY(), v.getZ());
		boundsX = mapInstance.boundsX;
		boundsZ = mapInstance.boundsZ;

		if (Math.abs(centre.getX() - loc.getX()) > boundsX)
			return false;
		if (Math.abs(centre.getZ() - loc.getZ()) > boundsZ)
			return false;
		return true;
	}

	public void StartGame() {
		if (s != null) {
			s.setLine(0, getManager().getMain().color("&2In Progress"));
			s.setLine(3, "" + ChatColor.BLACK + ChatColor.UNDERLINE + "Spectate");
			s.update();
		}
		for (Player gamePlayer : players) {
			PlayerData data = gameManager.getMain().getDataManager().getPlayerData(gamePlayer);

			if (data != null) {
				data.votes = 0;
				gamePlayer.setLevel(data.level);
			}

			if (this.duosMap != null) {
				if (!(this.team.containsKey(gamePlayer))) {
					if (redTeam.size() == 1 || redTeam.isEmpty()) {
						redTeam.add(gamePlayer);
						team.put(gamePlayer, "Red");
						gamePlayer.sendMessage(this.gameManager.getMain().color("&2&l(!) &rYou are on &c&lRed Team"));
					} else if (blueTeam.size() == 1 || blueTeam.isEmpty()) {
						blueTeam.add(gamePlayer);
						team.put(gamePlayer, "Blue");
						gamePlayer.sendMessage(this.gameManager.getMain().color("&2&l(!) &rYou are on &b&lBlue Team"));
					} else if (blackTeam.size() == 1 || blackTeam.isEmpty()) {
						blackTeam.add(gamePlayer);
						team.put(gamePlayer, "Black");
						gamePlayer.sendMessage(this.gameManager.getMain().color("&2&l(!) &rYou are on &0&lBlack Team"));
					}
				}
			}
		}
		totalVotes = 0;
		startLightningDropsTimer();
		for (Player player : players) {
			player.getInventory().clear();
			player.teleport(GetRespawnLoc());
		}

		TellAll("" + ChatColor.BOLD + "===============================");
		TellAll("" + ChatColor.BOLD + "||");
		TellAll("" + ChatColor.BOLD + "||");
		TellAll("" + ChatColor.BOLD + "||");
		TellAll("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD + "  GAME STARTED");
		if (gameType == GameType.DUEL) {
			String playersInGame = "";
			for (Player gamePlayer : players) {
				playersInGame += gamePlayer.getName() + "";
				if (!players.isEmpty())
					playersInGame += ", ";
			}
			TellAll("" + ChatColor.BOLD + "|| " + "    " + ChatColor.RED + ChatColor.BOLD + " Players: "
					+ ChatColor.RESET + playersInGame);
		}
		TellAll("" + ChatColor.BOLD + "||");
		TellAll("" + ChatColor.BOLD + "||");
		TellAll("" + ChatColor.BOLD + "||");
		TellAll("" + ChatColor.BOLD + "===============================");
		state = GameState.STARTED;
		LoadClasses();
		// gameManager.getMain().getNPCManager().updateRandomNpc();

		for (Player player2 : players) {
			BaseClass bc = this.classes.get(player2);

			if (bc != null) {
				if (bc.getType() == ClassType.Melon)
					player2.getInventory().setItem(2, this.getItemToDrop());
				else
					player2.getInventory().addItem(this.getItemToDrop());
			}
		}

		GameScoreboard();
		alivePlayers = players.size();
		if (redTeam != null) {
			if (redTeam.size() > 0) {
				aliveTeams++;
			}
		}
		if (blueTeam != null) {
			if (blueTeam.size() > 0) {
				aliveTeams++;
			}
		}
		if (blackTeam != null) {
			if (blackTeam.size() > 0) {
				aliveTeams++;
			}
		}

		for (Player player : players)
			player.setGameMode(GameMode.ADVENTURE);

		BukkitRunnable runnable = new BukkitRunnable() {

			@Override
			public void run() {
				if (gameTicks % 1200 == 0) {
					time.getScoreboard().resetScores(time.getEntry());
					time = o.getScore("" + ChatColor.YELLOW + "Game Time: " + ChatColor.RESET + gameTime + "m");
					time.setScore(0);
					gameTime++;
				}

				for (Entry<Player, BaseClass> playerClass : classes.entrySet())
					playerClass.getValue().Tick(gameTicks);

				for (Entity e : mapWorld.getEntities()) {
					if (e instanceof Arrow) {
						Arrow a = (Arrow) e;

						if (a.isOnGround())
							e.remove();
					}
				}

				if (gameTicks % 20 == 0) {
					for (Player gamePlayer : players) {
						if (gamePlayer.getFireTicks() >= 200) {
							gamePlayer.setFireTicks(110);
						}
					}
				}
				gameTicks++;
			}

		};
		runnable.runTaskTimer(gameManager.getMain(), 0, 1);
		runnables.add(runnable);
		for (Player gamePlayer : players)
			sendScoreboardUpdate(gamePlayer);
	}

	private String shortenString(String msg, int length) {
		if (msg.length() <= length)
			return msg;
		else
			return msg.substring(0, length);
	}

	@SuppressWarnings("deprecation")
	public void sendScoreboardUpdate(Player player) {
		if (map != null) {
			for (Player pl : Bukkit.getOnlinePlayers()) {
				Scoreboard board = pl.getScoreboard();
				Team team = board.getTeam(player.getName());
				if (team == null) {
					team = board.registerNewTeam(player.getName());
					team.addPlayer(player);
				}
				BaseClass baseClass = classes.get(player);
				String type = shortenString(baseClass.getType().getTag(), 12) + " " + ChatColor.RESET;
				team.setPrefix(type);
			}
		} else {
			for (Player pl : Bukkit.getOnlinePlayers()) {
				Scoreboard board = pl.getScoreboard();
				Team team = board.getTeam(player.getName());
				if (team == null) {
					team = board.registerNewTeam(player.getName());
					team.addPlayer(player);
				}
			}
		}
	}

	public Location getItemSpawnLoc() {
		Random rand = new Random();
		int attempts = 0;
		Location respawnLoc = GetRespawnLoc();
		while (true) {
			Location loc = respawnLoc.clone().add(rand.nextFloat() * 50 - 25, 10, rand.nextFloat() * 50 - 25);
			while (true) {
				loc.setY(loc.getY() - 1);
				Material mat = loc.getBlock().getType();
				if (mat.isSolid()) {
					return loc.add(0, 1, 0);
				}
				if (loc.getY() < 40) // Too low down without finding block
					break;
			}
			if (attempts > 500)
				return respawnLoc;
			attempts++;
		}
	}

	public void startLightningDropsTimer() {
		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				Location loc = getItemSpawnLoc();
				ItemStack drop = getItemToDrop();
				Item item = mapWorld.dropItem(loc, drop);
				item.setVelocity(new Vector(0, 0, 0));
				mapWorld.strikeLightningEffect(loc);

				int x = (int) loc.getX();
				int y = (int) loc.getY();
				int z = (int) loc.getZ();
				loc = new Location(mapWorld, x, y, z);
				recentDrop = loc;

				for (Player gamePlayer : players) {
					BaseClass bc = classes.get(gamePlayer);

					if (bc != null) {
						if (bc.getType() == ClassType.DarkSethBling) {
							DarkSethBlingClass d = (DarkSethBlingClass) bc;

							if (d.usedTp == false) {
								gamePlayer.sendMessage(getManager().getMain()
										.color("&2&l(!) &eAn item just spawned at &e&l" + x + ", " + y + ", " + z));
							}
						}
					}
				}
			}
		};
		runnable.runTaskTimer(gameManager.getMain(), 20 * 30, 20 * 30);
		runnables.add(runnable);
	}

	public ItemStack getItemToDrop() {

		ItemStack item5 = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				"" + ChatColor.RED + ChatColor.BOLD + "Slowness", "");
		Potion pot5 = new Potion(3);
		pot5.setType(PotionType.SLOWNESS);
		pot5.setSplash(true);
		pot5.apply(item5);

		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				"" + ChatColor.YELLOW + ChatColor.BOLD + "Instant Heal");
		Potion pot = new Potion(1);
		pot.setType(PotionType.INSTANT_HEAL);
		pot.setSplash(true);
		pot.apply(item);

		ItemStack item2 = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				"" + ChatColor.GREEN + ChatColor.BOLD + "Speed Pot");
		Potion pot2 = new Potion(1);
		pot2.setType(PotionType.SPEED);
		pot2.setSplash(true);
		pot2.apply(item2);

		ItemStack broom = ItemHelper.setDetails(new ItemStack(Material.WHEAT, 4),
				this.getManager().getMain().color("&0&lBroom"));

		ItemStack item6 = ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD, 1, (short) 250),
						"" + ChatColor.YELLOW + ChatColor.BOLD + "HAMMER", ChatColor.YELLOW + ""),
				Enchantment.KNOCKBACK, 10);
		item5.getDurability();

		ItemStack item7 = ItemHelper.setDetails(new ItemStack(Material.DIAMOND_HOE, 3, (short) 250),
				"" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Bazooka", ChatColor.YELLOW + "");
		item5.getDurability();

		ItemStack extraLife = ItemHelper.setDetails(new ItemStack(Material.PRISMARINE_SHARD),
				"" + ChatColor.RESET + ChatColor.BOLD + "Extra Life");
		ItemStack pearl = ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL),
				"" + ChatColor.RED + ChatColor.BOLD + "Teleporter");

		ItemStack item8 = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				"" + ChatColor.RED + ChatColor.BOLD + "Bomb");
		Potion pot8 = new Potion(1);
		pot8.setType(PotionType.INSTANT_DAMAGE);
		pot8.setSplash(true);
		pot8.apply(item8);

		ItemStack fireRes = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				"" + ChatColor.RED + ChatColor.BOLD + "Fire Resistance");
		Potion pot9 = new Potion(1);
		pot9.setType(PotionType.FIRE_RESISTANCE);
		pot9.setSplash(true);
		PotionMeta meta2 = (PotionMeta) fireRes.getItemMeta();
		meta2.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0), true);
		fireRes.setItemMeta(meta2);
		pot9.apply(fireRes);

		ItemStack slowballs = ItemHelper.setDetails(new ItemStack(Material.SNOW_BALL, 8),
				"" + ChatColor.RED + ChatColor.BOLD + "Slowballs");

		ItemStack miniShield = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				"" + ChatColor.YELLOW + "Mini-Shield Potion");

		ItemStack bounty = ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR, 1),
				"" + ChatColor.YELLOW + "Bounty");

		ItemStack blooper = ItemHelper.setDetails(new ItemStack(Material.RABBIT_FOOT),
				gameManager.getMain().color("&6&lBlooper"));
		ItemStack nuke = ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.TNT, 3), this.getManager().getMain().color("&4&lNuke")),
				Enchantment.DAMAGE_ALL, 1);
		ItemStack bomb = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				getManager().getMain().color("&4&lBomb"));
		ItemStack instagib = ItemHelper.setDetails(new ItemStack(Material.GOLD_HOE, 5, (short) 250),
				"" + ChatColor.GREEN + ChatColor.ITALIC + "Instagib", ChatColor.YELLOW + "");
		instagib.getDurability();

		Potion pot100 = new Potion(1);
		pot100.setType(PotionType.INSTANT_DAMAGE);
		pot100.setSplash(true);
		PotionMeta meta = (PotionMeta) bomb.getItemMeta();
		meta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 100, 1000), true);
		bomb.setItemMeta(meta);
		pot100.apply(bomb);

		Potion pot1000 = new Potion(1);
		pot1000.setType(PotionType.INSTANT_HEAL);
		pot1000.setSplash(true);
		PotionMeta potMeta = (PotionMeta) item.getItemMeta();
		potMeta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 0, 1), true);
		item.setItemMeta(potMeta);
		pot1000.apply(item);

		Potion speedPot = new Potion(1);
		speedPot.setType(PotionType.SPEED);
		speedPot.setSplash(true);
		PotionMeta speedMeta = (PotionMeta) item2.getItemMeta();
		speedMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1), true);
		item2.setItemMeta(speedMeta);
		speedPot.apply(item2);

		ItemStack zombie = new ItemStack(Material.MONSTER_EGG, 1, EntityType.ZOMBIE.getTypeId());
		ItemMeta zMeta = zombie.getItemMeta();
		zMeta.setDisplayName("" + ChatColor.RESET + ChatColor.ITALIC + "Zombie Pokeball");
		zombie.setItemMeta(zMeta);

		ItemStack witch = new ItemStack(Material.MONSTER_EGG, 1, EntityType.WITCH.getTypeId());
		ItemMeta wMeta = witch.getItemMeta();
		wMeta.setDisplayName("" + ChatColor.RESET + ChatColor.ITALIC + "Witch Pokeball");
		witch.setItemMeta(wMeta);

		ItemStack skeleton = new ItemStack(Material.MONSTER_EGG, 1, EntityType.SKELETON.getTypeId());
		ItemMeta sMeta = skeleton.getItemMeta();
		sMeta.setDisplayName("" + ChatColor.RESET + ChatColor.ITALIC + "Skeleton Pokeball");
		skeleton.setItemMeta(sMeta);

		List<ItemStack> items = Arrays.asList(new ItemStack(Material.GOLDEN_APPLE),

				ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1),
						"" + ChatColor.BLACK + ChatColor.BOLD + "Notch Apple"),
				item5, item7, item7, item, item2, item5, item5, item2, item7, new ItemStack(Material.GOLDEN_APPLE),
				item6, item, extraLife, item, new ItemStack(Material.MILK_BUCKET), new ItemStack(Material.MILK_BUCKET),
				new ItemStack(Material.MILK_BUCKET), blooper, blooper, blooper, blooper, nuke, nuke, nuke, nuke, nuke,
				bomb, pearl, pearl, miniShield, miniShield, slowballs, slowballs, slowballs, fireRes, fireRes, instagib,
				instagib, instagib, broom, broom, zombie, zombie, zombie, skeleton, skeleton, witch, bounty);
		return items.get(random.nextInt(items.size()));
	}

	public String truncateString(String string, int length) {
		if (string.length() <= length)
			return string;
		else
			return string.substring(0, length);
	}

	private Scoreboard c;
	private Score time;
	private Objective o;
	private BukkitRunnable eventRunnable;

	public void GameScoreboard() {
		try {
			ScoreboardManager m = Bukkit.getScoreboardManager();
			c = m.getNewScoreboard();

			if (map != null)
				o = c.registerNewObjective("" + ChatColor.BOLD + map.toString(), "");
			else
				o = c.registerNewObjective("" + ChatColor.BOLD + duosMap.toString(), "");
			livesObjective = o;
			o.setDisplaySlot(DisplaySlot.SIDEBAR);

			for (Player player : players) {
				BaseClass playerClass = classes.get(player);
				PlayerData data = gameManager.getMain().getDataManager().getPlayerData(player);

				if (map != null) {
					if (data != null) {
						if (data.blue == 1) {
							Score livesScore = o.getScore(truncateString(
									playerClass.getType().getTag() + " " + ChatColor.BLUE + player.getName() + "", 38));
							livesScore.setScore(5);
							playerClass.score = livesScore;
						} else if (data.red == 1) {
							Score livesScore = o.getScore(truncateString(
									playerClass.getType().getTag() + " " + ChatColor.RED + player.getName() + "", 38));
							livesScore.setScore(5);
							playerClass.score = livesScore;
						} else if (data.green == 1) {
							Score livesScore = o.getScore(truncateString(
									playerClass.getType().getTag() + " " + ChatColor.GREEN + player.getName() + "",
									38));
							livesScore.setScore(5);
							playerClass.score = livesScore;
						} else if (data.yellow == 1) {
							Score livesScore = o.getScore(truncateString(
									playerClass.getType().getTag() + " " + ChatColor.YELLOW + player.getName() + "",
									38));
							livesScore.setScore(5);
							playerClass.score = livesScore;
						} else {
							Score livesScore = o.getScore(truncateString(
									playerClass.getType().getTag() + " " + ChatColor.WHITE + player.getName() + "",
									38));
							livesScore.setScore(5);
							playerClass.score = livesScore;
						}
					}
				} else {
					if (team.get(player).equals("Blue"))
						boardColor(o, player, ChatColor.BLUE);
					else if (team.get(player).equals("Red"))
						boardColor(o, player, ChatColor.RED);
					else if (team.get(player).equals("Black"))
						boardColor(o, player, ChatColor.BLACK);
				}
				time = o.getScore("" + ChatColor.YELLOW + "Game Time: " + ChatColor.RESET + gameTime + "m");
				time.setScore(0);
				player.setScoreboard(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void boardColor(Objective o, Player player, ChatColor c) {
		BaseClass bc = classes.get(player);

		if (bc != null) {
			Score livesScore = o.getScore(truncateString(bc.getType().getTag() + " " + c + player.getName() + "", 38));
			livesScore.setScore(5);
			bc.score = livesScore;
		}
	}

	private void setGameScore(Player player) {
		player.setScoreboard(c); // For joining spectators
	}

	public int aliveGamePlayers = 0;
	public int teamsAlive = 0;

	public void CheckForWin() {
		List<String> aliveTeam = new ArrayList<String>();

		if (map != null) {
			int alivePlayers = 0;
			Player alivePlayer = null;
			List<Player> lastAlive = new ArrayList<Player>();
			for (Entry<Player, BaseClass> entry : classes.entrySet())
				if (entry.getValue().getLives() > 0) {
					alivePlayers++;
					alivePlayer = entry.getKey();
					lastAlive.add(alivePlayer);
				}
			if (alivePlayers == 1 && alivePlayer != null && lastAlive.size() == 1) {
				WinGame(lastAlive);
			} else if (alivePlayers <= 0)
				EndGame();

			this.alivePlayers = alivePlayers;
		} else {
			teamsAlive = 0;
			for (Entry<Player, BaseClass> entry : classes.entrySet()) {
				if (entry.getValue().getLives() > 0) {
					if (!(aliveTeam.contains(team.get(entry.getKey())))) {
						aliveTeam.add(team.get(entry.getKey()));
						teamsAlive++;
					}
				}
			}

			if (teamsAlive == 1) {
				if (aliveTeam.contains("Red"))
					WinGame(redTeam);
				else if (aliveTeam.contains("Blue"))
					WinGame(blueTeam);
				else if (aliveTeam.contains("Black"))
					WinGame(blackTeam);
			} else if (teamsAlive == 0)
				EndGame();
		}
	}

	public void PlayerDeath(Player player) {
		this.blindness = 0;
		if (gameType == GameType.FRENZY) {
			rerandomizeClass(player);
		}
		BaseClass baseClass = classes.get(player);
		if (baseClass != null) {
			try {
				player.getInventory().clear();
				PlayerDeathEvent event = new PlayerDeathEvent(player, null, 0, null);
				player.setGameMode(GameMode.SPECTATOR);

				if (baseClass.isDead == false) {
					baseClass.isDead = true;
					baseClass.Death(event);
					player.teleport(GetSpecLoc());
				}

				BukkitRunnable runTimer = new BukkitRunnable() {
					int ticks = 3;

					@SuppressWarnings("deprecation")
					@Override
					public void run() {
						if (state == GameState.ENDED) {
							this.cancel();
						} else if (player.getWorld() != getMapWorld()) {
							this.cancel();
							player.setAllowFlight(true);
							player.setGameMode(GameMode.ADVENTURE);
							getManager().getMain().ResetPlayer(player);
						}

						if (baseClass.getLives() > 0) {
							if (ticks == 0) {
								player.teleport(GetRespawnLoc());
								player.setGameMode(GameMode.ADVENTURE);
								player.setHealth(20.0);
								player.setAllowFlight(true);
								getManager().spawnProtection2(player);

								if (!(players.contains(player))) {
									getManager().getMain().ResetPlayer(player);
								} else {
									if (gameType == GameType.FRENZY) {
										player.sendTitle("" + ChatColor.YELLOW + ChatColor.BOLD + "Respawned",
												"" + ChatColor.RESET + "Your new class for this life is "
														+ baseClass.getType().getTag());
									} else
										player.sendTitle("" + ChatColor.YELLOW + ChatColor.BOLD + "Respawned", "");

									baseClass.isDead = false;
								}
								this.cancel();
							} else if (ticks <= 3 && state == GameState.STARTED) {
								player.sendTitle("", "" + ChatColor.RED + ticks);
								player.setGameMode(GameMode.SPECTATOR);
							}
						}
						ticks--;
					}
				};
				runTimer.runTaskTimer(gameManager.getMain(), 0, 20);
				runnables.add(runTimer);

				if (player.getLastDamageCause() != null && player.getLastDamageCause().getCause() == DamageCause.VOID) {
					if (player.getKiller() != null)
						player.teleport(player.getKiller());
					else
						player.teleport(GetRespawnLoc());
				}

				player.setHealth(20.0);
				player.setAllowFlight(true);
				player.setGameMode(GameMode.ADVENTURE);
				if (baseClass.getLives() == 0) {
					playerPosition.add(player);
					if (players.size() > 2) {
						if (map != null)
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You are now spectating on " + ChatColor.GREEN + map.toString());
						else
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You are now spectating on " + ChatColor.GREEN + duosMap.toString());

						player.sendTitle("" + ChatColor.RED + "You have died!",
								"" + ChatColor.RESET + "You are now a Spectator");
						player.teleport(GetSpecLoc());
					}
					player.getPlayer().setGameMode(GameMode.ADVENTURE); // EDIT IF NEEDED
					player.spigot().setCollidesWithEntities(false);
					player.setAllowFlight(false);
					player.setAllowFlight(true);
					ItemStack spec = ItemHelper.setDetails(new ItemStack(Material.COMPASS),
							"" + ChatColor.GREEN + "Spectate a Player",
							ChatColor.GRAY + "Click to Spectate a specific player!");
					player.getInventory().setItem(0, spec);
					ItemStack leave = ItemHelper.setDetails(new ItemStack(Material.BARRIER),
							"" + ChatColor.RED + "Leave", ChatColor.GRAY + "Click to leave game");
					player.getInventory().setItem(8, leave);
					for (Player gamePlayer : players) {
						gamePlayer.hidePlayer(player);
					}
					try {
						baseClass.score.getScoreboard().resetScores(baseClass.score.getEntry());
					} catch (Exception e) {
						e.printStackTrace();
					}
					CheckForWin();
				} else
					baseClass.LoadPlayer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void EndGameAnimation(List<Player> winner) {
		String winners = "";

		if (winner.size() == 1)
			winners = "" + winner.get(0).getName();
		else if (winner.size() > 1)
			winners = "" + winner.get(0).getName() + " " + winner.get(1).getName();

		for (Player player : players) {
			if (!(winner.contains(player))) {
				player.setGameMode(GameMode.SPECTATOR);
				player.sendTitle("" + ChatColor.RED + ChatColor.BOLD + "GAME LOST",
						"" + winners + ChatColor.GREEN + " won the game!");
			}
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
			player.setDisplayName("" + player.getName());
		}
		for (Player w : winner) {
			w.sendTitle("" + ChatColor.YELLOW + ChatColor.BOLD + "VICTORY", "" + ChatColor.GREEN + "You won the game!");
			w.setDisplayName("" + w.getName());
		}
	}

	private BukkitRunnable endGameAnimation;
	public int count = 0;

	public void EndGame() {
		state = GameState.ENDED;
		// gameManager.getMain().getNPCManager().updateRandomNpc();
		for (Entity en : mapWorld.getEntities())
			if (!(en instanceof Player))
				en.remove();

		if (this.endGameAnimation == null) {
			this.endGameAnimation = new BukkitRunnable() {
				int ticks = 10;

				@Override
				public void run() {
					for (Player p : winnerList) {
						if (map != null) {
							if (p == null || p.getWorld() != mapWorld) {
								ticks = 0;
							}
						} else {
							if (p == null || p.getWorld() != mapWorld) {
								count += 1;

								if (winnerList.size() >= 2 && count >= 2)
									ticks = 0;
								else if (winnerList.size() == 1 && count == 1)
									ticks = 0;
							}
						}
					}
					if (ticks <= 10 && ticks > 0) {
						if (!(winnerList.isEmpty())) {
							for (Player w : winnerList) {
								Firework fw = (Firework) w.getWorld().spawnEntity(w.getLocation(), EntityType.FIREWORK);
								FireworkMeta fwm = fw.getFireworkMeta();
								fwm.setPower(1);

								Color c = null;
								Random r = new Random();
								int chance = r.nextInt(3);

								if (chance == 0)
									c = Color.BLUE;
								else if (chance == 1)
									c = Color.LIME;
								else if (chance == 2)
									c = Color.GREEN;
								else
									c = Color.YELLOW;
								fwm.addEffect(FireworkEffect.builder().withColor(c).flicker(true).build());
								fw.setFireworkMeta(fwm);
							}
						}
					}
					if (ticks == 0) {
						endGameAnimation = null;
						this.cancel();
						String mapName = "";

						if (map != null)
							mapName = map.toString();
						else
							mapName = duosMap.toString();

						for (Player gamePlayer : players) {
							gamePlayer.setAllowFlight(false);
							gamePlayer.setAllowFlight(true);
							for (Player player : Bukkit.getOnlinePlayers()) {
								if (gamePlayer != player) {
									gamePlayer.showPlayer(player);
								}
							}
						}

						for (Player spectator : spectators) {
							if (spectator.getWorld() == getMapWorld()) {
								gameManager.getMain().LobbyBoard(spectator);
								gameManager.getMain().LobbyItems(spectator);
								gameManager.getMain().SendPlayerToHub(spectator);
								spectator.setGameMode(GameMode.ADVENTURE);
								spectator.setAllowFlight(false);
								spectator.setAllowFlight(true);
								spectator.setDisplayName("" + spectator.getName());
								spectator.sendMessage(getManager().getMain().color("&2&l(!) &rThe game on &r&l"
										+ mapName + " &rhas ended. Moving you back to spawn.."));
								spectator.spigot().setCollidesWithEntities(true);
								for (Player p : Bukkit.getOnlinePlayers()) {
									p.showPlayer(spectator);
								}
							}
						}
						if (s != null) {
							s.setLine(0, getManager().getMain().color("&2Lobby"));
							s.setLine(1, getManager().getMain().color("&0" + mapName));
							if (map != null)
								s.setLine(2, getManager().getMain()
										.color("&0Players: 0/" + getMap().GetInstance().gameType.getMaxPlayers()));
							else
								s.setLine(2, getManager().getMain().color("&0Players: 0/6"));

							s.setLine(3, getManager().getMain().color("&030s"));
							s.update();
						}

						for (Player player : players) {
							gameManager.getMain().ResetPlayer(player);
							BaseClass bc = classes.get(player);
							bc.GameEnd();
							SetLobbyScoreboard(player);
							for (PotionEffect type : player.getActivePotionEffects())
								player.removePotionEffect(type.getType());

							removeArmor(player);
							gameManager.getMain().LobbyItems(player);
						}

						for (BukkitRunnable runnable : runnables)
							runnable.cancel();

						Bukkit.unloadWorld(mapWorld, false);
						if (map != null)
							gameManager.RemoveMap(map);
						else
							gameManager.RemoveDuosMap(duosMap);
					}

					ticks--;
				}
			};
			endGameAnimation.runTaskTimer(gameManager.getMain(), 0, 20);
		}
	}

	public void SetLobbyScoreboard(Player player) {
		gameManager.getMain().LobbyBoard(player);
	}

	public void givePositionTokens(int position, int tokens) {
		if (playerPosition.size() >= position) {
			Player player = playerPosition.get(playerPosition.size() - position);
			PlayerData data = gameManager.getMain().getDataManager().getPlayerData(player);
			BaseClass baseClass = classes.get(player);

			if (baseClass == null) {
				player.sendMessage("Your base class is fackin null");
			} else if (data != null) {
				data.tokens += tokens;
				baseClass.totalTokens += tokens;
			}
		}
	}

	public void givePositionNum(int position, String message) {
		if (playerPosition.size() >= position) {
			Player player = playerPosition.get(playerPosition.size() - position);
			player.sendMessage("" + ChatColor.BOLD + message);
		}
	}

	public void givePoints(int position, int points) {
		if (playerPosition.size() >= position) {
			Player player = playerPosition.get(playerPosition.size() - position);
			PlayerData data = gameManager.getMain().getDataManager().getPlayerData(player);

			if (data != null) {
				data.points += points;
			}
		}
	}

	public void WinGame(List<Player> winners) {
		// playerPosition.add(winner);
		PlayerData data3 = null;
		for (Player winner : winners) {
			data3 = gameManager.getMain().getDataManager().getPlayerData(winner);
			winnerList.add(winner);
			if (data3 != null) {
				if (data3.exp >= 2500) {
					data3.level++;
					data3.exp -= 2500;
					winner.sendMessage(getManager().getMain().color("&e&lLEVEL UPGRADED!"));
					winner.sendMessage("You are now Level: " + data3.level + "!");
				}

				BaseClass bc = classes.get(winner);
				if (data3.challenge1 == 0) {
					if (bc != null) {
						if (bc.getType() == ClassType.Pig) {
							winner.sendMessage(getManager().getMain()
									.color("&9&l(!) &rYou got a win with " + bc.getType().getTag()
											+ " &rand have now unlocked the " + ClassType.Notch.getTag()
											+ " &rclass!"));
							data3.challenge1 = 1;
							int classID = 29;
							ClassDetails details = data3.playerClasses.get(classID);

							if (details == null) {
								details = new ClassDetails();
								data3.playerClasses.put(classID, details);
							}
							details.setPurchased();
						}
					}
				}
				if (data3.challenge2 == 0) {
					winner.sendMessage(getManager().getMain()
							.color("&9&l(!) &rYou got a win and you are now rewarded with &e50 Bonus Tokens"));
					data3.challenge2 = 1;

					if (bc != null)
						bc.totalTokens += 50;
				}
			}

			Random r = new Random();
			int chance = r.nextInt(100);

			if (chance >= 0 && chance < 25) {
				if (data3 != null) {
					data3.mysteryChests++;
					winner.sendMessage(getManager().getMain().color("&5&l(!) &rYou have found &e1 Mystery Chest!"));
				}
			}
		}

		/*
		 * for (Player gamePlayer : players) { if
		 * (gamePlayer.hasPermission("scb.bonusTokens")) { PlayerData data =
		 * gameManager.getMain().getDataManager().getPlayerData(gamePlayer); data.tokens
		 * += 10; BaseClass baseClass = classes.get(gamePlayer); baseClass.totalTokens
		 * += 10; } }
		 */

		/*
		 * givePositionTokens(1, 10); givePositionTokens(2, 7); givePositionTokens(3,
		 * 5); givePositionTokens(4, 3); givePositionTokens(5, 1);
		 */

		if (getManager().getMain().tournament == true) {
			givePoints(1, 10);
			givePoints(2, 7);
			givePoints(3, 5);
			givePoints(4, 1);
		}

		for (Player winner : winners) {
			winner.getInventory().clear();
			winner.setGameMode(GameMode.ADVENTURE);
			BaseClass baseClass = classes.get(winner);
			if (baseClass != null) {
				if (baseClass.getLives() < 5) {
					PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winner);

					if (data != null) {
						data.wins += 1;
						data.winstreak += 1;
						data.exp += 113;
					}
					baseClass.totalExp += 113;

					winner.sendMessage("" + ChatColor.BOLD + "=====================");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD
							+ "  GAME WON");
					winner.sendMessage("        " + "  1st Place: 10 Tokens");
					baseClass.totalTokens += 10;

					if (baseClass.totalKills >= 0) {
						winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE + ChatColor.BOLD
								+ "  " + baseClass.totalKills + " Kills: " + ChatColor.RESET + ChatColor.YELLOW
								+ (baseClass.totalKills * 2) + " Tokens");
						baseClass.totalTokens += baseClass.totalKills;
					}

					if (winner.hasPermission("scb.rankBonusOne")) {
						winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE + ChatColor.BOLD
								+ "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW + "10 Tokens");
						baseClass.totalTokens += 10;
					}
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "=====================");
					winner.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.GREEN + "You have earned "
							+ baseClass.totalTokens + " Tokens!");

					if (data != null)
						data.tokens += baseClass.totalTokens;
				} else {
					PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winner);
					baseClass.totalTokens += 10;

					if (data != null) {
						data.wins += 1;
						data.flawlessWins += 1;
						data.winstreak += 1;
						data.exp += 133;
					}
					baseClass.totalExp += 133;

					winner.sendMessage("" + ChatColor.BOLD + "=====================");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD
							+ "  GAME WON");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("        " + "  1st Place: 10 Tokens");

					if (baseClass.totalKills >= 0) {
						winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE + ChatColor.BOLD
								+ "  " + baseClass.totalKills + " Kills: " + ChatColor.RESET + ChatColor.YELLOW
								+ (baseClass.totalKills * 2) + " Tokens");
						baseClass.totalTokens += baseClass.totalKills;
					}

					winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BOLD + "  Flawless Win: "
							+ ChatColor.RESET + ChatColor.YELLOW + "10 Tokens");
					baseClass.totalTokens += 10;
					if (winner.hasPermission("scb.rankBonus")) {
						winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE + ChatColor.BOLD
								+ "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW + "10 Tokens");
						baseClass.totalTokens += 10;
					}
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "||");
					winner.sendMessage("" + ChatColor.BOLD + "=====================");
					winner.getInventory().clear();
					winner.setGameMode(GameMode.ADVENTURE);

					if (data != null) {
						if (data.bonusTokens == 0) {
							winner.sendMessage(getManager().getMain().color(
									"&2&l(!) &rYou have completed the &eDouble Tokens &rchallenge! You will recieve double tokens for this game"));
							data.bonusTokens = 1;
							baseClass.totalTokens *= 2;
						}
					}
					// baseClass.totalTokens += baseClass.totalKills * 2;
					winner.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.GREEN + "You have earned "
							+ baseClass.totalTokens + " Tokens!");

					if (data != null)
						data.tokens += baseClass.totalTokens;
				}
			}
		}
		for (Player player : players) {
			gameManager.getMain().sendScoreboardUpdate(player);
			player.spigot().setCollidesWithEntities(true);
			player.getInventory().clear();
			for (PotionEffect type : player.getActivePotionEffects())
				player.removePotionEffect(type.getType());
		}

		EndGameAnimation(winnerList);
		EndGame();

		BaseClass baseClass = classes.get(winnerList.get(0));
		String tag = gameManager.getMain().getRankManager().getRank(winnerList.get(0)).getTagWithSpace();
		PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winnerList.get(0));

		if (map != null) {
			if (baseClass.getLives() >= 5) {
				if (data != null) {
					if (getManager().getMain().tournament == true) {
						data.points += 5;
					}
				}
				if (winnerList.get(0).hasPermission("scb.customWin")) {
					if (data.cwm == 1) {
						customFlawWinMsg(winnerList.get(0));
					} else {
						Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
								+ winnerList.get(0).getName() + ChatColor.WHITE + " just " + ChatColor.BOLD
								+ "FLAWLESSLY " + ChatColor.RESET + "won on " + ChatColor.BOLD + ChatColor.WHITE
								+ ChatColor.YELLOW + ChatColor.BOLD + map.toString());
					}
				} else {
					Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
							+ winnerList.get(0).getName() + ChatColor.WHITE + " just " + ChatColor.BOLD + "FLAWLESSLY "
							+ ChatColor.RESET + "won on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW
							+ ChatColor.BOLD + map.toString());
				}
			} else {
				if (winnerList.get(0).hasPermission("scb.customWin")) {
					if (data.cwm == 1) {
						customWinMsg(winnerList.get(0));
					} else {
						Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
								+ winnerList.get(0).getName() + ChatColor.WHITE + " just won on " + ChatColor.BOLD
								+ ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD + map.toString());
					}
				} else {
					Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
							+ winnerList.get(0).getName() + ChatColor.WHITE + " just won on " + ChatColor.BOLD
							+ ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD + map.toString());
				}
			}
		} else {
			Player display = winnerList.get(0);
			if (winnerList.size() > 1) {
				BaseClass bc = classes.get(winnerList.get(1));

				if (bc != null) {
					if (baseClass.getLives() >= bc.getLives())
						display = winnerList.get(0);
					else
						display = winnerList.get(1);
				}
			}
			if (baseClass.getLives() >= 5) {
				if (data != null) {
					if (getManager().getMain().tournament == true) {
						data.points += 5;
					}
				}
				if (display.hasPermission("scb.customWin")) {
					if (data.cwm == 1) {
						customFlawWinMsg(display);
					} else {
						Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(display)
								+ " Team" + ChatColor.WHITE + " just " + ChatColor.BOLD + "FLAWLESSLY "
								+ ChatColor.RESET + "won on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW
								+ ChatColor.BOLD + duosMap.toString());
					}
				} else {
					Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(display)
							+ " Team" + ChatColor.WHITE + " just " + ChatColor.BOLD + "FLAWLESSLY " + ChatColor.RESET
							+ "won on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD
							+ duosMap.toString());
				}
			} else {
				if (display.hasPermission("scb.customWin")) {
					if (data.cwm == 1) {
						customWinMsg(display);
					} else {
						Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(display)
								+ " Team" + ChatColor.WHITE + " just won on " + ChatColor.BOLD + ChatColor.WHITE
								+ ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
					}
				} else {
					Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(display)
							+ " Team" + ChatColor.WHITE + " just won on " + ChatColor.BOLD + ChatColor.WHITE
							+ ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
				}
			}
		}

		for (Player p : players) {
			PlayerData data4 = gameManager.getMain().getDataManager().getPlayerData(p);
			this.getManager().getMain().getDataManager().saveData(data4);
		}
	}

	private void customWinMsg(Player winner) {
		Random rand = new Random();
		int chance = rand.nextInt(3);
		String tag = gameManager.getMain().getRankManager().getRank(winner).getTagWithSpace();

		if (map != null) {
			if (chance == 0) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
						+ winner.getName() + ChatColor.WHITE + " got a Victory Royale on " + ChatColor.BOLD
						+ ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD + map.toString());
			} else if (chance == 1) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
						+ winner.getName() + ChatColor.WHITE + " just showed the entire lobby who's boss on "
						+ ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD + map.toString());
			} else if (chance == 2) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
						+ winner.getName() + ChatColor.WHITE + " just won on " + ChatColor.BOLD + ChatColor.WHITE
						+ ChatColor.YELLOW + ChatColor.BOLD + map.toString());
			}
		} else {
			if (chance == 0) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
						+ ChatColor.WHITE + " got a Victory Royale on " + ChatColor.BOLD + ChatColor.WHITE
						+ ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
			} else if (chance == 1) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
						+ ChatColor.WHITE + " just showed the entire lobby who's boss on " + ChatColor.BOLD
						+ ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
			} else if (chance == 2) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
						+ ChatColor.WHITE + " just won on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW
						+ ChatColor.BOLD + duosMap.toString());
			}
		}
	}

	private void customFlawWinMsg(Player winner) {
		Random rand = new Random();
		int chance = rand.nextInt(4);
		String tag = gameManager.getMain().getRankManager().getRank(winner).getTagWithSpace();

		if (map != null) {
			if (chance == 0) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
						+ winner.getName() + ChatColor.WHITE + " just " + ChatColor.BOLD + "ABSOLUTELY DESTROYED "
						+ ChatColor.RESET + "everyone on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW
						+ ChatColor.BOLD + map.toString());
			} else if (chance == 1) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + tag + ChatColor.YELLOW
						+ winner.getName() + ChatColor.WHITE + " just " + ChatColor.BOLD + "FLAWLESSLY "
						+ ChatColor.RESET + "won on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW
						+ ChatColor.BOLD + map.toString());
			} else if (chance == 2) {
				Bukkit.broadcastMessage(this.getManager().getMain().color("&r&l(!) &rThe game on &e&l" + map.toString()
						+ " &rwas too easy for " + tag + " &e" + winner.getName()));
			} else if (chance == 3) {
				Bukkit.broadcastMessage(this.getManager().getMain().color("&r&l(!) &rGet out of the way for " + tag
						+ " &e" + winner.getName() + ". &rHe &r&lDOMINATED &ron &e&l" + map.toString()));
			}
		} else {
			if (chance == 0) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
						+ ChatColor.WHITE + " just " + ChatColor.BOLD + "ABSOLUTELY DESTROYED " + ChatColor.RESET
						+ "everyone on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD
						+ duosMap.toString());
			} else if (chance == 1) {
				Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
						+ ChatColor.WHITE + " just " + ChatColor.BOLD + "FLAWLESSLY " + ChatColor.RESET + "won on "
						+ ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
			} else if (chance == 2) {
				Bukkit.broadcastMessage(this.getManager().getMain().color("&r&l(!) &rThe game on &e&l"
						+ duosMap.toString() + " &rwas too easy for " + tag + " &e" + winner.getName()));
			} else if (chance == 3) {
				Bukkit.broadcastMessage(this.getManager().getMain().color("&r&l(!) &rGet out of the way for " + tag
						+ " &e" + winner.getName() + ". &rHe &r&lDOMINATED &ron &e&l" + duosMap.toString()));
			}
		}
	}

	// public BaseClass oldBaseClass; // Public because we want to use this variable
	// in BaseClass.java

	private void rerandomizeClass(Player player) {
		BaseClass baseClass = classes.get(player);

		if (baseClass.getLives() > 1) {
			ClassType classType = ClassType.values()[random.nextInt(ClassType.values().length)];
			BaseClass newBaseClass = classType.GetClassInstance(this, player);
			BaseClass oldBaseClass = classes.get(player);
			oldClasses.put(player, oldBaseClass);

			Score newScore = livesObjective
					.getScore(truncateString(classType.getTag() + " " + ChatColor.WHITE + player.getName() + "", 40));
			newBaseClass.lives = oldBaseClass.lives;
			newBaseClass.tokens = oldBaseClass.tokens;
			newBaseClass.score = newScore;

			oldBaseClass.score.getScoreboard().resetScores(oldBaseClass.score.getEntry());

			classes.put(player, newBaseClass);
			sendScoreboardUpdate(player);

			player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "Your class has been randomly selected to " + classType.getTag());

			if (player.hasPermission("scb.chat"))
				player.setDisplayName("" + player.getName() + " " + classType.getTag());
			else
				player.setDisplayName("" + player.getName() + " " + classType.getTag() + ChatColor.GRAY);
		}
	}

	private void LoadClasses() {
		Random rand = new Random(); // for random class;
		int attempts = 0;

		for (Player player : players) {
			PlayerData playerData = gameManager.getMain().getDataManager().getPlayerData(player);
			ClassType selectedClass = gameType != GameType.FRENZY ? classSelection.get(player) : null;
			if (selectedClass == null) {
				selectedClass = ClassType.values()[rand.nextInt(ClassType.values().length)];
				if (gameType != GameType.FRENZY) {
					while (attempts <= 500) {
						attempts++;
						ClassType classType = ClassType.values()[rand.nextInt(ClassType.values().length)];
						Rank donor = classType.getMinRank();

						if (playerData.playerClasses.get(classType.getID()) != null
								&& playerData.playerClasses.get(classType.getID()).purchased
								|| classType.getTokenCost() == 0) {
							if (playerData.level >= classType.getLevel()) {
								if (donor == null || player.hasPermission("scb." + donor.toString().toLowerCase())) {
									selectedClass = classType;
									break;
								}
							}
						}
					}
				}
			}

			BaseClass baseClass = selectedClass.GetClassInstance(this, player);

			if (player.hasPermission("scb.chat"))
				player.setDisplayName("" + player.getName() + " " + selectedClass.getTag());
			else
				player.setDisplayName("" + player.getName() + " " + selectedClass.getTag() + ChatColor.GRAY);
			classes.put(player, baseClass);
			player.setHealth(20.0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlight(true);
			baseClass.LoadPlayer();
		}
	}

	// Sends msg to all players (inside the ArrayList)
	public void TellAll(String msg) {
		for (Player player : players)
			player.sendMessage(msg);
	}

	// Sends msg to all spectators (inside the ArrayList)
	public void TellSpec(String msg) {
		for (Player spectator : spectators)
			spectator.sendMessage(msg);
	}

	public boolean HasPlayer(Player player) {
		return players.contains(player);
	}

	public boolean HasSpectator(Player spectator) {
		return spectators.contains(spectator);
	}

	public boolean RemovePlayer(Player player) {
		BaseClass baseClass = this.classes.remove(player);
		this.playerPosition.remove(player);
		if (this.spectators.contains(player)) {
			this.spectators.remove(player);
			player.setDisplayName("" + player.getName());
			return true;
		}
		if (this.players.remove(player)) {
			player.setDisplayName("" + player.getName());
			try {
				if (baseClass != null) {
					baseClass.score.getScoreboard().resetScores(baseClass.score.getEntry());
					PlayerData data = this.gameManager.getMain().getDataManager().getPlayerData(player);
					if (this.state != GameState.ENDED && this.state != GameState.WAITING && data != null)
						data.losses++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (this.duosMap != null) {
				if (this.redTeam.contains(player)) {
					this.redTeam.remove(player);
				} else if (this.blueTeam.contains(player)) {
					this.blueTeam.remove(player);
				} else if (this.blackTeam.contains(player)) {
					this.blackTeam.remove(player);
				}
				this.team.remove(player);
			}
			if (this.state == GameState.WAITING)
				if (this.map != null) {
					for (Player gamePlayer : this.players) {
						FastBoard board = this.boards.get(gamePlayer);
						board.updateLine(5, " "
								+ (((this.map.GetInstance()).gameType == GameType.FRENZY) ? ("" + ChatColor.RESET
										+ this.players.size() + "/" + this.gameType.getMaxPlayers()) : "")
								+ (((this.map.GetInstance()).gameType == GameType.NORMAL) ? ("" + ChatColor.RESET
										+ this.players.size() + "/" + this.gameType.getMaxPlayers()) : "")
								+ (((this.map.GetInstance()).gameType == GameType.DUEL) ? ("" + ChatColor.RESET
										+ this.players.size() + "/" + this.gameType.getMaxPlayers()) : ""));
					}
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + player.getName()
							+ ChatColor.RED + " left " + ChatColor.RED + "(" + ChatColor.GREEN + (

							((this.map.GetInstance()).gameType == GameType.FRENZY)
									? ("" + ChatColor.RESET + this.players.size() + "/" + this.gameType.getMaxPlayers())
									: "")
							+ (((this.map.GetInstance()).gameType == GameType.NORMAL)
									? ("" + ChatColor.RESET + this.players.size() + "/" + this.gameType.getMaxPlayers())
									: "")
							+ (((this.map.GetInstance()).gameType == GameType.DUEL)
									? ("" + ChatColor.RESET + this.players.size() + "/" + this.gameType.getMaxPlayers())
									: "")
							+ ChatColor.RED + ")");
					if (this.players.size() < 2) {
						this.state = GameState.WAITING;
						if (this.gameStartTime != null) {
							this.gameStartTime.cancel();
							this.gameStartTime = null;
							TellAll("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "Game start cancelled, not enough players!");
							for (Player gamePlayer : this.players) {
								this.totalVotes = 0;
								PlayerData data = this.gameManager.getMain().getDataManager().getPlayerData(gamePlayer);
								if (data != null && data.votes == 1)
									data.votes = 0;
								if (gamePlayer.getInventory().contains(Material.PAPER))
									gamePlayer.getInventory().remove(Material.PAPER);
								FastBoard board = this.boards.get(gamePlayer);
								board.updateLine(5, " " + (

								((this.map.GetInstance()).gameType == GameType.FRENZY) ? (

								"" + ChatColor.RESET + this.players.size() + "/" + this.gameType.getMaxPlayers()) : "")
										+ (((this.map.GetInstance()).gameType == GameType.NORMAL) ? (

										"" + ChatColor.RESET + this.players.size() + "/"
												+ this.gameType.getMaxPlayers()) : "")
										+ (((this.map.GetInstance()).gameType == GameType.DUEL) ? (

										"" + ChatColor.RESET + this.players.size() + "/"
												+ this.gameType.getMaxPlayers()) : ""));
								board.updateLine(7, "" + ChatColor.BOLD + "Status:");
								board.updateLine(8, " " + ChatColor.ITALIC + "Waiting..");
							}
						}
					}
				} else {
					for (Player gamePlayer : this.players) {
						FastBoard board = this.boards.get(gamePlayer);
						board.updateLine(5, " " + this.players.size() + "/6");
					}
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + player.getName()
							+ ChatColor.RED + " left " + ChatColor.RED + "(" + ChatColor.GREEN + this.players.size()
							+ "/6" + ChatColor.RED + ")");
					if (this.players.size() < 2) {
						this.state = GameState.WAITING;
						if (this.gameStartTime != null) {
							this.gameStartTime.cancel();
							this.gameStartTime = null;
							TellAll(getManager().getMain()
									.color("&c&l(!) &rGame start cancelled. Not enough players!"));
							for (Player gamePlayer : this.players) {
								this.totalVotes = 0;
								PlayerData data = this.gameManager.getMain().getDataManager().getPlayerData(gamePlayer);
								if (data != null && data.votes == 1)
									data.votes = 0;
								if (gamePlayer.getInventory().contains(Material.PAPER))
									gamePlayer.getInventory().remove(Material.PAPER);
								FastBoard board = this.boards.get(gamePlayer);
								board.updateLine(5, " " + this.players.size() + "/6");
								board.updateLine(7, "" + ChatColor.BOLD + "Status:");
								board.updateLine(8, " " + ChatColor.ITALIC + "Waiting..");
							}
						}
					}
				}
			if (this.state == GameState.STARTED) {
				PlayerData data = this.gameManager.getMain().getDataManager().getPlayerData(player);
				if (data.withersk != 3)
					data.withersk = 0;
				List<String> aliveTeam = new ArrayList<>();
				if (this.map != null) {
					int alivePlayers = 0;
					Player alivePlayer = null;
					List<Player> lastAlive = new ArrayList<>();
					for (Map.Entry<Player, BaseClass> entry : this.classes.entrySet()) {
						if (((BaseClass) entry.getValue()).getLives() > 0) {
							alivePlayers++;
							alivePlayer = entry.getKey();
							lastAlive.add(alivePlayer);
						}
					}
					if (alivePlayers == 1 && alivePlayer != null && lastAlive.size() == 1) {
						WinGame(lastAlive);
					} else if (alivePlayers <= 0) {
						EndGame();
					}
				} else {
					this.teamsAlive = 0;
					for (Map.Entry<Player, BaseClass> entry : this.classes.entrySet()) {
						if (((BaseClass) entry.getValue()).getLives() > 0
								&& !aliveTeam.contains(this.team.get(entry.getKey()))) {
							aliveTeam.add(this.team.get(entry.getKey()));
							this.teamsAlive++;
						}
					}
					if (this.teamsAlive == 1) {
						if (aliveTeam.contains("Red")) {
							WinGame(this.redTeam);
						} else if (aliveTeam.contains("Blue")) {
							WinGame(this.blueTeam);
						} else if (aliveTeam.contains("Black")) {
							WinGame(this.blackTeam);
						}
					} else if (this.teamsAlive == 0) {
						EndGame();
					}
				}
				TellAll("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + player.getName()
						+ ChatColor.RESET + ChatColor.RED + " has left the game!");
				try {
					baseClass.score.getScoreboard().resetScores(baseClass.score.getEntry());
				} catch (Exception e) {
					e.printStackTrace();
				}
				SetLobbyScoreboard(player);
				RemovePlayer(player);
			}
			getManager().getMain().ResetPlayer(player);
			if (this.s != null) {
				if (this.map != null) {
					this.s.setLine(2, getManager().getMain().color("&0Players: " + this.players.size() + "/"
							+ (getMap().GetInstance()).gameType.getMaxPlayers()));
				} else {
					this.s.setLine(2, getManager().getMain().color("&0Players: " + this.players.size() + "/6"));
				}
				this.s.update();
			}
			return true;
		}
		return false;
	}

	public boolean PlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (players.contains(player)) {
			if (classes.containsKey(player)) {
				BaseClass baseClass = classes.get(player);
				if (event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL
						&& (event.getAction() == Action.RIGHT_CLICK_AIR
								|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					if (baseClass.pearlTimer.getTime() < 10000) {
						int seconds = (10000 - baseClass.pearlTimer.getTime()) / 1000 + 1;
						event.setCancelled(true);
						player.sendMessage(
								"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have to wait " + ChatColor.YELLOW
										+ seconds + " seconds " + ChatColor.RESET + "to use this item again");
					} else
						baseClass.pearlTimer.restart();
				}
				baseClass.UseItem(event);
			}
			return true;
		}
		return false;
	}
}
