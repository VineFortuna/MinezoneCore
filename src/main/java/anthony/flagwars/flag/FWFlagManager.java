package anthony.flagwars.flag;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.titles.TitleSequence;
import anthony.SuperCraftBrawl.titles.TitleUtil;
import anthony.flagwars.FWGameConstants;
import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWTeam;
import anthony.flagwars.map.FWMapConfig;
import anthony.flagwars.map.FWTeamSite;
import anthony.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spawns and tracks each team's flag (a colored banner block at their base)
 * and shopkeeper Villager, and handles stealing/carrying/capturing/returning
 * flags. The shopkeeper is pinned in place every tick - no wandering, no
 * knockback; the flag is a static block so it needs no such pinning.
 *
 * A flag is never actually broken or mined: stealing is triggered by a plain
 * left-click (see FWPlayerListener#onLeftClickFlag), which routes straight
 * into attemptSteal and cancels the interact event - the block never takes
 * any damage, avoiding the known glitchiness of breaking/re-rendering 1.8
 * banner blocks on newer client versions, and matching "replace it with a
 * new one" immediately since it was never actually removed in the first place.
 */
public class FWFlagManager {

	private final FWGameInstance instance;
	private final Map<Integer, Location> flagLocs = new HashMap<>();
	private final Map<Integer, Villager> shopkeepers = new HashMap<>();
	private final Map<Entity, Location> pinned = new LinkedHashMap<>();
	private BukkitTask pinTask;

	/** carrierUUID -> the site id of the flag they're currently carrying. */
	private final Map<UUID, Integer> carrying = new HashMap<>();
	/** carrierUUID -> their real helmet, saved while a flag banner occupies that slot. */
	private final Map<UUID, ItemStack> savedHelmets = new HashMap<>();

	public FWFlagManager(FWGameInstance instance) {
		this.instance = instance;
	}

	public void spawnAll() {
		World world = instance.getWorld();
		FWMapConfig config = instance.getMapConfig();

		for (FWTeam team : instance.getTeams().values()) {
			if (team.isEmpty()) continue;

			FWTeamSite site = config.getTeamSite(team.getSiteId());

			placeFlagBlock(site.getFlagLoc(), team);
			// getFlagLoc() is wherever staff stood during /fwsetup flag (fractional X/Z) - snap to
			// the actual placed block's corner so siteIdLookingAt's cylinder is centered correctly.
			flagLocs.put(team.getSiteId(), site.getFlagLoc().getBlock().getLocation());

			Villager shopkeeper = (Villager) world.spawnEntity(site.getShopLoc(), EntityType.VILLAGER);
			shopkeeper.setCustomName(instance.getManager().getMain().color("&6&lShop & Upgrades"));
			shopkeeper.setCustomNameVisible(true);
			shopkeeper.setRemoveWhenFarAway(false);
			shopkeepers.put(team.getSiteId(), shopkeeper);
			pinned.put(shopkeeper, site.getShopLoc().clone());
		}

		startPinning();
	}

	private void placeFlagBlock(Location loc, FWTeam team) {
		Block block = loc.getBlock();
		block.setType(Material.STANDING_BANNER);

		BlockState state = block.getState();
		if (state instanceof Banner) {
			Banner banner = (Banner) state;
			banner.setBaseColor(team.getColor());
			banner.update(true);
		}
	}

	/** Forces every tracked shopkeeper back to its spawn spot and zeroes velocity, so it never wanders or shows knockback. */
	private void startPinning() {
		if (pinTask != null) return;

		pinTask = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), () -> {
			for (Map.Entry<Entity, Location> entry : pinned.entrySet()) {
				Entity entity = entry.getKey();
				if (entity.isDead()) continue;

				Location pin = entry.getValue();
				Location current = entity.getLocation();
				if (current.getX() != pin.getX() || current.getY() != pin.getY() || current.getZ() != pin.getZ()) {
					entity.teleport(pin);
				}
				entity.setVelocity(new Vector(0, 0, 0));
			}
		}, 0L, 1L);
	}

	public void despawnAll() {
		if (pinTask != null) {
			pinTask.cancel();
			pinTask = null;
		}
		pinned.clear();

		for (Location loc : flagLocs.values()) {
			loc.getBlock().setType(Material.AIR);
		}
		flagLocs.clear();

		for (Villager shopkeeper : shopkeepers.values()) {
			shopkeeper.remove();
		}
		shopkeepers.clear();

		// In case the match ended mid-carry, give back any real helmets still held hostage.
		for (Map.Entry<UUID, ItemStack> entry : savedHelmets.entrySet()) {
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null) player.getInventory().setHelmet(entry.getValue());
		}
		carrying.clear();
		savedHelmets.clear();
	}

	public Integer findSiteByShopkeeper(Entity entity) {
		for (Map.Entry<Integer, Villager> entry : shopkeepers.entrySet()) {
			if (entry.getValue().equals(entity)) return entry.getKey();
		}
		return null;
	}

	/** The shopkeeper entity for a given site, or null if it's not currently spawned. */
	public Villager getShopkeeper(int siteId) {
		return shopkeepers.get(siteId);
	}

	/**
	 * Half-width of a standing banner's actual cloth/cap model, taken from vanilla's own
	 * ModelBanner: a 20px-wide box, scaled by TileEntityBannerRenderer's 2/3 render scale
	 * and the model's 1/16-per-pixel unit scale (20 * 2/3 * 1/16 = 5/6 blocks wide).
	 */
	private static final double FLAG_RADIUS = 5.0 / 12.0;

	/**
	 * Full height of a standing banner's pole+cap model above its block's base, derived the
	 * same way: the pole touches the ground, and the cap/cloth's top sits 1 + 5/6 blocks up.
	 */
	private static final double FLAG_HEIGHT = 11.0 / 6.0;

	/**
	 * Returns the site id of whichever tracked flag the player is aiming at within
	 * reach, or null if none. Checks the look-ray against a vertical cylinder sized
	 * to vanilla's real banner model (FLAG_RADIUS, FLAG_HEIGHT) - centered on the
	 * block and rooted at its base - instead of the block's own (much smaller)
	 * collision hitbox or a full block's footprint. Aiming at blank space next to
	 * or above the visible flag no longer registers, while the whole visible
	 * pole/cloth, top to bottom, does.
	 *
	 * This is a real ray/finite-cylinder intersection (solve for the whole t-range where the
	 * ray's horizontal projection is within FLAG_RADIUS, then clip that range by the height band
	 * and by reach) rather than just checking the single closest-horizontal-approach point. The
	 * old single-point check could reject a genuine hit near the bottom or corners of the hitbox:
	 * aiming steeply down, the ray's closest horizontal approach to the column's axis can land at
	 * a t whose height has already dropped below the column's base, even though the ray clearly
	 * passed through the column's actual volume a little earlier (smaller t, still in-band).
	 */
	public Integer siteIdLookingAt(Player player, double reach) {
		Location eye = player.getEyeLocation();
		double ox = eye.getX(), oy = eye.getY(), oz = eye.getZ();
		Vector direction = eye.getDirection().normalize();
		double dx = direction.getX(), dy = direction.getY(), dz = direction.getZ();

		Integer closestSite = null;
		double closestEntryT = reach;

		for (Map.Entry<Integer, Location> entry : flagLocs.entrySet()) {
			Location flagLoc = entry.getValue();
			if (!flagLoc.getWorld().equals(eye.getWorld())) continue;

			double horizDirSq = dx * dx + dz * dz;
			if (horizDirSq < 1.0E-6) continue; // looking straight up/down - no meaningful column hit

			double x0 = flagLoc.getX() + 0.5;
			double z0 = flagLoc.getZ() + 0.5;

			// Ray vs. infinite cylinder of radius FLAG_RADIUS around (x0, z0): solve
			// |((ox,oz) + t*(dx,dz)) - (x0,z0)| <= FLAG_RADIUS for the entry/exit t-range.
			double ax = ox - x0, az = oz - z0;
			double a = horizDirSq;
			double b = 2 * (ax * dx + az * dz);
			double c = ax * ax + az * az - FLAG_RADIUS * FLAG_RADIUS;
			double discriminant = b * b - 4 * a * c;
			if (discriminant < 0) continue; // ray's horizontal line never comes within radius of the axis

			double sqrtDisc = Math.sqrt(discriminant);
			double tEnter = (-b - sqrtDisc) / (2 * a);
			double tExit = (-b + sqrtDisc) / (2 * a);

			// Clip that range to the column's actual height band [flagLoc.Y, flagLoc.Y + FLAG_HEIGHT].
			double minY = flagLoc.getY();
			double maxY = flagLoc.getY() + FLAG_HEIGHT;

			if (Math.abs(dy) < 1.0E-9) {
				if (oy < minY || oy > maxY) continue; // looking flat, and not even level with the column
			} else {
				double tAtMinY = (minY - oy) / dy;
				double tAtMaxY = (maxY - oy) / dy;
				tEnter = Math.max(tEnter, Math.min(tAtMinY, tAtMaxY));
				tExit = Math.min(tExit, Math.max(tAtMinY, tAtMaxY));
			}

			// And clip to reach.
			tEnter = Math.max(tEnter, 0);
			tExit = Math.min(tExit, reach);

			if (tEnter > tExit) continue; // no overlap left between the cylinder, the height band and reach - genuine miss
			if (tEnter >= closestEntryT) continue; // a closer flag already claimed this hit

			// Checked against the actual point the ray enters the cylinder - i.e. wherever the
			// crosshair is really landing - not a fixed point like the flag's center. A fixed
			// center used to cause its own bugs: a block sitting right above the banner could
			// block line-of-sight to the (higher, fixed) center even while aiming at a perfectly
			// clear lower section, and vice versa. hasLineOfSight excludes the flag's own block
			// from the obstruction check below, so this is safe to aim anywhere on the column.
			Location actualHit = new Location(eye.getWorld(), ox + tEnter * dx, oy + tEnter * dy, oz + tEnter * dz);
			if (!hasLineOfSight(eye, actualHit, flagLoc.getBlock())) continue; // walls/blocks between the player and the flag block the steal

			closestEntryT = tEnter;
			closestSite = entry.getKey();
		}

		return closestSite;
	}

	/**
	 * Walks every single voxel the segment from "from" to "to" actually passes through - a proper
	 * 3D grid traversal (Amanatides & Woo), not fixed-distance sampling - and blocks the steal if
	 * any of them is opaque (a wall, a placed defense, terrain, etc.), same as you'd expect from
	 * a real line of sight. The previous version stepped in fixed 0.2-block Euclidean increments,
	 * which is only an approximation: it's mathematically possible for those sample points to
	 * land entirely on either side of a thin gap or a block's corner without a single one ever
	 * landing inside its voxel, letting the ray "thread the needle" through solid blocks - exactly
	 * what was happening at close range and near corners. Stepping through actual grid-line
	 * crossings instead guarantees every voxel along the path gets checked, with no gaps possible.
	 */
	private boolean hasLineOfSight(Location from, Location to, Block flagBlock) {
		World world = from.getWorld();
		double x0 = from.getX(), y0 = from.getY(), z0 = from.getZ();
		double dx = to.getX() - x0, dy = to.getY() - y0, dz = to.getZ() - z0;
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance < 1.0E-6) return true;

		dx /= distance;
		dy /= distance;
		dz /= distance;

		int x = (int) Math.floor(x0);
		int y = (int) Math.floor(y0);
		int z = (int) Math.floor(z0);

		int stepX = dx > 1.0E-9 ? 1 : (dx < -1.0E-9 ? -1 : 0);
		int stepY = dy > 1.0E-9 ? 1 : (dy < -1.0E-9 ? -1 : 0);
		int stepZ = dz > 1.0E-9 ? 1 : (dz < -1.0E-9 ? -1 : 0);

		double tMaxX = stepX != 0 ? ((stepX > 0 ? (x + 1 - x0) : (x0 - x)) / Math.abs(dx)) : Double.POSITIVE_INFINITY;
		double tMaxY = stepY != 0 ? ((stepY > 0 ? (y + 1 - y0) : (y0 - y)) / Math.abs(dy)) : Double.POSITIVE_INFINITY;
		double tMaxZ = stepZ != 0 ? ((stepZ > 0 ? (z + 1 - z0) : (z0 - z)) / Math.abs(dz)) : Double.POSITIVE_INFINITY;

		double tDeltaX = stepX != 0 ? 1.0 / Math.abs(dx) : Double.POSITIVE_INFINITY;
		double tDeltaY = stepY != 0 ? 1.0 / Math.abs(dy) : Double.POSITIVE_INFINITY;
		double tDeltaZ = stepZ != 0 ? 1.0 / Math.abs(dz) : Double.POSITIVE_INFINITY;

		double t = 0;
		while (t < distance) {
			Block block = world.getBlockAt(x, y, z);
			if (!block.equals(flagBlock) && !block.getType().isTransparent()) {
				return false;
			}

			if (tMaxX < tMaxY && tMaxX < tMaxZ) {
				t = tMaxX;
				x += stepX;
				tMaxX += tDeltaX;
			} else if (tMaxY < tMaxZ) {
				t = tMaxY;
				y += stepY;
				tMaxY += tDeltaY;
			} else {
				t = tMaxZ;
				z += stepZ;
				tMaxZ += tDeltaZ;
			}
		}
		return true;
	}

	public boolean isCarrying(Player player) {
		return carrying.containsKey(player.getUniqueId());
	}

	/**
	 * Called when a player left-clicks a flag block. Blocks self-steals, carrying
	 * two flags at once (only one helmet slot to hold one in), and re-capturing
	 * the same victim a second time - everyone else stays free to steal it.
	 */
	public void attemptSteal(int ownerSiteId, Player attacker) {
		Core main = instance.getManager().getMain();
		FWTeam ownerTeam = instance.getTeams().get(ownerSiteId);
		FWTeam attackerTeam = instance.findTeamOf(attacker);
		if (ownerTeam == null || attackerTeam == null) return;

		if (ownerTeam == attackerTeam) {
			attacker.sendMessage(main.color("&c&l(!) &rYou can't steal your own flag!"));
			return;
		}

		if (carrying.containsKey(attacker.getUniqueId())) {
			attacker.sendMessage(main.color("&c&l(!) &rYou're already carrying a flag! Capture or lose it first."));
			return;
		}

		if (attackerTeam.hasCapturedFrom(ownerSiteId)) {
			attacker.sendMessage(main.color("&c&l(!) &rYou've already captured this flag! Steal from someone else."));
			return;
		}

		carrying.put(attacker.getUniqueId(), ownerSiteId);
		savedHelmets.put(attacker.getUniqueId(), attacker.getInventory().getHelmet());
		attacker.getInventory().setHelmet(flagBannerItem(ownerTeam));

		String attackerColor = FWGameConstants.chatColorCodeFor(attackerTeam.getColor());
		String ownerColor = FWGameConstants.chatColorCodeFor(ownerTeam.getColor());

		instance.TellAll("");
		instance.TellAll(main.color("&2&l(!) &r" + attackerColor + attacker.getName()
				+ " &rhas stolen " + ownerColor + ownerTeam.getName() + " Team's &rflag!"));
		instance.TellAll("");
		instance.playSoundForAll(Sound.ENDERDRAGON_GROWL, 0.5f, 1.6f);
		instance.refreshScoreboards();

		TitleUtil.sendTitle(attacker, "", main.color("&rCarrying " + ownerColor + ownerTeam.getName() + " Team's &rflag"), 0, 60, 0);
		sendTeamTitle(ownerTeam, "&c&lFLAG STOLEN!", "&rKill &a" + attacker.getName() + " &rto retrieve!");
	}

	/** Called from FWPlayerListener#onPlayerMove once a carrier walks back into their own base's capture radius. */
	public void captureFlag(Player carrier) {
		Integer ownerSiteId = carrying.remove(carrier.getUniqueId());
		if (ownerSiteId == null) return;

		restoreHelmet(carrier);

		FWTeam carrierTeam = instance.findTeamOf(carrier);
		if (carrierTeam == null) return;
		FWTeam ownerTeam = instance.getTeams().get(ownerSiteId);

		carrierTeam.markCapturedFrom(ownerSiteId);
		carrierTeam.addCapturedFlag();
		instance.addFlagCapture(carrier);

		int required = instance.getRequiredCaptures();
		Core main = instance.getManager().getMain();

		String carrierColor = FWGameConstants.chatColorCodeFor(carrierTeam.getColor());
		String ownerColor = ownerTeam != null ? FWGameConstants.chatColorCodeFor(ownerTeam.getColor()) : "&7";
		String ownerName = ownerTeam != null ? ownerTeam.getName() : "a";

		instance.TellAll("");
		instance.TellAll(main.color("&2&l(!) &r" + carrierColor + carrier.getName()
				+ " &rsuccessfully captured " + ownerColor + ownerName + " Team's &rflag! &a("
				+ carrierTeam.getCapturedFlags() + "/" + required + ")"));
		instance.TellAll("");
		instance.playSoundForAll(Sound.LEVEL_UP, 1f, 1f);
		carrier.sendTitle(main.color("&6&lFLAG CAPTURED"),
				main.color("&e" + carrierTeam.getCapturedFlags() + "&7/&e" + required));
		if (ownerTeam != null) {
			sendTeamTitle(ownerTeam, "&c&lFLAG CAPTURED", "&a" + carrier.getName() + " &rcaptured your flag!");
		}

		instance.refreshScoreboards();
		instance.onFlagCaptured(carrierTeam);
	}

	/** Called when a carrier dies (by their flag's owner, by a third party, or the void) or leaves the game before capturing. */
	public void returnCarriedFlag(Player carrier, Player killer) {
		Integer ownerSiteId = carrying.remove(carrier.getUniqueId());
		if (ownerSiteId == null) return;

		restoreHelmet(carrier);

		FWTeam ownerTeam = instance.getTeams().get(ownerSiteId);
		Core main = instance.getManager().getMain();

		if (ownerTeam != null && killer != null && ownerTeam.isMember(killer)) {
			instance.TellAll(main.color("&a&l(!) &e" + killer.getName() + " &rrecovered their flag from &e"
					+ carrier.getName() + "&r!"));
			instance.playSoundForAll(Sound.ANVIL_LAND, 0.7f, 1f);
			sendTitleTo(killer, "&e&lFLAG RETRIEVED", "&rYou retrieved your flag from &a" + carrier.getName());
		} else {
			instance.TellAll(main.color("&e&l(!) &r" + carrier.getName() + "'s stolen flag returned to &e"
					+ (ownerTeam != null ? ownerTeam.getName() : "its owner") + "&r's base."));
		}

		instance.refreshScoreboards();
	}

	/** Same TitleSequence call shape used elsewhere (e.g. the join-banner titles), just a one-spec sequence rather than a chained pair - title stays on screen for 3 seconds (60 ticks). */
	private void sendTitleTo(Player player, String title, String subtitle) {
		TitleSequence.sendSequence(instance.getManager().getMain(), player,
				Collections.singletonList(new TitleSequence.TitleSpec(title, subtitle, 10, 60, 10)));
	}

	private void sendTeamTitle(FWTeam team, String title, String subtitle) {
		for (UUID uuid : team.getMembers()) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) sendTitleTo(p, title, subtitle);
		}
	}

	private void restoreHelmet(Player player) {
		ItemStack saved = savedHelmets.remove(player.getUniqueId());
		if (player.isOnline()) player.getInventory().setHelmet(saved);
	}

	private ItemStack flagBannerItem(FWTeam ownerTeam) {
		ItemStack item = new ItemStack(Material.BANNER, 1);
		BannerMeta meta = (BannerMeta) item.getItemMeta();
		meta.setBaseColor(ownerTeam.getColor());
		item.setItemMeta(meta);

		Core main = instance.getManager().getMain();
		return ItemHelper.setDetails(item, main.color("&e" + ownerTeam.getName() + "'s Flag"),
				Collections.singletonList(main.color("&7Return it to your base to capture it!")));
	}
}
