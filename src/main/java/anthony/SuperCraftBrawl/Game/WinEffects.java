package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.util.ItemHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
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

    private Player player;
    private GameInstance instance;
    private EnderDragon dragon;
    private boolean defaultEffect = false;
    private boolean rainEffect = false;
    private boolean floodEffect = false;
    private boolean treasureEffect = false;
    private boolean ritualEffect = false;

    private ArrayList<Item> fish = new ArrayList<>();
    private final List<Bat> ritualBats = new ArrayList<>();

    // reference to spawned Herobrine ArmorStand for cleanup
    private ArmorStand herobrineNPC = null;

    // If you have a specific Herobrine skin, paste its Base64 texture VALUE here (signature not required for heads).
    // Example format inside the decoded value: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/<hash>"}}}
    private static final String HEROBRINE_TEXTURE_VALUE = ""; // <--- paste Base64 here (optional)

    public WinEffects(Player player, GameInstance instance) {
        this.player = player;
        this.instance = instance;
    }

    public void checkWinEffect() { // Database checking here
        if (instance != null) {
            PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(player);
            if (data != null) {
                if (data.enderDragonEffect == 1) enderDragonEffect();
                else if (data.santaEffect == 1)   santaEffect();
                else if (data.fireParticlesEffect == 1) fireParticlesEffect();
                else if (data.broomWinEffect == 1) magicBroomEffect();
                else if (data.fishRainEffect == 1) fishRainEffect();
                else if (data.floodEffect == 1)    floodEffect();
                else if (data.treasureEffect == 1) treasureEffect();
                else if (data.ritualEffect == 1)   ritualEffect();
                else defaultEffect();
            }
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

        final Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
        horse.setTamed(true);
        horse.setOwner(player);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        player.teleport(horse.getLocation());
        horse.setPassenger(player);
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
        runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 1);
    }

    private void floodEffect() {
        World world = player.getWorld();
        if (world != instance.getMapWorld()) return;
        startFireworksRunnable(world);
        world.setStorm(true);
        world.setThundering(true);

        MapInstance map = instance.getMap().GetInstance();
        Vector center = map.center.clone();
        double centerX = center.getX();
        double centerY = player.getLocation().clone().getY() - 5;
        double centerZ = center.getZ();
        double width  = map.boundsX;
        double length = map.boundsZ;

        final Boat boat = (Boat) player.getWorld().spawnEntity(player.getLocation(), EntityType.BOAT);
        player.teleport(boat.getLocation());
        boat.setPassenger(player);

        BukkitRunnable runnable = new BukkitRunnable() {
            int rep = 0;
            double y = centerY;
            @Override
            public void run() {
                if (rep == 100) {
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
                    boat.teleport(boat.getLocation().add(0, 1, 0));
                }
                rep++;
            }
        };
        runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
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
                            }.runTaskLater(instance.getGameManager().getMain(), 40L);
                        }
                    }
                }
                tick++;
            }
        }.runTaskTimer(instance.getGameManager().getMain(), 0L, 4L);
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

                ItemStack head = createHerobrineHead();
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
        }.runTaskLater(instance.getGameManager().getMain(), 60L); // 3 seconds later

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
        }.runTaskLater(instance.getGameManager().getMain(), 200L);
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

    private void startFireworksRunnable(World world) {
        BukkitRunnable runnable = new BukkitRunnable() {
            int sec = 0;
            @Override
            public void run() {
                if (sec == 9) {
                    this.cancel();
                } else {
                    playFireworks(world);
                }
                sec++;
            }
        };
        runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
    }

    private void playFireworks(World world) {
        // Palette-locked: ORANGE, BLACK, PURPLE only
        Firework fw = (Firework) world.spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(1);

        org.bukkit.Color ORANGE = org.bukkit.Color.fromRGB(255, 165, 0);
        org.bukkit.Color BLACK  = org.bukkit.Color.fromRGB(0, 0, 0);
        org.bukkit.Color PURPLE = org.bukkit.Color.fromRGB(128, 0, 128);

        org.bukkit.Color[] palette = new org.bukkit.Color[] { ORANGE, BLACK, PURPLE };
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
        }
    }

    // =========================
    // Head texture helpers
    // =========================

    private ItemStack createHerobrineHead() {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta sm = (SkullMeta) head.getItemMeta();

        if (HEROBRINE_TEXTURE_VALUE != null && !HEROBRINE_TEXTURE_VALUE.isEmpty()) {
            applyTexture(sm, HEROBRINE_TEXTURE_VALUE);
        } else {
            // Fallback: try the classic username (many servers resolve a Herobrine skin with white eyes)
            sm.setOwner("AR4i_");
        }
        head.setItemMeta(sm);
        return head;
    }

    private void applyTexture(SkullMeta meta, String textureValue) {
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "AR4i_");
            profile.getProperties().put("textures", new Property("textures", textureValue));
            Field f = meta.getClass().getDeclaredField("profile");
            f.setAccessible(true);
            f.set(meta, profile);
        } catch (Exception ignored) {}
    }
}