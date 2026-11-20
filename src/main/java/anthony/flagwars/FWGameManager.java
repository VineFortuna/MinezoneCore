package anthony.flagwars;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import anthony.flagwars.map.FWMapConfig;
import anthony.flagwars.map.FWMapConfigManager;
import anthony.util.SoundManager;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tracks every concurrently-running Flag Wars match. Mirrors the role of
 * SuperCraftBros' GameManager / CrystalWars' GameManager, but deliberately
 * named FWGameManager (not GameManager) so it never collides with either of
 * those when Core.java wildcard-imports anthony.SuperCraftBrawl.Game.*.
 *
 * Every map staff set up with /fwsetup (createmap + a fully configured, saved
 * config) is in the rotation automatically. Each new match picks one of those
 * maps at random and plays on a throwaway copy of it (see provisionWorld()/
 * cloneWorld()) that's deleted from disk again the moment that match ends - so
 * any number of matches can run at once, on any mix of maps, and the original
 * maps are never played on (or griefed) directly.
 */
public class FWGameManager {

	private static final int SPAWN_PROT_DURATION_TICKS = 3 * 20;

	private final Core main;
	private final FWMapConfigManager mapConfigManager;
	private final List<FWGameInstance> activeInstances = new ArrayList<>();
	private final Set<UUID> spawnProtected = new HashSet<>();
	/** Prefix for every throwaway match world, so they're never mistaken for a real map and stale ones can be swept on startup. */
	private static final String CLONE_PREFIX = "fwgame_";
	private int cloneCounter = 0;
	private FWPlayerListener playerListener;

	public FWGameManager(Core main) {
		this.main = main;
		this.mapConfigManager = new FWMapConfigManager(main);
		deleteStaleClones();
	}

	public void setPlayerListener(FWPlayerListener playerListener) {
		this.playerListener = playerListener;
	}

	public void clearAttackerHistory(Player player) {
		if (playerListener != null) playerListener.clearAttackerHistory(player);
	}

	public boolean isSpawnProtected(Player player) {
		return spawnProtected.contains(player.getUniqueId());
	}

	/** Grants 3-second spawn protection to a player: blocks incoming/outgoing damage and shows enchantment-table particles. */
	public void grantSpawnProtection(Player player, FWGameInstance instance) {
		UUID uuid = player.getUniqueId();
		spawnProtected.add(uuid);
		new BukkitRunnable() {
			@Override public void run() { spawnProtected.remove(uuid); }
		}.runTaskLater(main, SPAWN_PROT_DURATION_TICKS);
		spawnProtParticles(player, instance);
	}

	private void spawnProtParticles(Player player, FWGameInstance instance) {
		new BukkitRunnable() {
			int ticks = 0;
			final double radius = 1.0;
			final int particleCount = 20;

			@Override
			public void run() {
				if (ticks >= SPAWN_PROT_DURATION_TICKS || !player.isOnline()) {
					cancel();
					return;
				}

				double angle = 2 * Math.PI * ticks / particleCount;
				double x = radius * Math.cos(angle);
				double z = radius * Math.sin(angle);

				for (double yOffset = 0; yOffset <= 2; yOffset += 0.1) {
					Location loc = player.getLocation().clone().add(x, yOffset, z);
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
							EnumParticle.ENCHANTMENT_TABLE, true,
							(float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
							0F, 0F, 0F, 0F, 1);
					for (Player viewer : instance.onlineParticipants()) {
						((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
					}
				}

				ticks++;
			}
		}.runTaskTimer(main, 0L, 1L);
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
	 * Every map that can host a match right now: fully configured (see FWMapConfig#isReady) and
	 * with a world folder on disk to copy from. New maps join the rotation automatically as soon
	 * as they're set up and saved - nothing here is hardcoded to a specific map.
	 */
	public List<FWMapConfig> getPlayableMaps() {
		List<FWMapConfig> playable = new ArrayList<>();
		for (FWMapConfig config : mapConfigManager.getAllConfigs()) {
			String name = config.getWorldName();
			if (name.startsWith(CLONE_PREFIX)) continue;
			if (!new File(new File(Bukkit.getWorldContainer(), name), "level.dat").exists()) continue;
			if (!config.isReady()) continue;
			playable.add(config);
		}
		return playable;
	}

	/**
	 * Loads a fresh copy of the given map for a brand new match. The original is never played on,
	 * so any number of matches can share the same map. Returns null if the copy failed (already logged).
	 */
	private World provisionWorld(FWMapConfig map) {
		World source = Bukkit.getWorld(map.getWorldName());
		// Staff may have it loaded for /fwsetup - flush their edits to disk so the copy is current.
		if (source != null) source.save();
		return cloneWorld(map.getWorldName());
	}

	/**
	 * Copies a map's world folder to a fresh, uniquely-named one and loads it as a brand new
	 * World - a byte-for-byte duplicate of the map, but a completely independent Bukkit world so
	 * matches can play out side by side without touching the original. uid.dat and session.lock
	 * are deliberately skipped so Bukkit assigns the clone its own UUID and lock instead of
	 * colliding with the original's.
	 */
	private World cloneWorld(String mapName) {
		String cloneName = CLONE_PREFIX + mapName + "_" + (++cloneCounter);
		File source = new File(Bukkit.getWorldContainer(), mapName);
		File target = new File(Bukkit.getWorldContainer(), cloneName);

		try {
			copyWorldFolder(source.toPath(), target.toPath());
		} catch (IOException e) {
			main.getLogger().severe("[Flag Wars] Failed to copy map " + mapName + " for a new match: " + e.getMessage());
			deleteWorldFolder(target);
			return null;
		}

		World clone;
		try {
			// Void generator, same as /fwsetup createmap, so unbuilt chunks stay empty instead of growing terrain
			clone = Bukkit.createWorld(new WorldCreator(cloneName)
					.environment(World.Environment.NORMAL)
					.generateStructures(false)
					.generator(new VoidGenerator()));
		} catch (Throwable t) {
			main.getLogger().severe("[Flag Wars] Failed to load copied map " + cloneName + ": " + t.getMessage());
			clone = null;
		}
		if (clone == null) {
			deleteWorldFolder(target);
			return null;
		}

		clone.setAutoSave(false);
		return clone;
	}

	/** Match copies left on disk by a crash/restart mid-game - nothing loads them, so they're safe to delete. */
	private void deleteStaleClones() {
		File[] folders = Bukkit.getWorldContainer().listFiles(
				f -> f.isDirectory() && f.getName().startsWith(CLONE_PREFIX));
		if (folders == null) return;
		for (File folder : folders) {
			if (Bukkit.getWorld(folder.getName()) == null) deleteWorldFolder(folder);
		}
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
	 * Unregisters a finished match, unloads its world without saving, and deletes that
	 * throwaway copy from disk once the unload succeeds. The original map is never touched.
	 */
	public void releaseArenaWorld(FWGameInstance instance) {
		activeInstances.remove(instance);

		World world = instance.getWorld();
		File worldFolder = new File(Bukkit.getWorldContainer(), world.getName());

		new BukkitRunnable() {
			@Override
			public void run() {
				if (Bukkit.unloadWorld(world, false)) {
					this.cancel();
					deleteWorldFolder(worldFolder);
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
	 * exists, otherwise starts a new one on a fresh copy of a randomly picked map.
	 *
	 * @return true if the player ended up in the match, false if they were rejected
	 * (a message was already sent to them either way).
	 */
	public boolean joinGame(Player player, FWGameMode mode) {
		return joinGame(player, mode, null);
	}

	/** A playable map by name (case-insensitive), or null if there's no such fully set-up map. */
	public FWMapConfig getPlayableMap(String name) {
		for (FWMapConfig config : getPlayableMaps()) {
			if (config.getWorldName().equalsIgnoreCase(name)) return config;
		}
		return null;
	}

	/**
	 * Same, but only on the given map (/join fw <map>): its open match of this mode, or a new one on a fresh copy
	 * of it. A null map means any map.
	 */
	public boolean joinGame(Player player, FWGameMode mode, FWMapConfig onlyMap) {
		if (getInstanceOfPlayer(player) != null || getInstanceOfSpectator(player) != null) {
			player.sendMessage(main.color("&c&l(!) &rYou are already in Flag Wars!"));
			return false;
		}

		if (main.isPlayerInAnyGame(player) || main.getParkour().hasPlayer(player)) {
			player.sendMessage(main.color("&c&l(!) &rYou have to leave your current game first!"));
			return false;
		}

		for (FWGameInstance instance : activeInstances) {
			if (instance.getMode() == mode
					&& (onlyMap == null || instance.getMapConfig().getWorldName().equals(onlyMap.getWorldName()))
					&& (instance.getState() == FWGameState.WAITING || instance.getState() == FWGameState.STARTING)
					&& instance.hasSpace()) {
				return instance.addPlayer(player);
			}
		}

		// No open match - start a new one on a random map out of every fully set-up map.
		List<FWMapConfig> playableMaps = getPlayableMaps();
		if (playableMaps.isEmpty()) {
			player.sendMessage(main.color(
					"&c&l(!) &rNo Flag Wars maps have been fully configured yet!"
			));
			return false;
		}

		FWMapConfig map = onlyMap != null ? onlyMap
				: playableMaps.get(ThreadLocalRandom.current().nextInt(playableMaps.size()));
		World world = provisionWorld(map);
		if (world == null) {
			player.sendMessage(main.color(
					"&c&l(!) &rFlag Wars's map failed to load. Tell staff to check the console."
			));
			return false;
		}

		FWGameInstance instance = new FWGameInstance(this, mode, world, map);
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
