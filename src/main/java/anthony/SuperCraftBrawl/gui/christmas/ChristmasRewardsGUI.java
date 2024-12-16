package anthony.SuperCraftBrawl.gui.christmas;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDate;

public class ChristmasRewardsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	public final String OPENED = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FhMDNmODE3MzQxY2FhNTZmYmZjNmQzZjI4MTFhZGI5ODliNjExNzgxMTYyYzEyYWY0YzU4YWYxOGM1M2M3MiJ9fX0=";
	public final String UNLOCKED = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmIxZWM3ZGM3NTMwNjFjYTE3NDQyNGVhNDVjZjk0OTBiMzljZDVkY2NhNDc3ZDEzOGE2MDNlNmJlNzU1ZWM3MiJ9fX0=";
	public final String LOCKED = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjFiYzlkNDJiMDA0MWU4Zjk1Y2I5YjI2NjI4ZmRhZjUwY2QwZTM2ZjdiYjlkNmIzYTRkMmFmMzk0OWRhOTdkNiJ9fX0=";

	public ChristmasRewardsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(2, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Christmas Rewards").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		Material mat = Material.ENDER_CHEST;
		String claim;

		if (data != null) {
			checkDay(data);
			
			if (data.december15 == 1)
				mat = Material.CHEST;
			
			if (data.december15 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 0, ClickableItem.of(ItemHelper.createSkullTexture(getTexture(data.december15),
					"" + ChatColor.RED + ChatColor.BOLD + "DAY 1", "", ChatColor.GRAY + "100 Tokens", "", claim), e -> {
				if (data.december15 == 1) {
					player.sendMessage(main.color("&c&l(&r&l!&c&l) &rYou were given &e100 Tokens!"));
					data.december15 = -1;
					data.tokens += 100;
					main.getScoreboardManager().lobbyBoard(player);
					inv.close(player);
				} else if (data.december15 == 0) {
					player.sendMessage(main.color("&c&l(!) &rWait until &eDecember 15 &rto open this!"));
				} else if (data.december15 == -1) {
					player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
				}
				
			}));
			
			mat = Material.ENDER_CHEST;
			if (data.december16 == 1)
				mat = Material.CHEST;
			
			if (data.december16 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 1,
					ClickableItem
							.of(ItemHelper.createSkullTexture(getTexture(data.december16),
									"" + ChatColor.RED + ChatColor.BOLD + "DAY 2", "", ChatColor.GRAY + "Snow Particles Cosmetic", "", claim), e -> {
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
			
			mat = Material.ENDER_CHEST;
			if (data.december17 == 1)
				mat = Material.CHEST;
			
			if (data.december17 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 2,
					ClickableItem.of(ItemHelper.createSkullTexture(getTexture(data.december17),
									"" + ChatColor.RED + ChatColor.BOLD + "DAY 3", "", ChatColor.GRAY + "3 Levels", "", claim),
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
			
			mat = Material.ENDER_CHEST;
			if (data.december18 == 1)
				mat = Material.CHEST;
			if (data.december18 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 3,
					ClickableItem
							.of(ItemHelper.createSkullTexture(getTexture(data.december18),
											"" + ChatColor.RED + ChatColor.BOLD + "DAY 4", "", ChatColor.GRAY + "Snowman Pet", "", claim),
									e -> {
										if (data.december18 == 1) {
											player.sendMessage(
													main.color("&c&l(&r&l!&c&l) &rYou were given &eSnowman &rpet!"));
											data.december18 = -1;
										} else if (data.december18 == 0) {
											player.sendMessage(
													main.color("&c&l(!) &rWait until &eDecember 18 &rto open this!"));
										} else if (data.december18 == -1) {
											player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
										}
									}));
			
			mat = Material.ENDER_CHEST;
			if (data.december19 == 1)
				mat = Material.CHEST;
			
			if (data.december19 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 4,
					ClickableItem
							.of(ItemHelper.createSkullTexture(getTexture(data.december19),
											"" + ChatColor.RED + ChatColor.BOLD + "DAY 5", "", ChatColor.GRAY + "Candy Cane Swirl Cosmetic", "", claim),
									e -> {
										if (data.december19 == 1) {
											player.sendMessage(main.color(
													"&c&l(&r&l!&c&l) &rYou were given &eCandy Cane Swirl &rcosmetic!"));
											data.december19 = -1;
										} else if (data.december19 == 0) {
											player.sendMessage(
													main.color("&c&l(!) &rWait until &eDecember 19 &rto open this!"));
										} else if (data.december19 == -1) {
											player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
										}
									}));
			
			mat = Material.ENDER_CHEST;
			if (data.december20 == 1)
				mat = Material.CHEST;
			
			if (data.december20 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 5,
					ClickableItem
							.of(ItemHelper.createSkullTexture(getTexture(data.december20),
											"" + ChatColor.RED + ChatColor.BOLD + "DAY 6", "", ChatColor.GRAY + "Elf Cosmetic", "", claim),
									e -> {
										if (data.december20 == 1) {
											player.sendMessage(
													main.color("&c&l(&r&l!&c&l) &rYou were given &eElf &rcosmetic!"));
											data.december20 = -1;
											data.elfCosmetic = 1;
										} else if (data.december20 == 0) {
											player.sendMessage(
													main.color("&c&l(!) &rWait until &eDecember 20 &rto open this!"));
										} else if (data.december20 == -1) {
											player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
										}
									}));
			
			mat = Material.ENDER_CHEST;
			if (data.december21 == 1)
				mat = Material.CHEST;
			
			if (data.december21 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 6,
					ClickableItem
							.of(ItemHelper.createSkullTexture(getTexture(data.december21),
											"" + ChatColor.RED + ChatColor.BOLD + "DAY 7", "", ChatColor.GRAY + "Ninja Class", "", claim),
									e -> {
										if (data.december21 == 1) {
											player.sendMessage(
													main.color("&c&l(&r&l!&c&l) &rYou were given &eNinja &rclass!"));
											data.december21 = -1;
											int classID = 5;
											PlayerData playerData = main.getDataManager().getPlayerData(player);
											ClassDetails details = playerData.playerClasses.get(classID);
											
											if (details == null) {
												details = new ClassDetails();
												playerData.playerClasses.put(classID, details);
											}
											details.setPurchased();
										} else if (data.december21 == 0) {
											player.sendMessage(
													main.color("&c&l(!) &rWait until &eDecember 21 &rto open this!"));
										} else if (data.december21 == -1) {
											player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
										}
									}));
			
			mat = Material.ENDER_CHEST;
			if (data.december22 == 1)
				mat = Material.CHEST;
			
			if (data.december22 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 7,
					ClickableItem.of(ItemHelper.createSkullTexture(getTexture(data.december22),
									"" + ChatColor.RED + ChatColor.BOLD + "DAY 8", "", ChatColor.GRAY + "3 Levels", "", claim),
							e -> {
								if (data.december22 == 1) {
									player.sendMessage(main.color("&c&l(&r&l!&c&l) &rYou were given &e3 Levels!"));
									data.december22 = -1;
									data.level += 3;
									main.getScoreboardManager().lobbyBoard(player);
								} else if (data.december22 == 0) {
									player.sendMessage(
											main.color("&c&l(!) &rWait until &eDecember 22 &rto open this!"));
								} else if (data.december22 == -1) {
									player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
								}
							}));
			
			mat = Material.ENDER_CHEST;
			if (data.december23 == 1)
				mat = Material.CHEST;
			
			if (data.december23 == -1)
				claim = main.color("&a&lCLAIMED");
			else
				claim = "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "CLICK TO CLAIM";
			
			contents.set(0, 8,
					ClickableItem
							.of(ItemHelper.createSkullTexture(getTexture(data.december23),
											"" + ChatColor.RED + ChatColor.BOLD + "DAY 9", "", ChatColor.GRAY + "Snowball Death Effect", "", claim),
									e -> {
										if (data.december23 == 1) {
											player.sendMessage(main
													.color("&c&l(&r&l!&c&l) &rYou were given &eSnowball Death Effect"));
											data.december23 = -1;
											data.snowballDeathEffect = 0;
										} else if (data.december23 == 0) {
											player.sendMessage(
													main.color("&c&l(!) &rWait until &eDecember 23 &rto open this!"));
										} else if (data.december23 == -1) {
											player.sendMessage(main.color("&c&l(!) &rYou already claimed this gift!"));
										}
									}));
			contents.set(1, 0,
					ClickableItem
							.of(ItemHelper.createSkullTexture(getTexture(data.december24),
											"" + ChatColor.RED + ChatColor.BOLD + "DAY 10", "", ChatColor.GRAY + "???", "", claim),
									e -> {
										if (data.december24 == 1) {
											player.sendMessage(main
													.color("&c&l(&r&l!&c&l) &rYou were given &eSnowball Death Effect"));
											data.december24 = -1;
											data.snowballDeathEffect = 0;
										} else if (data.december24 == 0) {
											player.sendMessage(
													main.color("&c&l(!) &rWait until &eDecember 24 &rto open this!"));
										} else if (data.december24 == -1) {
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
	
	public String getTexture(int i) {
		if (i == 1)
			return UNLOCKED;
		if (i == -1)
			return OPENED;
		return LOCKED;
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
