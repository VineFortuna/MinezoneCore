package anthony.skywars.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class KitInstance {
	
	public List<ItemStack> kitItems = new ArrayList<ItemStack>();

	public KitInstance() {
		
	}
	
	public KitInstance setKitItems(ItemStack...items) {
		for (ItemStack item : items)
			this.kitItems.add(item);
		
		return this;
	}
}
