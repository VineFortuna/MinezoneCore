package anthony.SuperCraftBrawl.gui.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.fishing.FishRarity;
import anthony.SuperCraftBrawl.fishing.FishType;
import anthony.SuperCraftBrawl.fishing.Fishing;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FishingGUI implements InventoryProvider {
    
    public Core main;
    public SmartInventory inv;
    private Player target;
    private int currentInfo = 0;
    
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

        // Sort FishType values by rarity and then alphabetically
        FishType[] sortedFishTypes = Arrays.stream(FishType.values())
                .sorted(Comparator.comparing(FishType::getRarity)
                        .thenComparing(FishType::getName, String.CASE_INSENSITIVE_ORDER)).toArray(FishType[]::new);

        ClickableItem[] items = new ClickableItem[FishType.values().length];
    
        int i = 0;
        for (FishType type : sortedFishTypes) {
            FishingDetails details = data.playerFishing.get(type.getId());
            ItemStack item = ItemHelper.setDetails(ItemHelper.createDye(DyeColor.GRAY, 1), "&c???",
                    type.getRarity().getColor() + type.getRarity().getName(), "");

            if (details != null && details.timesCaught > 0) {
                item = type.getItem();
                ItemHelper.setDetails(item, item.getItemMeta().getDisplayName(),
                        item.getItemMeta().getLore(), "", main.color("&7Times caught: " + details.timesCaught));
            }
            ItemHelper.setDetails(item, item.getItemMeta().getDisplayName(),
                    item.getItemMeta().getLore(), main.color("&7Found in: "), main.color(generateAreas(type)));

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
        
        if (inv.getParent().isPresent()) {
            contents.set(4, 8, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                        inv.getParent().get().open(player);
                    }));
        }
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

        FishingDetails mapDetails = data.playerFishing.get(FishType.MAP.getId());
        if (mapDetails != null && mapDetails.carrying > 0) {
            Location treasureLoc = main.getFishing().getTreasureLoc(data.treasureLoc);
            contents.set(4, 4, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.MAP, mapDetails.carrying), ChatColor.GRAY + "Treasure Map",
                            "", main.color("&e" + treasureLoc.getBlockX() + ", " + treasureLoc.getBlockY()
                                    + ", " + treasureLoc.getBlockZ())), e -> {
                    }));
        }

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
        String warps = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFlNzhmZjQ3NjNlOWFkMWE5OThjNzI4ZjcxZmE1ZGJiZDYxNjRhMjdjYTFmMGU0MjMyYzQxZDc0MjA4MTgwYSJ9fX0=";
        String info = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzA3ZGFiMmNiZWJlYTUzOWI2NGQ1YWQyNDZmOWNjYzFmY2RhN2FhOTRiODhlNTlmYzI4Mjk4NTJmNDYwNzEifX19";
    
        contents.set(0, 8,
                ClickableItem.of(ItemHelper.createSkullTexture(warps,
                        main.color("&aGo Fishing!"),
                        main.color("&7Instantly travel to a body of water")), e -> {
                    new FishingAreasGUI(main, inv).inv.open(player);
                }));

        List<String> infoMessages = Arrays.asList(
                "Click me for information about fishing!",
                "Grab your rod and go fishing in any body of water",
                "Reel in sea creatures, junk, or even treasure",
                "Some catches help you unlock classes faster",
                "Each catch has a type and/or rarity",
                "Certain catches can only be found in specific areas",
                "You can also claim rewards and upgrade your rod",
                "Fast travel to bodies of water using the globe",
                "Fun fishing! Squawk!"
        );

        contents.set(0, 0,
                ClickableItem.of(ItemHelper.createSkullTexture(info,
                                "&c&lSquawkbuckler",
                        "&7" + infoMessages.get(currentInfo),
                                "&8" + (currentInfo + 1) + "/" + infoMessages.size()), e -> {
                    if (currentInfo < infoMessages.size() - 1)
                        currentInfo++;
                    else
                        currentInfo = 0;
                    ItemHelper.setLore(player.getOpenInventory().getItem(0),
                            Arrays.asList(main.color("&7" + infoMessages.get(currentInfo)),
                            main.color("&8" + (currentInfo + 1) + "/" + infoMessages.size())));
                }));
        
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
        StringBuilder areas = new StringBuilder();
        if (type.getAreas() == null || type.getAreas().isEmpty()) {
            areas.append("All");
        } else {
            for (int i = 0; i < type.getAreas().size(); i++) {
                areas.append(type.getAreas().get(i).getName());
                if (i < type.getAreas().size() - 1) {
                    areas.append(", ");
                }
            }
        }
        return main.color("&e" + areas);
    }
    
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
    
}
