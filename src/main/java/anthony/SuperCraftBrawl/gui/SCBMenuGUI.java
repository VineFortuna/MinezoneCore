package anthony.SuperCraftBrawl.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class SCBMenuGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public SCBMenuGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "SCB Games").build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(1, 3,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD),
								"" + ChatColor.RESET + ChatColor.GREEN + "SCB Solos " + ChatColor.DARK_GREEN
										+ ChatColor.BOLD + "NEW UPDATE!",
								"" + ChatColor.GRAY + "Choose a class, fight, & ",
								ChatColor.GRAY + "claim the #1 spot!", "",
								main.color("&e&lPlayers: &e" + GameManager.playercount.getOrDefault("scb-1", 0))),
						e -> {
							inv.close(player);
							ByteArrayOutputStream b = new ByteArrayOutputStream();
							DataOutputStream out = new DataOutputStream(b);

							try {
								out.writeUTF("Connect");
								out.writeUTF("scb-1");
								player.sendMessage(main.color("&e&l(!) &rConnecting to &escb-1"));
							} catch (Exception ex) {
								player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &escb-1"));
							}
							player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
						}));

		ItemStack stats = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		SkullMeta statsMeta = (SkullMeta) stats.getItemMeta();
		statsMeta.setOwner(player.getName());
		stats.setItemMeta(statsMeta);

		contents.set(1, 5,
				ClickableItem.of(
						ItemHelper.setDetails(stats,
								"" + ChatColor.RESET + ChatColor.GREEN + "SCB Duos " + ChatColor.DARK_GREEN
										+ ChatColor.BOLD + "NEW UPDATE!",
								"" + ChatColor.GRAY + "Regular SCB, but", "" + ChatColor.GRAY + "with teammates", "",
								main.color("&e&lPlayers: &e" + GameManager.playercount.getOrDefault("scb-2", 0))),
						e -> {
							inv.close(player);
							Bukkit.getMessenger().registerOutgoingPluginChannel(main, "BungeeCord");

							ByteArrayOutputStream b = new ByteArrayOutputStream();
							DataOutputStream out = new DataOutputStream(b);

							try {
								out.writeUTF("Connect");
								out.writeUTF("scb-2");
								player.sendMessage(main.color("&e&l(!) &rConnecting to &escb-2"));
							} catch (Exception ex) {
								player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &escb-2"));
							}
							player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
						}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
