package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core; 
import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SuitsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public SuitsGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(3, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Suits")
                .parent(parent)
                .build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        // Icon Items
            // Santa Outfit
        String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";
        ItemStack santaHead = ItemHelper.createSkullTexture(texture, ChatColor.RED.toString() + ChatColor.BOLD + "Santa Outfit");

            // Astronaut Outfit
        ItemStack astronautHead = ItemHelper.create(Material.GLASS, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Astronaut Outfit");

        texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjlhYzgwNGEyYzVhOGVhNTdlZjY5NjU3YWI2NDM0N2QxZWQzNmIzNGNhNzBhMjE4ZjZhNjNkNWI2YWEyZmU5ZiJ9fX0=";
        ItemStack pirateHead = ItemHelper.createSkullTexture(texture, "&3&lPirate Outfit", "", "&aFishing reward!");

        // Setting Items
        contents.fillBorders(ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));

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
        contents.set(1,3,ClickableItem.of(
                pirateHead,
                e -> {
                    if (data.rewardLevel >= 7) {
                        if (!(main.po.containsKey(player))) {
                            main.po.put(player, true);
                            player.getInventory().setHelmet(pirateHead);
                            player.getInventory().setChestplate(
                                    ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.GREEN, ChatColor.DARK_AQUA + "Pirate Outfit"));
                            player.getInventory().setLeggings(
                                    ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.MAROON, ChatColor.DARK_AQUA + "Pirate Outfit"));
                            player.getInventory().setBoots(
                                    ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.BLACK, ChatColor.DARK_AQUA + "Pirate Outfit"));
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have equipped " + ChatColor.DARK_AQUA + "Pirate Outfit");
                        } else {
                            main.po.remove(player);
                            player.getInventory().setHelmet(new ItemStack(Material.AIR));
                            player.getInventory().setChestplate(new ItemStack(Material.AIR));
                            player.getInventory().setLeggings(new ItemStack(Material.AIR));
                            player.getInventory().setBoots(new ItemStack(Material.AIR));
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have unequipped " + ChatColor.DARK_AQUA + "Pirate Outfit");
                        }
                    } else {
                        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "You have not unlocked this cosmetic yet!");
                    }
                    inv.close(player);
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
