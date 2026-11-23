package anthony.flagwars;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.WinEffects;
import anthony.flagwars.map.FWTeamSite;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Satisfies WinEffects.Host so Flag Wars can play the exact same cosmetic
 * win effects SCB uses (WinEffects.java) without needing SCB's own
 * GameInstance. The flood effect's "map bounds" are sized around the
 * winner's base instead of a full SCB arena, since Flag Wars has no
 * equivalent map-bounds metadata.
 */
class FWWinEffectsHost implements WinEffects.Host {

	private static final double FLOOD_RADIUS = 12.0;

	private final FWGameInstance instance;
	private final Player winner;

	FWWinEffectsHost(FWGameInstance instance, Player winner) {
		this.instance = instance;
		this.winner = winner;
	}

	@Override
	public World getMapWorld() {
		return instance.getWorld();
	}

	@Override
	public Core getPlugin() {
		return instance.getManager().getMain();
	}

	@Override
	public Location GetRespawnLoc() {
		Location flagLoc = baseLocation();
		return flagLoc != null ? flagLoc : winner.getLocation();
	}

	@Override
	public Vector getFloodCenter() {
		Location loc = baseLocation();
		return (loc != null ? loc : winner.getLocation()).toVector();
	}

	@Override
	public double getFloodBoundsX() {
		return FLOOD_RADIUS;
	}

	@Override
	public double getFloodBoundsZ() {
		return FLOOD_RADIUS;
	}

	private Location baseLocation() {
		FWTeam team = instance.findTeamOf(winner);
		if (team == null) return null;
		FWTeamSite site = instance.getMapConfig().getTeamSite(team.getSiteId());
		return site != null ? site.getFlagLoc() : null;
	}
}
