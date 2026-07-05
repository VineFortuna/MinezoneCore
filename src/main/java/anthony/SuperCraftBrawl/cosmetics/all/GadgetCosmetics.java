package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRegistry;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class GadgetCosmetics {

    private static final int GADGET_SLOT = 5;

    private GadgetCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        registry.register(magicBroom(main));
        registry.register(paintballGun(main));
        registry.register(melon(main));
        registry.register(fishingRod(main));
    }

    private static Cosmetic magicBroom(Core main) {
        return new Cosmetic("magic_broom", CosmeticCategory.GADGET, "Magic Broom",
                Rank.PRO.getTag() + "&r Exclusive", "Purchase the PRO rank") {
            public ItemStack getIcon(Player player) {
                List<String> lore = new ArrayList<>();
                lore.add(main.color("&7Fly around like a Witch!"));
                return withRequirementLore(ItemHelper.create(Material.WHEAT, main.color("&6Magic Broom"), lore));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.wheat");
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need the rank " + Rank.PRO.getTag() + "&f to use this!");
            }

            public void onEquip(Player player) {
                player.getInventory().setItem(GADGET_SLOT, getIcon(player));
            }

            public void onUnequip(Player player) {
                player.getInventory().setItem(GADGET_SLOT, null);
            }
        };
    }

    private static Cosmetic paintballGun(Core main) {
        return new Cosmetic("paintball_gun", CosmeticCategory.GADGET, "Paintball Gun",
                "Mystery Chest Reward", "Collect paintballs from Mystery Chests") {
            public ItemStack getIcon(Player player) {
                int paintballs = main.getDataManager().getPlayerData(player).paintball;
                List<String> lore = new ArrayList<>();
                lore.add(main.color("&7Shoot paintballs as you want"));
                lore.add("");
                lore.add(main.color("&fYou have &a" + paintballs + "&f paintballs"));
                return withRequirementLore(ItemHelper.create(Material.GOLD_BARDING, main.color("&9Paintball Gun"), lore));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).paintball > 0;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou do not have any paintballs! Go collect from MysteryChests");
            }

            public void onEquip(Player player) {
                ItemStack item = ItemHelper.setDetails(new ItemStack(Material.GOLD_BARDING),
                        main.color("&9Paintball Gun"),
                        main.color("&7Right click to shoot a paintball!"));
                player.getInventory().setItem(GADGET_SLOT, item);
            }

            public void onUnequip(Player player) {
                player.getInventory().setItem(GADGET_SLOT, null);
            }
        };
    }

    private static Cosmetic melon(Core main) {
        return new Cosmetic("melon", CosmeticCategory.GADGET, "Melons",
                "Mystery Chest Reward", "Collect melons from Mystery Chests") {
            public ItemStack getIcon(Player player) {
                int melons = main.getDataManager().getPlayerData(player).melon;
                List<String> lore = new ArrayList<>();
                lore.add(main.color("&7A delicious melon that gives you..."));
                lore.add(main.color("&7Superpowers!"));
                lore.add("");
                lore.add(main.color("&fYou have &a" + melons + "&f melons"));
                return withRequirementLore(ItemHelper.create(Material.MELON, main.color("&aMelons"), lore));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).melon > 0;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou do not have any melons! Go collect from MysteryChests");
            }

            public void onEquip(Player player) {
                player.getInventory().setItem(GADGET_SLOT, getIcon(player));
            }

            public void onUnequip(Player player) {
                player.getInventory().setItem(GADGET_SLOT, null);
            }
        };
    }

    private static Cosmetic fishingRod(Core main) {
        return new Cosmetic("fishing_rod", CosmeticCategory.GADGET, "Fishing Rod") {
            public ItemStack getIcon(Player player) {
                return main.getFishingRod(player);
            }

            public boolean isUnlocked(Player player) {
                return true;
            }

            public String getUnlockMessage(Player player) {
                return "";
            }

            public void onEquip(Player player) {
                player.getInventory().setItem(GADGET_SLOT, getIcon(player));
            }

            public void onUnequip(Player player) {
                player.getInventory().setItem(GADGET_SLOT, null);
            }
        };
    }
}