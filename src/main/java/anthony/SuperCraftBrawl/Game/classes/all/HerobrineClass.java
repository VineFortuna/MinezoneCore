package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.util.ChatColorHelper;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class HerobrineClass extends BaseClass {

    private final ItemStack weapon;
    private final ItemStack despairItem;
    private final Ability despairAbility = new Ability("&b&lDiamond of Despair", DESPAIR_ABILITY_COOLDOWN / 1000, player);
    private static final double DESPAIR_ABILITY_COOLDOWN = 15 * 1000;
    private static final double DESPAIR_ABILITY_RANGE = 10;

    private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 4 * 20, 1, true, true);

    // Rising Ruin constants
    private static final double RUIN_EXPLOSION_RADIUS = 3.5;
    private static final double RUIN_DAMAGE_SELF = 3.0;
    private static final double RUIN_DAMAGE_ENEMY = 5.5;
    private static final double RUIN_UPWARD_VELOCITY = 0.95;

    private static final String RUIN_METADATA = "RisingRuinBlock";

    private static final int[][] RUIN_CIRCLE = {
            {-1, -3}, {0, -3}, {1, -3}, {2, -3},
            {-2, -2},                         {3, -2},
            {-2, -1},                         {3, -1},
            {-2,  0},                         {3,  0},
            {-2,  1},                         {3,  1},
            {-1,  2}, {0,  2}, {1,  2}, {2,  2}
    };

    public HerobrineClass(GameInstance instance, Player player) {
        super(instance, player);
        baseVerticalJump = 1.1;
        createArmor(
                null,
                "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM1YmRkN2VmZjExYzg3ZDUyYTExM2MyZWZiNGNhNDU3NzVlNTY3MzVkYzRiMzhkN2ZhMWRiNzA4NDU4In19fQ==",
                null,
                6,
                "Herobrine"
        );

        weapon = ItemHelper.setDetails(new ItemStack(Material.GOLD_SWORD), "&e&lHerobrine Sword");
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
        ItemHelper.setUnbreakable(weapon);

        String rangeDisplay = ItemHelper.formatDouble(DESPAIR_ABILITY_RANGE);
        despairItem = ItemHelper.setDetails(
                new ItemStack(Material.DIAMOND),
                despairAbility.getAbilityNameRightClickMessage(),
                "&7Cast one of 4 effects:",
                "&7▶ &2Poison " + (poison.getAmplifier() + 1) + " &rfor &a" + poison.getDuration() / 20 +
                        "s &ron enemies",
                "&7▶ &cFire&r lightning at enemies",
                "&7▶ &4Rising Ruin&r erupts damaging blocks around you",
                "&7▶ &8Invoke&r a zombie upon each enemy",
                "",
                "&rRange: &a" + rangeDisplay + " blocks"
        );
    }

    @Override
    public void SetItems(Inventory playerInv) {
        herobrine.startTime = System.currentTimeMillis() - 100000;
        playerInv.setItem(0, weapon);
        playerInv.setItem(1, despairItem);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
    }

    @Override
    public void Tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Herobrine
                && instance.classes.get(player).getLives() > 0) {
            int cooldownSec = (int) ((DESPAIR_ABILITY_COOLDOWN - herobrine.getTime()) / 1000 + 1);
            if (herobrine.getTime() < DESPAIR_ABILITY_COOLDOWN) {
                String msg = instance.color("&b&lDiamond of Despair &rregenerates in: &e" + cooldownSec + "s");
                getActionBarManager().setActionBar(player, "herobrine.cooldown", msg, 2);
            } else {
                String msg = instance.color("&rYou can use &b&lDiamond of Despair");
                getActionBarManager().setActionBar(player, "herobrine.cooldown", msg, 2);
            }
        }
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.DIAMOND
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);

            if (herobrine.getTime() < DESPAIR_ABILITY_COOLDOWN) {
                int seconds = (int) ((DESPAIR_ABILITY_COOLDOWN - herobrine.getTime()) / 1000 + 1);
                player.sendMessage(instance.color("&c&l(!) &rYour &b&lDiamond of Despair&r is still " +
                        "regenerating for &a" + seconds + "s"));
            } else {
                searchForPlayers();
            }
        }
    }

    private void searchForPlayers() {
        ArrayList<Player> playersInRange = new ArrayList<>();

        for (Entity entity : player.getWorld().getNearbyEntities(
                player.getLocation(), DESPAIR_ABILITY_RANGE, DESPAIR_ABILITY_RANGE, DESPAIR_ABILITY_RANGE)) {
            if (entity instanceof Player && !entity.equals(player)) {
                Player playerInRange = (Player) entity;
                if (!checkIfDead(playerInRange, instance) && !instance.HasSpectator(playerInRange)) {
                    playersInRange.add(playerInRange);
                }
            }
        }

        if (!playersInRange.isEmpty()) {
            herobrine.restart();

            switch (new Random().nextInt(4)) {
                case 0:
                    player.sendMessage(ChatColorHelper.color("&2&l(!) &rYou poisoned your enemies!"));
                    for (Player target : playersInRange) {
                        target.addPotionEffect(poison);
                        target.sendMessage(ChatColorHelper.color("&2&l(!) &e" + player.getName() + " &rpoisoned you!"));
                    }
                    break;
                case 1:
                    player.sendMessage(ChatColorHelper.color("&2&l(!) &rYou set your enemies on fire!"));
                    for (Player target : playersInRange) {
                        target.setFireTicks(80);
                        instance.getMapWorld().strikeLightningEffect(target.getLocation());
                        target.sendMessage(ChatColorHelper.color("&2&l(!) &e" + player.getName() + " &rset you on fire!"));
                    }
                    break;
                case 2:
                    launchRisingRuin(playersInRange, 1);
                    break;
                case 3:
                    spawnZombies(playersInRange);
                    break;
            }
        } else {
            player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
        }
    }

    private void spawnZombies(List<Player> targets) {
        player.sendMessage(ChatColorHelper.color("&2&l(!) &rYou summoned zombies upon your enemies!"));

        for (Player target : targets) {
            target.sendMessage(ChatColorHelper.color("&2&l(!) &e" + player.getName() + " &rsummoned a zombie on you!"));

            Zombie zombie = (Zombie) target.getWorld().spawnEntity(target.getLocation(), EntityType.ZOMBIE);
            zombie.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eZombie"));
            zombie.setCustomNameVisible(true);
            zombie.setTarget(target);
            zombie.getEquipment().clear();
            zombie.getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));
            zombie.setCanPickupItems(false);
        }
    }

    @SuppressWarnings("deprecation")
    private void launchRisingRuin(List<Player> targets, int wave) {
        World world = player.getWorld();
        Location origin = player.getLocation().getBlock().getLocation();

        if (wave == 1) {
            world.playSound(origin, Sound.WITHER_SPAWN, 1.0f, 0.6f);
            world.playSound(origin, Sound.PORTAL_TRAVEL, 0.5f, 0.3f);
            player.sendMessage(ChatColorHelper.color("&2&l(!) &4Rising Ruin&r erupts from the earth!"));
            targets.forEach(t -> t.sendMessage(
                    ChatColorHelper.color("&2&l(!) &e" + player.getName() + " &rsummoned &4Rising Ruin&r!")));
        } else {
            world.playSound(origin, Sound.WITHER_HURT, 0.8f, 0.5f);
        }

        final Player caster = this.player;
        final int[] landedCount = {0};
        final int totalBlocks = RUIN_CIRCLE.length;
        final String sessionId = "ruin-" + player.getUniqueId() + "-" + System.nanoTime() + "-" + wave;

        final List<List<RuinBlockSnapshot>> allSnapshots = new ArrayList<>();
        final Map<Integer, RuinEntityData> ruinEntities = new HashMap<>();

        final Listener ruinListener = new Listener() {
            @EventHandler
            public void onRisingRuinChangeBlock(EntityChangeBlockEvent event) {
                if (!isRisingRuinEntity(event.getEntity(), sessionId)) {
                    return;
                }

                event.setCancelled(true);

                RuinEntityData data = ruinEntities.get(event.getEntity().getEntityId());

                if (data != null) {
                    cleanupRuinBlocks(data.blockMat, data.snapshots);
                    scheduleExtraRuinCleanup(data.blockMat, data.snapshots);
                }

                resendActualBlock(event.getBlock());

                if (!event.getEntity().isDead()) {
                    event.getEntity().remove();
                }
            }
        };

        instance.getGameManager().getMain().getServer().getPluginManager()
                .registerEvents(ruinListener, instance.getGameManager().getMain());

        for (int i = 0; i < RUIN_CIRCLE.length; i++) {
            Location spawnLoc = new Location(
                    world,
                    origin.getX() + RUIN_CIRCLE[i][0],
                    origin.getY(),
                    origin.getZ() + RUIN_CIRCLE[i][1]
            );

            final Material blockMat = (i % 2 == 0) ? Material.NETHERRACK : Material.SOUL_SAND;
            final List<RuinBlockSnapshot> originalBlocks = captureRuinBlockSnapshot(world, spawnLoc);
            allSnapshots.add(originalBlocks);

            FallingBlock fb = world.spawnFallingBlock(spawnLoc, blockMat, (byte) 0);
            fb.setVelocity(new Vector(0, RUIN_UPWARD_VELOCITY, 0));
            fb.setDropItem(false);
            fb.setHurtEntities(false);
            fb.setMetadata(RUIN_METADATA, new FixedMetadataValue(instance.getGameManager().getMain(), sessionId));

            ruinEntities.put(fb.getEntityId(), new RuinEntityData(blockMat, originalBlocks));

            final Location[] lastPos = {fb.getLocation()};
            final boolean[] triggered = {false};

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (triggered[0]) return;

                    triggered[0] = true;
                    completeRuinBlock(fb, blockMat, originalBlocks, lastPos[0], world,
                            caster, targets, landedCount, totalBlocks, wave, ruinListener, ruinEntities);
                }
            }.runTaskLater(instance.getGameManager().getMain(), 100L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!fb.isValid() || fb.isDead()) {
                        if (!triggered[0]) {
                            triggered[0] = true;
                            completeRuinBlock(fb, blockMat, originalBlocks, lastPos[0], world,
                                    caster, targets, landedCount, totalBlocks, wave, ruinListener, ruinEntities);
                        }

                        this.cancel();
                        return;
                    }

                    Location current = fb.getLocation();
                    boolean movingDown = current.getY() < lastPos[0].getY();
                    lastPos[0] = current;

                    if (movingDown) {
                        Location below = current.clone().subtract(0, 0.2, 0);

                        if (below.getBlock().getType().isSolid() || below.getY() <= origin.getY() + 0.5) {
                            if (!triggered[0]) {
                                triggered[0] = true;
                                completeRuinBlock(fb, blockMat, originalBlocks, current, world,
                                        caster, targets, landedCount, totalBlocks, wave, ruinListener, ruinEntities);
                            }

                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(instance.getGameManager().getMain(), 5L, 1L);
        }

        /*
         * Final safety cleanup. If Bukkit/client weirdness leaves a ghost block,
         * this forces the real server block state back to every player again.
         */
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Integer, RuinEntityData> entry : ruinEntities.entrySet()) {
                    RuinEntityData data = entry.getValue();
                    cleanupRuinBlocks(data.blockMat, data.snapshots);
                }

                for (List<RuinBlockSnapshot> snapshots : allSnapshots) {
                    resendRuinSnapshots(snapshots);
                }

                HandlerList.unregisterAll(ruinListener);
            }
        }.runTaskLater(instance.getGameManager().getMain(), 140L);
    }

    private void completeRuinBlock(FallingBlock fb,
                                   Material blockMat,
                                   List<RuinBlockSnapshot> originalBlocks,
                                   Location impactLoc,
                                   World world,
                                   Player caster,
                                   List<Player> targets,
                                   int[] landedCount,
                                   int totalBlocks,
                                   int wave,
                                   Listener ruinListener,
                                   Map<Integer, RuinEntityData> ruinEntities) {
        if (fb != null) {
            ruinEntities.remove(fb.getEntityId());

            if (fb.isValid() && !fb.isDead()) {
                fb.remove();
            }
        }

        cleanupRuinBlocks(blockMat, originalBlocks);
        scheduleExtraRuinCleanup(blockMat, originalBlocks);

        Location safeImpact = impactLoc == null ? player.getLocation() : impactLoc.clone();
        onBlockLanded(world, safeImpact, caster, targets, landedCount, totalBlocks, wave, ruinListener);
    }

    private boolean isRisingRuinEntity(Entity entity, String sessionId) {
        if (entity == null || !entity.hasMetadata(RUIN_METADATA)) {
            return false;
        }

        for (MetadataValue value : entity.getMetadata(RUIN_METADATA)) {
            if (value != null && sessionId.equals(value.asString())) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private List<RuinBlockSnapshot> captureRuinBlockSnapshot(World world, Location spawnLoc) {
        List<RuinBlockSnapshot> snapshots = new ArrayList<>();

        if (world == null || spawnLoc == null) {
            return snapshots;
        }

        int x = spawnLoc.getBlockX();
        int z = spawnLoc.getBlockZ();
        int baseY = spawnLoc.getBlockY();

        /*
         * Capture the whole possible vertical path of the falling block.
         * This lets us safely restore or resend blocks without deleting map terrain.
         */
        for (int y = baseY - 4; y <= baseY + 32; y++) {
            if (y < 0 || y >= world.getMaxHeight()) continue;

            Block block = world.getBlockAt(x, y, z);
            snapshots.add(new RuinBlockSnapshot(world, x, y, z, block.getType(), block.getData()));
        }

        return snapshots;
    }

    @SuppressWarnings("deprecation")
    private void cleanupRuinBlocks(Material blockMat, List<RuinBlockSnapshot> snapshots) {
        if (snapshots == null) return;

        for (RuinBlockSnapshot snapshot : snapshots) {
            Block block = snapshot.world.getBlockAt(snapshot.x, snapshot.y, snapshot.z);

            boolean currentIsRuinBlock = block.getType() == blockMat;
            boolean mapAlreadyHadSameBlock = snapshot.originalType == blockMat
                    && snapshot.originalData == block.getData();

            /*
             * Only restore if Rising Ruin actually placed/ghosted its own block here.
             * If the map already had netherrack/soul sand here, leave it alone.
             */
            if (currentIsRuinBlock && !mapAlreadyHadSameBlock) {
                block.setTypeIdAndData(snapshot.originalType.getId(), snapshot.originalData, false);
            }

            resendActualBlock(block);
        }
    }

    private void scheduleExtraRuinCleanup(final Material blockMat, final List<RuinBlockSnapshot> snapshots) {
        int[] delays = {1, 3, 10, 20};

        for (final int delay : delays) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    cleanupRuinBlocks(blockMat, snapshots);
                    resendRuinSnapshots(snapshots);
                }
            }.runTaskLater(instance.getGameManager().getMain(), delay);
        }
    }

    private void resendRuinSnapshots(List<RuinBlockSnapshot> snapshots) {
        if (snapshots == null) return;

        for (RuinBlockSnapshot snapshot : snapshots) {
            Block block = snapshot.world.getBlockAt(snapshot.x, snapshot.y, snapshot.z);
            resendActualBlock(block);
        }
    }

    @SuppressWarnings("deprecation")
    private void resendActualBlock(Block block) {
        if (block == null || block.getWorld() == null) {
            return;
        }

        Location location = block.getLocation();

        for (Player onlinePlayer : block.getWorld().getPlayers()) {
            onlinePlayer.sendBlockChange(location, block.getType(), block.getData());
        }
    }

    private static class RuinEntityData {
        private final Material blockMat;
        private final List<RuinBlockSnapshot> snapshots;

        private RuinEntityData(Material blockMat, List<RuinBlockSnapshot> snapshots) {
            this.blockMat = blockMat;
            this.snapshots = snapshots;
        }
    }

    private static class RuinBlockSnapshot {
        private final World world;
        private final int x;
        private final int y;
        private final int z;
        private final Material originalType;
        private final byte originalData;

        private RuinBlockSnapshot(World world, int x, int y, int z, Material originalType, byte originalData) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.originalType = originalType;
            this.originalData = originalData;
        }
    }

    private void onBlockLanded(World world,
                               Location impactLoc,
                               Player caster,
                               List<Player> targets,
                               int[] landedCount,
                               int totalBlocks,
                               int wave,
                               Listener ruinListener) {
        triggerRuinImpact(world, impactLoc, caster, targets);
        landedCount[0]++;

        if (landedCount[0] >= totalBlocks) {
            HandlerList.unregisterAll(ruinListener);

            if (wave == 1) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            launchRisingRuin(targets, 2);
                        }
                    }
                }.runTaskLater(instance.getGameManager().getMain(), 10L);
            }
        }
    }

    private void triggerRuinImpact(World world, Location impactLoc, Player caster, List<Player> targets) {
        world.playSound(impactLoc, Sound.EXPLODE, 1.0f, 0.8f);
        world.playEffect(impactLoc, Effect.SMOKE, 10);
        world.playEffect(impactLoc, Effect.EXPLOSION_HUGE, 0);
        world.playEffect(impactLoc.clone().add(0, 0.5, 0), Effect.EXPLOSION_LARGE, 0);

        for (Entity entity : world.getNearbyEntities(impactLoc, RUIN_EXPLOSION_RADIUS, RUIN_EXPLOSION_RADIUS, RUIN_EXPLOSION_RADIUS)) {
            if (!(entity instanceof Player)) continue;

            Player hit = (Player) entity;

            if (hit.equals(caster)) {
                if (!checkIfDead(caster, instance)) {
                    EntityDamageEvent selfDmg = new EntityDamageEvent(caster, DamageCause.CUSTOM, RUIN_DAMAGE_SELF);
                    instance.getGameManager().getMain().getServer().getPluginManager().callEvent(selfDmg);

                    if (!selfDmg.isCancelled()) {
                        caster.damage(RUIN_DAMAGE_SELF);
                    }
                }
            } else if (targets.contains(hit) || (!checkIfDead(hit, instance) && !instance.HasSpectator(hit))) {
                EntityDamageEvent dmgEvent = new EntityDamageEvent(hit, DamageCause.CUSTOM, RUIN_DAMAGE_ENEMY);
                instance.getGameManager().getMain().getServer().getPluginManager().callEvent(dmgEvent);

                if (!dmgEvent.isCancelled()) {
                    hit.damage(RUIN_DAMAGE_ENEMY, caster);
                }
            }
        }
    }

    @Override
    public ClassType getType() {
        return ClassType.Herobrine;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }
}