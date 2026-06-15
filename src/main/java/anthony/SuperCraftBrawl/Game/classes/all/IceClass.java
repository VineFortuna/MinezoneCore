package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.ArrayList;
import java.util.List;

public class IceClass extends BaseClass {

    private int cooldownSec;

    public IceClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZlNDI5YzYwOTMyZWJjMzY2ZTc5MWE0MmUxODZhZjg4OGRlMDhlNWQ4ZWI4YWM2ZjViNmY0ZDQ0MGRiNDg2YyJ9fX0=",
                6,
                "92B9FE",
                "92B9FE",
                "92B9FE"
        );
    }

    @Override
    public ClassType getType() {
        return ClassType.Ice;
    }

    @Override
    public void setArmor(EntityEquipment playerEquip) {
        setArmorNew(playerEquip);
    }

    @Override
    public ItemStack getAttackWeapon() {
        ItemStack item = new ItemStack(Material.STONE_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.spigot().setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void SetNameTag() {}

    @Override
    public void SetItems(Inventory playerInv) {
        ice.startTime = System.currentTimeMillis() - 100000;
        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1,
                ItemHelper.setDetails(new ItemStack(Material.WOOL),
                        instance.getGameManager().getMain().color("&bFreeze Ray"), "",
                        instance.getGameManager().getMain().color("&7Right click to shoot a player with freeze ray!")));
        playerInv.setItem(2,
                ItemHelper.setDetails(new ItemStack(Material.PACKED_ICE),
                        instance.getGameManager().getMain().color("&bFreeze Bomb"), "",
                        instance.getGameManager().getMain().color("&7Right click to freeze nearby enemies!")));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1));
    }

    @Override
    public void Tick(int gameTicks) {
        if (!(player.getActivePotionEffects().contains(PotionEffectType.WEAKNESS)))
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1));

        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Ice
                && instance.classes.get(player).getLives() > 0) {
            this.cooldownSec = (10 * 1000 - ice.getTime()) / 1000 + 1;

            if (ice.getTime() < 10 * 1000) {
                String msg = instance.getGameManager().getMain()
                        .color("&b&lFreeze Ray &rregenerates in: &e" + this.cooldownSec + "s");
                getActionBarManager().setActionBar(player, "ice.cooldown", msg, 2);
            } else {
                String msg = instance.getGameManager().getMain().color("&rYou can use &b&lFreeze Ray");
                getActionBarManager().setActionBar(player, "ice.cooldown", msg, 2);
            }
        }
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null) {
            if (item.getType() == Material.WOOL
                    && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                if (ice.getTime() < 10000) {
                    int seconds = (10000 - ice.getTime()) / 1000 + 1;
                    event.setCancelled(true);
                    player.sendMessage(instance.color("&c&l(!) &rYour &b&lFreeze Ray &ris still regenerating for &a" + seconds + "s"));
                } else {
                    ice.restart();
                    int range = 30;
                    Location endLoc = player.getEyeLocation();
                    BlockIterator b = new BlockIterator(player.getEyeLocation(), 0, range);

                    while (b.hasNext()) {
                        Block block = b.next();
                        endLoc = block.getLocation();
                        if (block.getType().isSolid())
                            break;
                    }

                    Vector dir = player.getEyeLocation().getDirection();
                    double maxDist = endLoc.distance(player.getEyeLocation());

                    // Compute two vectors perpendicular to the ray direction for the wider beam
                    Vector right = dir.clone().crossProduct(new Vector(0, 1, 0));
                    if (right.lengthSquared() < 0.001) right = new Vector(1, 0, 0); // fallback for straight-up/down aim
                    right.normalize();
                    Vector up2 = dir.clone().crossProduct(right).normalize();
                    double beamRadius = 0.35;

                    // Wider beam: center + 4 surrounding particles at each step
                    for (double t = 1; t < maxDist; t += 0.5) {
                        Location center = player.getEyeLocation().add(dir.clone().multiply(t));
                        BlockTexture ice = new BlockTexture(Material.ICE);

                        ParticleEffect.BLOCK_CRACK.display(center,                                                    0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
                        ParticleEffect.BLOCK_CRACK.display(center.clone().add(right.clone().multiply( beamRadius)), 0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
                        ParticleEffect.BLOCK_CRACK.display(center.clone().add(right.clone().multiply(-beamRadius)), 0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
                        ParticleEffect.BLOCK_CRACK.display(center.clone().add(up2.clone().multiply( beamRadius)),   0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
                        ParticleEffect.BLOCK_CRACK.display(center.clone().add(up2.clone().multiply(-beamRadius)),   0.0F, 0.0F, 0.0F, 0.0F, 1, ice);
                    }

                    for (Player p : instance.players) {
                        SoundManager.playSoundToAll(player, Sound.GLASS, 1.0f, 0.8f);
                        if (p != player) {
                            Vector d = p.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
                            double dist = d.dot(dir);

                            if (dist < maxDist) {
                                Location closest = player.getEyeLocation().add(dir.clone().multiply(dist));

                                if (closest.distanceSquared(p.getLocation().add(0, 1, 0)) <= 1.5 * 1.5) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 4 * 20, 1));
                                    EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.VOID, 4.5);
                                    instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
                                    p.damage(4.5, player);

                                    Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
                                        p.playSound(p.getLocation(), Sound.GLASS, 1.0f, 0.75f);
                                    }, 5L);
                                    spawnIceHitEffect(p);
                                }
                            }
                        }
                    }
                }
            } else if (item.getType() == Material.PACKED_ICE
                    && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                List<Player> nearby = new ArrayList<>();

                for (Entity en : player.getNearbyEntities(10.0, 10.0, 10.0)) {
                    if (en instanceof Player) {
                        Player target = (Player) en;
                        if (!target.equals(player) && target.getGameMode() != GameMode.SPECTATOR) {
                            nearby.add(target);
                        }
                    }
                }

                if (nearby.isEmpty()) {
                    player.sendMessage(
                            instance.getGameManager().getMain().color("&c&l(!) &rNo nearby players have been found :("));
                    return;
                }

                for (Player p : nearby) {
                    if (p.getGameMode() != GameMode.SPECTATOR) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 4));
                        Firework firework = p.getWorld().spawn(p.getEyeLocation(), Firework.class);
                        FireworkEffect effect = FireworkEffect.builder().flicker(true)
                                .withColor(Color.WHITE)
                                .with(FireworkEffect.Type.BURST)
                                .build();
                        FireworkMeta meta = firework.getFireworkMeta();
                        meta.clearEffects();
                        meta.addEffect(effect);
                        firework.setFireworkMeta(meta);
                        Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), firework::detonate, 2L);
                    }
                }

                fireworkEffect(player);

                player.sendMessage(
                        instance.getGameManager().getMain().color("&2&l(!) &rYou have &b&lFrozen &rnearby players!"));
                player.getInventory().clear(player.getInventory().getHeldItemSlot());
            }
        }
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

    public void fireworkEffect(Player player) {
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkEffect effect = FireworkEffect.builder().flicker(true)
                .withColor(Color.AQUA, Color.SILVER, Color.WHITE)
                .withFade(Color.SILVER)
                .with(FireworkEffect.Type.BALL_LARGE)
                .build();
        FireworkMeta meta = firework.getFireworkMeta();
        meta.clearEffects();
        meta.addEffect(effect);
        firework.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), firework::detonate, 2L);
    }
}