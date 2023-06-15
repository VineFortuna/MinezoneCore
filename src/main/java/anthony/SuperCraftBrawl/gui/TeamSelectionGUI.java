package anthony.SuperCraftBrawl.gui;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class TeamSelectionGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public TeamSelectionGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Select your Team").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if (i != null) {
			contents.set(1, 2,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()),
							"" + ChatColor.RED + ChatColor.BOLD + "Red Team", "", "" + i.redTeam.size() + "/2"), e -> {
								if (i.redTeam.size() == 1 || i.redTeam.isEmpty()) {
									doStuff(i, player);
									i.redTeam.add(player);
									i.team.put(player, "Red");
									player.sendMessage(main.color("&2&l(!) &rYou have selected &c&lRed Team"));
								} else {
									player.sendMessage(main.color("&c&l(!) &rThis team is full!"));
								}
								inv.close(player);
							}));
			contents.set(1, 4,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()),
							"" + ChatColor.BLUE + ChatColor.BOLD + "Blue Team", "", "" + i.blueTeam.size() + "/2"), e -> {
								if (i.blueTeam.size() == 1 || i.blueTeam.isEmpty()) {
									doStuff(i, player);
									i.blueTeam.add(player);
									i.team.put(player, "Blue");
									player.sendMessage(main.color("&2&l(!) &rYou have selected &b&lBlue Team"));
								} else {
									player.sendMessage(main.color("&c&l(!) &rThis team is full!"));
								}
								inv.close(player);
							}));
			contents.set(1, 6,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()),
							"" + ChatColor.BLACK + ChatColor.BOLD + "Black Team", "", "" + i.blackTeam.size() + "/2"), e -> {
								if (i.blackTeam.size() == 1 || i.blackTeam.isEmpty()) {
									doStuff(i, player);
									i.blackTeam.add(player);
									i.team.put(player, "Black");
									player.sendMessage(main.color("&2&l(!) &rYou have selected &0&lBlack Team"));
								} else {
									player.sendMessage(main.color("&c&l(!) &rThis team is full!"));
								}
								inv.close(player);
							}));
		}
	}
	
	private void doStuff(GameInstance i, Player player) {
		if (i.team.containsKey(player))
			i.team.remove(player);
		
		if (i.redTeam.contains(player))
			i.redTeam.remove(player);
		else if (i.blueTeam.contains(player))
			i.blueTeam.remove(player);
		else if (i.blackTeam.contains(player))
			i.blackTeam.remove(player);
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
