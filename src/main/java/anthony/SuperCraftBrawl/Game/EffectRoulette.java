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
 * The Effect Roulette modifier: every few seconds each living player gets one random positive effect, picked for
 * them alone, that lasts until the next roll. A player is never given an effect they already have, Jump Boost is
 * left out while Low Gravity is on (it already gives it) and Strength while Strength on Kill is on.
 * Absorption gets stronger the longer the roll lasts. A player who respawns gets the effect they had before dying
 * back, or the one a roll gave them while they were respawning, lasting only until the next roll.
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

        static RouletteEffect of(PotionEffectType type) {
            for (RouletteEffect effect : values()) {
                if (effect.type.equals(type)) return effect;
            }
            return null;
        }
    }

    private EffectRoulette() {
    }

    /** Gives every living player in the game a new random effect that lasts as long as the roulette's interval. */
    public static void roll(GameInstance game) {
        int seconds = game.getGameSettings().effectRouletteSeconds;
        if (seconds <= 0) return;

        for (Player player : new ArrayList<>(game.players)) {
            BaseClass baseClass = game.classes.get(player);
            if (baseClass == null || baseClass.getLives() <= 0) continue;

            if (baseClass.isDead || player.getGameMode() == GameMode.SPECTATOR) {
                // Respawning: the roll still changes their effect, they get it when they're back
                RouletteEffect picked = pick(game, null);
                if (picked != null) game.rouletteEffects.put(player.getUniqueId(), picked.type);
            } else {
                give(game, player, pick(game, player), seconds * 20);
            }
        }
    }

    /**
     * A player who just respawned gets the effect they had before dying back (or the one the roll changed it to
     * while they were respawning), lasting only until the next roll.
     */
    public static void giveOnRespawn(GameInstance game, Player player) {
        int interval = game.getGameSettings().effectRouletteSeconds * 20;
        if (interval <= 0 || game.state != GameState.STARTED || game.gameTicks <= 0) return;

        int elapsed = game.gameTicks % interval;
        if (elapsed == 0) return; // the next tick is a roll, which covers them

        int remaining = interval - elapsed;
        if (remaining <= ENDING_TICKS) return; // it would be gone right away

        PotionEffectType last = game.rouletteEffects.get(player.getUniqueId());
        if (last != null && hasEffect(player, last)) return; // the one they were given is still running

        RouletteEffect effect = last == null ? null : RouletteEffect.of(last);
        if (effect == null || !isAllowed(game, effect)) effect = pick(game, player); // never had one yet
        give(game, player, effect, remaining);
    }

    /** Whether the game's modifiers let the roulette hand this effect out. */
    private static boolean isAllowed(GameInstance game, RouletteEffect effect) {
        GameSettings settings = game.getGameSettings();
        if (effect == RouletteEffect.JUMP_BOOST && settings.lowGravity) return false;
        return !(effect == RouletteEffect.STRENGTH && settings.strengthOnKillSeconds > 0);
    }

    /** A random effect the roulette can give (one the player doesn't have yet, when there is a player). */
    private static RouletteEffect pick(GameInstance game, Player player) {
        List<RouletteEffect> options = new ArrayList<>();
        for (RouletteEffect effect : RouletteEffect.values()) {
            if (!isAllowed(game, effect)) continue;
            if (player != null && hasEffect(player, effect.type)) continue;
            options.add(effect);
        }
        return options.isEmpty() ? null : options.get(RANDOM.nextInt(options.size()));
    }

    /** Gives the effect for the given time, if this player can get one. */
    private static void give(GameInstance game, Player player, RouletteEffect effect, int durationTicks) {
        BaseClass baseClass = game.classes.get(player);
        if (effect == null || baseClass == null || baseClass.getLives() <= 0 || baseClass.isDead
                || player.getGameMode() == GameMode.SPECTATOR) return;

        int amplifier = effect == RouletteEffect.ABSORPTION
                ? absorptionAmplifier(game.getGameSettings().effectRouletteSeconds) : effect.amplifier;
        player.addPotionEffect(new PotionEffect(effect.type, durationTicks, amplifier), true);
        game.rouletteEffects.put(player.getUniqueId(), effect.type);
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.6f, 1.4f);
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
