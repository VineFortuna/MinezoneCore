package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.halloween.Halloween;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRegistry;
import anthony.SuperCraftBrawl.cosmetics.DeathEffectAnimation;
import anthony.SuperCraftBrawl.cosmetics.ParticleHelper;
import anthony.SuperCraftBrawl.cosmetics.PassCosmetic;
import anthony.SuperCraftBrawl.cosmetics.PresentHeads;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
        if (Halloween.ENABLED) registry.register(pumpkinPie(main)); // a Halloween hunt reward
        registry.register(snowFlurry(main));
        registry.register(giftExplosion(main));
    }

    /** Base for death effects from the Battle Pass: they draw their own animation (see DeathEffectAnimation). */
    private abstract static class PassDeathEffect extends PassCosmetic implements DeathEffectAnimation {
        PassDeathEffect(Core main, String id, String displayName, Material icon, String... description) {
            super(main, CosmeticCategory.DEATH_EFFECT, id, displayName, "&e" + displayName, icon, 0, description);
        }
    }

    private static PassDeathEffect snowFlurry(Core main) {
        return new PassDeathEffect(main, "snow_flurry", "Snow Flurry", Material.SNOW_BLOCK,
                "&7A swirl of snowflakes rises", "&7where you were eliminated.") {
            public void play(Location where) {
                World world = where.getWorld();
                Location center = where.clone().add(0, 1, 0);
                world.playSound(center, Sound.DIG_SNOW, 1f, 1f);

                // Three bursts, each a bit higher than the last, so it looks like it's swirling upwards
                for (int pulse = 0; pulse < 3; pulse++) {
                    final Location at = center.clone().add(0, pulse * 0.5, 0);
                    Runnable burst = new Runnable() {
                        public void run() {
                            ParticleHelper.play(at, EnumParticle.SNOW_SHOVEL, 0.4f, 0.4f, 0.4f, 0.08f, 35);
                            ParticleHelper.play(at, EnumParticle.CLOUD, 0.3f, 0.3f, 0.3f, 0.04f, 8);
                        }
                    };
                    if (pulse == 0)
                        burst.run();
                    else
                        Bukkit.getScheduler().runTaskLater(main, burst, pulse * 4L);
                }
            }
        };
    }

    /**
     * The classic death effect: a ring of items dropped around where the player was eliminated. Nobody can
     * pick them up and they're removed after 5 seconds. The ring's i-th item is items[i % items.length].
     */
    public static void dropItemRing(Core main, Location where, ItemStack... items) {
        World world = where.getWorld();
        final List<Item> dropped = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5;
            Location at = new Location(world, where.getX() + Math.cos(angle) * 0.5, where.getY() + 1.5,
                    where.getZ() + Math.sin(angle) * 0.5);
            Item item = world.dropItem(at, items[i % items.length]);
            item.setPickupDelay(Integer.MAX_VALUE);
            dropped.add(item);
        }

        Bukkit.getScheduler().runTaskLater(main, new Runnable() {
            public void run() {
                for (Item item : dropped)
                    item.remove();
            }
        }, 5 * 20L);
    }

    // The same ring as the item death effects (golden apple etc.), but of red and blue present heads
    private static PassDeathEffect giftExplosion(final Core main) {
        return new PassDeathEffect(main, "gift_explosion", "Gift Explosion", Material.CHEST,
                "&7A ring of presents pops out", "&7where you were eliminated!") {
            // The menu icon is a present head too, the same as the ones in the ring
            @Override
            protected ItemStack baseIcon() {
                return PresentHeads.red();
            }

            public void play(Location where) {
                ItemStack[] presents = new ItemStack[10];
                for (int i = 0; i < presents.length; i++)
                    presents[i] = i % 2 == 0 ? PresentHeads.red() : PresentHeads.blue();
                dropItemRing(main, where, presents);
            }
        };
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
