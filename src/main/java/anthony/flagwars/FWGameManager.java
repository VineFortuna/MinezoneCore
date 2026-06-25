package anthony.flagwars;

import anthony.SuperCraftBrawl.Core;
import anthony.flagwars.map.FWMapConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Tracks every concurrently-running Flag Wars match. Mirrors the role of
 * SuperCraftBros' GameManager / CrystalWars' GameManager, but deliberately
 * named FWGameManager (not GameManager) so it never collides with either of
 * those when Core.java wildcard-imports anthony.SuperCraftBrawl.Game.*.
 *
 * Only one physical map is set up by staff (FWGameConstants.MAP_WORLD_NAME,
 * "Speedway"), but more than one match can run at once: the first match of
 * any kind uses that real world directly, and once it's occupied, any
 * further match gets a throwaway copy of it (see provisionWorld()/cloneWorld())
 * that's deleted from disk again the moment that match ends.
 */
public class FWGameManager {

	private final Core main;
	private final FWMapConfigManager mapConfigManager;
	private final List<FWGameInstance> activeInstances = new ArrayList<>();
	private World primaryWorld;
	private int cloneCounter = 0;

	public FWGameManager(Core main) {
		this.main = main;
		this.mapConfigManager = new FWMapConfigManager(main);
	}

	public Core getMain() {
		return main;
	}

	public FWMapConfigManager getMapConfigManager() {
		return mapConfigManager;
	}

	public List<FWGameInstance> getActiveInstances() {
		return activeInstances;
	}

	/** Finds whichever active match (if any) is actually running in this world - every instance owns a distinct world, clone or not. */
	public FWGameInstance getInstanceForWorld(World world) {
		if (world == null) return null;
		for (FWGameInstance instance : activeInstances) {
			if (instance.getWorld().equals(world)) return instance;
		}
		return null;
	}

	/**
	 * Loads the real, staff-configured arena world on first use only - never at plugin
	 * startup - so a problem with this one world (corrupt chunks, missing folder, etc.)
	 * can never take the rest of the plugin down with it. Returns null on failure;
	 * callers must handle that instead of assuming a world is always available. Used
	 * directly by /fwsetup (always configures the real map, never a clone) and as the
	 * template provisionWorld() clones from.
	 */
	public World getArenaWorld() {
		if (primaryWorld == null) {
			World existing = Bukkit.getWorld(FWGameConstants.MAP_WORLD_NAME);

			if (existing != null) {
				primaryWorld = existing;
			} else {
				try {
					primaryWorld = Bukkit.createWorld(
							new WorldCreator(FWGameConstants.MAP_WORLD_NAME)
					);
				} catch (Throwable t) {
					main.getLogger().severe(
							"[Flag Wars] Failed to load the "
									+ FWGameConstants.MAP_WORLD_NAME
									+ " world - Flag Wars will be unavailable until this is fixed."
					);
					t.printStackTrace();
				}
			}

			/*
			 * The map configuration was originally loaded before the arena
			 * world existed, meaning its Locations may contain a null World.
			 * Reload it now that Speedway is loaded.
			 */
			if (primaryWorld != null) {
				primaryWorld.setAutoSave(false); // never persist a match's grief to disk - see releaseArenaWorld()
				mapConfigManager.load();
			}
		}

		return primaryWorld;
	}

	/**
	 * Picks a world for a brand new match: the real arena if nothing's using it right
	 * now, otherwise a throwaway clone of it so several matches can run side by side
	 * instead of queuing behind each other.
	 */
	private World provisionWorld() {
		World primary = getArenaWorld();
		if (primary == null) return null;

		if (getInstanceForWorld(primary) == null) {
			return primary;
		}
		return cloneWorld(primary);
	}

	/**
	 * Copies the real arena world's folder to a fresh, uniquely-named one and loads it
	 * as a brand new World - a byte-for-byte duplicate of the same map, but a completely
	 * independent Bukkit world so a second/third/etc. match can play out on it without
	 * touching the original. uid.dat and session.lock are deliberately skipped so Bukkit
	 * assigns the clone its own UUID and lock instead of colliding with the original's.
	 */
	private World cloneWorld(World primary) {
		String cloneName = FWGameConstants.MAP_WORLD_NAME + "_temp" + (++cloneCounter);
		File source = new File(Bukkit.getWorldContainer(), primary.getName());
		File target = new File(Bukkit.getWorldContainer(), cloneName);

		try {
			copyWorldFolder(source.toPath(), target.toPath());
		} catch (IOException e) {
			main.getLogger().severe("[Flag Wars] Failed to clone the arena world for an overflow match: " + e.getMessage());
			return null;
		}

		World clone;
		try {
			clone = Bukkit.createWorld(new WorldCreator(cloneName));
		} catch (Throwable t) {
			main.getLogger().severe("[Flag Wars] Failed to load cloned arena world " + cloneName + ": " + t.getMessage());
			return null;
		}

		clone.setAutoSave(false);
		return clone;
	}

	private void copyWorldFolder(Path source, Path target) throws IOException {
		try {
			Files.walk(source).forEach(path -> {
				try {
					String name = path.getFileName().toString();
					if (name.equals("uid.dat") || name.equals("session.lock")) return;

					Path dest = target.resolve(source.relativize(path));
					if (Files.isDirectory(path)) {
						Files.createDirectories(dest);
					} else {
						Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	private void deleteWorldFolder(File folder) {
		if (folder == null || !folder.exists()) return;

		try {
			Files.walk(folder.toPath())
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		} catch (IOException e) {
			main.getLogger().warning("[Flag Wars] Failed to delete cloned arena world folder " + folder + ": " + e.getMessage());
		}
	}

	/**
	 * Unregisters a finished match, unloads its world without saving (discarding any
	 * grief), and - if that world was a temporary clone rather than the real arena -
	 * deletes its folder from disk entirely once the unload succeeds.
	 */
	public void releaseArenaWorld(FWGameInstance instance) {
		activeInstances.remove(instance);

		World world = instance.getWorld();
		boolean isClone = instance.isClonedWorld();
		File worldFolder = new File(Bukkit.getWorldContainer(), world.getName());

		if (world.equals(primaryWorld)) {
			primaryWorld = null;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if (Bukkit.unloadWorld(world, false)) {
					this.cancel();
					if (isClone) {
						deleteWorldFolder(worldFolder);
					}
				}
			}
		}.runTaskTimer(main, 0L, 1L);
	}

	public FWGameInstance getInstanceOfPlayer(Player player) {
		for (FWGameInstance instance : activeInstances) {
			if (instance.hasPlayer(player)) return instance;
		}
		return null;
	}

	public FWGameInstance getInstanceOfSpectator(Player player) {
		for (FWGameInstance instance : activeInstances) {
			if (instance.hasSpectator(player)) return instance;
		}
		return null;
	}

	/**
	 * Joins an existing joinable (waiting/starting, not full) match of this mode if one
	 * exists, otherwise provisions a world (real arena or a fresh clone) and starts a new one.
	 *
	 * @return true if the player ended up in the match, false if they were rejected
	 * (a message was already sent to them either way).
	 */
	public boolean joinGame(Player player, FWGameMode mode) {
		if (getInstanceOfPlayer(player) != null || getInstanceOfSpectator(player) != null) {
			player.sendMessage(main.color("&c&l(!) &rYou are already in Flag Wars!"));
			return false;
		}

		if (main.isPlayerInAnyGame(player) || main.getParkour().hasPlayer(player)) {
			player.sendMessage(main.color("&c&l(!) &rYou have to leave your current game first!"));
			return false;
		}

		// Just to make sure the real arena (and its map config) is loaded - this match may
		// still end up on a clone of it if the real one's already mid-game, see provisionWorld().
		if (getArenaWorld() == null) {
			player.sendMessage(main.color(
					"&c&l(!) &rFlag Wars's map failed to load. Tell staff to check the console."
			));
			return false;
		}

		if (!mapConfigManager.getConfig().isReady()) {
			player.sendMessage(main.color(
					"&c&l(!) &rFlag Wars's map hasn't been fully configured yet!"
			));
			return false;
		}

		if (mapConfigManager.getConfig().getLobby() == null
				|| mapConfigManager.getConfig().getLobby().getWorld() == null) {
			player.sendMessage(main.color(
					"&c&l(!) &rThe Flag Wars lobby location is invalid. Please set it again."
			));
			main.getLogger().severe(
					"[Flag Wars] Lobby location has no valid world."
			);
			return false;
		}

		for (FWGameInstance instance : activeInstances) {
			if (instance.getMode() == mode
					&& (instance.getState() == FWGameState.WAITING || instance.getState() == FWGameState.STARTING)
					&& instance.hasSpace()) {
				return instance.addPlayer(player);
			}
		}

		World world = provisionWorld();
		if (world == null) {
			player.sendMessage(main.color(
					"&c&l(!) &rFlag Wars's map failed to load. Tell staff to check the console."
			));
			return false;
		}

		FWGameInstance instance = new FWGameInstance(this, mode, world, !world.equals(primaryWorld));
		activeInstances.add(instance);
		return instance.addPlayer(player);
	}

	/**
	 * @return true if the player was removed from a match/spectating, false if they weren't in one.
	 */
	public boolean leaveGame(Player player) {
		FWGameInstance instance = getInstanceOfPlayer(player);
		if (instance != null) {
			instance.removePlayer(player);
			return true;
		}

		instance = getInstanceOfSpectator(player);
		if (instance != null) {
			instance.removeSpectator(player);
			return true;
		}

		return false;
	}

	/**
	 * Spectates one specific in-progress match - used by ActiveGamesGUI, where the player picks
	 * an exact instance from a list rather than joining/queueing for "any match of this mode."
	 *
	 * @return true if the player ended up spectating, false if they were rejected (a message was
	 * already sent to them either way).
	 */
	public boolean spectateGame(Player player, FWGameInstance instance) {
		if (getInstanceOfPlayer(player) != null || getInstanceOfSpectator(player) != null) {
			player.sendMessage(main.color("&c&l(!) &rYou are already in Flag Wars!"));
			return false;
		}

		if (main.isPlayerInAnyGame(player) || main.getParkour().hasPlayer(player)) {
			player.sendMessage(main.color("&c&l(!) &rYou have to leave your current game first!"));
			return false;
		}

		instance.addSpectator(player);
		return true;
	}
}
