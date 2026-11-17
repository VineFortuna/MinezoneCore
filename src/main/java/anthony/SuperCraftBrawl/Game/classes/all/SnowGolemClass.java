package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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

    // Slowballs
    private static final int SLOWBALL_SLOT = 2;
    private static final int STARTING_SLOWBALLS = 8;
    private static final int SLOWBALLS_PER_KILL = 3;
    /** A thrown slowball leaves its snow trail for this long at most, in case it never lands. */
    private static final int SLOWBALL_TRAIL_MAX_TICKS = 5 * 20;
    private static final int SLOWBALL_TRAIL_PERIOD_TICKS = 3;
    private static final int SLOWBALL_TRAIL_PARTICLES = 1;

    // Tower Up
    private static final int TOWER_STEPS = 8;
    private static final int TOWER_STEP_DELAY_TICKS = 2;
    private static final int PLATFORM_SIZE = 3;
    private static final long PLATFORM_DURATION_TICKS = 4 * 20;
    private static final String PLATFORM_METADATA = "SnowPlatform";

    // Pumpkin Head
    private static final double PUMPKIN_ABILITY_DURATION = 5;
    private static final double PUMPKIN_ABILITY_RANGE = 10;

    private final ItemStack weapon;
    private final ItemStack platformItem;
    private final ItemStack pumpkinItem;
    private final ItemStack barrierItem;
    private final Ability platformAbility = new Ability("&f&lTower Up", 20, player);
    private final Ability pumpkinAbility = new Ability("&6&lPumpkin Head", player);
    private final PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (PUMPKIN_ABILITY_DURATION * 20), 0, false, true);
    private boolean outOfSlowballs = true;

    public SnowGolemClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU2MzdhY2FkYWY3Nzc1OGFiYzdkMjQyZDRiODVmY2MyMGNhODM1NDU4MWI5MzNjMDE1Y2Y4NDVhYWFkMzQ4NSJ9fX0=",
                "FFFFFF",
                6,
                "SnowGolem"
        );

        // Weapon
        weapon = ItemHelper.setDetails(new ItemStack(Material.CARROT_ITEM), "&6&lCarrot Nose");
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

        // Tower Up Ability
        platformItem = ItemHelper.setDetails(
                new ItemStack(Material.SNOW_BLOCK),
                platformAbility.getAbilityNameRightClickMessage(),
                "&7Build a snow tower, sending you up"
        );

        // Pumpkin Head Ability
        pumpkinItem = ItemHelper.setDetails(
                new ItemStack(Material.PUMPKIN),
                pumpkinAbility.getAbilityNameRightClickMessage(),
                "&7Put a pumpkin on each enemy's head",
                "&7Gives you &4&oStrength &e" + (strength.getAmplifier() + 1) + " &7for &e" + strength.getDuration() / 20 + "s",
                "",
                "&7Duration: &a" + ItemHelper.formatDouble(PUMPKIN_ABILITY_DURATION) + "&as",
                "&7Range: &a" + ItemHelper.formatDouble(PUMPKIN_ABILITY_RANGE) + " &7blocks"
        );

        // Barrier, for when there are no slowballs left
        barrierItem = ItemHelper.setDetails(
                new ItemStack(Material.BARRIER),
                "&c&lOut of Slowballs!",
                "",
                "&7Each kill regenerates " + SLOWBALLS_PER_KILL + " slowballs"
        );
    }

    @Override
    public void setItems(Inventory playerInv) {
        platformAbility.getCooldownInstance().reset();
        outOfSlowballs = false;

        playerInv.setItem(0, weapon);
        playerInv.setItem(1, platformItem);
        playerInv.setItem(SLOWBALL_SLOT, createSlowballs(STARTING_SLOWBALLS));
        playerInv.setItem(3, pumpkinItem);
    }

    /** The same slowballs as the ones that drop from the sky. */
    private ItemStack createSlowballs(int amount) {
        ItemStack slowballs = GameLootDrops.SLOWBALLS.getItem();
        slowballs.setAmount(amount);
        return slowballs;
    }

    @Override
    public void tick(int gameTicks) {
        if (!isPlayerAlive()) return;

        platformAbility.updateActionBar(player, this);

        ItemStack slowballSlot = player.getInventory().getItem(SLOWBALL_SLOT);
        boolean slowballsGone = slowballSlot == null
                || (slowballSlot.getType() != Material.SNOW_BALL && slowballSlot.getType() != Material.BARRIER);
        if (slowballsGone && !outOfSlowballs) {
            outOfSlowballs = true;
            player.getInventory().setItem(SLOWBALL_SLOT, barrierItem);
        }
    }

    /** Picking up slowballs of a drop while there are none left replaces the barrier, like the ones of a kill do. */
    @Override
    public void onItemPickup(PlayerPickupItemEvent event) {
        ItemStack picked = event.getItem().getItemStack();
        if (picked.getType() != Material.SNOW_BALL || !GameLootDrops.isLootDropItem(picked)) return;

        ItemStack slowballSlot = player.getInventory().getItem(SLOWBALL_SLOT);
        if (slowballSlot == null || slowballSlot.getType() != Material.BARRIER) return;

        event.setCancelled(true);
        event.getItem().remove();
        player.getInventory().setItem(SLOWBALL_SLOT, picked);
        outOfSlowballs = false;
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.2f, 1.5f);
    }

    @Override
    public void useItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null) return;
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        if (item.getType() == Material.SNOW_BLOCK) {
            event.setCancelled(true);
            useTowerUpAbility();
        } else if (item.getType() == Material.PUMPKIN) {
            event.setCancelled(true);
            usePumpkinAbility(item);
        }
    }

    private void useTowerUpAbility() {
        if (!platformAbility.isReady()) {
            platformAbility.sendPlayerRemainingCooldownChatMessage();
            return;
        }

        if (wouldSuffocateAt(player.getLocation().add(0, 1, 0))) {
            platformAbility.sendCustomMessage("&c&l(!) &rYou need more space above you to use " + platformAbility.getAbilityName());
            return;
        }

        platformAbility.use();

        instance.trackClassTask(new BukkitRunnable() {
            private int steps = 0;

            @Override
            public void run() {
                if (steps >= TOWER_STEPS || !player.isOnline() || !isPlayerAlive()) {
                    cancel();
                    return;
                }

                Location currentLocation = player.getLocation();
                Location targetLocation = currentLocation.clone().add(0, 1, 0);

                // If moving up would put the player inside a block, the platform is placed at the current height and it ends
                if (wouldSuffocateAt(targetLocation)) {
                    placeSnowPlatform(currentLocation);
                    cancel();
                    return;
                }

                targetLocation.setYaw(currentLocation.getYaw());
                targetLocation.setPitch(currentLocation.getPitch());

                player.teleport(targetLocation);
                placeSnowPlatform(player.getLocation());
                steps++;
            }
        }.runTaskTimer(instance.getGameManager().getMain(), 0, TOWER_STEP_DELAY_TICKS));
    }

    private void usePumpkinAbility(ItemStack item) {
        boolean foundPlayers = false;

        for (Entity entity : player.getWorld().getNearbyEntities(
                player.getLocation(), PUMPKIN_ABILITY_RANGE, PUMPKIN_ABILITY_RANGE, PUMPKIN_ABILITY_RANGE)) {
            if (!(entity instanceof Player) || entity.equals(player)) continue;

            Player playerInRange = (Player) entity;
            if (!instance.classes.containsKey(playerInRange)) continue;
            if (checkIfDead(playerInRange, instance) || instance.HasSpectator(playerInRange)) continue;

            setPumpkinHead(playerInRange);
            foundPlayers = true;
        }

        if (!foundPlayers) {
            player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
            return;
        }

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().clear(player.getInventory().getHeldItemSlot());

        player.addPotionEffect(strength);
        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
    }

    private boolean wouldSuffocateAt(Location location) {
        if (location == null || location.getWorld() == null) return true;

        World world = location.getWorld();
        Block feetBlock = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Block headBlock = world.getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());

        return isSuffocatingBlock(feetBlock) || isSuffocatingBlock(headBlock);
    }

    private boolean isSuffocatingBlock(Block block) {
        return block != null && block.getType() != Material.AIR && block.getType().isSolid();
    }

    /** A platform of snow under the given location, that breaks again after a while. */
    private void placeSnowPlatform(Location centerLocation) {
        if (centerLocation == null || centerLocation.getWorld() == null) return;

        World world = centerLocation.getWorld();
        Location center = centerLocation.clone();

        for (int x = -PLATFORM_SIZE / 2; x <= PLATFORM_SIZE / 2; x++) {
            for (int z = -PLATFORM_SIZE / 2; z <= PLATFORM_SIZE / 2; z++) {
                Block platformBlock = world.getBlockAt(center.clone().add(x, -1, z));
                if (platformBlock.getType() != Material.AIR) continue;

                platformBlock.setType(Material.SNOW_BLOCK);
                platformBlock.setMetadata(PLATFORM_METADATA, new FixedMetadataValue(instance.getGameManager().getMain(), true));

                Location particleLoc = getBlockCenter(platformBlock);
                displaySnowParticles(particleLoc, 6);
                SoundManager.playSoundToAll(player, particleLoc, Sound.STEP_SNOW, 2, 1.5f);
            }
        }

        instance.trackClassTask(Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
            for (int x = -PLATFORM_SIZE / 2; x <= PLATFORM_SIZE / 2; x++) {
                for (int z = -PLATFORM_SIZE / 2; z <= PLATFORM_SIZE / 2; z++) {
                    Block platformBlock = world.getBlockAt(center.clone().add(x, -1, z));
                    if (!platformBlock.hasMetadata(PLATFORM_METADATA)) continue;

                    Location particleLoc = getBlockCenter(platformBlock);
                    displaySnowParticles(particleLoc, 8);

                    platformBlock.setType(Material.AIR);
                    platformBlock.removeMetadata(PLATFORM_METADATA, instance.getGameManager().getMain());

                    SoundManager.playSoundToAll(player, particleLoc, Sound.DIG_SNOW, 2, 2);
                }
            }
        }, PLATFORM_DURATION_TICKS));
    }

    private Location getBlockCenter(Block block) {
        return block.getLocation().clone().add(0.5, 0.5, 0.5);
    }

    private void displaySnowParticles(Location location, int amount) {
        ParticleEffect.BLOCK_CRACK.display(location, 0.3f, 0.3f, 0.3f, 0.05f, amount, new BlockTexture(Material.SNOW_BLOCK));
    }

    /** Puts a pumpkin on the player's head, and takes it off once the duration is over. */
    private void setPumpkinHead(Player target) {
        BaseClass targetClass = instance.classes.get(target);
        target.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1, 2);

        // A Spider that is invisible has a stronger helmet
        boolean invisibleSpider = targetClass.getType() == ClassType.Spider && ((SpiderClass) targetClass).invisTaskId != -1;
        ItemStack pumpkin = new ItemStack(Material.PUMPKIN);
        pumpkin.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, invisibleSpider ? 8 : 6);
        target.getInventory().setHelmet(pumpkin);

        instance.trackClassTask(Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
            if (instance.players.contains(target)) targetClass.resetHead();
        }, (long) (PUMPKIN_ABILITY_DURATION * 20)));
    }

    @Override
    public void killEvent(Player damagerPlayer) {
        if (!instance.classes.containsKey(damagerPlayer) || checkIfDead(player, instance)) return;

        ItemStack slowballs = createSlowballs(SLOWBALLS_PER_KILL);
        ItemStack slowballSlot = damagerPlayer.getInventory().getItem(SLOWBALL_SLOT);

        // Replaces the barrier, or adds to the slowballs left
        if (slowballSlot != null && slowballSlot.getType() != Material.SNOW_BALL) {
            damagerPlayer.getInventory().setItem(SLOWBALL_SLOT, slowballs);
            outOfSlowballs = false;
        } else {
            damagerPlayer.getInventory().addItem(slowballs);
        }

        damagerPlayer.sendMessage(instance.color("&2&l(!) &rYou got a kill and gained &e" + SLOWBALLS_PER_KILL
                + " &rextra &f&lSLOWBALLS"));
    }

    /** Every snowball the Snow Golem throws leaves a trail of snow until it lands. */
    @Override
    public void projectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        final Snowball snowball = (Snowball) event.getEntity();

        instance.trackClassTask(new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks += SLOWBALL_TRAIL_PERIOD_TICKS;
                if (!snowball.isValid() || ticks > SLOWBALL_TRAIL_MAX_TICKS) {
                    cancel();
                    return;
                }
                ParticleEffect.SNOW_SHOVEL.display(snowball.getLocation(), 0.08f, 0.08f, 0.08f, 0.01f, SLOWBALL_TRAIL_PARTICLES, null);
            }
        }.runTaskTimer(instance.getGameManager().getMain(), 1, SLOWBALL_TRAIL_PERIOD_TICKS));
    }

    @Override
    public ClassType getType() {
        return ClassType.SnowGolem;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }
}
