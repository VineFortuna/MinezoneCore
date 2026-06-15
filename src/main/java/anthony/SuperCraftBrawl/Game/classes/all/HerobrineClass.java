package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.util.ChatColorHelper;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
    private static final double DESPAIR_ABILITY_RANGE    = 10;

    private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 4 * 20, 1, true, true);

    // Rising Ruin constants
    private static final double RUIN_EXPLOSION_RADIUS = 3.5;
    private static final double RUIN_DAMAGE_SELF      = 3.0;
    private static final double RUIN_DAMAGE_ENEMY     = 8.0;
    private static final double RUIN_UPWARD_VELOCITY  = 0.95;

    private static final int[][] RUIN_CIRCLE = {
            {-1, -3}, {0, -3}, {1, -3}, {2, -3},   // top row
            {-2, -2},                         {3, -2},
            {-2, -1},                         {3, -1},
            {-2,  0},                         {3,  0},
            {-2,  1},                         {3,  1},
            {-1,  2}, {0,  2}, {1,  2}, {2,  2}    // bottom row
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
                "&r- &2Poison " + (poison.getAmplifier() + 1) + " &rfor &a" + poison.getDuration() / 20 +
                        "s &ron enemies",
                "&r- &cFire&r lightning at enemies",
                "&r- &4Rising Ruin&r erupts damaging blocks around you",
                "&r- &8Invoke&r a zombie upon each enemy",
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
            if (herobrine.getTime() < DESPAIR_ABILITY_COOLDOWN) {
                int seconds = (int) ((DESPAIR_ABILITY_COOLDOWN - herobrine.getTime()) / 1000 + 1);
                event.setCancelled(true);
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

    // -----------------------------------------------------------------------
    //  Zombie Summon
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    //  Rising Ruin
    // -----------------------------------------------------------------------

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
        final int[] landedCount = { 0 };
        final int totalBlocks = RUIN_CIRCLE.length;

        for (int i = 0; i < RUIN_CIRCLE.length; i++) {
            Location spawnLoc = new Location(
                    world,
                    origin.getX() + RUIN_CIRCLE[i][0],
                    origin.getY(),
                    origin.getZ() + RUIN_CIRCLE[i][1]
            );

            final Material blockMat = (i % 2 == 0) ? Material.NETHERRACK : Material.SOUL_SAND;

            FallingBlock fb = world.spawnFallingBlock(spawnLoc, blockMat, (byte) 0);
            fb.setVelocity(new Vector(0, RUIN_UPWARD_VELOCITY, 0));
            fb.setDropItem(false);
            fb.setHurtEntities(false);

            final Location storedLoc = spawnLoc.clone();
            final Location[] lastPos = { fb.getLocation() };
            final boolean[] triggered = { false };

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (triggered[0]) return;

                    triggered[0] = true;

                    if (fb.isValid() && !fb.isDead()) {
                        fb.remove();
                    }

                    removePlacedRuinBlocks(blockMat, lastPos[0], storedLoc);
                    onBlockLanded(world, lastPos[0], caster, targets, landedCount, totalBlocks, wave);
                }
            }.runTaskLater(instance.getGameManager().getMain(), 100L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!fb.isValid() || fb.isDead()) {
                        if (!triggered[0]) {
                            triggered[0] = true;
                            removePlacedRuinBlocks(blockMat, lastPos[0], storedLoc);
                            onBlockLanded(world, lastPos[0], caster, targets, landedCount, totalBlocks, wave);
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
                            fb.remove();

                            if (!triggered[0]) {
                                triggered[0] = true;
                                removePlacedRuinBlocks(blockMat, current, storedLoc);
                                onBlockLanded(world, current, caster, targets, landedCount, totalBlocks, wave);
                            }

                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(instance.getGameManager().getMain(), 5L, 1L);
        }
    }

    private void removePlacedRuinBlocks(Material blockMat, Location... locations) {
        if (locations == null) return;

        for (Location loc : locations) {
            if (loc == null || loc.getWorld() == null) continue;

            World world = loc.getWorld();
            int x = loc.getBlockX();
            int z = loc.getBlockZ();
            int baseY = loc.getBlockY();

            /*
             * FallingBlock sometimes settles slightly above/below the entity's last
             * position, especially on non-full collision blocks like:
             *
             * - fences
             * - cobblestone walls
             * - iron bars
             * - glass panes
             * - fence gates
             * - slabs/stairs
             *
             * So instead of only checking one exact block, check a small vertical
             * column and remove the temporary Rising Ruin block wherever Bukkit placed it.
             */
            for (int y = baseY - 2; y <= baseY + 2; y++) {
                Block block = world.getBlockAt(x, y, z);

                if (block.getType() == blockMat) {
                    block.setType(Material.AIR);
                }
            }
        }
    }

    private void onBlockLanded(World world, Location impactLoc, Player caster,
                               List<Player> targets, int[] landedCount, int totalBlocks, int wave) {
        triggerRuinImpact(world, impactLoc, caster, targets);
        landedCount[0]++;

        if (landedCount[0] >= totalBlocks && wave == 1) {
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