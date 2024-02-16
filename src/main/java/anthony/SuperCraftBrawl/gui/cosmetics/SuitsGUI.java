package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SuitsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public SuitsGUI(Core main) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(6, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Suits")
                .build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        // Icon Items
        ItemStack lockedCosmetic = ItemHelper.createDye(DyeColor.GRAY, 1, ChatColor.GRAY + "&&&&&&&");

            // Santa Outfit
        String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";
        ItemStack santaHead = ItemHelper.setDetails(ItemHelper.createSkullTexture(texture), ChatColor.RED.toString() + ChatColor.BOLD + "Santa Outfit");

//        ItemStack santa = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

            // Astronaut Outfit
        ItemStack astronautHead = ItemHelper.create(Material.GLASS, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Astronaut Outfit");


        // Setting Items
        contents.fillRect(1,1, 7,7, ClickableItem.of(
                lockedCosmetic,
                e -> {

                }));





            // Astronaut Outfit
        contents.set(1,1,ClickableItem.of(
                astronautHead,
                e -> {
                    if (data.astronaut == 1) {
                        if (!(main.ao.containsKey(player))) {
                            main.ao.put(player, true);
                            player.getInventory().setHelmet(astronautHead);
                            player.getInventory().setChestplate(
                                    ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.WHITE, ChatColor.WHITE + "Astronaut Outfit"));
                            player.getInventory().setLeggings(
                                    ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.WHITE, ChatColor.WHITE + "Astronaut Outfit"));
                            player.getInventory().setBoots(
                                    ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.WHITE, ChatColor.WHITE + "Astronaut Outfit"));
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have equipped " + ChatColor.YELLOW + "Astronaut Outfit");
                        } else {
                            main.ao.remove(player);
                            player.getInventory().setHelmet(new ItemStack(Material.AIR));
                            player.getInventory().setChestplate(new ItemStack(Material.AIR));
                            player.getInventory().setLeggings(new ItemStack(Material.AIR));
                            player.getInventory().setBoots(new ItemStack(Material.AIR));
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have unequipped " + ChatColor.YELLOW + "Astronaut Outfit");
                        }
                    } else {
                        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "You have not unlocked this cosmetic yet!");
                    }
                    inv.close(player);
                }));


            // Santa Outfit
        contents.set(1,2, ClickableItem.of(
                santaHead,
                e -> {
                    if (data != null) {
                        if (data.santaoutfit == 1) {
                            if (!(main.so.containsKey(player))) {
                                if (main.ao.containsKey(player)) {
                                    main.ao.remove(player);
                                    player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                            + "You have unequipped " + ChatColor.YELLOW + "Astronaut Outfit");
                                }
                                main.so.put(player, true);
                                player.getInventory().setHelmet(
                                        santaHead);
                                player.getInventory().setChestplate(
                                        ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.RED, ChatColor.RED + "Santa Outfit"));
                                player.getInventory().setLeggings(
                                        ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.RED, ChatColor.RED + "Santa Outfit"));
                                player.getInventory().setBoots(
                                        ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.RED, ChatColor.RED + "Santa Outfit"));
                                player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "You have equipped " + ChatColor.RED + ChatColor.BOLD + "Santa Outfit");
                            } else {
                                main.so.remove(player);
                                player.getInventory().setHelmet(new ItemStack(Material.AIR));
                                player.getInventory().setChestplate(new ItemStack(Material.AIR));
                                player.getInventory().setLeggings(new ItemStack(Material.AIR));
                                player.getInventory().setBoots(new ItemStack(Material.AIR));
                                player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "You have unequipped " + ChatColor.RED + ChatColor.BOLD + "Santa Outfit");
                            }
                        } else {
                            player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have not unlocked this cosmetic yet!");
                        }
                    }

                    inv.close(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
