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
        if (!isRightClick(event)) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.WHEAT) {
            return;
        }

        if (!canUseGadget(player)) return;

        PlayerData data = main.getDataManager().getPlayerData(player);

            double boosterStrength = 2.0;
            Vector vel = player.getLocation().getDirection().multiply(boosterStrength);
            player.setVelocity(vel);
            data.magicbroom = 1;
    }

    /*
     * This function deals with the Melon cosmetic
     */
    @EventHandler
    public void melon(PlayerInteractEvent event) {
        if (!isRightClick(event)) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.MELON) {
            return;
        }

        if (!canUseGadget(player)) {
            return;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        PlayerData data = main.getDataManager().getPlayerData(player);

        if (data.melon <= 0) {
            return;
        }

        data.melon--;
        main.getDataManager().saveData(data);

        sendActionBar(player, "&9&l(!) &rYou have &e" + data.melon + " melons");

        player.playSound(player.getLocation(), Sound.EAT, 2, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 110, 9), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 110, 9), true);

        if (data.melon == 0) {
            player.getInventory().clear(player.getInventory().getHeldItemSlot());
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void paintballGun(PlayerInteractEvent event) {
        if (!isRightClick(event)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.GOLD_BARDING) {
            return;
        }

        if (!canUseGadget(player)) {
            return;
        }

        PlayerData data = main.getDataManager().getPlayerData(player);

        if (data.paintball <= 0) {
            player.sendMessage(main.color("&c&l(!) &rYou do not have anymore &ePaintballs &r:("));
            return;
        }

        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setMetadata("paintball", new FixedMetadataValue(main, true));

        data.paintball--;
        main.getDataManager().saveData(data);

        sendActionBar(player, "&9&l(!) &rYou have &e" + data.paintball + " paintballs");

        player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
    }

    @EventHandler
    public void snowballHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Snowball) || !entity.hasMetadata("paintball")) {
            return;
        }

        Snowball snowball = (Snowball) entity;

        if (!(snowball.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) snowball.getShooter();

        GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

        // Do not allow paintballs to modify blocks during active games.
        if (game != null && game.state == GameState.STARTED) {
            return;
        }

        DyeColor color = DyeColor.values()[new Random().nextInt(DyeColor.values().length)];

        Block center = snowball.getLocation().getBlock();

        // paints 3x3 square
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    paintBlock(
                            center.getRelative(x, y, z),
                            color
                    );
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

    private boolean isRightClick(PlayerInteractEvent event) {
        Action action = event.getAction();

        return action.name().contains("RIGHT_CLICK");
    }

    private boolean canUseGadget(Player player) {
        return main.isInLobby(player);
    }

    private boolean isPaintable(Block block) {
        Material type = block.getType();

        return type != Material.AIR
                && type != Material.SIGN
                && type != Material.SIGN_POST
                && type != Material.WALL_SIGN
                && type != Material.CHEST
                && type != Material.LONG_GRASS
                && type != Material.RED_ROSE
                && type != Material.DEAD_BUSH
                && type != Material.FLOWER_POT
                && type != Material.DOUBLE_PLANT
                && type != Material.BED_BLOCK
                && type != Material.SKULL
                && type != Material.SOIL
                && type != Material.SEA_LANTERN
                && type != Material.BEACON
                && type != Material.GLOWSTONE
                && type != Material.LADDER
                && !(block.getState().getData() instanceof Door)
                && !(block.getState() instanceof InventoryHolder)
                && !(block.getState() instanceof Banner);
    }

    @SuppressWarnings("deprecation")
    private void paintBlock(Block block, DyeColor color) {
        Material type = block.getType();

        if (!isPaintable(block)) {
            return;
        }

        if (type == Material.WOOL) {
            block.setData(color.getData());
            return;
        }

        Material originalType = block.getType();
        byte originalData = block.getData();

        Location above = block.getRelative(0, 1, 0).getLocation();

        if (!above.getBlock().getType().isSolid()
                && above.getBlock().getType() != Material.AIR
                && above.getBlock().getType() != Material.TORCH) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(main, () -> {
            block.setType(originalType);
            block.setData(originalData);
        }, 20 * 5L);

        block.setType(Material.WOOL);
        block.setData(color.getData());
    }

    private void sendActionBar(Player player, String message) {
        String formatted = main.color(message);

        PacketPlayOutChat packet = new PacketPlayOutChat(
                IChatBaseComponent.ChatSerializer.a(
                        "{\"text\":\"" + formatted + "\"}"
                ),
                (byte) 2
        );

        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }
}
