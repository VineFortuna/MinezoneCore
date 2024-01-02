package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Game.abilities.AbilityOld;
import anthony.SuperCraftBrawl.Game.classes.all.EnchantTableClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class EnchantmentTableGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;
    public EnchantTableClass enchantTableClass;

    // Creating Cosmetics Inventory
    public EnchantmentTableGUI(Core main, EnchantTableClass enchantTableClass) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(1, 9)
                .title(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "Enchant Sword")
                .build();
        this.main = main;
        this.enchantTableClass = enchantTableClass;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getPlayerDataManager().getPlayerData(player);

        ItemStack weapon = enchantTableClass.getAttackWeapon();
        AbilityOld enchantAbility = enchantTableClass.getEnchantAbility();
        int xpLevelsAmount = enchantTableClass.getXpLevelsAmount();
        int levelEnchanted = enchantTableClass.getLevelEnchanted();

        // Icon Items
            // Sharpness 1
        ItemStack sharpness1 = ItemHelper.setDetails(new ItemStack(Material.DIAMOND), ChatColor.YELLOW + "Sharpness 1");

            // Knockback 1

        ItemStack knockback2 = ItemHelper.setDetails(new ItemStack(Material.DIAMOND), ChatColor.YELLOW + "Knockback 2");

            // Fire Aspect 1
        ItemStack lobby = ItemHelper.create(Material.BLAZE_POWDER, ChatColor.YELLOW + "Fire Aspect 1");

            // Game Cosmetics
        ItemStack ingame = ItemHelper.create(Material.ENDER_PORTAL_FRAME, ChatColor.YELLOW + "Game Cosmetics");

        // Setting Items
        contents.set(1, 1, ClickableItem.of(
                sharpness1,
                e -> {
//                    if (xpLevelsAmount < 1) {
//
//                    }
//
//                    // Adding Sharpness 1
//                    weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
//                    enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &c&lSharpness 1");
//
//                    xpSpent = 1;
//                    xpLevelsAmount -= 1;
//                    levelEnchanted = 1;

                    inv.close(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }
}

