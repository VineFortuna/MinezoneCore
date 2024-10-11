package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.fishing.FishRarity;
import anthony.SuperCraftBrawl.fishing.FishType;
import anthony.SuperCraftBrawl.playerdata.FishingDetails;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

public class StatsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private Player target;

	public StatsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Your Statistics").build();
		this.main = main;
	}

	public StatsGUI(Core main, Player target) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + target.getName() + "'s Statistics").build();
		this.main = main;
		this.target = target;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (this.target != null)
			data = main.getDataManager().getPlayerData(target);
		
		contents.fillRow(0, ClickableItem.of(ItemHelper.setDetails(
				new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
		contents.fillRow(4, ClickableItem.of(ItemHelper.setDetails(
				new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
		
		contents.set(4, 3, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.REDSTONE_COMPARATOR),
				"" + ChatColor.RESET + ChatColor.YELLOW + "Preferences"), e -> {
			new PrefsGUI(main).inv.open(player);
		}));
		contents.set(4, 5, ClickableItem.of(ItemHelper.setGlowing(ItemHelper.setDetails(new ItemStack(Material.BOOK),
				"" + ChatColor.RESET + ChatColor.YELLOW + "My Stats"),
				data.playerName.equals(player.getName())), e -> {
			if (this.target != null && this.target != player)
				new StatsGUI(main).inv.open(player);
		}));
		
		String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19";
		contents.set(4, 8, ClickableItem.of(ItemHelper.setDetails(ItemHelper.createSkullTexture(texture),
				main.color("&2&l(!)"), main.color("&7Use &a/stats <player>"),
				main.color("&7to view another player's stats!")), e -> {}));
		
		if (data != null) {
			contents.set(0, 4,
					ClickableItem.of(ItemHelper.createSkullHeadPlayer(1, data.playerName, main.color("&e" + data.playerName),
							Arrays.asList(main.color("&aRank: &r" + data.getRank().getTag()),
									main.color("&aLevel: &r" + data.level),
									main.color("&aMatches Played: &r" + (data.wins + data.losses)))), e-> {}));
			/*contents.set(2, 2,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.FEATHER), "&cComing soon..."), e-> {}));*/
			contents.set(2, 4,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.DIAMOND_SWORD),
							main.color("&e&lSCB Stats"),
							"" + ChatColor.RESET + ChatColor.GREEN + "Wins: " + ChatColor.RESET + data.wins,
							"" + ChatColor.RESET + ChatColor.GREEN + "Winstreak: " + ChatColor.RESET + data.winstreak,
							"" + ChatColor.RESET + ChatColor.GREEN + "Flawless Wins: " + ChatColor.RESET
									+ data.flawlessWins,
							"" + ChatColor.RESET + ChatColor.GREEN + "Losses: " + ChatColor.RESET + data.losses,
							"" + ChatColor.RESET + ChatColor.GREEN + "Match MVPs: " + ChatColor.RESET + data.matchMvps,
							"", "" + ChatColor.RESET + ChatColor.GREEN + "Kills: " + ChatColor.RESET + data.kills,
							"" + ChatColor.RESET + ChatColor.GREEN + "Deaths: " + ChatColor.RESET + data.deaths), e -> {
							}));
			
			int treasure = 0;
			for (FishType type : FishType.values()) {
				if (type.getRarity() == FishRarity.TREASURE) {
					FishingDetails details = data.playerFishing.get(type.getId());
					if (details != null && details.timesCaught > 0) {
						treasure += details.timesCaught;
					}
				}
			}
			int uniqueCaught = main.getTotalFish(player);
			if (target != null)
				uniqueCaught = main.getTotalFish(target);
			
			contents.set(2, 6,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD),
							main.color("&e&lFishing Stats"),
							main.color("&aCaught: &r" + data.totalcaught),
							main.color("&aUnique Caught: &r" + uniqueCaught),
							main.color("&aTreasure Caught: &r" + treasure)), e -> {
					}));
			String fishingTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTk2YTQ4ZGNkYWY0MThmMjJjZDE4NjdjMWViMGFlMjgyYzI4NGI2Nzk5MDZiNzk3ODFkOGQyYjJlZWJhMjEwMiJ9fX0=";
			contents.set(4, 0,
					ClickableItem.of(ItemHelper.setDetails(ItemHelper.createSkullTexture(fishingTexture),
							main.color("&eFishing")), e-> {
						if (target != null)
							new FishingGUI(main, target, inv).inv.open(player);
						else
							new FishingGUI(main, inv).inv.open(player);
					}));
			
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		
	}
}
