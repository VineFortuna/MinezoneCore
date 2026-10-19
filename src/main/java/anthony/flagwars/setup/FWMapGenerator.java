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
 * /fwsetup generatemap: builds a complete 4-team Flag Wars map from code (laid out like a Hypixel Bedwars 4-team map)
 * and sets up all of its Flag Wars config, so it's playable as soon as it's done:
 * - 4 floating bases (Red, Blue, Lime, Yellow), one on each side of mid. Back to front: a team-colored forge building
 *   with the iron and gold generators and the chests, a raised spawn plaza with the shop stall on one side and an
 *   anvil stall on the other, the flag on a raised dais, and a lit path to the front edge - with big trees, boulders
 *   and hills around it, plus its island boundary and no-build areas
 * - 4 diamond islands on the diagonals, each between two bases, with ruins
 * - a big mid island: a raised central platform (half-slab ramps on all four sides) with a quartz tower, a stone brick
 *   plaza around it with the 4 emerald generators in pillared shrines, and a grassy outer ring with paths to each base
 * - a glass waiting lobby high above mid
 * The blocks are worked out first and then placed a few thousand per tick, so the server doesn't freeze.
 */
public class FWMapGenerator {

	private static final int GROUND = 64; // top block of every island; players stand at GROUND + 1
	private static final int BASE_DISTANCE = 72;
	private static final int DIAMOND_OFFSET = 38; // diamond islands at (±38, ±38)
	private static final int EMERALD_OFFSET = 8; // emerald generators at (±8, ±8), 16 blocks apart
	private static final int MID_RADIUS = 23;
	private static final int MID_PLATFORM_RADIUS = 6; // the raised center, 2 blocks up
	private static final int MID_PLAZA_RADIUS = 15;
	private static final int LOBBY_Y = 120;
	private static final int BLOCKS_PER_TICK = 4000;

	/** Team site ids (see FWGameConstants.TEAM_NAMES) and which way each base is from mid. */
	private static final int[] TEAM_SITES = {5, 4, 1, 2}; // Red, Blue, Lime, Yellow
	private static final int[][] BASE_DIRECTIONS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

	// Block ids (1.8)
	private static final int STONE = 1, GRASS = 2, DIRT = 3, COBBLE = 4, PLANKS = 5, IRON_ORE = 15, COAL_ORE = 16,
			LOG = 17, LEAVES = 18, WOOL = 35, YELLOW_FLOWER = 37, RED_FLOWER = 38, GOLD_BLOCK = 41, IRON_BLOCK = 42,
			DOUBLE_SLAB = 43, SLAB = 44, BOOKSHELF = 47, MOSSY_COBBLE = 48, TORCH = 50, CHEST = 54, DIAMOND_BLOCK = 57,
			FENCE = 85, GLOWSTONE = 89, STAINED_GLASS = 95, STONE_BRICK = 98, CAULDRON = 118, WOOD_SLAB = 126,
			ENDER_CHEST = 130, EMERALD_BLOCK = 133, COBBLE_WALL = 139, ANVIL = 145, QUARTZ = 155, STAINED_CLAY = 159,
			BARRIER = 166, SEA_LANTERN = 169, HAY = 170, CARPET = 171, TALL_GRASS = 31;
	private static final int STONE_BRICK_SLAB = 5, QUARTZ_SLAB = 7, TOP_SLAB = 8; // slab data, TOP_SLAB is OR'd in
	private static final int ANDESITE = 5, POLISHED_ANDESITE = 6; // stone data
	private static final int MOSSY = 1, CRACKED = 2, CHISELED = 3; // stone brick data
	private static final int QUARTZ_CHISELED = 1, QUARTZ_PILLAR = 2;
	private static final int GREEN = 13; // dye data

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
		layout(build, config);

		player.sendMessage(color("&2&l(!) &rBuilding &e" + mapName + "&r (" + build.blocks.size() + " blocks)..."));
		place(player, world, build, config);
	}

	/** Works out every block of the map and fills in its config. */
	private void layout(Build build, FWMapConfig config) {
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
		config.setSpectatorLocation(new Location(b.world, 0.5, GROUND + 30, -40.5, 0, 35));
	}

	// ===== Mid =====

	private void buildMid(Build b, FWMapConfig config) {
		b.island(0, 0, MID_RADIUS, MID_RADIUS, 19, 0.08);

		for (int x = -MID_RADIUS - 4; x <= MID_RADIUS + 4; x++) {
			for (int z = -MID_RADIUS - 4; z <= MID_RADIUS + 4; z++) {
				if (!b.has(x, GROUND, z)) continue;
				double r = Math.sqrt(x * x + z * z);
				boolean onAxis = Math.abs(x) <= 1 || Math.abs(z) <= 1;
				double roll = b.random(x, GROUND, z);

				if (r <= MID_PLATFORM_RADIUS) {
					// Raised center, 2 blocks up, with a stone brick rim and a quartz floor
					b.set(x, GROUND + 1, z, STONE, 0);
					boolean rim = r > MID_PLATFORM_RADIUS - 1;
					boolean inner = r >= MID_PLATFORM_RADIUS - 2.5 && r <= MID_PLATFORM_RADIUS - 1.5;
					b.set(x, GROUND + 2, z, rim || inner ? STONE_BRICK : QUARTZ, inner ? CHISELED : 0);
				} else if (r <= MID_PLAZA_RADIUS) {
					boolean ring = r >= MID_PLATFORM_RADIUS + 3.5 && r <= MID_PLATFORM_RADIUS + 4.5;
					boolean outerRing = r >= MID_PLAZA_RADIUS - 1.5 && r <= MID_PLAZA_RADIUS - 0.5;
					if (outerRing) b.set(x, GROUND, z, STONE, POLISHED_ANDESITE);
					else if (!ring && roll < 0.05) b.set(x, GROUND, z, STONE, ANDESITE);
					else b.set(x, GROUND, z, STONE_BRICK, ring ? CHISELED : roll < 0.12 ? MOSSY : roll < 0.18 ? CRACKED : 0);
				} else if (onAxis) {
					b.set(x, GROUND, z, STONE_BRICK, roll < 0.15 ? MOSSY : 0);
				}
			}
		}

		// Half-slab ramps down from the platform on all four sides
		for (int[] dir : BASE_DIRECTIONS) {
			for (int side = -1; side <= 1; side++) {
				for (int t = MID_PLATFORM_RADIUS; t <= MID_PLATFORM_RADIUS + 3; t++) {
					int x = dir[0] * t + (dir[0] == 0 ? side : 0);
					int z = dir[1] * t + (dir[1] == 0 ? side : 0);
					int step = t - MID_PLATFORM_RADIUS; // 0: top edge .. 3: back on the plaza
					b.clear(x, GROUND + 1, z);
					b.clear(x, GROUND + 2, z);
					if (step == 0) {
						b.set(x, GROUND + 1, z, STONE, 0);
						b.set(x, GROUND + 2, z, SLAB, STONE_BRICK_SLAB);
					} else if (step == 1) {
						b.set(x, GROUND + 1, z, STONE_BRICK, 0);
					} else if (step == 2) {
						b.set(x, GROUND + 1, z, SLAB, STONE_BRICK_SLAB);
					}
					b.set(x, GROUND, z, STONE_BRICK, 0);
				}
			}
		}

		// Quartz tower in the middle, with a sea lantern band and a glowing top
		int top = GROUND + 14;
		for (int y = GROUND + 3; y <= top; y++) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					boolean corner = Math.abs(x) == 1 && Math.abs(z) == 1;
					if (y == GROUND + 9 && !corner) b.set(x, y, z, SEA_LANTERN, 0);
					else b.set(x, y, z, QUARTZ, corner ? QUARTZ_PILLAR : y == top ? QUARTZ_CHISELED : 0);
				}
			}
		}
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				if (Math.abs(x) == 2 || Math.abs(z) == 2) {
					b.set(x, GROUND + 3, z, SLAB, QUARTZ_SLAB);
					b.set(x, top + 1, z, SLAB, QUARTZ_SLAB);
				}
			}
		}
		b.set(0, top + 1, 0, GLOWSTONE, 0);
		b.set(0, top + 2, 0, SEA_LANTERN, 0);

		// Lanterns on the platform
		for (int sx = -1; sx <= 1; sx += 2) {
			for (int sz = -1; sz <= 1; sz += 2) {
				b.set(sx * 3, GROUND + 3, sz * 3, QUARTZ, QUARTZ_PILLAR);
				b.set(sx * 3, GROUND + 4, sz * 3, GLOWSTONE, 0);
			}
		}

		// Emerald generators in pillared shrines on the plaza, 20 blocks apart
		int id = 1;
		for (int sx = -1; sx <= 1; sx += 2) {
			for (int sz = -1; sz <= 1; sz += 2) {
				int gx = sx * EMERALD_OFFSET;
				int gz = sz * EMERALD_OFFSET;
				for (int x = -2; x <= 2; x++) {
					for (int z = -2; z <= 2; z++) {
						boolean rim = Math.abs(x) == 2 || Math.abs(z) == 2;
						b.set(gx + x, GROUND, gz + z, rim ? STONE_BRICK : STAINED_CLAY, rim ? 0 : GREEN);
						if (Math.abs(x) == 2 && Math.abs(z) == 2) {
							b.set(gx + x, GROUND + 1, gz + z, STONE_BRICK, MOSSY);
							b.set(gx + x, GROUND + 2, gz + z, STONE_BRICK, 0);
							b.set(gx + x, GROUND + 3, gz + z, STONE_BRICK, CHISELED);
							b.set(gx + x, GROUND + 4, gz + z, GLOWSTONE, 0);
						}
					}
				}
				b.set(gx, GROUND, gz, EMERALD_BLOCK, 0);
				config.setEmeraldGenerator(id++, new Location(b.world, gx + 0.5, GROUND + 1, gz + 0.5));
			}
		}

		// Lamps where the paths leave the plaza
		for (int[] dir : BASE_DIRECTIONS) {
			for (int side = -1; side <= 1; side += 2) {
				int t = MID_PLAZA_RADIUS + 1;
				b.lampPost(dir[0] * t + (dir[0] == 0 ? side * 2 : 0), dir[1] * t + (dir[1] == 0 ? side * 2 : 0));
				t = MID_RADIUS - 3;
				b.lampPost(dir[0] * t + (dir[0] == 0 ? side * 2 : 0), dir[1] * t + (dir[1] == 0 ? side * 2 : 0));
			}
		}

		// Big trees, bushes and boulders around the grassy outer ring
		for (int i = 0; i < 8; i++) {
			double angle = Math.PI / 8 + i * Math.PI / 4 + (i % 2 == 0 ? 0.12 : -0.12);
			int tx = (int) Math.round(Math.cos(angle) * (MID_RADIUS - 5));
			int tz = (int) Math.round(Math.sin(angle) * (MID_RADIUS - 5));
			if (i % 2 == 0) b.bigTree(tx, tz, 7, 3, 0);
			else b.tree(tx, GROUND + 1, tz, 2, 6);
		}
		for (int i = 0; i < 4; i++) {
			double angle = i * Math.PI / 2 + Math.PI / 4;
			b.bush((int) Math.round(Math.cos(angle) * (MID_RADIUS - 4)), GROUND + 1, (int) Math.round(Math.sin(angle) * (MID_RADIUS - 4)));
			double boulderAngle = i * Math.PI / 2 + 0.55;
			b.boulder((int) Math.round(Math.cos(boulderAngle) * (MID_RADIUS - 3)), (int) Math.round(Math.sin(boulderAngle) * (MID_RADIUS - 3)), 2);
		}
		b.decorate(0, 0, MID_RADIUS, 0.08, 0.7);
	}

	// ===== Diamond islands =====

	private void buildDiamondIsland(Build b, FWMapConfig config, int id, int cx, int cz) {
		b.island(cx, cz, 8.5, 8.5, 11, 0.12);
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				boolean rim = Math.abs(x) == 2 || Math.abs(z) == 2;
				b.set(cx + x, GROUND, cz + z, STONE_BRICK, rim ? (b.random(cx + x, 1, cz + z) < 0.5 ? MOSSY : CRACKED) : 0);
			}
		}
		b.set(cx, GROUND, cz, DIAMOND_BLOCK, 0);
		config.setDiamondGenerator(id, new Location(b.world, cx + 0.5, GROUND + 1, cz + 0.5));

		// Ruins and a tree on the far side from mid, a boulder at the side
		int ox = Integer.signum(cx);
		int oz = Integer.signum(cz);
		b.pillar(cx + ox * 4, cz - oz * 2, 4);
		b.pillar(cx - ox * 2, cz + oz * 4, 2);
		b.pillar(cx + ox * 5, cz + oz * 1, 1);
		b.bigTree(cx + ox * 3, cz + oz * 4, 6, 2, 0);
		b.boulder(cx - ox * 5, cz + oz * 2, 1);
		b.decorate(cx, cz, 8.5, 0.12, 0.5);
	}

	// ===== Bases =====

	/** Builds one team's base; returns the next free chest hologram id. */
	private int buildBase(Build b, FWMapConfig config, int siteId, int[] dir, int hologramId) {
		int team = colorOf(siteId);
		b.frame(dir[0] * BASE_DISTANCE, dir[1] * BASE_DISTANCE, -dir[0], -dir[1]);

		// The island: longer towards mid than it is wide
		boolean alongZ = dir[0] == 0;
		b.island(b.bx, b.bz, alongZ ? 15 : 19, alongZ ? 19 : 15, 18, 0.08);

		FWTeamSite site = config.getTeamSite(siteId);
		site.setCreated(true);

		hologramId = buildForge(b, config, site, team, hologramId);
		buildSpawnPlaza(b, site, team);

		// Flag on a raised dais in front of spawn
		for (int lx = -2; lx <= 2; lx++) {
			for (int lz = 3; lz <= 7; lz++) {
				b.foundationLocal(lx, lz);
				boolean border = Math.abs(lx) == 2 || lz == 3 || lz == 7;
				b.setLocal(lx, GROUND, lz, border ? STONE_BRICK : STAINED_CLAY, border ? CHISELED : team);
				if (!border) b.setLocal(lx, GROUND + 1, lz, CARPET, team);
			}
		}
		b.setLocal(0, GROUND + 1, 5, QUARTZ, QUARTZ_CHISELED);
		site.setFlagLoc(b.standLocal(0, 5, 2, false));

		// Lit path from the dais to the front edge
		for (int lz = 8; lz <= 22; lz++) {
			for (int lx = -1; lx <= 1; lx++) {
				if (!b.hasLocal(lx, lz)) continue;
				b.setLocal(lx, GROUND, lz, STONE_BRICK, lx == 0 && lz % 4 == 0 ? CHISELED : b.randomLocal(lx, lz) < 0.18 ? MOSSY : 0);
			}
		}
		b.lampLocal(-2, 10);
		b.lampLocal(2, 10);
		b.lampLocal(-2, 15);
		b.lampLocal(2, 15);

		// Trees, boulders, then hills, grass and flowers wherever's free
		b.bigTreeLocal(-10, -14, 7, 3, 0);
		b.bigTreeLocal(10, -14, 7, 3, 0);
		b.treeLocal(-9, 8, 2, 6);
		b.treeLocal(9, 9, 0, 5);
		b.bushLocal(-12, 1);
		b.bushLocal(5, 14);
		b.boulderLocal(12, 2, 2);
		b.boulderLocal(-6, 15, 1);
		b.decorate(b.bx, b.bz, 19, 0.08, 0.6);

		// Island boundary (for traps and the heal pool), and no-build areas at the forge, spawn and shop
		site.setBoundaryCorner1(b.cornerLocal(-17, -22, GROUND - 30));
		site.setBoundaryCorner2(b.cornerLocal(17, 22, GROUND + 30));
		site.setImportantArea(1, b.areaLocal(0, -15, 4, 4, 3));
		site.setImportantArea(2, b.areaLocal(0, -7, 2, 2, 2));
		site.setImportantArea(3, b.areaLocal(-8, -7, 1, 2, 1));

		return hologramId;
	}

	/** The forge: a team-colored building at the back of the base with the generators and chests inside. */
	private int buildForge(Build b, FWMapConfig config, FWTeamSite site, int team, int hologramId) {
		for (int lx = -6; lx <= 6; lx++) {
			for (int lz = -19; lz <= -11; lz++) b.foundationLocal(lx, lz);
		}
		for (int lx = -5; lx <= 5; lx++) {
			for (int lz = -18; lz <= -12; lz++) {
				boolean wall = Math.abs(lx) == 5 || lz == -18;
				b.setLocal(lx, GROUND, lz, wall ? STONE_BRICK : DOUBLE_SLAB, 0);
				if (!wall) continue;
				boolean frontPillar = lz == -12;
				for (int y = GROUND + 1; y <= GROUND + 4; y++) {
					if (frontPillar) {
						b.setLocal(lx, y, lz, QUARTZ, QUARTZ_PILLAR);
					} else if (y == GROUND + 3) {
						b.setLocal(lx, y, lz, STAINED_CLAY, team);
					} else if (y == GROUND + 2 && Math.abs(lx) == 5 && lz >= -16 && lz <= -14) {
						b.setLocal(lx, y, lz, STAINED_GLASS, team); // side windows
					} else {
						b.setLocal(lx, y, lz, STONE_BRICK, b.randomLocal(lx * 7 + y, lz) < 0.15 ? MOSSY : 0);
					}
				}
			}
		}
		// Sea lanterns set into the back wall
		b.setLocal(-3, GROUND + 2, -18, SEA_LANTERN, 0);
		b.setLocal(3, GROUND + 2, -18, SEA_LANTERN, 0);

		// Roof: a stone brick ceiling with a slab rim, then a stepped team-colored top
		for (int lx = -6; lx <= 6; lx++) {
			for (int lz = -19; lz <= -11; lz++) {
				boolean rim = Math.abs(lx) == 6 || lz == -19 || lz == -11;
				b.setLocal(lx, GROUND + 5, lz, rim ? SLAB : STONE_BRICK, rim ? STONE_BRICK_SLAB : 0);
				if (Math.abs(lx) <= 4 && lz >= -17 && lz <= -13) b.setLocal(lx, GROUND + 6, lz, STAINED_CLAY, team);
				if (Math.abs(lx) <= 2 && lz >= -16 && lz <= -14) b.setLocal(lx, GROUND + 7, lz, WOOL, team);
			}
		}
		b.setLocal(0, GROUND + 8, -15, GLOWSTONE, 0);

		// Generators on iron and gold blocks
		b.setLocal(-1, GROUND, -15, IRON_BLOCK, 0);
		b.setLocal(1, GROUND, -15, GOLD_BLOCK, 0);
		site.setIronGenerator(b.standLocal(-1, -15, 1, false));
		site.setGoldGenerator(b.standLocal(1, -15, 1, false));

		// Team chest and ender chest just inside, with their Punch to Deposit holograms
		b.setLocal(-4, GROUND + 1, -13, CHEST, b.facingData());
		b.setLocal(4, GROUND + 1, -13, ENDER_CHEST, b.facingData());
		config.setChestHologram(hologramId++, b.standLocal(-4, -13, 1, false).add(0, 1.2, 0));
		config.setChestHologram(hologramId++, b.standLocal(4, -13, 1, false).add(0, 1.2, 0));
		return hologramId;
	}

	/** A raised spawn plaza in front of the forge, with the shop stall on one side and an anvil stall on the other. */
	private void buildSpawnPlaza(Build b, FWTeamSite site, int team) {
		for (int lx = -11; lx <= 11; lx++) {
			for (int lz = -11; lz <= -3; lz++) {
				b.foundationLocal(lx, lz);
				boolean border = Math.abs(lx) == 11 || lz == -11 || lz == -3;
				boolean aisle = lx == 0 && lz >= -10 && lz <= -4;
				b.setLocal(lx, GROUND, lz, STONE, 0);
				if (border) {
					boolean stripe = (Math.abs(lx) + Math.abs(lz)) % 4 < 2;
					b.setLocal(lx, GROUND + 1, lz, stripe ? STAINED_CLAY : STONE_BRICK, stripe ? team : 0);
				}
				else if (aisle) b.setLocal(lx, GROUND + 1, lz, STAINED_CLAY, team);
				else b.setLocal(lx, GROUND + 1, lz, STONE, POLISHED_ANDESITE);
			}
		}
		// Half-slab step down towards the flag
		for (int lx = -2; lx <= 2; lx++) b.setLocal(lx, GROUND + 1, -2, SLAB, STONE_BRICK_SLAB);
		site.setSpawn(b.standLocal(0, -7, 2, true));

		b.stallLocal(-8, -7, team, GROUND + 1);
		site.setShopLoc(b.standLocal(-8, -7, 2, false));
		b.stallLocal(8, -7, team, GROUND + 1);
		b.setLocal(8, GROUND + 2, -7, ANVIL, 0);
		b.setLocal(9, GROUND + 2, -8, BOOKSHELF, 0);
		b.setLocal(7, GROUND + 2, -6, CAULDRON, 0);
		b.setLocal(9, GROUND + 2, -6, HAY, 0);

		// Hedge planters on the plaza
		int[][] planters = {{-4, -10}, {4, -10}, {-4, -4}, {4, -4}};
		for (int[] planter : planters) {
			b.setLocal(planter[0], GROUND + 1, planter[1], GRASS, 0);
			b.setLocal(planter[0], GROUND + 2, planter[1], LEAVES, 4);
		}

		// Lanterns at the plaza's front corners
		b.lampLocal(-11, -2);
		b.lampLocal(11, -2);
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

		void clear(int x, int y, int z) {
			blocks.remove(key(x, y, z));
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
					// A rocky point underneath, with a few stalactites hanging off it
					double stalactite = d < 0.55 && random(x, 5, z) < 0.03 ? random(x, 6, z) * 4 : 0;
					int columnDepth = 2 + (int) Math.round(Math.pow(1 - d, 0.7) * depth + random(x, 0, z) * 2.5 + stalactite);
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
					else if (roll < 0.20) set(x, y, z, STONE, ANDESITE);
					else if (roll < 0.30) set(x, y, z, COBBLE, 0);
					else set(x, y, z, STONE, 0);
				}
			}
		}

		/** Makes sure a structure's block has ground under it, even past the island's wavy edge. */
		void foundation(int x, int z) {
			if (!has(x, GROUND, z)) column(x, z, 5);
		}

		/**
		 * Gentle hills (1-2 blocks) on the open grass from hillStart (0..1 of the radius) out, then grass tufts and
		 * flowers on top. Hills keep a block away from anything built, so paths and stalls stay flat to walk on.
		 */
		void decorate(int cx, int cz, double radius, double wobble, double hillStart) {
			int reach = (int) Math.ceil(radius * (1 + wobble * 1.8)) + 1;
			double p = (seed & 0xFF) / 40.0;
			List<int[]> raise = new ArrayList<>();
			for (int x = cx - reach; x <= cx + reach; x++) {
				for (int z = cz - reach; z <= cz + reach; z++) {
					if (!isOpenGrass(x, z)) continue;
					double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz)) / radius;
					if (d < hillStart || !openAround(x, z)) continue;
					double hill = Math.sin(x * 0.29 + p) * Math.cos(z * 0.25 - p) + (random(x, 7, z) - 0.5) * 0.3;
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

		/** A big round-crowned tree: a thick trunk base with roots and a layered canopy. */
		void bigTree(int x, int z, int height, int radius, int woodData) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) foundation(x + dx, z + dz);
			}
			set(x, GROUND, z, DIRT, 0);
			for (int y = 1; y <= height; y++) set(x, GROUND + y, z, LOG, woodData);
			// Roots
			int[][] roots = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
			for (int[] root : roots) {
				if (random(x + root[0], 9, z + root[1]) < 0.6) set(x + root[0], GROUND + 1, z + root[1], LOG, woodData);
			}
			// Canopy: a squashed sphere around the top of the trunk
			int cy = GROUND + height;
			for (int dy = -2; dy <= 2; dy++) {
				int r = dy <= 0 ? radius : radius - dy;
				for (int dx = -r; dx <= r; dx++) {
					for (int dz = -r; dz <= r; dz++) {
						double dist = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.5);
						if (dist > r + 0.5) continue;
						if (dist > r - 0.5 && random(x + dx, cy + dy, z + dz) < 0.35) continue; // ragged edge
						if (dx == 0 && dz == 0 && dy <= 0) continue; // trunk
						if (!has(x + dx, cy + dy, z + dz)) set(x + dx, cy + dy, z + dz, LEAVES, woodData | 4);
					}
				}
			}
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

		/** A mossy boulder half sunk into the ground. */
		void boulder(int x, int z, int size) {
			for (int dx = -size; dx <= size; dx++) {
				for (int dz = -size; dz <= size; dz++) {
					foundation(x + dx, z + dz);
					for (int dy = 0; dy <= size; dy++) {
						double dist = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.6);
						if (dist > size + 0.4 || (dist > size - 0.4 && random(x + dx, dy, z + dz) < 0.4)) continue;
						double roll = random(x + dx, GROUND + 1 + dy, z + dz);
						set(x + dx, GROUND + 1 + dy, z + dz, roll < 0.4 ? MOSSY_COBBLE : roll < 0.7 ? STONE : COBBLE,
								roll >= 0.4 && roll < 0.55 ? ANDESITE : 0);
					}
				}
			}
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
				set(x, GROUND + y, z, y == height ? COBBLE_WALL : STONE_BRICK, y == height ? 1 : random(x, y, z) < 0.5 ? MOSSY : CRACKED);
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

		void bigTreeLocal(int lx, int lz, int height, int radius, int woodData) {
			bigTree(wx(lx, lz), wz(lx, lz), height, radius, woodData);
		}

		void bushLocal(int lx, int lz) {
			bush(wx(lx, lz), GROUND + 1, wz(lx, lz));
		}

		void boulderLocal(int lx, int lz, int size) {
			boulder(wx(lx, lz), wz(lx, lz), size);
		}

		void lampLocal(int lx, int lz) {
			lampPost(wx(lx, lz), wz(lx, lz));
		}

		/** A 3x3 market stall on floorY: plank floor, fence posts and a striped awning with carpet on top. */
		void stallLocal(int lx, int lz, int team, int floorY) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					foundationLocal(lx + dx, lz + dz);
					setLocal(lx + dx, floorY, lz + dz, PLANKS, 1);
					if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
						setLocal(lx + dx, floorY + 1, lz + dz, FENCE, 0);
						setLocal(lx + dx, floorY + 2, lz + dz, FENCE, 0);
					}
					int stripe = dx == 0 ? 0 : team;
					setLocal(lx + dx, floorY + 3, lz + dz, WOOL, stripe);
					setLocal(lx + dx, floorY + 4, lz + dz, CARPET, stripe);
				}
			}
		}

		/** Where a player stands on this block (its center), `up` blocks above GROUND, facing mid if asked. */
		Location standLocal(int lx, int lz, int up, boolean faceMid) {
			float yaw = faceMid ? (float) Math.toDegrees(Math.atan2(-fx, fz)) : 0;
			return new Location(world, wx(lx, lz) + 0.5, GROUND + up, wz(lx, lz) + 0.5, yaw, 0);
		}

		Location cornerLocal(int lx, int lz, int y) {
			return new Location(world, wx(lx, lz), y, wz(lx, lz));
		}

		/** A no-build area centered on this block, sideRadius to the side and forwardRadius towards mid. */
		FWImportantArea areaLocal(int lx, int lz, int sideRadius, int yRadius, int forwardRadius) {
			boolean alongZ = fx == 0;
			return new FWImportantArea(standLocal(lx, lz, 1, false),
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
