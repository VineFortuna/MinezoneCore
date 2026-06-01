package anthony.SuperCraftBrawl.mysterychest;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.MysteryChestsGUI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MysteryChestManager implements Listener {

    private Core main;
    public boolean chestCanOpen = false;

    public MysteryChestManager(Core main) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void mysteryChest(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        List<Material> list = new ArrayList<>(Arrays.asList(Material.CHEST));

        if (player.getWorld() == main.getLobbyWorld()) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && list.contains(e.getClickedBlock().getType())) {
                if (chestCanOpen == false) {
                    e.setCancelled(true);
                    new MysteryChestsGUI(main, e.getClickedBlock().getLocation()).inv.open(player);
                } else {
                    e.setCancelled(true);
                    player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + "This MysteryChest is already in use!");
                }
            }
        } else {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && list.contains(e.getClickedBlock().getType())) {
                // e.setCancelled(true);
                // REMOVE LATER
            }
        }
    }

}
