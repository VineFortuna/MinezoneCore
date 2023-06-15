package anthony.skywars.kits;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;

public enum Kit {
	Knight("Knight",
			new KitInstance().setKitItems(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.APPLE, 3))),
	Chicken("Chicken", new KitInstance().setKitItems(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.EGG, 8),
			ItemHelper.addEnchant(new ItemStack(Material.IRON_BOOTS), Enchantment.PROTECTION_FALL, 4)));

	public String name = "";
	public KitInstance ki = null;

	Kit(String name, KitInstance ki) {
		this.name = name;
		this.ki = ki;
	}

	public KitInstance getKitInstance() {
		return this.ki;
	}

	public String getKitName() {
		return this.name;
	}

	public ItemStack getMenuItem() {
		ItemStack item = null;
		switch (this) {
		case Knight:
			item = new ItemStack(Material.IRON_SWORD);
			break;
		case Chicken:
			item = new ItemStack(Material.EGG);
			break;
		default:
			item = new ItemStack(Material.WOOD);
			break;
		}

		return item;
	}

	public ArrayList<ItemStack> getKitItems() {
		ArrayList<ItemStack> item = new ArrayList<ItemStack>();
		switch (this) {
		case Knight:
			item.add(new ItemStack(Material.IRON_SWORD));
			item.add(new ItemStack(Material.APPLE, 3));
			break;
		case Chicken:
			item.add(new ItemStack(Material.IRON_SWORD));
			item.add(new ItemStack(Material.EGG, 8));
			item.add(ItemHelper.addEnchant(new ItemStack(Material.IRON_BOOTS), Enchantment.PROTECTION_FALL, 4));
			break;
		default:
			System.out.println("Error when printing kits");
			break;
		}

		return item;
	}
}
