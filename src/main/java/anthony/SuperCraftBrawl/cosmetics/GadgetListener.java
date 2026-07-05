package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

/** Right-click behavior for the physical GADGET cosmetic items (Magic Broom, Paintball Gun,
 *  Melon) - not equip/unequip, which is handled by the Cosmetic equip system instead
 *  (see GadgetCosmetics in cosmetics.categories). */
public class GadgetListener implements Listener {

    private final Core main;

    public GadgetListener(Core main) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, main);
    }

    /*
     * This function deals with the Magic Broom cosmetic
     */
    @EventHandler
    public void magicBroom(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;

        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        PlayerData data = main.getDataManager().getPlayerData(player);
        GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

        if ((player.getWorld() == main.getLobbyWorld())
                || (i != null && (i.state == GameState.WAITING || i.state == GameState.ENDED))) {
            if (item != null && item.getType() == Material.WHEAT) {
                double boosterStrength = 2.0;
                Vector vel = player.getLocation().getDirection().multiply(boosterStrength);
                player.setVelocity(vel);
                data.magicbroom = 1;
            }
        }
    }

    /*
     * This function deals with the Melon cosmetic
     */
    @EventHandler
    public void melon(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        PlayerData data = main.getDataManager().getPlayerData(player);
        GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

        if ((player.getWorld() == main.getLobbyWorld()) || (i != null && i.state == GameState.WAITING)) {
            if (item != null && item.getType() == Material.MELON) {
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    if (data.melon > 0) {
                        data.melon--;
                        main.getDataManager().saveData(data);
                        String msg = main.color("&9&l(!) &rYou have &e" + data.melon + " melons");
                        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
                                (byte) 2);
                        CraftPlayer craft = (CraftPlayer) player;
                        craft.getHandle().playerConnection.sendPacket(packet);
                        player.playSound(player.getLocation(), Sound.EAT, 2, 1);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 110, 9), true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 110, 9), true);
                        if (data.melon == 0)
                            player.getInventory().clear(player.getInventory().getHeldItemSlot());
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void paintballGun(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;

        Player player = event.getPlayer();
        PlayerData data = main.getDataManager().getPlayerData(player);
        ItemStack item = event.getItem();
        GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

        if (item != null) {
            if (item.getType() == Material.GOLD_BARDING) {
                if ((player.getWorld() == main.getLobbyWorld())
                        || (i != null && i.state == GameState.WAITING)) {

                    if (data != null) {
                        if (data.paintball > 0) {
                            Snowball snowball = player.launchProjectile(Snowball.class);
                            snowball.setMetadata("paintball", new FixedMetadataValue(main, true));
                            data.paintball--;
                            main.getDataManager().saveData(data);

                            String msg = main.color("&9&l(!) &rYou have &e" + data.paintball + " paintballs");
                            PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
                                    (byte) 2);
                            CraftPlayer craft = (CraftPlayer) player;
                            craft.getHandle().playerConnection.sendPacket(packet);

                            player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
                        } else
                            player.sendMessage(main.color("&c&l(!) &rYou do not have anymore &ePaintballs &r:("));
                    }
                }
            }
        }
    }

    @EventHandler
    public void snowballHit(ProjectileHitEvent event) {
        Entity e = event.getEntity();
        Snowball s;

        if (e instanceof Snowball && e.hasMetadata("paintball")) {
            s = (Snowball) e;
            DyeColor col = DyeColor.values()[new Random().nextInt(DyeColor.values().length)];
            if (s.getShooter() instanceof Player) {
                Player p = (Player) s.getShooter();
                GameInstance i = main.getGameManager().GetInstanceOfPlayer(p);

                if (i != null && i.state == GameState.STARTED)
                    return;

                Block center = s.getLocation().getBlock();
                int x = center.getX();
                int z = center.getZ();
                if (center.getType() != Material.AIR) {
                    doTheWorkForMe(center, col);

                }

                int max = s.getLocation().getBlock().getY() + 1;
                int min = s.getLocation().getBlock().getY() - 1;
                Location loc;
                for (int y = min; y <= max; y++) {
                    loc = new Location(center.getWorld(), x + 1, y, z);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x, y, z);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x + 1, y, z + 1);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x, y, z + 1);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x - 1, y, z + 1);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x - 1, y, z);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x - 1, y, z);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x - 1, y, z - 1);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x, y, z - 1);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                    loc = new Location(center.getWorld(), x + 1, y, z - 1);
                    doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void randomizeColor(Block block, DyeColor color) {
        block.setData(color.getData());
    }

    @SuppressWarnings("deprecation")
    private void doTheWorkForMe(Block block, DyeColor color) {
        if (block.getType() != Material.AIR && block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST
                && block.getType() != Material.WALL_SIGN && block.getType() != Material.WOOL
                && block.getType() != Material.CHEST && block.getType() != Material.LONG_GRASS
                && block.getType() != Material.RED_ROSE && block.getType() != Material.DEAD_BUSH
                && block.getType() != Material.FLOWER_POT && block.getType() != Material.DOUBLE_PLANT
                && block.getType() != Material.BED_BLOCK && !(block.getState().getData() instanceof Door)
                && !(block.getState() instanceof InventoryHolder) && !(block.getState() instanceof Banner)
                && block.getType() != Material.SKULL && block.getType() != Material.SOIL
                && block.getType() != Material.SEA_LANTERN && block.getType() != Material.BEACON
                && block.getType() != Material.GLOWSTONE && block.getType() != Material.LADDER) {
            Material og = block.getType();
            byte data = block.getData();
            if (og == Material.WOOL) {
                randomizeColor(block, color);
                return;
            }
            Location loc = new Location(block.getWorld(), block.getX(), block.getY() + 1, block.getZ());

            if (!loc.getBlock().getType().isSolid() && loc.getBlock().getType() != Material.AIR
                    && loc.getBlock().getType() != Material.TORCH)
                return;

            Bukkit.getScheduler().runTaskLater(main, () -> {
                block.setType(og);
                block.setData(data);
            }, 20 * 5L);
            block.setType(Material.WOOL);
            randomizeColor(block, color);
        }
    }
}
