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

public final class TitleCosmetics {

    private static final String FREDDY_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiMjdjY2I0ZjEyNjQwZjFiNThlYTYyZDkwY2RhY2U0NGMwZjJkYTlmMzkwOGUyNWViMTZiZGI1YmJiNWE2NSJ9fX0";
    private static final String HOHOHO_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";

    private TitleCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        main.getTitlePacket().registerTitle("Trick-or-Treater", main.color("&6&lTrick-or-Treater"), 0.2);
        main.getTitlePacket().registerTitle("Freddy Fazbear", main.color("&6&lFreddy Fazbear"), 0.2);
        main.getTitlePacket().registerTitle("Fiesta De La Noche", main.color("&b&lFIESTA DE LA NOCHE"), 0.2);
        main.getTitlePacket().registerTitle("Merry", main.color("&c&lMerry"), 0.2);
        main.getTitlePacket().registerTitle("Ho Ho Ho", main.color("&c&lHO &2&lHO &c&lHO"), 0.2);
        main.getTitlePacket().registerTitle("SCB Summer Champ 2021", main.color("&b&lSCB SUMMER CHAMP (2021)"), 0.2);
        main.getTitlePacket().registerTitle("SCB Summer Champ 2022", main.color("&b&lSCB SUMMER CHAMP (2022)"), 0.2);
        main.getTitlePacket().registerTitle("SCB Cash Cup Champ 2023", main.color("&a&lSCB CASH CUP CHAMP (2023)"), 0.2);
        main.getTitlePacket().registerTitle("SCB Halloween Champ 2024", main.color("&6&lSCB HALLOWEEN CHAMP (2024)"), 0.2);
        main.getTitlePacket().registerTitle("SCB Winter Champ 2025", main.color("&b&lSCB WINTER CHAMP (2025)"), 0.2);

        registry.register(trickOrTreater(main));
        registry.register(freddyFazbear(main));
        registry.register(fiestaDeLaNoche(main));
        registry.register(merry(main));
        registry.register(hoHoHo(main));
        registry.register(nameLockedTitle(main, "SCB Summer Champ 2021", Material.SAND,
                "&b&lSCB SUMMER CHAMP (2021)", "2021 Summer Champion", "CowNecromancer"));
        registry.register(nameLockedTitle(main, "SCB Summer Champ 2022", Material.SAND,
                "&b&lSCB SUMMER CHAMP (2022)", "2022 Summer Champion", "Adwyr"));
        registry.register(nameLockedTitle(main, "SCB Cash Cup Champ 2023", Material.EMERALD,
                "&a&lSCB CASH CUP CHAMP (2023)", "2023 Cash Cup Champion", "N1zman"));
        registry.register(nameLockedTitle(main, "SCB Halloween Champ 2024", Material.JACK_O_LANTERN,
                "&6&lSCB HALLOWEEN CHAMP (2024)", "2024 Halloween Champion", "Jessey2105"));
        registry.register(nameLockedTitle(main, "SCB Winter Champ 2025", Material.SNOW_BLOCK,
                "&b&lSCB WINTER CHAMP (2025)", "2025 Winter Champion", "CowNecromancer"));
    }

    private static Cosmetic trickOrTreater(Core main) {
        return new Cosmetic("Trick-or-Treater", CosmeticCategory.TITLE, "Trick-or-Treater",
                "Halloween 2025 Exclusive", "Find 2 baskets in the lobby") {
            public ItemStack getIcon(Player player) {
                int found = main.getHalloweenManager() != null ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.PUMPKIN), main.color("&6&lTrick-or-Treater"),
                        main.color("&8Progress: &e" + Math.min(found, 2) + "&7/2")));
            }

            public boolean isUnlocked(Player player) {
                int found = main.getHalloweenManager() != null ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return found >= 2;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need to find &e2 baskets &rto use &6&lTrick-or-Treater &rtitle.");
            }

            public void onEquip(Player player) {
                main.getTitlePacket().setTitle(player, id);
            }

            public void onUnequip(Player player) {
                main.getTitlePacket().clearTitle(player);
            }
        };
    }

    private static Cosmetic freddyFazbear(Core main) {
        return new Cosmetic("Freddy Fazbear", CosmeticCategory.TITLE, "Freddy Fazbear",
                "Halloween 2025 Exclusive", "Find 8 baskets in the lobby") {
            public ItemStack getIcon(Player player) {
                int found = main.getHalloweenManager() != null ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return withRequirementLore(ItemHelper.createSkullTexture(FREDDY_TEXTURE, "&6&lFreddy Fazbear",
                        main.color("&8Progress: &e" + Math.min(found, 8) + "&7/8")));
            }

            public boolean isUnlocked(Player player) {
                int found = main.getHalloweenManager() != null ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return found >= 8;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need to find &e8 baskets &rto use &6&lFreddy Fazbear &rtitle.");
            }

            public void onEquip(Player player) {
                main.getTitlePacket().setTitle(player, id);
            }

            public void onUnequip(Player player) {
                main.getTitlePacket().clearTitle(player);
            }
        };
    }

    private static Cosmetic fiestaDeLaNoche(Core main) {
        return new Cosmetic("Fiesta De La Noche", CosmeticCategory.TITLE, "Fiesta De La Noche",
                Rank.VIP.getTag() + "&r+ Exclusive", "Purchase the VIP rank or higher") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.FIREWORK), main.color("&b&lFIESTA DE LA NOCHE"),
                        main.color("&7Go listen to the album DiscO-Zone.")));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.fiesta");
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need the rank &e&lVIP&r+ for this!");
            }

            public void onEquip(Player player) {
                main.getTitlePacket().setTitle(player, id);
            }

            public void onUnequip(Player player) {
                main.getTitlePacket().clearTitle(player);
            }
        };
    }

    private static Cosmetic merry(Core main) {
        return new Cosmetic("Merry", CosmeticCategory.TITLE, "Merry",
                "Christmas 2025 Exclusive", "Claim the Day 6 advent calendar reward") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.LONG_GRASS, 1, (byte) 2), "&c&lMerry"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).merryTitle == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou must claim the &eDay 6 reward &rto use the &c&lMerry &rtitle.");
            }

            public void onEquip(Player player) {
                main.getTitlePacket().setTitle(player, id);
            }

            public void onUnequip(Player player) {
                main.getTitlePacket().clearTitle(player);
            }
        };
    }

    private static Cosmetic hoHoHo(Core main) {
        return new Cosmetic("Ho Ho Ho", CosmeticCategory.TITLE, "Ho Ho Ho",
                "Christmas 2025 Exclusive", "Claim the Christmas Day advent calendar reward") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.createSkullTexture(HOHOHO_TEXTURE, "&c&lHO &2&lHO &c&lHO"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).hohohoTitle == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou must claim the &eChristmas Day reward &rto use the &c&lHO &2&lHO &c&lHO &rtitle.");
            }

            public void onEquip(Player player) {
                main.getTitlePacket().setTitle(player, id);
            }

            public void onUnequip(Player player) {
                main.getTitlePacket().clearTitle(player);
            }
        };
    }

    /** The five past-tournament-champion titles: same shape (locked to one exact player name), different icon/name. */
    private static Cosmetic nameLockedTitle(Core main, String cosmeticId, Material iconMaterial, String displayName,
                                             String championLabel, String requiredPlayerName) {
        return new Cosmetic(cosmeticId, CosmeticCategory.TITLE, main.color(displayName),
                "Tournament Champion Exclusive", "Awarded to the " + championLabel) {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(iconMaterial), main.color(displayName),
                        main.color("&7Exclusive title for the winner of the"),
                        main.color("&7the " + championLabel + ".")));
            }

            public boolean isUnlocked(Player player) {
                return player.getName().equals(requiredPlayerName);
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need to be the &b&l" + championLabel + " &rto use this title.");
            }

            public void onEquip(Player player) {
                main.getTitlePacket().setTitle(player, id);
            }

            public void onUnequip(Player player) {
                main.getTitlePacket().clearTitle(player);
            }
        };
    }
}
