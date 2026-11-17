package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.ArrayList;
import java.util.List;

public class IceClass extends BaseClass {

    private static final double FREEZE_RAY_RANGE = 30;
    private static final double FREEZE_RAY_DAMAGE = 4.5;
    private static final double FREEZE_RAY_BEAM_RADIUS = 0.35;
    private static final double FREEZE_RAY_HIT_RADIUS = 1.5;
    private static final double FREEZE_BOMB_RANGE = 10;

    private final ItemStack weapon;
    private final ItemStack freezeRayItem;
    private final ItemStack freezeBombItem;
    private final Ability freezeRayAbility = new Ability("&b&lFreeze Ray", 10, player);
    private final Ability freezeBombAbility = new Ability("&b&lFreeze Bomb", player);
    /** Permanent Weakness, to balance. */
    private final PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1, false, false);
    private final PotionEffect rayFreeze = new PotionEffect(PotionEffectType.SLOW, 4 * 20, 1);
    private final PotionEffect bombFreeze = new PotionEffect(PotionEffectType.SLOW, 5 * 20, 4);

    public IceClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZlNDI5YzYwOTMyZWJjMzY2ZTc5MWE0MmUxODZhZjg4OGRlMDhlNWQ4ZWI4YWM2ZjViNmY0ZDQ0MGRiNDg2YyJ9fX0=",
                "92B9FE",
                6,
                "Ice"
        );

        // Weapon
        weapon = ItemHelper.setDetails(new ItemStack(Material.STONE_SWORD), "&f&lIce Sword");
        ItemHelper.setUnbreakable(weapon);

        // Freeze Ray Ability
        freezeRayItem = ItemHelper.setDetails(
                new ItemStack(Material.QUARTZ_BLOCK),
                freezeRayAbility.getAbilityNameRightClickMessage(),
                "&7Shoot a damaging ray that applies",
                "&3&o" + ItemHelper.getPotionEffectName(rayFreeze.getType()) + "&r &e" + (rayFreeze.getAmplifier() + 1)
                        + " &7for &e" + rayFreeze.getDuration() / 20 + "s",
                "",
                "&7Range: &a" + ItemHelper.formatDouble(FREEZE_RAY_RANGE) + " &7blocks"
        );

        // Freeze Bomb Ability
        freezeBombItem = ItemHelper.setDetails(
                new ItemStack(Material.PACKED_ICE),
                freezeBombAbility.getAbilityNameRightClickMessage(),
                "&7Freeze nearby enemies in place",
                "",
                "&7Range: &a" + ItemHelper.formatDouble(FREEZE_BOMB_RANGE) + " &7blocks"
        );
    }

    @Override
    public ClassType getType() {
        return ClassType.Ice;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }

    @Override
    public void setItems(Inventory playerInv) {
        freezeRayAbility.getCooldownInstance().reset(); // Restarts cooldown per life
        playerInv.setItem(0, weapon);
        playerInv.setItem(1, freezeRayItem);
        playerInv.setItem(2, freezeBombItem);

        player.addPotionEffect(weakness);
    }

    @Override
    public void tick(int gameTicks) {
        if (!player.hasPotionEffect(weakness.getType())) player.addPotionEffect(weakness);

        if (!isPlayerAlive()) return;
        freezeRayAbility.updateActionBar(player, this);
    }

    @Override
    public void useItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null) return;
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        if (item.getType() == Material.QUARTZ_BLOCK) {
            event.setCancelled(true);
            if (!freezeRayAbility.isReady()) {
                freezeRayAbility.sendPlayerRemainingCooldownChatMessage();
                return;
            }
            freezeRayAbility.use();
            freezeRayEffect();
        } else if (item.getType() == Material.PACKED_ICE) {
            freezeBombEffect();
        }
    }

    private boolean isEnemy(Player other) {
        return other != player
                && other.getGameMode() != GameMode.SPECTATOR
                && (instance.duosMap == null || !instance.team.get(other).equals(instance.team.get(player)));
    }

    private void freezeRayEffect() {
        Location eyeLocation = player.getEyeLocation();
        Location endLoc = eyeLocation;
        BlockIterator blocks = new BlockIterator(eyeLocation, 0, (int) FREEZE_RAY_RANGE);

        while (blocks.hasNext()) {
            Block block = blocks.next();
            endLoc = block.getLocation();
            if (block.getType().isSolid())
                break;
        }

        Vector dir = eyeLocation.getDirection();
        double maxDist = endLoc.distance(eyeLocation);

        displayRay(eyeLocation, dir, maxDist);
        SoundManager.playSoundToAll(player, Sound.GLASS, 1.0f, 0.8f);

        for (Player target : instance.players) {
            if (!isEnemy(target)) continue;

            Vector toTarget = target.getLocation().add(0, 1, 0).subtract(eyeLocation).toVector();
            double dist = toTarget.dot(dir);
            if (dist <= 0 || dist >= maxDist) continue;

            Location closest = eyeLocation.clone().add(dir.clone().multiply(dist));
            if (closest.distanceSquared(target.getLocation().add(0, 1, 0)) > FREEZE_RAY_HIT_RADIUS * FREEZE_RAY_HIT_RADIUS)
                continue;

            hitWithFreezeRay(target);
        }
    }

    /** A wider beam: at each step a particle in the middle and four around it. */
    private void displayRay(Location eyeLocation, Vector dir, double maxDist) {
        // Two vectors perpendicular to the ray direction
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0));
        if (right.lengthSquared() < 0.001) right = new Vector(1, 0, 0); // straight up/down aim
        right.normalize();
        Vector up = dir.clone().crossProduct(right).normalize();

        BlockTexture ice = new BlockTexture(Material.ICE);
        for (double t = 1; t < maxDist; t += 0.5) {
            Location center = eyeLocation.clone().add(dir.clone().multiply(t));

            ParticleEffect.BLOCK_CRACK.display(center, 0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
            ParticleEffect.BLOCK_CRACK.display(center.clone().add(right.clone().multiply(FREEZE_RAY_BEAM_RADIUS)), 0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
            ParticleEffect.BLOCK_CRACK.display(center.clone().add(right.clone().multiply(-FREEZE_RAY_BEAM_RADIUS)), 0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
            ParticleEffect.BLOCK_CRACK.display(center.clone().add(up.clone().multiply(FREEZE_RAY_BEAM_RADIUS)), 0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
            ParticleEffect.BLOCK_CRACK.display(center.clone().add(up.clone().multiply(-FREEZE_RAY_BEAM_RADIUS)), 0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
        }
    }

    private void hitWithFreezeRay(Player target) {
        target.addPotionEffect(rayFreeze);

        EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(
                player, target, DamageCause.CUSTOM, FREEZE_RAY_DAMAGE);
        Bukkit.getPluginManager().callEvent(damageEvent);
        if (damageEvent.isCancelled()) return;

        target.damage(damageEvent.getDamage(), player);

        // Force the last damage cause to be the Ice player who hit them with Freeze Ray.
        // This helps kill credit / death logic detect the correct attacker.
        target.setLastDamageCause(damageEvent);

        Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(),
                () -> target.playSound(target.getLocation(), Sound.GLASS, 1.0f, 0.75f), 5L);

        spawnIceHitEffect(target);
    }

    private void freezeBombEffect() {
        List<Player> nearby = new ArrayList<>();

        for (Entity en : player.getNearbyEntities(FREEZE_BOMB_RANGE, FREEZE_BOMB_RANGE, FREEZE_BOMB_RANGE))
            if (en instanceof Player && isEnemy((Player) en))
                nearby.add((Player) en);

        if (nearby.isEmpty()) {
            freezeBombAbility.sendCustomMessage("&c&l(!) &rNo nearby players have been found!");
            return;
        }

        for (Player target : nearby) {
            target.addPotionEffect(bombFreeze);
            Firework firework = target.getWorld().spawn(target.getEyeLocation(), Firework.class);
            displayFirework(firework, FireworkEffect.builder().flicker(true)
                    .withColor(Color.WHITE)
                    .with(FireworkEffect.Type.BURST)
                    .build());
        }

        displayFirework(player.getWorld().spawn(player.getLocation(), Firework.class), FireworkEffect.builder().flicker(true)
                .withColor(Color.AQUA, Color.SILVER, Color.WHITE)
                .withFade(Color.SILVER)
                .with(FireworkEffect.Type.BALL_LARGE)
                .build());

        freezeBombAbility.sendCustomMessage("&2&l(!) &rYou have &b&lFrozen &rnearby players!");
        player.getInventory().clear(player.getInventory().getHeldItemSlot());
    }

    private void displayFirework(Firework firework, FireworkEffect effect) {
        FireworkMeta meta = firework.getFireworkMeta();
        meta.clearEffects();
        meta.addEffect(effect);
        firework.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), firework::detonate, 2L);
    }

    /**
     * Spawns a ring of ice-break particles around the hit player's head to give a
     * clear visual that they were struck by the Freeze Ray.
     */
    private void spawnIceHitEffect(Player target) {
        Location headLoc = target.getEyeLocation().add(0, 0.3, 0);
        BlockTexture iceTex = new BlockTexture(Material.ICE);
        int points = 12;
        double radius = 0.65;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;
            Location particleLoc = headLoc.clone().add(
                    radius * Math.cos(angle), 0, radius * Math.sin(angle));
            // offsetY > 0 makes shards fly upward slightly, like ice bursting apart
            ParticleEffect.BLOCK_CRACK.display(particleLoc, 0.0f, 0.25f, 0.0f, 0.08f, 3, iceTex);
        }
    }
}
