package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.ArrayList;
import java.util.List;

public class EndermiteClass extends BaseClass {
    
    private List<Endermite> endermites = new ArrayList<>();
    private int summonCooldownSec;
    private ItemStack enderSwap = ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER, 10),
            instance.getGameManager().getMain().color("&5&lPhase &0Shifter"),
            instance.getGameManager().getMain().color("&7Shoot at your Endermite to swap places with it!"),
            instance.getGameManager().getMain().color("   &rRange: &e25 blocks"));
    
    public EndermiteClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0=",
                "3C2B4F",
                "3C2B4F",
                "23192E",
                6,
                "Endermite"
        );
    }
    
    @Override
    public ClassType getType() {
        return ClassType.Endermite;
        
    }
    
    @Override
    public void setArmor(EntityEquipment playerEquip) {
        setArmorNew(playerEquip);
    }
    
    @Override
    public ItemStack getAttackWeapon() {
        ItemStack item = ItemHelper.setUnbreakable(ItemHelper.addEnchant(
                new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1));
        return item;
    }
    
    @Override
    public void SetNameTag() {
    
    }
    
    @Override
    public void SetItems(Inventory playerInv) {
        for (Entity e : player.getWorld().getEntities())
            if (e instanceof Endermite)
                if (e.getName().contains(player.getName()))
                    e.remove();
    
        swarmSummon.startTime = System.currentTimeMillis() - 100000;
        endermites.clear();
        
        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.ENDER_PORTAL_FRAME),
                instance.getGameManager().getMain().color("&5&lSwarm Summon")));
        playerInv.setItem(2, enderSwap);
        playerInv.setItem(3, ItemHelper.createMonsterEgg(EntityType.ENDERMITE, 6,
                instance.getGameManager().getMain().color("&5&lEndermite Pokeball")));
    }
    
    @Override
    public void Tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Endermite
                && instance.classes.get(player).getLives() > 0) {
            /*if (!(player.getInventory().contains(this.enderSwap)))
                player.getInventory().setItem(1, this.enderSwap);*/
    
            this.summonCooldownSec = (20000 - swarmSummon.getTime()) / 1000 + 1;
            
            if (swarmSummon.getTime() < 20000) {
                String msg = instance.getGameManager().getMain()
                        .color("&5&lSwarm Summon &rin: &e" + this.summonCooldownSec + "s");
                getActionBarManager().setActionBar(player, "swarm.cooldown", msg, 2);
            } else {
                String msg = instance.getGameManager().getMain().color("&rYou can use &5&lSwarm Summon");
                getActionBarManager().setActionBar(player, "swarm.cooldown", msg, 2);
            }
        }
    }
    
    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && event.getAction().name().contains("RIGHT_CLICK")) {
            if (item.getType() == Material.EYE_OF_ENDER) {
                event.setCancelled(true);
                
                int amount = item.getAmount();
                amount--;
                if (amount == 0)
                    player.getInventory().clear(player.getInventory().getHeldItemSlot());
                else
                    player.getItemInHand().setAmount(amount);
                
                int range = 25;
                Location playerEyeLoc = player.getEyeLocation();
                Vector dir = playerEyeLoc.getDirection();
    
                Location endLoc = playerEyeLoc;
                BlockIterator b = new BlockIterator(playerEyeLoc, 0, range);
    
                // Determine the farthest valid location along the beam
                while (b.hasNext()) {
                    Block block = b.next();
                    endLoc = block.getLocation();
        
                    if (block.getType().isSolid())
                        break;
                }
    
                // Maximum distance along the beam
                double maxDist = endLoc.distance(playerEyeLoc);
    
                for (double t = 1; t < maxDist; t += 0.5) {
                    ParticleEffect.BLOCK_CRACK.display(player.getEyeLocation().add(dir.clone().multiply(t)), 0.0F,
                            0.0F, 0.0F, 0.0F, 1, new BlockTexture(Material.PORTAL));
                }
    
                for (Endermite e : endermites) {
                    Vector d = e.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
                    double dist = d.dot(dir);
    
                    if (dist < maxDist) {
                        Location closest = player.getEyeLocation().add(dir.clone().multiply(dist));
    
                        if (closest.distanceSquared(e.getLocation().add(0, 1, 0)) <= 1.5 * 1.5) {
                            if (instance.isInBounds(e.getLocation())) {
                                Location playerLoc = player.getLocation();
                                Location targetLoc = e.getLocation();
        
                                // Swap player and Endermite locations
                                player.playSound(playerLoc, Sound.ENDERMAN_TELEPORT, 0.5f, 0);
                                player.teleport(targetLoc);
                                player.getWorld().playEffect(playerLoc.add(0, 1, 0), Effect.PORTAL, 1);
                                e.teleport(playerLoc);
        
                                e.setTarget(instance.getNearestPlayer(player, e, 150));
        
                                player.sendMessage(instance.getGameManager().getMain()
                                        .color("&2&l(!) &rYou and your Endermite teleported to each other's location"));
                                break;
                            }
                        }
                    }
                }
            } else if (item.getType() == Material.ENDER_PORTAL_FRAME) {
                event.setCancelled(true);
                if (swarmSummon.getTime() < 20000) {
                    int seconds = (20000 - swarmSummon.getTime()) / 1000 + 1;
                    event.setCancelled(true);
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + "Swarm Summon is still on cooldown for " + ChatColor.YELLOW + seconds + " more seconds ");
                } else {
                    if (endermites.stream().filter(p -> !p.isDead()).toArray().length > 0) {
                        for (Endermite endermite : endermites) {
                            if (!endermite.isDead()) {
                                endermite.teleport(player.getLocation().add(Math.random() - 0.5, 0, Math.random() - 0.5));
                                endermite.setTarget(instance.getNearestPlayer(player, endermite, 150));
                                endermite.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 0));
                            } else {
                                endermites.remove(endermite);
                            }
                        }
                        player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 1, 0);
                        player.getWorld().playEffect(player.getLocation().add(0, 2, 0), Effect.ENDER_SIGNAL, 0);
                        swarmSummon.restart();
                    } else {
                        player.sendMessage(instance.getGameManager().getMain()
                                .color("&c&l(!) &rYou do not have any Endermites to summon!"));
                    }
                }
            } else if (item.getType() == Material.MONSTER_EGG) {
                ItemMeta meta = item.getItemMeta();
        
                if (meta != null && meta.getDisplayName().contains("Endermite")) {
                    int amount = item.getAmount();
                    amount--;
            
                    if (amount == 0)
                        player.getInventory().clear(player.getInventory().getHeldItemSlot());
                    else
                        item.setAmount(amount);
            
                    ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
                        @Override
                        public void onHit(Player hit) {
                            Location hitLoc = this.getBaseProj().getEntity().getLocation();
                            player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
                            Endermite en = (Endermite) player.getWorld().spawnEntity(hitLoc, EntityType.ENDERMITE);
                            en.setCustomName(
                                    "" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Endermite");
                            en.setCustomNameVisible(true);
                            player.playSound(player.getLocation(), Sound.ENDERMAN_STARE, 1, 1);
                            en.setTarget(instance.getNearestPlayer(player, en, 150));
                            endermites.add(en);
                        }
                
                    }, ItemHelper.createMonsterEgg(EntityType.ENDERMITE, 1));
                    instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                            player.getLocation().getDirection().multiply(2.0D));
                }
            }
        }
    }
}
