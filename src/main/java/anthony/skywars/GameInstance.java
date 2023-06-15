package anthony.skywars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import anthony.skywars.kits.Kit;
import anthony.skywars.kits.KitInstance;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class GameInstance {

	public List<Player> players;
	public List<Player> spectators;
	private GameManager gm;
	private Maps map;
	private GameState state;
	private World mapWorld;
	public HashMap<Player, FastBoard> boards = new HashMap();
	public BukkitRunnable runnable;
	public boolean populateChests;
	public Map<Player, Kit> selectedKit;
	public int totalPlayers = 0;
	public Map<Player, PlayerClass> playersStats;

	public GameInstance(GameManager gm, Maps map) {
		this.gm = gm;
		this.map = map;
		this.state = GameState.LOBBY; // Default game state
		this.players = new ArrayList<Player>();
		this.spectators = new ArrayList<Player>();
		this.selectedKit = new HashMap<Player, Kit>();
		this.playersStats = new HashMap<Player, PlayerClass>();
		this.populateChests = false;
		this.initialiseMap();
	}

	// Getters:

	public GameManager getManager() {
		return this.gm;
	}

	public Maps getMap() {
		return this.map;
	}

	public GameState getState() {
		return this.state;
	}

	public void initialiseMap() {
		WorldCreator w = new WorldCreator(map.GetInstance().worldName).environment(World.Environment.NORMAL);
		w.generator(new VoidGenerator());
		mapWorld = Bukkit.getServer().createWorld(w);
		mapWorld.setAutoSave(false);
	}

	public boolean hasPlayer(Player player) {
		return this.players.contains(player);
	}

	public boolean hasSpectator(Player player) {
		return this.spectators.contains(player);
	}

	public void setBoard(Player player) {
		FastBoard board = new FastBoard(player);
		boards.put(player, board);

		if (this.map != null) {
			board.updateTitle(this.getManager().getMain().color("&r&l" + this.map.toString()));
			board.updateLines("", "" + ChatColor.RESET + ChatColor.BOLD + "Players:",
					" " + ChatColor.RESET + this.players.size() + "/" + map.GetInstance().getMaxPlayers(), "",
					"" + ChatColor.RESET + ChatColor.BOLD + "Kit:",
					" " + ChatColor.RESET + this.selectedKit.get(player).getKitName() + ChatColor.GRAY
							+ ChatColor.ITALIC + " Default",
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Status:",
					"" + ChatColor.RESET + ChatColor.ITALIC + " Waiting..");
		}
	}

	private void removeArmor(Player player) { // Remove player's armor
		ItemStack air = new ItemStack(Material.AIR, 1);
		player.getInventory().setHelmet(air);
		player.getInventory().setChestplate(air);
		player.getInventory().setLeggings(air);
		player.getInventory().setBoots(air);
	}

	private void lobbyItems(Player player) { // Gives player the waiting lobby items
		this.removeArmor(player);
		player.getInventory().clear();
		player.getInventory().setItem(0,
				ItemHelper.setDetails(new ItemStack(Material.COMPASS),
						this.getManager().getMain().color("&eKit Selector"),
						this.getManager().getMain().color("&7Right click to select a kit")));
		// player.getInventory().setItem(4,
		// ItemHelper.setDetails(new ItemStack(Material.CHEST),
		// this.getManager().getMain().color("&eCosmetics")));
		player.getInventory().setItem(7, ItemHelper.setDetails(new ItemStack(Material.SKULL_ITEM),
				this.getManager().getMain().color("&eProfile")));
		player.getInventory().setItem(8,
				ItemHelper.setDetails(new ItemStack(Material.BARRIER),
						this.getManager().getMain().color("&cLeave Game"),
						this.getManager().getMain().color("&7Click to leave your game")));
	}

	public void addPlayer(Player player) { // Adding player to game
		if (this.getManager().getMain().msHologram.containsKey(player)) {
			this.getManager().getMain().msHologram.remove(player);
		}
		if (getState() == GameState.LOBBY) {
			if (!(hasPlayer(player))) {
				if (this.players.size() != this.map.GetInstance().getMaxPlayers()) {
					this.players.add(player);
					TellAll(this.getManager().getMain().color("&2&l(!) &e" + player.getName() + " &rjoined ("
							+ this.players.size() + "/" + this.map.GetInstance().getMaxPlayers() + ")"));
					player.sendMessage(
							this.getManager().getMain().color("&2&l(!) &rYou have joined &r&l" + this.getMap()));
					this.checkForStart();
					this.sendToGame(player);
					this.lobbyItems(player);
					this.selectedKit.put(player, Kit.Knight); // Default kit if none will be selected
					this.setBoard(player);
				} else
					player.sendMessage(this.getManager().getMain().color("&c&l(!) &rThis game is full!"));
			} else
				player.sendMessage(this.getManager().getMain().color("&c&l(!) &rYou are already in a game!"));
		} else
			player.sendMessage(this.getManager().getMain().color("&c&l(!) &rThis game is already playing!"));
	}

	public void addSpectator(Player player) { // Adding spectator to game
		if (this.getManager().getMain().msHologram.containsKey(player)) {
			this.getManager().getMain().msHologram.remove(player);
		}
		if (getState() == GameState.STARTED) {
			if (!(hasSpectator(player))) {
				this.spectators.add(player);
				player.sendMessage(this.getManager().getMain()
						.color("&2&l(!) &rYou are spectating on &e" + this.getMap() + "&r. Use &e/leave &rto leave"));
				player.setGameMode(GameMode.SPECTATOR);
				MapInstance i = this.map.GetInstance();
				// Teleports to spectate location:
				player.teleport(new Location(this.mapWorld, i.specLoc.getX(), i.specLoc.getY(), i.specLoc.getZ()));
			} else
				player.sendMessage(this.getManager().getMain().color("&c&l(!) &rYou are already in a game!"));
		} else
			player.sendMessage(this.getManager().getMain().color("&c&l(!) &rThis game is not playing!"));
	}

	private void sendToGame(Player player) {
		Vector vec = this.map.GetInstance().lobbyLoc;
		Location loc = new Location(this.mapWorld, vec.getX(), vec.getY(), vec.getZ());
		player.teleport(loc);
		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
	}

	private void getSpawnLoc() {
		MapInstance i = this.map.GetInstance();
		Location loc = null;
		List<Vector> spawnLocs = new ArrayList<Vector>();
		spawnLocs = i.spawnPos;
		int count = 0;

		for (Player gamePlayer : this.players) {
			loc = new Location(this.mapWorld, spawnLocs.get(count).getX(), spawnLocs.get(count).getY(),
					spawnLocs.get(count).getZ());
			gamePlayer.teleport(loc);
			count++;
		}
	}

	private void checkForStart() {
		if (this.players.size() == 2)
			this.startTimer();
	}

	private void gameTicks() {
		MapInstance i = this.map.GetInstance();
		int count = i.chestLocs.size();
		BukkitRunnable runnable = new BukkitRunnable() {

			@Override
			public void run() {

				for (int a = 0; a < count; a++) {
					Location loc = new Location(mapWorld, i.chestLocs.get(a).getX(), i.chestLocs.get(a).getY(),
							i.chestLocs.get(a).getZ());
					Block b = mapWorld.getBlockAt(loc);

					if (b.getState() instanceof Chest) {
						Chest chest = (Chest) b.getState();

						if (chest != null) {
							Inventory inventory = chest.getBlockInventory();
							boolean hasItems = false;

							for (ItemStack item : inventory.getContents()) {
								if (item != null) { // Checks to see if there's a block in the chest
									hasItems = true; // Chest does have items
									break;
								}
							}

							if (hasItems == false) {
								Location standLoc = new Location(mapWorld, loc.getX(), loc.getY() - 0.5, loc.getZ());
								ArmorStand stand = (ArmorStand) standLoc.getWorld().spawnEntity(standLoc,
										EntityType.ARMOR_STAND);
								stand.setVisible(false);
								stand.setGravity(false);
								stand.setCustomNameVisible(true);
								stand.setCustomName(getManager().getMain().color("&cLooted"));
								for (Entity e : mapWorld.getEntities()) {
									if (e instanceof ArmorStand) {
										ArmorStand st = (ArmorStand) e;
										loc.setY(loc.getY() - 0.5);

										if (st.getLocation() == loc) {
											st.remove();
											e.remove();
											break;
										}
									}
								}
							}
						}
					}
				}
			}

		};
		runnable.runTaskTimer(getManager().getMain(), 0, 20);
	}

	private void populateChestsInWorld(String worldName) {
		MapInstance i = this.map.GetInstance();

		// FOR ISLAND CHESTS
		int count = i.chestLocs.size();
		Location loc = null;

		for (int a = 0; a < count; a++) {
			loc = new Location(this.mapWorld, i.chestLocs.get(a).getX(), i.chestLocs.get(a).getY(),
					i.chestLocs.get(a).getZ());
			Block b = this.mapWorld.getBlockAt(loc);

			if (b.getState() instanceof Chest) {
				Chest chest = (Chest) b.getState();

				if (chest != null) {
					Inventory inventory = chest.getBlockInventory();
					inventory.clear(); // Reset all previous items
					populateChest(inventory);
					Location standLoc = new Location(this.mapWorld, loc.getX(), loc.getY() - 0.5, loc.getZ());
					ArmorStand stand = (ArmorStand) standLoc.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
					stand.setVisible(false);
					stand.setGravity(false);
					stand.setCustomNameVisible(true);
					stand.setCustomName(this.getManager().getMain().color("&eLoot"));
				}
			}
		}

		// FOR MID CHESTS
		count = i.opChestLocs.size();
		loc = null;

		for (int a = 0; a < count; a++) {
			loc = new Location(this.mapWorld, i.opChestLocs.get(a).getX(), i.opChestLocs.get(a).getY(),
					i.opChestLocs.get(a).getZ());
			Block b = this.mapWorld.getBlockAt(loc);

			if (b.getState() instanceof Chest) {
				Chest chest = (Chest) b.getState();

				if (chest != null) {
					Inventory inventory = chest.getBlockInventory();
					inventory.clear(); // Reset all previous items
					populateOPChest(inventory);
					ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
					stand.setVisible(false);
					stand.setGravity(false);
					stand.setCustomNameVisible(true);
					stand.setCustomName(this.getManager().getMain().color("&eLoot"));
				}
			}
		}
	}

	private void populateOPChest(Inventory inventory) {
		Random r = new Random();
		ItemStack enchIronChest = ItemHelper.addEnchant(new ItemStack(Material.IRON_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		ItemStack enchIronChest2 = ItemHelper.addEnchant(new ItemStack(Material.IRON_CHESTPLATE),
				Enchantment.PROTECTION_EXPLOSIONS, 3);
		ItemStack enchIronLeg = ItemHelper.addEnchant(new ItemStack(Material.IRON_LEGGINGS),
				Enchantment.PROTECTION_PROJECTILE, 3);
		ItemStack enchBow = ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_FIRE, 1);
		ItemStack enchBow2 = ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_KNOCKBACK, 1);

		ItemStack fireRes = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				this.getManager().getMain().color("&cFire Res Potion &7(30 Seconds)"));
		Potion pot = new Potion(1);
		pot.setType(PotionType.FIRE_RESISTANCE);
		pot.setSplash(true);
		PotionMeta meta = (PotionMeta) fireRes.getItemMeta();
		meta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0), true);
		fireRes.setItemMeta(meta);
		pot.apply(fireRes);

		ItemStack regen = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				this.getManager().getMain().color("&bRegen Potion &7(15 Seconds)"));
		Potion pot2 = new Potion(1);
		pot2.setType(PotionType.REGEN);
		pot2.setSplash(true);
		PotionMeta meta2 = (PotionMeta) regen.getItemMeta();
		meta2.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 0), true);
		regen.setItemMeta(meta2);
		pot2.apply(regen);

		ItemStack speed = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				this.getManager().getMain().color("&2Speed II Potion &7(15 Seconds)"));
		Potion pot3 = new Potion(1);
		pot3.setType(PotionType.SPEED);
		pot3.setSplash(true);
		PotionMeta meta3 = (PotionMeta) speed.getItemMeta();
		meta3.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1), true);
		speed.setItemMeta(meta3);
		pot3.apply(speed);

		List<ItemStack> lootItems = new ArrayList<>(Arrays.asList(new ItemStack(Material.ENDER_PEARL),
				new ItemStack(Material.GOLDEN_APPLE), new ItemStack(Material.LADDER, 10),
				new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.WATER_BUCKET),
				new ItemStack(Material.DIAMOND_HELMET), new ItemStack(Material.DIAMOND_BOOTS),
				new ItemStack(Material.FLINT_AND_STEEL), enchIronChest, enchIronChest2, enchIronLeg, enchBow, enchBow2,
				new ItemStack(Material.FISHING_ROD), new ItemStack(Material.WOOD, 32),
				new ItemStack(Material.STONE, 64), new ItemStack(Material.ARROW, 16), regen, fireRes, speed));
		int itemCount = 6;
		int randInv = 0;
		// List<Integer> usedSlots = new ArrayList<Integer>();
		// List<Integer> usedItems = new ArrayList<Integer>();
		ItemStack randomItem = null;

		for (int i = 0; i < itemCount; i++) {
			randInv = r.nextInt(26) + 1;
			randomItem = lootItems.get(r.nextInt(lootItems.size()));
			inventory.setItem(randInv, randomItem);
		}
	}

	private void populateChest(Inventory inventory) {
		Random r = new Random();
		List<ItemStack> lootItems = new ArrayList<>(Arrays.asList(new ItemStack(Material.DIAMOND_PICKAXE),
				new ItemStack(Material.IRON_AXE), new ItemStack(Material.FISHING_ROD),
				new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.WATER_BUCKET),
				new ItemStack(Material.ARROW, 12), new ItemStack(Material.BOW), new ItemStack(Material.SNOW_BALL, 12)));

		ItemStack enchIronSword = ItemHelper.addEnchant(new ItemStack(Material.IRON_SWORD), Enchantment.DAMAGE_ALL, 1);
		List<ItemStack> requiredWeapons = new ArrayList<>(
				Arrays.asList(new ItemStack(Material.DIAMOND_SWORD), enchIronSword));
		List<ItemStack> blocks = new ArrayList<>(Arrays.asList(new ItemStack(Material.WOOD, 12),
				new ItemStack(Material.STONE, 24), new ItemStack(Material.MOSSY_COBBLESTONE, 36)));
		List<ItemStack> requiredEquip = new ArrayList<>(Arrays.asList(new ItemStack(Material.IRON_CHESTPLATE),
				new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_HELMET),
				new ItemStack(Material.CHAINMAIL_HELMET), new ItemStack(Material.CHAINMAIL_BOOTS),
				new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.CHAINMAIL_LEGGINGS)));

		int itemCount = 3;
		int randInv = 0;
		List<Integer> usedSlots = new ArrayList<Integer>();
		// List<Integer> usedItems = new ArrayList<Integer>();
		ItemStack randomItem = null;

		for (int i = 0; i < itemCount; i++) {
			randInv = r.nextInt(26) + 1;
			boolean loop = true;

			while (loop) {
				if (usedSlots.contains(randInv))
					randInv = r.nextInt(26) + 1;
				else
					loop = false; // Break the loop
			}
			randomItem = lootItems.get(r.nextInt(lootItems.size()));
			inventory.setItem(randInv, randomItem);
			usedSlots.add(randInv);
		}

		itemCount = r.nextInt(3) + 1;

		for (int i = 0; i < itemCount; i++) { // For spawning blocks in every island chest
			randInv = r.nextInt(26) + 1;

			boolean loop = true;

			while (loop) {
				if (usedSlots.contains(randInv))
					randInv = r.nextInt(26) + 1;
				else
					loop = false; // Break the loop
			}
			randomItem = blocks.get(r.nextInt(blocks.size()));
			inventory.setItem(randInv, randomItem);
			usedSlots.add(randInv);
		}

		itemCount = r.nextInt(2) + 1;

		for (int i = 0; i < itemCount; i++) { // For spawning weapons in every island chest
			randInv = r.nextInt(26) + 1;

			boolean loop = true;

			while (loop) {
				if (usedSlots.contains(randInv))
					randInv = r.nextInt(26) + 1;
				else
					loop = false; // Break the loop
			}
			randomItem = requiredWeapons.get(r.nextInt(requiredWeapons.size()));
			inventory.setItem(randInv, randomItem);
			usedSlots.add(randInv);
		}

		itemCount = 2;

		for (int i = 0; i < itemCount; i++) { // For spawning blocks in every island chest
			randInv = r.nextInt(26) + 1;

			boolean loop = true;

			while (loop) {
				if (usedSlots.contains(randInv))
					randInv = r.nextInt(26) + 1;
				else
					loop = false; // Break the loop
			}
			randomItem = requiredEquip.get(r.nextInt(requiredEquip.size()));
			inventory.setItem(randInv, randomItem);
			usedSlots.add(randInv);
		}
	}

	private void startTimer() {
		if (runnable == null) {
			runnable = new BukkitRunnable() {
				int ticks = 15;
				MapInstance i = map.GetInstance();

				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					if (ticks == 0) {
						startGame();
						runnable = null;
						this.cancel();
					} else {
						for (Player gamePlayer : players) {
							boards.get(gamePlayer).updateLine(8, getManager().getMain().color(" &r" + ticks + "s"));
							boards.get(gamePlayer).updateLine(7, getManager().getMain().color("&r&lStarting In:"));
							boards.get(gamePlayer).updateLine(2,
									getManager().getMain().color(" &r" + players.size() + "/" + i.getMaxPlayers()));
						}
					}

					if (ticks <= 5 && ticks > 0) {
						for (Player gamePlayer : players) {
							gamePlayer.sendTitle("", getManager().getMain().color("&e" + ticks + "s"));
							gamePlayer.playSound(gamePlayer.getLocation(), Sound.NOTE_PLING, 1, 1);
						}
					}

					if (ticks == 15) {
						Bukkit.broadcastMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ ChatColor.GREEN + ChatColor.BOLD + "A game on " + ChatColor.RESET + ChatColor.BOLD
								+ map.toString() + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD
								+ " is starting in 15 seconds.");
						Bukkit.broadcastMessage(
								"" + "    " + ChatColor.GREEN + ChatColor.BOLD + " Use " + ChatColor.RESET + "/join "
										+ map.toString() + ChatColor.GREEN + ChatColor.BOLD + " to join!");
					}

					ticks--;
				}
			};
			runnable.runTaskTimer(getManager().getMain(), 0, 20);
		}
	}

	private void loadKits() { // Load kits for each player
		KitInstance ki = null;
		for (Player gamePlayer : this.players) {
			ki = this.selectedKit.get(gamePlayer).getKitInstance();
			gamePlayer.getInventory().clear();

			for (ItemStack item : ki.kitItems)
				gamePlayer.getInventory().addItem(item);

			String msg = this.getManager().getMain()
					.color("&2&l(!) &eSelected Kit: &e&l" + this.selectedKit.get(gamePlayer).getKitName());
			PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
			CraftPlayer craft = (CraftPlayer) gamePlayer;
			craft.getHandle().playerConnection.sendPacket(packet);
		}
	}

	private void events() {
		BukkitRunnable r = new BukkitRunnable() {
			int time = 2;
			int seconds = 59;

			@Override
			public void run() {
				for (Player gamePlayer : players)
					boards.get(gamePlayer).updateLine(8, " " + ChatColor.RESET + "" + time + "m " + seconds + "s");
				for (Player spec : spectators)
					if (boards.get(spec) != null)
						boards.get(spec).updateLine(8, " " + ChatColor.RESET + "" + time + "m " + seconds + "s");
				if (time == 0 && seconds == 0) {
					this.cancel();
					populateChestsInWorld(mapWorld.getName()); // To reset all chests
					for (Player gamePlayer : players) // Update scoreboard for game players
						boards.get(gamePlayer).updateLine(8, " " + ChatColor.RESET + "Full!");
					for (Player spec : spectators) // Update scoreboard for game players
						boards.get(spec).updateLine(8, " " + ChatColor.RESET + "Full!");

					TellAll(getManager().getMain().color("&2&l(!) &rChests have been reset! Loot them up!"));
				}

				if (getState() == GameState.ENDED)
					this.cancel();

				if (seconds == 0) {
					time--;
					seconds = 59;
				}

				seconds--;
			}
		};
		r.runTaskTimer(getManager().getMain(), 0, 20);
	}

	public void startGame() {
		this.state = GameState.STARTED;
		this.totalPlayers = this.players.size();
		this.populateChestsInWorld(mapWorld.getName());
		TellAll(getManager().getMain().color("&2&l(!) &rGame Started!"));
		this.getSpawnLoc(); // Teleport players to map
		this.loadKits(); // Load each player's kit
		this.events(); // Start the game events such as chest re-populate
		this.gameTicks(); // Starts ticking through the game, mainly for chests

		for (Player gamePlayer : this.players) {
			PlayerClass pc = new PlayerClass(this);
			this.playersStats.put(gamePlayer, pc);
			this.gameBoard(gamePlayer); // Gives players the game scoreboard
		}
	}

	public void gameBoard(Player player) {
		this.boards.get(player).updateLines("", "" + ChatColor.RESET + ChatColor.BOLD + "Players Left:",
				" " + ChatColor.RESET + this.players.size() + "/" + totalPlayers, "",
				"" + ChatColor.RESET + ChatColor.BOLD + "Kills:", " " + this.playersStats.get(player).getKills(), "",
				"" + ChatColor.RESET + ChatColor.BOLD + "Chests Reset:", " " + ChatColor.RESET + "3m 0s",
				"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
				"" + ChatColor.AQUA + "minezone.club");
	}

	public void updateBoard() {
		for (Player gamePlayer : this.players)
			this.boards.get(gamePlayer).updateLine(2, " " + ChatColor.RESET + this.players.size() + "/" + totalPlayers);
		for (Player spec : this.spectators)
			if (this.boards.get(spec) != null)
				this.boards.get(spec).updateLine(2, " " + ChatColor.RESET + this.players.size() + "/" + totalPlayers);
	}

	public void checkForWin() { // Checking to see if there's a winner yet
		this.updateBoard();
		if (this.players.size() == 1)
			if (this.players.get(0) != null)
				this.winGame(this.players.get(0));
	}

	@SuppressWarnings("deprecation")
	private void winGame(Player winner) { // Player wins the game
		winner.sendTitle(this.getManager().getMain().color("&e&lVICTORY"),
				this.getManager().getMain().color("&eYou won the game!"));
		this.getManager().getMain().ResetPlayer(winner);
		this.getManager().getMain().LobbyBoard(winner);
		this.removeArmor(winner);
		String tag = this.getManager().getMain().getRankManager().getRank(winner).getTagWithSpace();
		Bukkit.broadcastMessage(this.getManager().getMain()
				.color("&2&l(!) " + tag + "&e" + winner.getName() + " &rjust won on &e&l" + this.getMap().toString()));

		for (Player spec : this.spectators) {
			this.getManager().getMain().ResetPlayer(spec);
			this.getManager().getMain().LobbyBoard(spec);
			this.removeArmor(spec);
			spec.sendTitle(this.getManager().getMain().color("&c&lGAME LOST"),
					this.getManager().getMain().color("&e" + winner.getName() + " won the game!"));
		}
		this.endGame();
	}

	public void endGame() { // Ends the game
		this.state = GameState.ENDED;
		Player player = null;

		for (Entity e : mapWorld.getEntities()) { // Removing all entities from the game world once game over
			if (!(e instanceof Player)) {
				e.remove();
			} else { // If players still left in world
				player = (Player) e;
				this.getManager().getMain().ResetPlayer(player);
				player.setGameMode(GameMode.ADVENTURE);
				this.getManager().getMain().LobbyBoard(player);
				player.getInventory().clear();
				this.getManager().getMain().LobbyItems(player);
			}
		}

		BukkitRunnable r = new BukkitRunnable() {

			@Override
			public void run() {
				Bukkit.unloadWorld(mapWorld, false);

				if (Bukkit.unloadWorld(mapWorld, false)) {
					this.cancel();
				}
			}
		};
		r.runTaskTimer(getManager().getMain(), 0, 1);
		this.getManager().removeMap(this.map);
	}

	public boolean removePlayer(Player player) { // Remove player from game
		if (this.players.remove(player)) {
			if (this.playersStats != null && this.playersStats.containsKey(player))
				this.playersStats.remove(player);

			if (this.getState() == GameState.LOBBY) {
				if (this.players.size() == 0) {
					this.getManager().removeMap(this.map);
				} else if (this.players.size() == 1) {
					runnable.cancel();
					runnable = null;
					TellAll(this.getManager().getMain().color("&c&l(!) &rGame start cancelled. Not enough players!"));

					for (Player gamePlayer : this.players) { // Update board for people still in lobby back to default
						this.boards.get(gamePlayer).updateLine(7, "" + ChatColor.RESET + ChatColor.BOLD + "Status:");
						this.boards.get(gamePlayer).updateLine(8,
								" " + ChatColor.RESET + ChatColor.ITALIC + "Waiting..");
					}
				}

				for (Player gamePlayer : this.players)
					this.boards.get(gamePlayer).updateLine(2,
							" " + ChatColor.RESET + this.players.size() + "/" + this.map.GetInstance().getMaxPlayers());
			} else if (this.getState() == GameState.STARTED) {
				TellAll(getManager().getMain().color("&2&l(!) &e" + player.getName() + " &chas left the game!"));
				this.checkForWin(); // Checks to see if there's 1 player left
			}
			return true;
		}

		return false;
	}

	public void TellAll(String msg) {
		for (Player gamePlayer : this.players)
			gamePlayer.sendMessage(msg);
		for (Player spec : this.spectators)
			spec.sendMessage(msg);
	}
}
