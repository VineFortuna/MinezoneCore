package anthony.SuperCraftBrawl.gui.christmas;

import java.time.LocalDate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class ChristmasRewardsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public ChristmasRewardsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(2, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Christmas Rewards").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		Material mat = Material.BARRIER;

		if (data != null) {
			checkDay(data);

			if (data.december15 == 1)
				mat = Material.NETHER_STAR;

			contents.set(0, 0,
					ClickableItem.of(
							ItemHelper.setDetails(new ItemStack(mat), "" + ChatColor.RED + ChatColor.BOLD + "DAY 1", "",
									ChatColor.GRAY + "100 Tokens", "",
									"" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM"),
							e -> {
								if (data.december15 == 1) {
									player.sendMessage(main.color("&c&l(&r&l!&c&l) &rYou were given &e100 Tokens!"));
									data.december15 = -1;
									data.tokens += 100;
									main.getScoreboardManager().lobbyBoard(player);
								} else if (data.december15 == 0) {
									player.sendMessage(
											main.color("&c&l(!) &rWait until &eDecember 15 &rto open this!"));
								} else if (data.december15 == -1) {
									player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
								}

							}));

			contents.set(0, 1,
					ClickableItem.of(
							ItemHelper.setDetails(new ItemStack(mat), "" + ChatColor.RED + ChatColor.BOLD + "DAY 2", "",
									ChatColor.GRAY + "Snow Particles Cosmetic", "",
									"" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM"),
							e -> {
								if (data.december16 == 1) {
									player.sendMessage(main.color(
											"&c&l(&r&l!&c&l) &rYou were given the &eSnow Particles &rcosmetic!"));
									data.december16 = -1;
									data.snowParticles = 1;
								} else if (data.december16 == 0) {
									player.sendMessage(
											main.color("&c&l(!) &rWait until &eDecember 16 &rto open this!"));
								} else if (data.december16 == -1) {
									player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
								}
							}));

			contents.set(0, 2,
					ClickableItem.of(
							ItemHelper.setDetails(new ItemStack(mat), "" + ChatColor.RED + ChatColor.BOLD + "DAY 3", "",
									ChatColor.GRAY + "3 Levels", "",
									"" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM"),
							e -> {
								if (data.december17 == 1) {
									player.sendMessage(main.color("&c&l(&r&l!&c&l) &rYou were given &e3 Levels!"));
									data.december17 = -1;
									data.level += 3;
									main.getScoreboardManager().lobbyBoard(player);
								} else if (data.december17 == 0) {
									player.sendMessage(
											main.color("&c&l(!) &rWait until &eDecember 17 &rto open this!"));
								} else if (data.december17 == -1) {
									player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
								}
							}));
			contents.set(0, 3,
					ClickableItem.of(
							ItemHelper.setDetails(new ItemStack(mat), "" + ChatColor.RED + ChatColor.BOLD + "DAY 4", "",
									ChatColor.GRAY + "Snowman Pet", "",
									"" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM"),
							e -> {
								if (data.december18 == 1) {
									player.sendMessage(main.color("&c&l(&r&l!&c&l) &rYou were given &eSnowman &rpet!"));
									data.december18 = -1;
								} else if (data.december18 == 0) {
									player.sendMessage(
											main.color("&c&l(!) &rWait until &eDecember 18 &rto open this!"));
								} else if (data.december18 == -1) {
									player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
								}
							}));
		}
	}

	private int checkRewards() {
		LocalDate today = LocalDate.now();
		int day = 1;

		if (today.getMonthValue() == 12) {
			day = today.getDayOfMonth();
			Bukkit.broadcastMessage("Day: " + day);
		}

		return day;
	}

	private void checkDay(PlayerData data) {
		if (checkRewards() == 15 && data.december15 != -1) {
			data.december15 = 1;
		} else if (checkRewards() == 16) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
		} else if (checkRewards() == 17) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
		} else if (checkRewards() == 18) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
			if (data.december18 != -1)
				data.december18 = 1;
		} else if (checkRewards() == 19) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
			if (data.december18 != -1)
				data.december18 = 1;
			if (data.december19 != -1)
				data.december19 = 1;
		} else if (checkRewards() == 20) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
			if (data.december18 != -1)
				data.december18 = 1;
			if (data.december19 != -1)
				data.december19 = 1;
			if (data.december20 != -1)
				data.december20 = 1;
		} else if (checkRewards() == 21) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
			if (data.december18 != -1)
				data.december18 = 1;
			if (data.december19 != -1)
				data.december19 = 1;
			if (data.december20 != -1)
				data.december20 = 1;
			if (data.december21 != -1)
				data.december21 = 1;
		} else if (checkRewards() == 22) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
			if (data.december18 != -1)
				data.december18 = 1;
			if (data.december19 != -1)
				data.december19 = 1;
			if (data.december20 != -1)
				data.december20 = 1;
			if (data.december21 != -1)
				data.december21 = 1;
			if (data.december22 != -1)
				data.december22 = 1;
		} else if (checkRewards() == 23) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
			if (data.december18 != -1)
				data.december18 = 1;
			if (data.december19 != -1)
				data.december19 = 1;
			if (data.december20 != -1)
				data.december20 = 1;
			if (data.december21 != -1)
				data.december21 = 1;
			if (data.december22 != -1)
				data.december22 = 1;
			if (data.december23 != -1)
				data.december23 = 1;
		} else if (checkRewards() == 24) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
			if (data.december18 != -1)
				data.december18 = 1;
			if (data.december19 != -1)
				data.december19 = 1;
			if (data.december20 != -1)
				data.december20 = 1;
			if (data.december21 != -1)
				data.december21 = 1;
			if (data.december22 != -1)
				data.december22 = 1;
			if (data.december23 != -1)
				data.december23 = 1;
			if (data.december24 != -1)
				data.december24 = 1;
		} else if (checkRewards() == 25) {
			if (data.december15 != -1)
				data.december15 = 1;
			if (data.december16 != -1)
				data.december16 = 1;
			if (data.december17 != -1)
				data.december17 = 1;
			if (data.december18 != -1)
				data.december18 = 1;
			if (data.december19 != -1)
				data.december19 = 1;
			if (data.december20 != -1)
				data.december20 = 1;
			if (data.december21 != -1)
				data.december21 = 1;
			if (data.december22 != -1)
				data.december22 = 1;
			if (data.december23 != -1)
				data.december23 = 1;
			if (data.december24 != -1)
				data.december24 = 1;
			if (data.december25 != -1)
				data.december25 = 1;
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
