package anthony.villagerdefense.setup;

import anthony.villagerdefense.VDGameConstants;
import anthony.villagerdefense.VDGameManager;
import anthony.villagerdefense.map.VDMapConfig;
import anthony.villagerdefense.map.VDTeamSite;
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
 * Staff-only tool to capture VillagerDefense map coordinates in-game,
 * since they can't be supplied any other way. Walk the map, stand where
 * you want each point, and run the matching subcommand.
 */
public class VDSetupCommand implements CommandExecutor, TabExecutor {

	private static final List<String> SUBCOMMANDS = Arrays.asList(
			"lobby", "team", "villager", "shop", "generator", "list", "save");
	private static final List<String> RESOURCE_TYPES = Arrays.asList("iron", "gold", "emerald", "diamond");

	private final VDGameManager vdGameManager;

	public VDSetupCommand(VDGameManager vdGameManager) {
		this.vdGameManager = vdGameManager;
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

		VDMapConfig config = vdGameManager.getMapConfigManager().getConfig();
		String sub = args[0].toLowerCase();

		if (sub.equals("list")) {
			sendStatus(player, config);
			return true;
		}

		if (sub.equals("save")) {
			vdGameManager.getMapConfigManager().save();
			player.sendMessage(vdGameManager.getMain().color("&2&l(!) &rSaved VillagerDefense map config."));
			return true;
		}

		if (!player.getWorld().getName().equals(VDGameConstants.MAP_WORLD_NAME)) {
			player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rYou must be standing in the &e"
					+ VDGameConstants.MAP_WORLD_NAME + " &rworld to use this!"));
			return true;
		}

		Location loc = player.getLocation();

		switch (sub) {
			case "lobby":
				config.setLobby(loc);
				player.sendMessage(vdGameManager.getMain().color("&2&l(!) &rSet the VillagerDefense lobby point."));
				break;

			case "team":
			case "villager":
			case "shop": {
				Integer siteId = parseSiteId(player, args, 1);
				if (siteId == null) break;

				VDTeamSite site = config.getTeamSite(siteId);
				if (sub.equals("team")) site.setSpawn(loc);
				else if (sub.equals("villager")) site.setVillagerLoc(loc);
				else site.setShopLoc(loc);

				player.sendMessage(vdGameManager.getMain().color("&2&l(!) &rSet team &e" + siteId + "&r's " + sub + " point."));
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

	private void handleGenerator(Player player, VDMapConfig config, String[] args, Location loc) {
		if (args.length < 2 || !RESOURCE_TYPES.contains(args[1].toLowerCase())) {
			player.sendMessage(vdGameManager.getMain().color(
					"&c&l(!) &rTry: &e/vdsetup generator <iron|gold|emerald|diamond> <id>"));
			return;
		}

		String type = args[1].toLowerCase();

		if (type.equals("iron") || type.equals("gold")) {
			Integer siteId = parseSiteId(player, args, 2);
			if (siteId == null) return;

			VDTeamSite site = config.getTeamSite(siteId);
			if (type.equals("iron")) site.setIronGenerator(loc);
			else site.setGoldGenerator(loc);

			player.sendMessage(vdGameManager.getMain().color("&2&l(!) &rSet team &e" + siteId + "&r's " + type + " generator."));
			return;
		}

		if (args.length < 3) {
			player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rTry: &e/vdsetup generator " + type + " <id>"));
			return;
		}

		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rThe id must be a number!"));
			return;
		}

		if (type.equals("emerald")) config.setEmeraldGenerator(id, loc);
		else config.setDiamondGenerator(id, loc);

		player.sendMessage(vdGameManager.getMain().color("&2&l(!) &rSet " + type + " generator &e#" + id + "&r."));
	}

	private Integer parseSiteId(Player player, String[] args, int index) {
		if (args.length <= index) {
			player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rYou must specify a team id (1-"
					+ VDMapConfig.TEAM_COUNT + ")."));
			return null;
		}

		try {
			int id = Integer.parseInt(args[index]);
			if (id < 1 || id > VDMapConfig.TEAM_COUNT) {
				player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rTeam id must be between 1 and "
						+ VDMapConfig.TEAM_COUNT + "."));
				return null;
			}
			return id;
		} catch (NumberFormatException e) {
			player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rThat's not a valid team id!"));
			return null;
		}
	}

	private void sendStatus(Player player, VDMapConfig config) {
		List<String> missing = config.findMissing();

		if (missing.isEmpty()) {
			player.sendMessage(vdGameManager.getMain().color("&2&l(!) &rThe VillagerDefense map is fully configured!"));
			return;
		}

		player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rMissing " + missing.size() + " setup point(s):"));
		for (String entry : missing) {
			player.sendMessage(vdGameManager.getMain().color("&7- &e" + entry));
		}
	}

	private void sendUsage(Player player) {
		player.sendMessage(vdGameManager.getMain().color("&6&lVDSETUP COMMANDS"));
		player.sendMessage(vdGameManager.getMain().color("&e/vdsetup lobby"));
		player.sendMessage(vdGameManager.getMain().color("&e/vdsetup team <1-8>"));
		player.sendMessage(vdGameManager.getMain().color("&e/vdsetup villager <1-8>"));
		player.sendMessage(vdGameManager.getMain().color("&e/vdsetup shop <1-8>"));
		player.sendMessage(vdGameManager.getMain().color("&e/vdsetup generator <iron|gold> <1-8>"));
		player.sendMessage(vdGameManager.getMain().color("&e/vdsetup generator <emerald|diamond> <id>"));
		player.sendMessage(vdGameManager.getMain().color("&e/vdsetup list"));
		player.sendMessage(vdGameManager.getMain().color("&e/vdsetup save"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("generator")) {
			return StringUtil.copyPartialMatches(args[1], RESOURCE_TYPES, new ArrayList<>());
		}

		return Collections.emptyList();
	}
}
