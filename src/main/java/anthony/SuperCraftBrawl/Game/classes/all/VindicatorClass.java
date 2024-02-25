package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameManager;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class VindicatorClass extends BaseClass {
    
    private int cooldownSec;
    
    public VindicatorClass(GameInstance instance, Player player) {
        super(instance, player);
        baseVerticalJump = 0.9;
    }
    
    public ItemStack makePink(ItemStack armour) {
        LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
        lm.setColor(Color.BLACK);
        armour.setItemMeta(lm);
        return armour;
    }
    
    @Override
    public void SetArmour(EntityEquipment playerEquip) {
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFlZWQ5ZDhlZDE3NjllNzdlM2NmZTExZGMxNzk2NjhlZDBkYjFkZTZjZTI5ZjFjOGUwZDVmZTVlNjU3M2I2MCJ9fX0=";
        ItemStack skull = ItemHelper.createSkullTexture(texture, "");
        
        playerEquip.setHelmet(skull);
        playerEquip.setChestplate(makePink(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
                Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
        playerEquip.setLeggings(makePink(new ItemStack(Material.LEATHER_LEGGINGS)));
        playerEquip.setBoots(makePink(
                ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
    }
    
    @Override
    public void SetItems(Inventory playerInv) {
        vindication.startTime = System.currentTimeMillis() - 100000;
        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.EMERALD),
                "" + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "Vindication"));
    }
    
    @Override
    public void Tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Vindicator
                && instance.classes.get(player).getLives() > 0) {
            this.cooldownSec = (30000 - vindication.getTime()) / 1000 + 1;
        
            if (vindication.getTime() < 30000) {
                String msg = instance.getGameManager().getMain()
                        .color("&c&lVindication &rregenerates in: &e" + cooldownSec + "s");
                getActionBarManager().setActionBar(player, "vindication.cooldown", msg, 2);
            } else {
                String msg = instance.getGameManager().getMain().color("&rYou can use &c&lVindication");
                getActionBarManager().setActionBar(player, "vindication.cooldown", msg, 2);
            }
        }
        if (gameTicks % 5 == 0) {
            if (!player.hasPotionEffect(PotionEffectType.SPEED) && player.getGameMode() != GameMode.SPECTATOR &&
                    !player.getNearbyEntities(8, 8, 8).isEmpty()) {
                for (Entity nearby : player.getNearbyEntities(8, 8, 8)) {
                    if (nearby instanceof Player && instance.players.contains(nearby)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0));
                    }
                }
            }
        }
    }
    
    @Override
    public void DoDamage(EntityDamageByEntityEvent event) {
        BaseClass bc = instance.classes.get(player);
        if (bc != null && bc.getLives() <= 0)
            return;
        if (player.hasPotionEffect(PotionEffectType.SPEED) && player.getGameMode() != GameMode.SPECTATOR) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().equals(PotionEffectType.SPEED) && effect.getAmplifier() == 2) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
                    player.sendMessage(instance.getGameManager().getMain().color("&e&l(!) &rYou lost your speed and strength"));
                }
            }
        }
    }
    
    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        
        if (item != null && item.getType() == Material.EMERALD
                && event.getAction().toString().contains("RIGHT_CLICK")) {
            if (vindication.getTime() < 30000) {
                int seconds = (30000 - vindication.getTime()) / 1000 + 1;
                event.setCancelled(true);
                player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                        + "Vindication is still regenerating for " + ChatColor.YELLOW + seconds + " more seconds ");
            } else {
                vindication.restart();
                if (player.hasPotionEffect(PotionEffectType.SPEED))
                    player.removePotionEffect(PotionEffectType.SPEED);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                player.playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 1, 1);
                player.sendMessage(instance.getGameManager().getMain().color("&e&l(!) &rYou gained a quick burst of speed and strength"));
            }
        }
    }
    
    @Override
    public ClassType getType() {
        return ClassType.Vindicator;
    }
    
    @Override
    public void SetNameTag() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public ItemStack getAttackWeapon() {
        ItemStack item = ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.IRON_AXE),
                instance.getGameManager().getMain().color("&c&lVindicating Axe")), Enchantment.KNOCKBACK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.spigot().setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
}
