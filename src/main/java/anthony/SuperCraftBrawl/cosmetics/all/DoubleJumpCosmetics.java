package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRegistry;
import anthony.SuperCraftBrawl.cosmetics.DoubleJumpAnimation;
import anthony.SuperCraftBrawl.cosmetics.ParticleHelper;
import anthony.SuperCraftBrawl.cosmetics.PassCosmetic;
import anthony.SuperCraftBrawl.cosmetics.ReindeerLeapSpiral;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Double jump effects. With none equipped the double jump keeps its classic snowball poof.
 * Each effect is a single short burst (no ticking), so nothing is kept between jumps. The exception is the Reindeer
 * Leap, drawn over 13 ticks by one task that stops itself.
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

    /**
     * Rudolph's double helix: a red strand and a gold one, half a turn apart, spiralling down from under the feet and
     * widening like an inverted funnel, with the occasional spark between them and a small gold ring at the bottom
     * (the shape is ReindeerLeapSpiral's). It is drawn a slice at a time rather than all at once, so it is the one effect
     * that isn't a single burst: one task that stops itself after 13 ticks and remembers only the spot the jump started
     * from. The funnel stays under that spot, so it never ends up above or in front of the player. No snow.
     */
    private static PassDoubleJump reindeerLeap(final Core main) {
        return new PassDoubleJump(main, "reindeer_leap", "Reindeer Leap", Material.REDSTONE,
                "&7A red and gold double helix swirls", "&7down beneath you as you leap!") {
            public void play(final Player player) {
                final Location anchor = player.getLocation();
                final Random random = new Random();

                drawReindeerLeap(anchor, 0, random); // the first slice is there at once
                new BukkitRunnable() {
                    int tick = 1;

                    @Override
                    public void run() {
                        if (tick >= ReindeerLeapSpiral.TOTAL_TICKS || !player.isOnline()) {
                            cancel();
                            return;
                        }
                        drawReindeerLeap(anchor, tick, random);
                        tick++;
                    }
                }.runTaskTimer(main, 1L, 1L);
            }
        };
    }

    /** One tick of the Reindeer Leap: a slice of the helix and maybe a spark, then the growing ring. */
    private static void drawReindeerLeap(Location anchor, int tick, Random random) {
        if (tick < ReindeerLeapSpiral.SPIRAL_TICKS) {
            for (double[] dot : ReindeerLeapSpiral.spiral(tick))
                ParticleHelper.dust(below(anchor, dot), (int) dot[3], (int) dot[4], (int) dot[5]);

            // An occasional spark between the strands. Speed 0, so it stays where it is and doesn't drift up
            if (random.nextInt(2) == 0) {
                double[] spark = ReindeerLeapSpiral.spark(tick, random.nextDouble());
                ParticleHelper.play(below(anchor, spark), EnumParticle.FIREWORKS_SPARK, 0f, 0f, 0f, 0f, 1);
            }
        } else {
            for (double[] dot : ReindeerLeapSpiral.ring(tick - ReindeerLeapSpiral.SPIRAL_TICKS))
                ParticleHelper.dust(below(anchor, dot), (int) dot[3], (int) dot[4], (int) dot[5]);
        }
    }

    /** A point of the shape (x, y, z from the feet) in the world. */
    private static Location below(Location anchor, double[] offset) {
        return new Location(anchor.getWorld(), anchor.getX() + offset[0], anchor.getY() + offset[1],
                anchor.getZ() + offset[2]);
    }
}
