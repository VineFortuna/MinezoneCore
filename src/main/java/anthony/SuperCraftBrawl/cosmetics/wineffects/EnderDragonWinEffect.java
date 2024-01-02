package anthony.SuperCraftBrawl.cosmetics.wineffects;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRarities;
import anthony.SuperCraftBrawl.cosmetics.types.WinEffect;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnderDragonWinEffect extends WinEffect {
    public EnderDragonWinEffect(String name, CosmeticRarities rarity, String description, ItemStack displayItem) {
        super(name, rarity, description, displayItem);
    }

    @Override
    public void playWinEffect(Player player, GameInstance gameInstance) {
        // Spawning Ender Dragon
        EnderDragon enderDragon = (EnderDragon) player.getWorld().spawnEntity(player.getLocation(), EntityType.ENDER_DRAGON);
        // Setting player as passenger
        enderDragon.setPassenger(player);
    }
}
