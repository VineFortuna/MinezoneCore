package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import anthony.util.TitleHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class LargeFernClass extends BaseClass {

    private int numberOfSporesProjectiles = 5;
    private final ItemStack weapon;
    private final ItemStack transfernItem;
    private final Ability sporeAbility = new Ability("&2&lSpores", 4, player);
    private final Ability transfernAbility = new Ability("&2&lTransfern", player);
    private static final double SMALL_FERN_TIME = 2;
    private static final double LARGE_FERN_TIME = 10;
    private final PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 9999999, 1, false, false);
    public TransfernRunnable transfernRunnable;
    private boolean isTransferned;
    int sporeDamage = 4;

    public LargeFernClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQxNTczYmYyZTAyNzBiYjYyMDNkMmI3NjRkZDdkMGNiYmM1ZDdiMWJhNmNkY2NjOWFmNWZmNDc0MzRhMGViNCJ9fX0=",
                "285F3F",
                6,
                "LargeFern"
        );

        // Weapon
        weapon = ItemHelper.setDetails(
                new ItemStack(Material.DOUBLE_PLANT, 1, (short) 3),
                "&2&lSpore Burst &7(Right Click)",
                "",
                "&7Launch a burst of spores in a wide spread",
                "",
                "&7Effective at close range"
        );
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

        // Disguise Ability
        String durationDisplaySmall = ItemHelper.formatDouble(SMALL_FERN_TIME);
        String durationDisplayLarge = ItemHelper.formatDouble(LARGE_FERN_TIME);

        transfernItem = ItemHelper.setDetails(
                new ItemStack(Material.LONG_GRASS, 1, (short) 2),
                transfernAbility.getAbilityNameRightClickMessage(),
                "&7Transform into a fern to regenerate health",
                "",
                "&7Moving or performing actions cancel this",
                "",
                "&7Small Fern Time: &a" + durationDisplaySmall + "&as",
                "&7Large Fern Time: &a" + durationDisplayLarge + "&as"
        );
    }

    @Override
    public void SetItems(Inventory playerInv) {
        isTransferned = false;
        // Resetting Spore Launcher CD
        sporeAbility.getCooldownInstance().reset();
        // Setting Items
        playerInv.setItem(0, weapon);
        playerInv.setItem(1, transfernItem);
    }
    
    @Override
    public void Tick(int gameTicks) {
        if (!isPlayerAlive()) return;
        sporeAbility.updateActionBar(player,this);
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        super.onPlayerMove(event);
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        Action action = event.getAction();

        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        if (transfernRunnable != null && isTransferned) {
            transfernRunnable.cleanup();
            transfernRunnable.cancel();
        }

        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        // Spores Ability
        if (item.equals(weapon)) {
            event.setCancelled(true);
            if (!sporeAbility.isReady()) return;
            useSporesAbility();
            sporeAbility.use();
        }

        // Transfern Ability
        if (item.equals(transfernItem)) {
            useTransfernAbility();
        }
    }

    private void useSporesAbility() {
            ItemProjectile itemProjectile = new ItemProjectile(instance, player, new ProjectileOnHit() {
                @Override
                public void onHit(Player hit) {
                    if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                        Location hitLoc = this.getBaseProj().getEntity().getLocation();

                        for (Player gamePlayer : this.getNearby(3.0)) {
                            // Check if the hit player is not the player who triggered the ability
                            if (!gamePlayer.equals(player)) {
                                if (instance.duosMap != null) {
                                    if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
                                        @SuppressWarnings("deprecation")
                                        EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                                EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                        instance.getGameManager().getMain().getServer().getPluginManager()
                                                .callEvent(damageEvent);
                                        gamePlayer.damage(sporeDamage, player);
                                    }
                                } else {
                                    EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                            EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                    instance.getGameManager().getMain().getServer().getPluginManager()
                                            .callEvent(damageEvent);
                                    gamePlayer.damage(sporeDamage, player);
                                }
                            }
                        }
//                                player.getWorld().playSound(hitLoc, Sound.EXPLODE, 2, 5);
//                                player.getWorld().playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
                    }
                }
            }, new ItemStack(Material.WOOD_BUTTON));

            ItemProjectile itemProjectile2 = new ItemProjectile(instance, player, new ProjectileOnHit() {
                @Override
                public void onHit(Player hit) {
                    if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                        Location hitLoc = this.getBaseProj().getEntity().getLocation();

                        for (Player gamePlayer : this.getNearby(3.0)) {
                            // Check if the hit player is not the player who triggered the ability
                            if (!gamePlayer.equals(player)) {
                                if (instance.duosMap != null) {
                                    if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
                                        @SuppressWarnings("deprecation")
                                        EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                                EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                        instance.getGameManager().getMain().getServer().getPluginManager()
                                                .callEvent(damageEvent);
                                        gamePlayer.damage(sporeDamage, player);
                                    }
                                } else {
                                    EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                            EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                    instance.getGameManager().getMain().getServer().getPluginManager()
                                            .callEvent(damageEvent);
                                    gamePlayer.damage(sporeDamage, player);
                                }
                            }
                        }
                        for (Player gamePlayer : instance.players) {
//                                    gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 5);
//                                    gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
                        }
                    }
                }
            }, new ItemStack(Material.WOOD_BUTTON));

            ItemProjectile itemProjectile3 = new ItemProjectile(instance, player, new ProjectileOnHit() {
                @Override
                public void onHit(Player hit) {
                    if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                        Location hitLoc = this.getBaseProj().getEntity().getLocation();

                        for (Player gamePlayer : this.getNearby(3.0)) {
                            // Check if the hit player is not the player who triggered the ability
                            if (!gamePlayer.equals(player)) {
                                if (instance.duosMap != null) {
                                    if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
                                        @SuppressWarnings("deprecation")
                                        EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                                EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                        instance.getGameManager().getMain().getServer().getPluginManager()
                                                .callEvent(damageEvent);
                                        gamePlayer.damage(sporeDamage, player);
                                    }
                                } else {
                                    EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                            EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                    instance.getGameManager().getMain().getServer().getPluginManager()
                                            .callEvent(damageEvent);
                                    gamePlayer.damage(sporeDamage, player);
                                }
                            }
                        }
                        for (Player gamePlayer : instance.players) {
//                                    gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 5);
//                                    gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
                        }
                    }
                }
            }, new ItemStack(Material.WOOD_BUTTON));

            ItemProjectile itemProjectile4 = new ItemProjectile(instance, player, new ProjectileOnHit() {
                @Override
                public void onHit(Player hit) {
                    if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                        Location hitLoc = this.getBaseProj().getEntity().getLocation();

                        for (Player gamePlayer : this.getNearby(3.0)) {
                            // Check if the hit player is not the player who triggered the ability
                            if (!gamePlayer.equals(player)) {
                                if (instance.duosMap != null) {
                                    if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
                                        @SuppressWarnings("deprecation")
                                        EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                                EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                        instance.getGameManager().getMain().getServer().getPluginManager()
                                                .callEvent(damageEvent);
                                        gamePlayer.damage(sporeDamage, player);
                                    }
                                } else {
                                    EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                            EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                    instance.getGameManager().getMain().getServer().getPluginManager()
                                            .callEvent(damageEvent);
                                    gamePlayer.damage(sporeDamage, player);
                                }
                            }
                        }
                        for (Player gamePlayer : instance.players) {
//                                    gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 5);
//                                    gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
                        }
                    }
                }
            }, new ItemStack(Material.WOOD_BUTTON));

            ItemProjectile itemProjectile5 = new ItemProjectile(instance, player, new ProjectileOnHit() {
                @Override
                public void onHit(Player hit) {
                    if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                        Location hitLoc = this.getBaseProj().getEntity().getLocation();

                        for (Player gamePlayer : this.getNearby(3.0)) {
                            // Check if the hit player is not the player who triggered the ability
                            if (!gamePlayer.equals(player)) {
                                if (instance.duosMap != null) {
                                    if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
                                        @SuppressWarnings("deprecation")
                                        EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                                EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                        instance.getGameManager().getMain().getServer().getPluginManager()
                                                .callEvent(damageEvent);
                                        gamePlayer.damage(sporeDamage, player);
                                    }
                                } else {
                                    EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
                                            EntityDamageEvent.DamageCause.VOID, sporeDamage);
                                    instance.getGameManager().getMain().getServer().getPluginManager()
                                            .callEvent(damageEvent);
                                    gamePlayer.damage(sporeDamage, player);
                                }
                            }
                        }
                        for (Player gamePlayer : instance.players) {
//                                    gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 5);
//                                    gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
                        }
                    }
                }
            }, new ItemStack(Material.WOOD_BUTTON));

            // Shooting Projectile
//                    for (int i = 0; i < numberOfSporesProjectiles; i++) {
            // Generate a random offset for each projectile
            double xOffset = (Math.random() - 0.5) * 0.4; // Adjust the multiplier to control the spread
            double yOffset = (Math.random() - 0.5) * 0.4;
            double zOffset = (Math.random() - 0.5) * 0.4;

            double xOffset2 = (Math.random() - 0.5) * 0.4; // Adjust the multiplier to control the spread
            double yOffset2 = (Math.random() - 0.5) * 0.4;
            double zOffset2 = (Math.random() - 0.5) * 0.4;

            double xOffset3 = (Math.random() - 0.5) * 0.4; // Adjust the multiplier to control the spread
            double yOffset3 = (Math.random() - 0.5) * 0.4;
            double zOffset3 = (Math.random() - 0.5) * 0.4;

            double xOffset4 = (Math.random() - 0.5) * 0.4; // Adjust the multiplier to control the spread
            double yOffset4 = (Math.random() - 0.5) * 0.4;
            double zOffset4 = (Math.random() - 0.5) * 0.4;

            double xOffset5 = (Math.random() - 0.5) * 0.4; // Adjust the multiplier to control the spread
            double yOffset5 = (Math.random() - 0.5) * 0.4;
            double zOffset5 = (Math.random() - 0.5) * 0.4;

            // Apply the offset to the initial direction
            org.bukkit.util.Vector offset = new org.bukkit.util.Vector(xOffset, yOffset, zOffset);
            org.bukkit.util.Vector shotDirection = player.getLocation().getDirection().add(offset).normalize();

            org.bukkit.util.Vector offset2 = new org.bukkit.util.Vector(xOffset2, yOffset2, zOffset2);
            org.bukkit.util.Vector shotDirection2 = player.getLocation().getDirection().add(offset2).normalize();

            org.bukkit.util.Vector offset3 = new org.bukkit.util.Vector(xOffset3, yOffset3, zOffset3);
            org.bukkit.util.Vector shotDirection3 = player.getLocation().getDirection().add(offset3).normalize();

            org.bukkit.util.Vector offset4 = new org.bukkit.util.Vector(xOffset4, yOffset4, zOffset4);
            org.bukkit.util.Vector shotDirection4 = player.getLocation().getDirection().add(offset4).normalize();

            org.bukkit.util.Vector offset5 = new org.bukkit.util.Vector(xOffset5, yOffset5, zOffset5);
            org.bukkit.util.Vector shotDirection5 = player.getLocation().getDirection().add(offset5).normalize();

            // Shoot the projectile with the modified direction
            instance.getGameManager().getProjManager().shootProjectile(itemProjectile, player.getEyeLocation(), shotDirection.multiply(1.5D));
            instance.getGameManager().getProjManager().shootProjectile(itemProjectile2, player.getEyeLocation(), shotDirection2.multiply(1.5D));
            instance.getGameManager().getProjManager().shootProjectile(itemProjectile3, player.getEyeLocation(), shotDirection3.multiply(1.5D));
            instance.getGameManager().getProjManager().shootProjectile(itemProjectile4, player.getEyeLocation(), shotDirection4.multiply(1.5D));
            instance.getGameManager().getProjManager().shootProjectile(itemProjectile5, player.getEyeLocation(), shotDirection5.multiply(1.5D));

            // Playing Shotgun Sound
            SoundManager.playSoundToAll(player, Sound.EXPLODE, 1, 4);
    }

    private void useTransfernAbility() {
        if (isTransferned) return;
        if (!player.isOnGround()) {
            transfernAbility.sendCustomMessage(transfernAbility.getOnGroundChatMessage());
            return;
        }

        Location playerLoc = player.getLocation();
        Block feetBlock = playerLoc.getBlock();
        Block headBlock = playerLoc.clone().add(0, 1, 0).getBlock();
        Block groundBlock = playerLoc.clone().subtract(0, 1, 0).getBlock();

        if (feetBlock.getType() == Material.AIR &&
                headBlock.getType() == Material.AIR &&
                groundBlock.getType().isSolid()) {

            isTransferned = true;

            transfernRunnable = new TransfernRunnable();
            transfernRunnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
        } else {
            transfernAbility.sendCustomMessage("&c&l(!) &rYou cannot transfern on this block");
        }
    }

    @Override
    public void Death(PlayerDeathEvent e) {
        super.Death(e);
        if (transfernRunnable != null) {
            transfernRunnable.cleanup();
            transfernRunnable.cancel();
        }
    }

    @Override
    public void GameEnd() {
        if (transfernRunnable != null) {
            transfernRunnable.cleanup();
            transfernRunnable.cancel();
        }
    }

    public class TransfernRunnable extends BukkitRunnable {
        int standingStillTime = 0;
        Location fernLocation;
        boolean smallFernSet = false;
        boolean largeFernSet = false;
        final PotionEffect transfernInvisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false);
        final PotionEffect transfernRegeneration = regeneration;
        final int smallFernTime = (int) Math.ceil(SMALL_FERN_TIME);
        final int largeFernTime = (int) Math.ceil(LARGE_FERN_TIME);
        int lastDisplayedTime = -1;

        // Store original ground block information
        Block originalGroundBlock = null;
        Material originalGroundType = null;
        byte originalGroundData = 0;

        @Override
        public void run() {
            // Check if player moved first
            if (instance.hasPlayerMovedPosition(player)) {
                cleanup();
                this.cancel();
                return;
            }

            int remainingTime = largeFernTime - standingStillTime;

            // Handle countdown messages with accurate timing
            if (remainingTime > 0 && remainingTime <= largeFernTime && remainingTime != lastDisplayedTime) {
                TitleHelper.sendTitle(player, "", "&2" + remainingTime + "...", 0, 20, 5);
                lastDisplayedTime = remainingTime;
            }

            // Small Fern Transformation
            if (!smallFernSet && standingStillTime >= smallFernTime) {
                setSmallFern();
                smallFernSet = true;
            }

            // Large Fern Transformation
            if (!largeFernSet && standingStillTime >= largeFernTime) {
                setLargeFern();
                largeFernSet = true;
            }

            standingStillTime++;
        }

        private void setSmallFern() {
            SoundManager.playSoundToAll(player, Sound.DIG_GRASS, 1, 1);
            player.getInventory().setArmorContents(null);
            player.addPotionEffect(transfernInvisibility);

            fernLocation = player.getLocation().clone();

            // Store original ground block information
            originalGroundBlock = fernLocation.clone().subtract(0, 1, 0).getBlock();
            originalGroundType = originalGroundBlock.getType();
            originalGroundData = originalGroundBlock.getData();

            // Change the ground to grass to allow fern placement
            originalGroundBlock.setType(Material.GRASS);
            originalGroundBlock.setData((byte) 0); // Regular grass

            // Set the small fern
            fernLocation.getBlock().setType(Material.LONG_GRASS);
            fernLocation.getBlock().setData((byte) 2);
        }

        private void setLargeFern() {
            SoundManager.playSoundToAll(player, Sound.DIG_GRASS, 1, 1);
            player.addPotionEffect(transfernRegeneration);

            // Ensure we have a valid location reference
            if (fernLocation == null) {
                fernLocation = player.getLocation().clone();
            }

            // Remove the small fern
            fernLocation.getBlock().setType(Material.AIR);

            // Set bottom part of large fern
            Block bottomBlock = fernLocation.getBlock();
            bottomBlock.setType(Material.DOUBLE_PLANT);
            bottomBlock.setData((byte) 3); // Fern bottom

            // Set top part of large fern
            Block topBlock = fernLocation.clone().add(0, 1, 0).getBlock();
            topBlock.setType(Material.DOUBLE_PLANT);
            topBlock.setData((byte) 11); // Fern top
        }

        private void cleanup() {
            player.removePotionEffect(transfernInvisibility.getType());
            player.removePotionEffect(transfernRegeneration.getType());
            setArmor(player.getEquipment());

            // Restore original ground block if it was changed
            if (originalGroundBlock != null && originalGroundType != null) {
                originalGroundBlock.setType(originalGroundType);
                originalGroundBlock.setData(originalGroundData);
            }

            if (fernLocation != null) {
                fernLocation.getBlock().setType(Material.AIR);
                fernLocation.clone().add(0, 1, 0).getBlock().setType(Material.AIR);
            }

            if (!largeFernSet || player.getHealth() != player.getMaxHealth()) SoundManager.playErrorSound(player);

            standingStillTime = 0;
            isTransferned = false;
            smallFernSet = false;
            largeFernSet = false;
            lastDisplayedTime = -1;
            instance.clearLastPosition(player);
            // Reset ground block references
            originalGroundBlock = null;
            originalGroundType = null;
        }
    }

    @Override
    public ClassType getType() {
        return ClassType.LargeFern;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }
}
