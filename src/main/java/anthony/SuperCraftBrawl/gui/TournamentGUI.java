package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class TournamentGUI implements InventoryProvider {
    
    public Core main;
    public SmartInventory inv;
    
    public TournamentGUI(Core main) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
                .title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Tournament Stats").build();
        this.main = main;
    }
    
    @Override
    public void init(Player player, InventoryContents contents) {
        main.sortTourney();
        contents.fillRow(0, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
        contents.fillRow(4, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
        if (main.tournamentend) {
            String goldTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM0YTU5MmE3OTM5N2E4ZGYzOTk3YzQzMDkxNjk0ZmMyZmI3NmM4ODNhNzZjY2U4OWYwMjI3ZTVjOWYxZGZlIn19fQ==";
            String silverTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTllMGExYmM2ZWIwYzZmMjcxZDMyM2ExOGUwMTQwY2U0M2Q5NTQ1OGI2YjViNmU4NDhkZjE3NDI1ZGJhOTZhZCJ9fX0=";
            String bronzeTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjQxMWMyOGVlZTVkNThkMWI4NjNiNTRlNWNjNjJjMzA3MjM0ZDQzN2MxN2YxZmY3NjMzOGRmZWNjM2NjNjhkNSJ9fX0=";
            ArrayList<String> names = new ArrayList<>(main.tourney.keySet());
            if (names.size() >= 1)
                contents.set(0, 3, ClickableItem.of(
                        ItemHelper.setDetails(ItemHelper.createSkullTexture(goldTexture), main.color("&6First Place"),
                                main.color("&ePlayer: &r" + names.get(0)),
                                main.color("&ePoints: &r" + main.tourney.get(names.get(0)))),
                        e -> {
                        }));
            if (names.size() >= 2)
                contents.set(0, 4, ClickableItem.of(
                        ItemHelper.setDetails(ItemHelper.createSkullTexture(silverTexture), main.color("&bSecond Place"),
                                main.color("&ePlayer: &r" + names.get(1)),
                                main.color("&ePoints: &r" + main.tourney.get(names.get(1)))),
                        e -> {
                        }));
            if (names.size() >= 3)
                contents.set(0, 5, ClickableItem.of(
                        ItemHelper.setDetails(ItemHelper.createSkullTexture(bronzeTexture), main.color("&cThird Place"),
                                main.color("&ePlayer: &r" + names.get(2)),
                                main.color("&ePoints: &r" + main.tourney.get(names.get(2)))),
                        e -> {
                        }));
            
        }
        
        contents.set(4, 4, ClickableItem.of(
                ItemHelper.setDetails(ItemHelper.create(Material.PAPER), main.color("&ePoints System"),
                        Arrays.asList(main.color("&ePoints are awarded as follows:"),
                                main.color("&e- 1st Place: &r10"),
                                main.color("&e- 2nd Place: &r7"),
                                main.color("&e- 3rd Place: &r5"),
                                main.color("&e- 4th Place: &r3"),
                                main.color("&e- 5th Place: &r1"),
                                main.color("&e- Flawless Win: &r5"),
                                main.color("&e- Kill: &r1"),
                                main.color("&e- First Blood: &r2"))),
                e -> {
                }));
        
        int x = 1, y = 1;
    
        for (String s : main.tourney.keySet()) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(s);
            ItemStack stats = ItemHelper.createSkullHeadPlayer(1, p.getName());
            
            PlayerData data = main.getDataManager().getOffPlayerData(p);
            String rank = "";
            if (data.getRank() != null) {
                rank = data.getRank().getTagWithSpace();
            };
            
            contents.set(y, x,
                    ClickableItem.of(
                            ItemHelper.setDetails(stats, main.color("&ePlayer: " + rank + "&r" + p.getName()),
                                    main.color("&ePoints: &r" + data.points)),
                            e -> {
                            }));
    
            x++;
    
            if (x > 7) {
                y++;
                x = 1;
            }
            if (y > 3)
                break;
        }
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
}
