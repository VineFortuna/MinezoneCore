package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.MatchStats;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GameStatsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	public MatchStats stats;

	public GameStatsGUI(Core main, MatchStats stats) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Match Stats").build();
		this.main = main;
		this.stats = stats;
	}

	private MatchStats.Row matchMvp(List<MatchStats.Row> rows) {
		MatchStats.Row matchMvp = null;
		for (MatchStats.Row row : rows) {
			if (matchMvp == null || row.kills > matchMvp.kills)
				matchMvp = row;
			else if (row.kills == matchMvp.kills) {
				if (row.winner || row.deaths < matchMvp.deaths)
					matchMvp = row;
			}
		}
		if (matchMvp != null && matchMvp.kills == 0)
			matchMvp = null;

		return matchMvp;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int x = 0, y = 0;

		if (stats != null) {
			List<MatchStats.Row> rows = stats.getRows();
			MatchStats.Row mvp = matchMvp(rows);

			for (MatchStats.Row row : rows) {
				Player rowPlayer = Bukkit.getPlayer(row.uuid);
				if (rowPlayer != null && rowPlayer.isOnline()) {
					ItemStack item = ItemHelper.createSkullHeadPlayer(1, row.name);

					String rank = main.getRankManager().getRank(rowPlayer).getTagWithSpace();

					if (mvp == row) {
						contents.set(y, x,
								ClickableItem.of(
										ItemHelper.setDetails(item, main.color("&e&lMATCH MVP"), "",
												main.color("&rPlayer: " + rank + "&a" + row.name),
												main.color("&rClass: " + row.classTag),
												main.color("&rKills: &a" + row.kills),
												main.color("&rDeaths: &a" + row.deaths)),
										e -> {
										}));
					} else {
						contents.set(y, x,
								ClickableItem.of(
										ItemHelper.setDetails(item,
												main.color("&rPlayer: " + rank + "&a" + row.name),
												main.color("&rClass: " + row.classTag),
												main.color("&rKills: &a" + row.kills),
												main.color("&rDeaths: &a" + row.deaths)),
										e -> {
										}));
					}
					if (row.firstBlood) {
						ItemHelper.setDetails(item, item.getItemMeta().getDisplayName(), item.getItemMeta().getLore(),
								main.color("&eFirst Blood"));
					}

					contents.set(y, x,
							ClickableItem.of(item, e -> {
							}));

					x++;

					if (x > 8) {
						y++;
						x = 0;
					}
				}
			}
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
