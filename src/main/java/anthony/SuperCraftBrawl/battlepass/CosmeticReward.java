package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.PassCosmetic;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reward that permanently unlocks one cosmetic. The icon, name and description come from the
 * cosmetic itself, so they only ever have to be written once (in the cosmetic's class).
 */
class CosmeticReward extends BattlePassReward {

	private final CosmeticCategory category;
	private final String cosmeticId;

	CosmeticReward(CosmeticCategory category, String cosmeticId) {
		super(Material.PAPER, (short) 0, 1, "&7" + cosmeticId, null);
		this.category = category;
		this.cosmeticId = cosmeticId;
	}

	private Cosmetic find(Core main) {
		if (main.getCosmeticManager() == null)
			return null;
		return main.getCosmeticManager().registry().get(category, cosmeticId);
	}

	@Override
	public ItemStack createIcon(Core main, Player player) {
		Cosmetic cosmetic = find(main);
		return cosmetic == null ? super.createIcon(main, player) : cosmetic.getIcon(player).clone();
	}

	@Override
	public String getName(Core main, Player player) {
		Cosmetic cosmetic = find(main);
		if (cosmetic != null) {
			ItemStack icon = cosmetic.getIcon(player);
			if (icon.hasItemMeta() && icon.getItemMeta().hasDisplayName())
				return icon.getItemMeta().getDisplayName();
		}
		return super.getName(main, player);
	}

	@Override
	public String getChatName(Core main, Player player) {
		return getName(main, player) + " &7" + category.label;
	}

	@Override
	public List<String> getDescription(Core main, Player player) {
		List<String> lines = new ArrayList<>();
		lines.add("&7" + category.label); // the type comes first (BundleReward reads it from here)
		Cosmetic cosmetic = find(main);
		if (cosmetic instanceof PassCosmetic) {
			String[] description = ((PassCosmetic) cosmetic).getDescription();
			if (description.length > 0) {
				lines.add("");
				Collections.addAll(lines, description);
			}
		}
		return lines;
	}

	@Override
	public boolean grantsCosmetic(CosmeticCategory category, String cosmeticId) {
		return this.category == category && this.cosmeticId.equals(cosmeticId);
	}

	@Override
	public void grant(Core main, Player player, PlayerData data) {
		BattlePassManager manager = main.getBattlePassManager();
		if (manager == null)
			return;

		manager.unlockCosmetic(player, category, cosmeticId);
		// The name carries its own colors, so only the text around it is colored here
		player.sendMessage(main.color("&aYou unlocked the ") + getName(main, player)
				+ main.color(" &a" + category.label + "! Equip it from &e/cosmetics&a."));
	}
}
