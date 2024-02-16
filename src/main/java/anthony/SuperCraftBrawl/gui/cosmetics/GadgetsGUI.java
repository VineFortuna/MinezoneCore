package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GadgetsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public GadgetsGUI(Core main) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(6, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Gadgets")
                .build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        // Icon Items
        ItemStack lockedCosmetic = ItemHelper.createDye(DyeColor.GRAY, 1, ChatColor.GRAY + "&&&&&&&");

            // Broom
        List<String> broomList = new ArrayList<>();
        broomList.add(ChatColor.DARK_GRAY + "Fly around like a Witch!");
        ItemStack broom = ItemHelper.create(Material.WHEAT, ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Magic Broom", broomList);

            // Melon
        List<String> melonList = new ArrayList<>();
        melonList.add(ChatColor.DARK_GRAY + "A delicious melon that gives you...");
        melonList.add(ChatColor.DARK_GRAY + "                  Superpowers!");
        melonList.add("");
        melonList.add(ChatColor.RESET + "You have " + ChatColor.YELLOW + data.melon + ChatColor.RESET + " Melons");
        ItemStack melon = ItemHelper.create(Material.MELON, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Melons", melonList);

            // Paintball
        List<String> paintballList = new ArrayList<>();
        paintballList.add(ChatColor.DARK_GRAY + "Shoot paintballs as you want");
        paintballList.add("");
        paintballList.add(ChatColor.RESET + "You have " + ChatColor.YELLOW + data.paintball + ChatColor.RESET + " Paintballs");
        ItemStack paintball = ItemHelper.create(Material.GOLD_BARDING, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Paintball Gun", paintballList);

        // Setting Items
        contents.fillRect(1,1, 7,7, ClickableItem.of(
                lockedCosmetic,
                e -> {

                }));

            // Broom Gadget
        contents.set(1, 1, ClickableItem.of(
                broom,
                e -> {
                    if (player.hasPermission("scb.wheat")) {
                        if (!(player.getInventory().contains(broom))) {
                            player.getInventory().setItem(5, broom);
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have equipped " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
                            inv.close(player);
                        } else if (player.getInventory().contains(broom)) {
                            player.getInventory().remove(broom);
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have unequipped " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
                            inv.close(player);
                        }
                    } else {
                        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "You need the rank " + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN " + ChatColor.RESET
                                + "to use this item!");
                    }
                }));

            // Paintball Gadget
        contents.set(1, 2, ClickableItem.of(
                paintball,
                e -> {
                    if (data.paintball > 0) {
                        if (!(player.getInventory().contains(Material.GOLD_BARDING))) {
                            ItemStack p = ItemHelper.setDetails(new ItemStack(Material.GOLD_BARDING, data.paintball),
                                    "" + ChatColor.RESET + ChatColor.GREEN + "Paintball Gun", "",
                                    "" + ChatColor.RESET + ChatColor.GRAY + "Right click to shoot a paintball!");
                            player.getInventory().setItem(5, p);
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have equipped " + ChatColor.GREEN + "Paintball Gun");
                        } else {
                            player.getInventory().remove(Material.GOLD_BARDING);
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have unequipped " + ChatColor.GREEN + "Paintball Gun");
                        }
                    } else {
                        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "You do not have enough paintballs!");
                    }
                    inv.close(player);
                }));

            // Melon Gadget
        contents.set(1, 3, ClickableItem.of(
                melon,
                e -> {
                    if (data.melon > 0) {
                        if (!(player.getInventory().contains(melon))) {
                            player.getInventory().setItem(5, melon);
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have equipped " + ChatColor.YELLOW + "Melons");
                            inv.close(player);
                        } else if (player.getInventory().contains(melon)) {
                            player.getInventory().remove(melon);
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have unequipped " + ChatColor.YELLOW + "Melons");
                        }
                    } else {
                        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "You do not have enough melons!");
                    }
                    inv.close(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
