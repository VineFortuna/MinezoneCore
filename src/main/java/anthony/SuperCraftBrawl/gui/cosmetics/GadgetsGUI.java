package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

public class GadgetsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public GadgetsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(4, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Gadgets").parent(parent).build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		// Icon Items
		// Broom
		List<String> broomList = new ArrayList<>();
		broomList.add(ChatColor.DARK_GRAY + "Fly around like a Witch!");
		broomList.add("");
		broomList.add(Rank.CAPTAIN.getTag() + ChatColor.RESET + "+ exclusive!");
		ItemStack broom = ItemHelper.create(Material.WHEAT,
				ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Magic Broom", broomList);

		// Melon
		List<String> melonList = new ArrayList<>();
		melonList.add(ChatColor.DARK_GRAY + "A delicious melon that gives you...");
		melonList.add(ChatColor.DARK_GRAY + "                  Superpowers!");
		melonList.add("");
		melonList.add(ChatColor.RESET + "You have " + ChatColor.YELLOW + data.melon + ChatColor.RESET + " Melons");
		ItemStack melon = ItemHelper.create(Material.MELON, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Melons",
				melonList);

		// Paintball
		List<String> paintballList = new ArrayList<>();
		paintballList.add(ChatColor.DARK_GRAY + "Shoot paintballs as you want");
		paintballList.add("");
		paintballList.add(
				ChatColor.RESET + "You have " + ChatColor.YELLOW + data.paintball + ChatColor.RESET + " Paintballs");
		ItemStack paintball = ItemHelper.create(Material.GOLD_BARDING,
				ChatColor.YELLOW.toString() + ChatColor.BOLD + "Paintball Gun", paintballList);

		// Fishing
		ItemStack fishingRod = main.getFishingRod(player);

		ItemStack snowball = ItemHelper.setDetails(ItemHelper.create(Material.SNOW_BALL), "&r&lSnow Particles", "",
				"&cChristmas exclusive");

		ItemStack snowmanPet = ItemHelper.setDetails(ItemHelper.create(Material.MONSTER_EGG), "&e&lSnowman Pet", "",
				"&cChristmas exclusive");

		String candyCaneTexture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM4M2E0MmU4MmNkNmE3MGUyMTZkOWE4YzJmZjZmMWU1ZTViMjU2Y2VhM2I4Y2QyMjU0NzIzOTNhYTNlY2E1YSJ9fX0=";
		ItemStack candyCane = ItemHelper.createSkullTexture(candyCaneTexture, "&c&lCandy &r&lCane &c&lSwirl", "",
				"&cChristmas exclusive");

		// Setting Items
		contents.fillBorders(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		// Broom Gadget
		contents.set(1, 1, ClickableItem.of(broom, e -> {
			if (player.hasPermission("scb.wheat")) {
				if (!(player.getInventory().contains(broom))) {
					player.getInventory().setItem(5, broom);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have equipped " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
					inv.close(player);
				} else if (player.getInventory().contains(broom)) {
					player.getInventory().remove(broom);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have unequipped " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
					inv.close(player);
				}
			} else {
				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
						+ Rank.CAPTAIN.getTag() + ChatColor.RESET + " to use this item!");
			}
		}));

		// Paintball Gadget
		contents.set(1, 2, ClickableItem.of(paintball, e -> {
			if (data.paintball > 0) {
				if (!(player.getInventory().contains(Material.GOLD_BARDING))) {
					ItemStack p = ItemHelper.setDetails(new ItemStack(Material.GOLD_BARDING),
							"" + ChatColor.RESET + ChatColor.GREEN + "Paintball Gun", "",
							"" + ChatColor.RESET + ChatColor.GRAY + "Right click to shoot a paintball!");
					player.getInventory().setItem(5, p);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have equipped " + ChatColor.GREEN + "Paintball Gun");
				} else {
					player.getInventory().remove(Material.GOLD_BARDING);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have unequipped " + ChatColor.GREEN + "Paintball Gun");
				}
			} else {
				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You do not have enough paintballs!");
			}
			inv.close(player);
		}));

		// Melon Gadget
		contents.set(1, 3, ClickableItem.of(melon, e -> {
			if (data.melon > 0) {
				if (!(player.getInventory().contains(melon))) {
					player.getInventory().setItem(5, melon);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have equipped " + ChatColor.YELLOW + "Melons");
					inv.close(player);
				} else if (player.getInventory().contains(melon)) {
					player.getInventory().remove(melon);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have unequipped " + ChatColor.YELLOW + "Melons");
				}
			} else {
				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You do not have enough melons!");
			}
			inv.close(player);
		}));
		// Fishing Rod
		contents.set(1, 4, ClickableItem.of(fishingRod, e -> {
			if (!(player.getInventory().contains(fishingRod))) {
				player.getInventory().setItem(5, fishingRod);
				player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have equipped " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Fishing Rod");
				inv.close(player);
			} else if (player.getInventory().contains(fishingRod)) {
				player.getInventory().remove(fishingRod);
				player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have unequipped " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Fishing Rod");
				inv.close(player);
			}
		}));

		contents.set(1, 5, ClickableItem.of(snowball, e -> {
			if (data.snowParticles == 1) {
				if (!(main.getListener().snowParticlePlayers.contains(player))) {
					player.sendMessage(main.color("&r&l(!) &rYou equipped &eSnow Particles &rgadget"));
					main.getListener().snowParticlePlayers.add(player);
				} else {
					player.sendMessage(main.color("&r&l(!) &rYou removed &eSnow Particles &rgadget"));
					main.getListener().snowParticlePlayers.remove(player);
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this gadget yet!"));
			}
		}));

		contents.set(1, 6, ClickableItem.of(snowmanPet, e -> {
			if (data.snowmanPet == 1) {
				if (!(main.getListener().snowmanPetPlayers.containsKey(player))) {
					player.sendMessage(main.color("&r&l(!) &rYou equipped &eSnowman &rpet"));
					Location spawnLoc = player.getLocation().add(1, 0, 1);
					Snowman snowman = player.getWorld().spawn(spawnLoc, Snowman.class);
					snowman.setCustomName(ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Snowman");
					main.getListener().snowmanPetPlayers.put(player, snowman);
					main.getListener().snowmanPet(player);
				} else {
					player.sendMessage(main.color("&r&l(!) &rYou removed &eSnowman &rpet"));
					main.getListener().snowmanPetPlayers.get(player).remove();
					main.getListener().snowmanPetPlayers.remove(player);
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this gadget yet!"));
			}
		}));

		contents.set(1, 7, ClickableItem.of(candyCane, e -> {
			if (data.candycaneParticles == 1) {
				if (!(main.getListener().candyCaneSwirlPlayers.contains(player))) {
					player.sendMessage(main.color("&r&l(!) &rYou equipped &eCandy Cane Swirl &rgadget"));
					main.getListener().candyCaneSwirlPlayers.add(player);
					main.getListener().candyCaneSwirlCosmetic(player);
				} else {
					player.sendMessage(main.color("&r&l(!) &rYou removed &eCandy Cane Swirl &rgadget"));
					main.getListener().candyCaneSwirlPlayers.remove(player);
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this gadget yet!"));
			}
		}));

		// --- Candy Aura cosmetic (unlocks at 4 baskets) ---
		// Snapshot for icon/lore at open time (final so lambdas are happy)
		final int basketsFoundForLore = (main.getHalloweenManager() != null)
				? main.getHalloweenManager().getFoundCount(player.getUniqueId())
				: 0;
		final boolean candyAuraUnlockedAtOpen = basketsFoundForLore >= 4;

		List<String> candyAuraLore = new ArrayList<>();
		if (candyAuraUnlockedAtOpen) {
			candyAuraLore.add(ChatColor.DARK_GRAY + "A sweet particle swirl around you.");
			candyAuraLore.add("");
			candyAuraLore.add(ChatColor.GRAY + "Click to " + (ChatColor.LIGHT_PURPLE + "toggle"));
		} else {
			candyAuraLore.add(ChatColor.DARK_GRAY + "Unlock by finding 4 baskets in the lobby!");
			candyAuraLore.add("");
			candyAuraLore.add(
					ChatColor.GRAY + "Progress: " + ChatColor.YELLOW + basketsFoundForLore + ChatColor.GRAY + "/4");
			candyAuraLore.add(main.color("&7Progress: &e" + basketsFoundForLore + "&7/4"));
			candyAuraLore.add("");
			candyAuraLore.add(main.color("&cHalloween 2025 Exclusive"));
		}

		// Icon & name
		ItemStack candyAuraIcon = ItemHelper.create(Material.SUGAR,
				ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Candy Aura", candyAuraLore);

		// Place it in the GUI (slot row=1, col=0 is free in your layout)
		contents.set(2, 1, ClickableItem.of(candyAuraIcon, e -> {
			// Recompute live progress in case they found more while GUI was open
			int current = (main.getHalloweenManager() != null)
					? main.getHalloweenManager().getFoundCount(player.getUniqueId())
					: 0;

			if (current < 4) {
				player.sendMessage(main.color("&c&l(!) &rYou need to find &e4 baskets &rto use &dCandy Aura&r."));
				player.sendMessage(main.color("&7Progress: &e" + current + "&7/10"));
				return;
			}

			player.performCommand("candyaura");
			inv.close(player);
		}));

		contents.set(2, 8, ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}));
	}

	/*
	 * This function checks whether the Trick-or-Treater title should be
	 * enabled/disabled
	 */
	private void enableDisableTrickTitle(Player player) {
		if (main.getTrickTitle().isEnabled(player)) {
			player.sendMessage(main.color("&r&l(!) &rYou have disabled &eTrick-or-Treater &rtitle"));
			main.getTrickPacket().enable(player);
		} else {
			player.sendMessage(main.color("&r&l(!) &rYou have enabled &eTrick-or-Treater &rtitle"));
			main.getTrickPacket().disable(player);
		}
	}

	private ItemStack getDyedArmor(Material material, Color color, String name) {
		ItemStack item = new ItemStack(material);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(color);
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
