package anthony.villagerdefense.shop;

import anthony.SuperCraftBrawl.Core;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.resources.VDEconomy;
import anthony.villagerdefense.resources.VDResourceType;
import anthony.villagerdefense.villager.VillagerLevel;
import anthony.util.ItemHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * One purchasable shop entry. The display icon is derived from giveItem so
 * there's a single source of truth for what the item actually looks like.
 * Cost is checked against the buyer's actual inventory, like Hypixel
 * Bedwars - there's no hidden per-team currency.
 *
 * Lore must be colored explicitly with Core#color() before being handed to
 * ItemHelper.setDetails(ItemStack, String, List&lt;String&gt;) - that overload
 * (unlike the String... varargs one) does not color its lore list itself.
 */
public class VDShopItem {

	private final String name;
	private final ItemStack giveItem;
	private final VDResourceType costType;
	private final int cost;
	private final VillagerLevel requiredLevel;

	public VDShopItem(String name, ItemStack giveItem, VDResourceType costType, int cost, VillagerLevel requiredLevel) {
		this.name = name;
		this.giveItem = giveItem;
		this.costType = costType;
		this.cost = cost;
		this.requiredLevel = requiredLevel;
	}

	public String getName() {
		return name;
	}

	public ItemStack getGiveItem() {
		return giveItem.clone();
	}

	public VDResourceType getCostType() {
		return costType;
	}

	public int getCost() {
		return cost;
	}

	public VillagerLevel getRequiredLevel() {
		return requiredLevel;
	}

	public boolean isUnlockedFor(VDTeam team) {
		return team.getLevel().getTier() >= requiredLevel.getTier();
	}

	public boolean isAffordableFor(Player player) {
		return VDEconomy.canAfford(player, costType, cost);
	}

	public ItemStack toDisplayItem(VDTeam team, Player player, Core main) {
		boolean unlocked = isUnlockedFor(team);
		boolean affordable = isAffordableFor(player);

		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(main.color("&7Cost: " + (affordable ? "&a" : "&c") + cost + " " + costType.name()));
		if (requiredLevel.getTier() > 1) {
			lore.add(main.color("&7Requires: &e" + requiredLevel.name()));
		}
		lore.add(main.color(unlocked ? (affordable ? "&a&lClick to purchase" : "&c&lNot enough resources") : "&c&lLocked"));

		String color = unlocked && affordable ? "&a" : "&c";
		return ItemHelper.setDetails(getGiveItem(), main.color(color + name), lore);
	}
}
