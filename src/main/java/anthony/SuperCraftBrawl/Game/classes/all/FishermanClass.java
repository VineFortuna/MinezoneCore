package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

    private final ItemStack weapon;
    private final ItemStack bucketItem;
    private final ItemStack waterBucketItem;
    private final Ability grappleAbility = new Ability("&3&lGrappling Hook", 8, player);
    private final Ability bucketAbility = new Ability("&3&lFish Bucket", player);
    private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 6 * 20, 0, false, true);
    private static final int RANDOM_FISH_AMOUNT = 4;
    private static final int MINIMUM_HITS_AMOUNT = 4;

    private int hits;
    private ArrayList<Item> puffer = new ArrayList<>();
    
    private final ItemStack flyingFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH),
            "&a&lFlying Fish",
            "&7Launch at enemies to fishslap them");
    
    private final ItemStack pufferFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH, 1, (short) 3),
            "&e&lTactical Fish",
            "&7Explodes when approached by an enemy",
            "&7Inflicts &2&oPoison&r &e1 &7for &e6 &7seconds",
            "&7Detonates after &e15s");
    
    private final ItemStack speedFish = ItemHelper.setDetails(new ItemStack(Material.RAW_FISH, 1, (short) 2),
            "&b&lTricky Fish",
            "&7Swim away with &b&oSpeed&e 1 &7for &e5 &7s");
    
    private final ItemStack healFish = ItemHelper.setDetails(new ItemStack(Material.COOKED_FISH, 1, (short) 1),
            "&c&lHearty Fish",
            "&7Heals &e0.5 &c❤");
    
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

        // Weapon
        weapon = ItemHelper.setDetails(
                new ItemStack(Material.FISHING_ROD),
                grappleAbility.getAbilityNameRightClickMessage(),
                "",
                "&7Hook onto a block to grapple towards it",
                ""
        );
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        ItemHelper.setUnbreakable(weapon);

        // Empty bucket
        bucketItem = ItemHelper.setDetails(
                new ItemStack(Material.BUCKET, MINIMUM_HITS_AMOUNT),
                "&fBucket",
                "&7Hit enemies to collect fish",
                "&7You need &e" + MINIMUM_HITS_AMOUNT + " &7to fill your bucket"
        );

        // Filled bucket ability
        waterBucketItem = ItemHelper.setDetails(
                new ItemStack(Material.WATER_BUCKET),
                bucketAbility.getAbilityNameRightClickMessage(),
                "&7Receive &e" + RANDOM_FISH_AMOUNT + " &7random fish"
        );
    }
    
    @Override
    public void onFish(PlayerFishEvent event) {
        if (!grappleAbility.isReady()){
//            grappleAbility.sendPlayerRemainingCooldownChatMessage();
            event.setCancelled(true);
            return;
        }

        PlayerFishEvent.State state = event.getState();

        if (state == PlayerFishEvent.State.FAILED_ATTEMPT || state == PlayerFishEvent.State.IN_GROUND) {
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
                player.setVelocity(v.multiply(2).add(new Vector(0, 0.8, 0)));
                player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 10);
                grappleAbility.use();
            }
        }
    }
    
    @Override
    public void Tick(int gameTicks) {
        if (!isPlayerAlive()) return;
        grappleAbility.updateActionBar(player, this);

        if (gameTicks % 10 == 0) {
            detonateTacticalFish();
        }
    }

    private void detonateTacticalFish() {
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
                            EntityDamageEvent.DamageCause.VOID, 5);
                    instance.getGameManager().getMain().getServer().getPluginManager()
                            .callEvent(damageEvent);
                    p.damage(5, player);
                    p.addPotionEffect(poison);
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
    
    @Override
    public void DoDamage(EntityDamageByEntityEvent event) {
        if (!isPlayerAlive()) return;
        if (!(event.getEntity() instanceof Player)) return;

        ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

        boolean isWeaponMelee =
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                        && heldItem != null
                        && heldItem.equals(weapon);

        if (!isWeaponMelee) return;
        onPlayerHit();
    }

    private void onPlayerHit() {
        if (hits < RANDOM_FISH_AMOUNT) {
            hits++;
            player.getInventory().getItem(1).setAmount(RANDOM_FISH_AMOUNT - hits);
            if (hits == RANDOM_FISH_AMOUNT) {
                bucketAbility.sendCustomMessage("&2&l(!) &rYour bucket is full of fish. Bring out the whole ocean!");
                player.getInventory().setItem(1, waterBucketItem);
                player.playSound(player.getLocation(), Sound.WATER, 1, 1);
            }
        }
    }
    
    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        Action action = event.getAction();
        boolean remove = false;

        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        if (item.isSimilar(bucketItem)) bucketAbility.sendCustomMessage("&c&l(!) &rCollect " + (4 - hits) + " more fish!");

        if (item.isSimilar(waterBucketItem)) onFishBucket();

        if (item.isSimilar(healFish)) {
            onHealFish();
            remove = true;
        }

        if (item.isSimilar(speedFish)) {
            onSpeedFish();
            remove = true;
        }

        if (item.isSimilar(flyingFish)) {
            onFlyingFish();
            remove = true;
        }

        if (item.isSimilar(pufferFish)) {
            onPufferFish();
            remove = true;
        }

        if (remove) {
            removeFish(event);
        }
    }

    private void onFishBucket() {
        addFish();
        player.getInventory().setItem(1, bucketItem);
        player.playSound(player.getLocation(), Sound.SPLASH2, 1, 1);
        hits = 0;
    }

    private void addFish() {
        Random random = new Random();
        for (int i = 1; i <= 4; i++) {
            int chance = random.nextInt(4) + 1;
            if (chance == 1)
                player.getInventory().addItem(flyingFish);
            else if (chance == 2)
                player.getInventory().addItem(speedFish);
            else if (chance == 3)
                player.getInventory().addItem(pufferFish);
            else
                player.getInventory().addItem(healFish);
        }
    }

    private void onHealFish() {
        double heal = Math.min(1, player.getMaxHealth() - player.getHealth());
        if (heal > 0) {
            player.setHealth(player.getHealth() + heal);
            player.playSound(player.getLocation(), Sound.EAT, 1, 1);
        } else {
            player.sendMessage(instance.getGameManager().getMain()
                    .color("&c&l(!) &rYou are already full of health!"));
        }
    }

    private void onSpeedFish() {
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
    }

    private void onFlyingFish() {
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

                    player.getWorld().playSound(hit.getLocation(), Sound.SPLASH, 1, 1);

                }

            }, new ItemStack(Material.RAW_FISH));
            instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                    player.getLocation().getDirection().multiply(2.0D));
        }
    }

    private void onPufferFish() {
        ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
            @Override
            public void onHit(Player hit) {
                Location hitLoc = this.getBaseProj().getEntity().getLocation();
                player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
                Item fish = player.getWorld().dropItem(hitLoc, pufferFish);
                fish.setPickupDelay(Integer.MAX_VALUE);
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        puffer.add(fish);
                        player.getWorld().playEffect(fish.getLocation().add(0, 0.5, 0), Effect.VILLAGER_THUNDERCLOUD, 1);
                        player.getWorld().playSound(fish.getLocation(), Sound.FIRE_IGNITE, 1, 1);
                    }
                };
                runnable.runTaskLater(instance.getGameManager().getMain(), 15);
                runnable = new BukkitRunnable() {
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
                runnable.runTaskLater(instance.getGameManager().getMain(), 20 * 10);
            }

        }, pufferFish);
        instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                player.getLocation().getDirection().multiply(2.0D));
    }

    private void removeFish(PlayerInteractEvent event) {
        event.setCancelled(true);
        int amount = event.getItem().getAmount();
        amount--;
        if (amount == 0)
            player.getInventory().remove(player.getItemInHand());
        else
            player.getItemInHand().setAmount(amount);
    }

    @Override
    public void SetItems(Inventory playerInv) {
        hits = 0;
        grappleAbility.getCooldownInstance().reset();
        playerInv.setItem(0, weapon);
        playerInv.setItem(1, bucketItem);
    }

    @Override
    public ClassType getType() {
        return ClassType.Fisherman;
    }

    @Override
    public ItemStack getAttackWeapon() {
       return weapon;
    }
}
