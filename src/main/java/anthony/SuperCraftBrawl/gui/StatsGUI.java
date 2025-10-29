package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.fishing.FishRarity;
import anthony.SuperCraftBrawl.fishing.FishType;
import anthony.SuperCraftBrawl.gui.fishing.FishingGUI;
import anthony.SuperCraftBrawl.playerdata.FishingDetails;
import anthony.SuperCraftBrawl.playerdata.ParkourDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.parkour.Arenas;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		
		contents.fillBorders(ClickableItem.of(ItemHelper.setDetails(
				new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
		
		contents.set(4, 0, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.REDSTONE_COMPARATOR),
				"" + ChatColor.RESET + ChatColor.YELLOW + "Preferences"), e -> {
			new PrefsGUI(main).inv.open(player);
		}));
		contents.set(4, 4, ClickableItem.of(ItemHelper.setGlowing(ItemHelper.setDetails(new ItemStack(Material.BOOK),
				"" + ChatColor.RESET + ChatColor.YELLOW + "My Stats"),
				data.playerName.equals(player.getName())), e -> {
			if (this.target != null && this.target != player)
				new StatsGUI(main).inv.open(player);
		}));

		String rankName = "";
		Rank rank = data.getRank();

		if (rank != null && rank.getTag() != null)
			rankName = rank == Rank.DEFAULT ? main.color("&7Default") : rank.getTag();
		
		if (data != null) {
			contents.set(0, 4,
					ClickableItem.of(ItemHelper.createSkullHeadPlayer(1, data.playerName, main.color("&e" + data.playerName),
							Arrays.asList(main.color("&fRank: &a" + rankName),
									main.color("&fLevel: &a" + data.level),
									main.color("&fEXP: &a" + data.exp + "/2500"),
									main.color("&fMatches Played: &a" + (data.wins + data.losses)))), e-> {}));
			contents.set(2, 4,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.DIAMOND_SWORD),
							main.color("&e&lSCB Stats"),
                            main.color("&fWins: &a" + data.wins),
                            main.color("&fFlawless Wins: &a" + data.flawlessWins),
                            main.color("&fMatch MVPs: &a" + data.matchMvps),
                            main.color("&fLosses: &a" +  data.losses),
                            "",
                                    main.color("&fCurrent Winstreak: &a" + data.winstreak),
                                    main.color("&fBest Winstreak: &a" + data.bestWinstreak),
                            "",
                            main.color("&fKills: &a" + data.kills),
                            main.color("&fDeaths: &a" + data.deaths)),
                            e -> {
                                //Do nothing when clicked
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
			int uniqueCaught = main.fishing.getTotalFish(player);
			if (target != null)
				uniqueCaught = main.fishing.getTotalFish(target);
			
			contents.set(2, 6,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD),
							main.color("&e&lFishing Stats"),
							main.color("&fCaught: &a" + data.totalcaught),
							main.color("&fUnique Caught: &a" + uniqueCaught + "/" + FishType.values().length),
							main.color("&fTreasure Caught: &a" + treasure)), e -> {
					}));


			List<String> parkourLore = new ArrayList<>();
			for (Arenas arenas : Arenas.values()) {
				ParkourDetails details = data.playerParkour.get(arenas.getId());
				if (details != null && details.totalTime > 0) {
					parkourLore.add(main.color("&f" + arenas.getName() + ": &a" + main.getParkour().formatTime(details.totalTime)));
				} else {
					parkourLore.add(main.color("&f" + arenas.getName() + ": &aN/A"));
				}
			}

			contents.set(2, 2,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.FEATHER),
							main.color("&e&lParkour Stats"), parkourLore), e -> {
					}));

			String fishingTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTk2YTQ4ZGNkYWY0MThmMjJjZDE4NjdjMWViMGFlMjgyYzI4NGI2Nzk5MDZiNzk3ODFkOGQyYjJlZWJhMjEwMiJ9fX0=";
			contents.set(4, 8,
					ClickableItem.of(ItemHelper.setDetails(ItemHelper.createSkullTexture(fishingTexture),
							main.color("&eFishingpedia")), e-> {
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
