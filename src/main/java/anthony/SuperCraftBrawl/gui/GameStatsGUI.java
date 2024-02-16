package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map.Entry;

public class GameStatsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	public GameInstance i;

	public GameStatsGUI(Core main, GameInstance i) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Match Stats").build();
		this.main = main;
		this.i = i;
	}
	
	private BaseClass matchMvp() {
		BaseClass matchMvp = null;
		for (Entry<Player, BaseClass> entry : i.allClasses.entrySet()) {
			if (entry.getKey() != null) {
				if (matchMvp == null || entry.getValue().totalKills > matchMvp.totalKills)
					matchMvp = entry.getValue();
				else if (entry.getValue().totalKills == matchMvp.totalKills) {
					if (i.getWinnerList().contains(entry.getKey()) ||
							entry.getValue().totalDeaths < matchMvp.totalDeaths)
						matchMvp = entry.getValue();
				}
			}
		}
		if (matchMvp.totalKills == 0)
			matchMvp = null;
		
		return matchMvp;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int x = 0, y = 0;
		
		if (i != null) {
			for (Entry<Player, BaseClass> entry : i.allClasses.entrySet()) {
				if (entry.getKey() != null) {
					ItemStack stats = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
					SkullMeta statsMeta = (SkullMeta) stats.getItemMeta();
					statsMeta.setOwner(entry.getKey().getName());
					stats.setItemMeta(statsMeta);
					
					String rank = main.getRankManager().getRank(entry.getKey()).getTagWithSpace();
					
					if (matchMvp() != null && matchMvp() == entry.getValue()) {
						contents.set(y, x,
								ClickableItem.of(
										ItemHelper.setDetails(stats, main.color("&e&lMATCH MVP"), "",
												main.color("&ePlayer: " + rank + "&r" + entry.getKey().getName()),
												main.color("&eClass: "
														+ i.allClasses.get(entry.getKey()).getType().getTag()),
												main.color("&eKills: &r" + i.allClasses.get(entry.getKey()).totalKills),
												main.color(
														"&eDeaths: &r" + i.allClasses.get(entry.getKey()).totalDeaths)),
										e -> {
										}));
					} else {
						contents.set(y, x,
								ClickableItem.of(
										ItemHelper.setDetails(stats,
												main.color("&ePlayer: " + rank + "&r" + entry.getKey().getName()),
												main.color("&eClass: "
														+ i.allClasses.get(entry.getKey()).getType().getTag()),
												main.color("&eKills: &r" + i.allClasses.get(entry.getKey()).totalKills),
												main.color(
														"&eDeaths: &r" + i.allClasses.get(entry.getKey()).totalDeaths)),
										e -> {
										}));
					}
					if (isFirstBlood(entry.getKey(), i)) {
						statsMeta = (SkullMeta) stats.getItemMeta();
						ItemHelper.setDetails(stats, statsMeta.getDisplayName(), statsMeta.getLore(),
								main.color("&eFirst Blood"));
					}
					
					contents.set(y, x,
							ClickableItem.of(stats, e -> {
							}));
				}
				
				x++;
				
				if (x > 8) {
					y++;
					x = 0;
				}
			}
		}
	}

	private boolean isFirstBlood(Player gamePlayer, GameInstance i) {
		if (i.firstBlood == gamePlayer)
			return true;
		return false;
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
