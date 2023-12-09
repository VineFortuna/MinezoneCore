package anthony.skywars;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Core;

public class AbilityManager implements Listener {

	private Core main;

	public AbilityManager(Core main) {
		this.main = main;
		this.main.getServer().getPluginManager().registerEvents(this, main);
	}

	@EventHandler
	public void playerInteractInGame(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();

		if (item != null && item.getType() == Material.PACKED_ICE && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			if (meta != null && meta.getDisplayName() != null && meta.getDisplayName().contains("Bridge")) {
				// Start creating the ice platforms in the direction the player is facing
				
			}
		}
	}
}
