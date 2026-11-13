package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRegistry;
import anthony.SuperCraftBrawl.cosmetics.DoubleJumpAnimation;
import anthony.SuperCraftBrawl.cosmetics.ParticleHelper;
import anthony.SuperCraftBrawl.cosmetics.PassCosmetic;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Double jump effects. With none equipped the double jump keeps its classic snowball poof.
 * Each effect is a single short burst (no ticking), so nothing is kept between jumps.
 */
public final class DoubleJumpCosmetics {

    private DoubleJumpCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        registry.register(snowPuff(main));
        registry.register(snowflakeBurst(main));
        registry.register(reindeerLeap(main));
    }

    /** Base for double jump cosmetics from the Battle Pass: a PassCosmetic that can also be played. */
    private abstract static class PassDoubleJump extends PassCosmetic implements DoubleJumpAnimation {
        PassDoubleJump(Core main, String id, String displayName, Material icon, String... description) {
            super(main, CosmeticCategory.DOUBLE_JUMP, id, displayName, "&e" + displayName, icon, 0, description);
        }
    }

    private static PassDoubleJump snowPuff(Core main) {
        return new PassDoubleJump(main, "snow_puff", "Snow Puff", Material.SNOW_BALL,
                "&7A fluffy puff of snow", "&7bursts under your feet!") {
            public void play(Player player) {
                Location feet = player.getLocation();
                ParticleHelper.play(feet, EnumParticle.CLOUD, 0.25f, 0.05f, 0.25f, 0.02f, 12);
                ParticleHelper.play(feet, EnumParticle.SNOW_SHOVEL, 0.3f, 0.1f, 0.3f, 0.08f, 18);
            }
        };
    }

    private static PassDoubleJump snowflakeBurst(Core main) {
        return new PassDoubleJump(main, "snowflake_burst", "Snowflake Burst", Material.SNOW_BLOCK,
                "&7A ring of snowflakes and sparkles", "&7explodes out as you jump!") {
            public void play(Player player) {
                Location feet = player.getLocation().add(0, 0.2, 0);

                // Expanding ring of snowflakes
                int points = 14;
                for (int i = 0; i < points; i++) {
                    double angle = i * 2 * Math.PI / points;
                    Location point = feet.clone().add(Math.cos(angle) * 1.1, 0, Math.sin(angle) * 1.1);
                    ParticleHelper.play(point, EnumParticle.SNOW_SHOVEL, 0f, 0.05f, 0f, 0.03f, 2);
                }

                ParticleHelper.play(feet, EnumParticle.FIREWORKS_SPARK, 0.1f, 0.1f, 0.1f, 0.18f, 14);
                ParticleHelper.play(feet, EnumParticle.CLOUD, 0.2f, 0.05f, 0.2f, 0.03f, 8);
            }
        };
    }

    private static PassDoubleJump reindeerLeap(Core main) {
        return new PassDoubleJump(main, "reindeer_leap", "Reindeer Leap", Material.REDSTONE,
                "&7A twirl of red and green", "&7sparkles follows your leap!") {
            public void play(Player player) {
                Location feet = player.getLocation();

                // Red and green spiral rising around the player
                int steps = 14;
                for (int i = 0; i < steps; i++) {
                    double angle = i * 0.65;
                    Location point = feet.clone().add(Math.cos(angle) * 0.8, i * 0.13, Math.sin(angle) * 0.8);
                    if (i % 2 == 0)
                        ParticleHelper.dust(point, 220, 30, 30);
                    else
                        ParticleHelper.dust(point, 30, 160, 50);
                }

                ParticleHelper.play(feet.clone().add(0, 0.3, 0), EnumParticle.VILLAGER_HAPPY, 0.4f, 0.3f, 0.4f, 0f, 5);
            }
        };
    }
}
