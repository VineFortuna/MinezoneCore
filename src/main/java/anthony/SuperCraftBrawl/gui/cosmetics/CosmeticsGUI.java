package anthony.SuperCraftBrawl.gui.cosmetics;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.RankManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class CosmeticsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private RankManager rm;

	// Creating Cosmetics Inventory
	public CosmeticsGUI(Core main) {
		inv = SmartInventory.builder()
				.id("myInventory")
				.provider(this)
				.size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Cosmetics")
				.build();
		this.main = main;
	}

	public RankManager getRankManager() {
		return rm;
	}

	public ItemStack colorArmor(ItemStack armour, Color c) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(c);
		armour.setItemMeta(lm);
		return armour;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		// Items
		ItemStack lobby = ItemHelper.create(Material.BOOKSHELF, ChatColor.YELLOW + "Lobby Cosmetics");

		ItemStack ingame = ItemHelper.create(Material.ENDER_PORTAL, ChatColor.YELLOW + "Game Cosmetics");

		// Setting Items
		contents.set(1, 2, ClickableItem.of(
				lobby,
				e -> {
					inv.close(player);
					new LobbyCosmeticsGUI(main).inv.open(player);
				}));

		contents.set(1, 6, ClickableItem.of(
				ingame,
				e -> {
					inv.close(player);
					new GameCosmeticsGUI(main).inv.open(player);
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
