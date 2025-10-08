package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TitlesCosmeticsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public TitlesCosmeticsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Titles").parent(parent).build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.fill(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		final int basketsFoundForLore = (main.getHalloweenManager() != null)
				? main.getHalloweenManager().getFoundCount(player.getUniqueId())
				: 0;
		
		// Trick-or-Treater title gadget
		ItemStack trickOrTreatTitle = ItemHelper.setDetails(new ItemStack(Material.PUMPKIN),
				main.color("&6&lTrick-or-Treater"), "",
				main.color("&7Unlock by finding 2 baskets in the lobby!"),
				main.color("&8Progress: &e" + Math.min(basketsFoundForLore, 2) + "&7/2"),
				"",
				main.color("&cHalloween 2025 exclusive"));

		contents.set(1, 4, ClickableItem.of(trickOrTreatTitle, e -> {
			int current = (main.getHalloweenManager() != null)
					? main.getHalloweenManager().getFoundCount(player.getUniqueId())
					: 0;

			if (current < 2) {
				player.sendMessage(
						main.color("&c&l(!) &rYou need to find &e2 baskets &rto use &6&lTrick-or-Treater &rtitle."));
				player.sendMessage(main.color("&7Progress: &e" + current + "&7/10"));
				return;
			}

			enableDisableTrickTitle(player); // Enable/disable gadget
			inv.close(player);
		}));

		contents.set(2, 8, ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}));
	}

	private void enableDisableTrickTitle(Player player) {
	    // pick ONE: TrickPacket or TrickTitle — using TrickPacket here
	    boolean enabled = main.getTrickPacket().isEnabled(player);

	    if (enabled) {
	        main.getTrickPacket().disable(player);
	        player.sendMessage(main.color("&r&l(!) &rYou have &cdisabled &eTrick-or-Treater &rtitle"));
	    } else {
	        main.getTrickPacket().enable(player);
	        player.sendMessage(main.color("&r&l(!) &rYou have &aenabled &eTrick-or-Treater &rtitle"));
	    }
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
