package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
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

public class EndermanClass extends BaseClass {

    private final ItemStack weapon;
    private final ItemStack teleportItem;
    private final ItemStack blockItem;
    private final Ability teleportAbility = new Ability("&5&lTeleport", 10, player);
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

        teleportItem = ItemHelper.setDetails(
                new ItemStack(Material.ENDER_PEARL, 10),
                teleportAbility.getAbilityNameRightClickMessage()
        );

        blockItem = ItemHelper.setDetails(
                new ItemStack(Material.STICK),
                blockAbility.getAbilityName() + " Pickup &7(Right click)",
                "&7Grab the block you're standing on",
                "&7You can throw it to damage and knock players",
                "",
                blockAbility.getOnGroundItemMessage()
        );
    }

    @Override
    public void SetItems(Inventory playerInv) {
        used = false;
        teleportAbility.getCooldownInstance().reset();
        blockAbility.getCooldownInstance().reset();

        playerInv.setItem(0, weapon);
        playerInv.setItem(1, blockItem);
        playerInv.setItem(2, teleportItem);
    }

    @Override
    public void Tick(int gameTicks) {
        if (!isPlayerAlive()) return;

        ActionBarManager actionBarManager = this.getActionBarManager();
        ActionBarManager.AbilityActionBar abilityActionBar = new ActionBarManager.AbilityActionBar(this, actionBarManager);
        abilityActionBar.setActionBarAbility(player, teleportAbility, blockAbility);

        Inventory inventory = player.getInventory();
        if (inventory.contains(weapon)) return;
        player.getInventory().setItem(0, weapon);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        if (item.isSimilar(weapon)) event.setCancelled(true);

        if (item.equals(blockItem)) {
            if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

            if (used) {
                blockAbility.sendCustomMessage("&c&l(!) &rYou already have a block in your inventory!");
                return;
            }

            if (!player.isOnGround()) {
                blockAbility.sendCustomMessage(blockAbility.getOnGroundChatMessage());
                return;
            }

            if (!blockAbility.isReady()) return;
            getBlockAbility();
        }

        if (item.isSimilar(teleportItem)) {
            if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

            if (!teleportAbility.isReady()) {
                event.setCancelled(true);
                return;
            }

            teleportAbility.use();
        }

        if (item.hasItemMeta() && item.isSimilar(newItem)) {
            throwBlockAbility();
        }
    }

    private void getBlockAbility() {
        Location playerLocation = player.getLocation();
        World playerWorld = player.getWorld();

        Block block = playerWorld.getBlockAt(
                playerLocation.getBlockX(),
                playerLocation.getBlockY() - 1,
                playerLocation.getBlockZ()
        );

        Block blockInside = playerWorld.getBlockAt(playerLocation);

        // Leaves need their own special case as their data comprises more than just the blocks' sub-id.
        // The metadata has the two least significant bits relating to the sub-id, and the other two are
        // related to decay properties, which don't matter in this case here, so they are masked out with
        // 0b0011, or 3. This can not be done with other blocks, such as wool, as they use all 4 bits for
        // sub-ids. It's split into LEAVES and LEAVES_2 because they needed more leaves, but the 2 bits
        // they had only let them have 4 tree species, so they made another block type for the other 2.
        //
        // The exact same applies for logs, but the uses of the other bits is different. I believe its
        // only block rotation?
        if (blockInside.getType().isSolid() && playerLocation.getY() % 1 != 0) {
            if (block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2 ||
                block.getType() == Material.LOG    || block.getType() == Material.LOG_2) {
                newItem = new ItemStack(block.getType(), 1, (short) 0, (byte) (block.getData() & 3));
            } else {
                newItem = new ItemStack(block.getType(), 1, (short) 0, block.getData());
            }
        } else if (block.getType().isSolid()) {
            if (block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2 ||
                block.getType() == Material.LOG    || block.getType() == Material.LOG_2) {
                newItem = new ItemStack(block.getType(), 1, (short) 0, (byte) (block.getData() & 3));
            } else if (block.getType() == Material.PISTON_EXTENSION    || block.getType() == Material.PUMPKIN             ||
                       block.getType() == Material.JACK_O_LANTERN      || block.getType() == Material.PISTON_BASE         ||
                       block.getType() == Material.PISTON_STICKY_BASE  || block.getType() == Material.BRICK_STAIRS        ||
                       block.getType() == Material.SMOOTH_STAIRS       || block.getType() == Material.NETHER_BRICK_STAIRS ||
                       block.getType() == Material.COBBLESTONE_STAIRS  || block.getType() == Material.WOOD_STAIRS         ||
                       block.getType() == Material.SANDSTONE_STAIRS    || block.getType() == Material.SPRUCE_WOOD_STAIRS  ||
                       block.getType() == Material.BIRCH_WOOD_STAIRS   || block.getType() == Material.JUNGLE_WOOD_STAIRS  ||
                       block.getType() == Material.QUARTZ_STAIRS       || block.getType() == Material.ACACIA_STAIRS       ||
                       block.getType() == Material.DARK_OAK_STAIRS     || block.getType() == Material.RED_SANDSTONE_STAIRS) {
                // Yes, I seriously hate this, but it does work. Its temporary until I can think of a better solution.
                newItem = new ItemStack(block.getType(), 1, (short) 0, (byte) 0);
            } else {
                newItem = new ItemStack(block.getType(), 1, (short) 0, block.getData());
            }
        } else {
            player.sendMessage(instance.color("&c&l(!) &rThere is no block under you. Please try again"));
            return;
        }


        // Needs to be done first, otherwise you would get a bunch of null errors from the comparisons.
        if (block.getType() == Material.PISTON_EXTENSION) {
            boolean isSticky = (block.getData() & 8) == 1;
            newItem = new ItemStack(isSticky ? Material.PISTON_BASE : Material.PISTON_STICKY_BASE, 1);
        }

        if (newItem.getType() == Material.DOUBLE_STEP || newItem.getType() == Material.DOUBLE_STONE_SLAB2 || newItem.getType() == Material.WOOD_DOUBLE_STEP) {
            newItem.setType(Material.valueOf(newItem.getType().name().replace("DOUBLE_", "")));
        } else if (newItem.getData() instanceof Stairs) {
            newItem.setType(block.getType());
        } else if (newItem.getData() instanceof Door) {
            newItem.setType(Material.WOOD_DOOR);
        } else if (newItem.getData() instanceof Skull) {
            newItem.setType(Material.SKULL_ITEM);
        } else if (newItem.getType() == Material.SOIL) {
            newItem.setType(Material.DIRT);
        } else if (newItem.getType() == Material.BED_BLOCK) {
            newItem.setType(Material.BED);
        } else if (newItem.getData() instanceof Banner) {
            newItem.setType(Material.BANNER);
        } else if (newItem.getType() == Material.BREWING_STAND) {
            newItem.setType(Material.BREWING_STAND_ITEM);
        } else if (newItem.getType() == Material.CAULDRON) {
            newItem.setType(Material.CAULDRON_ITEM);
        }


        ItemHelper.setDetails(newItem, instance.getGameManager().getMain().color("&e&lBlock"));
        ItemHelper.setDetails(newItem, blockThrowAbility.getAbilityNameLeftRightClickMessage());

        player.getInventory().setItem(1, newItem);
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
        SoundManager.playSoundToAll(player, Sound.ENDERMAN_IDLE, 1, 1);

        blockAbility.use();
        used = true;
    }

    @SuppressWarnings("deprecation")
    private void throwBlockAbility() {
        final ItemStack thrownBlockItem = new ItemStack(newItem);
        final Vector direction = player.getLocation().getDirection().normalize();

        player.getInventory().remove(newItem);
        player.getInventory().setItem(1, blockItem);

        Location spawnLocation = player.getLocation().add(0, 1.25D, 0).add(direction.clone().multiply(1.2D));

        byte data = 0;
        if (thrownBlockItem.getData() != null) {
            data = thrownBlockItem.getData().getData();
        }

        final FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(
                spawnLocation,
                thrownBlockItem.getType(),
                data
        );

        fallingBlock.setDropItem(false);
        fallingBlock.setVelocity(direction.clone().multiply(2.0D).setY(direction.getY() + 0.15D));

        used = false;
        newItem = null;

        final Listener blockLandListener = new Listener() {
            @EventHandler
            public void onFallingBlockLand(EntityChangeBlockEvent event) {
                if (!event.getEntity().equals(fallingBlock)) return;

                event.setCancelled(true);
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

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (fallingBlock == null || fallingBlock.isDead() || !fallingBlock.isValid()) {
                    HandlerList.unregisterAll(blockLandListener);
                    cancel();
                    return;
                }

                if (ticks > 80) {
                    fallingBlock.remove();
                    HandlerList.unregisterAll(blockLandListener);
                    cancel();
                    return;
                }

                for (Entity entity : fallingBlock.getNearbyEntities(0.2D, 0.2D, 0.2D)) {
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

                    player.playSound(hitPlayer.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

                    Location location = hitPlayer.getLocation();

                    EntityDamageEvent damageEvent = new EntityDamageEvent(hitPlayer, DamageCause.PROJECTILE, 4.5);
                    instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);

                    if (!damageEvent.isCancelled()) {
                        hitPlayer.damage(4.5, player);

                        Vector knockback = direction.clone();
                        knockback.setY(1.0D);
                        hitPlayer.setVelocity(knockback);

                        SoundManager.playSoundToAll(player, location, Sound.CHICKEN_EGG_POP, 1, 1);
                    }

                    fallingBlock.remove();
                    HandlerList.unregisterAll(blockLandListener);
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(instance.getGameManager().getMain(), 1L, 1L);
    }

    @Override
    public ClassType getType() {
        return ClassType.Enderman;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }
}