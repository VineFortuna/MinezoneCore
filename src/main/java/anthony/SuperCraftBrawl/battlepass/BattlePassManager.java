package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
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
	/** Cosmetics each online player has unlocked from the pass (kept forever, across seasons). */
	private final Map<UUID, Set<String>> unlocks = new ConcurrentHashMap<>();
	private final ExecutorService dbThread;
	private boolean enabled = false;
	/** Null if the quest table couldn't be created. The rest of the pass works without it. */
	private BattlePassQuestManager quests;

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

		// Quests are a separate table and their own switch: if it fails, only the quests are off
		try {
			quests = new BattlePassQuestManager(main, this);
		} catch (Throwable t) {
			quests = null;
			main.getLogger().severe("[BattlePass] Quests failed to start, Battle Pass quests are disabled: " + t);
		}

		main.getServer().getPluginManager().registerEvents(this, main);

		// Covers /reload while players are online
		for (Player online : Bukkit.getOnlinePlayers())
			load(online);
	}

	public boolean isEnabled() {
		return enabled;
	}

	/** Null if quests are off (the quest table failed to start), so always null-check. */
	public BattlePassQuestManager getQuests() {
		return quests;
	}

	/** Runs something on the Battle Pass database thread, so it lands in order with every other write. */
	void runDb(Runnable task) {
		try {
			dbThread.execute(task);
		} catch (RejectedExecutionException e) {
			// The plugin is shutting down, nothing more can be saved
		}
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
		unlocks.remove(event.getPlayer().getUniqueId());
		if (quests != null)
			quests.unload(event.getPlayer().getUniqueId());
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

		if (quests != null)
			quests.load(player);
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

	// ---------------------------------------------------------------- cosmetic unlocks

	/**
	 * Main thread only. Whether they unlocked this cosmetic through the Battle Pass.
	 * Loaded from MySQL the first time it's asked for (and then cached until they quit), instead
	 * of with the rest of their progress: the cosmetics system checks this on join to re-equip
	 * their cosmetics, which happens before the async pass load finishes.
	 */
	public boolean hasCosmetic(Player player, CosmeticCategory category, String cosmeticId) {
		return enabled && getUnlocks(player).contains(unlockKey(category, cosmeticId));
	}

	/** Main thread only. Permanently unlocks a cosmetic and saves it. */
	public void unlockCosmetic(Player player, CosmeticCategory category, String cosmeticId) {
		if (!enabled)
			return;

		String key = unlockKey(category, cosmeticId);
		if (!getUnlocks(player).add(key))
			return;

		UUID uuid = player.getUniqueId();
		dbThread.execute(() -> {
			try {
				dao.insertUnlock(uuid, BattlePassSeason.ID, key);
			} catch (SQLException e) {
				main.getLogger().warning("[BattlePass] Failed to save cosmetic unlock " + key + " for " + uuid + ": " + e.getMessage());
			}
		});
	}

	private static String unlockKey(CosmeticCategory category, String cosmeticId) {
		return category.name() + ":" + cosmeticId;
	}

	private Set<String> getUnlocks(Player player) {
		UUID uuid = player.getUniqueId();
		Set<String> keys = unlocks.get(uuid);
		if (keys != null)
			return keys;

		keys = ConcurrentHashMap.newKeySet();
		try {
			keys.addAll(dao.loadUnlocks(uuid));
		} catch (SQLException e) {
			main.getLogger().warning("[BattlePass] Failed to load cosmetic unlocks for " + uuid + ": " + e.getMessage());
			return keys; // not cached, so the next check tries the database again
		}

		// Only cache for players who are still online, or the entry would never be removed
		if (player.isOnline())
			unlocks.put(uuid, keys);
		return keys;
	}

	// ---------------------------------------------------------------- XP

	/**
	 * Main thread only. Call from game code - does nothing if the pass is off/out of season.
	 * Says nothing in chat by itself (the end-of-game summary reports the total, see Core#sendBattlePassXpEarned),
	 * except for the tier up announcement.
	 *
	 * @return the XP they actually got (with the Premium bonus, and cut off at the end of the pass): 0 if none,
	 *         for example because they already finished the whole pass
	 */
	public int addXp(Player player, int amount) {
		if (!enabled || amount <= 0 || !BattlePassSeason.isActive())
			return 0;

		BattlePassProgress progress = getProgress(player);
		if (progress == null || progress.xp >= BattlePassSeason.maxXp())
			return 0;

		int oldXp = progress.xp;
		int oldTier = progress.getTier();
		progress.xp = Math.min(BattlePassSeason.maxXp(), progress.xp + withPremiumBonus(progress, amount));
		int newTier = progress.getTier();
		save(player.getUniqueId(), progress);
		refreshLobbyBoard(player);

		if (newTier > oldTier)
			announceTierUp(player, newTier, progress);
		return progress.xp - oldXp;
	}

	/** What addXp would give right now for this amount, without giving it. 0 if it would give nothing. */
	public int previewXp(Player player, int amount) {
		if (!enabled || amount <= 0 || !BattlePassSeason.isActive())
			return 0;

		BattlePassProgress progress = getProgress(player);
		if (progress == null || progress.xp >= BattlePassSeason.maxXp())
			return 0;
		return Math.min(BattlePassSeason.maxXp() - progress.xp, withPremiumBonus(progress, amount));
	}

	/** Premium Pass owners earn extra XP. */
	private int withPremiumBonus(BattlePassProgress progress, int amount) {
		if (hasPremium(progress))
			return (int) Math.round(amount * BattlePassSeason.PREMIUM_XP_MULTIPLIER);
		return amount;
	}

	/** The tier up announcement and sound. */
	private void announceTierUp(Player player, int tier, BattlePassProgress progress) {
		BattlePassMessages.sendTierUp(main, player, tier, hasPremium(progress));
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.3f);
	}

	/**
	 * Admin/testing: moves a player one tier up (delta 1) or down (delta -1) by setting their XP to the
	 * start of that tier. Going up shows the normal tier up announcement. Rewards they already
	 * claimed stay claimed. Returns the new tier (unchanged at the first/last tier), or -1 if
	 * their progress hasn't loaded.
	 */
	public int shiftTier(Player player, int delta) {
		BattlePassProgress progress = getProgress(player);
		if (!enabled || progress == null)
			return -1;

		int oldTier = progress.getTier();
		int newTier = Math.max(0, Math.min(BattlePassSeason.MAX_TIER, oldTier + delta));
		if (newTier == oldTier)
			return newTier;

		progress.xp = newTier * BattlePassSeason.XP_PER_TIER;
		save(player.getUniqueId(), progress);
		refreshLobbyBoard(player);

		if (newTier > oldTier)
			announceTierUp(player, newTier, progress);
		return newTier;
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
		// Empty (not removed): reloading now could read the old rows before the queued delete runs
		unlocks.put(uuid, ConcurrentHashMap.<String>newKeySet());
		dbThread.execute(() -> {
			try {
				dao.reset(uuid, BattlePassSeason.ID);
			} catch (SQLException e) {
				main.getLogger().warning("[BattlePass] Failed to reset " + uuid + ": " + e.getMessage());
			}
		});
		if (quests != null)
			quests.reset(player);
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
