package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.cosmetics.doublejumpsounds.ChickenEggDoubleJumpSound;
import anthony.SuperCraftBrawl.cosmetics.types.KillEffect;
import anthony.SuperCraftBrawl.cosmetics.wineffects.DefaultFireworkWinEffect;
import anthony.SuperCraftBrawl.cosmetics.wineffects.EnderDragonWinEffect;
import anthony.SuperCraftBrawl.cosmetics.wineffects.MagicBroomWinEffect;
import anthony.SuperCraftBrawl.cosmetics.wineffects.SantaWinEffect;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CosmeticManager {
    private List<Cosmetic> cosmetics;

    public CosmeticManager() {
        this.cosmetics = new ArrayList<>();

        initializeCosmetics();  // Initializing cosmetics
    }

    private void initializeCosmetics() {
        // Kill Effects
        createKillEffects();
        // Win Effects
        createWinEffects();
        // Death Effects
        createDoubleJumpSounds();
        // Kill Messages
        // Win Messages
        // Gadgets
        // Arrow Effects
        // Double Jump Sound
    }

    public void addCosmetic(Cosmetic cosmetic) {
        cosmetics.add(cosmetic);
    }

    public void removeCosmetic(Cosmetic cosmetic) {
        cosmetics.remove(cosmetic);
    }

    public List<Cosmetic> getAllCosmetics() {
        return new ArrayList<>(cosmetics); // Return a copy to prevent external modifications
    }

    public List<Cosmetic> getCosmeticsByRarity(CosmeticRarities rarity) {
        List<Cosmetic> filteredCosmetics = new ArrayList<>();
        for (Cosmetic cosmetic : cosmetics) {
            if (cosmetic.getRarity() == rarity) {
                filteredCosmetics.add(cosmetic);
            }
        }
        return filteredCosmetics;
    }

    public List<Cosmetic> getCosmeticsByType(Class<? extends Cosmetic> cosmeticType) {
        List<Cosmetic> filteredCosmetics = new ArrayList<>();
        for (Cosmetic cosmetic : cosmetics) {
            if (cosmetic.getCosmeticType().equals(cosmeticType)) {
                filteredCosmetics.add(cosmetic);
            }
        }
        return filteredCosmetics;
    }

    public Cosmetic getCosmeticByName(String name) {
        return cosmetics.stream()
                .filter(cosmetic -> cosmetic.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    // Kill Effects
    private void createKillEffects() {
        KillEffect blueKillEffect = KillEffect.createColorableKillEffect(
                "Blue Kill Effect",
                CosmeticRarities.COMMON,
                "Blue particles on kill",
                ItemHelper.createDye(DyeColor.BLUE, 1),
                ParticleEffect.REDSTONE,
                Color.blue
        );

        KillEffect magentaKillEffect = KillEffect.createColorableKillEffect(
                "Magenta Kill Effect",
                CosmeticRarities.LEGENDARY,
                "Magenta particles on kill",
                ItemHelper.createDye(DyeColor.MAGENTA, 1),
                ParticleEffect.REDSTONE,
                Color.MAGENTA
        );

        // Adding to the cosmetics list
        cosmetics.add(blueKillEffect);
        cosmetics.add(magentaKillEffect);
    }

    // Win Effects
    private void createWinEffects() {
        DefaultFireworkWinEffect defaultFireworkWinEffect = new DefaultFireworkWinEffect(
                "Fireworks",
                CosmeticRarities.DEFAULT,
                "Fireworks up in the sky",
                new ItemStack(Material.FIREWORK)
        );

        EnderDragonWinEffect enderDragonRide = new EnderDragonWinEffect(
                "Ender Dragon Ride",
                CosmeticRarities.LEGENDARY,
                "Go on a ride with the Ender Dragon",
                new ItemStack(Material.EYE_OF_ENDER)
        );

        MagicBroomWinEffect magicBroomWinEffect = new MagicBroomWinEffect(
                "Magic Broom",
                CosmeticRarities.RARE,
                "Fly around like a Witch",
                new ItemStack(Material.WHEAT)
        );

        SantaWinEffect santaWinEffect = new SantaWinEffect(
                "Santa's",
                CosmeticRarities.COMMON,
                "Ho-ho-ho, merry christmas!",
                ItemHelper.createSkullHeadPlayer(1, "Santa")
        );

        // Adding to the cosmetics list
        cosmetics.add(defaultFireworkWinEffect);
        cosmetics.add(enderDragonRide);
        cosmetics.add(magicBroomWinEffect);
        cosmetics.add(santaWinEffect);
    }

    // Double Jump Sounds
    private void createDoubleJumpSounds() {
        ChickenEggDoubleJumpSound chickenEggDoubleJumpSound = new ChickenEggDoubleJumpSound(
                "Chicken Egg Pop",
                CosmeticRarities.DEFAULT,
                "Sounds like a chicken egg popping",
                new ItemStack(Material.EGG)
        );

        // Adding to the cosmetics list
        cosmetics.add(chickenEggDoubleJumpSound);
    }
}
