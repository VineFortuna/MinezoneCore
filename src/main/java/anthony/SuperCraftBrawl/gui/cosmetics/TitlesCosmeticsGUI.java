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
				main.color("&6&lTrick-or-Treater"), main.color("&7Unlock by finding 2 baskets in the lobby!"), "",
				main.color("&8Progress: &e" + Math.min(basketsFoundForLore, 2) + "&7/2"),
				main.color("&cHalloween 2025 exclusive"));

		contents.set(1, 2, ClickableItem.of(trickOrTreatTitle, e -> {
			int current = (main.getHalloweenManager() != null)
					? main.getHalloweenManager().getFoundCount(player.getUniqueId())
					: 0;

			if (current < 2) {
				player.sendMessage(
						main.color("&c&l(!) &rYou need to find &e2 baskets &rto use &6&lTrick-or-Treater &rtitle."));
				return;
			}

			enableDisableTitle(player, "Trick-or-Treater"); // Enable/disable gadget
			inv.close(player);
		}));

		String freddyTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiMjdjY2I0ZjEyNjQwZjFiNThlYTYyZDkwY2RhY2U0NGMwZjJkYTlmMzkwOGUyNWViMTZiZGI1YmJiNWE2NSJ9fX0";
		ItemStack freddy = ItemHelper.createSkullTexture(freddyTexture, "&6&lFreddy Fazbear",
				"&7Unlock by finding 8 baskets in the lobby!", "",
				main.color("&8Progress: &e" + Math.min(basketsFoundForLore, 8) + "&7/8"), "&cHalloween 2025 Exclusive");

		contents.set(1, 3, ClickableItem.of(freddy, e -> {
			int current = (main.getHalloweenManager() != null)
					? main.getHalloweenManager().getFoundCount(player.getUniqueId())
					: 0;

			if (current < 8) {
				player.sendMessage(
						main.color("&c&l(!) &rYou need to find &e8 baskets &rto use &6&lFreddy Fazbear &rtitle."));
				return;
			}

			enableDisableTitle(player, "Freddy Fazbear"); // Enable/disable gadget
			inv.close(player);
		}));

		ItemStack o_zone = ItemHelper.setDetails(new ItemStack(Material.FIREWORK), main.color("&b&lFIESTA DE LA NOCHE"),
				main.color("&7Go listen to the album DiscO-Zone."), "", main.color("&e&lVIP&8 or higher"));

		contents.set(1, 4, ClickableItem.of(o_zone, e -> {
			if (!player.hasPermission("scb.fiesta")) {
				player.sendMessage(main.color("&c&l(!) &rYou need the rank &e&lVIP&r+ for this!"));
				return;
			}

			enableDisableTitle(player, "Fiesta De La Noche"); // Enable/disable gadget
			inv.close(player);
		}));

		ItemStack wabyink = ItemHelper.setDetails(new ItemStack(Material.LAPIS_BLOCK), main.color("&f&lThe Wabyink Title"),
				main.color("&7The Wabyink Title, the one, and the only."), "", main.color("&8Must be Wabyink."));

		contents.set(1, 5, ClickableItem.of(wabyink, e -> {
			if (!player.getName().equals("Wabyink")) {
				player.sendMessage(main.color("&c&l(!) &rYou need to be Wabyink for this!"));
				return;
			}

			enableDisableTitle(player, "i'm gay btw..."); // Enable/disable gadget
			inv.close(player);
		}));

		contents.set(2, 8, ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}));
	}

	private void enableDisableTitle(Player player, String title) {
		boolean nowEnabled = main.getTrickPacket().toggleTitle(player, title);
		player.sendMessage(main.color(nowEnabled ? "&9&l(!) &rYou have enabled &e" + title + " &rtitle"
				: "&9&l(!) &rYou have disabled &e" + title + " &rtitle"));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
