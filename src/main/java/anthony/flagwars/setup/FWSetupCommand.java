package anthony.flagwars.setup;

import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import anthony.flagwars.FWGameConstants;
import anthony.flagwars.FWGameManager;
import anthony.flagwars.map.FWImportantArea;
import anthony.flagwars.map.FWMapConfig;
import anthony.flagwars.map.FWMapConfigManager;
import anthony.flagwars.map.FWTeamSite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Staff-only tool to configure Flag Wars maps. Each subcommand (except tp,
 * list, createmap, save) operates on the map whose world the player is
 * currently standing in, so builders just need to be in the right world.
 */
public class FWSetupCommand implements CommandExecutor, TabExecutor {

	private static final List<String> SUBCOMMANDS = Arrays.asList(
			"tp", "createmap", "generatemap", "size", "lobby", "spectator", "team", "flag", "shop",
			"generator", "boundary1", "boundary2", "importantarea", "chesthologram", "list", "maplist", "delete", "save");
	private static final long DELETE_CONFIRM_MILLIS = 15_000L;
	private static final String IMPORTANT_AREA_USAGE = "/fwsetup importantarea <color> <id> <X> <Y> <Z> &7or &e<id> remove";
	private static final List<String> RESOURCE_TYPES = Arrays.asList("iron", "gold", "emerald", "diamond");
	private static final List<String> COLOR_NAMES = Arrays.asList(FWGameConstants.TEAM_NAMES);
	private static final List<String> MAP_SIZES = Arrays.asList("4", "8");

	private final FWGameManager fwGameManager;
	// player -> map they asked to delete and when, so /fwsetup delete has to be run twice to go through
	private final Map<UUID, String> pendingDeleteMap = new HashMap<>();
	private final Map<UUID, Long> pendingDeleteTime = new HashMap<>();

	public FWSetupCommand(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You can't use this in the console!");
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 0) {
			sendUsage(player);
			return true;
		}

		FWMapConfigManager manager = fwGameManager.getMapConfigManager();
		String sub = args[0].toLowerCase();

		// ── Commands that don't require being inside a specific map world ──

		if (sub.equals("list")) {
			sendMapList(player, manager);
			return true;
		}

		if (sub.equals("maplist")) {
			sendPlayableMapList(player, manager);
			return true;
		}

		if (sub.equals("delete")) {
			handleDelete(player, manager, args);
			return true;
		}

		if (sub.equals("tp")) {
			if (args.length < 2) {
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fwsetup tp <mapname>"));
				return true;
			}
			String mapName = args[1];
			World world = Bukkit.getWorld(mapName);
			if (world == null) {
				try {
					world = Bukkit.createWorld(new WorldCreator(mapName)
							.environment(World.Environment.NORMAL)
							.generateStructures(false)
							.generator(new VoidGenerator()));
				} catch (Throwable t) {
					player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rFailed to load world: &e" + t.getMessage()));
					return true;
				}
			}
			if (world == null) {
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rWorld &e" + mapName + "&r doesn't exist."));
				return true;
			}
			FWMapConfig cfg = manager.getOrCreate(mapName);
			Location dest = cfg.getLobby() != null && cfg.getLobby().getWorld() != null
					? cfg.getLobby() : world.getSpawnLocation();
			player.teleport(dest);
			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rTeleported to &e" + mapName + "&r."));
			return true;
		}

		if (sub.equals("createmap")) {
			if (args.length < 2) {
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fwsetup createmap <mapname>"));
				return true;
			}
			String mapName = args[1];
			if (Bukkit.getWorld(mapName) != null) {
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rA world named &e" + mapName + "&r is already loaded!"));
				return true;
			}
			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rCreating void world &e" + mapName + "&r..."));
			World newWorld;
			try {
				newWorld = Bukkit.createWorld(new WorldCreator(mapName)
						.environment(World.Environment.NORMAL)
						.generateStructures(false)
						.generator(new VoidGenerator()));
			} catch (Throwable t) {
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rFailed to create world: &e" + t.getMessage()));
				return true;
			}
			if (newWorld == null) {
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rWorld creation returned null. Check the console."));
				return true;
			}
			newWorld.setSpawnFlags(false, false);
			newWorld.setTime(6000);
			manager.getOrCreate(mapName); // register in the config index
			player.teleport(newWorld.getSpawnLocation());
			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rVoid world &e" + mapName + "&r created! Build away."));
			return true;
		}

		if (sub.equals("generatemap")) {
			if (args.length < 2) {
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fwsetup generatemap <mapname>"));
				return true;
			}
			new FWMapGenerator(fwGameManager).generate(player, args[1]);
			return true;
		}

		if (sub.equals("save")) {
			FWMapConfig cfg = manager.getOrCreate(player.getWorld().getName());
			manager.save(cfg);
			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSaved map config for &e" + player.getWorld().getName() + "&r."));
			return true;
		}

		// ── Commands that operate on whichever map world the player is in ──

		FWMapConfig config = manager.getOrCreate(player.getWorld().getName());
		Location loc = player.getLocation();

		switch (sub) {
			case "size":
				handleSize(player, config, args);
				break;

			case "lobby":
				config.setLobby(loc);
				player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet the lobby point for &e" + player.getWorld().getName() + "&r."));
				break;

			case "spectator":
				config.setSpectatorLocation(loc);
				player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet the spectator spawn point."));
				break;

			case "team":
			case "flag":
			case "shop":
			case "boundary1":
			case "boundary2": {
				Integer siteId = parseTeamColor(player, args, 1);
				if (siteId == null) break;

				FWTeamSite site = config.getTeamSite(siteId);
				boolean newlyCreated = false;
				if (sub.equals("team") && !site.isCreated() && config.countCreatedTeams() >= config.getSize()) {
					player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThis is a " + config.getSize()
							+ "-player map and already has " + config.countCreatedTeams() + " teams. Use &e/fwsetup size 8&r for more."));
					break;
				}
				if (sub.equals("team")) {
					newlyCreated = !site.isCreated();
					site.setSpawn(loc);
					site.setCreated(true);
				}
				else if (sub.equals("flag")) site.setFlagLoc(loc);
				else if (sub.equals("shop")) site.setShopLoc(loc);
				else if (sub.equals("boundary1")) site.setBoundaryCorner1(loc);
				else site.setBoundaryCorner2(loc);

				player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet team &e"
						+ FWGameConstants.TEAM_NAMES[siteId - 1] + "&r's " + sub + " point."
						+ (newlyCreated ? " &7(team created)" : "")));
				break;
			}

			case "generator":
				handleGenerator(player, config, args, loc);
				break;

			case "chesthologram":
				handleChestHologram(player, config, args, loc);
				break;

			case "importantarea":
				handleImportantArea(player, config, args, loc);
				break;

			default:
				sendUsage(player);
				break;
		}

		return true;
	}

	private void handleGenerator(Player player, FWMapConfig config, String[] args, Location loc) {
		if (args.length < 2 || !RESOURCE_TYPES.contains(args[1].toLowerCase())) {
			player.sendMessage(fwGameManager.getMain().color(
					"&c&l(!) &rTry: &e/fwsetup generator <iron|gold|emerald|diamond> <id> [interval|clear]"));
			return;
		}

		String type = args[1].toLowerCase();

		if (type.equals("iron") || type.equals("gold")) {
			Integer siteId = parseTeamColor(player, args, 2);
			if (siteId == null) return;

			FWTeamSite site = config.getTeamSite(siteId);
			if (type.equals("iron")) {
				site.setIronGenerator(loc);
				if (!applyOptionalInterval(player, args, 3, site::setIronGeneratorInterval)) return;
			} else {
				site.setGoldGenerator(loc);
				if (!applyOptionalInterval(player, args, 3, site::setGoldGeneratorInterval)) return;
			}

			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet team &e"
					+ FWGameConstants.TEAM_NAMES[siteId - 1] + "&r's " + type + " generator"
					+ intervalSuffix(args, 3) + "&r."));
			return;
		}

		if (args.length < 3) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fwsetup generator " + type + " <id> [interval|clear]"));
			return;
		}

		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThe id must be a number!"));
			return;
		}

		if (type.equals("emerald")) {
			config.setEmeraldGenerator(id, loc);
			if (!applyOptionalInterval(player, args, 3, seconds -> config.setEmeraldGeneratorInterval(id, seconds))) return;
		} else {
			config.setDiamondGenerator(id, loc);
			if (!applyOptionalInterval(player, args, 3, seconds -> config.setDiamondGeneratorInterval(id, seconds))) return;
		}

		player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet " + type + " generator &e#" + id + "&r"
				+ intervalSuffix(args, 3) + "&r."));
	}

	/**
	 * Parses the optional trailing interval arg at args[index] (whole seconds, or "clear" to remove
	 * an existing override) and hands it to setter. Leaves the existing value untouched if the arg
	 * is absent. Returns false (after messaging the player) only on a genuinely invalid value.
	 */
	private boolean applyOptionalInterval(Player player, String[] args, int index, java.util.function.Consumer<Integer> setter) {
		if (args.length <= index) return true;

		if (args[index].equalsIgnoreCase("clear")) {
			setter.accept(null);
			return true;
		}

		try {
			int seconds = Integer.parseInt(args[index]);
			if (seconds < 1) throw new NumberFormatException();
			setter.accept(seconds);
			return true;
		} catch (NumberFormatException e) {
			player.sendMessage(fwGameManager.getMain().color(
					"&c&l(!) &rThe interval must be a whole number of seconds (or &eclear&r)!"));
			return false;
		}
	}

	/** Player-facing suffix describing what applyOptionalInterval just did, for the confirmation message. */
	private String intervalSuffix(String[] args, int index) {
		if (args.length <= index) return "";
		if (args[index].equalsIgnoreCase("clear")) return " &7(cleared custom interval)";
		return " &7(custom interval: &e" + args[index] + "s&7)";
	}

	private void handleChestHologram(Player player, FWMapConfig config, String[] args, Location loc) {
		if (args.length < 2) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fwsetup chesthologram <id>"));
			return;
		}

		int id;
		try {
			id = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThe id must be a number!"));
			return;
		}

		config.setChestHologram(id, loc);
		player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet chest hologram &e#" + id + "&r."));
	}

	/**
	 * /fwsetup importantarea <color> <id> <X> <Y> <Z> - a no-build area centered where the player stands,
	 * reaching that many blocks in both directions of each axis. "remove" in place of X deletes the area.
	 */
	private void handleImportantArea(Player player, FWMapConfig config, String[] args, Location loc) {
		Integer siteId = parseTeamColor(player, args, 1);
		if (siteId == null) return;

		if (args.length < 3) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e" + IMPORTANT_AREA_USAGE));
			return;
		}

		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThe id must be a number!"));
			return;
		}

		FWTeamSite site = config.getTeamSite(siteId);
		String teamName = FWGameConstants.TEAM_NAMES[siteId - 1];

		if (args.length > 3 && args[3].equalsIgnoreCase("remove")) {
			if (site.removeImportantArea(id)) {
				player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rRemoved team &e" + teamName
						+ "&r's important area &e#" + id + "&r."));
			} else {
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTeam &e" + teamName
						+ "&r has no important area &e#" + id + "&r!"));
			}
			return;
		}

		if (args.length < 6) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e" + IMPORTANT_AREA_USAGE));
			return;
		}

		Integer xRadius = parseAreaRadius(player, args[3], "X");
		Integer yRadius = xRadius == null ? null : parseAreaRadius(player, args[4], "Y");
		Integer zRadius = yRadius == null ? null : parseAreaRadius(player, args[5], "Z");
		if (zRadius == null) return;

		site.setImportantArea(id, new FWImportantArea(loc, xRadius, yRadius, zRadius));
		player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet team &e" + teamName + "&r's important area &e#"
				+ id + "&r &7(" + (xRadius * 2 + 1) + "x" + (yRadius * 2 + 1) + "x" + (zRadius * 2 + 1)
				+ " centered on you)&r."));
	}

	/** A non-negative whole number of blocks for one axis. Null (after messaging) if invalid. */
	private Integer parseAreaRadius(Player player, String arg, String axis) {
		try {
			int value = Integer.parseInt(arg);
			if (value < 0) throw new NumberFormatException();
			return value;
		} catch (NumberFormatException e) {
			player.sendMessage(fwGameManager.getMain().color(
					"&c&l(!) &rThe " + axis + " size must be a whole number of blocks!"));
			return null;
		}
	}

	private Integer parseTeamColor(Player player, String[] args, int index) {
		if (args.length <= index) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rYou must specify a team color: &e"
					+ FWGameConstants.colorNameList()));
			return null;
		}

		Integer siteId = FWGameConstants.siteIdForColorName(args[index]);
		if (siteId == null) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThat's not a valid team color! Try: &e"
					+ FWGameConstants.colorNameList()));
		}
		return siteId;
	}

	/** /fwsetup size <4|8> - a 4-player map has 4 teams, needs all of them filled to start, and takes 3 captures to win. */
	private void handleSize(Player player, FWMapConfig config, String[] args) {
		int size;
		try {
			size = args.length < 2 ? -1 : Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			size = -1;
		}

		if (size != FWMapConfig.SIZE_FOUR && size != FWMapConfig.SIZE_EIGHT) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fwsetup size <4|8> &7(this map is "
					+ config.getSize() + "-player)"));
			return;
		}

		int created = config.countCreatedTeams();
		if (size < created) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThis map already has &e" + created
					+ " &rteams created - a " + size + "-player map can only have " + size + "."));
			return;
		}

		config.setSize(size);
		String rules = size == FWMapConfig.SIZE_FOUR
				? " &7Needs exactly 4 teams, starts once all 4 bases have a player, and takes 3 captures to win."
				: "";
		player.sendMessage(fwGameManager.getMain().color("&2&l(!) &e" + player.getWorld().getName()
				+ " &ris now a &e" + size + "-player &rmap." + rules + " &7Run &e/fwsetup save &7to keep it."));
	}

	/** /fwsetup maplist - playable (fully set up) maps in green, unfinished ones in red. */
	private void sendPlayableMapList(Player player, FWMapConfigManager manager) {
		List<FWMapConfig> all = new ArrayList<>(manager.getAllConfigs());
		int playable = 0;
		for (FWMapConfig cfg : all) {
			if (cfg.isReady()) playable++;
		}

		player.sendMessage(fwGameManager.getMain().color("&6&lFLAG WARS MAPS &7(&a" + playable + " playable&7, &c"
				+ (all.size() - playable) + " not set up&7)"));
		for (FWMapConfig cfg : all) {
			String size = " &7(" + cfg.getSize() + "-player)";
			if (cfg.isReady()) {
				player.sendMessage(fwGameManager.getMain().color("  &a" + cfg.getWorldName() + size));
			} else {
				player.sendMessage(fwGameManager.getMain().color("  &c" + cfg.getWorldName() + size
						+ " &8- missing " + cfg.findMissing().size() + " point(s), see &7/fwsetup list"));
			}
		}
	}

	/** /fwsetup delete <map> - run twice within 15s to confirm. Removes the map's Flag Wars setup, not its world. */
	private void handleDelete(Player player, FWMapConfigManager manager, String[] args) {
		if (args.length < 2) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fwsetup delete <map>"));
			return;
		}

		FWMapConfig cfg = manager.find(args[1]);
		if (cfg == null) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThere's no Flag Wars map called &e" + args[1]
					+ "&r. See &e/fwsetup maplist&r."));
			return;
		}

		String mapName = cfg.getWorldName();
		UUID id = player.getUniqueId();
		Long askedAt = pendingDeleteTime.get(id);
		boolean confirmed = mapName.equals(pendingDeleteMap.get(id)) && askedAt != null
				&& System.currentTimeMillis() - askedAt <= DELETE_CONFIRM_MILLIS;

		if (!confirmed) {
			pendingDeleteMap.put(id, mapName);
			pendingDeleteTime.put(id, System.currentTimeMillis());
			player.sendMessage(fwGameManager.getMain().color("&e&l(!) &rThis deletes &e" + mapName
					+ "&r's whole Flag Wars setup (teams, flags, generators...) and takes it out of rotation."
					+ " The world itself isn't deleted."));
			player.sendMessage(fwGameManager.getMain().color("&e&l(!) &rRun &e/fwsetup delete " + mapName
					+ " &ragain within 15 seconds to confirm."));
			return;
		}

		pendingDeleteMap.remove(id);
		pendingDeleteTime.remove(id);
		manager.delete(mapName);
		player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rDeleted the Flag Wars setup for &e" + mapName + "&r."));
	}

	private void sendMapList(Player player, FWMapConfigManager manager) {
		List<FWMapConfig> all = new ArrayList<>(manager.getAllConfigs());
		player.sendMessage(fwGameManager.getMain().color("&6&lFLAG WARS MAPS &7(" + all.size() + " known)"));
		for (FWMapConfig cfg : all) {
			List<String> missing = cfg.findMissing();
			boolean loaded = Bukkit.getWorld(cfg.getWorldName()) != null;
			String worldTag = (loaded ? "&2[loaded]" : "&8[unloaded]") + " &b[" + cfg.getSize() + "-player]";
			if (missing.isEmpty()) {
				player.sendMessage(fwGameManager.getMain().color("  &e" + cfg.getWorldName()
						+ " " + worldTag + " &a[Ready]"));
			} else {
				player.sendMessage(fwGameManager.getMain().color("  &e" + cfg.getWorldName()
						+ " " + worldTag + " &c[Missing " + missing.size() + " point(s)]"));
			}
		}
	}

	private void sendUsage(Player player) {
		player.sendMessage(fwGameManager.getMain().color("&6&lFWSETUP COMMANDS"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup list &7-> &rShow all maps and their status"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup maplist &7-> &rPlayable maps in &agreen&r, unfinished ones in &cred"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup delete <map> &7-> &rDelete a map's Flag Wars setup (asks to confirm)"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup tp <map> &7-> &rTeleport into a map (loads it if needed)"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup createmap <map> &7-> &rCreate a new void world"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup generatemap <map> &7-> &rBuild a complete, ready-to-play 4-team map"));
		player.sendMessage(fwGameManager.getMain().color("&7(Stand in the target map world for all commands below)"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup size <4|8> &7- 4-player maps need all 4 teams to start and 3 captures to win"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup lobby"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup spectator"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup team <color> &7- also creates that team if it isn't already on this map"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup flag <color>"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup shop <color>"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup boundary1 <color> &7- first corner of that team's island"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup boundary2 <color> &7- opposite corner"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup generator <iron|gold> <color> [interval|clear]"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup generator <emerald|diamond> <id> [interval|clear]"));
		player.sendMessage(fwGameManager.getMain().color("&7  interval (seconds) overrides that generator's timing entirely; &eclear&7 removes the override"));
		player.sendMessage(fwGameManager.getMain().color("&e" + IMPORTANT_AREA_USAGE));
		player.sendMessage(fwGameManager.getMain().color("&7  no-build area centered on you, X/Y/Z blocks out in both"
				+ " directions of each axis (3 = 7 wide); &e<id> remove&7 deletes it"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup chesthologram <id>"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup save &7-> &rSave the current map's config to disk"));
		player.sendMessage(fwGameManager.getMain().color("&7Colors: &e" + FWGameConstants.colorNameList()));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
		}

		if (args.length == 2 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("delete"))) {
			List<String> mapNames = new ArrayList<>(fwGameManager.getMapConfigManager().getAllConfigs()
					.stream().map(FWMapConfig::getWorldName).collect(Collectors.toList()));
			return StringUtil.copyPartialMatches(args[1], mapNames, new ArrayList<>());
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("generator")) {
			return StringUtil.copyPartialMatches(args[1], RESOURCE_TYPES, new ArrayList<>());
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("size")) {
			return StringUtil.copyPartialMatches(args[1], MAP_SIZES, new ArrayList<>());
		}

		if (args.length == 2 && (args[0].equalsIgnoreCase("team") || args[0].equalsIgnoreCase("flag")
				|| args[0].equalsIgnoreCase("shop") || args[0].equalsIgnoreCase("boundary1")
				|| args[0].equalsIgnoreCase("boundary2") || args[0].equalsIgnoreCase("importantarea"))) {
			return StringUtil.copyPartialMatches(args[1], COLOR_NAMES, new ArrayList<>());
		}

		if (args.length == 3 && args[0].equalsIgnoreCase("generator")
				&& (args[1].equalsIgnoreCase("iron") || args[1].equalsIgnoreCase("gold"))) {
			return StringUtil.copyPartialMatches(args[2], COLOR_NAMES, new ArrayList<>());
		}

		if (args.length == 4 && args[0].equalsIgnoreCase("importantarea")) {
			return StringUtil.copyPartialMatches(args[3], Collections.singletonList("remove"), new ArrayList<>());
		}

		if (args.length == 4 && args[0].equalsIgnoreCase("generator")) {
			return StringUtil.copyPartialMatches(args[3], Collections.singletonList("clear"), new ArrayList<>());
		}

		return Collections.emptyList();
	}
}
