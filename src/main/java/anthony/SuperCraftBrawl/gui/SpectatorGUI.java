package anthony.SuperCraftBrawl.gui;

import java.text.DecimalFormat;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class SpectatorGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private int count;

	public SpectatorGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spectate").build();
		this.main = main;
		this.count = 0;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if (i != null)
			this.spectate(i, player, contents);
		else {
			i = main.getGameManager().GetInstanceOfSpectator(player);

			if (i != null)
				this.spectate(i, player, contents);
		}
		this.count = 0;
	}

	private void spectate(GameInstance i, Player player, InventoryContents contents) {
		for (Player gamePlayer : i.players) {
			ItemStack p = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
			SkullMeta pMeta = (SkullMeta) p.getItemMeta();
			pMeta.setOwner(gamePlayer.getName());
			p.setItemMeta(pMeta);
			i = main.getGameManager().GetInstanceOfPlayer(gamePlayer);

			if (i != null) {
				if (i.classes.get(gamePlayer).getLives() > 0) {
					double health = gamePlayer.getHealth();
					DecimalFormat decimalFormat = new DecimalFormat("#.#");
					String formatHealth = decimalFormat.format(health);
					contents.set(0, count,
							ClickableItem.of(ItemHelper.setDetails(p, main.color("&fPlayer: &a" + gamePlayer.getName()),
									main.color("&fClass: &a" + i.classes.get(gamePlayer).getType()),
									main.color("&fLives: &a" + i.classes.get(gamePlayer).getLives()),
									main.color("&fHealth: &a" + formatHealth + "/20")), e -> {
										player.teleport(gamePlayer);
										inv.close(player);
									}));
					this.count++;
				}
			}
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
