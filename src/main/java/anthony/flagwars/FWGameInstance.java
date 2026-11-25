package anthony.flagwars;

import anthony.SuperCraftBrawl.Game.WinEffects;
import anthony.SuperCraftBrawl.party.Party;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.titles.TitleSequence;
import anthony.SuperCraftBrawl.titles.TitleUtil;
import anthony.flagwars.flag.FWFlagManager;
import anthony.flagwars.map.FWMapConfig;
import anthony.flagwars.map.FWTeamSite;
import anthony.flagwars.resources.FWEconomy;
import anthony.flagwars.resources.FWResourceGenerator;
import anthony.flagwars.resources.FWResourceType;
import anthony.flagwars.shop.FWTeamUpgradeManager;
import anthony.flagwars.vote.FWGameSettings;
import anthony.flagwars.vote.FWLobbyItems;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/**
 * One Flag Wars match: lobby -> countdown -> in-progress -> reset. Several of these can run
 * concurrently now (one per mode/queue, plus overflow matches on cloned worlds) - see
 * FWGameManager, which owns the list of active instances and provisions each one's world.
 */
public class FWGameInstance {

	private final FWGameManager manager;
	private final FWGameMode mode;
	private final World world;
	/** Name of the map this match is a copy of (e.g. "Speedway") - the world itself is a throwaway "fwgame_" copy. */
	private final String mapName;
	private final FWMapConfig mapConfig;
	private final Map<Integer, FWTeam> teams = new LinkedHashMap<>();
	private final List<Player> spectators = new ArrayList<>();
	private final List<BlockState> undoLog = new ArrayList<>();
	private final Set<Block> placedBlocks = new HashSet<>();
	private final Map<UUID, Integer> kills = new HashMap<>();
	private final Map<UUID, Integer> flagCaptures = new HashMap<>();
	/** Battle Pass XP each player earned this match (quests), reported in the end-of-match summary. Gone with the match. */
	private final Map<UUID, Integer> battlePassXp = new HashMap<>();
	private final FWFlagManager flagManager;
	private final FWResourceGenerator resourceGenerator;
	private final FWScoreboardManager scoreboardManager;
	private final FWTeamUpgradeManager teamUpgradeManager;
	private final FWGameSettings gameSettings;
	private final FWChestHologramManager chestHologramManager;
	private final FWTrapManager trapManager;

	private FWGameState state = FWGameState.WAITING;
	private BukkitTask countdownTask;
	private int countdownSecondsLeft;
	private boolean settingsLocked = false;
	private int requiredCaptures = FWGameConstants.MAX_WINNING_FLAG_CAPTURES;
	/** Set when the match starts: a 1v1, where the same flag is captured twice instead of every flag once. */
	private boolean duel = false;
	private final List<WinEffects> activeWinEffects = new ArrayList<>();
	/** Zombie/Skeleton Pokeball mobs alive in this match (entity UUID -> thrower and team), so nothing outlives the match. */
	private final Map<UUID, PokeballMob> pokeballMobs = new HashMap<>();

	public FWGameInstance(FWGameManager manager, FWGameMode mode, World world, FWMapConfig map) {
		this.manager = manager;
		this.mode = mode;
		this.world = world;
		this.mapName = map.getWorldName();
		this.mapConfig = map.translateTo(world);
		this.flagManager = new FWFlagManager(this);
		this.resourceGenerator = new FWResourceGenerator(this);
		this.scoreboardManager = new FWScoreboardManager(this);
		this.teamUpgradeManager = new FWTeamUpgradeManager(this);
		this.gameSettings = new FWGameSettings(this);
		this.chestHologramManager = new FWChestHologramManager(this);
		this.trapManager = new FWTrapManager(this);

		// Peaceful removes hostile mobs the moment they spawn (the Pokeball Zombies and Skeletons)
		if (world.getDifficulty() == Difficulty.PEACEFUL) world.setDifficulty(Difficulty.EASY);

		// Only sites actually created via /fwsetup team <color> get a runtime team - an uncreated
		// slot has no configured spawn/flag/shop, so letting players get assigned to it would break.
		for (int siteId = 1; siteId <= FWMapConfig.TEAM_COUNT; siteId++) {
			if (!mapConfig.getTeamSite(siteId).isCreated()) continue;

			teams.put(siteId, new FWTeam(
					siteId,
					FWGameConstants.TEAM_NAMES[siteId - 1],
					FWGameConstants.TEAM_COLORS[siteId - 1],
					mode.getTeamCapacity()
			));
		}
	}

	/** Actual max players for this specific map - scales with however many teams were actually created on it, not a fixed 8-team ceiling. */
	public int getMaxPlayers() {
		return mode.getTeamCapacity() * teams.size();
	}

	public FWGameManager getManager() {
		return manager;
	}

	public FWGameMode getMode() {
		return mode;
	}

	/** This instance's own copy of the map config, Location-translated to whichever world it's actually running on (see FWMapConfig#translateTo). */
	public FWMapConfig getMapConfig() {
		return mapConfig;
	}

	public String getMapName() {
		return mapName;
	}

	public boolean hasSpace() {
		return teams.values().stream().anyMatch(FWTeam::hasSpace);
	}

	/** Seconds left in the queued countdown - only meaningful while state == STARTING. */
	public int getCountdownSecondsLeft() {
		return countdownSecondsLeft;
	}

	public FWFlagManager getFlagManager() {
		return flagManager;
	}

	/** How this player is named in the tab list of everyone in the match. */
	public String getTabName(Player player) {
		// Someone who is watching is never listed with a team, even if they were on one before they were out
		return scoreboardManager.tabName(player, spectators.contains(player) ? null : findTeamOf(player));
	}

	public FWScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public FWResourceGenerator getResourceGenerator() {
		return resourceGenerator;
	}

	public FWTeamUpgradeManager getTeamUpgradeManager() {
		return teamUpgradeManager;
	}

	public FWTrapManager getTrapManager() {
		return trapManager;
	}

	public FWGameSettings getGameSettings() {
		return gameSettings;
	}

	/** Refreshes every participant's in-progress scoreboard; called whenever team status changes. */
	public void refreshScoreboards() {
		if (state == FWGameState.IN_PROGRESS) {
			scoreboardManager.refreshAllGame();
		}
	}

	public World getWorld() {
		return world;
	}

	public FWGameState getState() {
		return state;
	}

	/** How many flags the winning team needs this match - scaled down for small player counts, see FWGameConstants#requiredCapturesFor. */
	public int getRequiredCaptures() {
		return requiredCaptures;
	}

	/** True in a 1v1, where the one enemy flag has to be captured more than once, see FWGameConstants#isDuel. */
	public boolean isDuel() {
		return duel;
	}

	public Map<Integer, FWTeam> getTeams() {
		return teams;
	}

	public List<Player> getSpectators() {
		return spectators;
	}

	/**
	 * Who threw a Pokeball mob and which team it fights for. UUID and number only, plus the timer that takes the mob
	 * away, which is cancelled as soon as the mob is forgotten so it never keeps the mob or the match alive.
	 */
	public static final class PokeballMob {
		public final UUID owner;
		public final int teamSiteId;
		private BukkitTask despawnTask;

		PokeballMob(UUID owner, int teamSiteId) {
			this.owner = owner;
			this.teamSiteId = teamSiteId;
		}

		void setDespawnTask(BukkitTask task) {
			this.despawnTask = task;
		}

		private void cancelDespawn() {
			if (despawnTask != null) {
				despawnTask.cancel();
				despawnTask = null;
			}
		}
	}

	public PokeballMob addPokeballMob(Entity mob, UUID owner, FWTeam team) {
		PokeballMob info = new PokeballMob(owner, team.getSiteId());
		pokeballMobs.put(mob.getUniqueId(), info);
		return info;
	}

	/** The Pokeball info of this entity, or null if it isn't one of this match's Pokeball mobs. */
	public PokeballMob getPokeballMob(Entity entity) {
		return entity == null ? null : pokeballMobs.get(entity.getUniqueId());
	}

	public void removePokeballMob(Entity entity) {
		PokeballMob info = pokeballMobs.remove(entity.getUniqueId());
		if (info != null) info.cancelDespawn();
	}

	public boolean hasPlayer(Player player) {
		return findTeamOf(player) != null;
	}

	public boolean hasSpectator(Player player) {
		return spectators.contains(player);
	}

	public FWTeam findTeamOf(Player player) {
		for (FWTeam team : teams.values()) {
			if (team.isMember(player)) return team;
		}
		return null;
	}

	/** Tracks a block's pre-change state during a match so it can be restored on reset. */
	public void recordBlockChange(BlockState before) {
		undoLog.add(before);
	}

	/** Marks a block as player-placed - only these can be broken back (see FWPlayerListener#onBlockBreak). */
	public void markPlaced(Block block) {
		placedBlocks.add(block);
	}

	public boolean isPlacedByPlayer(Block block) {
		return placedBlocks.contains(block);
	}

	public void unmarkPlaced(Block block) {
		placedBlocks.remove(block);
	}

	public boolean addPlayer(Player player) {
		if (state != FWGameState.WAITING && state != FWGameState.STARTING) {
			player.sendMessage(manager.getMain().color("&c&l(!) &rA Flag Wars match is already in progress!"));
			return false;
		}

		// On the same team as their party, if a team holds more than one player and has room for them
		FWTeam freeTeam = teamWithPartyMate(player);
		if (freeTeam == null)
			freeTeam = teams.values().stream().filter(FWTeam::hasSpace).findFirst().orElse(null);
		if (freeTeam == null) {
			player.sendMessage(manager.getMain().color("&c&l(!) &rFlag Wars is full!"));
			return false;
		}

		return seat(player, freeTeam);
	}

	/** Seats the player on this team (which must have room) and does everything else joining a match does. */
	private boolean seat(Player player, FWTeam freeTeam) {
		freeTeam.addMember(player.getUniqueId());
		player.getInventory().clear();
		player.getEnderChest().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.teleport(mapConfig.getLobby());
		FWLobbyItems.give(player);
		scoreboardManager.applyNameTag(player, freeTeam);

		// Same join-time hygiene SCB's own GameInstance#AddPlayer applies (armor),
		// plus a potion-effect reset since Flag Wars shops sell Speed/Jump/Invisibility that
		// could otherwise be carried in from a previous match. One choke point so it applies
		// the same way whether a player joined via /join flagwars or the FWModeSelectGUI.
		manager.getMain().getListener().resetArmor(player);
		manager.getMain().getListener().resetPotionEffects(player);

		player.sendMessage(manager.getMain().color("&2&l(!) &rYou joined Flag Wars as Team &e"
				+ freeTeam.getName()));
		TellAll(manager.getMain().color("&2&l(!) &f" + player.getName() + "&a joined ("
				+ countPlayers() + "/" + getMaxPlayers() + ")"));

		checkForStart();

		// The ready-up dye only goes out once the countdown is actually running (2+ players),
		// matching SCB's own ready-up - never handed out while still alone in the lobby. Players who
		// join during the countdown get one too, and anyone who already has it keeps theirs.
		if (state == FWGameState.STARTING) {
			for (FWTeam t : teams.values()) {
				for (UUID uuid : t.getMembers()) {
					Player p = Bukkit.getPlayer(uuid);
					if (p != null) FWLobbyItems.giveReadyItem(p, gameSettings.isReady(p));
				}
			}
		}

		scoreboardManager.refreshAllWaiting(waitingStatusLine());
		return true;
	}

	/**
	 * A team that has someone from this player's party on it and room for them, or null. Only matters in a mode where
	 * a team holds more than one player (Solo teams are one player, so a party there is spread over its own islands).
	 */
	private FWTeam teamWithPartyMate(Player player) {
		if (mode.getTeamCapacity() <= 1 || manager.getMain().getPartyManager() == null) return null;

		Party party = manager.getMain().getPartyManager().getParty(player);
		if (party == null) return null;

		for (FWTeam team : teams.values()) {
			if (!team.hasSpace()) continue;
			for (UUID member : team.getMembers()) {
				if (party.contains(member)) return team;
			}
		}
		return null;
	}

	/**
	 * Whether a party this big fits in this match with its members kept together: whole teams for as many of them as
	 * fill one, then a team with room for the rest. In Solo that is simply one free island each.
	 */
	public boolean canSeatTogether(int partySize) {
		int capacity = mode.getTeamCapacity();
		int wholeTeams = partySize / capacity;
		int rest = partySize % capacity;

		int empty = 0;
		for (FWTeam team : teams.values()) {
			if (team.isEmpty()) empty++;
		}
		if (empty < wholeTeams) return false;
		if (rest == 0 || empty > wholeTeams) return true;

		for (FWTeam team : teams.values()) {
			if (!team.isEmpty() && team.freeSpace() >= rest) return true;
		}
		return false;
	}

	/** Seats a whole party (check canSeatTogether first), keeping its members together on teams that hold more than one. */
	public void addParty(List<Player> members) {
		if (state != FWGameState.WAITING && state != FWGameState.STARTING) return;

		int capacity = mode.getTeamCapacity();
		List<Player> waiting = new ArrayList<>(members);
		while (!waiting.isEmpty()) {
			int group = Math.min(capacity, waiting.size());

			// The snuggest team that has room for the group, so a small group fills a gap and empty teams stay free
			FWTeam team = null;
			for (FWTeam candidate : teams.values()) {
				if (candidate.freeSpace() < group) continue;
				if (team == null || candidate.freeSpace() < team.freeSpace()) team = candidate;
			}

			for (int i = 0; i < group; i++) {
				Player member = waiting.remove(0);
				if (team != null) seat(member, team);
				else addPlayer(member);
			}
		}
	}

	public void removePlayer(Player player) {
		FWTeam team = findTeamOf(player);
		if (team == null) return;

		team.removeMember(player.getUniqueId());
		scoreboardManager.remove(player);
		readyCooldownEnds.remove(player.getUniqueId());

		if (state == FWGameState.WAITING || state == FWGameState.STARTING) {
			TellAll(manager.getMain().color("&2&l(!) &e" + player.getName() + " &chas left the game!"));
			gameSettings.removePlayer(player);

			if (state == FWGameState.STARTING && !hasEnoughToStart()) {
				cancelCountdown();
			}

			if (countPlayers() == 0) {
				manager.releaseArenaWorld(this);
			} else {
				scoreboardManager.refreshAllWaiting(waitingStatusLine());
			}
		} else if (state == FWGameState.IN_PROGRESS) {
			flagManager.returnCarriedFlag(player, null);

			if (team.isEmpty()) {
				team.setInactive(true);
				TellAll(manager.getMain().color("&c&l(!) &e" + player.getName()
						+ "'s &rbase has been vacated!"));
			}
			checkForWin();
			if (state == FWGameState.IN_PROGRESS) announcePlayersLeft();
			refreshScoreboards();

			// Save now rather than only at match end, so kills/deaths/captures earned before
			// leaving early aren't lost - no win/loss penalty for leaving, just what they already earned.
			PlayerData data = playerDataOf(player);
			if (data != null) manager.getMain().getDataManager().saveData(data);
		}

		player.getEnderChest().clear();
		manager.getMain().ResetPlayer(player);
		manager.getMain().getListener().resetArmor(player); // ResetPlayer() only clears the main inventory, not armor slots
		manager.getMain().getListener().resetPotionEffects(player); // nor any Speed/Jump/Invisibility potions bought from the shop
	}

	/** Same wording as SCB's waiting board ("Waiting for 1 player"), with the real number still needed to start. */
	/** Same as SCB's GameInstance#announcePlayersLeft: a chat line with the count, and a ping for everyone still in the match. */
	private void announcePlayersLeft() {
		TellAll(manager.getMain().color("&2&l(!) &rThere are &e" + countPlayers() + "&r players left!"));
		playSoundForAll(Sound.NOTE_PLING, 1f, 1f);
	}

	private String waitingStatusLine() {
		if (state == FWGameState.STARTING) {
			return "&rStarting In: &a" + FWGameConstants.STARTING_COUNTDOWN_SECONDS + "s";
		}
		int needed = Math.max(1, mode.getMinPlayers() - countPlayers());
		// "...&7&oplayers" was 31 chars, one over what a scoreboard line can hold on 1.8, so this stays short
		return "&7&oWaiting for &a" + needed + " &7&omore";
	}

	/**
	 * Exactly mirrors SCB's own GameInstance#AddSpectator: GameMode.ADVENTURE (not vanilla
	 * GameMode.SPECTATOR) plus the same manual fakery - hidden from every active player, visible
	 * to other spectators, no entity collision, flight enabled by hand. Interaction/damage/attack
	 * blocking for spectators is handled separately in FWPlayerListener, same split SCB uses.
	 */
	public void addSpectator(Player player) {
		spectators.add(player);

		for (FWTeam team : teams.values()) {
			for (UUID uuid : team.getMembers()) {
				Player teamMember = Bukkit.getPlayer(uuid);
				if (teamMember != null) teamMember.hidePlayer(player);
			}
		}
		for (Player existingSpectator : spectators) {
			existingSpectator.showPlayer(player);
		}

		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.spigot().setCollidesWithEntities(false);
		player.setAllowFlight(true);
		Location spectatorLoc = mapConfig.getSpectatorLocation();
		player.teleport(spectatorLoc != null ? spectatorLoc : mapConfig.getLobby());
		FWLobbyItems.spectatorItems(player);
		scoreboardManager.joinNameTagScoreboard(player);

		if (state == FWGameState.IN_PROGRESS) {
			scoreboardManager.showGame(player);
		} else {
			scoreboardManager.showWaiting(player, waitingStatusLine());
		}
	}

	public void removeSpectator(Player player) {
		spectators.remove(player);
		scoreboardManager.remove(player);

		// Toggling false then true (ResetPlayer below sets it back true) forces the client to
		// properly resync flight state - just leaving it true straight through from spectating breaks it.
		player.setAllowFlight(false);
		manager.getMain().ResetPlayer(player);
		manager.getMain().getListener().resetArmor(player);
		manager.getMain().getListener().resetPotionEffects(player);

		player.spigot().setCollidesWithEntities(true);
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.showPlayer(player);
		}
	}

	public int countPlayers() {
		return teams.values().stream().mapToInt(t -> t.getMembers().size()).sum();
	}

	/** Occupied islands, not raw player count - in Duos a team of 2 is still only one flag to capture. */
	public int countActiveTeams() {
		return (int) teams.values().stream().filter(t -> !t.isEmpty()).count();
	}

	/** All currently-online team members and spectators; used to refresh scoreboards. */
	List<Player> onlineParticipants() {
		List<Player> online = new ArrayList<>();
		for (FWTeam team : teams.values()) {
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) online.add(p);
			}
		}
		online.addAll(spectators);
		return online;
	}

	private void checkForStart() {
		if (state == FWGameState.WAITING && hasEnoughToStart()) {
			startCountdown();
		}
	}

	/**
	 * Enough players for the countdown to run. Empty bases are fine on any map size: the win target scales
	 * down with the number of occupied bases (see FWGameConstants#requiredCapturesFor), so 3 players on a
	 * 4-player map just play to 2 captures.
	 */
	private boolean hasEnoughToStart() {
		return countPlayers() >= mode.getMinPlayers();
	}

	private void startCountdown() {
		state = FWGameState.STARTING;
		countdownSecondsLeft = FWGameConstants.STARTING_COUNTDOWN_SECONDS;
		announceGameStarting();

		countdownTask = new org.bukkit.scheduler.BukkitRunnable() {
			@Override
			public void run() {
				if (countdownSecondsLeft <= 0) {
					countdownTask = null;
					this.cancel();
					startMatch();
					return;
				}

				// Same NOTE_PLING schedule as SCB's GameInstance#StartGameTimer - no chat spam,
				// just the scoreboard countdown below plus these cues building up to the final ding.
				if (countdownSecondsLeft == 25 || countdownSecondsLeft == 20 || countdownSecondsLeft == 15 || countdownSecondsLeft == 10
						|| (countdownSecondsLeft <= 5 && countdownSecondsLeft >= 2)) {
					playSoundForAll(Sound.NOTE_PLING, 1f, 1f);
				} else if (countdownSecondsLeft == 1) {
					playSoundForAll(Sound.NOTE_PLING, 5f, 7f);
				}

				if (countdownSecondsLeft == 5) {
					lockInVoteSettings();
				}

				scoreboardManager.refreshAllWaiting("&rStarting In: &a" + countdownSecondsLeft + "s");
				countdownSecondsLeft--;
			}
		}.runTaskTimer(manager.getMain(), 0L, 20L);
	}

	/**
	 * Mirrors SCB's own GameInstance#gameStartingMessage - same clickable "Click here to join!"
	 * broadcast to the lobby world, with the same color scheme (its Frenzy variant inserts a
	 * "&7&oFrenzy" tag the same way this inserts "&6&lFlagWars"). Fires once, right as the
	 * countdown begins - Flag Wars' whole countdown already is 30 seconds, unlike SCB's longer
	 * timer where 30-seconds-left is just a specific milestone partway through.
	 */
	private void announceGameStarting() {
		World lobby = manager.getMain().getLobbyWorld();
		if (lobby == null) return;

		String msg = manager.getMain().color("&2&l(!) &a&lA &6&lFlagWars&a&l game on&f&l "
				+ mapName + " &a&lis starting in 30 seconds");

		TextComponent join = new TextComponent("     " + manager.getMain().color("&a&lClick here to join!"));
		join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/join flagwars " + (mode == FWGameMode.SOLO ? "solo" : "duo")));

		for (Player p : lobby.getPlayers()) {
			p.sendMessage(msg);
			p.spigot().sendMessage(join);
		}
	}

	/** How long a player has to wait between two clicks on the ready-up dye, so it can't be spammed. Same as SCB. */
	private static final long READY_COOLDOWN_MILLIS = 3000L;

	/** Player to the System.currentTimeMillis() until which they can't click the ready-up dye again. Dropped when they leave. */
	private final Map<UUID, Long> readyCooldownEnds = new HashMap<>();

	/**
	 * The player clicked the ready-up dye: they're ready now, or not anymore, and the dye shows it. Same as SCB's
	 * GameInstance#toggleReady, including the 3 second cooldown. Only works while the countdown is running, which
	 * is the only time the dye is handed out.
	 */
	public void toggleReady(Player player) {
		if (state != FWGameState.STARTING || !hasPlayer(player)) return;

		long now = System.currentTimeMillis();
		int secondsLeft = readyCooldownSecondsLeft(readyCooldownEnds.get(player.getUniqueId()), now);
		if (secondsLeft > 0) {
			player.sendMessage(manager.getMain().color("&c&l(!) &rYou can use this again in &e" + secondsLeft + "s"));
			return;
		}
		readyCooldownEnds.put(player.getUniqueId(), now + READY_COOLDOWN_MILLIS);

		player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
		gameSettings.toggleReady(player);

		// Everyone being ready starts the match, and that clears their inventories
		if (state != FWGameState.STARTING) return;
		FWLobbyItems.updateReadyItem(player, gameSettings.isReady(player));
	}

	/** Whole seconds (rounded up) until a player may click the ready-up dye again, given when their cooldown ends (null if they never clicked). 0 means they can click now. */
	static int readyCooldownSecondsLeft(Long cooldownEnd, long now) {
		if (cooldownEnd == null || now >= cooldownEnd) return 0;
		return (int) Math.ceil((cooldownEnd - now) / 1000.0);
	}

	/** Used by the ready-up dye to skip the rest of the queued countdown once every player is ready. */
	public void forceStartNow() {
		if (state != FWGameState.STARTING) return;
		forceStartGame();
	}

	/** Used by /startgame (/sg) to force a match to begin immediately, regardless of player count or whether the countdown's even running yet. */
	public void forceStartGame() {
		if (state == FWGameState.IN_PROGRESS || state == FWGameState.ENDING) return;

		if (countdownTask != null) {
			countdownTask.cancel();
			countdownTask = null;
		}
		state = FWGameState.STARTING;

		TellAll(manager.getMain().color("&2&l(!) &rGame is now starting!"));
		lockInVoteSettings();
		startMatch();
	}

	/**
	 * Takes away every ready-up dye (it's too late to ready up) and applies whichever lobby votes passed -
	 * mirrors SCB locking its own settings (time of day, drop rate, etc.) in 5 seconds before the match
	 * actually begins. Guarded so it only ever runs once, since it can be reached both from the countdown's
	 * 5-second mark and from forceStartNow() skipping straight past it.
	 */
	private void lockInVoteSettings() {
		if (settingsLocked) return;
		settingsLocked = true;

		for (FWTeam team : teams.values()) {
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) FWLobbyItems.removeReadyItem(p);
			}
		}

		if (gameSettings.isNightVotePassed()) {
			world.setTime(18000);
			TellAll(manager.getMain().color("&2&l(!) &rThe time has been set to &9&lNight"));
		}
		if (gameSettings.isDoubleGeneratorVotePassed()) {
			resourceGenerator.doubleSpawnRate();
			TellAll(manager.getMain().color("&2&l(!) &rGenerator spawn rate is now &e&l2x"));
		}
	}

	private void cancelCountdown() {
		if (countdownTask != null) {
			countdownTask.cancel();
			countdownTask = null;
		}
		state = FWGameState.WAITING;
		TellAll(manager.getMain().color("&c&l(!) &rNot enough players, countdown cancelled."));

		// Nobody is ready anymore, and the dye is handed out again when the next countdown starts
		gameSettings.clearReady();
		for (FWTeam team : teams.values()) {
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) FWLobbyItems.removeReadyItem(p);
			}
		}

		scoreboardManager.refreshAllWaiting(waitingStatusLine());
	}

	private void startMatch() {
		state = FWGameState.IN_PROGRESS;
		duel = FWGameConstants.isDuel(countActiveTeams(), countPlayers());
		requiredCaptures = FWGameConstants.requiredCapturesFor(countActiveTeams(), countPlayers());

		for (FWTeam team : teams.values()) {
			if (team.isEmpty()) continue;

			FWTeamSite site = mapConfig.getTeamSite(team.getSiteId());
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p == null) continue;

				p.teleport(site.getSpawn());
				p.setGameMode(GameMode.SURVIVAL);
				p.setHealth(20.0);
				p.setFoodLevel(20);
				giveStarterKit(p, team);
				manager.grantSpawnProtection(p, this);
			}
		}

		flagManager.spawnAll();
		chestHologramManager.start();
		trapManager.start();
		resourceGenerator.start();
		teamUpgradeManager.start();
		scoreboardManager.refreshAllGame();

		TellAll(manager.getMain().color("&e&l----------------------------------------"));
		TellAll(manager.getMain().color("                   &6&lFLAG WARS"));
		TellAll("");
		TellAll(manager.getMain().color("&r    Steal enemy flags and capture them at home."));
		TellAll(manager.getMain().color("&r      Earn resources to buy gear in the shop."));
		TellAll(manager.getMain().color("&r          Defend your flag from thieves!"));
		TellAll(manager.getMain().color("&r                    Good Luck!"));
		TellAll(manager.getMain().color("&e&l----------------------------------------"));
		if (duel) {
			TellAll(manager.getMain().color("&a&l(!) &rIt's a 1v1! Capture your opponent's flag &e" + requiredCaptures + " &rtimes to win!"));
		} else {
			TellAll(manager.getMain().color("&a&l(!) &rCapture &e" + requiredCaptures + " &rflags to win!"));
		}
		playSoundForAll(Sound.ENDERDRAGON_GROWL, 0.35f, 1.0f);
	}

	/** Level 1 kit per the design doc: a sword plus team-colored leather armor - everything else is bought from the shop. */
	public void giveStarterKit(Player player, FWTeam team) {
		// Copies, since the inventory is wiped below
		ItemStack diedWithPickaxe = bestToolOf(player, FWGameInstance::pickaxeTierOf);
		ItemStack diedWithAxe = bestToolOf(player, FWGameInstance::axeTierOf);
		// Unlike the pickaxe and axe, Shears have no tiers to drop - a bought pair is
		// just kept as-is across deaths instead of being lost like the rest of the starter-kit wipe.
		boolean hadShears = hasToolType(player, "SHEARS");

		player.getInventory().clear();
		// getInventory().clear() doesn't touch whatever item the player's mid-click holding on
		// their cursor (e.g. dragged out of a shop/inventory GUI) - without this, dying while
		// holding one let it survive straight through to the respawn kit.
		player.setItemOnCursor(new ItemStack(Material.AIR));
		player.getInventory().addItem(teamUpgradeManager.applyUpgrades(team, new ItemStack(Material.WOOD_SWORD)));

		// Dying drops a pickaxe or axe down exactly one tier instead of resetting it to the bottom. Neither goes below
		// wood (Diamond -> Iron -> Stone -> Wood, and Wood stays Wood). Everything handed back goes through the team
		// upgrades, which also makes it unbreakable like the same item bought in the shop.
		ItemStack respawnPickaxe = toolAfterDeath(diedWithPickaxe, team, FWGameInstance::pickaxeTierOf, this::pickaxeForTier, true);
		if (respawnPickaxe != null) player.getInventory().addItem(respawnPickaxe);

		ItemStack respawnAxe = toolAfterDeath(diedWithAxe, team, FWGameInstance::axeTierOf, this::axeForTier, false);
		if (respawnAxe != null) player.getInventory().addItem(respawnAxe);

		if (hadShears) player.getInventory().addItem(teamUpgradeManager.applyUpgrades(team, new ItemStack(Material.SHEARS)));
		giveStarterArmorIfUnequipped(player, team);
	}

	/** Returns true if the player is carrying any tool whose material name ends with the given suffix (e.g. "SHEARS"). */
	private boolean hasToolType(Player player, String suffix) {
		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null && item.getType().name().endsWith(suffix)) return true;
		}
		return false;
	}

	/** 0=none, 1=wood, 2=stone, 3=iron, 4=diamond - same ladder FWShopCatalog's own pickaxeTierOf uses. */
	private static int pickaxeTierOf(Material type) {
		switch (type) {
			case DIAMOND_PICKAXE: return 4;
			case IRON_PICKAXE: return 3;
			case STONE_PICKAXE: return 2;
			case WOOD_PICKAXE: return 1;
			default: return 0;
		}
	}

	/** 0=none, 1=wood, 2=stone, 3=iron, 4=diamond - same ladder FWShopCatalog's own axeTierOf uses. */
	private static int axeTierOf(Material type) {
		switch (type) {
			case DIAMOND_AXE: return 4;
			case IRON_AXE: return 3;
			case STONE_AXE: return 2;
			case WOOD_AXE: return 1;
			default: return 0;
		}
	}

	/** The tier a pickaxe or axe comes back as after dying: one lower, but never lower than wood. No tool (0) stays none. */
	static int toolTierAfterDeath(int tier) {
		return tier <= 0 ? 0 : Math.max(1, tier - 1);
	}

	/** A copy of the best tool of one kind (pickaxe or axe) the player carries (a pickup can leave a lower spare next to it), or null if there is none. */
	private ItemStack bestToolOf(Player player, ToIntFunction<Material> tierOf) {
		ItemStack best = null;
		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null) continue;
			if (tierOf.applyAsInt(item.getType()) > (best == null ? 0 : tierOf.applyAsInt(best.getType()))) best = item;
		}
		return best == null ? null : best.clone();
	}

	/**
	 * The pickaxe or axe a player respawns with, or null if they had none. A wood one comes back exactly as it was.
	 * A higher tier is made again one tier lower, with that tier's own enchants, or with the enchants the old one had
	 * when keepEnchants is set. The team's upgrades are applied to it like to everything a player is given, which
	 * also keeps it unbreakable.
	 */
	private ItemStack toolAfterDeath(ItemStack died, FWTeam team, ToIntFunction<Material> tierOf,
									 IntFunction<ItemStack> forTier, boolean keepEnchants) {
		if (died == null) return null;

		int tier = tierOf.applyAsInt(died.getType());
		int respawnTier = toolTierAfterDeath(tier);
		if (respawnTier == 0) return null;

		ItemStack tool;
		if (respawnTier == tier) {
			tool = died;
		} else {
			tool = forTier.apply(respawnTier);
			if (keepEnchants) tool.addUnsafeEnchantments(died.getEnchantments());
		}
		return teamUpgradeManager.applyUpgrades(team, tool);
	}

	/** Enchanted the same way FWShopCatalog.tools() enchants each pickaxe tier - Efficiency I across the board. Null at tier &lt;= 0 (nothing to give). */
	private ItemStack pickaxeForTier(int tier) {
		switch (tier) {
			case 4: return ItemHelper.addEnchant(new ItemStack(Material.DIAMOND_PICKAXE), Enchantment.DIG_SPEED, 1);
			case 3: return ItemHelper.addEnchant(new ItemStack(Material.IRON_PICKAXE), Enchantment.DIG_SPEED, 1);
			case 2: return ItemHelper.addEnchant(new ItemStack(Material.STONE_PICKAXE), Enchantment.DIG_SPEED, 1);
			case 1: return ItemHelper.addEnchant(new ItemStack(Material.WOOD_PICKAXE), Enchantment.DIG_SPEED, 1);
			default: return null;
		}
	}

	/** Enchanted the same way FWShopCatalog.tools() enchants each axe tier - Wood/Stone at Efficiency I, Iron at II, Diamond at III. Null at tier &lt;= 0 (nothing to give). */
	private ItemStack axeForTier(int tier) {
		switch (tier) {
			case 4: return ItemHelper.addEnchant(new ItemStack(Material.DIAMOND_AXE), Enchantment.DIG_SPEED, 3);
			case 3: return ItemHelper.addEnchant(new ItemStack(Material.IRON_AXE), Enchantment.DIG_SPEED, 2);
			case 2: return ItemHelper.addEnchant(new ItemStack(Material.STONE_AXE), Enchantment.DIG_SPEED, 1);
			case 1: return ItemHelper.addEnchant(new ItemStack(Material.WOOD_AXE), Enchantment.DIG_SPEED, 1);
			default: return null;
		}
	}

	/**
	 * Free team-colored leather armor, same as Hypixel Bedwars' cosmetic starter set. Only fills
	 * slots that are currently empty so a respawn never strips off armor a player already bought.
	 */
	private void giveStarterArmorIfUnequipped(Player player, FWTeam team) {
		Color teamColor = team.getColor().getColor();
		PlayerInventory inv = player.getInventory();

		if (isEmptySlot(inv.getHelmet())) inv.setHelmet(ItemHelper.setColor(new ItemStack(Material.LEATHER_HELMET), teamColor));
		if (isEmptySlot(inv.getChestplate())) inv.setChestplate(ItemHelper.setColor(new ItemStack(Material.LEATHER_CHESTPLATE), teamColor));
		if (isEmptySlot(inv.getLeggings())) inv.setLeggings(ItemHelper.setColor(new ItemStack(Material.LEATHER_LEGGINGS), teamColor));
		if (isEmptySlot(inv.getBoots())) inv.setBoots(ItemHelper.setColor(new ItemStack(Material.LEATHER_BOOTS), teamColor));
	}

	private boolean isEmptySlot(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}

	private static final int RESPAWN_SECONDS = 5;

	/**
	 * Called when a player's fatal damage (PvP, mob, void, etc.) was intercepted
	 * during IN_PROGRESS. There's no elimination in Flag Wars - everyone always
	 * respawns after a 3-second spectating countdown; only the flag-capture count
	 * decides the winner.
	 */
	public void killPlayer(Player player, Player killer) {
		if (killer != null) addKill(killer);

		String message = killer != null
				? "&r" + coloredName(player) + " &cwas killed by &r" + coloredName(killer)
				: "&r" + coloredName(player) + " &cdied";
		handleDeath(player, killer, message);
	}

	/** Player's name in their team's chat color, same coloring FWFlagManager already uses for steal/capture broadcasts. */
	private String coloredName(Player player) {
		FWTeam team = findTeamOf(player);
		return team != null ? FWGameConstants.chatColorCodeFor(team.getColor()) + player.getName() : player.getName();
	}

	public void addKill(Player killer) {
		kills.merge(killer.getUniqueId(), 1, Integer::sum);

		PlayerData data = playerDataOf(killer);
		if (data != null) data.fwKills++;
	}

	public int getKills(Player player) {
		return kills.getOrDefault(player.getUniqueId(), 0);
	}

	/**
	 * Called by FWFlagManager once a carrier banks a capture, crediting it to them personally
	 * (FWTeam tracks the team-wide count). Persisted to FWFlagsCaptured here - per the brief, a
	 * steal alone never counts, only an actual capture.
	 */
	public void addFlagCapture(Player carrier) {
		flagCaptures.merge(carrier.getUniqueId(), 1, Integer::sum);

		PlayerData data = playerDataOf(carrier);
		if (data != null) data.fwFlagsCaptured++;

		addBattlePassXp(carrier, manager.getMain().battlePassQuest(carrier,
				anthony.SuperCraftBrawl.battlepass.BattlePassQuest.FLAG_WARS_CAPTURE, 1));
	}

	private void addBattlePassXp(Player player, int xp) {
		if (xp > 0)
			battlePassXp.merge(player.getUniqueId(), xp, Integer::sum);
	}

	/** Null-safe shorthand for the per-player stats record used throughout this class - never null for an online player. */
	private PlayerData playerDataOf(Player player) {
		return manager.getMain().getDataManager().getPlayerData(player);
	}

	public int getFlagCaptures(Player player) {
		return flagCaptures.getOrDefault(player.getUniqueId(), 0);
	}

	/**
	 * @param attacker whoever most recently hit this player before they fell, or null - see
	 *                 FWPlayerListener#recentAttackerOf, which mirrors SCB's own getLastDamageCause()
	 *                 check for crediting a "doomed to fall" kill instead of a plain void death.
	 */
	public void killPlayerInVoid(Player player, Player attacker) {
		if (attacker != null) {
			addKill(attacker);
			handleDeath(player, attacker, "&r" + coloredName(player) + " &cwas doomed to fall by &r" + coloredName(attacker));
		} else {
			handleDeath(player, null, "&r" + coloredName(player) + " &cfell into the void");
		}
	}

	private void handleDeath(Player player, Player killer, String coloredMessage) {
		FWTeam team = findTeamOf(player);
		if (team == null) return;

		PlayerData data = playerDataOf(player);
		if (data != null) data.fwDeaths++;

		TellAll(manager.getMain().color("&2&l(!) " + coloredMessage));
		flagManager.returnCarriedFlag(player, killer);
		if (killer != null) lootResourcesToKiller(player, killer);
		refreshScoreboards(); // so the killer's Kills line (and the victim's, if they were carrying) updates immediately
		beginRespawn(player, team);
		teleportToDeathView(player, killer);
	}

	/** While respawning, the dead player watches from their killer, or from the spectator point if nobody killed them. */
	private void teleportToDeathView(Player player, Player killer) {
		Location view;
		if (killer != null && killer.isOnline() && killer.getWorld().equals(player.getWorld())) {
			view = killer.getLocation();
		} else {
			Location spectatorLoc = mapConfig.getSpectatorLocation();
			view = spectatorLoc != null ? spectatorLoc : mapConfig.getLobby();
		}
		if (view != null) player.teleport(view);
	}

	/** Display order for the kill-feedback chat lines below - Iron, Gold, Diamond, Emerald. */
	private static final FWResourceType[] LOOT_FEEDBACK_ORDER =
			{FWResourceType.IRON, FWResourceType.GOLD, FWResourceType.DIAMOND, FWResourceType.EMERALD};

	/** Vanilla plays one pickup pop per item stack collected; a kill plays up to this many so a huge haul isn't a wall of noise. */
	private static final int MAX_LOOT_POPS = 6;
	private static final long LOOT_POP_INTERVAL_TICKS = 2L;
	private static final float LOOT_POP_VOLUME = 1.0f;

	/**
	 * Bedwars rule: a kill loots the victim's iron/gold/emerald/diamond straight into the killer's
	 * inventory instead of dropping it. The killer hears Minecraft's normal item pickup sound once
	 * per stack looted (see playLootPickupSounds), so a bigger haul sounds like picking up more
	 * items, and nothing plays when the victim had no resources.
	 * Also tells the killer exactly what they picked up, one "&lt;color&gt;+N &lt;Name&gt;" line per
	 * type looted, so a kill's payoff is visible without opening the inventory.
	 */
	private void lootResourcesToKiller(Player victim, Player killer) {
		List<String> feedback = new ArrayList<>();
		int stacks = 0;

		for (FWResourceType type : LOOT_FEEDBACK_ORDER) {
			int amount = FWEconomy.count(victim, type);
			if (amount <= 0) continue;

			FWEconomy.withdraw(victim, type, amount);
			killer.getInventory().addItem(new ItemStack(type.getMaterial(), amount));
			stacks += (amount + type.getMaterial().getMaxStackSize() - 1) / type.getMaterial().getMaxStackSize();
			feedback.add(type.getColorCode() + "+" + amount + " " + type.getDisplayName());
		}

		playLootPickupSounds(killer, stacks);

		for (String line : feedback) {
			killer.sendMessage(manager.getMain().color(line));
		}
	}

	/** One vanilla pickup pop per looted stack (capped), a couple of ticks apart so several are heard as several. Nothing for 0. */
	private void playLootPickupSounds(Player killer, int stacks) {
		int pops = Math.min(stacks, MAX_LOOT_POPS);
		if (pops <= 0) return;

		playPickupPop(killer);
		if (pops == 1) return;

		UUID killerId = killer.getUniqueId();
		new BukkitRunnable() {
			int left = pops - 1;

			@Override
			public void run() {
				Player p = Bukkit.getPlayer(killerId);
				if (p == null || state != FWGameState.IN_PROGRESS) {
					cancel();
					return;
				}

				playPickupPop(p);
				if (--left <= 0) cancel();
			}
		}.runTaskTimer(manager.getMain(), LOOT_POP_INTERVAL_TICKS, LOOT_POP_INTERVAL_TICKS);
	}

	/**
	 * Minecraft's own item pickup sound ("random.pop") with its randomized pitch (the client caps it
	 * at 2.0). Vanilla plays it at volume 0.2, which is far too quiet to hear over a fight, so this
	 * uses the loudest the client will play (volume above 1.0 only widens the range, not the loudness).
	 */
	private static void playPickupPop(Player player) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		float pitch = ((random.nextFloat() - random.nextFloat()) * 0.7f + 1.0f) * 2.0f;
		player.playSound(player.getLocation(), Sound.ITEM_PICKUP, LOOT_POP_VOLUME, Math.max(0.5f, Math.min(2.0f, pitch)));
	}

	private void beginRespawn(Player player, FWTeam team) {
		UUID uuid = player.getUniqueId();
		player.setGameMode(GameMode.SPECTATOR);

		new BukkitRunnable() {
			int secondsLeft = RESPAWN_SECONDS;

			@Override
			public void run() {
				Player p = Bukkit.getPlayer(uuid);
				if (p == null || state != FWGameState.IN_PROGRESS) {
					cancel();
					return;
				}

				if (secondsLeft <= 0) {
					cancel();
					respawn(p, team);
					return;
				}

				// Same as Super Craft Bros: the title is sent once and stays for the whole countdown, then only the number changes
				String seconds = "&c" + secondsLeft;
				if (secondsLeft == RESPAWN_SECONDS) {
					TitleUtil.sendTitle(p, "&eRespawning In", seconds, 1, RESPAWN_SECONDS * 20, 1);
				} else {
					TitleUtil.updateTitleText(p, null, seconds);
				}
				secondsLeft--;
			}
		}.runTaskTimer(manager.getMain(), 0L, 20L);
	}

	private void respawn(Player player, FWTeam team) {
		if (!team.isMember(player)) {
			return; // they already left the game through other means (e.g. /leave) while respawning
		}

		manager.clearAttackerHistory(player);

		FWTeamSite site = mapConfig.getTeamSite(team.getSiteId());
		// Spawn first, Survival second: switching first leaves them live (able to fall, take damage or
		// trigger things) at the spot they watched from for a moment before they arrive home.
		player.teleport(site.getSpawn());
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth(20.0);
		player.setFoodLevel(20);
		manager.getMain().getListener().resetPotionEffects(player); // Speed/Jump/Invisibility etc. shouldn't survive a death
		giveStarterKit(player, team);
		manager.grantSpawnProtection(player, this);
		// Same as Super Craft Bros
		TitleUtil.sendTimes(player, 1, 30, 10);
		TitleUtil.setSubtitle(player, "");
		TitleUtil.setTitle(player, "&eRespawned");
		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
	}

	/** Called by FWFlagManager once a carrier banks a capture; checks the win condition. */
	public void onFlagCaptured(FWTeam carrierTeam) {
		checkForWin();
	}

	private void checkForWin() {
		List<FWTeam> active = new ArrayList<>();
		for (FWTeam team : teams.values()) {
			if (!team.isEmpty()) active.add(team);
		}

		for (FWTeam team : active) {
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

	private void endMatch(FWTeam winner) {
		state = FWGameState.ENDING;
		applyMatchStats(winner);

		if (winner != null) {
			// Server-wide, not TellAll (which only reaches this match's own players) - a Flag Wars win is worth announcing to everyone.
			Bukkit.broadcastMessage(manager.getMain().color("&6&lFW> &e" + winningPlayerNames(winner) + " &rjust won on &e&l"
					+ mapName));
			playSoundForAll(Sound.LEVEL_UP, 1f, 0.7f);
			for (UUID uuid : winner.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) {
					TitleSequence.sendSequence(manager.getMain(), p,
							Collections.singletonList(new TitleSequence.TitleSpec("&e&lVICTORY", "&aYou won the game!", 10, 100, 20)));

					WinEffects winEffects = new WinEffects(p, new FWWinEffectsHost(this, p));
					winEffects.checkWinEffect();
					activeWinEffects.add(winEffects);
				}
			}
		} else {
			TellAll(manager.getMain().color("&6&lFW> &rFlag Wars ended with no winner."));
		}

		for (FWTeam team : teams.values()) {
			if (team == winner) continue;
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) p.setGameMode(GameMode.SPECTATOR);
			}
		}

		Bukkit.getScheduler().runTaskLater(manager.getMain(), () -> {
			for (WinEffects winEffects : activeWinEffects) {
				winEffects.removeWinEffects();
			}
			activeWinEffects.clear();
			resetAndClear();
		}, WIN_EFFECT_SECONDS * 20L);
	}

	/**
	 * Persists Wins/Losses/Winstreaks and the Match MVP award for everyone in the match - Kills,
	 * Deaths and Flags Captured are already tracked live as they happen (see addKill/handleDeath/
	 * addFlagCapture above), so this only needs to touch the stats that depend on how the match as
	 * a whole turned out. A true draw (winner == null) updates nobody's win/loss/streak.
	 */
	private static final int KILL_TOKENS = 1;
	private static final int KILL_EXP = 30;
	private static final int FLAG_CAPTURE_TOKENS = 2;
	private static final int FLAG_CAPTURE_EXP = 40;
	private static final int WIN_TOKENS = 10;
	private static final int WIN_EXP = 100;
	private static final int RANK_BONUS_TOKENS = 10;

	private void applyMatchStats(FWTeam winner) {
		UUID mvpUuid = topKillerUuid();
		Map<FWTeam, Integer> placements = teamPlacements();

		for (FWTeam team : teams.values()) {
			if (team.isEmpty()) continue;
			boolean won = team == winner;

			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p == null) continue;

				PlayerData data = playerDataOf(p);
				if (data == null) continue;

				// Battle Pass quests: they were on a team when the match ended
				addBattlePassXp(p, manager.getMain().battlePassQuest(p, anthony.SuperCraftBrawl.battlepass.BattlePassQuest.FLAG_WARS_PLAY, 1));
				if (won && winner != null)
					addBattlePassXp(p, manager.getMain().battlePassQuest(p, anthony.SuperCraftBrawl.battlepass.BattlePassQuest.FLAG_WARS_WIN, 1));

				if (winner != null) {
					if (won) {
						data.fwWins++;
						data.fwCurrentWinstreak++;
						data.fwBestWinstreak = Math.max(data.fwBestWinstreak, data.fwCurrentWinstreak);

						try {
							if (manager.getMain().snapshotDAO != null) {
								manager.getMain().snapshotDAO.recordPeriodWinstreak(
										uuid.toString(), data.fwCurrentWinstreak, "fw_period_winstreaks");
							}
						} catch (Throwable ignored) {
						}
					} else {
						data.fwLosses++;
						data.fwCurrentWinstreak = 0;
					}

					sendGameEndRewards(p, data, won, placements.getOrDefault(team, 1));
				}

				if (mvpUuid != null && uuid.equals(mvpUuid)) {
					data.fwMatchMvps++;
				}

				manager.getMain().getDataManager().saveData(data);
			}
		}
	}

	/**
	 * Every active team, ranked by total flags captured (most first) - the win condition already
	 * guarantees the winning team has captures >= everyone else, so they always land on #1. Ties
	 * are broken by total team kills, per the brief.
	 */
	private Map<FWTeam, Integer> teamPlacements() {
		List<FWTeam> ranked = new ArrayList<>();
		for (FWTeam team : teams.values()) {
			if (!team.isEmpty()) ranked.add(team);
		}

		ranked.sort((a, b) -> {
			int captureCompare = Integer.compare(b.getCapturedFlags(), a.getCapturedFlags());
			if (captureCompare != 0) return captureCompare;
			return Integer.compare(teamKills(b), teamKills(a));
		});

		Map<FWTeam, Integer> placements = new HashMap<>();
		for (int i = 0; i < ranked.size(); i++) {
			placements.put(ranked.get(i), i + 1);
		}
		return placements;
	}

	private int teamKills(FWTeam team) {
		int total = 0;
		for (UUID uuid : team.getMembers()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) total += getKills(p);
		}
		return total;
	}

	/**
	 * Mirrors SCB's own GameInstance (GAME WON) / BaseClass (GAME LOST) end-of-match reward
	 * breakdown verbatim - same bordered block, same "Placed #N" / Kills / Rank Bonus lines and
	 * the same closing "You earned X Tokens and Y EXP!" line - just with Flag Wars' own
	 * per-kill/per-capture/per-win rates and an added Flags Captured line neither SCB message
	 * has. Only sent when there's an actual winner - a true draw (winner == null, handled by the
	 * caller) doesn't fit either framing.
	 */
	private void sendGameEndRewards(Player player, PlayerData data, boolean won, int placement) {
		int kills = getKills(player);
		int captures = getFlagCaptures(player);

		int killTokens = kills * KILL_TOKENS;
		int killExp = kills * KILL_EXP;
		int captureTokens = captures * FLAG_CAPTURE_TOKENS;
		int captureExp = captures * FLAG_CAPTURE_EXP;
		int placeTokens = won ? WIN_TOKENS : 0;
		int placeExp = won ? WIN_EXP : 0;

		boolean rankBonus = won ? player.hasPermission("scb.rankBonusOne") : player.hasPermission("scb.rankBonus");
		int rankBonusTokens = rankBonus ? RANK_BONUS_TOKENS : 0;

		int totalTokens = killTokens + captureTokens + placeTokens + rankBonusTokens;
		int totalExp = killExp + captureExp + placeExp;

		String border = won ? "&l===========================" : "&l=====================";
		String title = won ? "&e&l  GAME WON" : "&c&l  GAME LOST";

		player.sendMessage(manager.getMain().color(border));
		player.sendMessage(manager.getMain().color("&l||"));
		player.sendMessage(manager.getMain().color("&l|| " + "        " + title));
		player.sendMessage(manager.getMain().color("&l||"));
		player.sendMessage(manager.getMain().color("&l|| " + "        &r  Placed #" + placement + ": &a" + placeTokens + " Tokens"));
		player.sendMessage(manager.getMain().color("&l|| " + "        &r  " + kills + " Kills: &a" + killTokens + " Tokens"));
		player.sendMessage(manager.getMain().color("&l|| " + "        &r  " + captures + " Flags Captured: &a" + captureTokens + " Tokens"));
		if (rankBonus) {
			player.sendMessage(manager.getMain().color("&l|| " + "        &r  Rank Bonus: &a" + RANK_BONUS_TOKENS + " Tokens"));
		}
		player.sendMessage(manager.getMain().color("&l||"));
		player.sendMessage(manager.getMain().color(border));
		player.sendMessage(manager.getMain().color("&2&l(!) &rYou earned &a" + totalTokens + " &rTokens and &a" + totalExp + " &rEXP!"));
		manager.getMain().sendBattlePassXpEarned(player, battlePassXp.getOrDefault(player.getUniqueId(), 0));

		data.tokens += totalTokens;
		data.exp += totalExp;
		manager.getMain().getLevelManager().checkLevelUp(player);
	}

	/** Whoever had the single highest kill count this match, or null if nobody got a kill. Ties just keep whichever was found first. */
	private UUID topKillerUuid() {
		UUID top = null;
		int best = 0;
		for (Map.Entry<UUID, Integer> entry : kills.entrySet()) {
			if (entry.getValue() > best) {
				best = entry.getValue();
				top = entry.getKey();
			}
		}
		return top;
	}

	/** Every online member of the winning team, joined for the victory message - just one name in Solos, "&PlayerA &r& &ePlayerB" in Duos. */
	private String winningPlayerNames(FWTeam winner) {
		StringBuilder names = new StringBuilder();
		for (UUID uuid : winner.getMembers()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p == null) continue;
			if (names.length() > 0) names.append("&r &e& &e");
			names.append(p.getName());
		}
		return names.toString();
	}

	/**
	 * Order matters here: players have to actually be out of the world before it can be
	 * unloaded (Bukkit.unloadWorld fails while anyone's still in it), and any leftover
	 * entities (dropped items, mobs, stray win-effect boats, etc.) should be swept before
	 * that unload too, so nothing is left holding a chunk open.
	 */
	private void resetAndClear() {
		resourceGenerator.stop();
		chestHologramManager.stop();
		trapManager.stop();
		flagManager.despawnAll();
		teamUpgradeManager.stop();
		scoreboardManager.removeAll();
		restoreWorld();

		sendPlayersHome();           // 1) remove all players from the world first
		clearNonPlayerEntities();    // 2) then sweep every remaining entity
		manager.releaseArenaWorld(this); // 3) unregister, unload, and (if this was a temp clone) delete it
	}

	/** Leftover mobs/NPCs/arrows/dropped items/etc. from the match - everything except players themselves. */
	private void clearNonPlayerEntities() {
		for (PokeballMob info : pokeballMobs.values()) info.cancelDespawn();
		pokeballMobs.clear();
		for (org.bukkit.entity.Entity entity : world.getEntities()) {
			if (!(entity instanceof Player)) {
				entity.remove();
			}
		}
	}

	private void sendPlayersHome() {
		for (FWTeam team : teams.values()) {
			team.setInactive(false);
			team.resetUpgrades();
			team.resetFlagProgress();

			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) resetAndSendHome(p);
			}
			team.getMembers().clear();
		}

		for (Player spectator : spectators) {
			resetAndSendHome(spectator);
		}
		spectators.clear();
	}

	/** Ender chest contents shouldn't carry over between matches - Flag Wars has no persistent loot. */
	private void resetAndSendHome(Player player) {
		player.getEnderChest().clear();

		// Toggling false then true (ResetPlayer below sets it back true) forces the client to
		// properly resync flight state - just leaving it true straight through from spectating breaks it.
		player.setAllowFlight(false);
		manager.getMain().ResetPlayer(player);
		manager.getMain().getListener().resetArmor(player); // ResetPlayer() only clears the main inventory, not armor slots
		manager.getMain().getListener().resetPotionEffects(player); // nor any Speed/Jump/Invisibility potions bought from the shop

		// Only meaningful for spectators (see addSpectator), but harmless no-ops for regular
		// players - same blanket re-show/re-collide SCB's own end-of-match cleanup does.
		player.spigot().setCollidesWithEntities(true);
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.showPlayer(player);
		}
	}

	private void restoreWorld() {
		placedBlocks.clear();
		for (int i = undoLog.size() - 1; i >= 0; i--) {
			undoLog.get(i).update(true, false);
		}
		undoLog.clear();
	}

	public void TellAll(String msg) {
		for (FWTeam team : teams.values()) {
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
