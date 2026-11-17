package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.List;

public class ZombieVillagerClass extends BaseClass {

    private static final int STARTING_POTATOES = 7;
    private static final int POTATOES_PER_KILL = 2;
    private static final double POTATO_SPEED = 2.0;
    private static final double POTATO_RADIUS = 2.5;
    private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 80, 2);
    private final Ability potatoAbility = new Ability("&2&lPoisonous Potato", player);

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
    public ItemStack getAttackWeapon() {
        ItemStack item = ItemHelper.addEnchant(
                ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.ROTTEN_FLESH), "&6&lFlesh"),
                        Enchantment.DAMAGE_ALL, 3),
                Enchantment.KNOCKBACK, 2);
        return item;
    }

    @Override
    public void setItems(Inventory playerInv) {
        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1, getPotatoes(STARTING_POTATOES));
    }

    private ItemStack getPotatoes(int amount) {
        return ItemHelper.setDetails(new ItemStack(Material.POISONOUS_POTATO, amount),
                potatoAbility.getAbilityNameRightClickMessage(),
                "&7Throw at enemies to inflict:",
                "&7▸ &2&oPoison&r &e" + (poison.getAmplifier() + 1));
    }

    private ItemStack getBarrier() {
        return ItemHelper.setDetails(new ItemStack(Material.BARRIER),
                "&c&lOut of Potatoes!",
                "",
                "&7Each kill regenerates " + POTATOES_PER_KILL + " potatoes");
    }

    @Override
    public void killEvent(Player damagerPlayer) {
        if (instance.classes.containsKey(damagerPlayer) && !checkIfDead(player, instance)) {
            damagerPlayer.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &rYou got a kill and gained &e"
                    + POTATOES_PER_KILL + " &rextra &2Poisonous Potatoes"));

            // Replace the barrier if there is one, or add to the potatoes left
            ItemStack current = damagerPlayer.getInventory().getItem(1);
            if (current == null || current.getType() != Material.POISONOUS_POTATO)
                damagerPlayer.getInventory().setItem(1, getPotatoes(POTATOES_PER_KILL));
            else
                damagerPlayer.getInventory().addItem(getPotatoes(POTATOES_PER_KILL));
        }
    }

    @Override
    public void useItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null) {
            if (item.getType() == Material.POISONOUS_POTATO
                    && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);

                int amount = item.getAmount();
                amount--;
                item.setAmount(amount);

                if (amount <= 0) {
                    player.getInventory().setItem(1, getBarrier());
                }

                ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onHit(Player hit) {
                        if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                            Location hitLoc = this.getBaseProj().getEntity().getLocation();
                            player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

                            List<Player> affectedPlayers = new ArrayList<Player>();

                            for (Player gamePlayer : this.getNearby(POTATO_RADIUS)) {
                                if (instance.duosMap != null) {
                                    if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
                                        gamePlayer.addPotionEffect(poison);
                                        affectedPlayers.add(gamePlayer);
                                    }
                                } else if (gamePlayer != player) {
                                    gamePlayer.addPotionEffect(poison);
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
                        player.getLocation().getDirection().multiply(POTATO_SPEED)
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