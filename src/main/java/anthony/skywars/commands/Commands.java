package anthony.skywars.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.skywars.GameInstance;
import anthony.skywars.GameState;

public class Commands implements CommandExecutor {

	private Core main;

	public Commands(Core main) {
		this.main = main;
	}

	private void removeArmor(Player player) {
		ItemStack air = new ItemStack(Material.AIR, 1);
		player.getInventory().setHelmet(air);
		player.getInventory().setChestplate(air);
		player.getInventory().setLeggings(air);
		player.getInventory().setBoots(air);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			switch (cmd.getName().toLowerCase()) {
			case "join":
				if (args.length > 0) {
					String map = args[0];

					for (anthony.skywars.Maps maps : anthony.skywars.Maps.values()) {
						if (maps.toString().equalsIgnoreCase(map)) {
							main.getSWManager().JoinGame(player, maps);
							return true;
						}
					}

					player.sendMessage(main.color("&c&l(!) &rThis map does not exist!"));
				} else
					player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/join <map>"));
				break;

			case "spectate":
				if (args.length > 0) {
					String map = args[0];

					for (anthony.skywars.Maps maps : anthony.skywars.Maps.values()) {
						if (maps.toString().equalsIgnoreCase(map)) {
							main.getSWManager().SpectateMap(player, maps);
							return true;
						}
					}
				} else
					player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/spectate <map>"));
				break;

			case "leave":
				this.leaveGame(player);
				break;
			case "l":
				this.leaveGame(player);
				break;
			case "kit":
				GameInstance i = main.getSWManager().getInstanceOfPlayer(player);

				if (i != null) {
					if (i.getState() == GameState.LOBBY) {
						if (args.length > 0) {
							String kitName = args[0];

							for (anthony.skywars.kits.Kit kits : anthony.skywars.kits.Kit.values()) {
								if (kits.toString().equalsIgnoreCase(kitName)) {
									i.selectedKit.put(player, kits);
									player.sendMessage(main.color("&2&l(!) &rYou have selected &e" + kits.toString()));
									return true;
								}
							}
						} else
							player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/kit <kitname>"));
					} else
						player.sendMessage(main.color("&c&l(!) &rYou cannot use this command in game!"));
				} else
					player.sendMessage(main.color("&c&l(!) &rYou cannot select a kit in lobby!"));
				break;
			}
		}
		return true;
	}

	private void leaveGame(Player player) {
		GameInstance i = main.getSWManager().getInstanceOfPlayer(player);
		GameInstance i2 = main.getSWManager().getInstanceOfSpectator(player);
		player.spigot().setCollidesWithEntities(true);
		player.setAllowFlight(false);
		player.setAllowFlight(true);

		for (Player p : Bukkit.getOnlinePlayers()) {
			player.showPlayer(p);
			p.showPlayer(player);
		}

		if (i != null && i.getState() == GameState.ENDED) {
			return;
		} else if (main.getSWManager().removePlayer(player)) {
			player.teleport(main.LobbyLoc());
			player.setHealth(20.0f);
			player.getInventory().clear();
			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlight(true);
			main.LobbyItems(player);
			player.setGameMode(GameMode.ADVENTURE);
			main.LobbyBoard(player);
			player.getInventory().clear();
			main.LobbyItems(player);
			player.sendMessage(main.color("&2&l(!) &rYou have left your game"));
			for (PotionEffect type : player.getActivePotionEffects())
				player.removePotionEffect(type.getType());
			main.sendScoreboardUpdate(player);
			player.setGameMode(GameMode.ADVENTURE);
			removeArmor(player);
			main.mysteryChestHologram(player);
		} else if (i2 != null && i2.hasSpectator(player)) {
			main.ResetPlayer(player);
			player.setGameMode(GameMode.ADVENTURE);
			main.LobbyBoard(player);
			player.getInventory().clear();
			main.LobbyItems(player);
			player.sendMessage(main.color("&2&l(!) &rYou have left your game"));
			for (PotionEffect type : player.getActivePotionEffects())
				player.removePotionEffect(type.getType());
			main.sendScoreboardUpdate(player);
			player.setGameMode(GameMode.ADVENTURE);
			removeArmor(player);
		} else
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
	}

	private ItemStack enchantments(ItemStack item, Enchantment ench, int level) {
		item.addUnsafeEnchantment(ench, level);
		return item;
	}

	private Material testMaterial(String st) {
		try {
			return Material.getMaterial(st.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}

	private Enchantment testEnchant(String st) {
		try {
			return Enchantment.getByName(st.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}

}
