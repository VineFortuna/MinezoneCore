package anthony.flagwars;

import anthony.flagwars.map.FWTeamSite;
import anthony.flagwars.shop.FWTrap;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks which players are inside each team's boundary and fires the front
 * trap in that team's queue when an enemy steps in for the first time.
 * Mirrors Hypixel Bedwars traps: one trap consumed per entry event.
 */
public class FWTrapManager {

	public static final int MAX_QUEUE_SIZE = 3;

	private final FWGameInstance instance;
	/** siteId → set of player UUIDs currently inside that island's boundary. */
	private final Map<Integer, Set<UUID>> insideBoundary = new HashMap<>();

	public FWTrapManager(FWGameInstance instance) {
		this.instance = instance;
	}

	/**
	 * Call from PlayerMoveEvent (block-to-block only) for SURVIVAL players in active matches.
	 * Detects entry into any enemy boundary and triggers the next queued trap.
	 */
	public void checkEntry(Player player) {
		FWTeam playerTeam = instance.findTeamOf(player);
		if (playerTeam == null) return;

		for (FWTeam team : instance.getTeams().values()) {
			if (team == playerTeam) continue;

			int siteId = team.getSiteId();
			FWTeamSite site = instance.getMapConfig().getTeamSite(siteId);
			if (!site.hasBoundary()) continue;

			boolean inside = site.isWithinBoundary(player.getLocation());
			Set<UUID> current = insideBoundary.computeIfAbsent(siteId, k -> new HashSet<>());
			boolean wasInside = current.contains(player.getUniqueId());

			if (inside && !wasInside) {
				current.add(player.getUniqueId());
				triggerNextTrap(team, player);
			} else if (!inside) {
				current.remove(player.getUniqueId());
			}
		}
	}

	private void triggerNextTrap(FWTeam defender, Player intruder) {
		FWTrap trap = defender.pollTrap();
		if (trap == null) return;

		for (UUID uuid : defender.getMembers()) {
			Player member = Bukkit.getPlayer(uuid);
			if (member == null) continue;
			member.sendMessage(instance.getManager().getMain().color(
					"&c&l(!) &e" + intruder.getName() + " &rtriggered your &e" + trap.getDisplayName() + "&r!"));
			member.playSound(member.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1.8f);
		}

		applyTrap(trap, intruder);
		intruder.sendMessage(instance.getManager().getMain().color(
				"&c&l(!) &rYou triggered a &e" + trap.getDisplayName() + "&r!"));
		intruder.playSound(intruder.getLocation(), Sound.ENDERDRAGON_GROWL, 0.5f, 1.5f);
	}

	private void applyTrap(FWTrap trap, Player target) {
		switch (trap) {
			case MINING_FATIGUE:
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10 * 20, 2), true);
				break;
			case MLG:
				target.setVelocity(new Vector(0, 2.5, 0));
				break;
			case SLOWNESS_BLINDNESS:
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,      6 * 20, 0), true);
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 6 * 20, 0), true);
				break;
		}
	}

	public void reset() {
		insideBoundary.clear();
	}
}
