package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.util.ChatColorHelper;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
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
    private static final double DESPAIR_ABILITY_RANGE = 10;
    private final PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, 5 * 20, 2, true, true);
    private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 4 * 20, 1, true, true);

    // Rising Ruin constants
    private static final int   RUIN_BLOCK_COUNT     = 8;
    private static final double RUIN_RING_RADIUS     = 2.5;
    private static final double RUIN_EXPLOSION_RADIUS = 3.5;
    private static final double RUIN_DAMAGE_SELF      = 3.0;
    private static final double RUIN_DAMAGE_ENEMY     = 6.0;

    private PotionEffect effect;

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

        // Weapon
        weapon = ItemHelper.setDetails(
                new ItemStack(Material.GOLD_SWORD),
                "&e&lHerobrine Sword"
        );
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
        ItemHelper.setUnbreakable(weapon);

        // Despair Ability
        String rangeDisplay = ItemHelper.formatDouble(DESPAIR_ABILITY_RANGE);

        despairItem = ItemHelper.setDetails(
                new ItemStack(Material.DIAMOND),
                despairAbility.getAbilityNameRightClickMessage(),
                "&7Inflict one of 4 effects on enemies:",
                "&7▶ &3&oSlowness &e" + (slowness.getAmplifier() + 1) + " &7for &e" + slowness.getDuration() / 20 + "s",
                "&7▶ &2&oPoison &e" + (poison.getAmplifier() + 1) + " &7for &e" + poison.getDuration() / 20 + "s",
                "&7▶ &c&oFire&7, by striking lightning at them",
                "&7▶ &4&o☠ Rising Ruin&7, blocks erupt around you",
                "",
                "&7Range: &a" + rangeDisplay + " &7blocks"
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
                String msg = instance.getGameManager().getMain()
                        .color("&b&lDiamond of Despair &rregenerates in: &e" + cooldownSec + "s");
                getActionBarManager().setActionBar(player, "herobrine.cooldown", msg, 2);
            } else {
                String msg = instance.getGameManager().getMain().color("&rYou can use &b&lDiamond of Despair");
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
                player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                        + "Your Diamond of Despair is still regenerating for " + ChatColor.YELLOW + seconds
                        + " more seconds ");
            } else {
                searchForPlayers();
            }
        }
    }

    private void searchForPlayers() {
        boolean foundPlayers = false;
        ArrayList<Player> playersInRange = new ArrayList<>();

        for (Entity entity : player.getWorld().getNearbyEntities(
                player.getLocation(),
                DESPAIR_ABILITY_RANGE,
                DESPAIR_ABILITY_RANGE,
                DESPAIR_ABILITY_RANGE
        )) {
            if (entity instanceof Player && !entity.equals(player)) {
                Player playerInRange = (Player) entity;
                if (!checkIfDead(playerInRange, instance) && !instance.HasSpectator(playerInRange)) {
                    playersInRange.add(playerInRange);
                    foundPlayers = true;
                }
            }
        }
        if (foundPlayers) {
            herobrine.restart();

            // TESTING: Rising Ruin only
            launchRisingRuin(playersInRange);
        } else player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
    }

    // -----------------------------------------------------------------------
    //  Rising Ruin ability
    // -----------------------------------------------------------------------

    @SuppressWarnings("deprecation")
    private void launchRisingRuin(List<Player> targets) {
        World world = player.getWorld();
        Location origin = player.getLocation().getBlock().getLocation();

        // Ominous activation sounds
        world.playSound(origin, Sound.WITHER_SPAWN, 1.0f, 0.6f);
        world.playSound(origin, Sound.PORTAL_TRAVEL, 0.5f, 0.3f);

        player.sendMessage(ChatColorHelper.color("&4&l(!) &r☠ &4Rising Ruin&r erupts from the earth!"));
        targets.forEach(t -> t.sendMessage(
                ChatColorHelper.color("&4&l(!) &e" + player.getName() + " &rsummoned &4Rising Ruin&r!")));

        final Player caster = this.player;

        // --- Place glass ring exactly where the blocks will fly from ---
        final List<Location> glassLocations = new ArrayList<>();
        final List<Material> previousMaterials = new ArrayList<>();

        for (int i = 0; i < RUIN_BLOCK_COUNT; i++) {
            double angle = (2 * Math.PI / RUIN_BLOCK_COUNT) * i;
            Location glassLoc = new Location(
                    world,
                    origin.getX() + RUIN_RING_RADIUS * Math.cos(angle),
                    origin.getY(),
                    origin.getZ() + RUIN_RING_RADIUS * Math.sin(angle)
            );
            glassLocations.add(glassLoc.clone());
            previousMaterials.add(glassLoc.getBlock().getType());
            glassLoc.getBlock().setType(Material.GLASS);
        }

        final int[] landedCount = { 0 };
        // Exact same velocity for every block — they all rise and fall in perfect sync
        final double upwardVelocity = 0.95;

        // Spawn ALL blocks in the same tick, no stagger
        for (int i = 0; i < RUIN_BLOCK_COUNT; i++) {
            double angle = (2 * Math.PI / RUIN_BLOCK_COUNT) * i;
            Location spawnLoc = new Location(
                    world,
                    origin.getX() + RUIN_RING_RADIUS * Math.cos(angle),
                    origin.getY(),
                    origin.getZ() + RUIN_RING_RADIUS * Math.sin(angle)
            );

            Material blockMat = (i % 2 == 0) ? Material.NETHERRACK : Material.SOUL_SAND;

            @SuppressWarnings("deprecation")
            FallingBlock fb = world.spawnFallingBlock(spawnLoc, blockMat, (byte) 0);
            fb.setVelocity(new Vector(0, upwardVelocity, 0)); // straight up, no drift
            fb.setDropItem(false);
            fb.setHurtEntities(false);

            final Location[] lastPos = { fb.getLocation() };
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!fb.isValid() || fb.isDead()) {
                        onBlockLanded(world, fb.getLocation(), caster, targets,
                                glassLocations, previousMaterials, landedCount);
                        this.cancel();
                        return;
                    }

                    Location current = fb.getLocation();
                    boolean movingDown = current.getY() < lastPos[0].getY();
                    lastPos[0] = current;

                    if (movingDown) {
                        Location below = current.clone().subtract(0, 0.2, 0);
                        if (below.getBlock().getType().isSolid()
                                || below.getY() <= origin.getY() + 0.5) {
                            fb.remove();
                            onBlockLanded(world, current, caster, targets,
                                    glassLocations, previousMaterials, landedCount);
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(instance.getGameManager().getMain(), 5L, 2L);
        }
    }

    /** Called when each falling block hits the ground. Removes glass once all have landed. */
    private void onBlockLanded(World world, Location impactLoc, Player caster, List<Player> targets,
                               List<Location> glassLocations, List<Material> previousMaterials, int[] landedCount) {

        triggerRuinImpact(world, impactLoc, caster, targets);

        landedCount[0]++;
        if (landedCount[0] >= RUIN_BLOCK_COUNT) {
            // Short delay so explosion effects are visible before the glass ring disappears
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int i = 0; i < glassLocations.size(); i++) {
                        glassLocations.get(i).getBlock().setType(previousMaterials.get(i));
                    }
                }
            }.runTaskLater(instance.getGameManager().getMain(), 15L);
        }
    }

    private void triggerRuinImpact(World world, Location impactLoc, Player caster, List<Player> targets) {
        // Explosion sound
        world.playSound(impactLoc, Sound.EXPLODE, 1.0f, 0.8f);

        // Particle burst — smoke + large explosion effect
        world.playEffect(impactLoc, Effect.SMOKE, 10);
        world.playEffect(impactLoc, Effect.EXPLOSION_HUGE, 0);
        world.playEffect(impactLoc.clone().add(0, 0.5, 0), Effect.EXPLOSION_LARGE, 0);

        // Damage anyone within the blast radius of this individual block impact
        for (Entity entity : world.getNearbyEntities(impactLoc, RUIN_EXPLOSION_RADIUS, RUIN_EXPLOSION_RADIUS, RUIN_EXPLOSION_RADIUS)) {
            if (!(entity instanceof Player)) continue;
            Player hit = (Player) entity;

            if (hit.equals(caster)) {
                // Self-damage — Herobrine is not exempt
                if (!checkIfDead(caster, instance)) {
                    caster.damage(RUIN_DAMAGE_SELF);
                }
            } else if (targets.contains(hit) || (!checkIfDead(hit, instance) && !instance.HasSpectator(hit))) {
                hit.damage(RUIN_DAMAGE_ENEMY, caster);
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Original Despair helpers
    // -----------------------------------------------------------------------

    private void applyDespairEffect(Player playerInRange) {
        if (effect == null) {
            playerInRange.setFireTicks(80);
            instance.getMapWorld().strikeLightningEffect(playerInRange.getLocation());
        } else {
            playerInRange.addPotionEffect(effect);
        }

        String enemyMessage = getEnemyFeedbackMessage(effect);
        playerInRange.sendMessage(ChatColorHelper.color("&2&l(!) &e" + player.getName() + enemyMessage));
    }

    private PotionEffect getRandomEffect() {
        Random rand = new Random();
        int chance = rand.nextInt(3);

        switch (chance) {
            case 0:
                return slowness;
            case 1:
                return poison;
            case 2:
                return null;
            default:
                return null;
        }
    }

    private String getEnemyFeedbackMessage(PotionEffect effect) {
        if (effect == slowness) {
            return " &rslowed you!";
        } else if (effect == poison) {
            return " &rpoisoned you!";
        } else {
            return " &rset you on fire!";
        }
    }

    private String getCasterFeedbackMessage(PotionEffect effect) {
        if (effect == slowness) {
            return "&rYou slowed your enemies";
        } else if (effect == poison) {
            return "&rYou poisoned your enemies";
        } else {
            return "&rYou set your enemies on fire";
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
