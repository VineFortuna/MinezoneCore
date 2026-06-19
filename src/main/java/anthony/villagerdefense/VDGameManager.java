package anthony.villagerdefense;

import anthony.SuperCraftBrawl.Core;
import anthony.villagerdefense.map.VDMapConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

/**
 * Tracks the single active VillagerDefense match. Mirrors the role of
 * SuperCraftBros' GameManager / CrystalWars' GameManager, but deliberately
 * named VDGameManager (not GameManager) so it never collides with either of
 * those when Core.java wildcard-imports anthony.SuperCraftBrawl.Game.*.
 */
public class VDGameManager {

	private final Core main;
	private final VDMapConfigManager mapConfigManager;
	private VDGameInstance activeInstance;
	private World arenaWorld;

	public VDGameManager(Core main) {
		this.main = main;
		this.mapConfigManager = new VDMapConfigManager(main);
	}

	public Core getMain() {
		return main;
	}

	public VDMapConfigManager getMapConfigManager() {
		return mapConfigManager;
	}

	public VDGameInstance getActiveInstance() {
		return activeInstance;
	}

	public World getArenaWorld() {
		if (arenaWorld == null) {
			World existing = Bukkit.getWorld(VDGameConstants.MAP_WORLD_NAME);
			arenaWorld = existing != null ? existing
					: Bukkit.createWorld(new WorldCreator(VDGameConstants.MAP_WORLD_NAME));
		}
		return arenaWorld;
	}

	public VDGameInstance getInstanceOfPlayer(Player player) {
		return (activeInstance != null && activeInstance.hasPlayer(player)) ? activeInstance : null;
	}

	public VDGameInstance getInstanceOfSpectator(Player player) {
		return (activeInstance != null && activeInstance.hasSpectator(player)) ? activeInstance : null;
	}

	/**
	 * @return true if the player ended up in the match, false if they were rejected
	 * (a message was already sent to them either way).
	 */
	public boolean joinGame(Player player) {
		if (getInstanceOfPlayer(player) != null || getInstanceOfSpectator(player) != null) {
			player.sendMessage(main.color("&c&l(!) &rYou are already in VillagerDefense!"));
			return false;
		}

		if (main.isPlayerInAnyGame(player) || main.getParkour().hasPlayer(player)) {
			player.sendMessage(main.color("&c&l(!) &rYou have to leave your current game first!"));
			return false;
		}

		if (!mapConfigManager.getConfig().isReady()) {
			player.sendMessage(main.color("&c&l(!) &rVillagerDefense's map hasn't been fully configured yet!"));
			return false;
		}

		if (activeInstance != null && activeInstance.getState() != VDGameState.WAITING
				&& activeInstance.getState() != VDGameState.STARTING) {
			player.sendMessage(main.color("&c&l(!) &rA VillagerDefense match is already in progress. Try again soon!"));
			return false;
		}

		if (activeInstance == null) {
			activeInstance = new VDGameInstance(this);
		}

		return activeInstance.addPlayer(player);
	}

	/**
	 * @return true if the player was removed from a match/spectating, false if they weren't in one.
	 */
	public boolean leaveGame(Player player) {
		if (activeInstance == null) return false;

		if (activeInstance.hasPlayer(player)) {
			activeInstance.removePlayer(player);
			return true;
		}

		if (activeInstance.hasSpectator(player)) {
			activeInstance.removeSpectator(player);
			return true;
		}

		return false;
	}

	/** Called by VDGameInstance once a match has fully ended and reset. */
	void clearActiveInstance(VDGameInstance instance) {
		if (activeInstance == instance) {
			activeInstance = null;
		}
	}
}
