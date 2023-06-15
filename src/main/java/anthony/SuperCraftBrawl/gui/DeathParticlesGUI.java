package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class DeathParticlesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public DeathParticlesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Death Particles").build();
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

		if (data != null) {
			contents.set(0, 0, ClickableItem.of(
					ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE), "" + ChatColor.YELLOW + "Golden Apple"),
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

			contents.set(0, 1, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.GLOWSTONE_DUST),
					"" + ChatColor.YELLOW + "Glowstone Dust"), e -> {
						if (data.glowstone == 0) {
							this.resetData(data);
							data.glowstone = 1;
							player.sendMessage(main.color("&9&l(!) &rYou have enabled &eGlowstone Dust Death Particle"));
						} else {
							this.resetData(data);
							player.sendMessage(main.color("&9&l(!) &rYou have disabled &eGlowstone Dust Death Particle"));
						}
					}));
			contents.set(0, 2, ClickableItem.of(
					ItemHelper.setDetails(new ItemStack(Material.REDSTONE), "" + ChatColor.YELLOW + "Redstone"), e -> {
						if (data.redstone == 0) {
							this.resetData(data);
							data.redstone = 1;
							player.sendMessage(main.color("&9&l(!) &rYou have enabled &eRedstone Death Particle"));
						} else {
							this.resetData(data);
							player.sendMessage(main.color("&9&l(!) &rYou have disabled &eRedstone Death Particle"));
						}
					}));
			contents.set(0, 3, ClickableItem
					.of(ItemHelper.setDetails(new ItemStack(Material.WEB), "" + ChatColor.YELLOW + "Cobweb"), e -> {
						if (data.web == 0) {
							this.resetData(data);
							data.web = 1;
							player.sendMessage(main.color("&9&l(!) &rYou have enabled &eCobweb Death Particle"));
						} else {
							this.resetData(data);
							player.sendMessage(main.color("&9&l(!) &rYou have disabled &eCobweb Death Particle"));
						}
					}));
			contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EXP_BOTTLE),
					"" + ChatColor.YELLOW + "Bottle o' enchanting"), e -> {
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