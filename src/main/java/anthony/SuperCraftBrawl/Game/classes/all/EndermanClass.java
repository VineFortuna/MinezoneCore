package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EndermanClass extends BaseClass {

    // Teleporter
    private static final int BLOCK_SLOT = 1;
    private static final int TELEPORTER_SLOT = 2;
    private static final int STARTING_TELEPORTERS = 3;
    private static final int TELEPORTERS_PER_KILL = 1;

    // Block
    private static final double BLOCK_THROW_SPEED = 2.0;
    /** Added to the vertical speed of a thrown block, so it flies a bit higher than the aim. */
    private static final double BLOCK_THROW_LIFT = 0.15;
    private static final double BLOCK_DAMAGE = 4.5;
    /** The vertical speed of an enemy a block hits. They are also knocked back the way it flew. */
    private static final double BLOCK_KNOCKBACK_UP = 1.0;
    private static final double BLOCK_HIT_RADIUS = 0.2;
    private static final int BLOCK_LIFETIME_TICKS = 80;

    /** Blocks that have a different look for each data value, which is kept on the item. */
    private static final Set<Material> DATA_BLOCKS = new HashSet<>(Arrays.asList(
            Material.STONE, Material.DIRT, Material.WOOD, Material.SAND, Material.SPONGE, Material.SANDSTONE,
            Material.WOOL, Material.STEP, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.MONSTER_EGG,
            Material.SMOOTH_BRICK, Material.WOOD_STEP, Material.COBBLE_WALL, Material.QUARTZ_BLOCK,
            Material.PRISMARINE, Material.RED_SANDSTONE
    ));
    /** Same, but only the first 2 bits of the data are the look. The rest is the direction. */
    private static final Set<Material> MASKED_DATA_BLOCKS = new HashSet<>(Arrays.asList(
            Material.LEAVES, Material.LEAVES_2, Material.LOG, Material.LOG_2
    ));

    private final ItemStack weapon;
    private final ItemStack blockItem;
    /** No cooldown, so it only gives the ability its name. */
    private final Ability teleportAbility = new Ability("&5&lTeleporter", player);
    private final Ability blockAbility = new Ability("&5&lBlock", 7, player);
    private final Ability blockThrowAbility = new Ability("&5&lBlock Throw", player);

    private ItemStack newItem = null;
    private boolean used = false;

    public EndermanClass(GameInstance instance, Player player) {
        super(instance, player);
        baseVerticalJump = 1.0;
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E1OWJiMGE3YTMyOTY1YjNkOTBkOGVhZmE4OTlkMTgzNWY0MjQ1MDllYWRkNGU2YjcwOWFkYTUwYjljZiJ9fX0",
                "101010",
                6,
                "Enderman"
        );

        weapon = ItemHelper.setDetails(
                new ItemStack(Material.EYE_OF_ENDER),
                "&5&lEnderman Soul"
        );
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

        blockItem = ItemHelper.setDetails(
                new ItemStack(Material.STICK),
                blockAbility.getAbilityName() + " Pickup &7(Right Click)",
                "&7Grab a block and throw it to damage and knock players up and back"
        );
    }

    private ItemStack getTeleporters(int amount) {
        return ItemHelper.setDetails(
                new ItemStack(Material.ENDER_PEARL, amount),
                teleportAbility.getAbilityNameRightClickMessage()
        );
    }

    private ItemStack getBarrier() {
        ItemStack barrier = ItemHelper.setDetails(new ItemStack(Material.BARRIER),
                instance.color("&c&lOut of Pearls!"),
                instance.color("&7Get a kill to gain a pearl"));
        return barrier;
    }

    @Override
    public void setItems(Inventory playerInv) {
        used = false;
        blockAbility.getCooldownInstance().reset();

        playerInv.setItem(0, weapon);
        playerInv.setItem(BLOCK_SLOT, blockItem);
        playerInv.setItem(TELEPORTER_SLOT, getTeleporters(STARTING_TELEPORTERS));
    }

    @Override
    public void tick(int gameTicks) {
        if (!isPlayerAlive()) return;
        if (instance.state == GameState.ENDED)
            return;

        blockAbility.updateActionBar(player, this);

        Inventory inventory = player.getInventory();

        if (!inventory.contains(weapon)) {
            inventory.setItem(0, weapon);
        }

        // A pearl that was the last one leaves the slot empty, and a barrier takes its place
        if (inventory.getItem(TELEPORTER_SLOT) == null) {
            inventory.setItem(TELEPORTER_SLOT, getBarrier());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void useItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        if (item.isSimilar(weapon)) {
            event.setCancelled(true);
            return;
        }

        if (item.equals(blockItem)) {
            if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) {
                return;
            }

            event.setCancelled(true);

            if (used) {
                blockAbility.sendCustomMessage("&c&l(!) &rYou already have a block in your inventory!");
                return;
            }

            if (!blockAbility.isReady()) return;

            if (action != Action.RIGHT_CLICK_BLOCK) {
                blockAbility.sendCustomMessage("&c&l(!) &rClick a block to grab it");
                return;
            }

            grabBlock(event.getClickedBlock());
            return;
        }

        if (newItem != null && item.hasItemMeta() && item.isSimilar(newItem)) {
            event.setCancelled(true);

            if (!action.name().contains("LEFT_CLICK")) {
                return;
            }

            throwBlockAbility();
        }
    }

    /** "Grabs" the block that was clicked: the player gets an item of it to throw, and the block itself stays. */
    private void grabBlock(Block block) {
        Material blockMaterial = block.getType();

        if (!blockMaterial.isSolid()) {
            player.sendMessage(instance.color("&c&l(!) &rYou can't grab that block"));
            return;
        }

        if (MASKED_DATA_BLOCKS.contains(blockMaterial))
            newItem = new ItemStack(blockMaterial, 1, (short) 0, (byte) (block.getData() & 3));
        else if (DATA_BLOCKS.contains(blockMaterial))
            newItem = new ItemStack(blockMaterial, 1, (short) 0, (byte) block.getData());
        else
            newItem = new ItemStack(blockMaterial, 1, (short) 0, (byte) 0);

        // Needs to be done first, otherwise you would get a bunch of null errors from the comparisons.
        if (blockMaterial == Material.PISTON_EXTENSION)
            newItem = new ItemStack(((block.getData() & 8) == 1) ? Material.PISTON_BASE : Material.PISTON_STICKY_BASE, 1);

        if (newItem.getType() == Material.DOUBLE_STEP || newItem.getType() == Material.DOUBLE_STONE_SLAB2 || newItem.getType() == Material.WOOD_DOUBLE_STEP)
            newItem.setType(Material.valueOf(newItem.getType().name().replace("DOUBLE_", "")));
        else if (newItem.getData() instanceof Stairs)
            newItem.setType(blockMaterial);
        else if (newItem.getData() instanceof Door)
            newItem.setType(Material.WOOD_DOOR);
        else if (newItem.getData() instanceof Skull)
            newItem.setType(Material.SKULL_ITEM);
        else if (newItem.getType() == Material.SOIL)
            newItem.setType(Material.DIRT);
        else if (newItem.getType() == Material.BED_BLOCK)
            newItem.setType(Material.BED);
        else if (blockMaterial == Material.BREWING_STAND)
            newItem.setType(Material.BREWING_STAND_ITEM);
        else if (newItem.getType() == Material.CAULDRON)
            newItem.setType(Material.CAULDRON_ITEM);


        ItemHelper.setDetails(newItem, instance.getGameManager().getMain().color("&e&lBlock"));
        ItemHelper.setDetails(newItem, blockThrowAbility.getAbilityNameLeftClickMessage());

        player.getInventory().setItem(BLOCK_SLOT, newItem);
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
        SoundManager.playSoundToAll(player, Sound.ENDERMAN_IDLE, 1, 1);

        blockAbility.use();
        used = true;
    }

    @SuppressWarnings("deprecation")
    private void throwBlockAbility() {
        final ItemStack thrownBlockItem = new ItemStack(newItem);
        final Vector direction = player.getLocation().getDirection().normalize();

        Location spawnLocation = player.getLocation().add(0, 1.25D, 0).add(direction.clone().multiply(1.2D));

        final byte blockData = thrownBlockItem.getData() != null
                ? thrownBlockItem.getData().getData()
                : 0;

        if (thrownBlockItem.getType() == Material.WOOD_DOOR)
            thrownBlockItem.setType(Material.WOODEN_DOOR);
        else if (thrownBlockItem.getType() == Material.SKULL_ITEM)
            thrownBlockItem.setType(Material.SKULL);
        else if (thrownBlockItem.getType() == Material.BED)
            thrownBlockItem.setType(Material.BED_BLOCK);
        else if (thrownBlockItem.getType() == Material.BREWING_STAND_ITEM)
            thrownBlockItem.setType(Material.BREWING_STAND);
        else if (thrownBlockItem.getType() == Material.CAULDRON_ITEM)
            thrownBlockItem.setType(Material.CAULDRON);

        final FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(
                spawnLocation,
                thrownBlockItem.getType(),
                blockData
        );

        player.getInventory().remove(newItem);
        player.getInventory().setItem(BLOCK_SLOT, blockItem);

        fallingBlock.setDropItem(false);
        fallingBlock.setVelocity(direction.clone().multiply(BLOCK_THROW_SPEED).setY(direction.getY() + BLOCK_THROW_LIFT));

        // Sound when the block is thrown
        SoundManager.playSoundToAll(player, spawnLocation, Sound.CHICKEN_EGG_POP, 1.0f, 0.75f);

        used = false;
        newItem = null;

        final Listener blockLandListener = new Listener() {
            @EventHandler
            public void onFallingBlockLand(EntityChangeBlockEvent event) {
                if (!event.getEntity().equals(fallingBlock)) return;

                event.setCancelled(true);

                spawnBlockCollisionParticles(
                        event.getEntity().getLocation(),
                        thrownBlockItem.getType(),
                        blockData
                );

                playBlockCollisionSound(
                        event.getEntity().getLocation(),
                        thrownBlockItem.getType()
                );

                event.getEntity().remove();

                if (event.getBlock().getType() == thrownBlockItem.getType()) {
                    event.getBlock().setType(Material.AIR);
                }

                HandlerList.unregisterAll(this);
            }
        };

        instance.getGameManager().getMain().getServer().getPluginManager().registerEvents(
                blockLandListener,
                instance.getGameManager().getMain()
        );

        instance.trackClassTask(new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (fallingBlock == null || fallingBlock.isDead() || !fallingBlock.isValid()) {
                    HandlerList.unregisterAll(blockLandListener);
                    cancel();
                    return;
                }

                if (ticks > BLOCK_LIFETIME_TICKS) {
                    fallingBlock.remove();
                    HandlerList.unregisterAll(blockLandListener);
                    cancel();
                    return;
                }

                for (Entity entity : fallingBlock.getNearbyEntities(BLOCK_HIT_RADIUS, BLOCK_HIT_RADIUS, BLOCK_HIT_RADIUS)) {
                    if (!(entity instanceof Player)) continue;

                    Player hitPlayer = (Player) entity;

                    if (hitPlayer.equals(player)) continue;
                    if (!hitPlayer.isOnline()) continue;

                    if (instance.duosMap != null) {
                        if (instance.team.get(hitPlayer) != null && instance.team.get(player) != null) {
                            if (instance.team.get(hitPlayer).equals(instance.team.get(player))) {
                                continue;
                            }
                        }
                    }

                    Location location = hitPlayer.getLocation();

                    EntityDamageEvent damageEvent = new EntityDamageEvent(hitPlayer, DamageCause.PROJECTILE, BLOCK_DAMAGE);
                    instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);

                    if (!damageEvent.isCancelled()) {
                        hitPlayer.damage(BLOCK_DAMAGE, player);

                        Vector knockback = direction.clone();
                        knockback.setY(BLOCK_KNOCKBACK_UP);
                        hitPlayer.setVelocity(knockback);

                        spawnBlockCollisionParticles(
                                hitPlayer.getLocation(),
                                thrownBlockItem.getType(),
                                blockData
                        );

                        playBlockCollisionSound(
                                hitPlayer.getLocation(),
                                thrownBlockItem.getType()
                        );
                    }

                    fallingBlock.remove();
                    HandlerList.unregisterAll(blockLandListener);
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(instance.getGameManager().getMain(), 1L, 1L));
    }

    private void spawnBlockCollisionParticles(Location location, Material material, byte data) {
        if (location == null || location.getWorld() == null || material == null) return;

        ParticleEffect.BLOCK_CRACK.display(
                location.clone().add(0, 0.5, 0),
                0.35f,
                0.35f,
                0.35f,
                0.08f,
                18,
                new BlockTexture(material, data)
        );
    }
    private void playBlockCollisionSound(Location location, Material material) {
        if (location == null || location.getWorld() == null || material == null) return;

        Sound sound = getBlockCollisionSound(material);

        SoundManager.playSoundToAll(player, location, sound, 1.0f, 1.0f);
    }

    private Sound getBlockCollisionSound(Material material) {
        String name = material.name();

        if (name.contains("GLASS") || name.contains("THIN_GLASS")) {
            return Sound.GLASS;
        }

        if (name.contains("WOOD") || name.contains("LOG") || name.contains("PLANK")
                || name.contains("FENCE") || name.contains("CHEST")) {
            return Sound.DIG_WOOD;
        }

        if (name.contains("WOOL") || name.contains("CARPET")) {
            return Sound.DIG_WOOL;
        }

        if (name.contains("SAND") || name.contains("GRAVEL")) {
            return name.contains("SAND") ? Sound.DIG_SAND : Sound.DIG_GRAVEL;
        }

        if (name.contains("SNOW") || name.contains("ICE")) {
            return Sound.DIG_SNOW;
        }

        if (name.contains("GRASS") || name.contains("DIRT") || name.contains("SOIL")
                || name.contains("LEAVES")) {
            return Sound.DIG_GRASS;
        }

        return Sound.DIG_STONE;
    }


    @Override
    public ClassType getType() {
        return ClassType.Enderman;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }

    @Override
    public void killEvent(Player damagerPlayer) {
        if (instance.classes.containsKey(damagerPlayer) && !checkIfDead(player, instance)) {
            damagerPlayer.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &rYou got a kill and gained &e"
                    + TELEPORTERS_PER_KILL + " &rextra " + teleportAbility.getAbilityName()));

            // Adds to the pearls left, or replaces the barrier
            ItemStack currentItem = damagerPlayer.getInventory().getItem(TELEPORTER_SLOT);

            if (currentItem == null || currentItem.getType() != Material.ENDER_PEARL) {
                damagerPlayer.getInventory().setItem(TELEPORTER_SLOT, getTeleporters(TELEPORTERS_PER_KILL));
            } else {
                currentItem.setAmount(currentItem.getAmount() + TELEPORTERS_PER_KILL);
            }
        }
    }
}