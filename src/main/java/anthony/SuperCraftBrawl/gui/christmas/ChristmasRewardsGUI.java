package anthony.SuperCraftBrawl.gui.christmas;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.christmas.ChristmasRewardDAO;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.time.*;
import java.util.UUID;

public class ChristmasRewardsGUI implements InventoryProvider {

	private final Core main;
	private final ChristmasRewardDAO dao;

	public SmartInventory inv;

	public final String OPENED =
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FhMDNmODE3MzQxY2FhNTZmYmZjNmQzZjI4MTFhZGI5ODliNjExNzgxMTYyYzEyYWY0YzU4YWYxOGM1M2M3MiJ9fX0=";
	public final String UNLOCKED =
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmIxZWM3ZGM3NTMwNjFjYTE3NDQyNGVhNDVjZjk0OTBiMzljZDVkY2NhNDc3ZDEzOGE2MDNlNmJlNzU1ZWM3MiJ9fX0=";
	public final String LOCKED =
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjFiYzlkNDJiMDA0MWU4Zjk1Y2I5YjI2NjI4ZmRhZjUwY2QwZTM2ZjdiYjlkNmIzYTRkMmFmMzk0OWRhOTdkNiJ9fX0=";

	public ChristmasRewardsGUI(Core main) {
		this.main = main;
		this.dao = new ChristmasRewardDAO(main);

		this.inv = SmartInventory.builder()
				.id("christmasRewards")
				.provider(this)
				.size(4, 9)
				.title(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Christmas Advent Calendar")
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) return;

		UUID uuid = player.getUniqueId();

		LocalDate todayEST = LocalDate.now(ZoneId.of("America/New_York"));
		int currentUnlockDay = getUnlockDay(todayEST);

		// Rewards 1–9 on row 1; reward 10 on row 2, slot 0
		for (int rewardDay = 1; rewardDay <= 10; rewardDay++) {

			int row;
			int col;

			if (rewardDay <= 5) {
				row = 1;
				col = rewardDay + 1; // columns 2–6
			} else {
				row = 2;
				col = (rewardDay - 5) + 1; // columns 2–6
			}

			boolean unlocked = rewardDay <= currentUnlockDay;
			boolean claimed = dao.hasClaimed(uuid, rewardDay);

			LocalDate unlockDate = LocalDate.of(todayEST.getYear(), 12, 16).plusDays(rewardDay - 1);

			String title = ChatColor.RED + "" + ChatColor.BOLD + "DAY " + rewardDay;
			String rewardDesc = ChatColor.GRAY + getRewardDescription(rewardDay);

			String stateLine;
			if (claimed)
				stateLine = main.color("&a&lCLAIMED");
			else if (unlocked)
				stateLine = main.color("&e&lCLICK TO CLAIM");
			else
				stateLine = main.color("&cUnlocks on &e" + unlockDate.getMonthValue() + "/" + unlockDate.getDayOfMonth());

			String texture = getTexture(unlocked, claimed);

			int finalDay = rewardDay;

			contents.set(row, col, ClickableItem.of(
					ItemHelper.createSkullTexture(
							texture, title, "", rewardDesc, "", stateLine
					),
					e -> {
						if (!unlocked) {
							player.sendMessage(main.color("&c&l(!) &rYou cannot claim this yet!"));
							return;
						}

						if (claimed) {
							player.sendMessage(main.color("&c&l(!) &rYou already claimed this reward!"));
							return;
						}

						grantReward(finalDay, player, data);
						dao.claim(uuid, finalDay);

						inv.close(player);
						inv.open(player); // refresh GUI
					}
			));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

	private int getUnlockDay(LocalDate todayEST) {
		LocalDate first = LocalDate.of(todayEST.getYear(), 12, 16);
		LocalDate last  = LocalDate.of(todayEST.getYear(), 12, 25);

		if (todayEST.isBefore(first)) return 0;
		if (todayEST.isAfter(last)) return 10;

		return (int) (todayEST.toEpochDay() - first.toEpochDay()) + 1;
	}

	private String getTexture(boolean unlocked, boolean claimed) {
		if (claimed) return OPENED;
		if (unlocked) return UNLOCKED;
		return LOCKED;
	}

	private String getRewardDescription(int day) {
		switch (day) {
			case 1: return "100 Tokens";
			case 2: return "Snow Particles Cosmetic";
			case 3: return "1 Level";
			case 4: return "Rudolph Outfit";
			case 5: return "200 Tokens";
			case 6: return "Merry Title";
			case 7: return "3 Levels";
			case 8: return "SnowGolem Class";
			case 9: return "500 Tokens";
			case 10: return "HO HO HO Title (Christmas Day)";
		}
		return "";
	}

	private void grantReward(int day, Player player, PlayerData data) {

		switch (day) {
			case 1:
				data.tokens += 100;
				player.sendMessage(main.color("&aYou received &e100 Tokens!"));
				break;

			case 2:
				if (data.snowParticles != 1) {
					data.snowParticles = 1;
					player.sendMessage(main.color("&aYou unlocked &eSnow Particles&r!"));
				} else {
					data.tokens += 150;
					player.sendMessage(main.color("&aYou have already unlocked &eSnow Particles&r so you received &e150 Tokens instead!"));
				}
				break;

			case 3:
				data.level += 1;
				player.sendMessage(main.color("&aYou gained &e1 Level!"));
				break;

			case 4:
				data.rudolphOutfit = 1;
				player.sendMessage(main.color("&aYou unlocked the &eRudolph Outfit&r!"));
				break;

			case 5:
				data.tokens += 200;
				player.sendMessage(main.color("&aYou received &e200 Tokens!"));
				break;

			case 6:
				data.merryTitle = 1;
				player.sendMessage(main.color("&aYou unlocked the &eMerry Title&r!"));
				break;

			case 7:
				data.level += 3;
				player.sendMessage(main.color("&aYou gained &e3 Levels!"));
				break;

			case 8:
				int classID = ClassType.SnowGolem.getID();
				ClassDetails details = data.playerClasses.get(classID);
				if (details == null) {
					details = new ClassDetails();
					data.playerClasses.put(classID, details);
				}
				details.setPurchased();
				player.sendMessage(main.color("&aYou unlocked the &eSnowGolem Class!"));
				break;

			case 9:
				data.tokens += 500;
				player.sendMessage(main.color("&aYou received &e500 Tokens!"));
				break;

			case 10:
				data.hohohoTitle = 1;
				player.sendMessage(main.color("&aYou unlocked the &eHO HO HO Title&r! &6Merry Christmas!"));
				break;
		}

		main.getScoreboardManager().lobbyBoard(player);
	}
}
