package anthony.SuperCraftBrawl.cosmetics.wineffects;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRarities;
import anthony.SuperCraftBrawl.cosmetics.types.WinEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MagicBroomWinEffect extends WinEffect {
    public MagicBroomWinEffect(String name, CosmeticRarities rarity, String description, ItemStack displayItem) {
        super(name, rarity, description, displayItem);
    }

    @Override
    public void playWinEffect(Player player, GameInstance gameInstance) {
        // Creating Broom ItemStack
        ItemStack broom = ItemHelper.setDetails(new ItemStack(Material.WHEAT), ChatColorHelper.color("&2&lMagic Broom"));
        // Setting Broom on player inventory
        player.getInventory().setItem(0, broom);
        // Setting selected slot to the Broom's
        player.getInventory().setHeldItemSlot(0);
    }
}
