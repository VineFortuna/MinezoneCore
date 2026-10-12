package anthony.flagwars.map;

import anthony.SuperCraftBrawl.Core;
import anthony.flagwars.FWGameConstants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages Flag Wars map configs, one YAML file per world under
 * plugins/MinezoneCore/flagwars/<worldname>.yml. All saved configs are loaded
 * at startup; new ones are created in-memory on demand and written to disk
 * when /fwsetup save is run.
 */
public class FWMapConfigManager {

	private final Core main;
	private final File configDir;
	private final Map<String, FWMapConfig> configs = new LinkedHashMap<>();

	public FWMapConfigManager(Core main) {
		this.main = main;
		this.configDir = new File(main.getDataFolder(), "flagwars");
		loadAll();
	}

	private void loadAll() {
		if (configDir.exists()) {
			File[] files = configDir.listFiles((dir, name) -> name.endsWith(".yml"));
			if (files != null) {
				for (File f : files) {
					String worldName = f.getName().replace(".yml", "");
					configs.put(worldName, loadFromFile(f, worldName));
				}
			}
		}

		// Migrate the old flagdefense save if no flagwars file exists for the primary map yet.
		if (!configs.containsKey(FWGameConstants.MAP_WORLD_NAME)) {
			File legacy = new File(new File(main.getDataFolder(), "flagdefense"),
					FWGameConstants.MAP_WORLD_NAME + ".yml");
			if (legacy.exists()) {
				configs.put(FWGameConstants.MAP_WORLD_NAME, loadFromFile(legacy, FWGameConstants.MAP_WORLD_NAME));
			} else {
				configs.put(FWGameConstants.MAP_WORLD_NAME, new FWMapConfig(FWGameConstants.MAP_WORLD_NAME));
			}
		}
	}

	/**
	 * Re-reads all configs from disk. Called after the arena world loads so saved
	 * Locations get a live World object (they're null until the world exists in Bukkit).
	 */
	public void reload() {
		configs.clear();
		loadAll();
	}

	/** Returns the config for the primary game map (MAP_WORLD_NAME). Used by the game engine. */
	public FWMapConfig getConfig() {
		return getOrCreate(FWGameConstants.MAP_WORLD_NAME);
	}

	/** Returns the config for the given world name, creating a blank in-memory one if unknown. */
	public FWMapConfig getOrCreate(String worldName) {
		return configs.computeIfAbsent(worldName, FWMapConfig::new);
	}

	/** All currently tracked map configs (saved and unsaved). */
	public Collection<FWMapConfig> getAllConfigs() {
		return configs.values();
	}

	/** Saves the primary map config. */
	public void save() {
		save(getConfig());
	}

	/** Saves one config to flagwars/<worldName>.yml. */
	public void save(FWMapConfig config) {
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("world", config.getWorldName());
		writeLocation(yaml, "lobby", config.getLobby());
		writeLocation(yaml, "spectatorLocation", config.getSpectatorLocation());

		for (FWTeamSite site : config.getTeamSites().values()) {
			String base = "teams." + site.getId() + ".";
			yaml.set(base + "created", site.isCreated());
			writeLocation(yaml, base + "spawn", site.getSpawn());
			writeLocation(yaml, base + "flag", site.getFlagLoc());
			writeLocation(yaml, base + "shop", site.getShopLoc());
			writeLocation(yaml, base + "ironGenerator", site.getIronGenerator());
			writeLocation(yaml, base + "goldGenerator", site.getGoldGenerator());
			if (site.getIronGeneratorInterval() != null) yaml.set(base + "ironGeneratorInterval", site.getIronGeneratorInterval());
			if (site.getGoldGeneratorInterval() != null) yaml.set(base + "goldGeneratorInterval", site.getGoldGeneratorInterval());
			writeLocation(yaml, base + "boundary1", site.getBoundaryCorner1());
			writeLocation(yaml, base + "boundary2", site.getBoundaryCorner2());
		}

		for (Map.Entry<Integer, Location> entry : config.getEmeraldGenerators().entrySet()) {
			writeLocation(yaml, "generators.emerald." + entry.getKey(), entry.getValue());
			Integer interval = config.getEmeraldGeneratorInterval(entry.getKey());
			if (interval != null) yaml.set("generators.emerald." + entry.getKey() + ".interval", interval);
		}
		for (Map.Entry<Integer, Location> entry : config.getDiamondGenerators().entrySet()) {
			writeLocation(yaml, "generators.diamond." + entry.getKey(), entry.getValue());
			Integer interval = config.getDiamondGeneratorInterval(entry.getKey());
			if (interval != null) yaml.set("generators.diamond." + entry.getKey() + ".interval", interval);
		}
		for (Map.Entry<Integer, Location> entry : config.getChestHolograms().entrySet()) {
			writeLocation(yaml, "chestHolograms." + entry.getKey(), entry.getValue());
		}

		File file = new File(configDir, config.getWorldName() + ".yml");
		try {
			file.getParentFile().mkdirs();
			yaml.save(file);
		} catch (IOException e) {
			main.getLogger().warning("Failed to save Flag Wars map config for " + config.getWorldName() + ": " + e.getMessage());
		}
	}

	private FWMapConfig loadFromFile(File source, String worldName) {
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(source);
		FWMapConfig loaded = new FWMapConfig(worldName);
		loaded.setLobby(readLocation(yaml, "lobby"));
		loaded.setSpectatorLocation(readLocation(yaml, "spectatorLocation"));

		for (int i = 1; i <= FWMapConfig.TEAM_COUNT; i++) {
			String base = "teams." + i + ".";
			FWTeamSite site = loaded.getTeamSite(i);
			site.setSpawn(readLocation(yaml, base + "spawn"));
			// Back-compat: maps saved before the "created" flag existed had every configured team's
			// spawn set with no explicit flag - treat "spawn already set" as "was already in play".
			site.setCreated(yaml.isSet(base + "created") ? yaml.getBoolean(base + "created") : site.getSpawn() != null);
			site.setFlagLoc(readLocation(yaml, base + "flag"));
			site.setShopLoc(readLocation(yaml, base + "shop"));
			site.setIronGenerator(readLocation(yaml, base + "ironGenerator"));
			site.setGoldGenerator(readLocation(yaml, base + "goldGenerator"));
			if (yaml.isSet(base + "ironGeneratorInterval")) site.setIronGeneratorInterval(yaml.getInt(base + "ironGeneratorInterval"));
			if (yaml.isSet(base + "goldGeneratorInterval")) site.setGoldGeneratorInterval(yaml.getInt(base + "goldGeneratorInterval"));
			site.setBoundaryCorner1(readLocation(yaml, base + "boundary1"));
			site.setBoundaryCorner2(readLocation(yaml, base + "boundary2"));
		}

		ConfigurationSection chestHolograms = yaml.getConfigurationSection("chestHolograms");
		if (chestHolograms != null) {
			for (String key : chestHolograms.getKeys(false)) {
				loaded.setChestHologram(Integer.parseInt(key), readLocation(yaml, "chestHolograms." + key));
			}
		}

		ConfigurationSection emerald = yaml.getConfigurationSection("generators.emerald");
		if (emerald != null) {
			for (String key : emerald.getKeys(false)) {
				int id = Integer.parseInt(key);
				loaded.setEmeraldGenerator(id, readLocation(yaml, "generators.emerald." + key));
				if (yaml.isSet("generators.emerald." + key + ".interval")) {
					loaded.setEmeraldGeneratorInterval(id, yaml.getInt("generators.emerald." + key + ".interval"));
				}
			}
		}

		ConfigurationSection diamond = yaml.getConfigurationSection("generators.diamond");
		if (diamond != null) {
			for (String key : diamond.getKeys(false)) {
				int id = Integer.parseInt(key);
				loaded.setDiamondGenerator(id, readLocation(yaml, "generators.diamond." + key));
				if (yaml.isSet("generators.diamond." + key + ".interval")) {
					loaded.setDiamondGeneratorInterval(id, yaml.getInt("generators.diamond." + key + ".interval"));
				}
			}
		}

		return loaded;
	}

	private void writeLocation(YamlConfiguration yaml, String path, Location loc) {
		if (loc == null) return;
		yaml.set(path + ".world", loc.getWorld().getName());
		yaml.set(path + ".x", loc.getX());
		yaml.set(path + ".y", loc.getY());
		yaml.set(path + ".z", loc.getZ());
		yaml.set(path + ".yaw", loc.getYaw());
		yaml.set(path + ".pitch", loc.getPitch());
	}

	private Location readLocation(YamlConfiguration yaml, String path) {
		if (!yaml.isSet(path + ".world")) return null;
		World world = Bukkit.getWorld(yaml.getString(path + ".world"));
		double x = yaml.getDouble(path + ".x");
		double y = yaml.getDouble(path + ".y");
		double z = yaml.getDouble(path + ".z");
		float yaw = (float) yaml.getDouble(path + ".yaw");
		float pitch = (float) yaml.getDouble(path + ".pitch");
		return new Location(world, x, y, z, yaw, pitch);
	}
}
