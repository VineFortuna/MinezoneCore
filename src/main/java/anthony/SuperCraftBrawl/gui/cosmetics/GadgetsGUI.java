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
		broomList.add(main.color("&7Fly around like a Witch!"));
		broomList.add("");
		broomList.add(Rank.PRO.getTag() + ChatColor.RESET + " exclusive!");
		ItemStack broom = ItemHelper.create(Material.WHEAT,
				main.color("&6Magic Broom"), broomList);

		// Melon
		List<String> melonList = new ArrayList<>();
		melonList.add(main.color("&7A delicious melon that gives you..."));
		melonList.add(main.color("&7Superpowers!"));
		melonList.add("");
        melonList.add(main.color("&fYou have &a" + data.melon + "&f melons"));
        ItemStack melon = ItemHelper.create(Material.MELON,main.color("&aMelons"),
                melonList);

		// Paintball
		List<String> paintballList = new ArrayList<>();
		paintballList.add(main.color("&7Shoot paintballs as you want"));
		paintballList.add("");
        paintballList.add(main.color("&fYou have &a" +  data.paintball + "&f paintballs"));
        ItemStack paintball = ItemHelper.create(Material.GOLD_BARDING,
                main.color("&9Paintball Gun"), paintballList);

		// Fishing
		ItemStack fishingRod = main.getFishingRod(player);

		ItemStack snowball = ItemHelper.setDetails(ItemHelper.create(Material.SNOW_BALL), "&bSnow Particles", "",
				"&cChristmas 2024 exclusive");

		ItemStack snowmanPet = ItemHelper.setDetails(ItemHelper.create(Material.MONSTER_EGG), "&bSnowman Pet", "",
				"&cChristmas 2024 exclusive");

		String candyCaneTexture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM4M2E0MmU4MmNkNmE3MGUyMTZkOWE4YzJmZjZmMWU1ZTViMjU2Y2VhM2I4Y2QyMjU0NzIzOTNhYTNlY2E1YSJ9fX0=";
		ItemStack candyCane = ItemHelper.createSkullTexture(candyCaneTexture, "&cCandy &fCane &cSwirl", "",
				"&cChristmas 2024 exclusive");

		// Setting Items
		contents.fillBorders(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		// Broom Gadget
		contents.set(1, 1, ClickableItem.of(broom, e -> {
			if (player.hasPermission("scb.wheat")) {
				if (!(player.getInventory().contains(broom))) {
					player.getInventory().setItem(5, broom);
                    player.sendMessage(main.color("&9&l(!) &rYou have equipped &6Magic Broom"));
					inv.close(player);
				} else if (player.getInventory().contains(broom)) {
					player.getInventory().remove(broom);
                    player.sendMessage(main.color("&9&l(!) &rYou have unequipped &6Magic Broom"));
					inv.close(player);
				}
			} else
                player.sendMessage(main.color("&c&l(!) &rYou need the rank " + Rank.PRO.getTag() + "&f to use this!"));
		}));

		// Paintball Gadget
		contents.set(1, 2, ClickableItem.of(paintball, e -> {
			if (data.paintball > 0) {
				if (!(player.getInventory().contains(Material.GOLD_BARDING))) {
                    ItemStack p = ItemHelper.setDetails(new ItemStack(Material.GOLD_BARDING),
                            main.color("&9Paintball Gun"),
                            main.color("&7Right click to shoot a paintball!"));
					player.getInventory().setItem(5, p);
                    player.sendMessage(main.color("&9&l(!) &rYou have equipped &9Paintball Gun"));
				} else {
					player.getInventory().remove(Material.GOLD_BARDING);
                    player.sendMessage(main.color("&9&l(!) &rYou have unequipped &9Paintball Gun"));
				}
			} else
                player.sendMessage(main.color("&c&l(!) &rYou do not have any paintballs! Go collect from MysteryChests"));
			inv.close(player);
		}));

		// Melon Gadget
		contents.set(1, 3, ClickableItem.of(melon, e -> {
			if (data.melon > 0) {
				if (!(player.getInventory().contains(melon))) {
					player.getInventory().setItem(5, melon);
                    player.sendMessage(main.color("&9&l(!) &rYou have equipped &aMelons"));
					inv.close(player);
				} else if (player.getInventory().contains(melon)) {
					player.getInventory().remove(melon);
                    player.sendMessage(main.color("&9&l(!) &rYou have unequipped &aMelons"));
				}
			} else {
                player.sendMessage(main.color("&c&l(!) &rYou do not have any melons! Go collect from MysteryChests"));
			}
			inv.close(player);
		}));
		// Fishing Rod
		contents.set(1, 4, ClickableItem.of(fishingRod, e -> {
			if (!(player.getInventory().contains(fishingRod))) {
				player.getInventory().setItem(5, fishingRod);
                player.sendMessage(main.color("&9&l(!) &rYou have equipped &bFishing Rod"));
				inv.close(player);
			} else if (player.getInventory().contains(fishingRod)) {
				player.getInventory().remove(fishingRod);
                player.sendMessage(main.color("&9&l(!) &rYou have unequipped &bFishing Rod"));
				inv.close(player);
			}
		}));

		contents.set(1, 5, ClickableItem.of(snowball, e -> {
			if (data.snowParticles == 1) {
				if (!(main.getListener().snowParticlePlayers.contains(player))) {
					player.sendMessage(main.color("&r&l(!) &fYou equipped &bSnow Particles &fgadget"));
					main.getListener().snowParticlePlayers.add(player);
				} else {
					player.sendMessage(main.color("&r&l(!) &rYou unequipped &bSnow Particles &rgadget"));
					main.getListener().snowParticlePlayers.remove(player);
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou do not have this gadget!"));
			}
		}));

		contents.set(1, 6, ClickableItem.of(snowmanPet, e -> {
			if (data.snowmanPet == 1) {
				if (!(main.getListener().snowmanPetPlayers.containsKey(player))) {
					player.sendMessage(main.color("&9&l(!) &fYou equipped &bSnowman &fpet"));
					Location spawnLoc = player.getLocation().add(1, 0, 1);
					Snowman snowman = player.getWorld().spawn(spawnLoc, Snowman.class);
					snowman.setCustomName(ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Snowman");
					main.getListener().snowmanPetPlayers.put(player, snowman);
					main.getListener().snowmanPet(player);
				} else {
					player.sendMessage(main.color("&9&l(!) &fYou removed &bSnowman &fpet"));
					main.getListener().snowmanPetPlayers.get(player).remove();
					main.getListener().snowmanPetPlayers.remove(player);
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou do not have this gadget!"));
			}
		}));

		contents.set(1, 7, ClickableItem.of(candyCane, e -> {
			if (data.candycaneParticles == 1) {
				if (!(main.getListener().candyCaneSwirlPlayers.contains(player))) {
					player.sendMessage(main.color("&9&l(!) &fYou equipped &cCandy &fCane &cSwirl &fgadget"));
					main.getListener().candyCaneSwirlPlayers.add(player);
					main.getListener().candyCaneSwirlCosmetic(player);
				} else {
					player.sendMessage(main.color("&9&l(!) &fYou removed &cCandy &fCane &cSwirl &fgadget"));
					main.getListener().candyCaneSwirlPlayers.remove(player);
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou do not have this gadget!"));
			}
		}));

		// --- Candy Aura cosmetic (unlocks at 4 baskets) ---
		// Snapshot for icon/lore at open time (final so lambdas are happy)
		final int basketsFoundForLore = (main.getHalloweenManager() != null)
				? main.getHalloweenManager().getFoundCount(player.getUniqueId())
				: 0;
		List<String> candyAuraLore = new ArrayList<>();
		candyAuraLore.add(main.color("&7Unlock by finding 4 baskets in the lobby!"));
		candyAuraLore.add("");
		candyAuraLore.add(main.color("&fProgress: &a" + basketsFoundForLore + "/4"));
        candyAuraLore.add("");
		candyAuraLore.add(main.color("&cHalloween 2025 exclusive"));

        // Icon & name
		ItemStack candyAuraIcon = ItemHelper.setDetails(ItemHelper.create(Material.SUGAR),
				main.color("&dCandy Aura"), "",
				main.color("&7Unlock by finding 4 baskets in the lobby!"),
				main.color("&fProgress: &a" + Math.min(basketsFoundForLore, 4) + "/4"),
				"",
				main.color("&cHalloween 2025 exclusive"));

		// Place it in the GUI (slot row=1, col=0 is free in your layout)
		contents.set(2, 1, ClickableItem.of(candyAuraIcon, e -> {
			// Recompute live progress in case they found more while GUI was open
			int current = (main.getHalloweenManager() != null)
					? main.getHalloweenManager().getFoundCount(player.getUniqueId())
					: 0;

			if (current < 4) {
				player.sendMessage(main.color("&c&l(!) &rYou need to find &e4 baskets &rto use &dCandy Aura&r."));
				player.sendMessage(main.color("&fProgress: &a" + current + "/10"));
				return;
			}

			toggleCandyAura(player);
			inv.close(player);
		}));

		contents.set(3, 8, ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}));
	}
	
	private void toggleCandyAura(Player player) {
		if (main.getCandyAuraManager().isEnabled(player)) {
			main.getCandyAuraManager().disable(player);
			player.sendMessage(main.color("&9&l(!) &rYou have unequipped &eCandy Aura &rgadget"));
		} else {
			main.getCandyAuraManager().enable(player);
			player.sendMessage(main.color("&9&l(!) &rYou have equipped &eCandy Aura &rgadget"));
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
