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

public final class WinEffectCosmetics {

    private static final String SETHBLING_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==";
    private static final String SANTA_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";

    private WinEffectCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        registry.register(defaultEffect(main));
        registry.register(magicBroom(main));
        registry.register(santa(main));
        registry.register(enderDragon(main));
        registry.register(fishRain(main));
        registry.register(flood(main));
        registry.register(treasureHoard(main));
        registry.register(ritual(main));
    }

    private static Cosmetic defaultEffect(Core main) {
        return new Cosmetic("default", CosmeticCategory.WIN_EFFECT, "Default Effect") {
            public ItemStack getIcon(Player player) {
                return ItemHelper.setDetails(ItemHelper.createSkullTexture(SETHBLING_TEXTURE), main.color("&eDefault Effect"),
                        "", main.color("&7Fireworks shoot up when"), main.color("&7winning a game!"));
            }

            public boolean isUnlocked(Player player) {
                return true;
            }

            public String getUnlockMessage(Player player) {
                return "";
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }

    private static Cosmetic magicBroom(Core main) {
        return new Cosmetic("magic_broom", CosmeticCategory.WIN_EFFECT, "Magic Broom",
                Rank.PRO.getTag() + "&r Exclusive", "Purchase the PRO rank") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.WHEAT), main.color("&eMagic Broom"),
                        "", main.color("&7Fly around the map with"), main.color("&7this when you win!")));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.winEffects");
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need the rank " + Rank.PRO.getTag() + "&r to use this!");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }

    private static Cosmetic santa(Core main) {
        return new Cosmetic("santa", CosmeticCategory.WIN_EFFECT, "Santa Claus",
                Rank.PRO.getTag() + "&r Exclusive", "Purchase the PRO rank") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(ItemHelper.createSkullTexture(SANTA_TEXTURE), main.color("&eSanta Claus"),
                        "", main.color("&7Become old Saint Nick himself"), main.color("&7and ride along!")));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.winEffects");
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need the rank " + Rank.PRO.getTag() + "&r to use this!");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }

    private static Cosmetic enderDragon(Core main) {
        return new Cosmetic("ender_dragon", CosmeticCategory.WIN_EFFECT, "EnderDragon",
                Rank.PRO.getTag() + "&r Exclusive", "Purchase the PRO rank") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.DRAGON_EGG), main.color("&eEnderDragon"), "",
                        main.color("&7Fly around the map with an"), main.color("&7EnderDragon when you win!")));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.winEffects");
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need the rank " + Rank.PRO.getTag() + "&r to use this!");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }

    private static Cosmetic fishRain(Core main) {
        return new Cosmetic("fish_rain", CosmeticCategory.WIN_EFFECT, "Fish Rain",
                "Fishing Reward", "Reach fishing reward level 4") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.RAW_FISH), main.color("&eFish Rain"), "",
                        main.color("&7Cover the map with fish"), main.color("&7falling from the sky")));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).rewardLevel >= 4;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this yet! Better get fishin'");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }

    private static Cosmetic flood(Core main) {
        return new Cosmetic("flood", CosmeticCategory.WIN_EFFECT, "Flood",
                "Fishing Reward", "Catch every type of fish") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.BOAT), main.color("&eFlood"), "",
                        main.color("&7Flood the map and"), main.color("&7ride a boat to safety")));
            }

            public boolean isUnlocked(Player player) {
                return main.getFishing().hasAllFish(player);
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this yet! Better get fishin'");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }

    private static Cosmetic treasureHoard(Core main) {
        return new Cosmetic("treasure_hoard", CosmeticCategory.WIN_EFFECT, "Treasure Hoard",
                "Fishing Reward", "Open the sunken treasure while fishing") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.GOLD_BLOCK), main.color("&eTreasure Hoard"), "",
                        main.color("&7Plunder shiny riches"), main.color("&7like a true pirate")));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).treasureOpened == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this yet! Better get fishin'");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }

    private static Cosmetic ritual(Core main) {
        return new Cosmetic("ritual", CosmeticCategory.WIN_EFFECT, "Ritual",
                "Halloween 2025 Exclusive", "Find 3 baskets in the lobby") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.NETHERRACK), main.color("&eRitual"), "",
                        main.color("&7Herobrine totem and bats"), main.color("&7take over the map...")));
            }

            public boolean isUnlocked(Player player) {
                return main.getListener().getHalloweenEventProgress(player) >= 3;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this cosmetic!");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }
}
