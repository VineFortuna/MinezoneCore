package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DeathEffectsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public DeathEffectsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Death Effects").parent(parent).build();
		this.main = main;
	}

	private void resetData(PlayerData data) {
		data.goldApple = 0;
		data.glowstone = 0;
		data.redstone = 0;
		data.web = 0;
		data.bottleEXP = 0;
		data.snowball = 0;
		data.pumpkinPie = 0;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		final int basketsFoundForLore = (main.getHalloweenManager() != null)
				? main.getHalloweenManager().getFoundCount(player.getUniqueId())
				: 0;

		// Icons Items
		ItemStack goldenApple = ItemHelper.create(Material.GOLDEN_APPLE, ChatColor.YELLOW + "Golden Apple");
		ItemStack glowstone = ItemHelper.create(Material.GLOWSTONE_DUST, ChatColor.YELLOW + "Glowstone");
		ItemStack redstone = ItemHelper.create(Material.REDSTONE, ChatColor.YELLOW + "Redstone");
		ItemStack cobweb = ItemHelper.create(Material.WEB, ChatColor.YELLOW + "Cobweb");
		ItemStack expBottle = ItemHelper.create(Material.EXP_BOTTLE, ChatColor.YELLOW + "Exp Bottle");
		ItemStack snowball = ItemHelper.setDetails(ItemHelper.create(Material.SNOW_BALL), ChatColor.YELLOW + "Snowball",
				"", main.color("&cChristmas 2024 exclusive"));
		ItemStack pumpkinPie = ItemHelper.setDetails(ItemHelper.create(Material.PUMPKIN_PIE),
				main.color("&6Pumpkin Pie"), "",
				main.color("&7Unlock by finding 7 baskets in the lobby!"),
				main.color("&7Progress: &e" + Math.min(basketsFoundForLore, 7) + "&7/7"),
				"",
				main.color("&cHalloween 2025 exclusive"));

		// Setting Items
		contents.fillBorders(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		if (data != null) {
			// Golden Apple
			contents.set(1, 1, ClickableItem.of(goldenApple, e -> {
				if (data.goldApple == 0) {
					this.resetData(data);
					data.goldApple = 1;
					player.sendMessage(main.color("&9&l(!) &rYou have enabled &eGolden Apple Death Particle"));
				} else {
					this.resetData(data);
					player.sendMessage(main.color("&9&l(!) &rYou have disabled &eGolden Apple Death Particle"));
				}
			}));

			// Glowstone
			contents.set(1, 2, ClickableItem.of(glowstone, e -> {
				if (data.glowstone == 0) {
					this.resetData(data);
					data.glowstone = 1;
					player.sendMessage(main.color("&9&l(!) &rYou have enabled &eGlowstone Dust Death Particle"));
				} else {
					this.resetData(data);
					player.sendMessage(main.color("&9&l(!) &rYou have disabled &eGlowstone Dust Death Particle"));
				}
			}));

			// Redstone
			contents.set(1, 3, ClickableItem.of(redstone, e -> {
				if (data.redstone == 0) {
					this.resetData(data);
					data.redstone = 1;
					player.sendMessage(main.color("&9&l(!) &rYou have enabled &eRedstone Death Particle"));
				} else {
					this.resetData(data);
					player.sendMessage(main.color("&9&l(!) &rYou have disabled &eRedstone Death Particle"));
				}
			}));

			// Cobweb
			contents.set(1, 4, ClickableItem.of(cobweb, e -> {
				if (data.web == 0) {
					this.resetData(data);
					data.web = 1;
					player.sendMessage(main.color("&9&l(!) &rYou have enabled &eCobweb Death Particle"));
				} else {
					this.resetData(data);
					player.sendMessage(main.color("&9&l(!) &rYou have disabled &eCobweb Death Particle"));
				}
			}));

			// Exp Bottle
			contents.set(1, 5, ClickableItem.of(expBottle, e -> {
				if (data.bottleEXP == 0) {
					this.resetData(data);
					data.bottleEXP = 1;
					player.sendMessage(main.color("&9&l(!) &rYou have enabled &eBottle o' enchanting Death Particle"));
				} else {
					this.resetData(data);
					player.sendMessage(main.color("&9&l(!) &rYou have disabled &eBottle o' enchanting Death Particle"));
				}
			}));
			contents.set(1, 6, ClickableItem.of(snowball, e -> {
				if (data.snowballDeathEffect == 1) {
					if (data.snowball == 0) {
						this.resetData(data);
						data.snowball = 1;
						player.sendMessage(main.color("&9&l(!) &rYou have enabled &eSnowball Death Particle"));
					} else {
						this.resetData(data);
						player.sendMessage(main.color("&9&l(!) &rYou have disabled &eSnowball Death Particle"));
					}
				} else {
					player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this yet!"));
				}
			}));
			contents.set(1, 7, ClickableItem.of(pumpkinPie, e -> {
				pumpkinPieEffect(player, data);
			}));

			contents.set(2, 8, ClickableItem
					.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
						inv.getParent().get().open(player);
					}));
		}
	}
	
	private void pumpkinPieEffect(Player player, PlayerData data) {
		int progress = main.getListener().getHalloweenEventProgress(player);
		
		if (progress >= 7) {
			if (data.pumpkinPie == 0) {
				resetData(data);
				data.pumpkinPie = 1;
				player.sendMessage(main.color("&9&l(!) &rYou have enabled &ePumpkin Pie &rdeath particle"));
			} else {
				resetData(data);
				player.sendMessage(main.color("&9&l(!) &rYou have disabled &ePumpkin Pie &rdeath particle"));
			}
		} else
			player.sendMessage(main.color("&c&l(!) &rYou need &e7&r baskets to unlock this!"));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
