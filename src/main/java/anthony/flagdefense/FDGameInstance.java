package anthony.flagdefense;

import anthony.SuperCraftBrawl.Game.WinEffects;
import anthony.flagdefense.defense.FDDefenseManager;
import anthony.flagdefense.flag.FDFlagManager;
import anthony.flagdefense.flag.TeamLevel;
import anthony.flagdefense.map.FDMapConfig;
import anthony.flagdefense.map.FDTeamSite;
import anthony.flagdefense.resources.FDResourceGenerator;
import anthony.flagdefense.shop.FDTeamUpgradeManager;
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
 * One FlagDefense match: lobby -> countdown -> in-progress -> reset.
 * Only one of these exists at a time (see FDGameManager).
 */
public class FDGameInstance {

	private final FDGameManager manager;
	private final World world;
	private final Map<Integer, FDTeam> teams = new LinkedHashMap<>();
	private final List<Player> spectators = new ArrayList<>();
	private final List<BlockState> undoLog = new ArrayList<>();
	private final Set<Block> placedBlocks = new HashSet<>();
	private final FDFlagManager flagManager;
	private final FDResourceGenerator resourceGenerator;
	private final FDDefenseManager defenseManager;
	private final FDScoreboardManager scoreboardManager;
	private final FDTeamUpgradeManager teamUpgradeManager;

	private FDGameState state = FDGameState.WAITING;
	private BukkitTask countdownTask;
	private int requiredCaptures = FDGameConstants.MAX_WINNING_FLAG_CAPTURES;
	private final List<WinEffects> activeWinEffects = new ArrayList<>();

	public FDGameInstance(FDGameManager manager) {
		this.manager = manager;
		this.world = manager.getArenaWorld();
		this.flagManager = new FDFlagManager(this);
		this.resourceGenerator = new FDResourceGenerator(this);
		this.defenseManager = new FDDefenseManager(this);
		this.scoreboardManager = new FDScoreboardManager(this);
		this.teamUpgradeManager = new FDTeamUpgradeManager(this);

		for (int siteId = 1; siteId <= FDMapConfig.TEAM_COUNT; siteId++) {
			teams.put(siteId, new FDTeam(
					siteId,
					FDGameConstants.TEAM_NAMES[siteId - 1],
					FDGameConstants.TEAM_COLORS[siteId - 1],
					FDMapConfig.TEAM_CAPACITY
			));
		}
	}

	public FDGameManager getManager() {
		return manager;
	}

	public FDFlagManager getFlagManager() {
		return flagManager;
	}

	public FDDefenseManager getDefenseManager() {
		return defenseManager;
	}

	public FDScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public FDTeamUpgradeManager getTeamUpgradeManager() {
		return teamUpgradeManager;
	}

	/** Refreshes every participant's in-progress scoreboard; called whenever team status changes. */
	public void refreshScoreboards() {
		if (state == FDGameState.IN_PROGRESS) {
			scoreboardManager.refreshAllGame();
		}
	}

	public World getWorld() {
		return world;
	}

	public FDGameState getState() {
		return state;
	}

	/** How many flags the winning team needs this match - scaled down for small player counts, see FDGameConstants#requiredCapturesFor. */
	public int getRequiredCaptures() {
		return requiredCaptures;
	}

	public Map<Integer, FDTeam> getTeams() {
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

	public FDTeam findTeamOf(Player player) {
		for (FDTeam team : teams.values()) {
			if (team.isMember(player)) return team;
		}
		return null;
	}

	/** Tracks a block's pre-change state during a match so it can be restored on reset. */
	public void recordBlockChange(BlockState before) {
		undoLog.add(before);
	}

	/** Marks a block as player-placed - only these can be broken back (see FDPlayerListener#onBlockBreak). */
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
		if (state != FDGameState.WAITING && state != FDGameState.STARTING) {
			player.sendMessage(manager.getMain().color("&c&l(!) &rA FlagDefense match is already in progress!"));
			return false;
		}

		FDTeam freeTeam = teams.values().stream().filter(FDTeam::hasSpace).findFirst().orElse(null);
		if (freeTeam == null) {
			player.sendMessage(manager.getMain().color("&c&l(!) &rFlagDefense is full!"));
			return false;
		}

		freeTeam.addMember(player.getUniqueId());
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.teleport(manager.getMapConfigManager().getConfig().getLobby());

		player.sendMessage(manager.getMain().color("&2&l(!) &rYou joined FlagDefense as Team &e"
				+ freeTeam.getName()));
		TellAll(manager.getMain().color("&2&l(!) &e" + player.getName() + " &rjoined! ("
				+ countPlayers() + "/" + (FDMapConfig.TEAM_COUNT * FDMapConfig.TEAM_CAPACITY) + ")"));

		checkForStart();
		scoreboardManager.refreshAllWaiting(waitingStatusLine());
		return true;
	}

	public void removePlayer(Player player) {
		FDTeam team = findTeamOf(player);
		if (team == null) return;

		team.removeMember(player.getUniqueId());
		scoreboardManager.remove(player);

		if (state == FDGameState.WAITING || state == FDGameState.STARTING) {
			TellAll(manager.getMain().color("&c&l(!) &e" + player.getName() + " &rleft FlagDefense."));

			if (state == FDGameState.STARTING && countPlayers() < FDGameConstants.MIN_PLAYERS) {
				cancelCountdown();
			}

			if (countPlayers() == 0) {
				manager.clearActiveInstance(this);
			} else {
				scoreboardManager.refreshAllWaiting(waitingStatusLine());
			}
		} else if (state == FDGameState.IN_PROGRESS) {
			flagManager.returnCarriedFlag(player, null);

			if (team.isEmpty()) {
				team.setInactive(true);
				TellAll(manager.getMain().color("&c&l(!) &e" + player.getName()
						+ "'s &rbase has been vacated!"));
			}
			checkForWin();
			refreshScoreboards();
		}

		manager.getMain().ResetPlayer(player);
	}

	private String waitingStatusLine() {
		return state == FDGameState.STARTING ? "&aStarting soon..." : "&7Waiting for players...";
	}

	public void addSpectator(Player player) {
		spectators.add(player);
		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(manager.getMapConfigManager().getConfig().getLobby());

		if (state == FDGameState.IN_PROGRESS) {
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
		for (FDTeam team : teams.values()) {
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) online.add(p);
			}
		}
		online.addAll(spectators);
		return online;
	}

	private void checkForStart() {
		if (state == FDGameState.WAITING && countPlayers() >= FDGameConstants.MIN_PLAYERS) {
			startCountdown();
		}
	}

	private void startCountdown() {
		state = FDGameState.STARTING;

		countdownTask = new org.bukkit.scheduler.BukkitRunnable() {
			int secondsLeft = FDGameConstants.STARTING_COUNTDOWN_SECONDS;

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
		state = FDGameState.WAITING;
		TellAll(manager.getMain().color("&c&l(!) &rNot enough players, countdown cancelled."));
		scoreboardManager.refreshAllWaiting(waitingStatusLine());
	}

	private void startMatch() {
		state = FDGameState.IN_PROGRESS;
		requiredCaptures = FDGameConstants.requiredCapturesFor(countPlayers());
		FDMapConfig config = manager.getMapConfigManager().getConfig();

		for (FDTeam team : teams.values()) {
			if (team.isEmpty()) continue;

			FDTeamSite site = config.getTeamSite(team.getSiteId());
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

		flagManager.spawnAll();
		resourceGenerator.start();
		teamUpgradeManager.start();
		scoreboardManager.refreshAllGame();

		TellAll(manager.getMain().color("&a&l(!) &rFlagDefense has started! Defend your flag and capture &e"
				+ requiredCaptures + " &rothers to win!"));
		playSoundForAll(Sound.ENDERDRAGON_GROWL, 0.5f, 1.5f);
	}

	/** Level 1 kit per the design doc: blocks, basic weapons, tools, simple food. */
	public void giveStarterKit(Player player, FDTeam team) {
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
	 * during IN_PROGRESS. There's no elimination in FlagDefense - everyone always
	 * respawns after a 3-second spectating countdown; only the flag-capture count
	 * decides the winner.
	 */
	public void killPlayer(Player player, Player killer) {
		String message = killer != null
				? "&e" + player.getName() + " &rwas killed by &e" + killer.getName() + "&r!"
				: "&e" + player.getName() + " &rdied!";
		handleDeath(player, killer, message);
	}

	public void killPlayerInVoid(Player player) {
		handleDeath(player, null, "&e" + player.getName() + " &rfell into the void!");
	}

	private void handleDeath(Player player, Player killer, String coloredMessage) {
		FDTeam team = findTeamOf(player);
		if (team == null) return;

		TellAll(manager.getMain().color("&c&l(!) &r" + coloredMessage));
		flagManager.returnCarriedFlag(player, killer);
		beginRespawn(player, team);
	}

	private void beginRespawn(Player player, FDTeam team) {
		UUID uuid = player.getUniqueId();
		player.setGameMode(GameMode.SPECTATOR);

		new BukkitRunnable() {
			int secondsLeft = RESPAWN_SECONDS;

			@Override
			public void run() {
				Player p = Bukkit.getPlayer(uuid);
				if (p == null || state != FDGameState.IN_PROGRESS) {
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

	private void respawn(Player player, FDTeam team) {
		if (!team.isMember(player)) {
			return; // they already left the game through other means (e.g. /fd leave) while respawning
		}

		FDTeamSite site = manager.getMapConfigManager().getConfig().getTeamSite(team.getSiteId());
		player.setGameMode(GameMode.SURVIVAL);
		player.teleport(site.getSpawn());
		player.setHealth(20.0);
		player.setFoodLevel(20);
		giveStarterKit(player, team);
		player.sendTitle(manager.getMain().color("&a&lRESPAWNED"), "");
		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
	}

	/** Called by FDFlagManager once a carrier banks a capture; checks the win condition. */
	public void onFlagCaptured(FDTeam carrierTeam) {
		checkForWin();
	}

	private void checkForWin() {
		List<FDTeam> active = new ArrayList<>();
		for (FDTeam team : teams.values()) {
			if (!team.isEmpty()) active.add(team);
		}

		for (FDTeam team : active) {
			if (team.getCapturedFlags() >= requiredCaptures) {
				endMatch(team);
				return;
			}
		}

		if (active.size() <= 1) {
			endMatch(active.isEmpty() ? null : active.get(0));
		}
	}

	private static final long WIN_EFFECT_SECONDS = 10L;

	private void endMatch(FDTeam winner) {
		state = FDGameState.ENDING;

		if (winner != null) {
			TellAll(manager.getMain().color("&6&l(!) &eTeam " + winner.getName() + " &rhas won FlagDefense!"));
			playSoundForAll(Sound.LEVEL_UP, 1f, 0.7f);
			for (UUID uuid : winner.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) {
					p.sendTitle(manager.getMain().color("&6&lVICTORY"), manager.getMain().color("&eYou won the game!"));

					WinEffects winEffects = new WinEffects(p, new FDWinEffectsHost(this, p));
					winEffects.checkWinEffect();
					activeWinEffects.add(winEffects);
				}
			}
		} else {
			TellAll(manager.getMain().color("&6&l(!) &rFlagDefense ended with no winner."));
		}

		Bukkit.getScheduler().runTaskLater(manager.getMain(), () -> {
			for (WinEffects winEffects : activeWinEffects) {
				winEffects.removeWinEffects();
			}
			activeWinEffects.clear();
			resetAndClear();
		}, WIN_EFFECT_SECONDS * 20L);
	}

	private void resetAndClear() {
		resourceGenerator.stop();
		flagManager.despawnAll();
		defenseManager.stopAll();
		teamUpgradeManager.stop();
		scoreboardManager.removeAll();
		restoreWorld();

		for (FDTeam team : teams.values()) {
			team.setInactive(false);
			team.setLevel(TeamLevel.LEVEL_1);
			team.resetUpgrades();
			team.resetFlagProgress();

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
		for (FDTeam team : teams.values()) {
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
