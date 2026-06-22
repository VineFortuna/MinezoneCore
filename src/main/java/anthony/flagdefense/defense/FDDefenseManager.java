package anthony.flagdefense.defense;

import anthony.flagdefense.FDGameInstance;
import anthony.flagdefense.FDTeam;
import anthony.flagdefense.flag.TeamLevel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Aggregates all base-defense mechanics for one match and dispatches
 * level-up triggers / ability-item activations to the right one.
 */
public class FDDefenseManager {

	private final ZombieGuardMechanic zombieGuard;
	private final RepairGolemMechanic repairGolem;
	private final VillageBellMechanic villageBell;
	private final SnareTrapMechanic snareTrap;
	private final BarricadeMechanic barricade;
	private final DecoyFlagMechanic decoyFlag;

	public FDDefenseManager(FDGameInstance instance) {
		this.zombieGuard = new ZombieGuardMechanic(instance);
		this.repairGolem = new RepairGolemMechanic(instance);
		this.villageBell = new VillageBellMechanic(instance);
		this.snareTrap = new SnareTrapMechanic();
		this.barricade = new BarricadeMechanic(instance);
		this.decoyFlag = new DecoyFlagMechanic(instance);
	}

	public ZombieGuardMechanic getZombieGuard() {
		return zombieGuard;
	}

	public RepairGolemMechanic getRepairGolem() {
		return repairGolem;
	}

	public VillageBellMechanic getVillageBell() {
		return villageBell;
	}

	public SnareTrapMechanic getSnareTrap() {
		return snareTrap;
	}

	public BarricadeMechanic getBarricade() {
		return barricade;
	}

	public DecoyFlagMechanic getDecoyFlag() {
		return decoyFlag;
	}

	/** Called by FDTeamUpgradeManager right after a team's level changes. */
	public void onTeamUpgraded(FDTeam team) {
		TeamLevel level = team.getLevel();

		if (level == TeamLevel.LEVEL_2) {
			zombieGuard.activate(team);
		}

		if (level == TeamLevel.LEVEL_3) {
			repairGolem.activate(team);
			snareTrap.onTeamUpgraded(team);
			giveLevel3AbilityItems(team);
		}
	}

	private void giveLevel3AbilityItems(FDTeam team) {
		for (UUID uuid : team.getMembers()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) continue;

			player.getInventory().addItem(VillageBellMechanic.create());
			player.getInventory().addItem(BarricadeMechanic.create());
			player.getInventory().addItem(DecoyFlagMechanic.create());
		}
	}

	public void stopAll() {
		zombieGuard.stopAll();
		repairGolem.stopAll();
		decoyFlag.stopAll();
		snareTrap.reset();
	}
}
