package anthony.flagwars.setup;

import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import anthony.flagwars.FWGameConstants;
import anthony.flagwars.FWGameManager;
import anthony.flagwars.map.FWImportantArea;
import anthony.flagwars.map.FWMapConfig;
import anthony.flagwars.map.FWMapConfigManager;
import anthony.flagwars.map.FWTeamSite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * /fwsetup generatemap: builds a complete 4-team Flag Wars map from code (Hypixel Bedwars style) and sets up all of
 * its Flag Wars config, so it's playable as soon as it's done:
 * - 4 floating bases (Red, Blue, Lime, Yellow), one on each side of mid, each with a roofed spawn pavilion in the
 *   team's color, a forge with the iron and gold generators, the flag on a pedestal, a shop stall, chests, a lit path
 *   to the front edge, trees and flowers - plus its island boundary and no-build areas
 * - 4 diamond islands on the diagonals, each between two bases
 * - a mid island with a stone brick plaza, a quartz monument and the 4 emerald generators in a circle around it
 * - a glass waiting lobby high above mid
 * The blocks are worked out first and then placed a few thousand per tick, so the server doesn't freeze.
 */
public class FWMapGenerator {

	private static final int GROUND = 64; // top block of every island; players stand at GROUND + 1
	private static final int BASE_DISTANCE = 70;
	private static final int DIAMOND_OFFSET = 38; // diamond islands at (±38, ±38)
	private static final int EMERALD_OFFSET = 5; // emerald generators at (±5, ±5)
	private static final int LOBBY_Y = 110;
	private static final int BLOCKS_PER_TICK = 4000;

	/** Team site ids (see FWGameConstants.TEAM_NAMES) and which way each base is from mid. */
	private static final int[] TEAM_SITES = {5, 4, 1, 2}; // Red, Blue, Lime, Yellow
	private static final int[][] BASE_DIRECTIONS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

	// Block ids (1.8)
	private static final int STONE = 1, GRASS = 2, DIRT = 3, COBBLE = 4, PLANKS = 5, IRON_ORE = 15, COAL_ORE = 16,
			LOG = 17, LEAVES = 18, WOOL = 35, YELLOW_FLOWER = 37, RED_FLOWER = 38, GOLD_BLOCK = 41, IRON_BLOCK = 42,
			DOUBLE_SLAB = 43, SLAB = 44, MOSSY_COBBLE = 48, TORCH = 50, CHEST = 54, DIAMOND_BLOCK = 57, FENCE = 85,
			GLOWSTONE = 89, STAINED_GLASS = 95, STONE_BRICK = 98, CAULDRON = 118, WOOD_SLAB = 126, ENDER_CHEST = 130,
			EMERALD_BLOCK = 133, COBBLE_WALL = 139, QUARTZ = 155, STAINED_CLAY = 159, BARRIER = 166,
			SEA_LANTERN = 169, HAY = 170, CARPET = 171, TALL_GRASS = 31;

	private final FWGameManager fwGameManager;

	public FWMapGenerator(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
	}

	public void generate(Player player, String mapName) {
		if (Bukkit.getWorld(mapName) != null || new File(Bukkit.getWorldContainer(), mapName).exists()) {
			player.sendMessage(color("&c&l(!) &rA world named &e" + mapName + "&r already exists!"));
			return;
		}

		World world;
		try {
			world = Bukkit.createWorld(new WorldCreator(mapName)
					.environment(World.Environment.NORMAL)
					.generateStructures(false)
					.generator(new VoidGenerator()));
		} catch (Throwable t) {
			player.sendMessage(color("&c&l(!) &rFailed to create world: &e" + t.getMessage()));
			return;
		}
		if (world == null) {
			player.sendMessage(color("&c&l(!) &rWorld creation returned null. Check the console."));
			return;
		}
		world.setSpawnFlags(false, false);
		world.setTime(6000);
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setGameRuleValue("doMobSpawning", "false");

		Build build = new Build(world, mapName.hashCode());
		FWMapConfig config = fwGameManager.getMapConfigManager().getOrCreate(mapName);
		config.setSize(FWMapConfig.SIZE_FOUR);

		buildLobby(build, config);
		buildMid(build, config);
		for (int i = 0; i < 4; i++) {
			int sx = i == 0 || i == 3 ? -1 : 1;
			int sz = i < 2 ? -1 : 1;
			buildDiamondIsland(build, config, i + 1, sx * DIAMOND_OFFSET, sz * DIAMOND_OFFSET);
		}
		int hologramId = 1;
		for (int i = 0; i < 4; i++) {
			hologramId = buildBase(build, config, TEAM_SITES[i], BASE_DIRECTIONS[i], hologramId);
		}

		player.sendMessage(color("&2&l(!) &rBuilding &e" + mapName + "&r (" + build.blocks.size() + " blocks)..."));
		place(player, world, build, config);
	}

	/** Places the worked-out blocks a few thousand per tick, then saves the world and its config. */
	@SuppressWarnings("deprecation")
	private void place(Player player, World world, Build build, FWMapConfig config) {
		Iterator<Map.Entry<Long, Integer>> it = build.blocks.entrySet().iterator();
		new BukkitRunnable() {
			@Override
			public void run() {
				for (int i = 0; i < BLOCKS_PER_TICK && it.hasNext(); i++) {
					Map.Entry<Long, Integer> entry = it.next();
					long key = entry.getKey();
					int x = (int) (key >> 32) - 4096;
					int z = (int) ((key >> 12) & 0xFFFFF) - 4096;
					int y = (int) (key & 0xFFF);
					int value = entry.getValue();
					world.getBlockAt(x, y, z).setTypeIdAndData(value >> 4, (byte) (value & 0xF), false);
				}
				if (it.hasNext()) return;
				cancel();
				build.blocks.clear();

				world.setSpawnLocation(0, LOBBY_Y + 1, 0);
				world.save();
				FWMapConfigManager manager = fwGameManager.getMapConfigManager();
				manager.save(config);

				List<String> missing = config.findMissing();
				player.sendMessage(color(missing.isEmpty()
						? "&2&l(!) &rMap &e" + world.getName() + "&r is built and ready to play!"
						: "&c&l(!) &rMap built, but its setup is missing: &e" + String.join(", ", missing)));
				if (player.isOnline()) player.teleport(config.getLobby());
			}
		}.runTaskTimer(fwGameManager.getMain(), 1L, 1L);
	}

	// ===== Lobby =====

	private void buildLobby(Build b, FWMapConfig config) {
		int[] quadrantColors = {colorOf(TEAM_SITES[0]), colorOf(TEAM_SITES[1]), colorOf(TEAM_SITES[2]), colorOf(TEAM_SITES[3])};
		for (int x = -10; x <= 10; x++) {
			for (int z = -10; z <= 10; z++) {
				boolean edge = Math.abs(x) == 10 || Math.abs(z) == 10;
				if (x == 0 || z == 0) {
					b.set(x, LOBBY_Y, z, QUARTZ, 0);
				} else if (edge) {
					b.set(x, LOBBY_Y, z, STONE_BRICK, 0);
				} else {
					int quadrant = z < 0 ? (x > 0 ? 1 : 0) : (x > 0 ? 2 : 3);
					boolean tinted = Math.abs(x) >= 7 || Math.abs(z) >= 7;
					b.set(x, LOBBY_Y, z, STAINED_GLASS, tinted ? quadrantColors[quadrant] : 0);
				}
				if (edge) {
					for (int y = LOBBY_Y + 1; y <= LOBBY_Y + 4; y++) b.set(x, y, z, BARRIER, 0);
				}
			}
		}
		b.set(0, LOBBY_Y, 0, SEA_LANTERN, 0);
		config.setLobby(new Location(b.world, 0.5, LOBBY_Y + 1, 0.5, 0, 0));
		config.setSpectatorLocation(new Location(b.world, 0.5, GROUND + 30, -25.5, 0, 45));
	}

	// ===== Mid =====

	private void buildMid(Build b, FWMapConfig config) {
		b.island(0, 0, 19, 19, 16, 0.11);

		for (int x = -19; x <= 19; x++) {
			for (int z = -19; z <= 19; z++) {
				double r = Math.sqrt(x * x + z * z);
				if (!b.has(x, GROUND, z)) continue;
				if (r <= 3) {
					b.set(x, GROUND, z, QUARTZ, 0);
				} else if (r <= 11) {
					boolean ring = r >= 7.5 && r <= 8.5;
					double roll = b.random(x, GROUND, z);
					b.set(x, GROUND, z, STONE_BRICK, ring ? 3 : roll < 0.08 ? 1 : roll < 0.14 ? 2 : 0);
				} else if (Math.abs(x) <= 1 || Math.abs(z) <= 1) {
					// Paths out towards each base
					b.set(x, GROUND, z, STONE_BRICK, b.random(x, GROUND, z) < 0.15 ? 1 : 0);
				}
			}
		}

		// Quartz monument in the middle
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0) continue;
				b.set(x, GROUND + 1, z, SLAB, 7);
			}
		}
		for (int y = GROUND + 1; y <= GROUND + 6; y++) b.set(0, y, 0, QUARTZ, 2);
		b.set(0, GROUND + 7, 0, QUARTZ, 1);
		b.set(0, GROUND + 8, 0, GLOWSTONE, 0);

		// Emerald generators in a circle around it
		int id = 1;
		for (int sx = -1; sx <= 1; sx += 2) {
			for (int sz = -1; sz <= 1; sz += 2) {
				int gx = sx * EMERALD_OFFSET;
				int gz = sz * EMERALD_OFFSET;
				for (int x = -1; x <= 1; x++) {
					for (int z = -1; z <= 1; z++) b.set(gx + x, GROUND, gz + z, STAINED_CLAY, 13);
				}
				b.set(gx, GROUND, gz, EMERALD_BLOCK, 0);
				config.setEmeraldGenerator(id++, new Location(b.world, gx + 0.5, GROUND + 1, gz + 0.5));
			}
		}

		// Lamps beside each path, at the plaza's edge
		for (int[] dir : BASE_DIRECTIONS) {
			for (int side = -1; side <= 1; side += 2) {
				int px = dir[0] * 12 + (dir[0] == 0 ? side * 2 : 0);
				int pz = dir[1] * 12 + (dir[1] == 0 ? side * 2 : 0);
				b.lampPost(px, pz);
			}
		}

		// Trees and bushes around the outer ring
		int[][] trees = {{14, 6}, {-6, 14}, {-14, -6}, {6, -14}};
		for (int[] t : trees) b.tree(t[0], GROUND + 1, t[1], 0, 5);
		int[][] bushes = {{13, -8}, {8, 13}, {-13, 8}, {-8, -13}};
		for (int[] bush : bushes) b.bush(bush[0], GROUND + 1, bush[1]);
		b.decorate(0, 0, 19, 0.11);
	}

	// ===== Diamond islands =====

	private void buildDiamondIsland(Build b, FWMapConfig config, int id, int cx, int cz) {
		b.island(cx, cz, 6.5, 6.5, 8, 0.12);
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) b.set(cx + x, GROUND, cz + z, STONE_BRICK, 1);
		}
		b.set(cx, GROUND, cz, DIAMOND_BLOCK, 0);
		config.setDiamondGenerator(id, new Location(b.world, cx + 0.5, GROUND + 1, cz + 0.5));

		// Ruined pillars and a tree on the far side from mid
		int ox = Integer.signum(cx);
		int oz = Integer.signum(cz);
		b.pillar(cx + ox * 4, cz - oz * 1, 3);
		b.pillar(cx - ox * 1, cz + oz * 4, 2);
		b.tree(cx + ox * 3, GROUND + 1, cz + oz * 3, 2, 4);
		b.decorate(cx, cz, 6.5, 0.12);
	}

	// ===== Bases =====

	/** Builds one team's base; returns the next free chest hologram id. */
	private int buildBase(Build b, FWMapConfig config, int siteId, int[] dir, int hologramId) {
		int team = colorOf(siteId);
		b.frame(dir[0] * BASE_DISTANCE, dir[1] * BASE_DISTANCE, -dir[0], -dir[1]);
		World w = b.world;

		// The island: longer towards mid than it is wide
		boolean alongZ = dir[0] == 0;
		b.island(b.bx, b.bz, alongZ ? 12 : 15, alongZ ? 15 : 12, 15, 0.1);

		// Path from the flag to the front edge
		for (int lz = 4; lz <= 16; lz++) {
			for (int lx = -1; lx <= 1; lx++) {
				if (!b.hasLocal(lx, lz)) continue;
				b.setLocal(lx, GROUND, lz, STONE_BRICK, lx == 0 && lz % 4 == 0 ? 3 : b.randomLocal(lx, lz) < 0.15 ? 1 : 0);
			}
		}
		b.lampLocal(-2, 8);
		b.lampLocal(2, 8);
		b.lampLocal(-2, 13);
		b.lampLocal(2, 13);

		// Flag plaza
		for (int lx = -2; lx <= 2; lx++) {
			for (int lz = 1; lz <= 5; lz++) {
				boolean border = Math.abs(lx) == 2 || lz == 1 || lz == 5;
				b.foundationLocal(lx, lz);
				b.setLocal(lx, GROUND, lz, border ? STONE_BRICK : STAINED_CLAY, border ? 0 : team);
			}
		}
		b.setLocal(0, GROUND, 3, QUARTZ, 1);

		// Spawn pavilion
		for (int lx = -4; lx <= 4; lx++) {
			for (int lz = -10; lz <= -2; lz++) {
				b.foundationLocal(lx, lz);
				boolean inside = Math.abs(lx) <= 3 && lz >= -9 && lz <= -3;
				if (inside) {
					boolean border = Math.abs(lx) == 3 || lz == -9 || lz == -3;
					b.setLocal(lx, GROUND, lz, PLANKS, border ? 1 : 0);
				}
			}
		}
		int[][] corners = {{-3, -9}, {3, -9}, {-3, -3}, {3, -3}};
		for (int[] c : corners) {
			for (int y = GROUND + 1; y <= GROUND + 4; y++) b.setLocal(c[0], y, c[1], LOG, 0);
		}
		for (int lx = -4; lx <= 4; lx++) {
			for (int lz = -10; lz <= -2; lz++) {
				boolean rim = Math.abs(lx) == 4 || lz == -10 || lz == -2;
				b.setLocal(lx, GROUND + 5, lz, rim ? WOOD_SLAB : STAINED_CLAY, rim ? 1 : team);
				if (Math.abs(lx) <= 2 && lz >= -8 && lz <= -4) b.setLocal(lx, GROUND + 6, lz, STAINED_CLAY, team);
				if (Math.abs(lx) <= 1 && lz >= -7 && lz <= -5) b.setLocal(lx, GROUND + 7, lz, WOOL, team);
			}
		}
		b.setLocal(0, GROUND + 8, -6, GLOWSTONE, 0);
		b.setLocal(0, GROUND + 4, -6, GLOWSTONE, 0);

		FWTeamSite site = config.getTeamSite(siteId);
		site.setCreated(true);
		site.setSpawn(b.standLocal(0, -6, true));
		site.setFlagLoc(b.standLocal(0, 3, false));

		// Team chest and ender chest, with their Punch to Deposit holograms
		b.setLocal(-2, GROUND + 1, -8, CHEST, b.facingData());
		b.setLocal(2, GROUND + 1, -8, ENDER_CHEST, b.facingData());
		config.setChestHologram(hologramId++, b.standLocal(-2, -8, false).add(0, 1.2, 0));
		config.setChestHologram(hologramId++, b.standLocal(2, -8, false).add(0, 1.2, 0));

		// Forge at the back with the iron and gold generators
		for (int lx = -3; lx <= 3; lx++) {
			for (int lz = -14; lz <= -11; lz++) {
				b.foundationLocal(lx, lz);
				b.setLocal(lx, GROUND, lz, DOUBLE_SLAB, 0);
			}
			b.setLocal(lx, GROUND + 1, -14, STONE_BRICK, 0);
			b.setLocal(lx, GROUND + 2, -14, SLAB, 5);
		}
		for (int lx = -3; lx <= 3; lx += 6) {
			b.setLocal(lx, GROUND + 2, -14, STONE_BRICK, 0);
			b.setLocal(lx, GROUND + 3, -14, SEA_LANTERN, 0);
		}
		b.setLocal(-1, GROUND, -12, IRON_BLOCK, 0);
		b.setLocal(1, GROUND, -12, GOLD_BLOCK, 0);
		site.setIronGenerator(b.standLocal(-1, -12, false));
		site.setGoldGenerator(b.standLocal(1, -12, false));

		// Shop stall on one side, a farm stall on the other
		b.stallLocal(-7, -2, team);
		site.setShopLoc(b.standLocal(-7, -2, false));
		b.stallLocal(7, -2, team);
		b.setLocal(7, GROUND + 1, -2, HAY, 0);
		b.setLocal(8, GROUND + 1, -3, HAY, 0);
		b.setLocal(6, GROUND + 1, -1, CAULDRON, 0);

		// Trees, then grass and flowers wherever's free
		b.treeLocal(7, -9, 0, 5);
		b.treeLocal(-7, 8, 0, 5);
		b.treeLocal(8, 5, 2, 6);
		b.bushLocal(-9, -8);
		b.decorate(b.bx, b.bz, 15, 0.1);

		// Island boundary (for traps and the heal pool), and no-build areas at the generators, spawn and shop
		site.setBoundaryCorner1(b.cornerLocal(-14, -17, GROUND - 25));
		site.setBoundaryCorner2(b.cornerLocal(14, 17, GROUND + 25));
		site.setImportantArea(1, b.areaLocal(0, -12, 2, 3, 1));
		site.setImportantArea(2, b.areaLocal(0, -6, 1, 2, 1));
		site.setImportantArea(3, b.areaLocal(-7, -2, 1, 2, 1));

		return hologramId;
	}

	private static int colorOf(int siteId) {
		return FWGameConstants.TEAM_COLORS[siteId - 1].getWoolData();
	}

	private String color(String s) {
		return fwGameManager.getMain().color(s);
	}

	/** The blocks worked out for one map, plus the current base's local frame. Thrown away once it's placed. */
	private static final class Build {
		final World world;
		final long seed;
		final Map<Long, Integer> blocks = new HashMap<>();

		// Local frame of the base being built: lz points towards mid, lx to its side
		int bx, bz, fx, fz, rx, rz;

		Build(World world, long seed) {
			this.world = world;
			this.seed = seed;
		}

		private static long key(int x, int y, int z) {
			return ((long) (x + 4096) << 32) | ((long) (z + 4096) << 12) | y;
		}

		void set(int x, int y, int z, int id, int data) {
			blocks.put(key(x, y, z), (id << 4) | (data & 0xF));
		}

		boolean has(int x, int y, int z) {
			return blocks.containsKey(key(x, y, z));
		}

		int idAt(int x, int y, int z) {
			Integer value = blocks.get(key(x, y, z));
			return value == null ? 0 : value >> 4;
		}

		/** A repeatable 0..1 value for a block, so the same map name always builds the same map. */
		double random(int x, int y, int z) {
			long h = seed ^ (x * 73856093L) ^ (y * 19349663L) ^ (z * 83492791L);
			h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
			h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
			h ^= h >>> 33;
			return (h >>> 11) / (double) (1L << 53);
		}

		/**
		 * A floating island: a grass top at GROUND over an elliptical, wavy outline, hanging down into a rocky point
		 * (dirt, then stone with andesite, cobble and ores, mossy at the very bottom).
		 */
		void island(int cx, int cz, double a, double c, int depth, double wobble) {
			double p1 = random(cx, 1, cz) * Math.PI * 2;
			double p2 = random(cx, 2, cz) * Math.PI * 2;
			double p3 = random(cx, 3, cz) * Math.PI * 2;
			int reach = (int) Math.ceil(Math.max(a, c) * (1 + wobble * 1.8)) + 1;
			for (int x = cx - reach; x <= cx + reach; x++) {
				for (int z = cz - reach; z <= cz + reach; z++) {
					double dx = x - cx;
					double dz = z - cz;
					double angle = Math.atan2(dz, dx);
					double edge = 1 + wobble * (Math.sin(3 * angle + p1) + 0.7 * Math.sin(5 * angle + p2)
							+ 0.4 * Math.sin(9 * angle + p3)) / 2.1;
					double d = Math.sqrt((dx / a) * (dx / a) + (dz / c) * (dz / c)) / edge;
					if (d > 1) continue;
					int columnDepth = 2 + (int) Math.round(Math.pow(1 - d, 0.75) * depth + random(x, 0, z) * 2.5);
					column(x, z, columnDepth);
				}
			}
		}

		void column(int x, int z, int depth) {
			int bottom = GROUND - depth + 1;
			for (int y = bottom; y <= GROUND; y++) {
				if (y == GROUND) {
					set(x, y, z, GRASS, 0);
				} else if (y >= GROUND - 2) {
					set(x, y, z, DIRT, 0);
				} else if (y <= bottom + 1) {
					set(x, y, z, random(x, y, z) < 0.5 ? MOSSY_COBBLE : STONE, 0);
				} else {
					double roll = random(x, y, z);
					if (roll < 0.03) set(x, y, z, COAL_ORE, 0);
					else if (roll < 0.05) set(x, y, z, IRON_ORE, 0);
					else if (roll < 0.20) set(x, y, z, STONE, 5); // andesite
					else if (roll < 0.30) set(x, y, z, COBBLE, 0);
					else set(x, y, z, STONE, 0);
				}
			}
		}

		/** Makes sure a structure's block has ground under it, even past the island's wavy edge. */
		void foundation(int x, int z) {
			if (!has(x, GROUND, z)) column(x, z, 4);
		}

		/**
		 * Gentle hills (1-2 blocks) on the open grass towards the island's edge, then grass tufts and flowers on
		 * top. Hills keep a block away from anything built, so paths, stalls and generators stay flat to walk on.
		 */
		void decorate(int cx, int cz, double radius, double wobble) {
			int reach = (int) Math.ceil(radius * (1 + wobble * 1.8)) + 1;
			double p = (seed & 0xFF) / 40.0;
			List<int[]> raise = new ArrayList<>();
			for (int x = cx - reach; x <= cx + reach; x++) {
				for (int z = cz - reach; z <= cz + reach; z++) {
					if (!isOpenGrass(x, z)) continue;
					double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz)) / radius;
					if (d < 0.55 || !openAround(x, z)) continue;
					double hill = Math.sin(x * 0.31 + p) * Math.cos(z * 0.27 - p) + (random(x, 7, z) - 0.5) * 0.3;
					int height = hill > 0.75 ? 2 : hill > 0.35 ? 1 : 0;
					if (height > 0) raise.add(new int[]{x, z, height});
				}
			}
			for (int[] r : raise) {
				for (int i = 1; i <= r[2]; i++) {
					set(r[0], GROUND + i - 1, r[1], DIRT, 0);
					set(r[0], GROUND + i, r[1], GRASS, 0);
				}
			}

			for (int x = cx - reach; x <= cx + reach; x++) {
				for (int z = cz - reach; z <= cz + reach; z++) {
					int top = GROUND;
					while (idAt(x, top + 1, z) == GRASS) top++;
					if (idAt(x, top, z) != GRASS || has(x, top + 1, z)) continue;
					double roll = random(x, top + 1, z);
					if (roll < 0.16) set(x, top + 1, z, TALL_GRASS, 1);
					else if (roll < 0.18) set(x, top + 1, z, RED_FLOWER, 0);
					else if (roll < 0.195) set(x, top + 1, z, YELLOW_FLOWER, 0);
					else if (roll < 0.205) set(x, top + 1, z, RED_FLOWER, 3);
					else if (roll < 0.212) set(x, top + 1, z, RED_FLOWER, 8);
				}
			}
		}

		/** Plain grass with nothing on it. */
		private boolean isOpenGrass(int x, int z) {
			return idAt(x, GROUND, z) == GRASS && !has(x, GROUND + 1, z);
		}

		/** Every block around is plain grass or the island's edge (nothing built there). */
		private boolean openAround(int x, int z) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (!has(x + dx, GROUND, z + dz)) continue;
					if (!isOpenGrass(x + dx, z + dz)) return false;
				}
			}
			return true;
		}

		void tree(int x, int y, int z, int woodData, int height) {
			foundation(x, z);
			set(x, GROUND, z, DIRT, 0);
			for (int ly = 0; ly < height; ly++) set(x, y + ly, z, LOG, woodData);
			for (int ly = height - 3; ly <= height; ly++) {
				int r = ly >= height - 1 ? 1 : 2;
				for (int dx = -r; dx <= r; dx++) {
					for (int dz = -r; dz <= r; dz++) {
						boolean corner = Math.abs(dx) == r && Math.abs(dz) == r;
						if (corner && (r == 1 || random(x + dx, y + ly, z + dz) < 0.6)) continue;
						if (dx == 0 && dz == 0 && ly < height) continue; // trunk
						if (!has(x + dx, y + ly, z + dz)) set(x + dx, y + ly, z + dz, LEAVES, woodData | 4);
					}
				}
			}
			set(x, y + height, z, LEAVES, woodData | 4);
		}

		void bush(int x, int y, int z) {
			foundation(x, z);
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (Math.abs(dx) + Math.abs(dz) == 2 && random(x + dx, y, z + dz) < 0.5) continue;
					set(x + dx, y, z + dz, LEAVES, 4);
				}
			}
			set(x, y + 1, z, LEAVES, 4);
		}

		void lampPost(int x, int z) {
			foundation(x, z);
			set(x, GROUND + 1, z, FENCE, 0);
			set(x, GROUND + 2, z, FENCE, 0);
			set(x, GROUND + 3, z, TORCH, 5);
		}

		/** A broken mossy stone brick pillar. */
		void pillar(int x, int z, int height) {
			foundation(x, z);
			set(x, GROUND, z, STONE_BRICK, 0);
			for (int y = 1; y <= height; y++) {
				set(x, GROUND + y, z, y == height ? COBBLE_WALL : STONE_BRICK, y == height ? 1 : random(x, y, z) < 0.5 ? 1 : 2);
			}
		}

		// ----- Base-local helpers (lx to the side, lz towards mid) -----

		void frame(int baseX, int baseZ, int towardsX, int towardsZ) {
			bx = baseX;
			bz = baseZ;
			fx = towardsX;
			fz = towardsZ;
			rx = -fz; // the same turn for every base, so all four are the same base rotated
			rz = fx;
		}

		int wx(int lx, int lz) {
			return bx + lx * rx + lz * fx;
		}

		int wz(int lx, int lz) {
			return bz + lx * rz + lz * fz;
		}

		void setLocal(int lx, int y, int lz, int id, int data) {
			set(wx(lx, lz), y, wz(lx, lz), id, data);
		}

		boolean hasLocal(int lx, int lz) {
			return has(wx(lx, lz), GROUND, wz(lx, lz));
		}

		double randomLocal(int lx, int lz) {
			return random(wx(lx, lz), GROUND, wz(lx, lz));
		}

		void foundationLocal(int lx, int lz) {
			foundation(wx(lx, lz), wz(lx, lz));
		}

		void treeLocal(int lx, int lz, int woodData, int height) {
			tree(wx(lx, lz), GROUND + 1, wz(lx, lz), woodData, height);
		}

		void bushLocal(int lx, int lz) {
			bush(wx(lx, lz), GROUND + 1, wz(lx, lz));
		}

		void lampLocal(int lx, int lz) {
			lampPost(wx(lx, lz), wz(lx, lz));
		}

		/** A 3x3 market stall: plank floor, fence posts and a striped awning (team color and white) with carpet on top. */
		void stallLocal(int lx, int lz, int team) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					foundationLocal(lx + dx, lz + dz);
					setLocal(lx + dx, GROUND, lz + dz, PLANKS, 1);
					if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
						setLocal(lx + dx, GROUND + 1, lz + dz, FENCE, 0);
						setLocal(lx + dx, GROUND + 2, lz + dz, FENCE, 0);
					}
					int stripe = dx == 0 ? 0 : team;
					setLocal(lx + dx, GROUND + 3, lz + dz, WOOL, stripe);
					setLocal(lx + dx, GROUND + 4, lz + dz, CARPET, stripe);
				}
			}
		}

		/** Where a player stands on this block (its center, on top), facing mid if asked. */
		Location standLocal(int lx, int lz, boolean faceMid) {
			float yaw = faceMid ? (float) Math.toDegrees(Math.atan2(-fx, fz)) : 0;
			return new Location(world, wx(lx, lz) + 0.5, GROUND + 1, wz(lx, lz) + 0.5, yaw, 0);
		}

		Location cornerLocal(int lx, int lz, int y) {
			return new Location(world, wx(lx, lz), y, wz(lx, lz));
		}

		/** A no-build area centered on this block, sideRadius to the side and forwardRadius towards mid. */
		FWImportantArea areaLocal(int lx, int lz, int sideRadius, int yRadius, int forwardRadius) {
			boolean alongZ = fx == 0;
			return new FWImportantArea(standLocal(lx, lz, false),
					alongZ ? sideRadius : forwardRadius, yRadius, alongZ ? forwardRadius : sideRadius);
		}

		/** Chest data facing towards mid (2 north, 3 south, 4 west, 5 east). */
		int facingData() {
			if (fz < 0) return 2;
			if (fz > 0) return 3;
			return fx < 0 ? 4 : 5;
		}
	}
}
