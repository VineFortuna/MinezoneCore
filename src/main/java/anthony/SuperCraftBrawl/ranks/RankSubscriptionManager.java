package anthony.SuperCraftBrawl.ranks;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Timed ranks bought as a subscription on the store (LEGENDARY monthly or yearly).
 *
 * The store only has to run "/setrank <player> LEGENDARY 1m" (or 1y, or 28d; "/ranksub give" does the same) when a payment
 * goes through, the first one and every renewal. Everything else happens here: each payment pushes the paid-through date forward, the rank
 * is switched on, and once the paid time (plus a short grace for a late renewal) runs out the player gets the rank
 * they had before back, so VIP + a month of LEGENDARY ends as VIP again. Cancelling needs no command at all: the
 * renewals just stop coming.
 *
 * Everything is kept in its own table, one row per subscriber, so a restart, a crash or a player who is offline can't
 * lose track of it. All database work runs on one background thread; ranks of online players are changed on the main
 * thread through the RankManager, ranks of offline players straight in the PlayerData table. If the table can't be
 * created the manager just disables itself and nothing else is affected.
 */
public class RankSubscriptionManager implements Listener {

	private static final String TABLE = "scb_rank_subscriptions";
	/** A renewal that lands this long after the paid time ran out still continues from the old date, and the rank stays meanwhile. */
	private static final long GRACE_MS = TimeUnit.DAYS.toMillis(2);
	private static final long SWEEP_PERIOD_TICKS = 20L * 30;
	private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

	/** The ranks that can be a subscription. */
	private static final Set<Rank> SUBSCRIBABLE = EnumSet.of(Rank.VIP, Rank.PRO, Rank.LEGENDARY);

	private final Core main;
	private final ExecutorService dbThread;
	private BukkitTask sweepTask;
	private boolean enabled = false;

	/** How long one payment lasts: calendar months plus days, so "1m" matches a monthly bill instead of a flat 30 days. */
	public static final class Period {
		private final int months;
		private final int days;
		/** What was typed ("1m"), for the log line that says what to run by hand. */
		private final String spec;

		private Period(int months, int days, String spec) {
			this.months = months;
			this.days = days;
			this.spec = spec;
		}

		long applyTo(long millis) {
			return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).plusMonths(months).plusDays(days).toInstant()
					.toEpochMilli();
		}

		@Override
		public String toString() {
			StringBuilder text = new StringBuilder();
			if (months > 0)
				text.append(months).append(months == 1 ? " month" : " months");
			if (days > 0)
				text.append(text.length() > 0 ? " " : "").append(days).append(days == 1 ? " day" : " days");
			return text.toString();
		}
	}

	private static final Pattern PERIOD = Pattern.compile("(\\d{1,4})([dwmy]?)", Pattern.CASE_INSENSITIVE);

	/** "30d", "2w", "1m", "12m", "1y" or a plain number of days. Null if it isn't one, or is zero. */
	public static Period parsePeriod(String text) {
		Matcher m = PERIOD.matcher(text);
		if (!m.matches())
			return null;
		int amount = Integer.parseInt(m.group(1));
		if (amount <= 0)
			return null;
		switch (m.group(2).toLowerCase()) {
			case "m":
				return new Period(amount, 0, text);
			case "y":
				return new Period(amount * 12, 0, text);
			case "w":
				return new Period(0, amount * 7, text);
			default:
				return new Period(0, amount, text);
		}
	}

	private static class Sub {
		final UUID uuid;
		String name;
		final int rankId;
		int previousId;
		long expiresAt;

		Sub(UUID uuid, String name, int rankId, int previousId, long expiresAt) {
			this.uuid = uuid;
			this.name = name;
			this.rankId = rankId;
			this.previousId = previousId;
			this.expiresAt = expiresAt;
		}

		boolean isOver(long now) {
			return now > expiresAt + GRACE_MS;
		}
	}

	private static class PlayerRow {
		final UUID uuid;
		final String name;
		final int roleId;

		PlayerRow(UUID uuid, String name, int roleId) {
			this.uuid = uuid;
			this.name = name;
			this.roleId = roleId;
		}
	}

	public RankSubscriptionManager(Core main) {
		this.main = main;
		this.dbThread = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "MinezoneCore-RankSubscriptions-DB");
				thread.setDaemon(true);
				return thread;
			}
		});

		try {
			ensureTable();
			enabled = true;
		} catch (Exception e) {
			main.getLogger().severe("[RankSubscriptions] Could not create the table, rank subscriptions are disabled: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		main.getServer().getPluginManager().registerEvents(this, main);
		// Ends the ones that ran out, also for players who are offline (and the ones that ran out while the server was down)
		sweepTask = Bukkit.getScheduler().runTaskTimer(main, new Runnable() {
			@Override
			public void run() {
				runDb(new Runnable() {
					@Override
					public void run() {
						sweep();
					}
				});
			}
		}, 100L, SWEEP_PERIOD_TICKS);
	}

	public boolean isEnabled() {
		return enabled;
	}

	/** Call from onDisable so pending writes finish before the plugin goes away. */
	public void shutdown() {
		if (sweepTask != null)
			sweepTask.cancel();
		dbThread.shutdown();
		try {
			if (!dbThread.awaitTermination(5, TimeUnit.SECONDS))
				dbThread.shutdownNow();
		} catch (InterruptedException e) {
			dbThread.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	// ---------------------------------------------------------------- what the store and staff use

	/**
	 * One more payment for this player: extends their subscription by the period (starting it if they have none) and
	 * switches the rank on. Works for players who are offline. Main thread.
	 */
	public void give(final CommandSender sender, final String name, final Rank rank, final Period period) {
		if (!ready(sender, name))
			return;
		if (!SUBSCRIBABLE.contains(rank)) {
			tell(sender, "&c&l(!) &r" + rank.name() + " can't be a subscription. Use VIP, PRO or LEGENDARY.");
			return;
		}

		// What only the main thread can read: whether they're online and which rank they hold right now
		final Player online = Bukkit.getPlayerExact(name);
		final PlayerData onlineData = online == null ? null : main.getDataManager().getPlayerData(online);
		if (online != null && onlineData == null) {
			fail(sender, "&c&l(!) &r" + name + "'s data is still loading, run it again in a moment.", "give " + name + " " + rank + " " + period.spec);
			return;
		}
		final UUID onlineId = online == null ? null : online.getUniqueId();
		final String onlineName = online == null ? null : online.getName();
		final int onlineRole = onlineData == null ? 0 : onlineData.roleID;

		runDb(new Runnable() {
			@Override
			public void run() {
				try {
					UUID uuid;
					String realName;
					int currentRole;
					if (onlineId != null) {
						uuid = onlineId;
						realName = onlineName;
						currentRole = onlineRole;
					} else {
						PlayerRow row = findPlayer(name);
						if (row == null) {
							fail(sender, "&c&l(!) &r" + name + " has never joined, so there is no one to give it to.", "give " + name + " " + rank + " " + period.spec);
							return;
						}
						uuid = row.uuid;
						realName = row.name;
						currentRole = row.roleId;
					}

					long now = System.currentTimeMillis();
					Sub sub = readSub(uuid);
					if (sub != null && sub.rankId != rank.getRoleID()) {
						tell(sender, "&c&l(!) &r" + realName + " already has a " + Rank.getRankFromID(sub.rankId).name()
								+ " subscription. End it first with /ranksub end " + realName + ".");
						return;
					}
					// What they hold today is what they go back to: when there's a row already, that was recorded
					// then (the rank they hold now is the subscription's own)
					if (sub == null)
						sub = new Sub(uuid, realName, rank.getRoleID(), currentRole, 0);

					// Paying again before it runs out (or a bit late, within the grace) continues from the old date
					long base = sub.expiresAt != 0 && !sub.isOver(now) ? sub.expiresAt : now;
					sub.name = realName;
					sub.expiresAt = period.applyTo(base);
					writeSub(sub);

					final Sub saved = sub;
					final String finalName = realName;
					onMain(new Runnable() {
						@Override
						public void run() {
							applyActive(saved, true, 3);
							sender.sendMessage(main.color("&a&l(!) &r" + finalName + " now has " + rank.getTag()
									+ main.color("&r until &e" + formatDate(saved.expiresAt) + " &r(+" + period + "), then goes back to &e"
									+ Rank.getRankFromID(saved.previousId).name() + "&r.")));
						}
					});
				} catch (SQLException e) {
					fail(sender, "&c&l(!) &rDatabase error: " + e.getMessage(), "give " + name + " " + rank + " " + period.spec);
				}
			}
		});
	}

	/**
	 * A payment as it is typed: "/setrank <player> <rank> <time>" (or "/ranksub give ..."). Checks the rank and the time
	 * (1m, 12m, 1y, 28d, 2w or a plain number of days) and tells the sender what is wrong with them, then gives it. Main thread.
	 */
	public void giveFromCommand(CommandSender sender, String name, String rankName, String timeText) {
		Rank rank = Rank.getRankFromName(rankName);
		// getRankFromName falls back to DEFAULT for a name it doesn't know
		if (rank == Rank.DEFAULT && !rankName.equalsIgnoreCase("default")) {
			sender.sendMessage(main.color("&c&l(!) &r" + rankName + " is not a rank."));
			return;
		}
		Period period = parsePeriod(timeText);
		if (period == null) {
			sender.sendMessage(main.color("&c&l(!) &r" + timeText + " is not a time. Use 1m, 12m, 1y, 28d, 2w or a number of days."));
			return;
		}
		give(sender, name, rank, period);
	}

	/** Ends their subscription right now (refund, chargeback, staff): they go back to the rank they had before. Main thread. */
	public void end(final CommandSender sender, final String name) {
		if (!ready(sender, name))
			return;
		final Player online = Bukkit.getPlayerExact(name);
		final UUID onlineId = online == null ? null : online.getUniqueId();

		runDb(new Runnable() {
			@Override
			public void run() {
				try {
					UUID uuid = onlineId;
					if (uuid == null) {
						PlayerRow row = findPlayer(name);
						uuid = row == null ? null : row.uuid;
					}
					Sub sub = uuid == null ? null : readSub(uuid);
					if (sub == null) {
						tell(sender, "&c&l(!) &r" + name + " has no subscription.");
						return;
					}
					finish(sub, sender);
				} catch (SQLException e) {
					fail(sender, "&c&l(!) &rDatabase error: " + e.getMessage(), "end " + name);
				}
			}
		});
	}

	/** Tells the sender what subscription the player has. Main thread. */
	public void info(final CommandSender sender, final String name) {
		if (!ready(sender, name))
			return;
		final Player online = Bukkit.getPlayerExact(name);
		final UUID onlineId = online == null ? null : online.getUniqueId();

		runDb(new Runnable() {
			@Override
			public void run() {
				try {
					UUID uuid = onlineId;
					if (uuid == null) {
						PlayerRow row = findPlayer(name);
						uuid = row == null ? null : row.uuid;
					}
					Sub sub = uuid == null ? null : readSub(uuid);
					if (sub == null) {
						tell(sender, "&e" + name + " &rhas no rank subscription.");
						return;
					}
					long now = System.currentTimeMillis();
					String state = now <= sub.expiresAt ? "&aactive" : sub.isOver(now) ? "&cover (ends on the next check)" : "&6late, in the grace period";
					tell(sender, "&6&lSUBSCRIPTION &e" + sub.name);
					tell(sender, "&7Rank: &r" + Rank.getRankFromID(sub.rankId).getTag() + " &7(" + state + "&7)");
					tell(sender, "&7Paid through: &e" + formatDate(sub.expiresAt));
					tell(sender, "&7Goes back to: &e" + Rank.getRankFromID(sub.previousId).name());
				} catch (SQLException e) {
					fail(sender, "&c&l(!) &rDatabase error: " + e.getMessage(), "info " + name);
				}
			}
		});
	}

	/**
	 * /setrank goes through here first. Someone with a subscription running holds its rank, so setting VIP or PRO (a
	 * store purchase while they're subscribed, say) must not replace it: it becomes the rank they go back to. Any other
	 * rank (staff, or the subscription's own rank for good) takes effect right away and ends the subscription's
	 * bookkeeping. With no subscription, normalSetRank runs. Main thread; normalSetRank runs on the main thread too.
	 */
	public void setRankOrDefer(final CommandSender sender, final String name, final Rank rank, final Runnable normalSetRank) {
		if (!enabled || !NAME.matcher(name).matches()) {
			normalSetRank.run();
			return;
		}
		final Player online = Bukkit.getPlayerExact(name);
		final UUID onlineId = online == null ? null : online.getUniqueId();

		runDb(new Runnable() {
			@Override
			public void run() {
				try {
					UUID uuid = onlineId;
					if (uuid == null) {
						PlayerRow row = findPlayer(name);
						uuid = row == null ? null : row.uuid;
					}
					Sub sub = uuid == null ? null : readSub(uuid);
					if (sub != null) {
						boolean becomesTheRankAfter = rank == Rank.DEFAULT || rank == Rank.VIP || rank == Rank.PRO;
						if (becomesTheRankAfter && rank.getRoleID() != sub.rankId) {
							sub.previousId = rank.getRoleID();
							writeSub(sub);
							tell(sender, "&e&l(!) &r" + sub.name + " has a " + Rank.getRankFromID(sub.rankId).name()
									+ " subscription until &e" + formatDate(sub.expiresAt) + "&r, so they keep it. Their rank after it ends is now &e"
									+ rank.name() + "&r. To end it now: /ranksub end " + sub.name);
							return;
						}
						deleteSub(uuid); // the rank takes effect now and nothing is given back later
					}
				} catch (SQLException e) {
					main.getLogger().warning("[RankSubscriptions] Could not check " + name + " before /setrank: " + e.getMessage());
				}
				onMain(normalSetRank);
			}
		});
	}

	// ---------------------------------------------------------------- applying it

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		// Switches the rank back on if a crash lost it, and ends the subscription if it ran out while they were away
		Bukkit.getScheduler().runTaskLater(main, new Runnable() {
			@Override
			public void run() {
				if (Bukkit.getPlayer(uuid) == null)
					return;
				runDb(new Runnable() {
					@Override
					public void run() {
						try {
							Sub sub = readSub(uuid);
							if (sub == null)
								return;
							if (sub.isOver(System.currentTimeMillis()))
								finish(sub, null);
							else
								onMain(applyAction(sub, 3));
						} catch (SQLException e) {
							main.getLogger().warning("[RankSubscriptions] Could not check " + uuid + " on join: " + e.getMessage());
						}
					}
				});
			}
		}, 20L);
	}

	private Runnable applyAction(final Sub sub, final int triesLeft) {
		return new Runnable() {
			@Override
			public void run() {
				applyActive(sub, false, triesLeft);
			}
		};
	}

	/** The subscription is running: they must hold its rank. Main thread. */
	private void applyActive(final Sub sub, final boolean announce, final int triesLeft) {
		Player player = Bukkit.getPlayer(sub.uuid);
		if (player == null) {
			final int subRole = sub.rankId;
			final UUID uuid = sub.uuid;
			runDb(new Runnable() {
				@Override
				public void run() {
					try {
						setStoredRoleIfSwappable(uuid, subRole);
					} catch (SQLException e) {
						main.getLogger().warning("[RankSubscriptions] Could not switch on the rank of " + uuid + ": " + e.getMessage());
					}
				}
			});
			return;
		}

		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) {
			// Still loading: look again a moment later
			if (triesLeft > 0)
				Bukkit.getScheduler().runTaskLater(main, new Runnable() {
					@Override
					public void run() {
						applyActive(sub, announce, triesLeft - 1);
					}
				}, 40L);
			return;
		}

		Rank rank = Rank.getRankFromID(sub.rankId);
		// Staff and event ranks are never replaced, and someone who already holds it has nothing to switch
		if (data.roleID != sub.rankId && isSwappable(data.roleID))
			main.getRankManager().setRank(player, rank);
		if (announce)
			player.sendMessage(main.color("&6&l(!) &rYour ") + rank.getTag() + main.color(" &rrank is active until &e"
					+ formatDate(sub.expiresAt) + "&r. Thank you for supporting Minezone!"));
	}

	/**
	 * Ends the subscription for good: whoever still holds its rank goes back to the rank they had before, then the row
	 * is deleted. Runs on the database thread and hops to the main thread for online players. If they're online with
	 * their data not loaded yet, nothing is done and the next check tries again.
	 */
	private void finish(final Sub sub, final CommandSender replyTo) {
		onMain(new Runnable() {
			@Override
			public void run() {
				final Rank back = Rank.getRankFromID(sub.previousId);
				Player player = Bukkit.getPlayer(sub.uuid);

				if (player != null) {
					PlayerData data = main.getDataManager().getPlayerData(player);
					if (data == null) {
						if (replyTo != null)
							replyTo.sendMessage(main.color("&c&l(!) &r" + sub.name + "'s data is still loading, run it again in a moment."));
						return;
					}
					if (data.roleID == sub.rankId && sub.previousId != sub.rankId) {
						main.getRankManager().setRank(player, back);
						player.sendMessage(main.color("&c&l(!) &rYour ") + Rank.getRankFromID(sub.rankId).getTag()
								+ main.color(" &rsubscription has ended. You're back to "
								+ (back == Rank.DEFAULT ? "the default rank" : back.getTag() + main.color("&r"))
								+ "&r. Use &e/store &rto get it back!"));
					}
					runDb(new Runnable() {
						@Override
						public void run() {
							try {
								deleteSub(sub.uuid);
							} catch (SQLException e) {
								main.getLogger().warning("[RankSubscriptions] Could not delete the subscription of " + sub.name + ": " + e.getMessage());
							}
						}
					});
				} else {
					runDb(new Runnable() {
						@Override
						public void run() {
							try {
								if (sub.previousId != sub.rankId)
									setStoredRoleIfCurrent(sub.uuid, sub.rankId, sub.previousId);
								deleteSub(sub.uuid);
							} catch (SQLException e) {
								main.getLogger().warning("[RankSubscriptions] Could not end the subscription of " + sub.name + ": " + e.getMessage());
							}
						}
					});
				}

				if (replyTo != null)
					replyTo.sendMessage(main.color("&a&l(!) &rEnded " + sub.name + "'s " + Rank.getRankFromID(sub.rankId).name()
							+ " subscription. They are back to &e" + back.name() + "&r."));
			}
		});
	}

	/** Database thread. */
	private void sweep() {
		try {
			for (Sub sub : readOver(System.currentTimeMillis()))
				finish(sub, null);
		} catch (SQLException e) {
			main.getLogger().warning("[RankSubscriptions] Could not check for ended subscriptions: " + e.getMessage());
		}
	}

	// ---------------------------------------------------------------- small helpers

	private static boolean isSwappable(int roleId) {
		return roleId == Rank.DEFAULT.getRoleID() || roleId == Rank.VIP.getRoleID() || roleId == Rank.PRO.getRoleID();
	}

	private boolean ready(CommandSender sender, String name) {
		if (!enabled) {
			sender.sendMessage(main.color("&c&l(!) &rRank subscriptions are unavailable, the database table didn't start."));
			return false;
		}
		if (!NAME.matcher(name).matches()) {
			sender.sendMessage(main.color("&c&l(!) &r" + name + " is not a valid player name."));
			return false;
		}
		return true;
	}

	/** A store command can't be retried by the store, so a failure is also logged with the command to run by hand. */
	private void fail(CommandSender sender, String message, String commandToRetry) {
		main.getLogger().severe("[RankSubscriptions] Failed, run by hand: /ranksub " + commandToRetry + " (" + withoutColors(message) + ")");
		tell(sender, message);
	}

	private static String formatDate(long millis) {
		SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy HH:mm 'UTC'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format.format(new Date(millis));
	}

	private void tell(final CommandSender sender, final String message) {
		if (sender == null)
			return;
		onMain(new Runnable() {
			@Override
			public void run() {
				sender.sendMessage(main.color(message));
			}
		});
	}

	private void onMain(Runnable task) {
		try {
			Bukkit.getScheduler().runTask(main, task);
		} catch (RuntimeException e) {
			// The plugin is shutting down
		}
	}

	private void runDb(Runnable task) {
		try {
			dbThread.execute(task);
		} catch (RejectedExecutionException e) {
			// The plugin is shutting down, nothing more can be saved
		}
	}

	/** Strips the color codes of a message for the console log. */
	private static String withoutColors(String message) {
		return message.replaceAll("&[0-9a-fk-or]", "");
	}

	// ---------------------------------------------------------------- database (database thread only)

	private Connection connection() throws SQLException {
		Connection c = main.getDatabaseManager().getConnection();
		if (c == null)
			throw new SQLException("MySQL is not connected");
		return c;
	}

	private void ensureTable() throws SQLException {
		try (Statement st = connection().createStatement()) {
			st.execute("CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
					"  uuid        VARCHAR(36) NOT NULL," +
					"  name        VARCHAR(16) NOT NULL," +
					"  rank_id     INT NOT NULL," +
					"  previous_id INT NOT NULL," +
					"  expires_at  BIGINT NOT NULL," +
					"  PRIMARY KEY (uuid)," +
					"  INDEX (expires_at)" +
					") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		}
	}

	private Sub readSub(UUID uuid) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"SELECT uuid, name, rank_id, previous_id, expires_at FROM " + TABLE + " WHERE uuid = ?")) {
			ps.setString(1, uuid.toString());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? subFrom(rs) : null;
			}
		}
	}

	/** Every subscription whose paid time and grace are both over. */
	private List<Sub> readOver(long now) throws SQLException {
		List<Sub> subs = new ArrayList<>();
		try (PreparedStatement ps = connection().prepareStatement(
				"SELECT uuid, name, rank_id, previous_id, expires_at FROM " + TABLE + " WHERE expires_at < ?")) {
			ps.setLong(1, now - GRACE_MS);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					subs.add(subFrom(rs));
			}
		}
		return subs;
	}

	private static Sub subFrom(ResultSet rs) throws SQLException {
		return new Sub(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getInt("rank_id"),
				rs.getInt("previous_id"), rs.getLong("expires_at"));
	}

	private void writeSub(Sub sub) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"INSERT INTO " + TABLE + " (uuid, name, rank_id, previous_id, expires_at) VALUES (?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE name=VALUES(name), rank_id=VALUES(rank_id), " +
						"previous_id=VALUES(previous_id), expires_at=VALUES(expires_at)")) {
			ps.setString(1, sub.uuid.toString());
			ps.setString(2, sub.name);
			ps.setInt(3, sub.rankId);
			ps.setInt(4, sub.previousId);
			ps.setLong(5, sub.expiresAt);
			ps.executeUpdate();
		}
	}

	private void deleteSub(UUID uuid) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement("DELETE FROM " + TABLE + " WHERE uuid = ?")) {
			ps.setString(1, uuid.toString());
			ps.executeUpdate();
		}
	}

	/** An offline player by name, from the main player table. */
	private PlayerRow findPlayer(String name) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"SELECT UUID, LastPlayerName, RoleID FROM PlayerData WHERE LastPlayerName = ? LIMIT 1")) {
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return new PlayerRow(UUID.fromString(rs.getString("UUID")), rs.getString("LastPlayerName"), rs.getInt("RoleID"));
			}
		}
		return null;
	}

	/** Offline players: gives the subscription's rank, but only over DEFAULT, VIP or PRO. */
	private void setStoredRoleIfSwappable(UUID uuid, int newRole) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"UPDATE PlayerData SET RoleID = ? WHERE UUID = ? AND RoleID IN (?, ?, ?)")) {
			ps.setInt(1, newRole);
			ps.setString(2, uuid.toString());
			ps.setInt(3, Rank.DEFAULT.getRoleID());
			ps.setInt(4, Rank.VIP.getRoleID());
			ps.setInt(5, Rank.PRO.getRoleID());
			ps.executeUpdate();
		}
	}

	/** Offline players: takes the subscription's rank off, but only if they still hold it (a promotion since stays). */
	private void setStoredRoleIfCurrent(UUID uuid, int expectedRole, int newRole) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"UPDATE PlayerData SET RoleID = ? WHERE UUID = ? AND RoleID = ?")) {
			ps.setInt(1, newRole);
			ps.setString(2, uuid.toString());
			ps.setInt(3, expectedRole);
			ps.executeUpdate();
		}
	}
}
