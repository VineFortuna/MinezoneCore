package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRegistry;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class DeathEffectCosmetics {

    private DeathEffectCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        registry.register(simple("redstone", "Redstone", Material.REDSTONE));
        registry.register(simple("gold_apple", "Golden Apple", Material.GOLDEN_APPLE));
        registry.register(simple("glowstone", "Glowstone", Material.GLOWSTONE_DUST));
        registry.register(simple("cobweb", "Cobweb", Material.WEB));
        registry.register(simple("exp_bottle", "Exp Bottle", Material.EXP_BOTTLE));
        registry.register(simple("blaze_powder", "Blaze Powder", Material.BLAZE_POWDER));
        registry.register(snowball(main));
        registry.register(pumpkinPie(main));
    }

    /** The always-unlocked death particles have same format, different icon/name. */
    private static Cosmetic simple(String id, String displayName, Material iconMaterial) {
        return new Cosmetic(id, CosmeticCategory.DEATH_EFFECT, displayName) {
            public ItemStack getIcon(Player player) {
                return ItemHelper.create(iconMaterial, ChatColor.YELLOW + displayName);
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

    private static Cosmetic snowball(Core main) {
        return new Cosmetic("snowball", CosmeticCategory.DEATH_EFFECT, "Snowball") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(ItemHelper.create(Material.SNOW_BALL), ChatColor.YELLOW + "Snowball"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).snowballDeathEffect == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this yet!");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }

    private static Cosmetic pumpkinPie(Core main) {
        return new Cosmetic("pumpkin_pie", CosmeticCategory.DEATH_EFFECT, "Pumpkin Pie",
                "Halloween 2025 Exclusive") {
            public ItemStack getIcon(Player player) {
                int found = main.getHalloweenManager() != null ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return withRequirementLore(ItemHelper.setDetails(ItemHelper.create(Material.PUMPKIN_PIE), main.color("&6Pumpkin Pie"), "",
                        main.color("&7Progress: &e" + Math.min(found, 7) + "&7/7 baskets")));
            }

            public boolean isUnlocked(Player player) {
                return main.getListener().getHalloweenEventProgress(player) >= 7;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need &e7&r baskets to unlock this!");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }
        };
    }
}
