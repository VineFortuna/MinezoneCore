package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRegistry;
import anthony.SuperCraftBrawl.cosmetics.Outfit;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class OutfitCosmetics {

    private static final String ELF_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQwZmJjN2E2YWQ4M2U5MjRkYjZjYTBjYTM0N2RjZjVmMmY0MzRmMzQ3NDJmODMyOTYwYTA0MDZmYmRiYjE4NyJ9fX0=";
    private static final String SANTA_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";
    private static final String PIRATE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjlhYzgwNGEyYzVhOGVhNTdlZjY5NjU3YWI2NDM0N2QxZWQzNmIzNGNhNzBhMjE4ZjZhNjNkNWI2YWEyZmU5ZiJ9fX0=";
    private static final String FREDDY_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiMjdjY2I0ZjEyNjQwZjFiNThlYTYyZDkwY2RhY2U0NGMwZjJkYTlmMzkwOGUyNWViMTZiZGI1YmJiNWE2NSJ9fX0=";
    private static final String RUDOLPH_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWJkNDZiMzhiMjFiMzQyY2FmOTE3YWQ5Y2E0MmFmYjY4Mzg4YTU1OTFiY2M5YWRlZDFlOGUzNDZlMTg4OTAifX19";

    private OutfitCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        registry.register(astronaut(main));
        registry.register(santa(main));
        registry.register(pirate(main));
        registry.register(elf(main));
        registry.register(golden(main));
        registry.register(diamond(main));
        registry.register(freddy(main));
        registry.register(rudolph(main));
    }

    private static Cosmetic astronaut(Core main) {
        return new Cosmetic("astronaut", CosmeticCategory.OUTFIT, "Astronaut Outfit",
                "Mystery Chest Reward", "Unlocked from Mystery Chests") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.create(Material.GLASS, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Astronaut Outfit"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).astronaut == 1;
            }

            public String getUnlockMessage(Player player) {
                return "" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have not unlocked this cosmetic yet!";
            }

            public void onEquip(Player player) {
                new Outfit(displayName, getIcon(player),
                        ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.WHITE),
                        ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.WHITE),
                        ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.WHITE)
                ).equip(player);
            }

            public void onUnequip(Player player) {
                main.getListener().resetArmor(player);
            }
        };
    }

    private static Cosmetic santa(Core main) {
        return new Cosmetic("santa", CosmeticCategory.OUTFIT, "Santa Outfit",
                "Mystery Chest Reward", "Unlocked from Mystery Chests") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.createSkullTexture(SANTA_TEXTURE, ChatColor.RED.toString() + ChatColor.BOLD + "Santa Outfit"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).santaoutfit == 1;
            }

            public String getUnlockMessage(Player player) {
                return "" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have not unlocked this cosmetic yet!";
            }

            public void onEquip(Player player) {
                new Outfit(displayName, getIcon(player),
                        ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.RED),
                        ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.RED),
                        ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.RED)
                ).equip(player);
            }

            public void onUnequip(Player player) {
                main.getListener().resetArmor(player);
            }
        };
    }

    private static Cosmetic pirate(Core main) {
        return new Cosmetic("pirate", CosmeticCategory.OUTFIT, "Pirate Outfit",
                "Fishing Reward", "Reach fishing reward level 6") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.createSkullTexture(PIRATE_TEXTURE, "&3&lPirate Outfit"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).rewardLevel >= 6;
            }

            public String getUnlockMessage(Player player) {
                return "" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have not unlocked this cosmetic yet!";
            }

            public void onEquip(Player player) {
                new Outfit(displayName, getIcon(player),
                        ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.GREEN),
                        ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.MAROON),
                        ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.BLACK)
                ).equip(player);
            }

            public void onUnequip(Player player) {
                main.getListener().resetArmor(player);
            }
        };
    }

    private static Cosmetic elf(Core main) {
        return new Cosmetic("elf", CosmeticCategory.OUTFIT, "Elf Outfit",
                "Christmas 2024 Exclusive", "Awarded during the Christmas 2024 event") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.createSkullTexture(ELF_TEXTURE, "&a&lElf Outfit"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).elfCosmetic == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this cosmetic yet!");
            }

            public void onEquip(Player player) {
                new Outfit(displayName, getIcon(player),
                        ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.GREEN),
                        ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.RED),
                        ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.GREEN)
                ).equip(player);
            }

            public void onUnequip(Player player) {
                main.getListener().resetArmor(player);
            }
        };
    }

    private static Cosmetic golden(Core main) {
        return new Cosmetic("golden", CosmeticCategory.OUTFIT, "Golden Outfit",
                Rank.VIP.getTag() + "&r+ Exclusive", "Purchase the VIP rank or higher") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.create(Material.GOLD_HELMET, main.color("&6&lGolden Outfit")));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.vip");
            }

            public String getUnlockMessage(Player player) {
                return "" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                        + Rank.VIP.getTag() + ChatColor.RESET + " to use this!";
            }

            public void onEquip(Player player) {
                new Outfit(displayName, getIcon(player),
                        ItemHelper.create(Material.GOLD_CHESTPLATE),
                        ItemHelper.create(Material.GOLD_LEGGINGS),
                        ItemHelper.create(Material.GOLD_BOOTS)
                ).equip(player);
            }

            public void onUnequip(Player player) {
                main.getListener().resetArmor(player);
            }
        };
    }

    private static Cosmetic diamond(Core main) {
        return new Cosmetic("diamond", CosmeticCategory.OUTFIT, "Diamond Outfit",
                Rank.PRO.getTag() + "&r Exclusive", "Purchase the PRO rank") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.create(Material.DIAMOND_HELMET, main.color("&b&lDiamond Outfit")));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.pro");
            }

            public String getUnlockMessage(Player player) {
            return "" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                        + Rank.PRO.getTag() + ChatColor.RESET + " to use this!";
            }

            public void onEquip(Player player) {
                new Outfit(displayName, getIcon(player),
                        ItemHelper.create(Material.DIAMOND_CHESTPLATE),
                        ItemHelper.create(Material.DIAMOND_LEGGINGS),
                        ItemHelper.create(Material.DIAMOND_BOOTS)
                ).equip(player);
            }

            public void onUnequip(Player player) {
                main.getListener().resetArmor(player);
            }
        };
    }

    private static Cosmetic freddy(Core main) {
        return new Cosmetic("freddy", CosmeticCategory.OUTFIT, "Freddy Outfit",
                "Halloween 2025 Exclusive", "Find 6 baskets in the lobby") {
            public ItemStack getIcon(Player player) {
                int basketsFound = main.getHalloweenManager() != null
                        ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return withRequirementLore(ItemHelper.createSkullTexture(FREDDY_TEXTURE, main.color("&4&6Freddy Outfit"), "",
                        main.color("&7Progress: &e" + Math.min(basketsFound, 6) + "&7/6")));
            }

            public boolean isUnlocked(Player player) {
                return main.getListener().getHalloweenEventProgress(player) >= 6;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need &e6/10 &rbaskets to use this!");
            }

            public void onEquip(Player player) {
                skullAndLeatherOutfit(displayName, getIcon(player), "7F3A1A", false).equip(player);
            }

            public void onUnequip(Player player) {
                main.getListener().resetArmor(player);
            }
        };
    }

    private static Cosmetic rudolph(Core main) {
        return new Cosmetic("rudolph", CosmeticCategory.OUTFIT, "Rudolph Outfit",
                "Christmas 2025 Exclusive", "Claim the Day 4 advent calendar reward") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.createSkullTexture(RUDOLPH_TEXTURE, main.color("&c&6Rudolph Outfit")));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).rudolphOutfit == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou must claim the &eDay 4 reward &rto use this!");
            }

            public void onEquip(Player player) {
                skullAndLeatherOutfit(displayName, getIcon(player), "7F3A1A", false).equip(player);
            }

            public void onUnequip(Player player) {
                main.getListener().resetArmor(player);
            }
        };
    }

    /** Given helmet + hex-dyed leather chest/legs/boots, all automatically sharing the given
     *  name, unbreakable, with an optional glow. */
    private static Outfit skullAndLeatherOutfit(String name, ItemStack helmet, String hex, boolean glow) {
        ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, hex);
        ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, hex);
        ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, hex);

        if (glow) {
            ItemHelper.setGlowing(helmet, true);
            ItemHelper.setGlowing(chestplate, true);
            ItemHelper.setGlowing(leggings, true);
            ItemHelper.setGlowing(boots, true);
        }

        ItemHelper.setUnbreakable(helmet);
        ItemHelper.setUnbreakable(chestplate);
        ItemHelper.setUnbreakable(leggings);
        ItemHelper.setUnbreakable(boots);

        return new Outfit(name, helmet, chestplate, leggings, boots);
    }
}