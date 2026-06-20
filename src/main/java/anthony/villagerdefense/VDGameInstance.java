package anthony.villagerdefense;

import anthony.villagerdefense.defense.VDDefenseManager;
import anthony.villagerdefense.map.VDMapConfig;
import anthony.villagerdefense.map.VDTeamSite;
import anthony.villagerdefense.resources.VDResourceGenerator;
import anthony.villagerdefense.shop.VDTeamUpgradeManager;
import anthony.villagerdefense.villager.VDVillagerManager;
import anthony.villagerdefense.villager.VillagerLevel;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One VillagerDefense match: lobby -> countdown -> in-progress -> reset.
 * Only one of these exists at a time (see VDGameManager).
 */
public class VDGameInstance {

	private final VDGameManager manager;
	private final World world;
	private final Map<Integer, VDTeam> teams = new LinkedHashMap<>();
	private final List<Player> spectators = new ArrayList<>();
	private final List<BlockState> undoLog = new ArrayList<>();
	private final Set<Block> placedBlocks = new HashSet<>();
	private final VDVillagerManager villagerManager;
	private final VDResourceGenerator resourceGenerator;
	private final VDDefenseManager defenseManager;
	private final VDScoreboardManager scoreboardManager;
	private final VDTeamUpgradeManager teamUpgradeManager;

	private VDGameState state = VDGameState.WAITING;
	private BukkitTask countdownTask;

	public VDGameInstance(VDGameManager manager) {
		this.manager = manager;
		this.world = manager.getArenaWorld();
		this.villagerManager = new VDVillagerManager(this);
		this.resourceGenerator = new VDResourceGenerator(this);
		this.defenseManager = new VDDefenseManager(this);
		this.scoreboardManager = new VDScoreboardManager(this);
		this.teamUpgradeManager = new VDTeamUpgradeManager(this);

		for (int siteId = 1; siteId <= VDMapConfig.TEAM_COUNT; siteId++) {
			teams.put(siteId, new VDTeam(
					siteId,
					VDGameConstants.TEAM_NAMES[siteId - 1],
					VDGameConstants.TEAM_COLORS[siteId - 1],
					VDMapConfig.TEAM_CAPACITY
			));
		}
	}

	public VDGameManager getManager() {
		return manager;
	}

	public VDVillagerManager getVillagerManager() {
		return villagerManager;
	}

	public VDDefenseManager getDefenseManager() {
		return defenseManager;
	}

	public VDScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public VDTeamUpgradeManager getTeamUpgradeManager() {
		return teamUpgradeManager;
	}

	/** Refreshes every participant's in-progress scoreboard; called whenever team status changes. */
	public void refreshScoreboards() {
		if (state == VDGameState.IN_PROGRESS) {
			scoreboardManager.refreshAllGame();
		}
	}

	public World getWorld() {
		return world;
	}

	public VDGameState getState() {
		return state;
	}

	public Map<Integer, VDTeam> getTeams() {
		return teams;
	}

	public List<Player> getSpectators() {
		return spectators;
	}

	public boolean hasPlayer(Player player) {
		return findTeamOf(player) != null;
	}

	public boolean hasSpectator(Player player) {
		return spectators.contains(player);
	}

	public VDTeam findTeamOf(Player player) {
		for (VDTeam team : teams.values()) {
			if (team.isMember(player)) return team;
		}
		return null;
	}

	/** Tracks a block's pre-change state during a match so it can be restored on reset. */
	public void recordBlockChange(BlockState before) {
		undoLog.add(before);
	}

	/** Marks a block as player-placed - only these can be broken back (see VDPlayerListener#onBlockBreak). */
	public void markPlaced(Block block) {
		placedBlocks.add(block);
	}

	public boolean isPlacedByPlayer(Block block) {
		return placedBlocks.contains(block);
	}

	public void unmarkPlaced(Block block) {
		placedBlocks.remove(block);
	}

	/**
	 * Used by RepairGolemMechanic: restores up to maxBlocks distinct locations
	 * near center to their earliest recorded state. Doesn't remove anything from
	 * the undo log, so the final end-of-match restoreWorld() pass still wins
	 * regardless of what's repaired mid-match.
	 */
	public void repairNear(Location center, double radius, int maxBlocks) {
		Map<String, BlockState> earliestByLocation = new LinkedHashMap<>();

		for (BlockState saved : undoLog) {
			if (!saved.getWorld().equals(center.getWorld())) continue;
			if (saved.getLocation().distanceSquared(center) > radius * radius) continue;

			String key = saved.getX() + "," + saved.getY() + "," + saved.getZ();
			earliestByLocation.putIfAbsent(key, saved);
		}

		int repaired = 0;
		for (BlockState saved : earliestByLocation.values()) {
			if (repaired >= maxBlocks) break;
			saved.update(true, false);
			placedBlocks.remove(saved.getBlock()); // repaired back to original map terrain, not a player placement anymore
			repaired++;
		}
	}

	public boolean addPlayer(Player player) {
		if (state != VDGameState.WAITING && state != VDGameState.STARTING) {
			player.sendMessage(manager.getMain().color("&c&l(!) &rA VillagerDefense match is already in progress!"));
			return false;
		}

		VDTeam freeTeam = teams.values().stream().filter(VDTeam::hasSpace).findFirst().orElse(null);
		if (freeTeam == null) {
			player.sendMessage(manager.getMain().color("&c&l(!) &rVillagerDefense is full!"));
			return false;
		}

		freeTeam.addMember(player.getUniqueId());
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.teleport(manager.getMapConfigManager().getConfig().getLobby());

		player.sendMessage(manager.getMain().color("&2&l(!) &rYou joined VillagerDefense as Team &e"
				+ freeTeam.getName()));
		TellAll(manager.getMain().color("&2&l(!) &e" + player.getName() + " &rjoined! ("
				+ countPlayers() + "/" + (VDMapConfig.TEAM_COUNT * VDMapConfig.TEAM_CAPACITY) + ")"));

		checkForStart();
		scoreboardManager.refreshAllWaiting(waitingStatusLine());
		return true;
	}

	public void removePlayer(Player player) {
		VDTeam team = findTeamOf(player);
		if (team == null) return;

		team.removeMember(player.getUniqueId());
		scoreboardManager.remove(player);

		if (state == VDGameState.WAITING || state == VDGameState.STARTING) {
			TellAll(manager.getMain().color("&c&l(!) &e" + player.getName() + " &rleft VillagerDefense."));

			if (state == VDGameState.STARTING && countPlayers() < VDGameConstants.MIN_PLAYERS) {
				cancelCountdown();
			}

			if (countPlayers() == 0) {
				manager.clearActiveInstance(this);
			} else {
				scoreboardManager.refreshAllWaiting(waitingStatusLine());
			}
		} else if (state == VDGameState.IN_PROGRESS) {
			if (team.isEmpty()) {
				team.setEliminated(true);
				TellAll(manager.getMain().color("&c&l(!) &e" + player.getName()
						+ "'s &rteam has been eliminated!"));
			}
			checkForWin();
			refreshScoreboards();
		}

		manager.getMain().ResetPlayer(player);
	}

	private String waitingStatusLine() {
		return state == VDGameState.STARTING ? "&aStarting soon..." : "&7Waiting for players...";
	}

	public void addSpectator(Player player) {
		spectators.add(player);
		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(manager.getMapConfigManager().getConfig().getLobby());

		if (state == VDGameState.IN_PROGRESS) {
			scoreboardManager.showGame(player);
		} else {
			scoreboardManager.showWaiting(player, waitingStatusLine());
		}
	}

	public void removeSpectator(Player player) {
		spectators.remove(player);
		scoreboardManager.remove(player);
		manager.getMain().ResetPlayer(player);
	}

	int countPlayers() {
		return teams.values().stream().mapToInt(t -> t.getMembers().size()).sum();
	}

	/** All currently-online team members and spectators; used to refresh scoreboards. */
	List<Player> onlineParticipants() {
		List<Player> online = new ArrayList<>();
		for (VDTeam team : teams.values()) {
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) online.add(p);
			}
		}
		online.addAll(spectators);
		return online;
	}

	private void checkForStart() {
		if (state == VDGameState.WAITING && countPlayers() >= VDGameConstants.MIN_PLAYERS) {
			startCountdown();
		}
	}

	private void startCountdown() {
		state = VDGameState.STARTING;

		countdownTask = new org.bukkit.scheduler.BukkitRunnable() {
			int secondsLeft = VDGameConstants.STARTING_COUNTDOWN_SECONDS;

			@Override
			public void run() {
				if (secondsLeft <= 0) {
					countdownTask = null;
					this.cancel();
					startMatch();
					return;
				}

				if (secondsLeft <= 5 || secondsLeft % 10 == 0) {
					TellAll(manager.getMain().color("&2&l(!) &rGame starting in &e" + secondsLeft + "s"));
					playSoundForAll(Sound.NOTE_PLING, 1f, secondsLeft <= 5 ? 2f : 1f);
				}

				scoreboardManager.refreshAllWaiting("&aStarting in &e" + secondsLeft + "s");
				secondsLeft--;
			}
		}.runTaskTimer(manager.getMain(), 0L, 20L);
	}

	private void cancelCountdown() {
		if (countdownTask != null) {
			countdownTask.cancel();
			countdownTask = null;
		}
		state = VDGameState.WAITING;
		TellAll(manager.getMain().color("&c&l(!) &rNot enough players, countdown cancelled."));
		scoreboardManager.refreshAllWaiting(waitingStatusLine());
	}

	private void startMatch() {
		state = VDGameState.IN_PROGRESS;
		VDMapConfig config = manager.getMapConfigManager().getConfig();

		for (VDTeam team : teams.values()) {
			if (team.isEmpty()) continue;

			VDTeamSite site = config.getTeamSite(team.getSiteId());
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p == null) continue;

				p.teleport(site.getSpawn());
				p.setGameMode(GameMode.SURVIVAL);
				p.setHealth(20.0);
				p.setFoodLevel(20);
				giveStarterKit(p, team);
			}
		}

		villagerManager.spawnAll();
		resourceGenerator.start();
		teamUpgradeManager.start();
		scoreboardManager.refreshAllGame();

		TellAll(manager.getMain().color("&a&l(!) &rVillagerDefense has started! Protect your villager!"));
		playSoundForAll(Sound.ENDERDRAGON_GROWL, 0.5f, 1.5f);
	}

	/** Level 1 kit per the design doc: blocks, basic weapons, tools, simple food. */
	public void giveStarterKit(Player player, VDTeam team) {
		player.getInventory().clear();
		player.getInventory().addItem(teamUpgradeManager.applyUpgrades(team, new ItemStack(Material.WOOD_SWORD)));
		player.getInventory().addItem(new ItemStack(Material.WOOD_PICKAXE));
		player.getInventory().addItem(new ItemStack(Material.WOOD_AXE));
		player.getInventory().addItem(new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()));
		player.getInventory().addItem(new ItemStack(Material.BREAD, 5));
	}

	private static final int RESPAWN_SECONDS = 3;

	/**
	 * Called when a player's fatal damage (PvP, mob, void, etc.) was intercepted
	 * during IN_PROGRESS. Drops them into a 3-second spectating respawn if their
	 * villager is still alive (bed-equivalent); otherwise eliminates their team.
	 */
	public void killPlayer(Player player, Player killer) {
		String message = killer != null
				? "&e" + player.getName() + " &rwas killed by &e" + killer.getName() + "&r!"
				: "&e" + player.getName() + " &rdied!";
		handleDeath(player, message);
	}

	public void killPlayerInVoid(Player player) {
		handleDeath(player, "&e" + player.getName() + " &rfell into the void!");
	}

	private void handleDeath(Player player, String coloredMessage) {
		VDTeam team = findTeamOf(player);
		if (team == null) return;

		TellAll(manager.getMain().color("&c&l(!) &r" + coloredMessage));

		if (!team.isVillagerAlive()) {
			eliminateTeam(team, player);
			return;
		}

		beginRespawn(player, team);
	}

	private void beginRespawn(Player player, VDTeam team) {
		UUID uuid = player.getUniqueId();
		player.setGameMode(GameMode.SPECTATOR);

		new BukkitRunnable() {
			int secondsLeft = RESPAWN_SECONDS;

			@Override
			public void run() {
				Player p = Bukkit.getPlayer(uuid);
				if (p == null || state != VDGameState.IN_PROGRESS) {
					cancel();
					return;
				}

				if (secondsLeft <= 0) {
					cancel();
					respawn(p, team);
					return;
				}

				p.sendTitle(manager.getMain().color("&c&lYOU DIED"),
						manager.getMain().color("&eRespawning in &c" + secondsLeft + "&e..."));
				secondsLeft--;
			}
		}.runTaskTimer(manager.getMain(), 0L, 20L);
	}

	private void respawn(Player player, VDTeam team) {
		if (!team.isMember(player)) {
			return; // they already left the game through other means (e.g. /vd leave) while respawning
		}

		if (!team.isVillagerAlive() || team.isEliminated()) {
			eliminateTeam(team, player);
			return;
		}

		VDTeamSite site = manager.getMapConfigManager().getConfig().getTeamSite(team.getSiteId());
		player.setGameMode(GameMode.SURVIVAL);
		player.teleport(site.getSpawn());
		player.setHealth(20.0);
		player.setFoodLevel(20);
		giveStarterKit(player, team);
		player.sendTitle(manager.getMain().color("&a&lRESPAWNED"), "");
		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
	}

	private void eliminateTeam(VDTeam team, Player player) {
		team.setEliminated(true);

		if (player.isOnline()) {
			player.setGameMode(GameMode.SPECTATOR);
			player.teleport(manager.getMapConfigManager().getConfig().getLobby());
		}

		TellAll(manager.getMain().color("&4&l(!) &eTeam " + team.getName() + " &rhas been eliminated!"));
		playSoundForAll(Sound.WITHER_SPAWN, 0.4f, 1f);
		checkForWin();
		refreshScoreboards();
	}

	private void checkForWin() {
		List<VDTeam> remaining = new ArrayList<>();
		for (VDTeam team : teams.values()) {
			if (!team.isEliminated() && !team.isEmpty()) {
				remaining.add(team);
			}
		}

		if (remaining.size() <= 1) {
			endMatch(remaining.isEmpty() ? null : remaining.get(0));
		}
	}

	private void endMatch(VDTeam winner) {
		state = VDGameState.ENDING;

		if (winner != null) {
			TellAll(manager.getMain().color("&6&l(!) &eTeam " + winner.getName() + " &rhas won VillagerDefense!"));
			playSoundForAll(Sound.LEVEL_UP, 1f, 0.7f);
			for (UUID uuid : winner.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) {
					p.sendTitle(manager.getMain().color("&6&lVICTORY"), manager.getMain().color("&eYou won the game!"));
				}
			}
		} else {
			TellAll(manager.getMain().color("&6&l(!) &rVillagerDefense ended with no winner."));
		}

		Bukkit.getScheduler().runTaskLater(manager.getMain(), this::resetAndClear, 5L * 20L);
	}

	private void resetAndClear() {
		resourceGenerator.stop();
		villagerManager.despawnAll();
		defenseManager.stopAll();
		teamUpgradeManager.stop();
		scoreboardManager.removeAll();
		restoreWorld();

		for (VDTeam team : teams.values()) {
			team.setVillagerAlive(true);
			team.setEliminated(false);
			team.setLevel(VillagerLevel.LEVEL_1);
			team.resetUpgrades();

			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) manager.getMain().ResetPlayer(p);
			}
			team.getMembers().clear();
		}

		for (Player spectator : spectators) {
			manager.getMain().ResetPlayer(spectator);
		}
		spectators.clear();

		manager.clearActiveInstance(this);
	}

	private void restoreWorld() {
		placedBlocks.clear();
		for (int i = undoLog.size() - 1; i >= 0; i--) {
			undoLog.get(i).update(true, false);
		}
		undoLog.clear();
	}

	public void TellAll(String msg) {
		for (VDTeam team : teams.values()) {
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) p.sendMessage(msg);
			}
		}
		for (Player spectator : spectators) {
			spectator.sendMessage(msg);
		}
	}

	/** Plays a sound for every current participant (team members + spectators), Hypixel-style audio cues. */
	public void playSoundForAll(Sound sound, float volume, float pitch) {
		for (Player p : onlineParticipants()) {
			p.playSound(p.getLocation(), sound, volume, pitch);
		}
	}
}
