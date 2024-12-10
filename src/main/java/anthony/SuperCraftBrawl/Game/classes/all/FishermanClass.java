package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class FishermanClass extends BaseClass {
    
    private int cooldownSec = 0, cooldownDuration = 8000;
    private int hits;
    private ArrayList<Item> puffer = new ArrayList<>();
    
    private ItemStack flyingFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH),
            "&a&lFlying Fish",
            "&7Launch at enemies to fishslap them");
    
    private ItemStack pufferFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH, 1, (short) 3),
            "&e&lTactical Fish",
            "&7Explodes when approached by a player",
            "&7Inflicts Poison for 4 seconds",
            "&7Detonates after 15 seconds");
    
    private ItemStack speedFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH, 1, (short) 2),
            "&b&lTricky Fish",
            "&7Swim away with Speed for 5 seconds");
    
    private ItemStack healFish = ItemHelper.setDetails(new ItemStack(Material.COOKED_FISH, 1, (short) 1),
            "&d&lHealing Fish",
            "&7Eat to gain 1 heart");
    
    private ItemStack bucket = ItemHelper.setDetails(new ItemStack(Material.BUCKET, 4), "&rBucket");
    
    private ItemStack waterbucket = ItemHelper.setDetails(new ItemStack(Material.WATER_BUCKET, 1), "&3Fish Bucket &7(Right Click)");
    
    public FishermanClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWY1ZDM4MTlhNjVkYjc5YzQ1ZmQwMDE0MWMwODgyZTQ3YWQyMzRjMGU1Zjg5OTJiZjRhZjE4Y2VkMGUxZWNkYyJ9fX0=",
                "6E504B",
                "8F4020",
                "452518",
                6,
                "Fisherman"
        );

        playerHead.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 1);
    }
    
    @Override
    public ClassType getType() {
        return ClassType.Fisherman;
    }
    
    @Override
    public void setArmor(EntityEquipment playerEquip) {
        setArmorNew(playerEquip);
    }
    
    @Override
    public ItemStack getAttackWeapon() {
        return ItemHelper.setUnbreakable(ItemHelper.addEnchant(
                ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD),
                        instance.getGameManager().getMain().color("&bFishing Rod"),
                                instance.getGameManager().getMain().color("&7Hook onto" +
                                        " a block to grapple towards it")), Enchantment.DAMAGE_ALL, 3),
                Enchantment.KNOCKBACK, 1));
    }
    
    @Override
    public void SetNameTag() {
    
    }
    
    @Override
    public void onFish(PlayerFishEvent event) {
        if (fishing.getTime() < this.cooldownDuration) {
            int seconds = (this.cooldownDuration - fishing.getTime()) / 1000 + 1;
            player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                    + "Your Grappling Hook is still regenerating for " + ChatColor.YELLOW + seconds
                    + " more seconds ");
            event.setCancelled(true);
        } else {
            if (event.getState() == PlayerFishEvent.State.FAILED_ATTEMPT ||
                    event.getState() == PlayerFishEvent.State.IN_GROUND) {
                FishHook hook = event.getHook();
                Block block = hook.getLocation().getBlock();
                boolean grapple = false;
                if (block.getType() != Material.AIR) {
                    grapple = true;
                } else {
                    for (BlockFace face : BlockFace.values()) {
                        Block adjacentBlock = block.getRelative(face);
                        if (adjacentBlock.getType() != Material.AIR) {
                            grapple = true;
                            break;
                        }
                    }
                }
                if (grapple) {
                    Location d = event.getHook().getLocation();
                    Vector v = d.toVector().subtract(player.getLocation().toVector()).normalize();
                    player.setVelocity(v.multiply(2).add(new Vector(0, 0.75, 0)));
                    player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 10);
                    fishing.restart();
                }
            }
        }
    }
    
    @Override
    public void SetItems(Inventory playerInv) {
        hits = 0;
        fishing.startTime = System.currentTimeMillis() - 100000;
        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1, bucket);
    }
    
    @Override
    public void Tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Fisherman
                && instance.classes.get(player).getLives() > 0) {
            this.cooldownSec = (cooldownDuration - fishing.getTime()) / 1000 + 1;
            
            if (gameTicks % 10 == 0) {
                Iterator<Item> it= puffer.iterator();
                while (it.hasNext()) {
                    Item fish = it.next();
                    boolean nearby = false;
                    Location loc = fish.getLocation();
                    for (Player p : instance.players) {
                        if (!checkIfDead(p, instance) &&
                                p != player && p.getLocation().distance(fish.getLocation()) <= 2) {
                            nearby = true;
                            EntityDamageEvent damageEvent = new EntityDamageEvent(p,
                                    EntityDamageEvent.DamageCause.VOID, 4);
                            instance.getGameManager().getMain().getServer().getPluginManager()
                                    .callEvent(damageEvent);
                            p.damage(4, player);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 4 * 20, 0));
                        }
                    }
                    if (nearby) {
                        player.getWorld().playSound(loc, Sound.EXPLODE, 1, 1);
                        player.getWorld().playEffect(loc, Effect.EXPLOSION_LARGE, 1);
                        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                        fish.remove();
                        puffer.remove(fish);
                    }
                }
            }
            cooldownActionBar(this.cooldownSec, this.cooldownDuration, fishing, ClassType.Fisherman,
                    "fisherman.cooldown", "Grappling Hook");
        }
    }
    
    @Override
    public void DoDamage(EntityDamageByEntityEvent event) {
        BaseClass bc = instance.classes.get(player);
        if (bc != null && bc.getLives() <= 0)
            return;
        
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (instance.duosMap != null)
                if (instance.team.get(p).equals(instance.team.get(player)))
                    return;
            if (hits < 4) {
                hits++;
                player.getInventory().getItem(1).setAmount(4 - hits);
                if (hits == 4) {
                    player.sendMessage(instance.getGameManager().getMain()
                            .color("&2&l(!) &rYour bucket is full of fish. Bring out the whole ocean!"));
                    player.getInventory().setItem(1, waterbucket);
                    player.playSound(player.getLocation(), Sound.WATER, 1, 1);
                }
            }
        }
    }
    
    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        boolean remove = false;
        if (item != null && event.getAction().toString().contains("RIGHT_CLICK")) {
            if (item.isSimilar(bucket)) {
                player.sendMessage(instance.getGameManager().getMain()
                        .color("&c&l(!) &rCollect " + (4 - hits) + " more fish!"));
            } else if (item.isSimilar(waterbucket)) {
                Random r = new Random();
                for (int i = 1; i <= 4; i++) {
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
                player.getInventory().setItem(1, bucket);
                player.playSound(player.getLocation(), Sound.SPLASH2, 1, 1);
                hits = 0;
            } else if (item.isSimilar(healFish)) {
                double heal = Math.min(2, player.getMaxHealth() - player.getHealth());
                if (heal > 0) {
                    player.setHealth(player.getHealth() + heal);
                    player.playSound(player.getLocation(), Sound.EAT, 1, 1);
                    remove = true;
                } else {
                    player.sendMessage(instance.getGameManager().getMain()
                            .color("&c&l(!) &rYou are already full of health!"));
                }
            } else if (item.isSimilar(speedFish)) {
                remove = true;
                int duration = 0;
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    if (effect.getType().equals(PotionEffectType.SPEED)) {
                        duration = effect.getDuration();
                        player.removePotionEffect(PotionEffectType.SPEED);
                        break;
                    }
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20 + duration, 0));
                player.playSound(player.getLocation(), Sound.EAT, 1, 1);
            } else if (item.isSimilar(flyingFish)) {
                remove = true;
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
                        @Override
                        public void onHit(Player hit) {
                            if (instance.duosMap != null)
                                if (instance.team.get(hit).equals(instance.team.get(player)))
                                    return;
                
                            player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                
                            Vector v = player.getLocation().getDirection();
                            EntityDamageEvent damageEvent = new EntityDamageEvent(hit, EntityDamageEvent.DamageCause.PROJECTILE, 4.5);
                            instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
                            hit.damage(2, player);
                            v.setY(0.7);
                            hit.setVelocity(v);
                
                            for (Player gamePlayer : instance.players)
                                gamePlayer.playSound(hit.getLocation(), Sound.SPLASH, 1, 1);
                
                        }
            
                    }, new ItemStack(Material.RAW_FISH));
                    instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                            player.getLocation().getDirection().multiply(2.0D));
                }
            } else if (item.isSimilar(pufferFish)) {
                remove = true;
                ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
                    @Override
                    public void onHit(Player hit) {
                        Location hitLoc = this.getBaseProj().getEntity().getLocation();
                        player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
                        Item fish = player.getWorld().dropItem(hitLoc, pufferFish);
                        fish.setPickupDelay(Integer.MAX_VALUE);
                        BukkitRunnable r = new BukkitRunnable() {
                            @Override
                            public void run() {
                                puffer.add(fish);
                                player.getWorld().playEffect(fish.getLocation().add(0, 0.5, 0), Effect.VILLAGER_THUNDERCLOUD, 1);
                                player.getWorld().playSound(fish.getLocation(), Sound.FIRE_IGNITE, 1, 1);
                            }
                        };
                        r.runTaskLater(instance.getGameManager().getMain(), 15);
                        r = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (puffer.contains(fish)) {
                                    Location loc = fish.getLocation();
                                    player.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2, false, false);
                                    fish.remove();
                                    puffer.remove(fish);
                                }
                            }
                        };
                        r.runTaskLater(instance.getGameManager().getMain(), 20 * 10);
                    }
        
                }, pufferFish);
                instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                        player.getLocation().getDirection().multiply(2.0D));
            }
            if (remove) {
                event.setCancelled(true);
                int amount = item.getAmount();
                amount--;
                if (amount == 0)
                    player.getInventory().remove(player.getItemInHand());
                else
                    player.getItemInHand().setAmount(amount);
            }
        }
    }
}
