package anthony.SuperCraftBrawl.cosmetics.doublejumpsounds;

import anthony.SuperCraftBrawl.cosmetics.CosmeticRarities;
import anthony.SuperCraftBrawl.cosmetics.types.DoubleJumpSound;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChickenEggDoubleJumpSound extends DoubleJumpSound {
    public ChickenEggDoubleJumpSound(String name, CosmeticRarities rarity, String description, ItemStack displayItem) {
        super(name, rarity, description, displayItem, Sound.CHICKEN_EGG_POP, 1, 1);
    }

    @Override
    public void playSoundAtPlayerLocation(Player player) {
        super.playSoundAtPlayerLocation(player);
    }
}
