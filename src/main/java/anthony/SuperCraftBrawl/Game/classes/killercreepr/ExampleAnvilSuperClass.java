package anthony.SuperCraftBrawl.Game.classes.killercreepr;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.ItemHelper;
import com.avaje.ebean.validation.NotNull;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class ExampleAnvilSuperClass extends SuperClass{
    public ExampleAnvilSuperClass() {
        super("anvil", 100);
    }

    @Override
    public ClassInstance buildInstance(GameInstance gameInstance, Player p) {
        return super.buildInstance(gameInstance, p);
    }

    @NotNull
    @Override
    public Map<Integer, ItemStack> getEquipment() {
        Map<Integer, ItemStack> map = new HashMap<>();
        map.put(getSlot(EquipmentSlot.HEAD), new ItemStack(Material.IRON_BLOCK));
        map.put(getSlot(EquipmentSlot.CHEST),
                ItemHelper.setColor(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
                        Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.GRAY));
        map.put(getSlot(EquipmentSlot.LEGS),
                ItemHelper.setColor(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4),
                        Color.GRAY));
        ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1);
        ItemMeta meta = sword.getItemMeta();
        meta.spigot().setUnbreakable(true);
        sword.setItemMeta(meta);
        map.put(getSlot(EquipmentSlot.HAND), sword);
        return map;
    }
}
