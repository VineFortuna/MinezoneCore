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

	/**
	 * Loads the arena world on first use only - never at plugin startup - so a
	 * problem with this one world (corrupt chunks, missing folder, etc.) can
	 * never take the rest of the plugin down with it. Returns null on failure;
	 * callers must handle that instead of assuming a world is always available.
	 */
	public World getArenaWorld() {
		if (arenaWorld == null) {
			World existing = Bukkit.getWorld(VDGameConstants.MAP_WORLD_NAME);

			if (existing != null) {
				arenaWorld = existing;
			} else {
				try {
					arenaWorld = Bukkit.createWorld(
							new WorldCreator(VDGameConstants.MAP_WORLD_NAME)
					);
				} catch (Throwable t) {
					main.getLogger().severe(
							"[VillagerDefense] Failed to load the "
									+ VDGameConstants.MAP_WORLD_NAME
									+ " world - VillagerDefense will be unavailable until this is fixed."
					);
					t.printStackTrace();
				}
			}

			/*
			 * The map configuration was originally loaded before the arena
			 * world existed, meaning its Locations may contain a null World.
			 * Reload it now that Speedway is loaded.
			 */
			if (arenaWorld != null) {
				mapConfigManager.load();
			}
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

		World world = getArenaWorld();

		if (world == null) {
			player.sendMessage(main.color(
					"&c&l(!) &rVillagerDefense's map failed to load. Tell staff to check the console."
			));
			return false;
		}

		if (!mapConfigManager.getConfig().isReady()) {
			player.sendMessage(main.color(
					"&c&l(!) &rVillagerDefense's map hasn't been fully configured yet!"
			));
			return false;
		}

		if (mapConfigManager.getConfig().getLobby() == null
				|| mapConfigManager.getConfig().getLobby().getWorld() == null) {
			player.sendMessage(main.color(
					"&c&l(!) &rThe VillagerDefense lobby location is invalid. Please set it again."
			));
			main.getLogger().severe(
					"[VillagerDefense] Lobby location has no valid world."
			);
			return false;
		}

		if (activeInstance != null
				&& activeInstance.getState() != VDGameState.WAITING
				&& activeInstance.getState() != VDGameState.STARTING) {
			player.sendMessage(main.color(
					"&c&l(!) &rA VillagerDefense match is already in progress. Try again soon!"
			));
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
