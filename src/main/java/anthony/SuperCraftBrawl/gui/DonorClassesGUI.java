package anthony.SuperCraftBrawl.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class DonorClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public DonorClassesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Donor Classes").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int a = 0;
		int b = 0;

		for (ClassType type : ClassType.values()) {
			if (type.getMinRank() == Rank.VIP) {
				contents.set(a, b,
						ClickableItem.of(ItemHelper.setDetails(ItemHelper.setHideFlags(type.getItem(), true),
								type.getTag(), type.buildDescription(), "",
								"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Left Click" + ChatColor.RESET
										+ ChatColor.YELLOW + " to choose a class",
								"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Right Click" + ChatColor.RESET
										+ ChatColor.YELLOW + " to add a favorite class"),
								e -> {
									Rank donor = type.getMinRank();

									if (donor == null
											|| player.hasPermission("scb." + donor.toString().toLowerCase())) {
										if (e.isLeftClick()) {
											main.getGameManager().playerSelectClass(player, type);
											player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
													+ "==============================================");
											player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
											player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
											player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
													+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
													+ "Selected Class: " + type.getTag());
											player.sendMessage(
													"" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
															+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: "
															+ ChatColor.RESET + ChatColor.YELLOW + type.getClassDesc());
											player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
											player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
											player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
													+ "==============================================");
										} else if (e.isRightClick()) {
											PlayerData data = main.getDataManager().getPlayerData(player);

											if (data != null) {
												data.customIntegers.add(type.getID());
												player.sendMessage(main
														.color("&2&l(!) &rAdded new favorite class: " + type.getTag()));
												main.getDataManager().saveData(data);
											}
										}
										inv.close(player);
									} else {
										player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD
												+ "(!) " + ChatColor.RESET
												+ "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
										inv.close(player);
									}
								}));
				b++;

				if (b > 8) {
					if (a == 0) {
						a = 1;
						b = 0;
					} else if (a == 1) {
						a = 2;
						b = 0;
					}
				}
			}
		}
		/*
		 * contents.set(0, 0, ClickableItem.of( ItemHelper.setDetails(new
		 * ItemStack(Material.IRON_AXE), "" + ChatColor.ITALIC + "Irongolem", "" +
		 * ChatColor.GRAY +
		 * "Smack your enemies into the air while defending your village!", "" +
		 * ChatColor.YELLOW + ChatColor.BOLD + "VIP" + ChatColor.WHITE +
		 * "+ exclusive!"), e -> { Rank donor = ClassType.IronGolem.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.IronGolem); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.IronGolem.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
		 * + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.IronGolem.getClassDesc()); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); inv.close(player); } else
		 * { player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(0, 1, ClickableItem.of(
		 * ItemHelper.setDetails(new ItemStack(Material.GHAST_TEAR), "Ghast", "" +
		 * ChatColor.GRAY + "Burn down your enemies with your sorrows", "" +
		 * ChatColor.YELLOW + ChatColor.BOLD + "VIP" + ChatColor.WHITE +
		 * "+ exclusive!"), e -> { Rank donor = ClassType.Ghast.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.Ghast); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.Ghast.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
		 * + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.Ghast.getClassDesc()); player.sendMessage("" + ChatColor.DARK_GREEN
		 * + ChatColor.BOLD + "|| "); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| "); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "==============================================");
		 * inv.close(player); } else { player.sendMessage("" + ChatColor.RESET +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(0, 2, ClickableItem.of(
		 * ItemHelper.setDetails(new ItemStack(Material.SLIME_BALL), "Slime", "" +
		 * ChatColor.GRAY + "Throw sticky grenades and attack enemies!", "" +
		 * ChatColor.YELLOW + ChatColor.BOLD + "VIP" + ChatColor.WHITE +
		 * "+ exclusive!"), e -> { Rank donor = ClassType.Slime.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.Slime); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.Slime.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
		 * + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.Slime.getClassDesc()); player.sendMessage("" + ChatColor.DARK_GREEN
		 * + ChatColor.BOLD + "|| "); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| "); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "==============================================");
		 * inv.close(player); } else { player.sendMessage("" + ChatColor.RESET +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(0, 3,
		 * ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.GOLD_AXE),
		 * "ButterGolem", "" + ChatColor.GRAY +
		 * "Once a proud member of the Sky Army, the ButterGolem now stands as a relic of a bygone era.."
		 * , "" + ChatColor.YELLOW + ChatColor.BOLD + "VIP" + ChatColor.RESET +
		 * "+ exclusive!"), e -> { Rank donor = ClassType.ButterGolem.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.ButterGolem); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.ButterGolem.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN
		 * + ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW +
		 * ChatColor.BOLD + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.ButterGolem.getClassDesc()); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); inv.close(player); } else
		 * { player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(0, 4, ClickableItem.of(
		 * ItemHelper.setDetails(new ItemStack(Material.DRAGON_EGG), "EnderDragon", "" +
		 * ChatColor.GRAY + "Jump higher than your opponents and teleport around!", "" +
		 * ChatColor.YELLOW + ChatColor.BOLD + "VIP" + ChatColor.RESET +
		 * "+ exclusive!"), e -> { Rank donor = ClassType.Enderdragon.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.Enderdragon); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.Enderdragon.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN
		 * + ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW +
		 * ChatColor.BOLD + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.Enderdragon.getClassDesc()); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); inv.close(player); } else
		 * { player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(0, 5, ClickableItem.of(
		 * ItemHelper.setDetails(new ItemStack(Material.SHEARS), "Bat", "" +
		 * ChatColor.GRAY + "Dance around your opponents with SUPER high jumps!", "" +
		 * ChatColor.YELLOW + ChatColor.BOLD + "VIP" + ChatColor.RESET +
		 * "+ exclusive!"), e -> { Rank donor = ClassType.Bat.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.Bat); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.Bat.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
		 * + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.Bat.getClassDesc()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| "); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| "); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "==============================================");
		 * inv.close(player); } else { player.sendMessage("" + ChatColor.RESET +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(0, 6, ClickableItem.of(
		 * ItemHelper.setDetails(new ItemStack(Material.REDSTONE_BLOCK), "SethBling", ""
		 * + ChatColor.GRAY + "The creator of SCB, wanna fight?!?!", "" +
		 * ChatColor.YELLOW + ChatColor.BOLD + "VIP" + ChatColor.RESET +
		 * "+ exclusive!"), e -> { Rank donor = ClassType.SethBling.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.SethBling); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.SethBling.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
		 * + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.SethBling.getClassDesc()); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); inv.close(player); } else
		 * { player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(0, 7, ClickableItem.of(
		 * ItemHelper.setDetails(new ItemStack(Material.MELON), "Melon", "" +
		 * ChatColor.GRAY + "The Owner of the server in the game?!", "" +
		 * ChatColor.YELLOW + ChatColor.BOLD + "VIP" + ChatColor.RESET +
		 * "+ exclusive!"), e -> { Rank donor = ClassType.Melon.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.Melon); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.Melon.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
		 * + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.Melon.getClassDesc()); player.sendMessage("" + ChatColor.DARK_GREEN
		 * + ChatColor.BOLD + "|| "); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| "); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "==============================================");
		 * inv.close(player); } else { player.sendMessage("" + ChatColor.RESET +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(0, 8, ClickableItem.of(
		 * ItemHelper.setDetails(new ItemStack(Material.RED_MUSHROOM), "BabyCow", "" +
		 * ChatColor.GRAY + "moo.. MOO!!", "" + ChatColor.YELLOW + ChatColor.BOLD +
		 * "VIP" + ChatColor.RESET + "+ exclusive!"), e -> { Rank donor =
		 * ClassType.BabyCow.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.BabyCow); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.BabyCow.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
		 * + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.BabyCow.getClassDesc()); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); inv.close(player); } else
		 * { player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } })); contents.set(1, 0, ClickableItem.of(
		 * ItemHelper.setDetails(new ItemStack(Material.DIAMOND), "Herobrine", "" +
		 * ChatColor.GRAY + "Use your Diamond of Despair to", "" + ChatColor.GRAY +
		 * "play tricks on your opponents!", "" + ChatColor.YELLOW + ChatColor.BOLD +
		 * "VIP" + ChatColor.RESET + "+ exclusive!"), e -> { Rank donor =
		 * ClassType.Herobrine.getMinRank();
		 * 
		 * if (donor == null || player.hasPermission("scb." +
		 * donor.toString().toLowerCase())) {
		 * main.getGameManager().playerSelectClass(player, ClassType.Herobrine); ;
		 * player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET +
		 * ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " +
		 * ClassType.Herobrine.getTag()); player.sendMessage("" + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "|| " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
		 * + "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW +
		 * ClassType.Herobrine.getClassDesc()); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "); player.sendMessage("" +
		 * ChatColor.DARK_GREEN + ChatColor.BOLD +
		 * "=============================================="); inv.close(player); } else
		 * { player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN +
		 * ChatColor.BOLD + "(!) " + ChatColor.RESET +
		 * "Stop tryna cheat the systemmmmm!! You need a rank to use this class");
		 * inv.close(player); } }));
		 */
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
