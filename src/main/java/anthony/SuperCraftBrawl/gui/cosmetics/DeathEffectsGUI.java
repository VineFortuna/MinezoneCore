package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DeathEffectsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public DeathEffectsGUI(Core main) {
		inv = SmartInventory.builder()
				.id("myInventory")
				.provider(this)
				.size(6, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Death Effects")
				.build();
		this.main = main;
	}

	private void resetData(PlayerData data) {
		data.goldApple = 0;
		data.glowstone = 0;
		data.redstone = 0;
		data.web = 0;
		data.bottleEXP = 0;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		// Icons Items
		ItemStack lockedCosmetic = ItemHelper.createDye(DyeColor.GRAY, 1, ChatColor.GRAY + "&&&&&&&");

		ItemStack goldenApple = ItemHelper.create(Material.GOLDEN_APPLE, org.bukkit.ChatColor.YELLOW + "Golden Apple");
		ItemStack glowstone = ItemHelper.create(Material.GLOWSTONE_DUST, org.bukkit.ChatColor.YELLOW + "Glowstone");
		ItemStack redstone = ItemHelper.create(Material.REDSTONE, org.bukkit.ChatColor.YELLOW + "Redstone");
		ItemStack cobweb = ItemHelper.create(Material.WEB, org.bukkit.ChatColor.YELLOW + "Cobweb");
		ItemStack expBottle = ItemHelper.create(Material.EXP_BOTTLE, org.bukkit.ChatColor.YELLOW + "Exp Bottle");


		// Setting Items
		contents.fillRect(1,1, 7,7, ClickableItem.of(
				lockedCosmetic,
				e -> {

				}));

		if (data != null) {
			// Golden Apple
			contents.set(1, 1, ClickableItem.of(
					goldenApple,
					e -> {
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
			contents.set(1, 2, ClickableItem.of(
					glowstone,
					e -> {
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
			contents.set(1, 3, ClickableItem.of(
					redstone,
					e -> {
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
			contents.set(1, 4, ClickableItem.of(
					cobweb,
					e -> {
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
			contents.set(1, 5, ClickableItem.of(
					expBottle,
					e -> {
						if (data.bottleEXP == 0) {
							this.resetData(data);
							data.bottleEXP = 1;
							player.sendMessage(main.color("&9&l(!) &rYou have enabled &eBottle o' enchanting Death Particle"));
						} else {
							this.resetData(data);
							player.sendMessage(main.color("&9&l(!) &rYou have disabled &eBottle o' enchanting Death Particle"));
						}
					}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
