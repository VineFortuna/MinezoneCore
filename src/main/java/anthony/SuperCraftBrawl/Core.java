package anthony.SuperCraftBrawl;

import anthony.SuperCraftBrawl.Game.*;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.commands.Commands;
import anthony.SuperCraftBrawl.doublejump.DoubleJumpManager;
import anthony.SuperCraftBrawl.fishing.FishRarity;
import anthony.SuperCraftBrawl.fishing.FishType;
import anthony.SuperCraftBrawl.fishing.Fishing;
import anthony.SuperCraftBrawl.gui.*;
import anthony.SuperCraftBrawl.leaderboards.*;
import anthony.SuperCraftBrawl.npcs.NPCManager;
import anthony.SuperCraftBrawl.packets.PacketMain;
import anthony.SuperCraftBrawl.playerdata.DatabaseManager;
import anthony.SuperCraftBrawl.playerdata.FishingDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.playerdata.PlayerDataManager;
import anthony.SuperCraftBrawl.practice.BowPractice;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.SuperCraftBrawl.ranks.RankManager;
import anthony.SuperCraftBrawl.tablist.TablistManager;
import anthony.parkour.Parkour;
import anthony.util.ItemHelper;
import me.itzzmic.minezone.api.PunishAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Core extends JavaPlugin implements Listener {

	static Core plugin;

	private ActionBarManager actionBarManager;
	public GameManager gameManager;
	public ScoreboardManager scoreboardManager;
	public TablistManager tabManager;
	public Version version;
	public FreeClassesGUI inventoryGUI;
	public anthony.CrystalWars.game.GameManager gm;
	public DonorClassesGUI donorGUI;
	public GameSelectorGUI hubGUI;
	public Commands commands;
	public World lobbyWorld;
	public PlayerListener listener;
	public DoubleJumpManager djManager;
	protected final Cooldown cooldownTime = null;
	public RankManager rankManager;
	public List<Player> staffchat;
	public List<Player> globalchat;
	public PlayerDataManager dataManager;
	public DatabaseManager databaseManager;
	public PacketMain packetMain;
	public NPCManager npcManager;
	public ActiveGamesGUI ag;
	public boolean tournament = false;
	public boolean tourneyreset = false;
	public boolean tournamentend = false;
	public Map<String, Integer> tourney = new HashMap<>();
	public HashMap<Player, Boolean> ao = new HashMap<>();
	public HashMap<Player, Boolean> so = new HashMap<>();
	public HashMap<Player, Boolean> po = new HashMap<>();
	public Parkour p;
	// public AntiCheat cheat;
	public Leaderboard lb;
	public FishingBoard fb;
	public KillsBoard kb;
	public BoardSettings boardSettings;
	public WinstreakBoard streakBoard;
	public FlawlessWinsBoard flawlessWinsBoard;
	public Fishing fishing;
	private ArrayList<String> msg;
	public Map<Player, Player> wagers = new HashMap<Player, Player>();

	// Player's game stats
	public Map<Player, GameInstance> gameStats = new HashMap<Player, GameInstance>();

	public boolean finalEvent = false;

	private long tickCounter = 0;

	public Core() {
		this.staffchat = new ArrayList<Player>();
		this.globalchat = new ArrayList<Player>();
	}

	public static Core inst() {
		return plugin;
	}

	// Getters:

	public ActionBarManager getActionBarManager() {
		return this.actionBarManager;
	}

	public ScoreboardManager getScoreboardManager() {
		return this.scoreboardManager;
	}

	public TablistManager getTabManager() {
		return this.tabManager;
	}

	public long getCurrentTick() {
		return this.tickCounter;
	}

	public Parkour getParkour() {
		return this.p;
	}

	public anthony.CrystalWars.game.GameManager getCwManager() {
		return gm;
	}

	public FlawlessWinsBoard getFlawlessWinsBoard() {
		return this.flawlessWinsBoard;
	}

	public BoardSettings getBoardSettings() {
		return this.boardSettings;
	}

	public WinstreakBoard getWinstreakBoard() {
		return this.streakBoard;
	}

	public PlayerDataManager getDataManager() {
		return dataManager;
	}

	public Version getVersion() {
		return this.version;
	}

	public Leaderboard getLeaderboard() {
		return lb;
	}

	public FishingBoard getFishingLeaderboard() {
		return fb;
	}

	public KillsBoard getKillsLeaderboard() {
		return kb;
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}

	public NPCManager getNPCManager() {
		return npcManager;
	}

	public ActiveGamesGUI getActiveGames() {
		return ag;
	}

	// public AntiCheat getAntiCheat() {
	// return cheat;
	// }

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		if (e.getClickedBlock() == null)
			return;
		if (e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST
				|| e.getClickedBlock().getType() == Material.SIGN) {
			Sign s = (Sign) e.getClickedBlock().getState();
			for (Maps map : Maps.values()) {
				if (s.getLine(1).equalsIgnoreCase(map.toString())) {
					this.getGameManager().JoinMap(player, map);
					GameInstance i = null;

					if (getGameManager().gameMap.containsKey(map)) {
						i = getGameManager().gameMap.get(map);

						if (i != null) {
							if (i.state == GameState.WAITING) {
								s.setLine(2, this.color("&0Players: " + i.players.size() + "/"
										+ i.getMap().GetInstance().gameType.getMaxPlayers()));
								s.setLine(3, this.color("&0" + i.ticksTilStart + "s"));
								s.update();
							} else if (i.state == GameState.STARTED) {
								player.sendMessage(this.color(
										"&2&l(!) &rSince the game you tried joining has started, you've joined as a Spectator"));
								getGameManager().SpectatorJoinMap(player, map);
							}
						}
						i.setSign(s);
					}
				}
			}
		}
	}

	public Location getSCBLoc() {
		return new Location(lobbyWorld, -8.531, 161, -406.493);
	}

	public void SendPlayerToSCB(Player player) {
		player.teleport(getSCBLoc());
	}

	public GameManager getGameManager() {
		return gameManager;
	}

	public PacketMain getPacketMain() {
		return packetMain;
	}

	public GameSelectorGUI getHubGUI() {
		return hubGUI;
	}

	public FreeClassesGUI getInventoryGUI() {
		return inventoryGUI;
	}

	public DonorClassesGUI getDonorGUI() {
		return donorGUI;
	}

	public Cooldown getCooldown() {
		return cooldownTime;
	}

	public PlayerListener getListener() {
		return listener;
	}

	public Commands getCommands() {
		return commands;
	}

	public String format(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public RankManager getRankManager() {
		return rankManager;
	}

	// private AntiCheat cheat;

	/*
	 * public AntiCheat getAntiCheat() { return cheat; }
	 */

	/*
	 * @EventHandler public void onMove(PlayerMoveEvent e) { Player p =
	 * e.getPlayer(); Location eye = p.getEyeLocation(); Vector vec =
	 * eye.getDirection(); vec.normalize(); for (int i = 0; i < 100; i++) {
	 * eye.add(vec); PacketPlayOutWorldParticles packet = new
	 * PacketPlayOutWorldParticles(EnumParticle.FLAME, true, (float) eye.getX(),
	 * (float) (float) eye.getY(), (float) (float) eye.getZ(), 0.75F, 0.75F, 0.75F,
	 * 0F, 25); ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet); }
	 * 
	 * }
	 */

	public void messages() {
		Random random = new Random();

		BukkitRunnable runnable = new BukkitRunnable() {
			Announcements msg = null;

			@Override
			public void run() {
				msg = Announcements.values()[random.nextInt(Announcements.values().length)];
				String msgToPlayers = msg.getName();
				if (Bukkit.getOnlinePlayers().size() > 0)
					for (Player player : Bukkit.getOnlinePlayers())
						player.sendMessage(msgToPlayers);
			}

		};
		runnable.runTaskTimer(this, 0, 6000);
	}

	// For tab organization.
	private Scoreboard lobbyScoreBoard;

	@Override
	public void onEnable() {
		plugin = this;
		msg = new ArrayList<>();
		msg.add(color("&4&lREMEMBER TO TELL ITZZMIC (I LOVE YOU)"));
		msg.add(color("&c&lHOW DO YOU SPELL SCB AGAIN?"));
		msg.add(color("&3ItzzMic coded this btw..."));
		msg.add(color("&9&lAnthonyFortuna is so cool"));
		msg.add(color("&cItzzMic wants to remind you to have a good day!"));
		msg.add(color("&e&lTacos are really good!"));
		msg.add(color("&cIdek what to put here"));
		msg.add(color("&cI be sweatin since 2002 baby"));
		msg.add(color("&3&lWho is Adwyr?"));
		msg.add(color("&cLove you!"));
		msg.add(color("&4&lSEASON 2 OUT ALREADY??"));
		msg.add(color("&a&lReminder to thank the Staff of &e&l&oMINEZONE"));
		msg.add(color("&cSheep kit is probably the best!"));
		msg.add(color("&dSubscribe to &e&l&oMINEZONE &don &cYou&fTube&d!"));
		msg.add(color("&3astro is &b&l20% &3better than you"));

		getLogger().info("(!) You have enabled Minezone-Core");
		// lobbyWorld = getServer().createWorld(new WorldCreator("lobby"));
		lobbyWorld = getServer().createWorld(new WorldCreator("lobby-1")); // Game servers
		// lobbyWorld = getServer().createWorld(new WorldCreator("lobbies")); //Hub
		// server
		// getServer().createWorld(new WorldCreator("name"));
		lobbyScoreBoard = Bukkit.getScoreboardManager().getNewScoreboard();

		for (Player onlinePlayer : this.getServer().getOnlinePlayers())
			this.ResetPlayer(onlinePlayer);

		listener = new PlayerListener(this);
		// smmmanager = new SmmManager(this);
		gameManager = new GameManager(this);
		scoreboardManager = new ScoreboardManager(this);
		tabManager = new TablistManager(this);
		commands = new Commands(this);
		// cmd = new anthony.skywars.commands.Commands(this);
		djManager = new DoubleJumpManager(this);
		databaseManager = new DatabaseManager(this);
		packetMain = new PacketMain(this);
		dataManager = new PlayerDataManager(this);
		npcManager = new NPCManager(this);
		rankManager = new RankManager(this);
		actionBarManager = new ActionBarManager(this);
		// gm = new anthony.CrystalWars.game.GameManager(this);
		ag = new ActiveGamesGUI(this);
		p = new Parkour(this);
		lb = new Leaderboard(this);
		kb = new KillsBoard(this);
		fb = new FishingBoard(this);
		boardSettings = new BoardSettings(this);
		streakBoard = new WinstreakBoard(this);
		flawlessWinsBoard = new FlawlessWinsBoard(this);
		fishing = new Fishing(this);
		// kb = new KillsBoard(this);
		// swManager = new anthony.skywars.GameManager(this);
		// abilityManager = new anthony.skywars.AbilityManager(this);
		// cheat = new AntiCheat(this);

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", gameManager);
		messages();

		if (this.getCommands() != null) {
			String[] commandTypes = { "join", "fav", "fly", "f", "shop", "leave", "l", "cw", "players", "class",
					"spectate", "startgame", "gamestats", "setlives", "purchases", "kit", "items" };

			for (String command : commandTypes) {
				PluginCommand pluginCommand = this.getCommand(command);
				if (pluginCommand != null) {
					pluginCommand.setExecutor(commands);
					pluginCommand.setTabCompleter(commands);
				} else
					System.out.print(command + " was null!");
			}
		}

		enablePracticeModes();

		new BukkitRunnable() {
			@Override
			public void run() {
				PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
				Object header = new ChatComponentText("" + ChatColor.AQUA + ChatColor.BOLD + "MINEZONE");
				Object footer = new ChatComponentText(
						color("&7You are playing on ") + ChatColor.AQUA + "minezone.club");
				try {
					Field a = packet.getClass().getDeclaredField("a");
					a.setAccessible(true);
					Field b = packet.getClass().getDeclaredField("b");
					b.setAccessible(true);

					a.set(packet, header);
					b.set(packet, footer);

					if (Bukkit.getOnlinePlayers().size() == 0)
						return;

					for (Player player : Bukkit.getOnlinePlayers()) {
						((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.runTaskTimer(this, 0, 20);

		new BukkitRunnable() {

			@Override
			public void run() {
				tickCounter++;
			}
		}.runTaskTimer(this, 0, 1);
	}

	public static BowPractice bowPractice;

	private void enablePracticeModes() {
		this.bowPractice = new BowPractice();
	}

	public Location GetLobbyLoc() {
		return new Location(lobbyWorld, -5.533, 143, 19.468);
	}

	public void SendPlayerToMap(Player player) {
		player.teleport(GetLobbyLoc());
	}

	public Location GetStaffLoc() {
		return new Location(lobbyWorld, 953.529, 177, 1036.495);
	}

	public void SendPlayerToStaff(Player player) {
		player.teleport(GetStaffLoc());
	}

	public Location GetHubLoc() {
		// return new Location(lobbyWorld, -199, 86, -7);
		// return new Location(lobbyWorld, -5.457, 143, 19.522);
		// return new Location(lobbyWorld, 288.507, 119, 2346.529);

		// return new Location(lobbyWorld, -58.507, 125, -18.519, -179, -1);
		// if (this.getCommands() != null || this.getSWCommands() != null)
		return new Location(lobbyWorld, 189.495, 115, 629.438, -0, 1);
		// else
		// return new Location(lobbyWorld, 0.478, 51, 0.550);
	}

	public void SendPlayerToHub(Player player) {
		player.teleport(GetHubLoc());
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

	public String staffhelp = "";
	public String staffhelpReply = "";

	@SuppressWarnings({ "null", "deprecation" })
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("sh")) {
			if (args.length == 0)
				player.sendMessage(color("&c&l(!) &rIncorrect usage! Try doing: &e/sh <message>"));
			else {
				staffhelp = "";

				for (int i = 0; i < args.length; i++) {
					staffhelp += args[i] + " ";
				}
				player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "StaffHelp> " + ChatColor.RESET
						+ getRankManager().getRank(player).getTagWithSpace() + ChatColor.RESET + player.getName() + ": "
						+ ChatColor.LIGHT_PURPLE + staffhelp);
				player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "If any staff is online, you will recieve a reply shortly");

				for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
					if (onlinePlayers.hasPermission("scb.staffhelp")) {
						onlinePlayers.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "StaffHelp> "
								+ ChatColor.RESET + getRankManager().getRank(player).getTagWithSpace() + ChatColor.RESET
								+ player.getName() + ": " + ChatColor.LIGHT_PURPLE + staffhelp);
					}
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("shr")) {
			if (player.hasPermission("scb.staffhelpreply")) {
				if (args.length == 0) {
					player.sendMessage(color("&c&l(!) &rIncorrect usage! Try doing: &e/shr <player> <message>"));
				} else if (args.length == 1) {
					player.sendMessage(color("&c&l(!) &rIncorrect usage! Try doing: &e/shr <player> <message>"));
				} else {
					Player target = Bukkit.getServer().getPlayerExact(args[0]);
					staffhelpReply = "";

					if (target != null) {
						for (int i = 1; i < args.length; i++) {
							staffhelpReply += args[i] + " ";
						}
						player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "StaffHelp REPLY> "
								+ ChatColor.RESET + getRankManager().getRank(player).getTagWithSpace() + ChatColor.RESET
								+ player.getName() + ": " + ChatColor.LIGHT_PURPLE + staffhelpReply);
						target.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "StaffHelp REPLY> "
								+ ChatColor.RESET + getRankManager().getRank(player).getTagWithSpace() + ChatColor.RESET
								+ player.getName() + ": " + ChatColor.LIGHT_PURPLE + staffhelpReply);
					} else {
						player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Please specify a player!");
					}
				}
			} else {
				player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You need the rank " + ChatColor.GOLD + ChatColor.BOLD + "TRAINEE " + ChatColor.RESET
						+ "to use this command");
			}
		}

		if (cmd.getName().equalsIgnoreCase("broadcast")) {
			if (player.hasPermission("scb.broadcast")) {
				if (args.length == 0) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/broadcast <message>");
					return true;
				} else {
					String message = "";

					for (int i = 0; i < args.length; i++) {
						message += args[i] + " ";
					}

					for (Player allPlayers : Bukkit.getOnlinePlayers()) {
						if (args.length != 0) {
							allPlayers.sendTitle(
									"" + ChatColor.GREEN + ChatColor.BOLD + ChatColor.UNDERLINE + "ANNOUNCEMENT",
									"" + ChatColor.RESET + message + " - " + ChatColor.YELLOW
											+ player.getName().substring(0, 3));
							allPlayers.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ message + " - " + ChatColor.YELLOW + player.getName());
						}
					}
				}
			} else
				player.sendMessage(color("&c&l(!) &rYou need the rank &c&lADMIN &rto use this command!"));
		}

		if (cmd.getName().equalsIgnoreCase("sc") && sender instanceof Player) {
			if (player.hasPermission("scb.staffchat")) {
				if (!(staffchat.contains(player))) {
					staffchat.add(player);
					player.sendMessage(color("&e&l(!) &rYou have &eenabled &rStaffChat"));
				} else {
					staffchat.remove(player);
					player.sendMessage(color("&e&l(!) &rYou have &cdisabled &rStaffChat"));
				}
			} else
				player.sendMessage(color("&c&l(!) &rYou need the rank &6&lTRAINEE &rto use this comamnd!"));
		}

		if (cmd.getName().equalsIgnoreCase("world")) {
			if (player.hasPermission("scb.tpWorld")) {
				World oldLobby = getServer().createWorld(new WorldCreator("world"));
				player.teleport(oldLobby.getSpawnLocation());
			}
		}

		if (cmd.getName().equalsIgnoreCase("hub")) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			try {
				out.writeUTF("Connect");
				out.writeUTF("lobby-1");
				player.sendMessage(color("&e&l(!) &rConnecting to &elobby-1"));
			} catch (Exception ex) {
				player.sendMessage(color("&c&l(!) &rThere was a problem connecting to &elobby-1"));
			}
			player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		}

		if (cmd.getName().equalsIgnoreCase("setlevel")) {
			if (player.hasPermission("scb.setlevel")) {
				PlayerData data = getDataManager().getPlayerData(player);
				if (args.length > 0) {
					try {
						int num = Integer.parseInt(args[0]);

						if (num >= 0) {
							if (data != null) {
								data.level = num;
								player.sendMessage(color("&2&l(!) &rYou set your level to &e" + num + "!"));
								if (this.getGameManager().GetInstanceOfPlayer(player) == null)
									getScoreboardManager().lobbyBoard(player);
								this.getDataManager().saveData(data);
							}
						} else {
							player.sendMessage(color("&c&l(!) &rPlease enter a number that is greater/equal to 0"));
						}
					} catch (Exception e) {
						player.sendMessage(color("&c&l(!) &rPlease enter a valid number!"));
						e.printStackTrace();
					}
				} else
					player.sendMessage(color("&r&l(!) &rIncorrect usage! Try doing: &e/setlevel <level>"));
			} else
				player.sendMessage(color("&c&l(!) &rYou need the rank &c&lADMIN &rto use this command!"));
		}

		if (cmd.getName().equalsIgnoreCase("give")) {
			if (player.hasPermission("scb.give")) {
				if (args.length > 0 && args.length < 4) {
					Player target = Bukkit.getServer().getPlayerExact(args[0]);
					Material mat = testMaterial(args[1]);
					int amount = Integer.parseInt(args[2]);
					ItemStack item = null;
					if (mat != null) {
						item = new ItemStack(mat, amount);
						target.getInventory().addItem(item);
					} else {
						player.sendMessage(color("&c&l(!) &rInvalid item!"));
						return false;
					}
					if (target != player) {
						target.sendMessage(color("&e&l(!) &rYou were given &e " + amount + " " + item.getType()));
					} else {
						player.sendMessage(color("&e&l(!) &rYou were given &e " + amount + " " + item.getType()));
					}
				} else if (args.length > 3 && args.length < 6) {
					Player target = Bukkit.getServer().getPlayerExact(args[0]);
					Material mat = testMaterial(args[1]);
					int amount = Integer.parseInt(args[2]);
					Enchantment ench = testEnchant(args[3]);
					int level = Integer.parseInt(args[4]);
					ItemStack item = null;

					if (level > 0) {
						if (mat != null) {
							item = new ItemStack(mat, amount);
							enchantments(item, ench, level);
							target.getInventory().addItem(item);
						} else {
							player.sendMessage(color("&c&l(!) &rInvalid item!"));
							return false;
						}
						if (target != player) {
							target.sendMessage(color("&e&l(!) &rYou were given &e " + amount + " " + item.getType()));
						} else {
							player.sendMessage(color("&e&l(!) &rYou were given &e " + amount + " " + item.getType()
									+ " &rwith &e " + ench.getName() + " " + level));
						}
					} else {
						player.sendMessage(color("&c&l(!) &rPlease enter an Enchantment level higher than 0!"));
					}
				} else {
					player.sendMessage(color(
							"&c&l(!) &rIncorrect usage! Try doing: &e/give <player> <item> <amount> <enchantment> <level>"));
				}
			} else {
				player.sendMessage(color("&c&l(!) &rYou need the rank &c&lADMIN &rto use this command!"));
			}
		}

		if (cmd.getName().equalsIgnoreCase("setrank")) {
			if (player.hasPermission("scb.setrank")) {
				if (args.length > 1) {
					Rank rank = Rank.getRankFromName(args[1]);
					Player target = Bukkit.getServer().getPlayerExact(args[0]);

					if (target != null) {
						getRankManager().setRank(target, rank);
						String temp = "" + getRankManager().getRank(target);
						String temp2 = temp.toUpperCase();
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + target.getName()
								+ "'s rank was set to " + ChatColor.YELLOW + temp2);
						target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your rank has been set to "
								+ ChatColor.YELLOW + temp2);
						target.kickPlayer("Your rank has been set to " + ChatColor.YELLOW + temp2);
					} else {
						player.sendMessage(
								"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "This player is not online!");
					}
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/setrank <player> <rank>");
				}
			}
		}

		if (cmd.getName().equalsIgnoreCase("list")) {
			String players = "";
			int count = 0;
			int totalPlayers = Bukkit.getOnlinePlayers().size();
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "There are " + ChatColor.YELLOW
					+ totalPlayers + ChatColor.RESET + " players online:");

			for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
				count++;
				players += "" + ChatColor.YELLOW + onlinePlayers.getName() + "";

				if (count < totalPlayers) {
					players += "" + ChatColor.RESET + ", ";
				}
			}

			player.sendMessage(players);
		}

		if (cmd.getName().equalsIgnoreCase("online")) {
			int online = Bukkit.getOnlinePlayers().size();
			player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET + "There are "
					+ ChatColor.YELLOW + online + ChatColor.RESET + " players online");
		}

		if (cmd.getName().equalsIgnoreCase("socials") && sender instanceof Player) {
			player.sendMessage(this.color("&8&m-------&8[Social Media]&8&m-------"));
			player.sendMessage("");
			player.sendMessage(this.color("&eDiscord: &7https://discord.gg/FSZpmY9FZB"));
			player.sendMessage(this.color("&eStore: &7minezone.tebex.io"));
			player.sendMessage(this.color("&eYouTube: &7https://www.youtube.com/@minezone6480"));
			player.sendMessage(this.color("&eTwitter: &7https://twitter.com/MinezoneMC"));
			player.sendMessage(this.color("&eTikTok: &7https://www.tiktok.com/@minezonemc"));
			player.sendMessage("");
			player.sendMessage(this.color("&8&m----------------------------"));
		}

		if (cmd.getName().equalsIgnoreCase("vanish") && sender instanceof Player) {
			if (sender.hasPermission("scb.vanish")) {
				player.sendMessage(
						"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You are now in vanish");
				player.setGameMode(GameMode.SPECTATOR);
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.RED + "You need the rank "
						+ ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + ChatColor.RED
						+ "to use this command");
			}
		}

		if (cmd.getName().equalsIgnoreCase("unvanish") && sender instanceof Player) {
			if (sender.hasPermission("scb.unvanish")) {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You are now "
						+ ChatColor.RESET + ChatColor.RED + "unvanished");
				player.setGameMode(GameMode.ADVENTURE);
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.RED + "You need the rank "
						+ ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + ChatColor.RED
						+ "to use this command");
			}
		}

		if (cmd.getName().equalsIgnoreCase("rules") && sender instanceof Player) {

			player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.WHITE
					+ "The rules can be found at, " + ChatColor.RESET + ChatColor.GREEN + "discord.gg/B9eHKg7");
		}

		if (cmd.getName().equalsIgnoreCase("staff") && sender instanceof Player) {
			if (sender.hasPermission("scb.staff")) {
				GameInstance instance = this.getGameManager().GetInstanceOfPlayer(player);

				if (instance != null) {
					player.sendMessage(color("&r&l(!) &rYou cannot teleport to &eStaff &rwhile in a game!"));
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Sending you to "
							+ ChatColor.GREEN + "Staff");
					SendPlayerToStaff(player);
				}
			} else {
				player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.RED
						+ "You don't have permission to join the " + ChatColor.RESET + ChatColor.GREEN
						+ "Staff Server");
			}

		}
		if (cmd.getName().equalsIgnoreCase("gmc") && sender instanceof Player) {
			if (sender.hasPermission("scb.gmc")) {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your gamemode has been updated to "
						+ ChatColor.RESET + ChatColor.GREEN + "Creative!");
				player.setGameMode(GameMode.CREATIVE);
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank " + ChatColor.RED
						+ ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
			}
		}
		if (cmd.getName().equalsIgnoreCase("gms") && sender instanceof Player) {
			if (sender.hasPermission("scb.gms")) {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your gamemode has been updated to "
						+ ChatColor.RESET + ChatColor.GREEN + "Survival!");
				player.setGameMode(GameMode.SURVIVAL);
				player.setAllowFlight(true);
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank " + ChatColor.RED
						+ ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
			}
		}
		if (cmd.getName().equalsIgnoreCase("gmsp") && sender instanceof Player) {
			if (sender.hasPermission("scb.gmsp")) {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your gamemode has been updated to "
						+ ChatColor.RESET + ChatColor.GREEN + "Spectator!");
				player.setGameMode(GameMode.SPECTATOR);
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank " + ChatColor.RED
						+ ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
			}
		}
		if (cmd.getName().equalsIgnoreCase("gma") && sender instanceof Player) {
			if (sender.hasPermission("scb.gma")) {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your gamemode has been updated to "
						+ ChatColor.RESET + ChatColor.GREEN + "Adventure!");
				player.setGameMode(GameMode.ADVENTURE);
				player.setAllowFlight(true);
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank " + ChatColor.RED
						+ ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
			}
		}
		if (cmd.getName().equalsIgnoreCase("gm") && sender instanceof Player) {
			if (sender.hasPermission("scb.gm")) {
				player.sendMessage(
						"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.RED + "Incorrect usage! Try doing: "
								+ ChatColor.RESET + ChatColor.GREEN + "/gms, /gmc, /gmsp or /gma");
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank " + ChatColor.RED
						+ ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
			}
		}

		if (cmd.getName().equalsIgnoreCase("help") && sender instanceof Player) {
			player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.AQUA
					+ "Need help? Go to our Discord Server for Help!");
			player.sendMessage(
					"- " + ChatColor.RED + ChatColor.BOLD + "Discord: " + ChatColor.GREEN + "discord.gg/FSZpmY9FZB");
		}

		if (cmd.getName().equalsIgnoreCase("classes") && sender instanceof Player) {
			player.sendMessage(color("&r&l(!) &eThe following classes are available to play:"));
			String dClasses = "";
			String tClasses = "";
			String rClasses = "";
			int count = 0;
			for (ClassType type : ClassType.values()) {
				if (type.getTokenCost() == 0 && type.getMinRank() != Rank.VIP)
					dClasses += type.getTag() + " ";
				else if (type.getTokenCost() > 0)
					tClasses += type.getTag() + " ";
				else if (type.getMinRank() == Rank.VIP)
					rClasses += type.getTag() + " ";
			}
			player.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + "DEFAULT CLASSES");
			player.sendMessage(dClasses);
			player.sendMessage("");
			player.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + "TOKEN CLASSES");
			player.sendMessage(tClasses);
			player.sendMessage("");
			player.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + "DONOR CLASSES");
			player.sendMessage(rClasses);
		}
		if (cmd.getName().equalsIgnoreCase("scb") && sender instanceof Player) {
			player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "[SUPER CRAFT BLOCKS]");
			player.sendMessage("" + ChatColor.GREEN + "Custom coded plugin by: VineFortuna & CowNecromancer");
			player.sendMessage("" + ChatColor.GREEN + "Version: " + Version.SCB.getVersion());
			player.sendMessage("" + ChatColor.GREEN + "Type " + ChatColor.WHITE + "/scbhelp " + ChatColor.GREEN
					+ "for more information");
		}
		if (cmd.getName().equalsIgnoreCase("scbhelp") && sender instanceof Player) {
			player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "GENERAL SCB COMMANDS");
			player.sendMessage("" + ChatColor.WHITE + "/join -> " + ChatColor.GREEN + "Join a game");
			player.sendMessage("" + ChatColor.WHITE + "/leave -> " + ChatColor.GREEN + "Leave your game");
			player.sendMessage("" + ChatColor.WHITE + "/classes -> " + ChatColor.GREEN + "Lists all available classes");
			player.sendMessage("" + ChatColor.WHITE + "/class -> " + ChatColor.GREEN + "Choose a class");
			player.sendMessage("" + ChatColor.WHITE + "/spectate -> " + ChatColor.GREEN + "Spectate a game");
			player.sendMessage("" + ChatColor.WHITE + "/maplist -> " + ChatColor.GREEN + "List of all available maps");
		}
		if (cmd.getName().equalsIgnoreCase("maplist") && sender instanceof Player) {
			String list = "";
			int count = 1;

			player.sendMessage(
					"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "The following maps are available to play:");
			for (Maps map : Maps.values()) {
				list += "" + ChatColor.YELLOW + map.toString() + ChatColor.RESET;
				if (count < Maps.values().length) {
					list += ", ";
				}
				count++;
			}
			player.sendMessage(list);
		}

		if (cmd.getName().equalsIgnoreCase("maps")) {
			int count = 0;
			for (int i = 0; i < Maps.values().length; i++) {
				count++;
			}
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "There are " + ChatColor.YELLOW + count
					+ ChatColor.RESET + " available maps to play");
		}

		if (cmd.getName().equalsIgnoreCase("exp")) {
			if (player.hasPermission("scb.exp")) {
				if (args.length == 0) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/exp <amount>");
				} else if (args.length == 1) {
					int num = Integer.parseInt(args[0]);
					PlayerData data = this.getDataManager().getPlayerData(player);
					data.exp += num;
					player.sendMessage("Added " + num + " to your account");

					if (data.exp >= 2500) {
						data.level++;
						data.exp -= 2500;
						player.sendMessage("Level upgraded to " + data.level + "!");
					}
					if (this.getGameManager().GetInstanceOfPlayer(player) == null)
						getScoreboardManager().lobbyBoard(player);
					this.getDataManager().saveData(data);
				}
			}
		}

		if (cmd.getName().equalsIgnoreCase("unmute")) {
			if (player.hasPermission("scb.unmute")) {
				if (args.length > 0) {
					Player target = Bukkit.getPlayerExact(args[0]);

					if (target != null) {
						PlayerData data = this.getDataManager().getPlayerData(target);
						data.muted = 0;
						player.sendMessage(color("&r&l(!) &e" + target.getName() + " &rhas been unmuted"));
					} else {
						player.sendMessage(color("&c&l(!) &rPlease specify a player!"));
					}
				} else {
					player.sendMessage(color("&c&l(!) &rIncorrect usage! Try doing: &e/unmute <player>"));
				}
			} else {
				player.sendMessage(color("&c&l(!) &rYou need the rank &6&lTRAINEE &rto use this command!"));
			}
		}

		if (cmd.getName().equalsIgnoreCase("invite")) {
			GameInstance instance = this.getGameManager().GetInstanceOfPlayer(player);

			if (player.hasPermission("scb.invite")) {
				if (instance != null) {
					if (instance.state == GameState.WAITING) {
						String mapName = "";
						if (instance.getMap() != null)
							mapName = "" + instance.getMap();
						else
							mapName = "" + instance.duosMap;

						Bukkit.broadcastMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
								+ getRankManager().getRank(player).getTagWithSpace() + ChatColor.YELLOW
								+ player.getName() + " " + ChatColor.RESET + "invited all players in the Lobby to join "
								+ ChatColor.YELLOW + mapName);
						TextComponent message = new TextComponent(
								"" + "     " + ChatColor.GREEN + ChatColor.BOLD + "Click here to join!");
						message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + mapName));
						Bukkit.spigot().broadcast(message);
					} else {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You must be in a Waiting Lobby to use this command");
					}
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You need to be in a game to use this command");
				}
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
						+ ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN " + ChatColor.RESET + "to use this command");
			}
		}

		if (cmd.getName().equalsIgnoreCase("fac")) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			try {
				out.writeUTF("Connect");
				out.writeUTF("factions");
			} catch (IOException ex) {

			}
			player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		}

		if (cmd.getName().equalsIgnoreCase("store")) {
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "Want to help support the server? Purchase a rank at " + ChatColor.GREEN
					+ "https://minezone.tebex.io/");
		}

		if (cmd.getName().equalsIgnoreCase("color")) {
			PlayerData data = this.getDataManager().getPlayerData(player);

			if (player.hasPermission("scb.color")) {
				if (data != null) {
					if (args.length == 0) {
						player.sendMessage(
								"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
										+ ChatColor.GREEN + "/color blue/green/red/yellow/reset");
					} else if (args[0].equalsIgnoreCase("blue")) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Changed your prefix to "
								+ ChatColor.BLUE + player.getName());
						data.blue = 1;
						data.red = 0;
						data.green = 0;
						data.yellow = 0;
					} else if (args[0].equalsIgnoreCase("red")) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Changed your prefix to "
								+ ChatColor.RED + player.getName());
						data.red = 1;
						data.blue = 0;
						data.green = 0;
						data.yellow = 0;
					} else if (args[0].equalsIgnoreCase("green")) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Changed your prefix to "
								+ ChatColor.GREEN + player.getName());
						data.green = 1;
						data.red = 0;
						data.blue = 0;
						data.yellow = 0;
					} else if (args[0].equalsIgnoreCase("yellow")) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Changed your prefix to "
								+ ChatColor.YELLOW + player.getName());
						data.green = 0;
						data.red = 0;
						data.blue = 0;
						data.yellow = 1;
					} else if (args[0].equalsIgnoreCase("reset")) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Reset your name color");
						data.green = 0;
						data.red = 0;
						data.blue = 0;
						data.yellow = 0;
					} else {
						player.sendMessage(
								"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
										+ ChatColor.GREEN + "/color blue/green/red/yellow/reset");
					}
				}
			} else {
				player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You need the rank " + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN " + ChatColor.RESET
						+ "to use this command");
			}
		}

		if (cmd.getName().equalsIgnoreCase("token") && sender instanceof Player) {
			if (player.hasPermission("scb.giveTokens")) {
				if (args.length == 0) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/token add <player> <amount>");
				} else if (args[0].equalsIgnoreCase("add")) {
					if (args.length == 1) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Incorrect usage! Try doing: " + ChatColor.GREEN + "/token add <player> <amount>");
					} else if (args.length == 2) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Incorrect usage! Try doing: " + ChatColor.GREEN + "/token add <player> <amount>");
					} else if (args.length == 3) {
						Player target = Bukkit.getServer().getPlayerExact(args[1]);
						try {
							int num = Integer.parseInt(args[2]);

							PlayerData data = this.getDataManager().getPlayerData(target);
							if (target != null) {
								data.tokens += num;

								player.sendMessage(
										"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You gave " + ChatColor.GREEN
												+ target.getName() + ChatColor.RESET + " " + num + " Tokens!");
								target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given "
										+ num + " Tokens!");
								if (this.getGameManager().GetInstanceOfPlayer(player) == null)
									getScoreboardManager().lobbyBoard(target);
								this.getDataManager().saveData(data);
							} else {
								player.sendMessage(
										"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
							}
						} catch (Exception e) {
							player.sendMessage(
									"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please enter a number!");
						}
					}
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/token add <player> <amount>");
				}
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank " + ChatColor.RED
						+ ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to use this command!");
			}
		}

		if (cmd.getName().equalsIgnoreCase("tp")) {
			if (player.hasPermission("scb.tp")) {
				if (args.length == 0) {
					player.sendMessage(color("&r&l(!) &rList of Teleport Commands:"));
					player.sendMessage(color("&r- &e/tp <player>"));
					player.sendMessage(color("&r- &e/tp <X> <Y> <Z>"));
				} else if (args.length == 1) {
					Player target = Bukkit.getServer().getPlayerExact(args[0]);

					if (target != null) {
						player.teleport(target.getLocation());
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Teleporting to "
								+ ChatColor.YELLOW + target.getName());
					} else {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please enter a player!");
					}
				} else if (args.length == 2) {
					player.sendMessage(color("&r&l(!) &rList of Teleport Commands:"));
					player.sendMessage(color("&r- &e/tp <player>"));
					player.sendMessage(color("&r- &e/tp <X> <Y> <Z>"));
				} else if (args.length == 3) {
					double x = Integer.parseInt(args[0]);
					double y = Integer.parseInt(args[1]);
					double z = Integer.parseInt(args[2]);

					player.teleport(new Location(player.getWorld(), x, y, z));
					player.sendMessage(color("&r&l(!) &rTeleporting to &e" + x + "&r, &e" + y + "&r, &e" + z));
				}
			} else
				player.sendMessage(color("&c&l(!) &rYou need the rank " + ChatColor.GOLD + ChatColor.BOLD
						+ "TRAINEE &rto use this command!"));
		}

		if (cmd.getName().equalsIgnoreCase("nick")) {
			GameInstance instance = this.getGameManager().GetInstanceOfPlayer(player);
			if (instance == null) {
				if (player.hasPermission("scb.nickname.use")) {
					if (args.length == 0 || args[0].equals(player.getName())) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.YELLOW
								+ "Your nickname has been reset!");
						player.setDisplayName("" + player.getName());
						return true;
					}

					String nick = "";
					if (args[0].matches("^[a-zA-Z0-9_]*$")) {
					} else {
						player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Please enter a name with only alphanumeric characters!");
						return true;
					}
					if (Bukkit.getPlayerExact(args[0]) == null) {
					} else {
						player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You cannot name yourself as another player!");
						return true;
					}
					if (args[0].length() <= 16) {
						nick += args[0] + " ";
					} else {
						player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Please enter a name up to 16 characters!");
						return true;
					}

					nick = nick.substring(0, nick.length() - 1);

					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You changed your name to "
							+ ChatColor.YELLOW + nick);
					player.setDisplayName("" + nick);
				} else {
					player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_RED + ChatColor.BOLD + "(!) "
							+ ChatColor.RESET + "You need a " + ChatColor.YELLOW + ChatColor.BOLD + "DONOR "
							+ ChatColor.RESET + "rank to access this command!");
				}
			} else {
				player.sendMessage(
						"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You cannot use this while in a game");
			}
		}
		if (cmd.getName().equalsIgnoreCase("tell") || cmd.getName().equalsIgnoreCase("msg")) {
			if (args.length == 0) {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
						+ ChatColor.GREEN + "/tell <player> <message>");
				return true;
			}
			Player target = Bukkit.getServer().getPlayerExact(args[0]);
			PlayerData data = this.getDataManager().getPlayerData(target);

			if (target != null) {
				if (data.pm == 0) {
					String message = "";

					for (int i = 1; i != args.length; i++) {
						message += args[i] + " ";
					}

					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GRAY + "You --> "
							+ target.getName() + ChatColor.RESET + ": " + ChatColor.RESET + message);
					target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GRAY
							+ player.getName() + " --> You" + ChatColor.RESET + ": " + ChatColor.RESET + message);
				} else if (data.pm == 1) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.YELLOW
							+ target.getName() + ChatColor.LIGHT_PURPLE + " has private messaging disabled!");
				}

			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("activegames"))
			new ActiveGamesGUI(this).inv.open(player);

		if (cmd.getName().equalsIgnoreCase("tournament")) {
			if (player.hasPermission("scb.toggleTournament")) {
				if (args.length != 1) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/tournament <toggle/reset/clear/end>");
				} else if (args[0].equalsIgnoreCase("toggle")) {
					if (tournament) {
						tournament = false;
						player.sendMessage(color("&e&l(!) &eTournament mode disabled!"));
						for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
							getScoreboardManager().lobbyBoard(onlinePlayers);
							onlinePlayers.getInventory().setItem(2, null);
						}
					} else {
						tournament = true;
						player.sendMessage(color("&e&l(!) &eTournament mode now enabled!"));
						for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
							PlayerData data = this.getDataManager().getPlayerData(onlinePlayers);
							getScoreboardManager().lobbyBoard(onlinePlayers);
							ItemStack tournament = ItemHelper.createSkullTexture(
									"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM0YTU5MmE3OTM5N2E4ZGYzOTk3YzQzMDkxNjk0ZmMyZmI3NmM4ODNhNzZjY2U4OWYwMjI3ZTVjOWYxZGZlIn19fQ==");
							onlinePlayers.getInventory().setItem(2,
									ItemHelper.setDetails(tournament, "&7>&f>&6&lTournament&f<&7<"));
							tourney.put(onlinePlayers.getName(), data.points);
						}
					}
				} else if (args[0].equalsIgnoreCase("reset")) {
					player.sendMessage(color("&e&l(!) &eResetting points!"));
					tourneyreset = true;
					for (String s : tourney.keySet()) {
						if (Bukkit.getOfflinePlayer(s).isOnline()) {
							Player p = Bukkit.getPlayer(s);
							PlayerData data = this.getDataManager().getPlayerData(p);
							data.points = 0;
							getScoreboardManager().lobbyBoard(p);
							this.getDataManager().saveData(data);
						}
						tourney.put(s, 0);
					}
				} else if (args[0].equalsIgnoreCase("clear")) {
					player.sendMessage(color("&e&l(!) &eRemoving all participants!"));
					for (Player p : Bukkit.getOnlinePlayers()) {
						getScoreboardManager().lobbyBoard(p);
						p.getInventory().setItem(2, null);
					}
					tourney.clear();
				} else if (args[0].equalsIgnoreCase("end")) {
					if (!tournament) {
						player.sendMessage(
								"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Tournament mode is not enabled");
						return false;
					}
					Bukkit.broadcastMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Ending tournament");
					// Hide tournament stats
					for (Player p : Bukkit.getOnlinePlayers()) {
						p.getInventory().setItem(6, null);
					}
					// Shoot fireworks
					Location newLoc = LobbyLoc();
					BukkitRunnable runnable = new BukkitRunnable() {
						int sec = 0;

						@Override
						public void run() {
							if (sec == 4) {
								this.cancel();
							} else {
								Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc, EntityType.FIREWORK);
								FireworkMeta fwm = fw.getFireworkMeta();
								fwm.setPower(1);

								Color c = null;
								if (sec == 0)
									c = Color.BLUE;
								else if (sec == 1)
									c = Color.LIME;
								else if (sec == 2)
									c = Color.GREEN;
								else
									c = Color.YELLOW;
								fwm.addEffect(FireworkEffect.builder().withColor(c).with(FireworkEffect.Type.BALL_LARGE)
										.flicker(true).build());
								fw.setFireworkMeta(fwm);
							}
							sec++;
						}

					};
					runnable.runTaskTimer(this, 0, 20);

					sortTourney();
					ArrayList<String> names = new ArrayList<>(tourney.keySet());

					new BukkitRunnable() {
						int size = Math.min(tourney.keySet().size(), 5);

						@Override
						public void run() {
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (size == 1) {
									p.sendTitle(color("&6And the winner is..."), "");
								} else {
									p.sendTitle(color("&aPlacing #" + size), "");
								}
								String name = names.get(size - 1);
								new BukkitRunnable() {
									@Override
									public void run() {
										p.sendTitle(color("&a" + name), color("&a" + tourney.get(name) + " points"));
									}
								}.runTaskLater(plugin, 50);
							}
							size--;
							if (size == 0)
								this.cancel();
						}
					}.runTaskTimer(plugin, 50, 150);

					// Display scores
					new BukkitRunnable() {
						@Override
						public void run() {
							tournamentend = true;
							String winnerName = names.get(0);
							if (Bukkit.getOfflinePlayer(winnerName).isOnline()) {
								Player winner = Bukkit.getPlayer(winnerName);
								Firework fw = (Firework) winner.getWorld().spawnEntity(winner.getLocation(),
										EntityType.FIREWORK);
								FireworkMeta fwm = fw.getFireworkMeta();
								fwm.setPower(1);
								fwm.addEffect(FireworkEffect.builder().withColor(Color.ORANGE)
										.with(FireworkEffect.Type.STAR).flicker(true).build());
								fw.setFireworkMeta(fwm);
							}
							for (Player p : Bukkit.getOnlinePlayers()) {
								ItemStack tournament = ItemHelper.createSkullTexture(
										"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM0YTU5MmE3OTM5N2E4ZGYzOTk3YzQzMDkxNjk0ZmMyZmI3NmM4ODNhNzZjY2U4OWYwMjI3ZTVjOWYxZGZlIn19fQ==");
								p.getInventory().setItem(6,
										ItemHelper.setDetails(tournament, "" + ChatColor.GRAY + "Tournament"));
								p.playSound(p.getLocation(), Sound.FIREWORK_TWINKLE2, 1, 0);
								p.sendMessage(color("&aTournament Scores:"));
								int count = 1;
								int placement = 0;
								for (String s : tourney.keySet()) {
									if (s.equals(p.getName())) {
										p.sendMessage(color("&a#" + count + " &e" + s + "&a - " + tourney.get(s)));
										placement = count;
									} else
										p.sendMessage(color("&a#" + count + " " + s + " - " + tourney.get(s)));
									count++;
								}
								p.sendMessage(color("&eYou placed #" + placement));
								new BukkitRunnable() {
									@Override
									public void run() {
										new TournamentGUI(plugin).inv.open(player);
									}
								}.runTaskLater(plugin, 100);
							}
							new BukkitRunnable() {
								@Override
								public void run() {
									player.sendMessage(color("&e&l(!) &eTournament mode disabled!"));
									tournament = false;
									tourneyreset = false;
									tournamentend = false;
									for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
										getScoreboardManager().lobbyBoard(onlinePlayers);
										onlinePlayers.getInventory().setItem(6, null);
									}
								}
							}.runTaskLater(plugin, 600);
						}
					}.runTaskLater(plugin, 150 * Math.min(tourney.keySet().size(), 5) - 50);

				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/tournament <toggle/reset/clear/end>");
				}
			} else {
				player.sendMessage(color("&r&l(!) &rYou need the rank &c&lOWNER &rto use this command!"));
			}
		}

		if (cmd.getName().equalsIgnoreCase("points")) {
			if (player.hasPermission("scb.points")) {
				if (args.length == 0) {
					player.sendMessage(color("&r&l(!) &rIncorrect usage! Try doing: &e/points <player>"));
				} else {
					Player target = Bukkit.getServer().getPlayerExact(args[0]);
					PlayerData data = this.getDataManager().getPlayerData(target);

					if (target != null) {
						if (data != null) {
							player.sendMessage(color("&r&l(!) &e" + target.getName() + "'s points: " + data.points));
						}
					} else {
						player.sendMessage(color("&r&l(!) &rPlease specify a player!"));
					}
				}
			} else {
				player.sendMessage(color("&r&l(!) &rYou need the rank &c&lOWNER &rto use this command!"));
			}
		}

		if (cmd.getName().equalsIgnoreCase("setpoints") && sender instanceof Player) {
			if (player.hasPermission("scb.setpoints")) {
				if (args.length < 2) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/setpoints <player> <amount>");
				} else {
					Player target = Bukkit.getServer().getPlayerExact(args[0]);
					try {
						int num = Integer.parseInt(args[1]);

						PlayerData data = this.getDataManager().getPlayerData(target);
						if (target != null) {
							data.points = num;

							player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You gave "
									+ ChatColor.GREEN + target.getName() + ChatColor.RESET + " " + num + " Tokens!");
							target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + num
									+ " Tokens!");
							if (this.getGameManager().GetInstanceOfPlayer(player) == null)
								getScoreboardManager().lobbyBoard(target);
							this.getDataManager().saveData(data);
						} else {
							player.sendMessage(
									"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
						}
					} catch (Exception e) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please enter a number!");
					}
				}
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank " + ChatColor.RED
						+ ChatColor.BOLD + "OWNER" + ChatColor.RESET + "to use this command!");
			}
		}

		if (cmd.getName().equalsIgnoreCase("mainworld")) {
			player.sendMessage("Test0");
			player.sendMessage("Test0");
			World minecadeLobby = getServer().createWorld(new WorldCreator("MinecadeLobby"));

			if (minecadeLobby != null)
				player.teleport(minecadeLobby.getSpawnLocation());

			player.sendMessage("Test1");
			player.sendMessage("Test2");
		}

		if (cmd.getName().equalsIgnoreCase("stats")) {
			GameInstance i = this.getGameManager().GetInstanceOfPlayer(player);

			if (i != null && i.state == GameState.STARTED)
				player.sendMessage(color("&c&l(!) &rYou cannot use this in a game!"));
			else {
				if (args.length == 0 || args[0].equals(player.getName())) {
					new StatsGUI(this).inv.open(player);
				} else if (args.length == 1) {
					Player target = Bukkit.getServer().getPlayerExact(args[0]);
					if (target != null) {
						new StatsGUI(this, target).inv.open(player);
						player.sendMessage(
								"" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Opening "
										+ ChatColor.YELLOW + target.getName() + "'s" + ChatColor.RESET + " statistics");
					} else
						player.sendMessage(
								"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "The specified target is not online!");
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("seen")) {
			if (args.length == 0) {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
						+ ChatColor.GREEN + "/seen <player>");
				return true;
			}
			OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(args[0]);
			if (target != null && target.hasPlayedBefore()) {
				long t = System.currentTimeMillis() - target.getLastPlayed();
				long h = TimeUnit.MILLISECONDS.toHours(t);
				long m = TimeUnit.MILLISECONDS.toMinutes(t);
				long s = TimeUnit.MILLISECONDS.toSeconds(t);
				if (!target.isOnline()) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + target.getName()
							+ " was last online " + ChatColor.GREEN + h + " hours, " + (m - h * 60) + " minutes, and "
							+ (s - m * 60) + " seconds ago");
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + target.getName()
							+ " was last online " + ChatColor.GREEN + "now");
				}
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
			}
		}
		if (cmd.getName().equalsIgnoreCase("ignite")) {
			if (player.hasPermission("scb.ignite")) {
				if (args.length == 0) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
							+ ChatColor.GREEN + "/ignite <player>");
					return true;
				}
				Player target = Bukkit.getServer().getPlayerExact(args[0]);

				if (target != null) {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have ignited "
							+ ChatColor.YELLOW + target.getName());
					target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were ignited by "
							+ ChatColor.YELLOW + player.getName());
					target.setFireTicks(1000);
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
					return false;
				}
			} else {
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank " + ChatColor.RED
						+ ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
			}
		}

		return false;

	}

	public int getTotalFish(Player player, FishRarity... rarity) {
		PlayerData data = this.getDataManager().getPlayerData(player);
		int totalFished = 0;
		for (FishType type : FishType.values()) {
			if (rarity == null || Arrays.asList(rarity).contains(type.getRarity())) {
				FishingDetails details = data.playerFishing.get(type.getId());
				if (details != null) {
					totalFished++;
				}
			}
		}
		return totalFished;
	}

	public int getTotalFish(Player player) {
		return getTotalFish(player, null);
	}

	@SuppressWarnings("deprecation")
	public void sendScoreboardUpdate(Player player) {
		// Organized tab list
		for (Player pl : Bukkit.getOnlinePlayers()) {
			StringBuilder teamName = new StringBuilder();
			Rank r = gameManager.getMain().getRankManager().getRank(player);
			if (r == null)
				teamName.append(Rank.values().length);
			else
				teamName.append(r.getTabListIndex());

			teamName.append("_").append(r);

			Scoreboard board = pl.getScoreboard();
			Team team = board.getTeam(teamName.toString());
			if (team == null) {
				team = board.registerNewTeam(teamName.toString());
				team.addPlayer(player);
			}

			String rank = this.getRankManager().getRank(player).getTagWithSpace();

			if (rank.length() >= 16) {
				String s = "";
				s = rank.substring(0, 9) + " ";
				team.setPrefix(s);
				continue;
			}
			team.setPrefix(rank);
		}
	}

	public Map<Player, Holograms> holograms = new HashMap<Player, Holograms>();

	/**
	 * This function handles when a player joins the server
	 * 
	 * @param e
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void joinEvent(PlayerJoinEvent e) {
		Player player = e.getPlayer(); // Gets the player that joined
		PlayerData data = this.getDataManager().getPlayerData(player); // Gets the player data from database
		String name = player.getName();

		getListener().resetDoubleJump(player);
		getListener().resetArmor(player);
		getListener().resetPotionEffects(player);
		getListener().checkIfTournament(player);
		getListener().setPlayerOnTablist(player);
		sendScoreboardUpdate(player); // This sets the rank next to player name above their head
		chatAnnouncementOnJoin(player);
		getScoreboardManager().lobbyBoard(player); // Gives the lobby scoreboard to player
		// getKillsLeaderboard().killsBoard(player); //Shows kills leaderboard to player

		// For join message:
		String rank = getRankManager().getRank(player).getTagWithSpace(); // Gets the player's rank
		e.setJoinMessage(color("&r&l[&a&l+&r&l] &r" + rank + "&b" + name + "&a connected"));

		if (data != null)
			player.setLevel(data.level); // Indication what the player's level is

		player.setHealth(20);
		player.setFoodLevel(20);
	}

	@SuppressWarnings("deprecation")
	private void chatAnnouncementOnJoin(Player p) {
		p.sendMessage("----------------------------------------------");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "        WELCOME TO THE MINEZONE NETWORK!");
		p.sendMessage("" + "     Be sure to join our Discord Server with " + ChatColor.GREEN + "/socials");
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage("----------------------------------------------");
		p.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + ChatColor.UNDERLINE + "MINEZONE",
				color("&2&lNEW &e&lFISHING &2&lLOBBY ACTIVITY!"));

		new BukkitRunnable() {
			@Override
			public void run() { // Runs after 4 seconds
				p.sendMessage("----------------------------------------------");
				p.sendMessage("");
				p.sendMessage(color("          &2&lNEW &e&lFISHING &2&lLOBBY ACTIVITY"));
				p.sendMessage("" + "     Try out fishing to get some amazing rewards");
				p.sendMessage("" + "     by clicking your Profile in your 8th slot!");
				p.sendMessage("");
				p.sendMessage("----------------------------------------------");
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
				p.sendTitle(color("&e&lFISHING"),
						color("&eVersion " + Version.FISHING.getVersion() + " available now!"));
			}
		}.runTaskLater(this, 80); // 80 ticks = 4 seconds (20 ticks per second)

		new BukkitRunnable() {
			@Override
			public void run() { // Runs after 20 seconds
				p.sendMessage("----------------------------------------------");
				p.sendMessage("");
				p.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "             Super Craft Blocks");
				p.sendMessage("");
				p.sendMessage(color("&7  Welcome to Super Craft Blocks! A custom suite of"));
				p.sendMessage(color("" + ChatColor.AQUA + "  Class PvP &7based gamemodes hosted exclusively on"));
				p.sendMessage("                    " + ChatColor.AQUA + ChatColor.UNDERLINE + "minezone.club");
				p.sendMessage("");
				if (p.hasPermission("scb.bonusTokens"))
					p.sendMessage(color("&c&l>> &rThanks for being a " + Rank.VIP.getTag() + "&r / "
							+ Rank.CAPTAIN.getTag() + "&r Supporter!"));

				p.sendMessage("----------------------------------------------");
			}
		}.runTaskLater(this, 400); // 400 ticks = 20 seconds (20 ticks per second)
	}

	public Map<Player, EntityArmorStand> msHologram = new HashMap<Player, EntityArmorStand>();

	public void mysteryChestHologram(Player p) {
		PlayerData data = this.getDataManager().getPlayerData(p);

		if (!(this.msHologram.containsKey(p))) {
			if (data != null) {
				Location loc = new Location(this.getLobbyWorld(), 194.520, 116, 641.500);
				WorldServer s = ((CraftWorld) loc.getWorld()).getHandle();
				EntityArmorStand stand = new EntityArmorStand(s);

				stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
				stand.setCustomName("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "MYSTERY CHESTS");
				stand.setCustomNameVisible(true);
				stand.setGravity(false);
				stand.setInvisible(true);
				PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

				loc = new Location(this.getLobbyWorld(), 194.520, 115.7, 641.500);
				stand = new EntityArmorStand(s);

				stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
				stand.setCustomName(color("&e&l" + data.mysteryChests + " &eto open!"));
				stand.setCustomNameVisible(true);
				stand.setGravity(false);
				stand.setInvisible(true);
				packet = new PacketPlayOutSpawnEntityLiving(stand);
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
				this.msHologram.put(p, stand);
			}
		}
	}

	@EventHandler
	public void leave(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		// this.packetMain.removePlayer(player);
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			try {
				out.writeUTF("PlayerCount");
				out.writeUTF("scb-1");
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
			b = new ByteArrayOutputStream();
			out = new DataOutputStream(b);

			try {
				out.writeUTF("PlayerCount");
				out.writeUTF("scb-2");
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		}, 10L);
		GameInstance game = this.getGameManager().GetInstanceOfPlayer(player);
		PunishAPI pu = PunishAPI.get();

		String IP = e.getPlayer().getAddress().getAddress().toString();
		if (pu.isPlayerNetworkBanned(player.getUniqueId())) {
			e.setQuitMessage(null);
			return;
		}
		if (pu.isIPBanned(IP)) {
			e.setQuitMessage(null);
			return;
		}
		if (pu.isPlayerBannedNoUnban(player.getUniqueId())) {
			e.setQuitMessage(null);
			return;
		}

		if (game != null && game.getGameSettings() != null) {
			game.getGameSettings().removeFromStartVotes(player);
			game.getGameSettings().removeFromGameTypeVotes(player);
			game.getGameSettings().removeFromLightningVotes(player);
			game.getGameSettings().removeFromTimeVotes(player);
		}

		e.setQuitMessage("" + ChatColor.BOLD + "[" + ChatColor.RED + ChatColor.BOLD + "-" + ChatColor.RESET
				+ ChatColor.BOLD + "] " + ChatColor.RESET + getRankManager().getRank(player).getTagWithSpace()
				+ ChatColor.AQUA + player.getName() + ChatColor.RED + " disconnected");
	}

	public Location hologramLoc(Player player) {
		return new Location(lobbyWorld, 288.557, 114, 2362.646);
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDescending(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public void sortTourney() {
		tourney = sortMapByValueDescending(tourney);
	}

	public Location LobbyLoc() {
		// return new Location(lobbyWorld, -199.517, 89.98466, -7.519);
		// return new Location(lobbyWorld, -5.457, 143, 19.522);
		// return new Location(lobbyWorld, 288.507, 119, 2346.529);

		// return new Location(lobbyWorld, -58.507, 125, -18.519, -179, -1);

		// if (this.getCommands() != null || this.getSWCommands() != null)
		return new Location(lobbyWorld, 189.495, 115, 629.438, -0, 1);
		// else
		// return new Location(lobbyWorld, 0.478, 51, 0.550);
	}

	public World getLobbyWorld() {
		return lobbyWorld;
	}

	public void LobbyItems(Player player) {
		if (this.getCommands() != null) {
			player.getInventory().setItem(1,
					ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER), "&b>&3>&f&lQuick Join&3<&b<"));
			player.getInventory().setItem(3,
					ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK), "&9>&1>&f&lClasses&1<&9<"));
			player.getInventory().setItem(8,
					ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR), "&e>&6>&f&lChallenges&6<&e<"));
		}
		player.getInventory().setItem(0,
				ItemHelper.setDetails(new ItemStack(Material.COMPASS), "&a>&2>&f&lGame Selector&2<&a<"));
		player.getInventory().setItem(4,
				ItemHelper.setDetails(new ItemStack(Material.CHEST), "&d>&5>&f&lCosmetics&5<&d<"));
		ItemStack stats = ItemHelper.createSkullHeadPlayer(1, player.getName());
		player.getInventory().setItem(7, ItemHelper.setDetails(stats, "&c>&4>&fProfile&4<&c<"));

		player.getInventory().setItem(5, getFishingRod(player));

		if (tournament) {
			ItemStack tournament = ItemHelper.createSkullTexture(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM0YTU5MmE3OTM5N2E4ZGYzOTk3YzQzMDkxNjk0ZmMyZmI3NmM4ODNhNzZjY2U4OWYwMjI3ZTVjOWYxZGZlIn19fQ==");
			player.getInventory().setItem(2, ItemHelper.setDetails(tournament, "&7>&f>&6&lTournament&f<&7<"));
		}
		player.setAllowFlight(true);
	}

	public void ResetPlayer(Player player) {
		player.teleport(LobbyLoc());
		player.setHealth(20.0f);
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);
		LobbyItems(player);
		mysteryChestHologram(player);

		if (!(holograms.containsKey(player)))
			holograms.put(player, new Holograms(this, player)); // All players' holograms
	}

	public Location GetSpawnLocation() {
		// return new Location(lobbyWorld, -199.517, 89.98466, -7.519);
		// return new Location(lobbyWorld, 288.507, 119, 2346.529);

		// return new Location(lobbyWorld, -58.507, 125, -18.519, -179, -1);
		return new Location(lobbyWorld, 189.495, 115, 629.438, -0, 1);
	}

	public boolean isInBounds(Location loc) {
		Vector v = new Vector(189.533, 115, 629.513);
		Location centre = new Location(lobbyWorld, v.getX(), v.getY(), v.getZ());
		double boundsX = 150, boundsZ = 160;

		if (Math.abs(centre.getX() - loc.getX()) > boundsX)
			return false;
		if (Math.abs(centre.getZ() - loc.getZ()) > boundsZ)
			return false;
		return true;
	}

	public String progressBar(int progress, int nextLevel, int segments) {
		String str = "";
		str += this.color("&8[");
		double frac = (double) progress / nextLevel;
		for (int i = 0; i < segments; i++) {
			if (i < Math.floor(frac * segments))
				str += this.color("&a|");
			else
				str += this.color("&7|");
		}
		str += this.color("&8] &7(" + progress + "/" + nextLevel + ")");
		return str;
	}

	public void SendToFactions(Player player) {
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			out.writeUTF("Connect");
			out.writeUTF("scb-2");
		} catch (IOException ex) {

		}
		player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
	}

	@Override
	public void onDisable() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		for (GameInstance instance : gameManager.gameMap.values()) {
			for (Map.Entry<Player, WinEffects> entry : instance.effects.entrySet())
				entry.getValue().removeWinEffects();
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			p.kickPlayer(color("&c&lSERVER IS RESTARTING\n&e\n" + msg.get(new Random().nextInt(msg.size()))));
		}
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(color("&4&l(!) &eServer Restarting..."));
		Bukkit.broadcastMessage("");

		// Saving data for players on server restart
		for (Player player : Bukkit.getOnlinePlayers()) {
			PlayerData data = this.getDataManager().getPlayerData(player);
			this.getDataManager().saveData(data);
		}

		getLeaderboard().close();
		getFishingLeaderboard().close();
		Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin);
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
		getLogger().info("(!) You have disabled Minezone-Core");
		for (World world : Bukkit.getWorlds()) {
			Bukkit.unloadWorld(world, false);
		}
	}

	public ItemStack getFishingRod(Player player) {
		PlayerData data = getDataManager().getPlayerData(player);

		ItemStack fishingRod = ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD), "&3&lGo Fishing!",
				"&fAnywhere with water", "&fFish for junk, fish and treasure", "&fEarn unique rewards");
		ItemHelper.setUnbreakable(fishingRod);
		if (data != null) {
			if (data.lure == 1 && data.lureLevel > 0) {
				ItemHelper.addEnchant(fishingRod, Enchantment.LURE, data.lureLevel);
			}
		}

		return fishingRod;
	}
}
