package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.Classes.ClassesDonorGUI;
import anthony.SuperCraftBrawl.gui.Classes.ClassesFreeGUI;
import anthony.SuperCraftBrawl.gui.Classes.ClassesLevelGUI;
import anthony.SuperCraftBrawl.gui.Classes.ClassesTokenGUI;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClassesGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public ClassesGUI(Core main) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
                .title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Class Selector").build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(1, 2, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.DIAMOND),
                        main.color("&eDonor Classes"), "",
                        main.color("" + Rank.VIP.getTag() + " &cexclusive!")
                ),e -> {
                    SoundManager.playClickSound(player);
                    new ClassesDonorGUI(main).getInventory().open(player);
                }));

        contents.set(1, 4, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK),
                        main.color("&eFree Classes"),
                        ChatColor.GRAY + "All the free classes!"), e -> {
                    SoundManager.playClickSound(player);
                    new ClassesFreeGUI(main).getInventory().open(player);
                }));

        contents.set(1, 6, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.EMERALD),
                        main.color("&eToken Classes"),
                        main.color("&7Classes unlocked with Tokens!"),
                        main.color(""),
                        main.color("&fUnlocked: &a" + 7 + "/" + 7), // TODO: Don't hardcode this.
                        main.color("&fMy Tokens: &a" + 12)),
                e -> {
                    SoundManager.playClickSound(player);
                    new ClassesTokenGUI(main).getInventory().open(player);
                }));

        contents.set(2, 4, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
                        main.color("&eLevel Classes"),
                        main.color("&7Classes unlocked with Levels"),
                        main.color(""),
                        main.color("&fUnlocked: &a" + 21 + "/" + 21), // TODO:
                        main.color("&fMy Level: &a" + 34)), // TODO:
                e -> {
                    SoundManager.playClickSound(player);
                    new ClassesLevelGUI(main).getInventory().open(player);
                }));

        contents.set(0, 4, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.GOLD_BLOCK),
                        main.color("&eFavorite Classes"),
                        main.color("&7Classes you have favorited here"), "",
                        "" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Left Click" + ChatColor.RESET + ChatColor.YELLOW + " to choose a class",
                        "" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Right Click" + ChatColor.RESET + ChatColor.YELLOW + " to choose random favorite class"),
                e -> {
                    // TODO: Favorite GUI
//                    if (e.isLeftClick()) {
//                        SoundManager.playClickSound(player);
//                        new FavoriteClassesGUI(main).inv.open(player);
//                    } else if (e.isRightClick()) {
//                        GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);
//
//                        if (instance != null && instance.state == GameState.WAITING) {
//                            if (!data.customIntegers.isEmpty()) {
//                                instance.boards.get(player).updateLine(3, main.color("&fClass: &eRandom Fav"));
//                                instance.favClassSelection.add(player);
//                                instance.classSelection.remove(player);
//                                player.sendMessage(
//                                main.color("&2&l(!) &rYou selected to go a random favorite class!"));
//                                player.setDisplayName(player.getName());
//                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
//                            } else {
//                                player.sendMessage(
//                                main.color("&c&l(!) &rYou don't have any favorite classes!"));
//                            }
//                        } else {
//                            SoundManager.playClickSound(player);
//                            new FavoriteClassesGUI(main).inv.open(player);
//                        }
//                    }
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
