package anthony.SuperCraftBrawl.cosmetics.wineffects;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRarities;
import anthony.SuperCraftBrawl.cosmetics.types.WinEffect;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SantaWinEffect extends WinEffect {
    public SantaWinEffect(String name, CosmeticRarities rarity, String description, ItemStack displayItem) {
        super(name, rarity, description, displayItem);
    }

    @Override
    public void playWinEffect(Player player, GameInstance gameInstance) {
        // Creating Armor items
        ItemStack santaHead = ItemHelper.createSkullHeadPlayer(1, "Santa", "&c&lSanta Outfit");
        ItemStack santaChestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.RED, "&c&lSanta Outfit");
        ItemStack santaLeggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.RED, "&c&lSanta Outfit");
        ItemStack santaBoots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.RED, "&c&lSanta Outfit");

        // Equipping Armor
        player.getInventory().setHelmet(santaHead);
        player.getInventory().setChestplate(santaChestplate);
        player.getInventory().setLeggings(santaLeggings);
        player.getInventory().setBoots(santaBoots);

        // Spawning Horse (Reindeer)
        final Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
        horse.setTamed(true);
        horse.setOwner(player);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        player.teleport(horse.getLocation());

        // Setting player as passenger
        horse.setPassenger(player);
    }
}
