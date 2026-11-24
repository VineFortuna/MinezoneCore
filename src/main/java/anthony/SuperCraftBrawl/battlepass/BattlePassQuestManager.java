package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Battle Pass quests. Only Premium Pass owners can do them.
 * <p>
 * Every quest in {@link BattlePassQuest} is open for the whole season and can be finished once, whenever. Progress is
 * loaded when a player joins and kept in memory until they quit, and every change is written straight to MySQL on the
 * Battle Pass database thread (see BattlePassManager). Nothing here holds a Player, a game or a world, only UUIDs and
 * numbers.
 * <p>
 * All the progress and read methods are main thread only. Game code should call them through Core#battlePassQuest
 * and Core#battlePassQuestReach, which are safe when the Battle Pass is off.
 */
public class BattlePassQuestManager {

	/** One quest's saved state for one player. */
	private static final class QuestState {
		int progress;
		boolean completed;
	}

	private final Core main;
	private final BattlePassManager manager;
	private final BattlePassQuestDAO dao;
	private final Map<UUID, Map<BattlePassQuest, QuestState>> cache = new ConcurrentHashMap<>();

	/** @throws SQLException if the table can't be created. The caller then runs the pass without quests. */
	public BattlePassQuestManager(Core main, BattlePassManager manager) throws SQLException {
		this.main = main;
		this.manager = manager;
		this.dao = new BattlePassQuestDAO(main);
		dao.ensureTable();
	}

	// ---------------------------------------------------------------- loading

	/** Loads a player's quest progress in the background. Called from BattlePassManager when they join. */
	public void load(Player player) {
		final UUID uuid = player.getUniqueId();

		manager.runDb(new Runnable() {
			@Override
			public void run() {
				try {
					final Map<String, int[]> rows = dao.load(uuid, BattlePassSeason.ID);
					Bukkit.getScheduler().runTask(main, new Runnable() {
						@Override
						public void run() {
							Player p = Bukkit.getPlayer(uuid);
							if (p == null || !p.isOnline())
								return; // quit while loading: don't cache anything for them

							Map<BattlePassQuest, QuestState> states = new EnumMap<>(BattlePassQuest.class);
							for (Map.Entry<String, int[]> row : rows.entrySet()) {
								BattlePassQuest quest = BattlePassQuest.byId(row.getKey());
								if (quest == null)
									continue;
								QuestState state = new QuestState();
								state.progress = row.getValue()[0];
								state.completed = row.getValue()[1] == 1;
								states.put(quest, state);
							}
							cache.put(uuid, states);
						}
					});
				} catch (Exception e) {
					main.getLogger().warning("[BattlePass] Failed to load quests for " + uuid + ": " + e.getMessage());
				}
			}
		});
	}

	/** Called when a player quits. Everything is already saved. */
	public void unload(UUID uuid) {
		cache.remove(uuid);
	}

	/** Admin/testing: forgets their quest progress for the season. The caller reloads them afterwards. */
	public void reset(Player player) {
		final UUID uuid = player.getUniqueId();
		cache.remove(uuid);
		manager.runDb(new Runnable() {
			@Override
			public void run() {
				try {
					dao.reset(uuid, BattlePassSeason.ID);
				} catch (SQLException e) {
					main.getLogger().warning("[BattlePass] Failed to reset quests for " + uuid + ": " + e.getMessage());
				}
			}
		});
	}

	// ---------------------------------------------------------------- reading a player's progress

	/** Whether this player can do quests right now: Premium, in season, and their quests have loaded. */
	public boolean canUse(Player player) {
		if (!manager.isEnabled() || !BattlePassSeason.isActive())
			return false;
		BattlePassProgress progress = manager.getProgress(player);
		return progress != null && manager.hasPremium(progress) && cache.containsKey(player.getUniqueId());
	}

	public int getProgress(Player player, BattlePassQuest quest) {
		QuestState state = stateOf(player, quest);
		return state == null ? 0 : state.progress;
	}

	public boolean isCompleted(Player player, BattlePassQuest quest) {
		QuestState state = stateOf(player, quest);
		return state != null && state.completed;
	}

	public int getCompletedCount(Player player) {
		int count = 0;
		for (BattlePassQuest quest : BattlePassQuest.values()) {
			if (isCompleted(player, quest))
				count++;
		}
		return count;
	}

	private QuestState stateOf(Player player, BattlePassQuest quest) {
		Map<BattlePassQuest, QuestState> states = cache.get(player.getUniqueId());
		return states == null ? null : states.get(quest);
	}

	// ---------------------------------------------------------------- making progress

	/**
	 * Adds to a quest's progress (catching a fish, finishing a game). Does nothing if they can't do quests.
	 *
	 * @return the Battle Pass XP this earned them (0 unless it finished the quest), so a game can count it in its summary
	 */
	public int progress(Player player, BattlePassQuest quest, int amount) {
		return update(player, quest, amount, false, false);
	}

	/**
	 * Sets a quest's progress to at least this value (kills in one game). Does nothing if they can't do quests.
	 *
	 * @return the Battle Pass XP this earned them (0 unless it finished the quest)
	 */
	public int reach(Player player, BattlePassQuest quest, int value) {
		return update(player, quest, value, true, false);
	}

	/**
	 * Admin/testing: finishes a quest right now, even for someone without the Premium Pass.
	 * Returns false if their quests haven't loaded.
	 */
	public boolean forceComplete(Player player, BattlePassQuest quest) {
		if (!cache.containsKey(player.getUniqueId()))
			return false;
		update(player, quest, quest.getGoal(), true, true);
		return true;
	}

	private int update(Player player, BattlePassQuest quest, int value, boolean best, boolean force) {
		if (player == null || !manager.isEnabled())
			return 0;
		if (!force && !canUse(player))
			return 0;

		Map<BattlePassQuest, QuestState> states = cache.get(player.getUniqueId());
		if (states == null)
			return 0;

		QuestState state = states.get(quest);
		if (state == null) {
			state = new QuestState();
			states.put(quest, state);
		}
		if (state.completed)
			return 0;

		int newProgress = best ? Math.max(state.progress, value) : state.progress + value;
		newProgress = Math.min(newProgress, quest.getGoal());
		if (newProgress == state.progress)
			return 0;

		state.progress = newProgress;
		boolean done = newProgress >= quest.getGoal();
		if (done)
			state.completed = true; // marked before the reward, so it can never be paid twice
		save(player.getUniqueId(), quest, state);

		return done ? complete(player, quest) : 0;
	}

	/** Tells them, and pays the XP. Returns the XP they got. */
	private int complete(Player player, BattlePassQuest quest) {
		// Nothing else says how much XP a quest gave, so it goes in this message (nothing once the pass is finished)
		int gain = manager.previewXp(player, BattlePassSeason.QUEST_XP);
		player.sendMessage(main.color("&2&l(!) &rQuest complete: &6" + quest.getName()
				+ (gain > 0 ? " &a+" + gain + " XP" : "")));
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 0.8f, 1.7f);
		return manager.addXp(player, BattlePassSeason.QUEST_XP);
	}

	private void save(UUID uuid, BattlePassQuest quest, QuestState state) {
		// Snapshot on the main thread so the DB thread never reads a half-updated object
		final String questId = quest.getId();
		final int progress = state.progress;
		final boolean completed = state.completed;
		final UUID savedUuid = uuid;

		manager.runDb(new Runnable() {
			@Override
			public void run() {
				try {
					dao.save(savedUuid, BattlePassSeason.ID, questId, progress, completed);
				} catch (SQLException e) {
					main.getLogger().warning("[BattlePass] Failed to save quest " + questId + " for " + savedUuid + ": " + e.getMessage());
				}
			}
		});
	}
}
