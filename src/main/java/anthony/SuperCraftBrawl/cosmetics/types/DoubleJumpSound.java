package anthony.SuperCraftBrawl.cosmetics.types;

import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRarities;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class DoubleJumpSound extends Cosmetic {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public DoubleJumpSound(String name, CosmeticRarities rarity, String description, ItemStack displayItem, Sound sound, float volume, float pitch) {
        super(name, rarity, description, displayItem, DoubleJumpSound.class);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playSoundAtPlayerLocation(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

}
