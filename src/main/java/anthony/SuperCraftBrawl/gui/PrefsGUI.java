package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Main;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.RankManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class PrefsGUI implements InventoryProvider {

	public Main main;
	public SmartInventory inv;
	private RankManager rm;

	public PrefsGUI(Main main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Preferences").build();
		this.main = main;
	}

	public RankManager getRankManager() {
		return rm;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		String line = "";

		if (data != null) {
			if (data.cwm == 0) {
				line = main.color("&eEnable");
			} else {
				line = main.color("&cDisable");
			}
			contents.set(1, 2, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.DIAMOND),
					main.color("&eCustom Win Messages"), "", line), e -> {
						if (player.hasPermission("scb.customWin")) {
							if (data.cwm == 1) {
								player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ "You have disabled " + ChatColor.YELLOW + "Custom Win Messages");
								data.cwm = 0;
							} else {
								data.cwm = 1;
								player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ "You have enabled " + ChatColor.YELLOW + "Custom Win Messages");
							}
						} else {
							player.sendMessage(main.color("&c&l(!) &rYou need the rank " + ChatColor.BLUE
									+ ChatColor.BOLD + "CAPTAIN " + "&rto use this feature!"));
						}
						inv.close(player);
					}));
			if (data.pm == 0) {
				line = main.color("&eEnable");
			} else {
				line = main.color("&cDisable");
			}
			contents.set(1, 6,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.PAPER),
							main.color("&ePrivate Messages"), "", line),
							e -> {
								if (data.pm == 0) {
									player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "You have disabled " + ChatColor.YELLOW + "Private Messages");
									data.pm = 1;
								} else {
									data.pm = 0;
									player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "You have enabled " + ChatColor.YELLOW + "Private Messages");
								}
								inv.close(player);
							}));

			if (data.killMsgs == 0) {
				line = main.color("&eEnable");
			} else {
				line = main.color("&cDisable");
			}
			contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.DIAMOND_SWORD),
					main.color("&eCustom Kill Messages"), "", line), e -> {
						if (player.hasPermission("scb.customKillMsgs")) {
							if (data.killMsgs == 0) {
								player.sendMessage(main.color("&r&l(!) &rYou have enabled &eCustom Kill Messages"));
								data.killMsgs = 1;
							} else {
								player.sendMessage(main.color("&r&l(!) &rYou have disabled &eCustom Kill Messages"));
								data.killMsgs = 0;
							}
						} else {
							player.sendMessage(main.color("&c&l(!) &rYou need the rank " + ChatColor.BLUE
									+ ChatColor.BOLD + "CAPTAIN " + "&rto use this feature!"));
						}
						inv.close(player);
					}));
			contents.set(2, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.SKULL_ITEM),
					main.color("&eCustom Death Particles"), "", main.color("&e&lVIP+ &rexclusive!")), e -> {
						if (player.hasPermission("scb.deathParticles")) {
							new DeathParticlesGUI(main).inv.open(player);
						} else {
							player.sendMessage(main.color("&c&l(!) &rYou need the rank " + ChatColor.YELLOW
									+ ChatColor.BOLD + "VIP " + "&rto use this feature!"));
						}
					}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
