package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.Timer;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

public class SnowGolemClass extends BaseClass {

    private boolean outOfSlowballs = true;

    private ItemStack weapon;
    private final Ability pumpkinAbility = new Ability("&6&lPumpkin Head", player);
    private final PotionEffect strength  = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (PUMPKIN_ABILITY_DURATION * 20), 0, false, true);
    private static final double PUMPKIN_ABILITY_DURATION = 5;
    private static final double PUMPKIN_ABILITY_RANGE    = 10;

    public Timer snowGolem = new Timer();

    public SnowGolemClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU2MzdhY2FkYWY3Nzc1OGFiYzdkMjQyZDRiODVmY2MyMGNhODM1NDU4MWI5MzNjMDE1Y2Y4NDVhYWFkMzQ4NSJ9fX0=",
                6,
                "FFFFFF",
                "FFFFFF",
                "FFFFFF"
        );
    }

    @Override
    public void SetItems(Inventory playerInv) {
        ItemStack weapon = ItemHelper.create(Material.STICK, ChatColor.GREEN + "Map Knocker");
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        this.weapon = weapon;

        ItemStack snowPlatform = ItemHelper.create(Material.SNOW_BLOCK,
                ChatColor.WHITE + "Snow Platform",
                ChatColor.GRAY + "Right click to save yourself from falling");

        outOfSlowballs = false;
        ItemStack slowballs = ItemHelper.setDetails(
                new ItemStack(Material.SNOW_BALL, 5),
                "&f&lSLOWBALL &7(Right click)",
                "&7Hit players within &e2 blocks &7to give:",
                "&7Slowness 2 for 3s + half a heart damage"
        );


        String radiusDisplay   = ItemHelper.formatDouble(PUMPKIN_ABILITY_RANGE);
        String durationDisplay = ItemHelper.formatDouble(PUMPKIN_ABILITY_DURATION);
        ItemStack pumpkin = ItemHelper.setDetails(
                new ItemStack(Material.PUMPKIN),
                pumpkinAbility.getAbilityNameRightClickMessage(),
                "&7Put a pumpkin on your enemies' head",
                "",
                "&7Gives you &4&oStrength &e" + (strength.getAmplifier() + 1) + " &7for &e" + strength.getDuration() / 20 + "s",
                "&7Duration: &a" + durationDisplay + "&as",
                "&7Range: &a" + radiusDisplay + " &7blocks"
        );

        playerInv.setItem(0, weapon);
        playerInv.setItem(1, snowPlatform);
        playerInv.setItem(2, slowballs);
        playerInv.setItem(3, pumpkin);
        snowGolem.startTime = System.currentTimeMillis() - 100000;
    }

    @Override
    public void Tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.SnowGolem
                && instance.classes.get(player).getLives() > 0) {
            int cooldownSec = (20000 - snowGolem.getTime()) / 1000 + 1;
            if (snowGolem.getTime() < 20000) {
                String msg = instance.getGameManager().getMain()
                        .color("&bSnow Platform &rregenerates in: &e" + cooldownSec + "s");
                getActionBarManager().setActionBar(player, "platform.cooldown", msg, 2);
            } else {
                String msg = instance.getGameManager().getMain().color("&rYou can use &bSnow Platform");
                getActionBarManager().setActionBar(player, "platform.cooldown", msg, 2);
            }
        }

        // Show a barrier in slot 2 when all slowballs are used up
        ItemStack slot2 = player.getInventory().getItem(2);
        if ( slot2 == null || (slot2.getType() != Material.SNOW_BALL && slot2.getType() != Material.BARRIER)) {
            if (!outOfSlowballs) {
                outOfSlowballs = true;
                player.getInventory().setItem(2, ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    instance.color("&c&lOut of Slowballs!"),
                    "",
                    instance.color("&7Get a kill to regen a snowball")));
            }
        }
    }

    // Cancel the vanilla Snowball entity so our ItemProjectile fires instead
    @Override
    public void ProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Snowball) {
            event.setCancelled(true);
        }
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        // SNOW PLATFORM ABILITY
        if (item.getType() == Material.SNOW_BLOCK
                && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                if (snowGolem.getTime() < 20000) {
                    int seconds = (20000 - snowGolem.getTime()) / 1000 + 1;
                    event.setCancelled(true);
                    player.sendMessage(instance.getGameManager().getMain().color(
                            "&c&l(!) &rYour &bSnow Platform &ris still regenerating for &e" + seconds + "s"));
                } else {
                    snowGolem.restart();
                    BukkitRunnable runnable = new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (ticks == 8) {
                                this.cancel();
                            } else {
                                Location targetLocation = new Location(player.getWorld(),
                                        player.getLocation().getX(), player.getLocation().getY() + 1,
                                        player.getLocation().getZ());
                                float originalYaw   = player.getLocation().getYaw();
                                float originalPitch = player.getLocation().getPitch();
                                player.teleport(targetLocation);
                                Location newLocation = player.getLocation();
                                newLocation.setYaw(originalYaw);
                                newLocation.setPitch(originalPitch);
                                player.teleport(newLocation);

                                World playerWorld       = player.getWorld();
                                Location playerLocation = player.getLocation();
                                int platformLength = 3;
                                int platformWidth  = 3;

                                for (int x = -platformLength / 2; x <= platformLength / 2; x++) {
                                    for (int z = -platformWidth / 2; z <= platformWidth / 2; z++) {
                                        Location platformLocation = playerLocation.clone().add(x, -1, z);
                                        Block platformBlock = playerWorld.getBlockAt(platformLocation);
                                        if (platformBlock.getType() == Material.AIR) {
                                            platformBlock.setType(Material.SNOW_BLOCK);
                                            platformBlock.setMetadata("SnowPlatform",
                                                    new FixedMetadataValue(instance.getGameManager().getMain(), true));
                                            Location particleLoc = platformLocation.clone().add(0.5, 1.0, 0.5);
                                            ParticleEffect.BLOCK_CRACK.display(particleLoc, 0.3f, 0.1f, 0.3f, 0.05f, 6,
                                                    new BlockTexture(Material.SNOW_BLOCK));
                                            SoundManager.playSoundToAll(player, platformLocation, Sound.STEP_SNOW, 2, 1.5f);
                                        }
                                    }
                                }

                                Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
                                    for (int x = -platformLength / 2; x <= platformLength / 2; x++) {
                                        for (int z = -platformWidth / 2; z <= platformWidth / 2; z++) {
                                            Location platformLocation = playerLocation.clone().add(x, -1, z);
                                            Block platformBlock = playerWorld.getBlockAt(platformLocation);
                                            if (platformBlock.hasMetadata("SnowPlatform")) {
                                                Location particleLoc = platformLocation.clone().add(0.5, 0.5, 0.5);
                                                ParticleEffect.BLOCK_CRACK.display(particleLoc, 0.3f, 0.3f, 0.3f, 0.05f, 8,
                                                        new BlockTexture(Material.SNOW_BLOCK));
                                                platformBlock.setType(Material.AIR);
                                                platformBlock.removeMetadata("SnowPlatform",
                                                        instance.getGameManager().getMain());
                                                SoundManager.playSoundToAll(player, platformLocation, Sound.DIG_SNOW, 2, 2);
                                            }
                                        }
                                    }
                                }, 4 * 20);
                                ticks++;
                            }
                        }
                    };
                    runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 2);
                }
            }
        }

        // SLOWBALL ABILITY — 5 total, no cooldown
        if (item.getType() == Material.SNOW_BALL
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            if (player.getGameMode() == GameMode.SPECTATOR) return;

            // Consume one snowball
            int amount = item.getAmount() - 1;
            if (amount <= 0) player.getInventory().clear(player.getInventory().getHeldItemSlot());
            else item.setAmount(amount);

            ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
                @Override
                public void onHit(Player hit) {
                    if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                        Location hitLoc = this.getBaseProj().getEntity().getLocation();

                        // Impact effect visible to all
                        for (Player gamePlayer : instance.players) {
                            gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
                        }

                        // Affect all players within 2.0 blocks of impact (covers both direct & ground hits)
                        for (Player gamePlayer : this.getNearby(2.0)) {
                            if (gamePlayer == player) continue;
                            if (checkIfDead(gamePlayer, instance) || instance.HasSpectator(gamePlayer)) continue;

                            gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 70, 1)); // Slowness 2, 3s
                            EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                    EntityDamageEvent.DamageCause.PROJECTILE, 2.0);
                            instance.getGameManager().getMain().getServer().getPluginManager()
                                    .callEvent(damageEvent);
                            gamePlayer.damage(2.0, player);
                        }
                    }
                }
            }, new ItemStack(Material.SNOW_BALL));
            instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                    player.getLocation().getDirection().multiply(2.5D));
        }

        // PUMPKIN HEAD ABILITY
        if (item.getType() == Material.PUMPKIN
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                int amount = item.getAmount();
                if (amount > 0) {
                    boolean foundPlayers = false;
                    for (Entity entity : player.getWorld().getNearbyEntities(
                            player.getLocation(), PUMPKIN_ABILITY_RANGE, PUMPKIN_ABILITY_RANGE, PUMPKIN_ABILITY_RANGE)) {
                        if (entity instanceof Player && !entity.equals(player)) {
                            Player playerInRange = (Player) entity;
                            if (!checkIfDead(playerInRange, instance) && !instance.HasSpectator(playerInRange)) {
                                setPumpkinHead(playerInRange);
                                foundPlayers = true;
                            }
                        }
                    }
                    if (foundPlayers) {
                        amount--;
                        if (amount == 0) player.getInventory().clear(player.getInventory().getHeldItemSlot());
                        else item.setAmount(amount);
                        player.addPotionEffect(strength);
                        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
                    } else {
                        player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    private void setPumpkinHead(Player playerInRange) {
        playerInRange.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1, 2);
        BukkitRunnable runTimer = new BukkitRunnable() {
            int ticks = (int) PUMPKIN_ABILITY_DURATION;
            BaseClass baseClass = instance.classes.get(playerInRange);
            @Override
            public void run() {
                if (ticks == PUMPKIN_ABILITY_DURATION) {
                    if (!checkIfDead(playerInRange, instance)) {
                        ItemStack pumpkin = new ItemStack(Material.PUMPKIN);
                        if (baseClass.getType() == ClassType.Spider) {
                            SpiderClass spiderClass = (SpiderClass) baseClass;
                            if (spiderClass.invisTaskId != -1)
                                pumpkin.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 8);
                            else
                                pumpkin.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 6);
                        } else {
                            pumpkin.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 6);
                        }
                        playerInRange.getInventory().setHelmet(pumpkin);
                    }
                } else if (ticks == 0) {
                    baseClass.resetHead();
                    this.cancel();
                }
                ticks--;
            }
        };
        runTimer.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
    }

    @Override
    public void classesEvent(Player damagerPlayer, BaseClass baseClass) {
        ItemStack slowballs = ItemHelper.setDetails(
                new ItemStack(Material.SNOW_BALL, 1),
                "&f&lSLOWBALL &7(Right click)",
                "&7Hit players within &e2 blocks &7to give:",
                "&7Slowness 2 for 3s + half a heart damage"
        );

        if (damagerPlayer.getInventory().getItem(2).getType() != Material.SNOW_BALL) {
            damagerPlayer.getInventory().setItem(2, slowballs);
            outOfSlowballs = false;
        } else
            damagerPlayer.getInventory().addItem(slowballs);

        damagerPlayer.sendMessage(instance.color("&2&l(!) &rYou got a kill and gained an extra &f&lSLOWBALL"));
    }

    @Override
    public ClassType getType() { return ClassType.SnowGolem; }

    @Override
    public ItemStack getAttackWeapon() { return weapon; }
}