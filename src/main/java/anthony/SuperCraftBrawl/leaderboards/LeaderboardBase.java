package anthony.SuperCraftBrawl.leaderboards;

import java.sql.SQLException;

import org.bukkit.Bukkit;

import anthony.SuperCraftBrawl.Core;

public abstract class LeaderboardBase {
	private int i;

	public LeaderboardBase(Core main) {
		Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
			i = 0;
			
			try {
				asyncUpdate();
				Bukkit.getScheduler().runTask(main, () -> {
					try {
						updateLeaderboard(i == 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

		}, 0, 20 * 60);
	}

	/**
	 * Called before {@link #updateLeaderboard(boolean)} for updating DB
	 * information.
	 */
	public abstract void asyncUpdate() throws SQLException;

	/**
	 * Called to update the armour stands.
	 */
	public abstract void updateLeaderboard(boolean init);

	/**
	 * Called when closing the leaderboards.
	 */
	public abstract void close();

}
