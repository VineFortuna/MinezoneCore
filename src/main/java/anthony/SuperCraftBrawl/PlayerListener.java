package anthony.SuperCraftBrawl;

import java.util.ArrayList; 
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.gui.ChallengesGUI;
import anthony.SuperCraftBrawl.gui.ClassSelectorGUI;
import anthony.SuperCraftBrawl.gui.CosmeticsGUI;
import anthony.SuperCraftBrawl.gui.GameSelectorGUI;
import anthony.SuperCraftBrawl.gui.PrefsGUI;
import anthony.SuperCraftBrawl.gui.StatsGUI;
import anthony.SuperCraftBrawl.gui.TournamentGUI;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import me.itzzmic.minezone.api.PunishAPI;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;

public class PlayerListener implements Listener {

	private final Core main;
	public ScoreboardManager scoreManager = Bukkit.getScoreboardManager();
	public Scoreboard c;

	public PlayerListener(Core main) {
		this.main = main;
		this.main.getServer().getPluginManager().registerEvents(this, main);
		this.c = scoreManager.getNewScoreboard();
	}
	
	/**
	 * This function just resets player double jump & sets gamemode to Adventure
	 * @param player to be reset
	 */
	public void resetDoubleJump(Player p) {
		p.setAllowFlight(false);
		p.setAllowFlight(true);
		p.setGameMode(GameMode.ADVENTURE);
	}
	
	/**
	 * This function resets the armor of a player
	 * @param p which is Player to remove armor
	 */
	public void resetArmor(Player p) {
		p.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
		p.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
		p.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
		p.getInventory().setBoots(new ItemStack(Material.AIR, 1));
	}
	
	/**
	 * This function resets the Player's potion effects if any is active
	 * @param p which is Player to remove effects
	 */
	public void resetPotionEffects(Player p) {
		for (PotionEffect type : p.getActivePotionEffects()) //Loop through all active effects
			p.removePotionEffect(type.getType());
	}
	
	/**
	 * This function sets the player's rank on the tablist to the left of their name
	 * @param p which is Player to set rank on tablist
	 */
	@SuppressWarnings("deprecation")
	public void setPlayerOnTablist(Player p) {
		String rank = main.getRankManager().getRank(p).getTagWithSpace(); //Gets the player's rank
		
		if (rank.length() >= 16) {
			String s = rank.substring(0, 9);
			p.setPlayerListName("" + s + " " + ChatColor.RESET + p.getName());
		} else
			p.setPlayerListName("" + rank + ChatColor.RESET + p.getName());

		if (main.getRankManager().getRank(p) == Rank.DEFAULT)
			p.setPlayerListName("" + rank + ChatColor.GRAY + p.getName());
		
		/*Team captain = c.registerNewTeam("b_captain");
		captain.setPrefix(Rank.CAPTAIN.getTagWithSpace());
		Team owner = c.registerNewTeam("a_owner");
		owner.setPrefix(Rank.OWNER.getTagWithSpace());
		
		if (main.getRankManager().getRank(p) == Rank.CAPTAIN)
			captain.addPlayer(p);
		else if (main.getRankManager().getRank(p) == Rank.OWNER)
			owner.addPlayer(p);
		
		p.setScoreboard(c);
		
		if (main.getTabManager() != null)
			main.getTabManager().setPlayerTeam(p);*/
	}
	
	/**
	 * This function checks if tournament mode is active on Player Join
	 * @param p which is Player to add to the tournament
	 */
	public void checkIfTournament(Player p) {
		if (main.tournament) {
			PlayerData data = main.getDataManager().getPlayerData(p);
			if (main.tourneyreset) {
				if (!main.tourney.containsKey(p.getName()))
					data.points = 0;
				else
					data.points = main.tourney.get(p.getName());
			}
			main.tourney.put(p.getName(), data.points);
		}
	}

	//EVENTS:
	
	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().teleport(main.GetHubLoc());
		for (int i = 0; i < 9; i++) {
			event.getPlayer().getInventory().setItem(i, new ItemStack(Material.WOOD_SWORD));
		}
		Bukkit.getScheduler().runTaskLater(main, () -> {
			event.getPlayer().getInventory().clear();
			main.ResetPlayer(event.getPlayer());
		}, 20);
	}

	@EventHandler
	public void OnPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);
		//anthony.CrystalWars.game.GameInstance i = main.getCwManager().getInstanceOfPlayer(player);
		//anthony.skywars.GameInstance i2 = main.getSWManager().getInstanceOfPlayer(player);

		if (instance != null)
			main.getGameManager().RemovePlayerFromAll(player);
		//else if (i != null)
			//main.getCwManager().removePlayer(player);
		//else if (i2 != null)
			//main.getSWManager().removePlayer(player);
	}

	@EventHandler
	public void waterNoFlow(BlockFromToEvent e) {
		if (main.getCommands() != null)
			e.setCancelled(true);
		else
			e.setCancelled(false);
	}
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer();
            Player target = (Player) event.getRightClicked();
            
            if (player != null && target != null) {
            	GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
            	GameInstance spectating = main.getGameManager().GetInstanceOfSpectator(player);
            	
            	if (game != null && game.state == GameState.STARTED)
            		return;
            	if (spectating != null)
            		return;
            	
            	new StatsGUI(main, target).inv.open(player);
            }
        }
    }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		//anthony.CrystalWars.game.GameInstance i = main.getCwManager().getInstanceOfPlayer(player);
		Block b = event.getBlock();

//		if (i != null) {
//			if (i.getState() == GameState.IN_PROGRESS) {
//				if (i.blocksPlaced.contains(b.getLocation().toVector())) {
//					event.setCancelled(false);
//					return;
//				}
//				event.setCancelled(true);
//				player.sendMessage(main.color("&c&l(!) &rYou can only destroy blocks placed by players!"));
//			}
//		} else {
			if (player.isOp())
				event.setCancelled(false);
			else
				event.setCancelled(true);
//		}
//		i = null;
//		anthony.skywars.GameInstance i2 = main.getSWManager().getInstanceOfPlayer(player);
//
//		if (i2 != null) {
//			if (i2.getState() == anthony.skywars.GameState.STARTED) {
//				event.setCancelled(false);
//				return;
//			}
//		}
//		i2 = null;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		/* anthony.CrystalWars.game.GameInstance i = main.getCwManager().getInstanceOfPlayer(player);

		if (i != null) {
			if (i.getState() == GameState.IN_PROGRESS) {
				event.setCancelled(false);
				i.blocksPlaced.add(event.getBlockPlaced().getLocation().toVector());
				return;
			}
		}
		i = null;

		anthony.skywars.GameInstance i2 = main.getSWManager().getInstanceOfPlayer(player);

		if (i2 != null) {
			if (i2.getState() == anthony.skywars.GameState.STARTED) {
				event.setCancelled(false);
				return;
			}
		} */
		
		if (!(player.isOp()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onFall(EntityDamageEvent e) {
		if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
			e.setCancelled(true);
	}
	
	@EventHandler
	public void armorStand(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player)
			if (e.getEntity() instanceof ArmorStand)
				if (((Player) e.getDamager()).getPlayer().getGameMode() != GameMode.CREATIVE)
					e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onClick(InventoryClickEvent event) {
		if (event.getSlotType() == InventoryType.SlotType.ARMOR)
			event.setCancelled(true);
	}

	@EventHandler
	public void tokenClassGUI(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);
		if (e.getItem() != null && e.getItem().getType() == Material.ENCHANTED_BOOK) {
			e.setCancelled(true);
			if (i == null) {
				new ClassSelectorGUI(main).inv.open(player);
			}
		}
	}

	@EventHandler
	public void cosmeticsGUI(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if (e.getItem() != null && e.getItem().getType() == Material.CHEST) {
			if (i != null && i.state == anthony.SuperCraftBrawl.Game.GameState.WAITING)
				new CosmeticsGUI(main).inv.open(player);
			else if (player.getWorld() == main.getLobbyWorld())
				new CosmeticsGUI(main).inv.open(player);
		}
	}

	@EventHandler
	public void prefsGUI(PlayerInteractEvent e) {
		ItemStack item = e.getItem();
		Player player = e.getPlayer();

		if (item != null && item.getType() == Material.REDSTONE_COMPARATOR)
			new PrefsGUI(main).inv.open(player);
	}

	@EventHandler
	public void joinItem(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();

		if (item != null && item.getType() == Material.WATCH)
			new GameSelectorGUI(main).inv.open(player);
	}

	@EventHandler
	public void manipulate(PlayerArmorStandManipulateEvent e) {
		if (!e.getRightClicked().isVisible())
			e.setCancelled(true);
	}

	@EventHandler
	public void someGuis(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();

		if (item != null) {
			if (item.getType() == Material.SKULL_ITEM) {
				if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
					if (item.getItemMeta().getDisplayName().contains("Profile"))
						new StatsGUI(main).inv.open(player);
					else if (item.getItemMeta().getDisplayName().contains("Tournament"))
						new TournamentGUI(main).inv.open(player);
				}
			} else if (item.getType() == Material.NETHER_STAR) {
				if (player.getWorld() == main.getLobbyWorld())
					new ChallengesGUI(main).inv.open(player);
			}
		}
	}

	@EventHandler
	public void containerInteract(PlayerInteractEvent e) {
		List<Material> list = new ArrayList<>(Arrays.asList(Material.FURNACE, Material.HOPPER, Material.ANVIL,
				Material.ENCHANTMENT_TABLE, Material.ANVIL, Material.WORKBENCH, Material.BREWING_STAND,
				Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.BEACON, Material.DISPENSER, Material.DROPPER,
				Material.CHEST));
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && list.contains(e.getClickedBlock().getType())) {
			Player player = e.getPlayer();
			if (!player.isOp())
				e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void playerRightClick(PlayerInteractEntityEvent e) {
		if (main.getGameManager().GetInstanceOfPlayer(e.getPlayer()) == null
				&& e.getRightClicked() instanceof Player) {
			Player target = ((Player) e.getRightClicked()).getPlayer();
			new StatsGUI(main, target).inv.open(e.getPlayer());
		}
	}

	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		// StaffChat
		event.setCancelled(true);
		if (main.staffchat.contains(event.getPlayer())) {
			String tag = main.getRankManager().getRank(event.getPlayer()).getTagWithSpace();
			String message = tag + event.getPlayer().getDisplayName() + ": " + event.getMessage();

			for (Player staff : main.staffchat) {
				TellAll(message);
				return;
			}
		} else {
			// Chat filter
			List<String> filteredWords = new ArrayList<>(Arrays.asList(
					"nibba", "nigga", "niggas", "nigger", "niggers", "porn", "pornhub",
					"cum", "fuck you", "fuckyou", "fuck", "bitch", "pussy", "fucker",
					"motherfucker", "celestepvp", "celeste", "kys", "pu$$y", "fag", "faggot",
					"bitchass", "cunt", "retard", "penis", "fucker", "twat", "cock"));
			PlayerData data = main.getDataManager().getPlayerData(event.getPlayer());
			String tag = main.getRankManager().getRank(event.getPlayer()).getTagWithSpace();
			String message = event.getMessage();
			
//			if (event.getPlayer().hasPermission("scb.chat"))
//				message = "" + ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level + ChatColor.RESET
//						+ ChatColor.YELLOW + "] " + tag + event.getPlayer().getDisplayName() + ChatColor.RESET + ": ";
//			else
//				message = "" + ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level + ChatColor.RESET
//						+ ChatColor.YELLOW + "] " + tag + ChatColor.GRAY + event.getPlayer().getDisplayName() + ": ";
			
			event.setFormat(ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level + ChatColor.RESET
							+ ChatColor.YELLOW + "] " + tag); // This part will always be included
			String displayName = event.getPlayer().getDisplayName(); // Base display name
			
			if (data.blue == 1)
				displayName = ChatColor.BLUE + displayName;
			else if (data.red == 1)
				displayName = ChatColor.RED + displayName;
			else if (data.green == 1)
				displayName = ChatColor.GREEN + displayName;
			else if (data.yellow == 1)
				displayName = ChatColor.YELLOW + displayName;
			
			if (event.getPlayer().hasPermission("scb.chat"))
				event.setFormat(event.getFormat() + displayName + ChatColor.RESET + ": ");
			else {
				displayName = ChatColor.GRAY + displayName;
				event.setFormat(event.getFormat() + displayName + ": ");
			}
			
			String tempmsg = "";
			for (String msgWord : message.split(" ")) { // Loop through each word and check if it is a banned word
				if (filteredWords.contains(msgWord.toLowerCase())) {
					tempmsg += StringUtils.repeat('*', msgWord.length()) + " ";
				} else
					tempmsg += msgWord + " ";
			}
			message = tempmsg.trim();
			event.setMessage(message);

//			if (censored) {
//				if (event.getPlayer().hasPermission("scb.chat"))
//					message = "" + ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level
//							+ ChatColor.RESET + ChatColor.YELLOW + "] " + tag + event.getPlayer().getDisplayName()
//							+ ChatColor.RESET + ": " + msg2;
//				else
//					message = "" + ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level
//							+ ChatColor.RESET + ChatColor.YELLOW + "] " + tag + ChatColor.GRAY
//							+ event.getPlayer().getDisplayName() + ": " + msg2;
//			} else {
//				if (data != null) {
//					if (data.blue == 1) {
//						message = "" + ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level
//								+ ChatColor.RESET + ChatColor.YELLOW + "] " + tag + ChatColor.BLUE
//								+ event.getPlayer().getDisplayName() + ChatColor.RESET + ": ";
//					} else if (data.red == 1) {
//						message = "" + ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level
//								+ ChatColor.RESET + ChatColor.YELLOW + "] " + tag + ChatColor.RED
//								+ event.getPlayer().getDisplayName() + ChatColor.RESET + ": ";
//					} else if (data.green == 1) {
//						message = "" + ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level
//								+ ChatColor.RESET + ChatColor.YELLOW + "] " + tag + ChatColor.GREEN
//								+ event.getPlayer().getDisplayName() + ChatColor.RESET + ": ";
//					} else if (data.yellow == 1) {
//						message = "" + ChatColor.YELLOW + "[" + ChatColor.YELLOW + ChatColor.BOLD + data.level
//								+ ChatColor.RESET + ChatColor.YELLOW + "] " + tag + ChatColor.YELLOW
//								+ event.getPlayer().getDisplayName() + ChatColor.RESET + ": ";
//					}
//				}
//			}
//			//AuthAPI api = AuthAPI.get();
			PunishAPI pu = PunishAPI.get();
			if (pu.isPlayerMuted(event.getPlayer().getUniqueId())) {
				String muteMsg = pu.getMuteMessage(event.getPlayer().getUniqueId());
				event.getPlayer().sendMessage(muteMsg);
				return;
			}
				Bukkit.broadcastMessage(event.getFormat() + event.getMessage());
//			if (censored) {
//				//if (api.isPlayerAuthed(event.getPlayer()))
//					Bukkit.broadcastMessage(message);
//			} else {
//				//if (api.isPlayerAuthed(event.getPlayer()))
//					Bukkit.broadcastMessage(message + event.getMessage());
//			}
		}
	}

	public void TellAll(String message) {
		for (Player staff : main.staffchat)
			staff.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "StaffChat> " + ChatColor.RESET + message);
	}

	/*
	 * @EventHandler public void onInventory(PlayerInteractEntityEvent e) { Player p
	 * = e.getPlayer();
	 * 
	 * System.out.println("Test1"); if (p.getWorld() == main.getLobbyWorld()) {
	 * System.out.println("Test2"); if
	 * (e.getRightClicked().getType().equals(EntityType.PLAYER)) {
	 * System.out.println("Test3"); Player t = (Player) e.getRightClicked();
	 * System.out.println("Test4"); new WagersGUI(main, t).inv.open(p);
	 * System.out.println("Test5"); } } }
	 */

	// COSMETICS:

	@EventHandler
	public void cosmetics(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		PlayerData data = main.getDataManager().getPlayerData(player);
		ItemStack item = event.getItem();
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if (item != null) {
			if (item.getType() == Material.GOLD_BARDING
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if ((player.getWorld() == main.getLobbyWorld())
						|| (i != null && i.state == anthony.SuperCraftBrawl.Game.GameState.WAITING)) {
					
					if (data != null) {
						if (data.paintball > 0) {
							player.launchProjectile(Snowball.class);
							int amt = item.getAmount();
							amt--;
							data.paintball--;
							main.getDataManager().saveData(data);
							item.setAmount(amt);

							for (Player players : Bukkit.getOnlinePlayers())
								players.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
						} else
							player.sendMessage(main.color("&c&l(!) &rYou do not have anymore &ePaintballs &r:("));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onHookHit(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof FishHook)
			event.setCancelled(true);
	}

	@EventHandler
	public void snowballHit(ProjectileHitEvent event) {
		Entity e = event.getEntity();
		Snowball s;

		if (e instanceof Snowball) {
			s = (Snowball) e;
			DyeColor col = DyeColor.values()[new Random().nextInt(DyeColor.values().length)];
			if (s.getShooter() instanceof Player) {
				Player p = (Player) s.getShooter();
				GameInstance i = main.getGameManager().GetInstanceOfPlayer(p);

				if (i != null && i.state == anthony.SuperCraftBrawl.Game.GameState.STARTED)
					return;

				Block center = s.getLocation().getBlock();
				int x = center.getX();
				int z = center.getZ();
				if (center.getType() != Material.AIR) {
					doTheWorkForMe(center, col);

				}

				int max = s.getLocation().getBlock().getY() + 1;
				int min = s.getLocation().getBlock().getY() - 1;
				Location loc;
				for (int y = min; y <= max; y++) {
					loc = new Location(center.getWorld(), x + 1, y, z);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x, y, z);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x + 1, y, z + 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x, y, z + 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x - 1, y, z + 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x - 1, y, z);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x - 1, y, z);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x - 1, y, z - 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x, y, z - 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x + 1, y, z - 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void randomizeColor(Block block, DyeColor color) {
		block.setData(color.getData());
	}

	@SuppressWarnings("deprecation")
	private void doTheWorkForMe(Block block, DyeColor color) {
		if (block.getType() != Material.AIR && block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST
				&& block.getType() != Material.WALL_SIGN && block.getType() != Material.WOOL
				&& block.getType() != Material.CHEST && block.getType() != Material.LONG_GRASS
				&& block.getType() != Material.RED_ROSE && block.getType() != Material.DEAD_BUSH
				&& block.getType() != Material.FLOWER_POT && block.getType() != Material.DOUBLE_PLANT
				&& block.getType() != Material.BED_BLOCK && !(block.getState().getData() instanceof Door)
				&& !(block.getState() instanceof InventoryHolder) && !(block.getState() instanceof Banner)
				&& block.getType() != Material.SKULL && block.getType() != Material.SOIL) {
			Material og = block.getType();
			Byte data = block.getData();
			if (og == Material.WOOL) {
				randomizeColor(block, color);
				return;
			}
			Location loc = new Location(block.getWorld(), block.getX(), block.getY() + 1, block.getZ());

			if (loc.getBlock().getType().isSolid() == false && loc.getBlock().getType() != Material.AIR
					&& loc.getBlock().getType() != Material.TORCH)
				return;

			Bukkit.getScheduler().runTaskLater(main, () -> {
				block.setType(og);
				block.setData(data);
			}, 20 * 5L);
			block.setType(Material.WOOL);
			randomizeColor(block, color);
		}
	}

	public void trampoline(Player player) {
		Location loc = player.getLocation();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if (i != null || player.getWorld() != main.getLobbyWorld()) {
			player.sendMessage(main.color("&c&l(!) &rYou can only use this Cosmetic in spawn!"));
			return;
		}

		loc.setY(y - 1);
		loc = new Location(player.getWorld(), x + 1, y, z);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x, y, z);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x + 1, y, z + 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x, y, z + 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x - 1, y, z + 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x - 1, y, z);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x - 1, y, z);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x - 1, y, z - 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x, y, z - 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x + 1, y, z - 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
	}

	private void doStuff(Block block, Player player) {
		if (block.getType() != Material.AIR && block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST
				&& block.getType() != Material.WALL_SIGN && block.getType() != Material.WOOL
				&& block.getType() != Material.CHEST && block.getType() != Material.LONG_GRASS
				&& block.getType() != Material.RED_ROSE && block.getType() != Material.DEAD_BUSH
				&& block.getType() != Material.FLOWER_POT) {
			Material og = block.getType();
			Byte data = block.getData();

			Bukkit.getScheduler().runTaskLater(main, () -> {
				block.setType(og);
				block.setData(data);
			}, 20 * 5L);
			block.setType(Material.SLIME_BLOCK);
		} else {
			player.sendMessage(main.color("&c&l(!) &rYou need to be in an open area to use this!"));
			return;
		}
	}
}
