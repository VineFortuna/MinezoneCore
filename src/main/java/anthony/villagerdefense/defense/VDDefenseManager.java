package anthony.villagerdefense.defense;

import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.villager.VillagerLevel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Aggregates all base-defense mechanics for one match and dispatches
 * level-up triggers / ability-item activations to the right one.
 */
public class VDDefenseManager {

	private final ZombieGuardMechanic zombieGuard;
	private final RepairGolemMechanic repairGolem;
	private final VillageBellMechanic villageBell;
	private final SnareTrapMechanic snareTrap;
	private final BarricadeMechanic barricade;
	private final DecoyVillagerMechanic decoyVillager;

	public VDDefenseManager(VDGameInstance instance) {
		this.zombieGuard = new ZombieGuardMechanic(instance);
		this.repairGolem = new RepairGolemMechanic(instance);
		this.villageBell = new VillageBellMechanic(instance);
		this.snareTrap = new SnareTrapMechanic();
		this.barricade = new BarricadeMechanic(instance);
		this.decoyVillager = new DecoyVillagerMechanic(instance);
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

	public DecoyVillagerMechanic getDecoyVillager() {
		return decoyVillager;
	}

	/** Called by VDVillagerManager right after a team's level changes. */
	public void onTeamUpgraded(VDTeam team) {
		VillagerLevel level = team.getLevel();

		if (level == VillagerLevel.LEVEL_2) {
			zombieGuard.activate(team);
		}

		if (level == VillagerLevel.LEVEL_3) {
			repairGolem.activate(team);
			snareTrap.onTeamUpgraded(team);
			giveLevel3AbilityItems(team);
		}
	}

	private void giveLevel3AbilityItems(VDTeam team) {
		for (UUID uuid : team.getMembers()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) continue;

			player.getInventory().addItem(VillageBellMechanic.create());
			player.getInventory().addItem(BarricadeMechanic.create());
			player.getInventory().addItem(DecoyVillagerMechanic.create());
		}
	}

	public void stopAll() {
		zombieGuard.stopAll();
		repairGolem.stopAll();
		decoyVillager.stopAll();
		snareTrap.reset();
	}
}
