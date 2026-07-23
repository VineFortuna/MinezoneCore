package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.List;

public class ZombieVillagerClass extends BaseClass {

    public ZombieVillagerClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ1YzExZTAzMjcwMzU2NDljYTA2MDBlZjkzODkwMGUyNWZkMWUzODAxNzQyMmJjOTc0MGU0Y2RhMmNiYTg5MiJ9fX0=",
                "7F5E58",
                "7F5E58",
                "4D6929",
                6,
                "ZombieVillager"
        );
    }

    @Override
    public ClassType getType() {
        return ClassType.ZombieVillager;
    }

    @Override
    public void setArmor(EntityEquipment playerEquip) {
        setArmorNew(playerEquip);
    }

    @Override
    public ItemStack getAttackWeapon() {
        ItemStack item = ItemHelper.addEnchant(
                ItemHelper.addEnchant(new ItemStack(Material.ROTTEN_FLESH), Enchantment.DAMAGE_ALL, 3),
                Enchantment.KNOCKBACK, 2);
        return item;
    }

    @Override
    public void SetNameTag() {}

    @Override
    public void SetItems(Inventory playerInv) {
        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1,
                ItemHelper.setDetails(new ItemStack(Material.POISONOUS_POTATO, 7), "", "",
                        instance.getGameManager().getMain().color("&7Throw at enemies to inflict:"),
                        instance.getGameManager().getMain().color("&7▶ &2&oPoison&r &e3")));
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null) {
            if (item.getType() == Material.POISONOUS_POTATO
                    && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);

                int amount = item.getAmount();
                amount--;
                item.setAmount(amount);

                if (amount <= 0) {
                    player.getInventory().clear(player.getInventory().getHeldItemSlot());
                }

                ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onHit(Player hit) {
                        if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                            Location hitLoc = this.getBaseProj().getEntity().getLocation();
                            player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

                            List<Player> affectedPlayers = new ArrayList<Player>();

                            for (Player gamePlayer : this.getNearby(2.5)) {
                                if (instance.duosMap != null) {
                                    if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
                                        gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 2));
                                        affectedPlayers.add(gamePlayer);
                                    }
                                } else if (gamePlayer != player) {
                                    gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 2));
                                    affectedPlayers.add(gamePlayer);
                                }
                            }

                            if (hit == null) {
                                // Hit the ground — green radius burst
                                spawnGroundSplash(hitLoc);

                                // Also show the green ring on every player actually infected by the splash radius
                                for (Player affected : affectedPlayers) {
                                    spawnPlayerHitRing(affected);
                                }
                            } else {
                                // Direct player hit — instant green ring around their eyes
                                spawnPlayerHitRing(hit);
                            }

                            for (Player gamePlayer : instance.players) {
                                gamePlayer.playSound(hitLoc, Sound.SPLASH2, 2, 1);
                                gamePlayer.playEffect(hitLoc, Effect.SPLASH, 1);
                            }
                        }
                    }
                }, new ItemStack(Material.POISONOUS_POTATO));

                instance.getGameManager().getProjManager().shootProjectile(
                        proj,
                        player.getEyeLocation(),
                        player.getLocation().getDirection().multiply(2.0D)
                );
            }
        }
    }

    private void spawnGroundSplash(Location hitLoc) {
        hitLoc.getWorld().playEffect(hitLoc, Effect.POTION_BREAK, 16388);
    }

    /**
     * Spawns two rings of green REDSTONE dust particles around the hit player's eyes
     * in a single instant — no duration, just a flash on impact.
     */
    private void spawnPlayerHitRing(Player hit) {
        Location eyeLoc = hit.getEyeLocation();
        int points = 16;
        double ringRadius = 0.7;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;

            // Lower ring at eye level
            Location lower = eyeLoc.clone().add(
                    ringRadius * Math.cos(angle), -0.1, ringRadius * Math.sin(angle));
            ParticleBuilder pbLow = new ParticleBuilder(ParticleEffect.REDSTONE);
            pbLow.setColor(new java.awt.Color(50, 205, 50));
            pbLow.setLocation(lower);
            pbLow.setOffset(0.0f, 0.0f, 0.0f);
            pbLow.display(instance.players);

            // Upper ring slightly above, offset so the two interleave
            double upperAngle = angle + (Math.PI / points);
            Location upper = eyeLoc.clone().add(
                    ringRadius * Math.cos(upperAngle), 0.25, ringRadius * Math.sin(upperAngle));
            ParticleBuilder pbHigh = new ParticleBuilder(ParticleEffect.REDSTONE);
            pbHigh.setColor(new java.awt.Color(50, 205, 50));
            pbHigh.setLocation(upper);
            pbHigh.setOffset(0.0f, 0.0f, 0.0f);
            pbHigh.display(instance.players);
        }
    }
}