package anthony.villagerdefense.resources;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Resources are literal items sitting in the player's inventory, exactly
 * like Hypixel Bedwars - no hidden per-team currency ledger. Purchases and
 * upgrades check and remove real ItemStacks from the buyer's inventory.
 */
public final class VDEconomy {

	private VDEconomy() {
	}

	public static int count(Player player, VDResourceType type) {
		int total = 0;
		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null && item.getType() == type.getMaterial()) {
				total += item.getAmount();
			}
		}
		return total;
	}

	public static boolean canAfford(Player player, VDResourceType type, int amount) {
		return count(player, type) >= amount;
	}

	public static boolean canAfford(Player player, Map<VDResourceType, Integer> cost) {
		for (Map.Entry<VDResourceType, Integer> entry : cost.entrySet()) {
			if (!canAfford(player, entry.getKey(), entry.getValue())) return false;
		}
		return true;
	}

	/** @return false (and withdraws nothing) if the player can't afford it. */
	public static boolean withdraw(Player player, VDResourceType type, int amount) {
		if (!canAfford(player, type, amount)) return false;

		Inventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		int remaining = amount;

		for (int i = 0; i < contents.length && remaining > 0; i++) {
			ItemStack item = contents[i];
			if (item == null || item.getType() != type.getMaterial()) continue;

			int take = Math.min(item.getAmount(), remaining);
			remaining -= take;

			if (item.getAmount() - take <= 0) {
				inventory.setItem(i, null);
			} else {
				item.setAmount(item.getAmount() - take);
				inventory.setItem(i, item);
			}
		}
		return true;
	}

	/** @return false (and withdraws nothing) if the player can't afford the full cost. */
	public static boolean withdraw(Player player, Map<VDResourceType, Integer> cost) {
		if (!canAfford(player, cost)) return false;
		for (Map.Entry<VDResourceType, Integer> entry : cost.entrySet()) {
			withdraw(player, entry.getKey(), entry.getValue());
		}
		return true;
	}
}
