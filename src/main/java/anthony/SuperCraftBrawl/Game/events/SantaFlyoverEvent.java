package anthony.SuperCraftBrawl.Game.events;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class SantaFlyoverEvent {

    private final GameInstance game;
    private final Random random = new Random();

    // Lower altitude for better visibility
    private final int santaY = 18;

    public SantaFlyoverEvent(GameInstance game) {
        this.game = game;
    }

    public void startEvent(int intervalSeconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (game.state != GameState.STARTED) return;
                startSingleFlyover();
            }
        }.runTaskTimer(game.getGameManager().getMain(),
                intervalSeconds * 20L,
                intervalSeconds * 20L);
    }

    private void startSingleFlyover() {

        World world = game.getMapWorld();
        Location center = game.GetRespawnLoc().clone();

        double bx = game.boundsX + 30;
        double bz = game.boundsZ + 30;

        int edge = random.nextInt(4);

        Location start;
        Location end;

        switch (edge) {

            case 0: // NORTH → SOUTH
                start = center.clone().add(randomBetween(-game.boundsX, game.boundsX), santaY, -bz);
                end   = center.clone().add(randomBetween(-game.boundsX, game.boundsX), santaY, bz);
                break;

            case 1: // SOUTH → NORTH
                start = center.clone().add(randomBetween(-game.boundsX, game.boundsX), santaY, bz);
                end   = center.clone().add(randomBetween(-game.boundsX, game.boundsX), santaY, -bz);
                break;

            case 2: // EAST → WEST
                start = center.clone().add(bx, santaY, randomBetween(-game.boundsZ, game.boundsZ));
                end   = center.clone().add(-bx, santaY, randomBetween(-game.boundsZ, game.boundsZ));
                break;

            default: // WEST → EAST
                start = center.clone().add(-bx, santaY, randomBetween(-game.boundsZ, game.boundsZ));
                end   = center.clone().add(bx, santaY, randomBetween(-game.boundsZ, game.boundsZ));
                break;
        }

        // Forced center waypoint (inner ~30% of map)
        Location centerWaypoint = center.clone().add(
                randomBetween(-game.boundsX * 0.15, game.boundsX * 0.15),
                santaY,
                randomBetween(-game.boundsZ * 0.15, game.boundsZ * 0.15)
        );

        // Spawn Santa
        ArmorStand santa = world.spawn(start, ArmorStand.class);
        santa.setVisible(false);
        santa.setGravity(false);
        santa.setSmall(true);

        // Santa head
        ItemStack santaHead = ItemHelper.createSkullTexture(
                "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19"
        );
        santa.getEquipment().setHelmet(santaHead);

        // Red suit
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack legs  = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        LeatherArmorMeta meta;

        meta = (LeatherArmorMeta) chest.getItemMeta();
        meta.setColor(Color.RED);
        chest.setItemMeta(meta);

        meta = (LeatherArmorMeta) legs.getItemMeta();
        meta.setColor(Color.RED);
        legs.setItemMeta(meta);

        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(Color.RED);
        boots.setItemMeta(meta);

        santa.getEquipment().setChestplate(chest);
        santa.getEquipment().setLeggings(legs);
        santa.getEquipment().setBoots(boots);

        // Two-phase movement vectors
        Vector toCenter = centerWaypoint.toVector().subtract(start.toVector()).normalize().multiply(0.65);
        Vector toEnd    = end.toVector().subtract(centerWaypoint.toVector()).normalize().multiply(0.65);

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (game.state != GameState.STARTED) {
                    santa.remove();
                    cancel();
                    return;
                }

                Location current = santa.getLocation();
                Vector move;

                // Phase 1 → center, Phase 2 → exit
                if (current.distance(centerWaypoint) > 1.5) {
                    move = toCenter;
                } else {
                    move = toEnd;
                }

                Location next = current.add(move);
                next.setDirection(move);
                santa.teleport(next);

                // Drop presents
                if (ticks % 8 == 0) {
                    dropPresent(santa.getLocation().clone().add(0, -1, 0));
                }

                // Jingle sound
                if (ticks % 15 == 0) {
                    world.playSound(santa.getLocation(), Sound.NOTE_PLING, 1f, 1.5f);
                }

                // End when past destination
                if (santa.getLocation().distance(end) < 2) {
                    santa.remove();
                    cancel();
                }

                ticks++;
            }

        }.runTaskTimer(game.getGameManager().getMain(), 0, 1);

        game.TellAll(ChatColorHelper.color("&2&l(!)&r Santa is flying across the map delivering presents!"));
    }

    private void dropPresent(Location loc) {
        World world = loc.getWorld();

        ItemStack present = game.getItemToDrop();
        Item drop = world.dropItemNaturally(loc, present);
        drop.setVelocity(new Vector(0, -0.15, 0));

        world.playEffect(loc, Effect.HAPPY_VILLAGER, 1);
    }

    private double randomBetween(double min, double max) {
        return min + (random.nextDouble() * (max - min));
    }
}
