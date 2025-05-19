package anthony.parkour;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.doublejump.DoubleJumpData;
import anthony.SuperCraftBrawl.doublejump.DoubleJumpManager;
import anthony.SuperCraftBrawl.playerdata.ParkourDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;

public class Parkour implements Listener {

	private Core main;
	public Map<Player, Arenas> players;
	public Map<Player, Integer> checkpoint;
	public Map<Player, FastBoard> b;
	public Map<Player, BukkitRunnable> runnables;
	public Map<Player, Long> startTime;
	private final Map<Player, List<EntityArmorStand>> checkpointHolograms = new HashMap<>();


	public Parkour(Core main) {
		this.main = main;
		this.players = new HashMap<>();
		this.checkpoint = new HashMap<>();
		this.b = new HashMap<>();
		this.startTime = new HashMap<>();
		this.runnables = new HashMap<>();
		this.main.getServer().getPluginManager().registerEvents(this, main);
	}

	public void addPlayer(Player player, Arenas arena) {
		if (!hasPlayer(player)) {
			players.put(player, arena);
			player.sendMessage(main.color("&e&l(!) &rYou have joined &r&l" + arena.getName()));

			player.getInventory().clear();
			gameBoard(player);
			gameItems(player);
			timeTicking(player);
			player.setAllowFlight(false);
			player.closeInventory();

			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}

			addHolograms(player, arena);
		} else {
			player.sendMessage(main.color("&c&l(!) &rYou are already in parkour mode!"));
		}
	}

	public void addHolograms(Player player, Arenas arena) {
		List<EntityArmorStand> stands = new ArrayList<>();
		checkpointHolograms.put(player, stands);

		WorldServer world = ((CraftWorld) main.getLobbyWorld()).getHandle();

		int i = 1;
		for (Location l : arena.getInstance().checkpoints) {
			Location loc = new Location(main.getLobbyWorld(), l.getX() + 0.5, l.getY() - 0.75, l.getZ() + 0.5);

			EntityArmorStand stand = new EntityArmorStand(world);
			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName(main.color("&e&lCheckpoint &b&l#" + i));
			stand.setCustomNameVisible(true);
			stand.setInvisible(true);
			stand.setGravity(false);

			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

			stands.add(stand);
			i++;
		}
	}

	public void removeHolograms(Player player, Arenas arena) {
		List<EntityArmorStand> stands = checkpointHolograms.remove(player);
		if (stands != null) {
			for (EntityArmorStand stand : stands) {
				PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(stand.getId());
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroy);
			}
		}
	}

	private void timeTicking(Player player) {
		long start = System.nanoTime();
		startTime.put(player, start);

		BukkitRunnable r = new BukkitRunnable() {
			@Override
			public void run() {
				if (runnables.get(player) != null) {
					long currentTime = System.nanoTime();
					long elapsedMillis = currentTime - startTime.get(player);

					String formattedTime = formatTimeScoreboard(elapsedMillis);

					b.get(player).updateLine(3, main.color("&r&lTime:&7 " + formattedTime));
				} else {
					this.cancel();
					runnables.remove(player);
				}
			}
		};

		r.runTaskTimer(main, 0, 2); // runs every 0.1 seconds
		runnables.put(player, r);
	}

	public void gameBoard(Player player) {
		FastBoard b = new FastBoard(player);
		b.updateTitle(main.color("&e&lPARKOUR"));
		b.updateLines("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
				main.color(
						"&r&lCheckpoints: &7" + (checkpoint.containsKey(player) ? checkpoint.get(player) + 1 : 0) +
								"/" + players.get(player).getCheckpoints()),
				"", main.color("&r&lTime:&7 0.0s"),
				"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
				main.color("&7minezone.club"));
		this.b.put(player, b);
	}

	public void gameItems(Player player) {
		player.getInventory().setItem(0,
				ItemHelper.setDetails(new ItemStack(Material.BEACON), main.color("&bReturn to Checkpoint")));
		player.getInventory().setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.SEA_LANTERN), main.color("&bRestart")));
		player.getInventory().setItem(2, ItemHelper.setDetails(new ItemStack(Material.BARRIER), main.color("&cLeave")));
	}

	public boolean hasPlayer(Player player) {
		return players.containsKey(player);
	}

	// Events

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (player.getWorld() == main.getLobbyWorld()) {
			if (event.getTo() == null || event.getFrom() == null
					|| event.getTo().toVector().toBlockVector().equals(event.getFrom().toVector().toBlockVector()))
				return;

			if (hasPlayer(player)) {
				ArenaInstance arenaInstance = players.get(player).getInstance();
				if (event.getTo().getY() < 50)
					teleportToCheckpoint(player);

				if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BEACON) {
					BlockVector blockVector = event.getTo().getBlock().getLocation().toVector().toBlockVector();

					if (arenaInstance.checkpointBlocks.contains(blockVector)) { // Check if checkpoint exists
						int newCheckpointIndex = arenaInstance.checkpointBlocks.indexOf(blockVector);
						Integer currentCheckpointIndex = checkpoint.get(player);

						if (currentCheckpointIndex == null && newCheckpointIndex == 0 ||
								currentCheckpointIndex != null && newCheckpointIndex != currentCheckpointIndex
										&& newCheckpointIndex == currentCheckpointIndex + 1) {

							checkpoint.put(player, newCheckpointIndex);
							b.get(player).updateLine(1, main.color("&r&lCheckpoints: &7" + (newCheckpointIndex + 1) + "/"
									+ players.get(player).getCheckpoints()));
							player.sendMessage(main.color("&e&l(!) &rYou reached checkpoint #" + (newCheckpointIndex + 1) + "!"));
						}
					}
				} else if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GLOWSTONE) {
					for (Arenas arena : Arenas.values()) {
						if (event.getTo().toVector().toBlockVector().equals(arena.getInstance().endLoc.toVector().toBlockVector())
								&& !event.getFrom().toVector().toBlockVector().equals(arena.getInstance().endLoc.toVector().toBlockVector())) {
							if (checkpoint.containsKey(player) && checkpoint.get(player) == arena.getCheckpoints() - 1) {

								long endTime = System.nanoTime();
								long start = startTime.getOrDefault(player, endTime);
								long totalTime = endTime - start;

								player.sendTitle(main.color("&aPARKOUR COMPLETE!"), null);

								PlayerData data = main.getDataManager().getPlayerData(player);
								int arenaID = players.get(player).getId();
								ParkourDetails details = data.playerParkour.get(arenaID);
								if (details == null) {
									details = new ParkourDetails();
									data.playerParkour.put(arenaID, details);

									player.sendMessage(
											main.color("&d&l(!) &rYou have earned &e" + arenaInstance.tokenReward + "Tokens &rfor clearing this parkour for the first time!"));
									data.tokens += arenaInstance.tokenReward;
								}
								if (details.totalTime == 0 || totalTime < details.totalTime) {
									details.completeParkour(totalTime);
									main.getDataManager().saveData(data);
									player.sendMessage(main.color("&e&l(!) &rParkour completed in &a" + formatTime(totalTime) +
											"&r! &e&lNEW RECORD"));
								} else {
									player.sendMessage(main.color("&e&l(!) &rParkour completed in &e" + formatTime(totalTime) + "&r!"));
									player.sendMessage(main.color("&e&l(!) &rYou did not beat your record of &a" +
											formatTime(details.totalTime)));
								}
								sendTeleportMessage(player, arenaID);

								removePlayer(player);
							} else {
								player.sendMessage(main.color("&c&l(!) &rYou must reach every checkpoint before completing the parkour!"));
							}
							return;
						}
					}
				} else if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SEA_LANTERN) {
					if (event.getTo().toVector().toBlockVector().equals(
							players.get(player).getInstance().startLoc.toVector().toBlockVector())) {
						checkpoint.remove(player);

						runnables.get(player).cancel();
						runnables.remove(player);

						timeTicking(player);

						gameBoard(player);
						player.sendMessage(main.color("&e&l(!) &rReset your time"));
					}
				}
			} else {
				if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SEA_LANTERN) {
					for (Arenas arena : Arenas.values()) {
						if (event.getTo().toVector().toBlockVector().equals(
								arena.getInstance().startLoc.toVector().toBlockVector())) {
							this.addPlayer(player, arena);
							return;
						}
					}
				}
			}
		}
	}

	public void removePlayer(Player player) {
		removeHolograms(player, players.get(player));

		this.startTime.remove(player);
		this.runnables.remove(player);
		this.checkpoint.remove(player);
		main.getParkour().players.remove(player);
		main.getScoreboardManager().lobbyBoard(player);
		player.getInventory().clear();
		main.LobbyItems(player);
		player.sendMessage(main.color("&r&l(!) &rYou have left parkour mode"));
		player.setAllowFlight(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Material item = event.getMaterial();

		if (hasPlayer(player)) {
			if (item != null) {
				switch (item) {
					case BEACON:
						teleportToCheckpoint(player);
						player.sendMessage(main.color("&e&l(!) &rSent back to checkpoint"));
						break;
					case SEA_LANTERN:
						teleportToStart(player);
						checkpoint.remove(player);

						runnables.get(player).cancel();
						runnables.remove(player);

						timeTicking(player);

						gameBoard(player);
						player.sendMessage(main.color("&e&l(!) &rSent back to start"));
						break;
					case BARRIER:
						removePlayer(player);
				}
			}
		}
	}

	public void teleportToStart(Player player) {
		Location loc = players.get(player).getInstance().startLoc;
		player.teleport(loc);
		player.setFireTicks(0);
	}

	public void teleportToCheckpoint(Player player) {
		if (checkpoint.containsKey(player)) {
			Location loc = players.get(player).getCheckpoint(checkpoint.get(player));
			player.teleport(loc);
			player.setFireTicks(0);
		} else {
			teleportToStart(player);
		}
	}

	public String formatTime(long nanoseconds) {
		double totalSeconds = nanoseconds / 1_000_000_000.0;
		long minutes = (long) (totalSeconds / 60);
		double seconds = totalSeconds % 60;

		if (minutes > 0) {
			return String.format("%dm %.3fs", minutes, seconds);
		} else {
			return String.format("%.3fs", seconds);
		}
	}

	public String formatTimeScoreboard(long nanoseconds) {
		double totalSeconds = nanoseconds / 1_000_000_000.0;
		long minutes = (long) (totalSeconds / 60);
		double seconds = totalSeconds % 60;

		if (minutes > 0) {
			return String.format("%dm %.1fs", minutes, seconds);
		} else {
			return String.format("%.1fs", seconds);
		}
	}

	public void sendTeleportMessage(Player player, int id) {
		TextComponent message = new TextComponent(main.color("&a&lClick to try again"));
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/_teleportstart " + id));

		player.spigot().sendMessage(message);
	}

	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage();
		Player player = event.getPlayer();

		if (msg.startsWith("/_teleportstart ")) {
			event.setCancelled(true); // prevent command from reaching server

			if (!players.containsKey(player)) {
				String[] parts = msg.split(" ");
				if (parts.length < 2) {
					return;
				}

				try {
					int id = Integer.parseInt(parts[1]);

					Arenas arena = Arenas.getById(id);
					if (arena != null) {
						Location targetLocation = arena.getInstance().startLoc;
						if (targetLocation == null) {
							return;
						}
						player.teleport(targetLocation);
						addPlayer(player, arena);
					}

				} catch (NumberFormatException ignored) {
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou are already in parkour mode!"));
			}
		}
	}
}
