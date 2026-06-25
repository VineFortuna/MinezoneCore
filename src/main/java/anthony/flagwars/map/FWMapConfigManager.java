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
import java.util.Map;

/**
 * Loads/saves the single Flag Wars map's configuration to a YAML
 * file under the plugin's data folder. The Bukkit 1.8.8 API doesn't have
 * YamlConfiguration#getLocation, so Locations are stored as plain
 * world/x/y/z/yaw/pitch fields instead.
 */
public class FWMapConfigManager {

	private final Core main;
	private final File file;
	/** Old pre-rename save location (the gamemode used to be called FlagDefense) - read once as a fallback so an existing map setup isn't lost. */
	private final File legacyFile;
	private FWMapConfig config;

	public FWMapConfigManager(Core main) {
		this.main = main;
		this.file = new File(new File(main.getDataFolder(), "flagwars"),
				FWGameConstants.MAP_WORLD_NAME + ".yml");
		this.legacyFile = new File(new File(main.getDataFolder(), "flagdefense"),
				FWGameConstants.MAP_WORLD_NAME + ".yml");
		load();
	}

	public FWMapConfig getConfig() {
		return config;
	}

	public void load() {
		File source = file.exists() ? file : (legacyFile.exists() ? legacyFile : null);

		if (source == null) {
			config = new FWMapConfig(FWGameConstants.MAP_WORLD_NAME);
			return;
		}

		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(source);
		FWMapConfig loaded = new FWMapConfig(FWGameConstants.MAP_WORLD_NAME);
		loaded.setLobby(readLocation(yaml, "lobby"));

		for (int i = 1; i <= FWMapConfig.TEAM_COUNT; i++) {
			String base = "teams." + i + ".";
			FWTeamSite site = loaded.getTeamSite(i);
			site.setSpawn(readLocation(yaml, base + "spawn"));
			site.setFlagLoc(readLocation(yaml, base + "flag"));
			site.setShopLoc(readLocation(yaml, base + "shop"));
			site.setIronGenerator(readLocation(yaml, base + "ironGenerator"));
			site.setGoldGenerator(readLocation(yaml, base + "goldGenerator"));
			site.setBoundaryCorner1(readLocation(yaml, base + "boundary1"));
			site.setBoundaryCorner2(readLocation(yaml, base + "boundary2"));
		}

		ConfigurationSection emerald = yaml.getConfigurationSection("generators.emerald");
		if (emerald != null) {
			for (String key : emerald.getKeys(false)) {
				loaded.setEmeraldGenerator(Integer.parseInt(key), readLocation(yaml, "generators.emerald." + key));
			}
		}

		ConfigurationSection diamond = yaml.getConfigurationSection("generators.diamond");
		if (diamond != null) {
			for (String key : diamond.getKeys(false)) {
				loaded.setDiamondGenerator(Integer.parseInt(key), readLocation(yaml, "generators.diamond." + key));
			}
		}

		config = loaded;
	}

	public void save() {
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("world", config.getWorldName());
		writeLocation(yaml, "lobby", config.getLobby());

		for (FWTeamSite site : config.getTeamSites().values()) {
			String base = "teams." + site.getId() + ".";
			writeLocation(yaml, base + "spawn", site.getSpawn());
			writeLocation(yaml, base + "flag", site.getFlagLoc());
			writeLocation(yaml, base + "shop", site.getShopLoc());
			writeLocation(yaml, base + "ironGenerator", site.getIronGenerator());
			writeLocation(yaml, base + "goldGenerator", site.getGoldGenerator());
			writeLocation(yaml, base + "boundary1", site.getBoundaryCorner1());
			writeLocation(yaml, base + "boundary2", site.getBoundaryCorner2());
		}

		for (Map.Entry<Integer, Location> entry : config.getEmeraldGenerators().entrySet()) {
			writeLocation(yaml, "generators.emerald." + entry.getKey(), entry.getValue());
		}
		for (Map.Entry<Integer, Location> entry : config.getDiamondGenerators().entrySet()) {
			writeLocation(yaml, "generators.diamond." + entry.getKey(), entry.getValue());
		}

		try {
			file.getParentFile().mkdirs();
			yaml.save(file);
		} catch (IOException e) {
			main.getLogger().warning("Failed to save Flag Wars map config: " + e.getMessage());
		}
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
