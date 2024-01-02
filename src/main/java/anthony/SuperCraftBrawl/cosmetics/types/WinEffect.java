package anthony.SuperCraftBrawl.cosmetics.types;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRarities;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class WinEffect extends Cosmetic {
    public WinEffect(String name, CosmeticRarities rarity, String description, ItemStack displayItem) {
        super(name, rarity, description, displayItem, WinEffect.class);
    }

    public abstract void playWinEffect(Player player, GameInstance gameInstance);
}
