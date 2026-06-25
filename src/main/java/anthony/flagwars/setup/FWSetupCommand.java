package anthony.flagwars.setup;

import anthony.flagwars.FWGameConstants;
import anthony.flagwars.FWGameManager;
import anthony.flagwars.map.FWMapConfig;
import anthony.flagwars.map.FWTeamSite;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Staff-only tool to capture Flag Wars map coordinates in-game,
 * since they can't be supplied any other way. Walk the map, stand where
 * you want each point, and run the matching subcommand.
 */
public class FWSetupCommand implements CommandExecutor, TabExecutor {

	private static final List<String> SUBCOMMANDS = Arrays.asList(
			"tp", "lobby", "team", "flag", "shop", "generator", "boundary1", "boundary2", "list", "save");
	private static final List<String> RESOURCE_TYPES = Arrays.asList("iron", "gold", "emerald", "diamond");
	private static final List<String> COLOR_NAMES = Arrays.asList(
			"Lime", "Yellow", "Cyan", "Blue", "LightBlue", "White", "Pink", "Gray");

	private final FWGameManager fwGameManager;

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

		// Force the arena world to load (if it hasn't already) before reading any
		// saved Location - FWMapConfig is loaded at plugin startup, before the arena
		// world exists, so saved Locations have a null World until this reloads it.
		org.bukkit.World arenaWorld = fwGameManager.getArenaWorld();
		FWMapConfig config = fwGameManager.getMapConfigManager().getConfig();
		String sub = args[0].toLowerCase();

		if (sub.equals("list")) {
			sendStatus(player, config);
			return true;
		}

		if (sub.equals("save")) {
			fwGameManager.getMapConfigManager().save();
			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSaved Flag Wars map config."));
			return true;
		}

		if (sub.equals("tp")) {
			Location target = config.getLobby();
			if (target == null || target.getWorld() == null) {
				if (arenaWorld == null) {
					player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rFailed to load &e"
							+ FWGameConstants.MAP_WORLD_NAME + "&r. Check the console for errors."));
					return true;
				}
				target = arenaWorld.getSpawnLocation();
			}
			player.teleport(target);
			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rTeleported to &e" + FWGameConstants.MAP_WORLD_NAME + "&r."));
			return true;
		}

		if (!player.getWorld().getName().equals(FWGameConstants.MAP_WORLD_NAME)) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rYou must be standing in the &e"
					+ FWGameConstants.MAP_WORLD_NAME + " &rworld to use this!"));
			return true;
		}

		Location loc = player.getLocation();

		switch (sub) {
			case "lobby":
				config.setLobby(loc);
				player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet the Flag Wars lobby point."));
				break;

			case "team":
			case "flag":
			case "shop":
			case "boundary1":
			case "boundary2": {
				Integer siteId = parseTeamColor(player, args, 1);
				if (siteId == null) break;

				FWTeamSite site = config.getTeamSite(siteId);
				if (sub.equals("team")) site.setSpawn(loc);
				else if (sub.equals("flag")) site.setFlagLoc(loc);
				else if (sub.equals("shop")) site.setShopLoc(loc);
				else if (sub.equals("boundary1")) site.setBoundaryCorner1(loc);
				else site.setBoundaryCorner2(loc);

				player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet team &e"
						+ FWGameConstants.TEAM_NAMES[siteId - 1] + "&r's " + sub + " point."));
				break;
			}

			case "generator":
				handleGenerator(player, config, args, loc);
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
					"&c&l(!) &rTry: &e/fwsetup generator <iron|gold|emerald|diamond> <id>"));
			return;
		}

		String type = args[1].toLowerCase();

		if (type.equals("iron") || type.equals("gold")) {
			Integer siteId = parseTeamColor(player, args, 2);
			if (siteId == null) return;

			FWTeamSite site = config.getTeamSite(siteId);
			if (type.equals("iron")) site.setIronGenerator(loc);
			else site.setGoldGenerator(loc);

			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet team &e"
					+ FWGameConstants.TEAM_NAMES[siteId - 1] + "&r's " + type + " generator."));
			return;
		}

		if (args.length < 3) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fwsetup generator " + type + " <id>"));
			return;
		}

		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThe id must be a number!"));
			return;
		}

		if (type.equals("emerald")) config.setEmeraldGenerator(id, loc);
		else config.setDiamondGenerator(id, loc);

		player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rSet " + type + " generator &e#" + id + "&r."));
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

	private void sendStatus(Player player, FWMapConfig config) {
		List<String> missing = config.findMissing();

		if (missing.isEmpty()) {
			player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rThe Flag Wars map is fully configured!"));
			return;
		}

		player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rMissing " + missing.size() + " setup point(s):"));
		for (String entry : missing) {
			player.sendMessage(fwGameManager.getMain().color("&7- &e" + entry));
		}
	}

	private void sendUsage(Player player) {
		player.sendMessage(fwGameManager.getMain().color("&6&lFWSETUP COMMANDS"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup tp -> &rTeleport into the map to start setting up"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup lobby"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup team <color>"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup flag <color>"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup shop <color>"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup boundary1 <color> &7- &rfirst corner of that team's island"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup boundary2 <color> &7- &ropposite corner"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup generator <iron|gold> <color>"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup generator <emerald|diamond> <id>"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup list"));
		player.sendMessage(fwGameManager.getMain().color("&e/fwsetup save"));
		player.sendMessage(fwGameManager.getMain().color("&7Colors: &e" + FWGameConstants.colorNameList()));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("generator")) {
			return StringUtil.copyPartialMatches(args[1], RESOURCE_TYPES, new ArrayList<>());
		}

		if (args.length == 2 && (args[0].equalsIgnoreCase("team") || args[0].equalsIgnoreCase("flag")
				|| args[0].equalsIgnoreCase("shop") || args[0].equalsIgnoreCase("boundary1")
				|| args[0].equalsIgnoreCase("boundary2"))) {
			return StringUtil.copyPartialMatches(args[1], COLOR_NAMES, new ArrayList<>());
		}

		if (args.length == 3 && args[0].equalsIgnoreCase("generator")
				&& (args[1].equalsIgnoreCase("iron") || args[1].equalsIgnoreCase("gold"))) {
			return StringUtil.copyPartialMatches(args[2], COLOR_NAMES, new ArrayList<>());
		}

		return Collections.emptyList();
	}
}
