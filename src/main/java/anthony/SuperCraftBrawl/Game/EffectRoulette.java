package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * The Effect Roulette modifier: every few seconds each living player gets one random effect, picked for them
 * alone, that lasts until the next roll. A player is never given an effect they already have, Jump Boost is
 * left out while Low Gravity is on (it already gives it) and Strength while Strength on Kill is on.
 * Absorption gets stronger the longer the roll lasts.
 */
public final class EffectRoulette {

    /** Effects with this much time left or less count as already gone (the last roll's own effect is ending). */
    private static final int ENDING_TICKS = 10;
    private static final Random RANDOM = new Random();

    private enum RouletteEffect {
        STRENGTH(PotionEffectType.INCREASE_DAMAGE, 0),
        REGENERATION(PotionEffectType.REGENERATION, 1),
        RESISTANCE(PotionEffectType.DAMAGE_RESISTANCE, 0),
        SPEED(PotionEffectType.SPEED, 1),
        JUMP_BOOST(PotionEffectType.JUMP, 7), // Jump Boost VIII
        ABSORPTION(PotionEffectType.ABSORPTION, 0); // its level depends on the interval, see absorptionAmplifier

        final PotionEffectType type;
        final int amplifier;

        RouletteEffect(PotionEffectType type, int amplifier) {
            this.type = type;
            this.amplifier = amplifier;
        }
    }

    private EffectRoulette() {
    }

    /** Gives every living player in the game a new random effect that lasts as long as the roulette's interval. */
    public static void roll(GameInstance game) {
        GameSettings settings = game.getGameSettings();
        int seconds = settings.effectRouletteSeconds;
        if (seconds <= 0) return;

        for (Player player : new ArrayList<>(game.players)) {
            BaseClass baseClass = game.classes.get(player);
            if (baseClass == null || baseClass.getLives() <= 0 || baseClass.isDead
                    || player.getGameMode() == GameMode.SPECTATOR) continue;

            List<RouletteEffect> options = new ArrayList<>();
            for (RouletteEffect effect : RouletteEffect.values()) {
                if (effect == RouletteEffect.JUMP_BOOST && settings.lowGravity) continue;
                if (effect == RouletteEffect.STRENGTH && settings.strengthOnKillSeconds > 0) continue;
                if (hasEffect(player, effect.type)) continue;
                options.add(effect);
            }
            if (options.isEmpty()) continue;

            RouletteEffect picked = options.get(RANDOM.nextInt(options.size()));
            int amplifier = picked == RouletteEffect.ABSORPTION ? absorptionAmplifier(seconds) : picked.amplifier;
            player.addPotionEffect(new PotionEffect(picked.type, seconds * 20, amplifier), true);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.6f, 1.4f);
        }
    }

    /** Absorption I every 5s, II every 15s and III every 30s: the longer the roll lasts, the stronger it is. */
    private static int absorptionAmplifier(int seconds) {
        if (seconds <= 5) return 0;
        if (seconds <= 15) return 1;
        return 2;
    }

    private static boolean hasEffect(Player player, PotionEffectType type) {
        for (PotionEffect active : player.getActivePotionEffects()) {
            if (active.getType().equals(type) && active.getDuration() > ENDING_TICKS) return true;
        }
        return false;
    }
}
