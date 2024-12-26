package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.map.FishArea;
import anthony.SuperCraftBrawl.fishing.FishRarity;
import anthony.SuperCraftBrawl.fishing.FishType;
import anthony.SuperCraftBrawl.playerdata.FishingDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class FishingGUI implements InventoryProvider {
    
    public Core main;
    public SmartInventory inv;
    private Player target;
    
    public FishingGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Fishing").parent(parent).build();
        this.main = main;
    }
    
    public FishingGUI(Core main, Player target, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Fishing").parent(parent).build();
        this.main = main;
        this.target = target;
    }
    
    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        
        PlayerData data = main.getDataManager().getPlayerData(player);
        if (this.target != null)
            data = main.getDataManager().getPlayerData(target);
        
        contents.fillBorders(ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
        
        
        
        ClickableItem[] items = new ClickableItem[FishType.values().length];
    
        int i = 0;
        for (FishType type : FishType.values()) {
            FishingDetails details = data.playerFishing.get(type.getId());
            ItemStack item = ItemHelper.setDetails(ItemHelper.createDye(DyeColor.GRAY, 1), main.color("&c???"));
            
            if (details != null && details.timesCaught > 0) {
                item = type.getItem();
                ItemHelper.setDetails(item, item.getItemMeta().getDisplayName(),
                        item.getItemMeta().getLore(), "", main.color("&7Times caught: " + details.timesCaught));
            }
            ItemHelper.setDetails(item, item.getItemMeta().getDisplayName(),
                    item.getItemMeta().getLore(), "", main.color("&7Found in: "), main.color(generateAreas(type)));
            
            items[i] = ClickableItem.empty(item);
            i++;
        }
        
        pagination.setItems(items);
        pagination.setItemsPerPage(21);
        
        Location fishingLoc = new Location(main.getLobbyWorld(), 303.500, 91.0, 526.500, 144.4F, 0.0F);
    
        if (data != null) {
            contents.set(0, 4,
                    ClickableItem.of(ItemHelper.createSkullHeadPlayer(1, data.playerName, main.color("&e" + data.playerName),
                            Arrays.asList(main.color("&aRank: &r" + data.getRank().getTag()),
                                    main.color("&aLevel: &r" + data.level),
                                    main.color("&aCaught: &r" + (data.totalcaught)))), e -> {
                    }));
        }
    
        PlayerData finalData = data;
        contents.set(0, 8,
                ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD),
                        main.color("&aGo Fishing!"), main.color("&7Instantly travel to the pond and cast your line"),
                        "", main.color("&eClick to teleport")), e -> {
                    
                    if (main.getGameManager().GetInstanceOfPlayer(player) == null &&
                            main.getGameManager().GetInstanceOfSpectator(player) == null) {
                        player.teleport(fishingLoc);
                        player.sendMessage(main.color("&3&l(!) &rGrab a rod and go fishing!"));
                        
                        if (!(player.getInventory().contains(main.getFishingRod(player)))) {
                            player.getInventory().setItem(5, main.getFishingRod(player));
                            player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You have equipped " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Fishing Rod");
                            inv.close(player);
                        }
                    } else {
                        player.sendMessage(main.color("&c&l(!) &rYou cannot do this while in a game!"));
                    }
                }));
        
        contents.set(4, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }));
        contents.set(4, 3, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.EMERALD), ChatColor.GRAY + "Rewards",
                        "", main.color("&eClick to view rewards")), e -> {
                    new FishingRewardsGUI(main, inv).inv.open(player);
                }));
        contents.set(4, 5, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ANVIL), ChatColor.GRAY + "Upgrades",
                        "", main.color("&eClick to view upgrades")), e -> {
                    new FishingUpgradesGUI(main, inv).inv.open(player);
                }));
        contents.set(4, 0, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.PAPER), ChatColor.GRAY + "Chance Breakdown",
                        main.color("&eTotal chance:"),
                        main.color("&e- Junk " + FishRarity.JUNK.getChance() + "%"),
                        main.color("&e- Sea Creature " + (100-(FishRarity.JUNK.getChance() +
                                FishRarity.TREASURE.getChance())) + "%"),
                        main.color("&e- Treasure " + FishRarity.TREASURE.getChance() + "%"),
                        "",
                        main.color("&eSea Creature chance:"),
                        main.color("&e- Common " + FishRarity.COMMON.getChance() + "%"),
                        main.color("&e- Rare " + FishRarity.RARE.getChance() + "%"),
                        main.color("&e- Epic " + FishRarity.EPIC.getChance() + "%"),
                        main.color("&e- Mythic " + FishRarity.MYTHIC.getChance() + "%"),
                        main.color("&e- Legendary " + FishRarity.LEGENDARY.getChance() + "%")), e -> {
                }));
        
        String next = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
        String prev = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
        
        if (!pagination.isFirst()) {
            contents.set(2, 0, ClickableItem.of(ItemHelper.createSkullTexture(prev, ChatColor.GRAY + "Previous Page"),
                    e -> inv.open(player, pagination.previous().getPage())));
        }
        if (!pagination.isLast()) {
            contents.set(2, 8, ClickableItem.of(ItemHelper.createSkullTexture(next, ChatColor.GRAY + "Next Page"),
                    e -> inv.open(player, pagination.next().getPage())));
        }
    
        SlotIterator iter = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 0));
        iter.blacklist(1, 0);
        iter.blacklist(2, 0);
        iter.blacklist(3, 0);
        iter.blacklist(1, 8);
        iter.blacklist(2, 8);
        iter.blacklist(3, 8);
        pagination.addToIterator(iter);
    }
    
    private String generateAreas(FishType type) {
        String areas = "";
        if (type.getAreas() == null) {
            areas += "All";
        }
        else {
            for (FishArea area : type.getAreas()) {
                areas += area.getName() + " ";
            }
        }
        return areas;
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
    
}
