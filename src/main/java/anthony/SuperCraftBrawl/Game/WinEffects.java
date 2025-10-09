package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.util.ItemHelper;
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
	private ArrayList<Item> fish = new ArrayList<>();

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
				else if (safeRitualEnabled(data)) // <--- NEW: ritual effect gate
					ritualEffect();
				else
					defaultEffect();
			}
		}
	}

	// If you haven't added this field yet, either add "public int ritualEffect;" in PlayerData,
	// or temporarily return false here to keep builds happy.
	private boolean safeRitualEnabled(PlayerData data) {
		try {
			return data.ritualEffect == 1;
		} catch (Throwable ignored) {
			return false;
		}
	}

	// ============== ALL WIN EFFECTS ==============

	//This spawns an Ender Dragon at the player & makes the player ride it
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
		// (left empty by your original class)
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
		if (world != instance.getMapWorld()) return;
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
						if (b.getType() == Material.GOLD_BLOCK) b.setType(Material.AIR);
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
					while (baseY > 0 && world.getBlockAt(loc.getBlockX() + dx, baseY, loc.getBlockZ() + dz).getType() == Material.AIR) {
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
							item.setVelocity(new Vector(
									(rand.nextDouble() - 0.5) * 0.8,
									0.6 + rand.nextDouble() * 0.4,
									(rand.nextDouble() - 0.5) * 0.8
							));
							new BukkitRunnable() {
								@Override
								public void run() {
									if (!item.isDead()) item.remove();
								}
							}.runTaskLater(instance.getGameManager().getMain(), 40L);
						}
					}
				}

				tick++;
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 0L, 4L); // every 4 ticks
	}

	// ========= NEW: Herobrine "Ritual" Win Effect =========
	private void ritualEffect() {
		final World w = player.getWorld();
		if (w != instance.getMapWorld()) return;
		startFireworksRunnable(w);

		// snapshot helper to restore blocks after the ritual
		final class Snap {
			final Block b; final Material type; final byte data;
			Snap(Block b){ this.b=b; this.type=b.getType(); this.data=b.getData(); }
			void restore(){ b.setType(type); b.setData(data, true); b.getState().update(true, false); }
		}
		final List<Snap> snaps = new ArrayList<>();

		// align base to grid so the totem looks neat
		Location base = player.getLocation().clone();
		base.setX(Math.floor(base.getX()));
		base.setY(Math.floor(base.getY()));
		base.setZ(Math.floor(base.getZ()));

		// Totem:
		// y  : +0 GOLD
		// y  : +1 GOLD
		// y  : +2 NETHERRACK + 4 wall redstone torches around
		// y  : +3 FIRE
		Block gold1 = w.getBlockAt(base);
		Block gold2 = w.getBlockAt(base.clone().add(0, 1, 0));
		Block nether = w.getBlockAt(base.clone().add(0, 2, 0));
		Block fire  = w.getBlockAt(base.clone().add(0, 3, 0));

		Block torchN = w.getBlockAt(base.clone().add(0, 2, -1)); // north (-Z)
		Block torchS = w.getBlockAt(base.clone().add(0, 2,  1)); // south (+Z)
		Block torchW = w.getBlockAt(base.clone().add(-1,2, 0));  // west  (-X)
		Block torchE = w.getBlockAt(base.clone().add(1, 2,  0)); // east  (+X)

		snaps.add(new Snap(gold1));
		snaps.add(new Snap(gold2));
		snaps.add(new Snap(nether));
		snaps.add(new Snap(fire));
		snaps.add(new Snap(torchN));
		snaps.add(new Snap(torchS));
		snaps.add(new Snap(torchW));
		snaps.add(new Snap(torchE));

		setBlock(gold1, Material.GOLD_BLOCK);
		setBlock(gold2, Material.GOLD_BLOCK);
		setBlock(nether, Material.NETHERRACK);

		// 1.8 wall torch data: 1=east, 2=west, 3=south, 4=north
		placeWallTorch(torchN, (byte)4);
		placeWallTorch(torchS, (byte)3);
		placeWallTorch(torchW, (byte)2);
		placeWallTorch(torchE, (byte)1);

		setBlock(fire, Material.FIRE);

		// spooky sounds/particles
		w.playSound(base, Sound.AMBIENCE_CAVE, 1.0f, 0.6f);
		w.playSound(base, Sound.GHAST_MOAN, 0.7f, 0.7f);
		w.spigot().playEffect(base.clone().add(0, 2.5, 0), Effect.SMOKE, 0, 0, 0.3f, 0.6f, 0.3f, 0.02f, 30, 16);
		w.spigot().playEffect(base.clone().add(0, 2.5, 0), Effect.ENDER_SIGNAL, 0, 0, 0,0,0, 0, 10, 16);

		// bats
		final List<Entity> bats = new ArrayList<>(10);
		for (int i = 0; i < 10; i++) {
			double angle = (Math.PI * 2) * i / 10.0;
			double r = 2.2;
			Location bLoc = base.clone().add(Math.cos(angle) * r, 2.0 + (i % 3) * 0.3, Math.sin(angle) * r);
			Bat bat = w.spawn(bLoc, Bat.class);
			bat.setRemoveWhenFarAway(false);
			bats.add(bat);
		}

		// lightning visuals
		new BukkitRunnable() {
			int flashes = 0;
			@Override public void run() {
				w.strikeLightningEffect(base.clone().add(0, 2.5, 0));
				if (++flashes >= 3) cancel();
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 0L, 10L);

		// animate bats in swirl + periodic particles, then restore blocks
		final int lifetime = 20 * 6; // 6s
		new BukkitRunnable() {
			int t = 0;
			@Override public void run() {
				if (t >= lifetime) {
					for (Entity e : bats) if (e != null && !e.isDead()) e.remove();
					// restore blocks
					for (Snap s : snaps) {
						try { s.restore(); } catch (Throwable ignored) {}
					}
					w.playSound(base, Sound.PORTAL_TRAVEL, 0.7f, 0.7f);
					w.spigot().playEffect(base.clone().add(0, 2, 0), Effect.LARGE_SMOKE, 0, 0, 0.5f,0.5f,0.5f, 0.02f, 20, 16);
					cancel();
					return;
				}

				double theta = (t / 6.0);
				int i = 0;
				for (Entity e : bats) {
					if (!(e instanceof Bat) || e.isDead()) continue;
					double a = theta + (i++ * (Math.PI * 2 / Math.max(1, bats.size())));
					double r = 2.2;
					Location target = base.clone().add(Math.cos(a) * r, 1.8 + Math.sin(theta * 0.7) * 0.3, Math.sin(a) * r);
					e.teleport(target);
				}

				if (t % 10 == 0) {
					w.spigot().playEffect(base.clone().add(0, 2.2, 0), Effect.PORTAL, 0, 0, 0.6f, 0.6f, 0.6f, 0.02f, 40, 16);
					w.playSound(base, Sound.AMBIENCE_THUNDER, 0.25f, 1.5f);
				}
				t += 2;
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 10L, 2L);
	}

	// ======= helpers for ritual =======
	private void setBlock(Block b, Material m) {
		b.setType(m);
		b.getState().update(true, false);
	}
	private void placeWallTorch(Block target, byte dataFacing) {
		target.setType(Material.REDSTONE_TORCH_ON);
		target.setData(dataFacing, true);
		target.getState().update(true, false);
	}

	// ============== DEFAULT + SHARED FIREWORKS ==============

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
		Firework fw = (Firework) world.spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		fwm.setPower(1);

		Color c;
		Random r = new Random();
		int chance = r.nextInt(4);

		if (chance == 0) c = Color.BLUE;
		else if (chance == 1) c = Color.LIME;
		else if (chance == 2) c = Color.GREEN;
		else c = Color.YELLOW;

		fwm.addEffect(FireworkEffect.builder().withColor(c).flicker(true).build());
		fw.setFireworkMeta(fwm);
	}

	// ============== REMOVE WIN EFFECTS ==============

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
		}
	}
}
