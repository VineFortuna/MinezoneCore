package anthony.villagerdefense.defense;

import anthony.SuperCraftBrawl.Timer;
import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Level 3 passive trap: the first intruder to enter a team's base radius
 * gets a brief Slowness - no long freeze, no nausea, per the design doc -
 * then has to wait for the trap to re-arm before it can trigger again.
 */
public class SnareTrapMechanic {

	private static final double TRIGGER_RADIUS = 6.0;
	private static final long REARM_COOLDOWN_MS = 30_000L;
	private static final int SLOW_DURATION_TICKS = 3 * 20;
	private static final int SLOW_AMPLIFIER = 1;

	private final Map<Integer, Boolean> armed = new HashMap<>();
	private final Map<Integer, Timer> rearmTimers = new HashMap<>();

	public void onTeamUpgraded(VDTeam team) {
		if (team.getLevel().hasSnareTrap()) {
			armed.putIfAbsent(team.getSiteId(), true);
		}
	}

	public void checkMovement(VDGameInstance instance, Player mover) {
		for (VDTeam team : instance.getTeams().values()) {
			if (!team.getLevel().hasSnareTrap()) continue;
			if (team.isMember(mover)) continue;
			if (!isArmed(team)) continue;

			Villager villager = team.getVillagerEntity();
			if (villager == null) continue;
			if (mover.getLocation().distanceSquared(villager.getLocation()) > TRIGGER_RADIUS * TRIGGER_RADIUS) continue;

			trigger(instance, team, mover);
			return; // only one trap fires per move event
		}
	}

	private boolean isArmed(VDTeam team) {
		Timer timer = rearmTimers.get(team.getSiteId());
		if (timer == null) return armed.getOrDefault(team.getSiteId(), false);
		return timer.getTime() >= REARM_COOLDOWN_MS;
	}

	private void trigger(VDGameInstance instance, VDTeam team, Player intruder) {
		intruder.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, SLOW_DURATION_TICKS, SLOW_AMPLIFIER, false, false));
		rearmTimers.put(team.getSiteId(), new Timer());

		instance.TellAll(instance.getManager().getMain().color("&6&l(!) &eTeam " + team.getName()
				+ "'s &rSnare Trap caught &e" + intruder.getName() + "&r!"));
	}

	public void reset() {
		armed.clear();
		rearmTimers.clear();
	}
}
