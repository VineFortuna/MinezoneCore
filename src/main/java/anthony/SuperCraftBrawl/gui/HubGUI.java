package anthony.SuperCraftBrawl.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Main;
import anthony.SuperCraftBrawl.Game.GameManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class HubGUI implements InventoryProvider {

	public Main main;
	public SmartInventory inv;

	public HubGUI(Main main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Game Selector").build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int skywarsCount = GameManager.playercount.getOrDefault("sw-1", 0);

		/*contents.set(1, 5,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BOW),
						"" + ChatColor.RESET + ChatColor.GREEN + "SkyWars " + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "NEW GAME!",
						"" + ChatColor.GRAY + "Loot up, build to center & ", ChatColor.GRAY + "claim the #1 spot!", "",
						main.color("&e&lPlayers: &e" + skywarsCount)), e -> {
							inv.close(player);
							ByteArrayOutputStream b = new ByteArrayOutputStream();
							DataOutputStream out = new DataOutputStream(b);

							try {
								out.writeUTF("Connect");
								out.writeUTF("sw-1");
								player.sendMessage(main.color("&e&l(!) &rConnecting to &esw-1"));
							} catch (Exception ex) {
								player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &esw-1"));
							}
							player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
						}));*/

		int scbCount = GameManager.playercount.getOrDefault("scb-1", 0)
				+ GameManager.playercount.getOrDefault("scb-2", 0);
		contents.set(1, 3,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD),
								"" + ChatColor.RESET + ChatColor.GREEN + "SuperCraftBlocks " + ChatColor.DARK_GREEN
										+ ChatColor.BOLD + "NEW UPDATE!",
								"" + ChatColor.GRAY + "Choose a class, fight, & ",
								ChatColor.GRAY + "claim the #1 spot!", "", main.color("&e&lPlayers: &e" + scbCount)),
						e -> {
							new SCBMenuGUI(main).inv.open(player);
						}));

		ItemStack stats = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		SkullMeta statsMeta = (SkullMeta) stats.getItemMeta();
		statsMeta.setOwner(player.getName());
		stats.setItemMeta(statsMeta);
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
