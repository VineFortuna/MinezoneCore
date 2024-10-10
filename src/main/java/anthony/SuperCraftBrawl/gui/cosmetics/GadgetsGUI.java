package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GadgetsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public GadgetsGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(3, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Gadgets")
                .parent(parent)
                .build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        // Icon Items
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

            // Fishing
        ItemStack fishingRod = main.getFishingRod(player);

        // Setting Items
        contents.fillBorders(ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));

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
            // Fishing Rod
        contents.set(1, 4, ClickableItem.of(
                fishingRod,
                e -> {
                    if (!(player.getInventory().contains(fishingRod))) {
                        player.getInventory().setItem(5, fishingRod);
                        player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "You have equipped " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Fishing Rod");
                        inv.close(player);
                    } else if (player.getInventory().contains(fishingRod)) {
                        player.getInventory().remove(fishingRod);
                        player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "You have unequipped " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Fishing Rod");
                        inv.close(player);
                    }
                }));

        contents.set(2, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }
        ));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
