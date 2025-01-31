package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.CooldownNatowski;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class LargeFernClass extends BaseClass {

    private int numberOfSporesProjectiles = 5;
    private final Ability sporesAbility = new Ability("&2&lSpore Launcher", 4, player);
    private final Ability transfernAbility = new Ability("&2&lTransfern", 9, player);
    private final CooldownNatowski transfernAbilitySmallFernTimer = new CooldownNatowski(2);
    private final ItemStack weapon;
    private final ItemStack transfernItem;
    private boolean isTransferned;
    private Location fernLocation;
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
                sporesAbility.getAbilityNameRightClickMessage(),
                "",
                "&7Launch spores at enemies",
                "&7High spread at close range");
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

        // Disguise Ability
        transfernItem = ItemHelper.setDetails(new ItemStack(Material.LONG_GRASS, 1, (short) 2),
                transfernAbility.getAbilityNameRightClickMessage(),
                "&7Transform into a fern to regenerate health");
    }

    @Override
    public void SetItems(Inventory playerInv) {
        // Resetting Spore Launcher CD
        sporesAbility.getCooldownInstance().reset();
        // Setting Items
        playerInv.setItem(0, weapon);
        playerInv.setItem(1, transfernItem);
        
        isTransferned = false;
    }
    
    @Override
    public void Tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.LargeFern
                && instance.classes.get(player).getLives() > 0) {
            
            if (!sporesAbility.isReady()) {
                String msg = instance.getGameManager().getMain()
                        .color("&2Spore Launch &rregenerates in: &e" +
                                (sporesAbility.getCooldownInstance().getRemainingCooldownSeconds() + 1) + "s");
                getActionBarManager().setActionBar(player, "spore.cooldown", msg, 2);
            } else {
                String msg = instance.getGameManager().getMain().color("&rYou can use &2Spore Launch");
                getActionBarManager().setActionBar(player, "spore.cooldown", msg, 2);
            }
        }
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        super.onPlayerMove(event);
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
    
        if (item != null) {
            // SPORE LAUNCH ABILITY
            if (item.equals(weapon) && (event.getAction().toString().contains("RIGHT_CLICK"))) {
                if (!sporesAbility.isReady()) return;

                sporesAbility.use();

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
//                    }

//                    Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
//                        for (World world : Bukkit.getWorlds()) {
//                            for (Entity entity : world.getEntities()) {
//                                if (entity instanceof Item) {
//                                    Item item2 = (Item) entity;
//                                    // Check if the item is dropped on a wood button block
//                                    Block block = item2.getLocation().getBlock();
//                                    if (block.getType() == Material.WOOD_BUTTON) {
//                                        // Remove the item
//                                        item2.remove();
//                                    }
//                                }
//                            }
//                        }
//                    }, 2 * 20L);

                // Run the task every 60 seconds (adjust as needed)

//                    Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
//                        for (Entity e : player.getWorld().getEntities()) {
//                            if (e != null && e instanceof BlockWoodButton) {
//                                e.remove();to
//                            }
//                        }
//                            } , 1 * 20);

                event.setCancelled(true);

                // Playing Shotgun Sound
                SoundManager.playSoundToAllFromPlayerLocation(instance, player, Sound.EXPLODE, 1, 4);
            }
        
            // TRANSFERN ABILITY
            if (item.equals(transfernItem)) {
                if (!isTransferned) {
                    if (player.getLocation().getBlock().getType() == Material.AIR &&
                            player.getLocation().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                            player.getLocation().subtract(0, 1, 0).getBlock().getType().isSolid() && player.isOnGround()) {
                        // Setting instant when disguise ability was clicked
                        transfernAbility.use();
                        isTransferned = true;
                        // Sending return message
                        transfernAbility.sendPlayerCustomUseAbilityChatMessage("&9&l(!) &rStand still for &6" + transfernAbility.getCooldownDurationSeconds() + " seconds");
    
                        TransfernRunnable runnableInstance = new TransfernRunnable();
                        runnableInstance.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
                    } else {
                        transfernAbility.sendPlayerCustomUseAbilityChatMessage("&c&l(!) &rYou cannot transfern on this block");
                    }
                }
            }
        }
    }
    public class TransfernRunnable extends BukkitRunnable {
        int standingStillTime = 0;
        PotionEffect transfernInvisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false);
        PotionEffect transfernRegeneration = new PotionEffect(PotionEffectType.REGENERATION, 99999, 0, false, false);

        public TransfernRunnable() {

        }

        @Override
        public void run() {
            
            if (transfernAbility.getCooldownInstance().getRemainingCooldownSeconds() > 0) {
                transfernAbility.sendPlayerCustomUseAbilityChatMessage("&6" + transfernAbility.getCooldownInstance().getRemainingCooldownSeconds() + "...");
            }

            // If player moves
            if (instance.hasPlayerMovedPosition(player)) {
                // Removing Invisibility
                player.removePotionEffect(transfernInvisibility.getType());
                // Removing Regeneration
                player.removePotionEffect(transfernRegeneration.getType());
                // Setting Armor back
                setArmor(player.getEquipment());
                // Removing Fern block
                if (fernLocation != null) {
                    fernLocation.getBlock().setType(Material.AIR);
                    fernLocation.add(0, 1, 0).getBlock().setType(Material.AIR);
                }
                // Sending player moving message
                transfernAbility.sendPlayerCustomUseAbilityChatMessage("&c&l(!) &rYou moved and you are no longer a fern");

                standingStillTime = 0;
                isTransferned = false;
                instance.clearLastPosition(player);
                this.cancel();
            }

            // If player stood still for duration required to transfern into the small fern
            if (standingStillTime == transfernAbilitySmallFernTimer.getCooldownDurationSeconds()) {
                // Sending Transfern Small fern return message
                transfernAbility.sendPlayerCustomUseAbilityChatMessage("&d&l(!) &rYou are now a &2Small Fern");
                // Adding invisibility
                player.getInventory().setArmorContents(null);
                player.addPotionEffect(transfernInvisibility);
                // Setting small fern on the player's location
                fernLocation = player.getLocation();
                fernLocation.getBlock().setType(Material.LONG_GRASS);
                fernLocation.getBlock().setData((byte) 2);
            }
            
            // If player stood still for duration required to transfern into the large fern
            if (standingStillTime == transfernAbility.getCooldownDurationSeconds()) {
                // Sending Transfern Large fern return message
                transfernAbility.sendPlayerCustomUseAbilityChatMessage("&d&l(!) &rYou are now a &2Large Fern");
                // Adding regeneration
                player.addPotionEffect(transfernRegeneration);

                // Place the bottom part of the double fern
                Block bottomBlock = fernLocation.getBlock();
                bottomBlock.setType(Material.DOUBLE_PLANT);
                bottomBlock.setData((byte) 3);  // Set to fern bottom part (byte 3 for bottom fern)

                /*// Place the top part of the double fern
                Block topBlock = fernLocation.clone().add(0, 1, 0).getBlock();
                topBlock.setType(Material.DOUBLE_PLANT);
                topBlock.setData((byte) 8);  // Set to fern top part (byte 8 for top fern)

                bottomBlock.setType(Material.DOUBLE_PLANT);
                bottomBlock.setData((byte) 3);  // Set to fern bottom part (byte 3 for bottom fern)*/

            }
    
            // Increasing standing still time on second
            standingStillTime++;
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
