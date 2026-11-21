package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.all.ReindeerFlight;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.ParticleHelper;
import anthony.SuperCraftBrawl.halloween.Halloween;
import anthony.util.ItemHelper;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

// For applying a custom head texture (optional, no extra classes)
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class WinEffects {

    /**
     * The slice of a game mode's match state WinEffects actually needs - lets this
     * class be shared by any gamemode (SCB's GameInstance, Flag Wars's FWGameInstance
     * via a small adapter, etc.) without depending on SCB's concrete GameInstance.
     */
    public interface Host {
        World getMapWorld();
        Core getPlugin();
        Location GetRespawnLoc();
        Vector getFloodCenter();
        double getFloodBoundsX();
        double getFloodBoundsZ();

        /** Whether the match is still in its win celebration (the Santa effect's Reindeer stops flying once it isn't). */
        default boolean isCelebrating() {
            return true;
        }

        /** Registers a task the game cancels itself if it's removed before the task finishes. Returns the task. */
        default BukkitTask trackTask(BukkitTask task) {
            return task;
        }
    }

    private Player player;
    private Host instance;
    private EnderDragon dragon;
    private boolean defaultEffect = false;
    private boolean rainEffect = false;
    private boolean floodEffect = false;
    private boolean treasureEffect = false;
    private boolean ritualEffect = false;
    private boolean presentEffect = false;
    private boolean winterEffect = false;

    private ReindeerFlight reindeerFlight; // Santa win effect

    private BukkitRunnable floodRunnable;
    private Boat floodBoat;

    private ArrayList<Item> fish = new ArrayList<>();
    private final List<Item> presents = new ArrayList<>();
    private final List<Bat> ritualBats = new ArrayList<>();

    // reference to spawned Herobrine ArmorStand for cleanup
    private ArmorStand herobrineNPC = null;

    // If you have a specific Herobrine skin, paste its Base64 texture VALUE here (signature not required for heads).
    // Example format inside the decoded value: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/<hash>"}}}
    private static final String HEROBRINE_TEXTURE_VALUE = ""; // <--- paste Base64 here (optional)

    public WinEffects(Player player, Host instance) {
        this.player = player;
        this.instance = instance;
    }

    public void checkWinEffect() {
        if (instance == null) return;

        Core main = instance.getPlugin();
        String id = main.getCosmeticManager().getEquippedId(player, CosmeticCategory.WIN_EFFECT);
        if (id == null) id = "default";

        switch (id) {
            case "ender_dragon": enderDragonEffect(); break;
            case "santa": santaEffect(); break;
            case "magic_broom": magicBroomEffect(); break;
            case "fish_rain": fishRainEffect(); break;
            case "flood": floodEffect(); break;
            case "treasure_hoard": treasureEffect(); break;
            // Someone can still have Ritual equipped from the hunt, and this reads the saved id, so it has to be off here too
            case "ritual": if (Halloween.ENABLED) ritualEffect(); else defaultEffect(); break;
            case "present_rain": presentRainEffect(); break;
            case "winter_wonderland": winterWonderlandEffect(); break;
            default: defaultEffect();
        }
    }

    // ALL WIN EFFECTS:

    private void enderDragonEffect() {
        World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        startFireworksRunnable(world);
        this.dragon = (EnderDragon) world.spawnEntity(this.player.getLocation(), EntityType.ENDER_DRAGON);
        this.dragon.setPassenger(this.player);
    }

    private void magicBroomEffect() {
        World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        startFireworksRunnable(world);
        ItemStack broom = ItemHelper.setDetails(new ItemStack(Material.WHEAT),"&2&lMagic Broom");
        player.getInventory().setItem(0, broom);
    }

    private ItemStack makeRed(ItemStack armour) { // FOR SANTA EFFECT
        LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
        lm.setColor(Color.RED);
        armour.setItemMeta(lm);
        return armour;
    }

    private void santaEffect() {
        World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        startFireworksRunnable(world);
        ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) playerskull.getItemMeta();
        meta.setOwner("Santa");
        meta.setDisplayName("");
        playerskull.setItemMeta(meta);

        player.getInventory().setHelmet(playerskull);
        player.getInventory().setChestplate(makeRed(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
                Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
        player.getInventory().setLeggings(makeRed(new ItemStack(Material.LEATHER_LEGGINGS)));
        player.getInventory().setBoots(makeRed(
                ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));

        // Santa's class Reindeer: flown (and sparkling) until the win effects are removed
        reindeerFlight = new ReindeerFlight(player, 0,
                () -> instance.isCelebrating() && player.getWorld() == instance.getMapWorld());
        instance.trackTask(reindeerFlight.start(instance.getPlugin()));
    }

    /** The Santa win effect's Reindeer (null for the other win effects). */
    public ReindeerFlight getReindeerFlight() {
        return reindeerFlight;
    }

    private void fireParticlesEffect() {
        // (left intentionally blank in your original)
    }

    public Location getItemRainLoc() {
        Random rand = new Random();
        int attempts = 0;
        Location respawnLoc = instance.GetRespawnLoc();
        while (true) {
            Location loc = respawnLoc.clone().add(rand.nextFloat() * 50 - 25, 35, rand.nextFloat() * 50 - 25);
            Location loc2 = loc.clone();
            while (true) {
                loc2.setY(loc2.getY() - 1);
                Material mat = loc2.getBlock().getType();
                if (mat.isSolid()) return loc;
                if (loc2.getY() < 40) break;
            }
            if (attempts > 100) return respawnLoc.add(0, 35, 0);
            attempts++;
        }
    }

    private void fishRainEffect() {
        this.rainEffect = true;
        World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        startFireworksRunnable(world);
        world.setStorm(true);
        world.setThundering(true);
        BukkitRunnable runnable = new BukkitRunnable() {
            int rep = 0;
            Random rand = new Random();
            @Override
            public void run() {
                if (rep == 240) {
                    this.cancel();
                } else {
                    int chance = rand.nextInt(4);
                    Item i = world.dropItem(getItemRainLoc(),
                            new ItemStack(Material.RAW_FISH, 1, (short) chance));
                    fish.add(i);
                }
                rep++;
            }
        };
        runnable.runTaskTimer(instance.getPlugin(), 0, 1);
    }

    // --- Present Rain (Battle Pass): fish rain, but with gift boxes ---

    /** Wool colours used as gift boxes: red, green, white, yellow. */
    private static final short[] GIFT_COLORS = { 14, 13, 0, 4 };

    private void presentRainEffect() {
        final World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        this.presentEffect = true;
        startFireworksRunnable(world, CHRISTMAS_PALETTE);

        BukkitRunnable runnable = new BukkitRunnable() {
            final Random rand = new Random();
            int rep = 0;

            @Override
            public void run() {
                if (rep >= 160 || !player.isOnline() || player.getWorld() != world) {
                    this.cancel();
                    return;
                }

                ItemStack gift = rand.nextInt(6) == 0
                        ? new ItemStack(Material.COOKIE)
                        : new ItemStack(Material.WOOL, 1, GIFT_COLORS[rand.nextInt(GIFT_COLORS.length)]);
                Item item = world.dropItem(getItemRainLoc(), gift);
                item.setPickupDelay(Integer.MAX_VALUE); // for looks only, nobody can pick them up
                presents.add(item);
                rep++;
            }
        };
        instance.trackTask(runnable.runTaskTimer(instance.getPlugin(), 0, 1));
    }

    // --- Winter Wonderland (Battle Pass): snow, a glittering particle Christmas tree and jingle bells ---

    private static final double TREE_HEIGHT = 5.0;
    private static final double TREE_RADIUS = 2.2;
    private static final int TREE_GROW_TICKS = 40;

    /** Jingle Bells: when each note plays (ticks into an 80 tick loop) and its note (0 = F#3, 12 = F#4, 24 = F#5). */
    private static final int[] JINGLE_AT = { 0, 5, 10, 20, 25, 30, 40, 45, 50, 57, 60 };
    private static final int[] JINGLE_NOTE = { 10, 10, 10, 10, 10, 10, 10, 13, 6, 8, 10 };

    private void winterWonderlandEffect() {
        final World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        this.winterEffect = true;
        startFireworksRunnable(world, CHRISTMAS_PALETTE);

        // The tree stands where the winner was standing when they won
        final Location treeBase = findGround(player.getLocation()).add(0, 1, 0);

        BukkitRunnable runnable = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= 240 || !player.isOnline() || player.getWorld() != world) {
                    this.cancel();
                    return;
                }

                // Snow falls around every player in the map, so it's visible wherever they are
                if (tick % 2 == 0) {
                    for (Player viewer : world.getPlayers()) {
                        Location above = viewer.getLocation().add(0, 5, 0);
                        ParticleHelper.playTo(viewer, above, EnumParticle.SNOW_SHOVEL, 7f, 2.5f, 7f, 0f, 25);
                    }
                }

                drawChristmasTree(treeBase, tick);
                playJingle(world, tick % 80);
                tick++;
            }
        };
        instance.trackTask(runnable.runTaskTimer(instance.getPlugin(), 0, 1));
    }

    /** One tick of the particle tree: it grows for the first couple of seconds, then twinkles. */
    private void drawChristmasTree(Location base, int tick) {
        Random rand = new Random();
        double grow = Math.min(1.0, tick / (double) TREE_GROW_TICKS);

        // Garland spiralling up the tree
        for (int k = 0; k < 3; k++) {
            double h = ((tick * 0.03) + k / 3.0) % 1.0; // how far up the tree, 0 - 1
            double angle = h * 6 * Math.PI + tick * 0.2;
            double radius = TREE_RADIUS * (1 - h) * grow;
            Location point = base.clone().add(Math.cos(angle) * radius, h * TREE_HEIGHT * grow, Math.sin(angle) * radius);
            ParticleHelper.dust(point, 255, 215, 0);
        }

        // Green foliage filling in the cone
        for (int k = 0; k < 4; k++) {
            double h = rand.nextDouble();
            double angle = rand.nextDouble() * 2 * Math.PI;
            double radius = TREE_RADIUS * (1 - h) * rand.nextDouble() * grow;
            Location point = base.clone().add(Math.cos(angle) * radius, h * TREE_HEIGHT * grow, Math.sin(angle) * radius);
            ParticleHelper.dust(point, 30, 160, 50);
        }

        // A red ornament somewhere on the surface
        double h = rand.nextDouble() * 0.85;
        double angle = rand.nextDouble() * 2 * Math.PI;
        double radius = TREE_RADIUS * (1 - h) * grow;
        ParticleHelper.dust(base.clone().add(Math.cos(angle) * radius, h * TREE_HEIGHT * grow, Math.sin(angle) * radius), 220, 30, 30);

        // Star on top
        if (tick % 3 == 0)
            ParticleHelper.play(base.clone().add(0, TREE_HEIGHT * grow + 0.2, 0), EnumParticle.FIREWORKS_SPARK, 0.15f, 0.15f, 0.15f, 0.02f, 3);
    }

    private void playJingle(World world, int loopTick) {
        for (int i = 0; i < JINGLE_AT.length; i++) {
            if (JINGLE_AT[i] != loopTick) continue;

            float pitch = (float) Math.pow(2.0, (JINGLE_NOTE[i] - 12) / 12.0);
            for (Player viewer : world.getPlayers())
                viewer.playSound(viewer.getLocation(), Sound.NOTE_PLING, 0.8f, pitch);
        }
    }

    private void floodEffect() {
        World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        this.floodEffect = true;
        startFireworksRunnable(world);
        world.setStorm(true);
        world.setThundering(true);

        Vector center = instance.getFloodCenter();
        double centerX = center.getX();
        double centerY = player.getLocation().clone().getY() - 5;
        double centerZ = center.getZ();
        double width  = instance.getFloodBoundsX();
        double length = instance.getFloodBoundsZ();

        this.floodBoat = (Boat) player.getWorld().spawnEntity(player.getLocation(), EntityType.BOAT);
        player.teleport(floodBoat.getLocation());
        floodBoat.setPassenger(player);

        this.floodRunnable = new BukkitRunnable() {
            int rep = 0;
            double y = centerY;
            @Override
            public void run() {
                // 9 reps (9s), same budget startFireworksRunnable uses - rises well past the player
                // and finishes on its own a second before FD's 10-second win-celebration cleanup runs,
                // instead of getting cut off mid-rise.
                if (rep == 9 || floodBoat == null || floodBoat.isDead()) {
                    this.cancel();
                } else {
                    for (int x = (int) (centerX - width); x <= centerX + width; x++) {
                        for (int z = (int) (centerZ - length); z <= centerZ + length; z++) { // <-- fixed condition
                            Block block = world.getBlockAt(x, (int) y, z);
                            Block blockBelow = block.getRelative(BlockFace.DOWN);
                            if (blockBelow.getType() != Material.AIR && block.getType() == Material.AIR) {
                                block.setType(Material.WATER);
                            }
                        }
                    }
                    y++;
                    floodBoat.teleport(floodBoat.getLocation().add(0, 1, 0));
                }
                rep++;
            }
        };
        floodRunnable.runTaskTimer(instance.getPlugin(), 0, 20);
    }

    public void treasureEffect() {
        final World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        startFireworksRunnable(world);
        final Random rand = new Random();
        final Location loc = player.getLocation();
        final List<Block> placedBlocks = new ArrayList<>();
        final int durationTicks = 200; // 10 seconds
        final double maxHeight = 5;

        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= durationTicks) {
                    cancel();
                    for (Block b : placedBlocks) if (b.getType() == Material.GOLD_BLOCK) b.setType(Material.AIR);
                }
                double rise = ((double) tick / durationTicks) * maxHeight;
                for (int i = 0; i < 3; i++) {
                    int dx = rand.nextInt(9) - 4;
                    int dz = rand.nextInt(9) - 4;
                    int baseY = loc.getBlockY() + (int) rise;
                    while (baseY > 0 && world.getBlockAt(loc.getBlockX() + dx, baseY, loc.getBlockZ() + dz).getType() == Material.AIR) {
                        baseY--;
                    }
                    Block target = world.getBlockAt(loc.getBlockX() + dx, baseY + 1, loc.getBlockZ() + dz);
                    if (target.getType() == Material.AIR) {
                        target.setType(Material.GOLD_BLOCK);
                        placedBlocks.add(target);
                        if (rand.nextInt(3) == 0) {
                            Location coinLoc = target.getLocation().add(0.5, 1.2, 0.5);
                            Material coinMat = rand.nextBoolean() ? Material.GOLD_NUGGET : Material.GOLD_INGOT;
                            Item item = world.dropItem(coinLoc, new ItemStack(coinMat, 1));
                            item.setPickupDelay(Integer.MAX_VALUE);
                            item.setVelocity(new Vector(
                                    (rand.nextDouble() - 0.5) * 0.8,
                                    0.6 + rand.nextDouble() * 0.4,
                                    (rand.nextDouble() - 0.5) * 0.8
                            ));
                            new BukkitRunnable() {
                                @Override public void run() { if (!item.isDead()) item.remove(); }
                            }.runTaskLater(instance.getPlugin(), 40L);
                        }
                    }
                }
                tick++;
            }
        }.runTaskTimer(instance.getPlugin(), 0L, 4L);
    }

    private void playRecord11Compat(World world, Location loc, float volume, float pitch) {
        try {
            Sound s = Sound.valueOf("RECORD_11");
            world.playSound(loc, s, volume, pitch);
            return;
        } catch (Throwable ignored) {}
        try {
            PacketPlayOutNamedSoundEffect pkt = new PacketPlayOutNamedSoundEffect("records.11", loc.getX(), loc.getY(),
                    loc.getZ(), volume, pitch);
            for (Player p : world.getPlayers()) {
                ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) p).getHandle().playerConnection.sendPacket(pkt);
            }
        } catch (Throwable ignored) {}
    }

    // --- Ritual effect: 3x3 Herobrine Totem (gold base, center netherrack+fire, 4 redstone torches) ---
    private void ritualEffect() {
        final World world = player.getWorld();
        if (world != instance.getMapWorld()) return;

        this.ritualEffect = true;

        startFireworksRunnable(world);

        // Night vibe
        final long prevTime = world.getTime();
        world.setTime(14000L);

        // Build totem one block above ground
        final List<Block> placed = new ArrayList<>();
        final Location baseCenter = findGround(player.getLocation()).add(0, 1, 0); // y = ground+1

        // 3x3 GOLD base at y
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block b = baseCenter.clone().add(dx, 0, dz).getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(Material.GOLD_BLOCK);
                    placed.add(b);
                }
            }
        }

        // Center NETHERRACK at y+1
        Block neth = baseCenter.clone().add(0, 1, 0).getBlock();
        if (neth.getType() == Material.AIR) {
            neth.setType(Material.NETHERRACK);
            placed.add(neth);
        }

        // Fire on top at y+2
        Block fire = baseCenter.clone().add(0, 2, 0).getBlock();
        if (fire.getType() == Material.AIR) {
            fire.setType(Material.FIRE);
            placed.add(fire);
        }

        // 4 redstone torches around the center (on the gold layer at y+1)
        int[][] torches = new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] off : torches) {
            Block t = baseCenter.clone().add(off[0], 1, off[1]).getBlock();
            if (t.getType() == Material.AIR) {
                t.setType(Material.REDSTONE_TORCH_ON);
                placed.add(t);
            }
        }

        // Lightning & audio on construction
        world.strikeLightningEffect(fire.getLocation());
        try { world.playSound(fire.getLocation(), Sound.ENDERMAN_SCREAM, 1.0f, 1.0f); } catch (Throwable ignored) {}

        // spooky disc 11 pitched up
        playRecord11Compat(world, baseCenter, 1.0f, 1.2f);

        // Spawn some bats nearby (default wandering)
        final int BAT_COUNT = 10;
        final Random rand = new Random();
        Location batCenter = baseCenter.clone().add(0.5, 2.0, 0.5);
        for (int i = 0; i < BAT_COUNT; i++) {
            Location spawn = batCenter.clone().add(
                    rand.nextDouble() * 6 - 3,
                    rand.nextDouble() * 2,
                    rand.nextDouble() * 6 - 3
            );
            Bat bat = (Bat) world.spawnEntity(spawn, EntityType.BAT);
            bat.setRemoveWhenFarAway(true);
            ritualBats.add(bat);
        }

        // === Spawn Herobrine armor stand (no name) 3 seconds later, centered, with lightning ===
        new BukkitRunnable() {
            @Override
            public void run() {
                Location top = baseCenter.clone().add(0.0, 3.2, 0.0); // perfectly centered above fire
                world.strikeLightningEffect(top);

                herobrineNPC = (ArmorStand) world.spawnEntity(top, EntityType.ARMOR_STAND);
                herobrineNPC.setCustomNameVisible(false);
                herobrineNPC.setArms(true);
                herobrineNPC.setBasePlate(false);
                herobrineNPC.setGravity(false);
                herobrineNPC.setSmall(false);
                herobrineNPC.setRemoveWhenFarAway(false);

                ItemStack head = ItemHelper.createSkullTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM1YmRkN2VmZjExYzg3ZDUyYTExM2MyZWZiNGNhNDU3NzVlNTY3MzVkYzRiMzhkN2ZhMWRiNzA4NDU4In19fQ");
                herobrineNPC.setHelmet(head);

                ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
                LeatherArmorMeta cm = (LeatherArmorMeta) chest.getItemMeta();
                cm.setColor(org.bukkit.Color.fromRGB(58, 175, 169)); // teal
                chest.setItemMeta(cm);

                ItemStack legs  = new ItemStack(Material.LEATHER_LEGGINGS);
                LeatherArmorMeta lm = (LeatherArmorMeta) legs.getItemMeta();
                lm.setColor(org.bukkit.Color.fromRGB(45, 66, 152)); // deep blue
                legs.setItemMeta(lm);

                ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
                LeatherArmorMeta bm = (LeatherArmorMeta) boots.getItemMeta();
                bm.setColor(org.bukkit.Color.fromRGB(40, 40, 40)); // dark grey
                boots.setItemMeta(bm);

                herobrineNPC.setChestplate(chest);
                herobrineNPC.setLeggings(legs);
                herobrineNPC.setBoots(boots);

                try { world.playSound(top, Sound.WITHER_SPAWN, 1.0f, 0.7f); } catch (Throwable ignored) {}
            }
        }.runTaskLater(instance.getPlugin(), 60L); // 3 seconds later

        // Cleanup after ~10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Bat b : ritualBats) if (b != null && !b.isDead()) b.remove();
                ritualBats.clear();

                if (herobrineNPC != null && !herobrineNPC.isDead()) {
                    herobrineNPC.remove();
                    herobrineNPC = null;
                }

                for (Block b : placed) {
                    Material t = b.getType();
                    if (t == Material.GOLD_BLOCK || t == Material.NETHERRACK || t == Material.FIRE || t == Material.REDSTONE_TORCH_ON) {
                        b.setType(Material.AIR);
                    }
                }

                world.setTime(prevTime);
                ritualEffect = false;
            }
        }.runTaskLater(instance.getPlugin(), 200L);
    }

    private Location findGround(Location start) {
        Location loc = start.clone();
        World w = loc.getWorld();
        int y = Math.min(255, Math.max(1, loc.getBlockY()));
        while (y > 1 && w.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) y--;
        return new Location(w, loc.getBlockX() + 0.5, y, loc.getBlockZ() + 0.5);
    }

    private void defaultEffect() {
        this.defaultEffect = true;
        if (this.defaultEffect) {
            World world = player.getWorld();
            if (world != instance.getMapWorld()) return;
            startFireworksRunnable(world);
        }
    }

    private static final org.bukkit.Color[] DEFAULT_PALETTE = new org.bukkit.Color[] {
            org.bukkit.Color.fromRGB(255, 165, 0),   // orange
            org.bukkit.Color.fromRGB(0, 0, 0),       // black
            org.bukkit.Color.fromRGB(128, 0, 128)    // purple
    };

    private static final org.bukkit.Color[] CHRISTMAS_PALETTE = new org.bukkit.Color[] {
            org.bukkit.Color.fromRGB(220, 30, 30),   // red
            org.bukkit.Color.fromRGB(30, 160, 50),   // green
            org.bukkit.Color.fromRGB(255, 255, 255)  // white
    };

    private void startFireworksRunnable(World world) {
        startFireworksRunnable(world, DEFAULT_PALETTE);
    }

    private void startFireworksRunnable(World world, final org.bukkit.Color[] palette) {
        BukkitRunnable runnable = new BukkitRunnable() {
            int sec = 0;
            @Override
            public void run() {
                if (sec == 9) {
                    this.cancel();
                } else {
                    playFireworks(world, palette);
                }
                sec++;
            }
        };
        // Tracked so it stops if the game is removed before it finishes
        instance.trackTask(runnable.runTaskTimer(instance.getPlugin(), 0, 20));
    }

    private void playFireworks(World world, org.bukkit.Color[] palette) {
        Firework fw = (Firework) world.spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(1);

        org.bukkit.Color c = palette[new Random().nextInt(palette.length)];

        fwm.addEffect(FireworkEffect.builder()
                .withColor(c)
                .with(FireworkEffect.Type.BALL_LARGE)
                .flicker(true)
                .trail(true)
                .build());
        fw.setFireworkMeta(fwm);
    }

    // REMOVE WIN EFFECTS:

    public void removeWinEffects() {
        if (reindeerFlight != null) {
            reindeerFlight.end();
            reindeerFlight = null;
        }
        if (this.dragon != null && !(this.dragon.isDead())) {
            this.dragon.remove();
        } else if (this.defaultEffect) {
            this.defaultEffect = false;
        } else if (this.rainEffect) {
            player.getWorld().setStorm(false);
            player.getWorld().setThundering(false);
            for (Item i : fish) i.remove();
            this.rainEffect = false;
        } else if (this.floodEffect) {
            if (this.floodRunnable != null) {
                try { this.floodRunnable.cancel(); } catch (IllegalStateException ignored) {}
                this.floodRunnable = null;
            }
            if (this.floodBoat != null && !this.floodBoat.isDead()) {
                this.floodBoat.eject();
                this.floodBoat.remove();
            }
            this.floodBoat = null;
            player.getWorld().setStorm(false);
            player.getWorld().setThundering(false);
            this.floodEffect = false;
        } else if (this.treasureEffect) {
            this.treasureEffect = false;
        } else if (this.ritualEffect) {
            for (Bat b : ritualBats) if (b != null && !b.isDead()) b.remove();
            ritualBats.clear();
            if (herobrineNPC != null && !herobrineNPC.isDead()) {
                herobrineNPC.remove();
                herobrineNPC = null;
            }
            this.ritualEffect = false;
        } else if (this.presentEffect) {
            for (Item i : presents) i.remove();
            presents.clear();
            this.presentEffect = false;
        } else if (this.winterEffect) {
            this.winterEffect = false;
        }
    }
}