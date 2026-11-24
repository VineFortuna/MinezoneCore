package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Several rewards given together on one tier. The first reward supplies the icon. */
class BundleReward extends BattlePassReward {

	private final BattlePassReward[] parts;

	BundleReward(BattlePassReward... parts) {
		super(Material.PAPER, (short) 0, 1, "&7Bundle", null);
		if (parts.length == 0)
			throw new IllegalArgumentException("A bundle needs at least one reward");
		this.parts = parts;
	}

	@Override
	public ItemStack createIcon(Core main, Player player) {
		return parts[0].createIcon(main, player);
	}

	@Override
	public String getName(Core main, Player player) {
		StringBuilder name = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0)
				name.append(" &7+ ");
			name.append(parts[i].getName(main, player));
		}
		return name.toString();
	}

	@Override
	public String getChatName(Core main, Player player) {
		StringBuilder name = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0)
				name.append(" &7+ ");
			name.append(parts[i].getChatName(main, player));
		}
		return name.toString();
	}

	@Override
	public List<String> getDescription(Core main, Player player) {
		List<String> lines = new ArrayList<>();
		lines.add("&7Includes:");
		for (BattlePassReward part : parts) {
			String line = "&8- " + part.getName(main, player);
			List<String> description = part.getDescription(main, player);
			// A cosmetic's first description line is its category (e.g. "Win Effect")
			if (!description.isEmpty())
				line += " &7(" + ChatColor.stripColor(main.color(description.get(0))) + ")";
			lines.add(line);
		}
		return lines;
	}

	@Override
	public boolean grantsCosmetic(CosmeticCategory category, String cosmeticId) {
		for (BattlePassReward part : parts) {
			if (part.grantsCosmetic(category, cosmeticId))
				return true;
		}
		return false;
	}

	@Override
	public void grant(Core main, Player player, PlayerData data) {
		for (BattlePassReward part : parts)
			part.grant(main, player, data);
	}
}
