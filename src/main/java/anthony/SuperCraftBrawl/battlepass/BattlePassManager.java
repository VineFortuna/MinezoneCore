package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Owns Battle Pass progress for online players.
 * Progress is loaded on join, cached in memory, and every change is written straight to MySQL.
 * All DB work runs on one background thread so writes for a player always land in order.
 * If the tables can't be created, the Battle Pass just disables itself - nothing else is affected.
 */
public class BattlePassManager implements Listener {

	public enum ClaimResult {
		SUCCESS, NOT_LOADED, SEASON_INACTIVE, TIER_LOCKED, NEEDS_PREMIUM, ALREADY_CLAIMED
	}

	private final Core main;
	private final BattlePassDAO dao;
	private final Map<UUID, BattlePassProgress> cache = new ConcurrentHashMap<>();
	private final ExecutorService dbThread;
	private boolean enabled = false;

	public BattlePassManager(Core main) {
		this.main = main;
		this.dao = new BattlePassDAO(main);
		this.dbThread = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "MinezoneCore-BattlePass-DB");
				thread.setDaemon(true);
				return thread;
			}
		});

		try {
			dao.ensureTables();
			enabled = true;
		} catch (Exception e) {
			main.getLogger().severe("[BattlePass] Could not create Battle Pass tables, Battle Pass is disabled: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		main.getServer().getPluginManager().registerEvents(this, main);

		// Covers /reload while players are online
		for (Player online : Bukkit.getOnlinePlayers())
			load(online);
	}

	public boolean isEnabled() {
		return enabled;
	}

	// ---------------------------------------------------------------- loading

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		load(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		// Nothing to save: every change is already written when it happens.
		cache.remove(event.getPlayer().getUniqueId());
	}

	private void load(Player player) {
		UUID uuid = player.getUniqueId();
		dbThread.execute(() -> {
			try {
				BattlePassProgress progress = dao.load(uuid, BattlePassSeason.ID);
				Bukkit.getScheduler().runTask(main, () -> {
					Player p = Bukkit.getPlayer(uuid);
					if (p != null && p.isOnline()) {
						cache.put(uuid, progress);
						refreshLobbyBoard(p);
					}
				});
			} catch (Exception e) {
				main.getLogger().warning("[BattlePass] Failed to load progress for " + uuid + ": " + e.getMessage());
			}
		});
	}

	/** Null if the pass is disabled or their data hasn't finished loading yet. */
	public BattlePassProgress getProgress(Player player) {
		return cache.get(player.getUniqueId());
	}

	/** Only redraws the lobby board if they're on it, so in-game scoreboards are left alone. */
	private void refreshLobbyBoard(Player player) {
		if (main.getScoreboardManager().hasLobbyBoard(player))
			main.getScoreboardManager().lobbyBoard(player);
	}

	public boolean hasPremium(BattlePassProgress progress) {
		return BattlePassSeason.PREMIUM_FREE_FOR_TESTING || progress.premium;
	}

	// ---------------------------------------------------------------- XP

	/** Main thread only. Call from game code - does nothing if the pass is off/out of season. */
	public void addXp(Player player, int amount) {
		if (!enabled || amount <= 0 || !BattlePassSeason.isActive())
			return;

		BattlePassProgress progress = getProgress(player);
		if (progress == null || progress.xp >= BattlePassSeason.maxXp())
			return;

		int oldTier = progress.getTier();
		progress.xp = Math.min(BattlePassSeason.maxXp(), progress.xp + amount);
		int newTier = progress.getTier();
		save(player.getUniqueId(), progress);
		refreshLobbyBoard(player);

		player.sendMessage(main.color("&6&l(!) &r+" + amount + " &6Battle Pass XP"));

		if (newTier > oldTier) {
			player.sendMessage(main.color("&8&m---------------------------"));
			player.sendMessage(main.color("&6&l✦✦ &e&lBATTLE PASS TIER UP! &6&l✦✦"));
			player.sendMessage(main.color("&rYou reached &eTier " + newTier + "&r! Type &e/battlepass &rto claim your rewards."));
			player.sendMessage(main.color("&8&m---------------------------"));
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.3f);
		}
	}

	public void setXp(Player player, int xp) {
		BattlePassProgress progress = getProgress(player);
		if (progress == null)
			return;
		progress.xp = Math.max(0, Math.min(BattlePassSeason.maxXp(), xp));
		save(player.getUniqueId(), progress);
		refreshLobbyBoard(player);
	}

	public void setPremium(Player player, boolean premium) {
		BattlePassProgress progress = getProgress(player);
		if (progress == null)
			return;
		progress.premium = premium;
		save(player.getUniqueId(), progress);
	}

	private void save(UUID uuid, BattlePassProgress progress) {
		// Snapshot on the main thread so the DB thread never reads a half-updated object
		final int xp = progress.xp;
		final boolean premium = progress.premium;
		dbThread.execute(() -> {
			try {
				dao.saveProgress(uuid, BattlePassSeason.ID, xp, premium);
			} catch (SQLException e) {
				main.getLogger().warning("[BattlePass] Failed to save progress for " + uuid + ": " + e.getMessage());
			}
		});
	}

	// ---------------------------------------------------------------- claiming

	/** Main thread only. */
	public ClaimResult claim(Player player, BattlePassTrack track, int tier) {
		BattlePassProgress progress = getProgress(player);
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (progress == null || data == null)
			return ClaimResult.NOT_LOADED;
		if (!BattlePassSeason.isActive())
			return ClaimResult.SEASON_INACTIVE;
		if (tier < 1 || tier > progress.getTier())
			return ClaimResult.TIER_LOCKED;
		if (track == BattlePassTrack.PREMIUM && !hasPremium(progress))
			return ClaimResult.NEEDS_PREMIUM;
		if (progress.hasClaimed(track, tier))
			return ClaimResult.ALREADY_CLAIMED;

		// Mark first so a double-click can never grant it twice
		progress.getClaimed(track).add(tier);
		BattlePassRewards.get(track, tier).grant(main, player, data);
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
		refreshLobbyBoard(player); // tokens/levels may have changed

		UUID uuid = player.getUniqueId();
		dbThread.execute(() -> {
			try {
				dao.insertClaim(uuid, BattlePassSeason.ID, track, tier);
			} catch (SQLException e) {
				main.getLogger().warning("[BattlePass] Failed to save claim " + track + " tier " + tier + " for " + uuid + ": " + e.getMessage());
			}
		});
		return ClaimResult.SUCCESS;
	}

	/** Wipes their season progress in the DB and reloads them (admin/testing). */
	public void reset(Player player) {
		UUID uuid = player.getUniqueId();
		cache.remove(uuid);
		dbThread.execute(() -> {
			try {
				dao.reset(uuid, BattlePassSeason.ID);
			} catch (SQLException e) {
				main.getLogger().warning("[BattlePass] Failed to reset " + uuid + ": " + e.getMessage());
			}
		});
		load(player); // queued after the reset on the same thread
	}

	/** Call from onDisable so pending writes finish before the plugin goes away. */
	public void shutdown() {
		dbThread.shutdown();
		try {
			if (!dbThread.awaitTermination(5, TimeUnit.SECONDS))
				main.getLogger().warning("[BattlePass] Some Battle Pass writes did not finish before shutdown.");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
