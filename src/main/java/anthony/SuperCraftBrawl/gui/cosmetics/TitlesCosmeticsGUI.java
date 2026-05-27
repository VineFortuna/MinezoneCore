package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TitlesCosmeticsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public TitlesCosmeticsGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(4, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Titles").parent(parent).build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        ClickableItem border = ClickableItem
                .of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
                });

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                if (row == 0 || row == 3 || col == 0 || col == 8) {
                    contents.set(row, col, border);
                }
            }
        }

        final int basketsFoundForLore = (main.getHalloweenManager() != null)
                ? main.getHalloweenManager().getFoundCount(player.getUniqueId())
                : 0;

        ItemStack trickOrTreatTitle = ItemHelper.setDetails(new ItemStack(Material.PUMPKIN),
                main.color("&6&lTrick-or-Treater"), main.color("&7Unlock by finding 2 baskets in the lobby!"), "",
                main.color("&8Progress: &e" + Math.min(basketsFoundForLore, 2) + "&7/2"),
                main.color("&cHalloween 2025 exclusive"));

        contents.set(1, 1, ClickableItem.of(trickOrTreatTitle, e -> {
            int current = (main.getHalloweenManager() != null)
                    ? main.getHalloweenManager().getFoundCount(player.getUniqueId())
                    : 0;

            if (current < 2) {
                player.sendMessage(
                        main.color("&c&l(!) &rYou need to find &e2 baskets &rto use &6&lTrick-or-Treater &rtitle."));
                return;
            }

            enableDisableTitle(player, "Trick-or-Treater");
            inv.close(player);
        }));

        String freddyTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiMjdjY2I0ZjEyNjQwZjFiNThlYTYyZDkwY2RhY2U0NGMwZjJkYTlmMzkwOGUyNWViMTZiZGI1YmJiNWE2NSJ9fX0";
        ItemStack freddy = ItemHelper.createSkullTexture(freddyTexture, "&6&lFreddy Fazbear",
                "&7Unlock by finding 8 baskets in the lobby!", "",
                main.color("&8Progress: &e" + Math.min(basketsFoundForLore, 8) + "&7/8"), "&cHalloween 2025 Exclusive");

        contents.set(1, 2, ClickableItem.of(freddy, e -> {
            int current = (main.getHalloweenManager() != null)
                    ? main.getHalloweenManager().getFoundCount(player.getUniqueId())
                    : 0;

            if (current < 8) {
                player.sendMessage(
                        main.color("&c&l(!) &rYou need to find &e8 baskets &rto use &6&lFreddy Fazbear &rtitle."));
                return;
            }

            enableDisableTitle(player, "Freddy Fazbear");
            inv.close(player);
        }));

        ItemStack o_zone = ItemHelper.setDetails(new ItemStack(Material.FIREWORK), main.color("&b&lFIESTA DE LA NOCHE"),
                main.color("&7Go listen to the album DiscO-Zone."), "", main.color("&e&lVIP&8 or higher"));

        contents.set(1, 3, ClickableItem.of(o_zone, e -> {
            if (!player.hasPermission("scb.fiesta")) {
                player.sendMessage(main.color("&c&l(!) &rYou need the rank &e&lVIP&r+ for this!"));
                return;
            }

            enableDisableTitle(player, "Fiesta De La Noche");
            inv.close(player);
        }));

        ItemStack merry = ItemHelper.setDetails(new ItemStack(Material.LONG_GRASS, 1, (byte) 2), "&c&lMerry",
                "&7Unlock through the advent calendar!",
                "",
                "&cChristmas 2025 Exclusive");

        contents.set(1, 4, ClickableItem.of(merry, e -> {
            if (data.merryTitle == 0) {
                player.sendMessage(main.color(
                        "&c&l(!) &rYou must claim the &eDay 6 reward &rto use the &c&lMerry &rtitle."
                ));
                return;
            }

            enableDisableTitle(player, "Merry");
            inv.close(player);
        }));

        String hohohoTexture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";
        ItemStack hohoho = ItemHelper.createSkullTexture(hohohoTexture, "&c&lHO &2&lHO &c&lHO",
                "&7Unlock through the advent calendar!",
                "",
                "&cChristmas 2025 Exclusive");

        contents.set(1, 5, ClickableItem.of(hohoho, e -> {
            if (data.hohohoTitle == 0) {
                player.sendMessage(main.color(
                        "&c&l(!) &rYou must claim the &eChristmas Day reward &rto use the &c&lHO &2&lHO &c&lHO &rtitle."
                ));
                return;
            }

            enableDisableTitle(player, "Ho Ho Ho");
            inv.close(player);
        }));

        ItemStack summer2021 = ItemHelper.setDetails(new ItemStack(Material.SAND),
                main.color("&b&lSCB SUMMER CHAMP (2021)"),
                main.color("&7Exclusive title for the winner of the"),
                main.color("&72021 SCB Summer Championship."),
                "",
                main.color("&8Requires: &b2021 Summer Champion"));

        contents.set(1, 6, ClickableItem.of(summer2021, e -> {
            if (!player.getName().equals("CowNecromancer")) {
                player.sendMessage(main.color("&c&l(!) &rYou need to be the &b&l2021 SCB Summer Champion &rto use this title."));
                return;
            }

            enableDisableTitle(player, "SCB Summer Champ 2021");
            inv.close(player);
        }));

        ItemStack summer2022 = ItemHelper.setDetails(new ItemStack(Material.SAND),
                main.color("&b&lSCB SUMMER CHAMP (2022)"),
                main.color("&7Exclusive title for the winner of the"),
                main.color("&72022 SCB Summer Championship."),
                "",
                main.color("&8Requires: &b2022 Summer Champion"));

        contents.set(1, 7, ClickableItem.of(summer2022, e -> {
            if (!player.getName().equals("Adwyr")) {
                player.sendMessage(main.color("&c&l(!) &rYou need to be the &b&l2022 SCB Summer Champion &rto use this title."));
                return;
            }

            enableDisableTitle(player, "SCB Summer Champ 2022");
            inv.close(player);
        }));

        ItemStack cashCup2023 = ItemHelper.setDetails(new ItemStack(Material.EMERALD),
                main.color("&a&lSCB CASH CUP CHAMP (2023)"),
                main.color("&7Exclusive title for the winner of the"),
                main.color("&72023 SCB Cash Cup."),
                "",
                main.color("&8Requires: &a2023 Cash Cup Champion"));

        contents.set(2, 1, ClickableItem.of(cashCup2023, e -> {
            if (!player.getName().equals("N1zman")) {
                player.sendMessage(main.color("&c&l(!) &rYou need to be the &a&l2023 SCB Cash Cup Champion &rto use this title."));
                return;
            }

            enableDisableTitle(player, "SCB Cash Cup Champ 2023");
            inv.close(player);
        }));

        ItemStack halloween2024 = ItemHelper.setDetails(new ItemStack(Material.JACK_O_LANTERN),
                main.color("&6&lSCB HALLOWEEN CHAMP (2024)"),
                main.color("&7Exclusive title for the winner of the"),
                main.color("&72024 SCB Halloween Tournament."),
                "",
                main.color("&8Requires: &62024 Halloween Champion"));

        contents.set(2, 2, ClickableItem.of(halloween2024, e -> {
            if (!player.getName().equals("Jessey2105")) {
                player.sendMessage(main.color("&c&l(!) &rYou need to be the &6&l2024 SCB Halloween Champion &rto use this title."));
                return;
            }

            enableDisableTitle(player, "SCB Halloween Champ 2024");
            inv.close(player);
        }));

        ItemStack winter2025 = ItemHelper.setDetails(new ItemStack(Material.SNOW_BLOCK),
                main.color("&b&lSCB WINTER CHAMP (2025)"),
                main.color("&7Exclusive title for the winner of the"),
                main.color("&72025 SCB Winter Championship."),
                "",
                main.color("&8Requires: &b2025 Winter Champion"));

        contents.set(2, 3, ClickableItem.of(winter2025, e -> {
            if (!player.getName().equals("CowNecromancer")) {
                player.sendMessage(main.color("&c&l(!) &rYou need to be the &b&l2025 SCB Winter Champion &rto use this title."));
                return;
            }

            enableDisableTitle(player, "SCB Winter Champ 2025");
            inv.close(player);
        }));

        contents.set(3, 4, ClickableItem
                .of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }));
    }

    private void enableDisableTitle(Player player, String title) {
        boolean nowEnabled = main.getTrickPacket().toggleTitle(player, title);
        player.sendMessage(main.color(nowEnabled ? "&9&l(!) &rYou have enabled &e" + title + " &rtitle"
                : "&9&l(!) &rYou have disabled &e" + title + " &rtitle"));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}