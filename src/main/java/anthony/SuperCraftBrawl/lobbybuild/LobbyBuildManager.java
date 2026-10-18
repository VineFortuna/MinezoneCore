package anthony.SuperCraftBrawl.lobbybuild;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * LEGENDARY players are in creative mode in the lobby and can build there. Every block they place slowly cracks
 * and is gone a minute later. They can't build inside the important areas set with /setimportantarea, and
 * they can only take plain building blocks out of the creative menu. Opped players build normally.
 */
public class LobbyBuildManager implements Listener, CommandExecutor {

    private static final long DECAY_MILLIS = 60_000L;
    private static final String AREAS_PATH = "importantAreas";

    // Blocks that can't decay cleanly (move, melt, spread, span 2 blocks, hold items, push others, explode)
    private static final Set<Material> BLOCKED_MATERIALS = EnumSet.of(
            Material.TNT, Material.ICE, Material.PISTON_BASE, Material.PISTON_STICKY_BASE,
            Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.SPRUCE_DOOR, Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.BED_BLOCK,
            Material.DOUBLE_PLANT, Material.MOB_SPAWNER, Material.COMMAND, Material.BEACON,
            Material.ENDER_PORTAL_FRAME, Material.PORTAL, Material.ENDER_PORTAL, Material.DRAGON_EGG);

    private static class PlacedBlock {
        final Material type;
        final BlockState replaced; // what was there before (air, tall grass, a flower...), put back afterwards
        final long placedAt;
        final int animationId;
        int lastStage = -1;

        PlacedBlock(Material type, BlockState replaced, long placedAt, int animationId) {
            this.type = type;
            this.replaced = replaced;
            this.placedAt = placedAt;
            this.animationId = animationId;
        }
    }

    private static class Area {
        final String world;
        final int minX, minY, minZ, maxX, maxY, maxZ;

        Area(String world, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.world = world;
            this.minX = Math.min(x1, x2);
            this.minY = Math.min(y1, y2);
            this.minZ = Math.min(z1, z2);
            this.maxX = Math.max(x1, x2);
            this.maxY = Math.max(y1, y2);
            this.maxZ = Math.max(z1, z2);
        }

        boolean contains(Block block) {
            return block.getWorld().getName().equals(world)
                    && block.getX() >= minX && block.getX() <= maxX
                    && block.getY() >= minY && block.getY() <= maxY
                    && block.getZ() >= minZ && block.getZ() <= maxZ;
        }

        String serialize() {
            return world + "," + minX + "," + minY + "," + minZ + "," + maxX + "," + maxY + "," + maxZ;
        }

        String describe() {
            return "(" + minX + ", " + minY + ", " + minZ + ") to (" + maxX + ", " + maxY + ", " + maxZ + ")";
        }
    }

    private final Core main;
    private final Map<Location, PlacedBlock> placedBlocks = new HashMap<>();
    private final List<Area> importantAreas = new ArrayList<>();
    private int nextAnimationId = Integer.MAX_VALUE; // fake entity ids for the crack animation, counting down

    public LobbyBuildManager(Core main) {
        this.main = main;
        loadAreas();

        new BukkitRunnable() {
            @Override
            public void run() {
                tickDecay();
            }
        }.runTaskTimer(main, 10L, 10L);
    }

    // ===== Gamemode =====

    public boolean hasLobbyCreative(Player player) {
        return main.getRankManager().getRank(player) == Rank.LEGENDARY;
    }

    /** Creative for LEGENDARY players, adventure for everyone else. */
    public GameMode getLobbyGameMode(Player player) {
        return hasLobbyCreative(player) ? GameMode.CREATIVE : GameMode.ADVENTURE;
    }

    /** A LEGENDARY player building under the lobby rules - ops aren't restricted. */
    public boolean isLobbyBuilder(Player player) {
        return !player.isOp() && player.getGameMode() == GameMode.CREATIVE && hasLobbyCreative(player)
                && main.isInLobby(player);
    }

    // ===== Building =====

    // Runs after PlayerListener has cancelled every non-op placement
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isLobbyBuilder(player)) return;

        Block block = event.getBlockPlaced();
        if (!isAllowedBlock(block.getType()) || block.getState() instanceof InventoryHolder) {
            event.setCancelled(true);
            player.sendMessage(main.color("&c&l(!) &rYou can't place that block in the lobby."));
            return;
        }
        if (isInImportantArea(block)) {
            event.setCancelled(true);
            player.sendMessage(main.color("&c&l(!) &rYou can't build here!"));
            return;
        }

        event.setCancelled(false);

        // A slab placed onto a placed slab turns it into a double slab - keep what was there originally
        PlacedBlock previous = placedBlocks.get(block.getLocation());
        BlockState replaced = previous != null ? previous.replaced : event.getBlockReplacedState();
        int animationId = previous != null ? previous.animationId : nextAnimationId--;
        placedBlocks.put(block.getLocation(), new PlacedBlock(block.getType(), replaced, System.currentTimeMillis(), animationId));
    }

    // Lets anyone who can break blocks knock down a placed block early; lobby blocks stay protected
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        PlacedBlock placed = placedBlocks.remove(block.getLocation());
        if (placed == null) return;

        event.setCancelled(false);
        sendCrack(block, placed, -1);

        // Put back whatever the block replaced once the break has gone through
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() == Material.AIR) restore(placed);
            }
        }.runTask(main);
    }

    // Grass covered by a placed block would die into dirt and stay that way after the block is gone
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent event) {
        Material type = event.getBlock().getType();
        if ((type == Material.GRASS || type == Material.MYCEL)
                && placedBlocks.containsKey(event.getBlock().getRelative(BlockFace.UP).getLocation()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (isLobbyBuilder(event.getPlayer())) event.setCancelled(true);
    }

    /*
     * PlayerListener cancels every inventory click for non-ops. Builders get creative clicks back so they can grab
     * blocks and move them around, but only plain building blocks (no eggs, buckets, TNT, etc.), and the items
     * the plugin gave them (lobby items) stay locked in place.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreativeClick(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player) || !isLobbyBuilder((Player) event.getWhoClicked())) return;

        ItemStack placing = event.getCursor();
        ItemStack replacing = event.getCurrentItem();
        boolean allowed = !isPluginItem(replacing) && !isPluginItem(placing)
                && (isEmpty(placing) || (placing.getType().isBlock() && isAllowedBlock(placing.getType())));
        event.setCancelled(!allowed);
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    // Items the plugin hands out all have a custom name or lore; blocks from the creative menu don't
    private static boolean isPluginItem(ItemStack item) {
        return !isEmpty(item) && item.hasItemMeta()
                && (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasLore());
    }

    private static boolean isAllowedBlock(Material type) {
        return type.isSolid() && !type.hasGravity() && !BLOCKED_MATERIALS.contains(type);
    }

    // ===== Decay =====

    private void tickDecay() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Location, PlacedBlock>> it = placedBlocks.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Location, PlacedBlock> entry = it.next();
            Block block = entry.getKey().getBlock();
            PlacedBlock placed = entry.getValue();

            if (block.getType() != placed.type) { // already gone or replaced by something else
                sendCrack(block, placed, -1);
                if (block.getType() == Material.AIR) restore(placed);
                it.remove();
                continue;
            }

            long age = now - placed.placedAt;
            if (age >= DECAY_MILLIS) {
                sendCrack(block, placed, -1);
                playBreakEffect(block);
                restore(placed);
                it.remove();
                continue;
            }

            int stage = (int) (age * 10 / DECAY_MILLIS); // 0-9
            if (stage != placed.lastStage) {
                placed.lastStage = stage;
                sendCrack(block, placed, stage);
            }
        }
    }

    /** Shows crack stage 0-9 on the block to everyone in its world; -1 clears it. */
    private void sendCrack(Block block, PlacedBlock placed, int stage) {
        PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(placed.animationId,
                new BlockPosition(block.getX(), block.getY(), block.getZ()), stage);
        for (Player player : block.getWorld().getPlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    private static void restore(PlacedBlock placed) {
        placed.replaced.update(true, false);
    }

    // Same block-break particles and sounds as the Enderman class's thrown blocks
    @SuppressWarnings("deprecation")
    private static void playBreakEffect(Block block) {
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        ParticleEffect.BLOCK_CRACK.display(center, 0.35f, 0.35f, 0.35f, 0.08f, 18,
                new BlockTexture(block.getType(), block.getData()));
        block.getWorld().playSound(center, getBreakSound(block.getType()), 1.0f, 1.0f);
    }

    private static Sound getBreakSound(Material material) {
        String name = material.name();

        if (name.contains("GLASS")) return Sound.GLASS;
        if (name.contains("WOOD") || name.contains("LOG") || name.contains("PLANK")
                || name.contains("FENCE") || name.contains("CHEST")) return Sound.DIG_WOOD;
        if (name.contains("WOOL") || name.contains("CARPET")) return Sound.DIG_WOOL;
        if (name.contains("SAND")) return Sound.DIG_SAND;
        if (name.contains("GRAVEL")) return Sound.DIG_GRAVEL;
        if (name.contains("SNOW") || name.contains("ICE")) return Sound.DIG_SNOW;
        if (name.contains("GRASS") || name.contains("DIRT") || name.contains("SOIL")
                || name.contains("LEAVES")) return Sound.DIG_GRASS;
        return Sound.DIG_STONE;
    }

    /** Puts every placed block back to what was there before (on plugin disable, so none get saved into the lobby). */
    public void clearPlacedBlocks() {
        for (Map.Entry<Location, PlacedBlock> entry : placedBlocks.entrySet()) {
            Block block = entry.getKey().getBlock();
            if (block.getType() == entry.getValue().type || block.getType() == Material.AIR) restore(entry.getValue());
        }
        placedBlocks.clear();
    }

    // ===== Important areas =====

    public boolean isInImportantArea(Block block) {
        for (Area area : importantAreas) {
            if (area.contains(block)) return true;
        }
        return false;
    }

    private void loadAreas() {
        importantAreas.clear();
        for (String line : main.getConfig().getStringList(AREAS_PATH)) {
            String[] parts = line.split(",");
            if (parts.length != 7) continue;
            try {
                importantAreas.add(new Area(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6])));
            } catch (NumberFormatException e) {
                main.getLogger().warning("Skipping bad important area: " + line);
            }
        }
    }

    private void saveAreas() {
        List<String> lines = new ArrayList<>();
        for (Area area : importantAreas) lines.add(area.serialize());
        main.getConfig().set(AREAS_PATH, lines);
        main.saveConfig();
    }

    /*
     * /setimportantarea <X> <Y> <Z>  - protects X/Y/Z blocks out in each direction from where you stand
     * /setimportantarea list         - list the important areas
     * /setimportantarea remove <#>   - remove one
     * /setimportantarea clear        - remove all of them
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp() && !player.hasPermission("scb.setimportantarea")) {
            player.sendMessage(main.color("&c&l(!) &rYou don't have permission to do this!"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            if (importantAreas.isEmpty()) {
                player.sendMessage(main.color("&e&l(!) &rThere are no important areas."));
                return true;
            }
            player.sendMessage(main.color("&e&l(!) &rImportant areas:"));
            for (int i = 0; i < importantAreas.size(); i++) {
                Area area = importantAreas.get(i);
                player.sendMessage(main.color("&e" + (i + 1) + ". &f" + area.world + " &7" + area.describe()));
            }
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            importantAreas.clear();
            saveAreas();
            player.sendMessage(main.color("&a&l(!) &rAll important areas have been removed."));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            try {
                Area removed = importantAreas.remove(Integer.parseInt(args[1]) - 1);
                saveAreas();
                player.sendMessage(main.color("&a&l(!) &rRemoved important area &e" + removed.describe()));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                player.sendMessage(main.color("&c&l(!) &rNo important area with that number. Try &e/" + label + " list"));
            }
            return true;
        }
        if (args.length != 3) {
            player.sendMessage(main.color("&c&l(!) &rIncorrect Usage! Try doing: &e/" + label + " <X> <Y> <Z>"));
            player.sendMessage(main.color("&7Protects X/Y/Z blocks out in each direction from you. Also: &e/" + label
                    + " list&7, &eremove <#>&7, &eclear"));
            return true;
        }

        int[] size = new int[3];
        try {
            for (int i = 0; i < 3; i++) {
                size[i] = Integer.parseInt(args[i]);
                if (size[i] < 0) throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            player.sendMessage(main.color("&c&l(!) &rX, Y and Z have to be whole numbers of 0 or more."));
            return true;
        }

        // The box reaches X/Y/Z blocks out in every direction from where the player stands
        Location center = player.getLocation();
        int x = center.getBlockX(), y = center.getBlockY(), z = center.getBlockZ();
        Area area = new Area(center.getWorld().getName(), x - size[0], y - size[1], z - size[2],
                x + size[0], y + size[1], z + size[2]);
        importantAreas.add(area);
        saveAreas();
        player.sendMessage(main.color("&a&l(!) &rImportant area &e#" + importantAreas.size() + " &rset from &e"
                + area.describe() + "&r. Legendary players can't build there."));
        return true;
    }
}
