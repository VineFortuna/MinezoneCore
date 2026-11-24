package anthony.flagwars;

import anthony.flagwars.map.FWTeamSite;
import anthony.flagwars.shop.FWTrap;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Decoy Flag Trap. The moment it is bought, the team's real flag is hidden at a random spot on their
 * island and a decoy banner is left at the flag's normal spot (it is never queued like the other traps).
 * The first enemy to grab the decoy hears a cackle and is thrown backwards and up, the team gets the usual
 * "triggered" chat line and the decoy is taken down. The real flag stays at its hidden spot, it does not go back
 * to its normal one. The real flag can be taken at its hidden spot like any flag, and doing that cancels the
 * decoy. It waits until someone tries. A team can buy the trap again once its flag is back at its normal spot.
 */
public class FWDecoyFlags {

	/**
	 * The real flag hides about 3 to 4 blocks away sideways from its normal spot (the distance rounds to 3 or
	 * 4, so 2.5 up to but not including 4.5), and up or down by at most HIDE_HEIGHT. It is never straight in
	 * front of, behind, or beside the normal spot, only off to a corner, like 2 forward and 2 to the left.
	 */
	private static final double HIDE_MIN_DISTANCE = 2.5;
	private static final double HIDE_MAX_DISTANCE = 4.5;
	private static final int HIDE_HEIGHT = 2;
	/** Same reach as taking a real flag (see FWPlayerListener#onClickFlag). */
	private static final double GRAB_REACH = 5.0;
	/** How long the real flag can't be taken after the decoy is grabbed, so one click can't take both flags. */
	private static final long REAL_FLAG_LOCK_MILLIS = 500L;
	/** Like the Iron Golem's Rose of Elevation: sent back with an upward speed of 1.0. */
	private static final double PUSH_HORIZONTAL = 1.0;
	private static final double PUSH_VERTICAL = 1.0;

	private static final class Decoy {
		final FWTeam owner;
		/** The flag's normal spot. The decoy banner stands here. */
		final Location home;

		Decoy(FWTeam owner, Location home) {
			this.owner = owner;
			this.home = home;
		}
	}

	private final FWGameInstance instance;
	/** Owner site id to its active decoy. An entry is removed when the decoy is grabbed, the flag is stolen or the match ends. */
	private final Map<Integer, Decoy> decoys = new HashMap<>();

	public FWDecoyFlags(FWGameInstance instance) {
		this.instance = instance;
	}

	/**
	 * Why the team can't set a decoy right now, or null if it can: the flag is already hidden behind a
	 * decoy, isn't at home (stolen), or there is no valid spot to hide it. Short plain text for the shop
	 * to show, so a purchase can be refused before any diamonds are taken.
	 */
	public String problem(FWTeam owner) {
		int siteId = owner.getSiteId();
		if (decoys.containsKey(siteId)) return "Your flag is already hidden behind a decoy.";

		FWTeamSite site = instance.getMapConfig().getTeamSite(siteId);
		if (site.getFlagLoc() == null) return "Your flag has no home spot.";
		Location home = site.getFlagLoc().getBlock().getLocation();

		Location current = instance.getFlagManager().getFlagLocation(siteId);
		if (current == null || !sameBlock(current, home)) return "Your flag isn't at home.";

		if (findHidingSpot(site, home.getBlock()) == null) return "There is no room to hide your flag.";
		return null;
	}

	/**
	 * Hides the team's real flag and leaves the decoy in its place, right now. The shop checks problem()
	 * first, so this does nothing only if something changed in between.
	 */
	public void arm(FWTeam owner) {
		if (problem(owner) != null) return;

		int siteId = owner.getSiteId();
		FWTeamSite site = instance.getMapConfig().getTeamSite(siteId);
		Location home = site.getFlagLoc().getBlock().getLocation();
		Block hiding = findHidingSpot(site, home.getBlock());
		if (hiding == null) return;

		// The banner at home stays where it is and becomes the decoy. The real one goes up at the new spot.
		instance.getFlagManager().moveFlag(siteId, hiding.getLocation());
		decoys.put(siteId, new Decoy(owner, home));

		home.getWorld().playSound(home, Sound.ENDERMAN_TELEPORT, 1f, 1.5f);
		tellTeam(owner, "&2&l(!) &rYour flag moved. A decoy is at its old spot.");
	}


	/**
	 * Called when a player clicks. Returns true if they were aiming at a decoy, so the click shouldn't do
	 * anything else. The rejections use the same words as a real flag so nobody can tell them apart.
	 */
	public boolean click(Player player) {
		if (decoys.isEmpty() || player.getGameMode() != GameMode.SURVIVAL) return false;

		FWTeam team = instance.findTeamOf(player);
		if (team == null) return false;

		for (Decoy decoy : new ArrayList<>(decoys.values())) {
			if (!instance.getFlagManager().isLookingAtBanner(player, decoy.home, GRAB_REACH)) continue;

			if (team == decoy.owner) {
				player.sendMessage(instance.getManager().getMain().color("&c&l(!) &rYou can't steal your own flag!"));
			} else if (instance.getFlagManager().isCarrying(player)) {
				player.sendMessage(instance.getManager().getMain().color(
						"&c&l(!) &rYou're already carrying a flag! Capture or lose it first."));
			} else {
				spring(player, decoy);
			}
			return true;
		}
		return false;
	}

	/** The witch laughs, the grabber is thrown back and up and the decoy goes away. The real flag stays hidden where it is. */
	private void spring(Player grabber, Decoy decoy) {
		decoys.remove(decoy.owner.getSiteId());

		Location banner = decoy.home.clone().add(0.5, 1, 0.5);
		playCackle(banner);
		banner.getWorld().playEffect(banner, Effect.CLOUD, 0);
		grabber.setVelocity(pushBack(grabber, decoy.home));
		// The same chat lines every other trap sends when someone sets it off
		instance.getTrapManager().announceTriggered(decoy.owner, grabber, FWTrap.DECOY_FLAG);

		// The decoy banner is used up. The real flag stays at its hidden spot. It can't be taken for a moment, so
		// the click that sprang the decoy (or its twin event) can't also take the real flag if they're in line.
		instance.getFlagManager().lockSteals(decoy.owner.getSiteId(), REAL_FLAG_LOCK_MILLIS);
		removeBanner(decoy.home);
	}

	/**
	 * The witch laugh. The 1.8 client has no witch sounds at all (witches were silent until 1.9), so this is
	 * a stand-in: three quick villager "hm" sounds climbing in pitch, which reads as a cackle. A real laugh
	 * would need a resource pack, and would be swapped in here.
	 */
	private void playCackle(final Location at) {
		final float[] pitches = {1.3f, 1.5f, 1.8f};
		at.getWorld().playSound(at, Sound.VILLAGER_HAGGLE, 1.5f, pitches[0]);

		for (int i = 1; i < pitches.length; i++) {
			final float pitch = pitches[i];
			Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), new Runnable() {
				@Override
				public void run() {
					at.getWorld().playSound(at, Sound.VILLAGER_HAGGLE, 1.5f, pitch);
				}
			}, i * 4L);
		}
	}

	/**
	 * Backwards (the opposite of the way they face) with an upward speed, the same shape as the Iron
	 * Golem's Rose of Elevation. If they look straight up or down it's away from the flag instead.
	 */
	private Vector pushBack(Player grabber, Location flag) {
		Vector back = grabber.getLocation().getDirection().setY(0).multiply(-1);

		if (back.lengthSquared() < 0.0001) {
			Location at = grabber.getLocation();
			back = new Vector(at.getX() - (flag.getBlockX() + 0.5), 0, at.getZ() - (flag.getBlockZ() + 0.5));
			if (back.lengthSquared() < 0.0001) back = new Vector(1, 0, 0);
		}
		return back.normalize().multiply(PUSH_HORIZONTAL).setY(PUSH_VERTICAL);
	}

	/** The real flag was stolen from its hidden spot, so the decoy at home is taken down. */
	public void cancel(FWTeam owner) {
		Decoy decoy = decoys.remove(owner.getSiteId());
		if (decoy != null) removeBanner(decoy.home);
	}

	/** Takes down every decoy banner. The real flags are cleared by FWFlagManager#despawnAll. Used when the match ends. */
	public void clearAll() {
		for (Decoy decoy : decoys.values()) removeBanner(decoy.home);
		decoys.clear();
	}

	private void removeBanner(Location loc) {
		Block block = loc.getBlock();
		if (block.getType() == Material.STANDING_BANNER) block.setType(Material.AIR);
	}

	private boolean sameBlock(Location a, Location b) {
		return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
	}

	/** A random valid spot 3 to 4 blocks from the flag's normal spot and off to a corner of it, or null if there isn't one. */
	private Block findHidingSpot(FWTeamSite site, Block home) {
		List<Block> spots = new ArrayList<>();
		int reach = (int) HIDE_MAX_DISTANCE;
		for (int dx = -reach; dx <= reach; dx++) {
			for (int dz = -reach; dz <= reach; dz++) {
				if (dx == 0 || dz == 0) continue; // never in a straight line with the normal spot

				double distanceSquared = dx * dx + dz * dz;
				if (distanceSquared < HIDE_MIN_DISTANCE * HIDE_MIN_DISTANCE) continue;
				if (distanceSquared >= HIDE_MAX_DISTANCE * HIDE_MAX_DISTANCE) continue;

				for (int dy = -HIDE_HEIGHT; dy <= HIDE_HEIGHT; dy++) {
					Block candidate = home.getRelative(dx, dy, dz);
					if (canHideFlag(site, candidate)) spots.add(candidate);
				}
			}
		}
		return spots.isEmpty() ? null : spots.get(ThreadLocalRandom.current().nextInt(spots.size()));
	}

	/**
	 * Room for a banner on ground that can't be broken, outside every important area and generator
	 * column, and on the team's own island.
	 */
	private boolean canHideFlag(FWTeamSite site, Block block) {
		if (block.getType() != Material.AIR) return false;
		if (block.getRelative(BlockFace.UP).getType() != Material.AIR) return false;

		Block below = block.getRelative(BlockFace.DOWN);
		Material ground = below.getType();
		if (!ground.isSolid() || ground == Material.CHEST || ground == Material.ENDER_CHEST) return false;
		// A player could break a block they placed, and the banner on top would pop off
		if (instance.isPlacedByPlayer(below)) return false;

		if (instance.getMapConfig().isProtectedFromBuilding(block.getLocation())) return false;
		return !site.hasBoundary() || site.isWithinFootprint(block.getLocation());
	}

	private void tellTeam(FWTeam team, String message) {
		for (UUID uuid : team.getMembers()) {
			Player member = Bukkit.getPlayer(uuid);
			if (member != null) member.sendMessage(instance.getManager().getMain().color(message));
		}
	}
}
