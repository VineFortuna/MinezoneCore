package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class FishermanClass extends BaseClass {
    
    private int cooldownSec;
    
    private ItemStack flyingFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH),
            "&a&lFlying Fish",
            "&7Launch at enemies to fishslap them");
    
    private ItemStack pufferFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH, 1, (short) 3),
            "&4&lTactical Fish",
            "&7Explodes when approached by a player, inflicting Poison for 3 seconds",
            "&7Lifespan of 45 seconds");
    
    private ItemStack speedFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH, 1, (short) 2),
            "&a&lSpeedy Fish",
            "&7Swim away with Speed for 5 seconds");
    
    private ItemStack healFish = ItemHelper.setDetails(new ItemStack(Material.COOKED_FISH, 1, (short) 1),
            "&a&lHealing Fish",
            "&7Eat it to gain 1 heart");
    
    public FishermanClass(GameInstance instance, Player player) {
        super(instance, player);
    }
    
    @Override
    public ClassType getType() {
        return ClassType.Fisherman;
    }
    
    public ItemStack makeBrown(ItemStack armour) {
        LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
        lm.setColor(Color.GREEN);
        armour.setItemMeta(lm);
        return armour;
    }
    
    @Override
    public void SetArmour(EntityEquipment playerEquip) {
        String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcxYjhiMmFlN2ZiMjc4MmRiZWU5M2E3ZTY3OTc4M2M1MGQ1YTg4NDA0NTcwOGEyMTU5NDE3ODVkN2MzY2NkIn19fQ==";
        ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
        
        playerEquip.setHelmet(playerskull);
        playerEquip.setChestplate(makeBrown(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
                Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
        playerEquip.setLeggings(makeBrown(new ItemStack(Material.LEATHER_LEGGINGS)));
        playerEquip.setBoots(makeBrown(
                ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
    }
    
    @Override
    public ItemStack getAttackWeapon() {
        return ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.FISHING_ROD), Enchantment.DAMAGE_ALL, 3),
                Enchantment.KNOCKBACK, 1);
    }
    
    @Override
    public void SetNameTag() {
    
    }
    
    @Override
    public void SetItems(Inventory playerInv) {
        villager.startTime = System.currentTimeMillis() - 100000;
        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1,
                ItemHelper.setDetails(new ItemStack(Material.BUCKET, 1), "", "",
                        instance.getGameManager().getMain().color("&7Collect 5 Fish to use")));
        playerInv.setItem(2, instance.getItemToDrop());
    }
    
    @Override
    public void Tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Villager
                && instance.classes.get(player).getLives() > 0) {
            this.cooldownSec = (5000 - villager.getTime()) / 1000 + 1;
            
            if (villager.getTime() < 5000) {
                String msg = instance.getGameManager().getMain()
                        .color("&2Baked Potato &rregenerates in: &e" + this.cooldownSec + "s");
                getActionBarManager().setActionBar(player, "potato.cooldown", msg, 2);
            } else {
                String msg = instance.getGameManager().getMain().color("&rYou can use &2Baked Potato");
                getActionBarManager().setActionBar(player, "potato.cooldown", msg, 2);
            }
        }
    }
    
    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && event.getAction().toString().contains("RIGHT_CLICK")) {
            int amount = item.getAmount();
            if (item.getType() == Material.BUCKET) {
                player.sendMessage("Collect more fish");
            } else if (item.getType() == Material.WATER_BUCKET) {
                Random r = new Random();
                for (int i = 1; i <= 5; i++) {
                    int chance = r.nextInt(4) + 1;
                    if (chance == 1)
                        player.getInventory().addItem(flyingFish);
                    else if (chance == 2)
                        player.getInventory().addItem(speedFish);
                    else if (chance == 3)
                        player.getInventory().addItem(pufferFish);
                    else
                        player.getInventory().addItem(healFish);
                }
            } else if (item.isSimilar(healFish)) {
                player.setHealth(player.getHealth() + 2);
                player.getInventory().remove(healFish);
            } else if (item.isSimilar(speedFish)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
                    player.getInventory().remove(speedFish);
        
            } else if (item.isSimilar(flyingFish)) {
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
                        @Override
                        public void onHit(Player hit) {
                            if (instance.duosMap != null)
                                if (instance.team.get(hit).equals(instance.team.get(player)))
                                    return;
            
                            player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                            hit.damage(2.0, player);
                            for (Player gamePlayer : instance.players)
                                gamePlayer.playSound(hit.getLocation(), Sound.SPLASH, 2, 1);
            
                        }
        
                    }, new ItemStack(Material.RAW_FISH));
                    instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                            player.getLocation().getDirection().multiply(2.0D));
                }
                event.setCancelled(true);
            } else if (item.isSimilar(pufferFish)) {
                ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
                    @Override
                    public void onHit(Player hit) {
                        Location hitLoc = this.getBaseProj().getEntity().getLocation();
                        player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
                       player.getWorld().dropItem(hitLoc, pufferFish);
                    }
        
                }, pufferFish);
                instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                        player.getLocation().getDirection().multiply(2.0D));
            }
        }
    }
    
}
