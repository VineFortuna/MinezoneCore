package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class WitherSkeletonClass extends BaseClass {

    private static final String BOW_NAME = "&7&lWitherSkeleton Bow";

    // Damage a hit of the bow adds, in health points, and how often each one is rolled
    private static final double BOW_DAMAGE = 8.0;
    private static final double STRONG_BOW_DAMAGE = 10.0;
    private static final double RARE_BOW_DAMAGE = 18.0;
    private static final int STRONG_BOW_CHANCE_PERCENT = 41;
    private static final int RARE_BOW_CHANCE_PERCENT = 2;

    private final PotionEffect witherShot = new PotionEffect(PotionEffectType.WITHER, 5 * 20, 1);
    private final Random random = new Random();

    private int cooldown = 0;
    private boolean used = false;
    private ItemStack bow;

    public WitherSkeletonClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjVlYzk2NDY0NWE4ZWZhYzc2YmUyZjE2MGQ3Yzk5NTYzNjJmMzJiNjUxNzM5MGM1OWMzMDg1MDM0ZjA1MGNmZiJ9fX0=",
                "363636",
                6,
                "WitherSkeleton"
        );
    }

    @Override
    public ClassType getType() {
        return ClassType.WitherSkeleton;
    }

    @Override
    public ItemStack getAttackWeapon() {
        ItemStack item = ItemHelper
                .addEnchant(
                        ItemHelper
                                .addEnchant(
                                        ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER),
                                                instance.getGameManager().getMain()
                                                        .color("&rEye of Ender")),
                                        Enchantment.DAMAGE_ALL, 1),
                        Enchantment.KNOCKBACK, 1);
        return item;
    }

    @Override
    public void projectileLaunch(ProjectileLaunchEvent event) {
        Entity e = event.getEntity();

        if (this.used == true) {
            if (e instanceof Arrow)
                event.setCancelled(true);
        } else {
            if (e instanceof Arrow) {
                for (Player p : instance.players)
                    p.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
                this.used = true;
            }
        }
    }

    @Override
    public void tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getLives() > 0
                && instance.classes.get(player).getType() == ClassType.WitherSkeleton)
            if (!(player.getInventory().contains(this.getAttackWeapon())))
                player.getInventory().setItem(0, this.getAttackWeapon());

        if (this.used) {
            for (Entity e : instance.getMapWorld().getEntities()) {
                if (e instanceof Arrow) {
                    Arrow a = (Arrow) e;

                    if (a.getShooter() instanceof Player) {
                        if (a.getShooter() == player) {
                            if (a.isOnGround()) {
                                for (Player gamePlayer : instance.players)
                                    gamePlayer.playEffect(a.getLocation(), Effect.EXPLOSION_HUGE, 1);
                                List<Entity> near = a.getNearbyEntities(2.5D, 2.0D, 2.5D);

                                for (Entity en : near) {
                                    if (en instanceof Player) {
                                        Player p = (Player) en;

                                        if (instance.duosMap != null) {
                                            if (!(instance.team.get(p).equals(instance.team.get(player)))) {
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                                                @SuppressWarnings("deprecation")
                                                EntityDamageEvent damageEvent = new EntityDamageEvent(p,
                                                        DamageCause.WITHER, 8.0);
                                                instance.getGameManager().getMain().getServer().getPluginManager()
                                                        .callEvent(damageEvent);
                                                p.damage(8.0, player);
                                            }
                                        } else {
                                            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                                            @SuppressWarnings("deprecation")
                                            EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.WITHER,
                                                    8.0);
                                            instance.getGameManager().getMain().getServer().getPluginManager()
                                                    .callEvent(damageEvent);
                                            p.damage(8.0, player);
                                        }
                                    }
                                }
                                e.remove();
                            }
                        }
                    }
                }
            }
            if (gameTicks % 20 == 0 && !checkIfDead(player, instance)) {
                if (this.bow != null) {
                    if (instance.state == GameState.ENDED) {
                        player.getInventory().clear();
                        return;
                    }

                    if (cooldown == 0)
                        this.bow.setDurability((short) 385);
                    else if (cooldown == 1) {
                        this.bow.setDurability((short) 320);
                    } else if (cooldown == 2) {
                        this.bow.setDurability((short) 280);
                    } else if (cooldown == 3) {
                        this.bow.setDurability((short) 230);
                    } else if (cooldown == 4) {
                        this.bow.setDurability((short) 202);
                    } else if (cooldown == 5) {
                        this.bow.setDurability((short) 170);
                    } else if (cooldown == 6) {
                        this.bow.setDurability((short) 145);
                    } else if (cooldown == 7) {
                        this.bow.setDurability((short) 100);
                    } else if (cooldown == 8) {
                        this.bow.setDurability((short) 50);
                    } else if (cooldown == 9) {
                        this.bow.setDurability((short) 0);
                    }

                    player.getInventory().remove(Material.BOW);
                    player.getInventory().addItem(this.bow);
                    cooldown++;

                    if (cooldown >= 9) {
                        this.used = false;
                        this.cooldown = 0;
                        player.getInventory().remove(Material.BOW);
                        player.getInventory().addItem(createBow());
                        String msg = instance.getGameManager().getMain()
                                .color("&rYou can now use " + BOW_NAME);
                        getActionBarManager().setActionBar(player, "wither.skeleton.cooldown", msg, 2);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void doDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow a = (Arrow) event.getDamager();
            if (a.getShooter() instanceof Player) {
                if (event.getEntity() instanceof Player) {
                    Player p = (Player) event.getEntity();
                    if (instance.duosMap != null) {
                        if (instance.team.get(p).equals(instance.team.get(player))) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    p.damage(rollBowDamage(), player);
                    p.addPotionEffect(witherShot);

                    // Creeper-face firework — power 1 keeps the explosion low, roughly 4-5 blocks up
                    Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    fwm.setPower(1);
                    fwm.addEffect(FireworkEffect.builder()
                            .with(FireworkEffect.Type.CREEPER)
                            .withColor(Color.RED, Color.BLACK)
                            .withFade(Color.MAROON)
                            .trail(true)
                            .flicker(false)
                            .build());
                    fw.setFireworkMeta(fwm);

                    for (Player gamePlayer : instance.players)
                        gamePlayer.playEffect(p.getLocation(), Effect.EXPLOSION_HUGE, 1);
                    return;
                }
            }
        } else if (event.getDamager() instanceof Player) {
            BaseClass bc = instance.classes.get(player);
            if (bc != null && bc.getLives() <= 0)
                return;
            int chance = random.nextInt(9);

            if (chance == 5 || chance == 1 || chance == 3 || chance == 7) {
                if (event.getEntity() instanceof Player) {
                    Player p = (Player) event.getEntity();
                    if (instance.duosMap != null)
                        if (instance.team.get(p).equals(instance.team.get(player)))
                            return;

                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 0, true));
                }
            }
        }
    }

    /** The extra damage a hit adds. Most shots are the normal one, some are stronger and a few are rare. */
    private double rollBowDamage() {
        int roll = random.nextInt(100);
        if (roll < RARE_BOW_CHANCE_PERCENT) return RARE_BOW_DAMAGE;
        if (roll < RARE_BOW_CHANCE_PERCENT + STRONG_BOW_CHANCE_PERCENT) return STRONG_BOW_DAMAGE;
        return BOW_DAMAGE;
    }

    private ItemStack createBow() {
        ItemStack item = ItemHelper.setDetails(
                new ItemStack(Material.BOW),
                BOW_NAME + " &7(Right Click)",
                "&7Hits apply &8&oWither &e" + (witherShot.getAmplifier() + 1) + " &7for &e" + witherShot.getDuration() / 20 + "s",
                "&7and deal &e" + ItemHelper.formatDouble(BOW_DAMAGE / 2) + " &c❤ &7of extra damage",
                "",
                "&7Strong shots (&e" + STRONG_BOW_CHANCE_PERCENT + "%&7) deal &e"
                        + ItemHelper.formatDouble(STRONG_BOW_DAMAGE / 2) + " &c❤ &7extra",
                "&7Rare shots (&e" + RARE_BOW_CHANCE_PERCENT + "%&7) deal &e"
                        + ItemHelper.formatDouble(RARE_BOW_DAMAGE / 2) + " &c❤ &7extra"
        );
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1000);
        item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        return ItemHelper.setHideFlags(item, true);
    }

    @Override
    public void setItems(Inventory playerInv) {
        this.cooldown = 0;
        this.used = false;
        this.bow = createBow();

        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1, this.bow);
        playerInv.setItem(35, new ItemStack(Material.ARROW));
    }

}