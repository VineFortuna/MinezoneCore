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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	public WinEffects(Player player, GameInstance instance) {
		this.player = player;
		this.instance = instance;
	}

	public void checkWinEffect() { // Database checking here
		if (instance != null) {
			PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(player);

			if (data != null) {
				if (data.enderDragonEffect == 1)
					enderDragonEffect();
				else if (data.santaEffect == 1)
					santaEffect();
				else if (data.fireParticlesEffect == 1)
					fireParticlesEffect();
				else if (data.broomWinEffect == 1)
					magicBroomEffect();
				else if (data.fishRainEffect == 1)
					fishRainEffect();
				else if (data.floodEffect == 1)
					floodEffect();
				else if (data.treasureEffect == 1)
					treasureEffect();
				else if (data.ritualEffect == 1)
					ritualEffect();
				else
					defaultEffect();
			}
		}
	}

	// ALL WIN EFFECTS:

	// This spawns an Ender Dragon at the player & makes the player ride it
	private void enderDragonEffect() {
		World world = player.getWorld();
		if (world != instance.getMapWorld())
			return;
		startFireworksRunnable(world);
		this.dragon = (EnderDragon) world.spawnEntity(this.player.getLocation(), EntityType.ENDER_DRAGON);
		this.dragon.setPassenger(this.player);
	}

	private void magicBroomEffect() {
		World world = player.getWorld();
		if (world != instance.getMapWorld())
			return;
		startFireworksRunnable(world);
		ItemStack broom = ItemHelper.setDetails(new ItemStack(Material.WHEAT), "&2&lMagic Broom");
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
		if (world != instance.getMapWorld())
			return;
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
				if (mat.isSolid()) {
					return loc;
				}
				if (loc2.getY() < 40) // Too low down without finding block
					break;
			}
			if (attempts > 100)
				return respawnLoc.add(0, 35, 0);
			attempts++;
		}
	}

	private void fishRainEffect() {
		this.rainEffect = true;
		World world = player.getWorld();
		if (world != instance.getMapWorld())
			return;
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
					Item i = world.dropItem(getItemRainLoc(), new ItemStack(Material.RAW_FISH, 1, (short) chance));
					fish.add(i);
				}
				rep++;
			}
		};
		runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 1);
	}

	private void floodEffect() {
		World world = player.getWorld();
		if (world != instance.getMapWorld())
			return;
		startFireworksRunnable(world);
		world.setStorm(true);
		world.setThundering(true);

		// Define the bounds for flooding (center and size)
		MapInstance map = instance.getMap().GetInstance();
		Vector center = map.center.clone();
		double centerX = center.getX();
		double centerY = player.getLocation().clone().getY() - 5;
		double centerZ = center.getZ();
		double width = map.boundsX; // Width of the flooded area (along X axis)
		double length = map.boundsZ; // Length of the flooded area (along Z axis)

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
					// Loop through the blocks in the defined bounds
					for (int x = (int) (centerX - width); x <= centerX + width; x++) {
						for (int z = (int) (centerZ - length); z <= centerZ + length; z++) {
							Block block = world.getBlockAt(x, (int) y, z);
							Block blockBelow = block.getRelative(BlockFace.DOWN);

							if (blockBelow.getType() != Material.AIR && block.getType() == Material.AIR) {
								block.setType(Material.WATER);
							}
						}
					}
					y++;
					boat.teleport(boat.getLocation().add(0, 1, 0)); // Move boat up
				}
				rep++;
			}
		};

		runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
	}

	public void treasureEffect() {
		final World world = player.getWorld();
		if (world != instance.getMapWorld())
			return;
		startFireworksRunnable(world);
		final Random rand = new Random();
		final Location loc = player.getLocation();
		final List<Block> placedBlocks = new ArrayList<>();
		final int durationTicks = 200; // 10 seconds at 20 ticks/sec
		final double maxHeight = 5; // max height the pile can reach above player

		new BukkitRunnable() {
			int tick = 0;

			@Override
			public void run() {
				if (tick >= durationTicks) {
					cancel();

					// Final explosion cleanup
					for (Block b : placedBlocks) {
						if (b.getType() == Material.GOLD_BLOCK)
							b.setType(Material.AIR);
					}
				}

				// Determine vertical rise based on elapsed time
				double rise = ((double) tick / durationTicks) * maxHeight;

				// Place multiple blocks per tick for a natural mound
				for (int i = 0; i < 3; i++) {
					int dx = rand.nextInt(9) - 4;
					int dz = rand.nextInt(9) - 4;

					// Base Y rises gradually
					int baseY = loc.getBlockY() + (int) rise;

					// Find highest solid block below current height
					while (baseY > 0 && world.getBlockAt(loc.getBlockX() + dx, baseY, loc.getBlockZ() + dz)
							.getType() == Material.AIR) {
						baseY--;
					}

					Block target = world.getBlockAt(loc.getBlockX() + dx, baseY + 1, loc.getBlockZ() + dz);

					if (target.getType() == Material.AIR) {
						target.setType(Material.GOLD_BLOCK);
						placedBlocks.add(target);

						// Coin trickle
						if (rand.nextInt(3) == 0) { // 1/3 chance
							Location coinLoc = target.getLocation().add(0.5, 1.2, 0.5);
							Material coinMat = rand.nextBoolean() ? Material.GOLD_NUGGET : Material.GOLD_INGOT;
							Item item = world.dropItem(coinLoc, new ItemStack(coinMat, 1));
							item.setPickupDelay(Integer.MAX_VALUE);
							item.setVelocity(new Vector((rand.nextDouble() - 0.5) * 0.8, 0.6 + rand.nextDouble() * 0.4,
									(rand.nextDouble() - 0.5) * 0.8));
							new BukkitRunnable() {
								@Override
								public void run() {
									if (!item.isDead())
										item.remove();
								}
							}.runTaskLater(instance.getGameManager().getMain(), 40L);
						}
					}
				}

				tick++;
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 0L, 4L); // every 4 ticks
	}

	private void playRecord11Compat(World world, Location loc, float volume, float pitch) {
		// Try Bukkit enum if present on this server
		try {
			Sound s = Sound.valueOf("RECORD_11");
			world.playSound(loc, s, volume, pitch);
			return;
		} catch (Throwable ignored) {
		}

		// Fallback: send the NMS sound name used in 1.8 ("records.11")
		try {
			PacketPlayOutNamedSoundEffect pkt = new PacketPlayOutNamedSoundEffect("records.11", loc.getX(), loc.getY(),
					loc.getZ(), volume, pitch);
			for (Player p : world.getPlayers()) {
				((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) p).getHandle().playerConnection.sendPacket(pkt);
			}
		} catch (Throwable ignored) {
		}
	}

	// --- UPDATED: Ritual effect (totem stays, bats wander normally, lightning then Enderman scream) ---
	private void ritualEffect() {
		World world = player.getWorld();
		if (world != instance.getMapWorld())
			return;

		this.ritualEffect = true;

		// Fireworks flair
		startFireworksRunnable(world);

		// Build a Herobrine-style totem near the winner
		final List<Block> placed = new ArrayList<>();
		final Location base = findGround(player.getLocation()).add(0, 1, 0); // one block above ground

		// Totem: GOLD -> GOLD -> NETHERRACK (+ fire on top), redstone torches on sides at netherrack level
		Block b1 = base.getBlock();
		Block b2 = base.clone().add(0, 1, 0).getBlock();
		Block b3 = base.clone().add(0, 2, 0).getBlock(); // netherrack
		Block fire = base.clone().add(0, 3, 0).getBlock();

		if (b1.getType() == Material.AIR) { b1.setType(Material.GOLD_BLOCK); placed.add(b1); }
		if (b2.getType() == Material.AIR) { b2.setType(Material.GOLD_BLOCK); placed.add(b2); }
		if (b3.getType() == Material.AIR) { b3.setType(Material.NETHERRACK); placed.add(b3); }
		if (fire.getType() == Material.AIR) { fire.setType(Material.FIRE); placed.add(fire); }

		// Four redstone torches around the netherrack level
		Block torchN = base.clone().add(0, 2, -1).getBlock();
		Block torchS = base.clone().add(0, 2,  1).getBlock();
		Block torchW = base.clone().add(-1,2,  0).getBlock();
		Block torchE = base.clone().add(1, 2,  0).getBlock();
		if (torchN.getType() == Material.AIR) { torchN.setType(Material.REDSTONE_TORCH_ON); placed.add(torchN); }
		if (torchS.getType() == Material.AIR) { torchS.setType(Material.REDSTONE_TORCH_ON); placed.add(torchS); }
		if (torchW.getType() == Material.AIR) { torchW.setType(Material.REDSTONE_TORCH_ON); placed.add(torchW); }
		if (torchE.getType() == Material.AIR) { torchE.setType(Material.REDSTONE_TORCH_ON); placed.add(torchE); }

		// Lightning at the totem top
		world.strikeLightningEffect(fire.getLocation());

		// After lightning, play ENDERMAN_SCREAM
		try {
			world.playSound(fire.getLocation(), Sound.ENDERMAN_SCREAM, 1.0f, 1.0f);
		} catch (Throwable ignored) {
			// 1.8 friendly; ENDERMAN_SCREAM exists here, just being cautious
		}

		// Spawn some bats nearby (default behavior; no orbit logic)
		final int BAT_COUNT = 10;
		final Random rand = new Random();
		Location center = base.clone().add(0.5, 2.0, 0.5);
		for (int i = 0; i < BAT_COUNT; i++) {
			Location spawn = center.clone().add(rand.nextDouble() * 6 - 3, rand.nextDouble() * 2, rand.nextDouble() * 6 - 3);
			Bat bat = (Bat) world.spawnEntity(spawn, EntityType.BAT);
			bat.setRemoveWhenFarAway(true);
			ritualBats.add(bat);
		}

		// Cleanup after ~10 seconds (remove bats + remove placed blocks safely)
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Bat b : ritualBats) {
					if (b != null && !b.isDead()) b.remove();
				}
				ritualBats.clear();

				for (Block b : placed) {
					// Avoid nuking map blocks if something else replaced them in the meantime
					// We only remove what we originally placed (gold, netherrack, fire, torches)
					Material t = b.getType();
					if (t == Material.GOLD_BLOCK || t == Material.NETHERRACK || t == Material.FIRE || t == Material.REDSTONE_TORCH_ON) {
						b.setType(Material.AIR);
					}
				}

				ritualEffect = false;
			}
		}.runTaskLater(instance.getGameManager().getMain(), 200L); // 10s
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
			if (world != instance.getMapWorld())
				return;
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
		Firework fw = (Firework) world.spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		fwm.setPower(1);

		Color c;
		Random r = new Random();
		int chance = r.nextInt(4);

		if (chance == 0)
			c = Color.BLUE;
		else if (chance == 1)
			c = Color.LIME;
		else if (chance == 2)
			c = Color.GREEN;
		else
			c = Color.YELLOW;
		fwm.addEffect(FireworkEffect.builder().withColor(c).flicker(true).build());
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
			for (Item i : fish) {
				i.remove();
			}
			this.rainEffect = false;
		} else if (this.floodEffect) {
			player.getWorld().setStorm(false);
			player.getWorld().setThundering(false);
			this.floodEffect = false;
		} else if (this.treasureEffect) {
			this.treasureEffect = false;
		} else if (this.ritualEffect) {
			// cleanup bats + try to remove any lingering simple totem parts (best-effort)
			for (Bat b : ritualBats) if (b != null && !b.isDead()) b.remove();
			ritualBats.clear();
			this.ritualEffect = false;
		}
	}
}