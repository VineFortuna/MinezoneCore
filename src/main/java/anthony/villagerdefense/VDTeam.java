package anthony.villagerdefense;

import anthony.villagerdefense.resources.VDResourceType;
import anthony.villagerdefense.villager.VillagerLevel;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runtime state for one base in a match. Modeled as a roster with a
 * capacity (rather than a single owner) so duos/squads can reuse this
 * unchanged later - this map just configures capacity 1 per team (solos).
 */
public class VDTeam {

	private final int siteId;
	private final String name;
	private final DyeColor color;
	private final int capacity;
	private final List<UUID> members = new ArrayList<>();

	private VillagerLevel level = VillagerLevel.LEVEL_1;
	private final Map<VDResourceType, Integer> resources = new EnumMap<>(VDResourceType.class);

	private boolean villagerAlive = true;
	private Villager villagerEntity;
	private boolean eliminated = false;

	public VDTeam(int siteId, String name, DyeColor color, int capacity) {
		this.siteId = siteId;
		this.name = name;
		this.color = color;
		this.capacity = capacity;
		for (VDResourceType type : VDResourceType.values()) {
			resources.put(type, 0);
		}
	}

	public int getSiteId() {
		return siteId;
	}

	public String getName() {
		return name;
	}

	public DyeColor getColor() {
		return color;
	}

	public boolean hasSpace() {
		return members.size() < capacity;
	}

	public boolean addMember(UUID id) {
		if (!hasSpace()) return false;
		return members.add(id);
	}

	public boolean removeMember(UUID id) {
		return members.remove(id);
	}

	public List<UUID> getMembers() {
		return members;
	}

	public boolean isMember(Player player) {
		return members.contains(player.getUniqueId());
	}

	public boolean isEmpty() {
		return members.isEmpty();
	}

	public VillagerLevel getLevel() {
		return level;
	}

	public void setLevel(VillagerLevel level) {
		this.level = level;
	}

	public int getResource(VDResourceType type) {
		return resources.getOrDefault(type, 0);
	}

	public void addResource(VDResourceType type, int amount) {
		resources.merge(type, amount, Integer::sum);
	}

	public boolean canAfford(Map<VDResourceType, Integer> cost) {
		for (Map.Entry<VDResourceType, Integer> entry : cost.entrySet()) {
			if (getResource(entry.getKey()) < entry.getValue()) return false;
		}
		return true;
	}

	public boolean spend(Map<VDResourceType, Integer> cost) {
		if (!canAfford(cost)) return false;
		for (Map.Entry<VDResourceType, Integer> entry : cost.entrySet()) {
			resources.merge(entry.getKey(), -entry.getValue(), Integer::sum);
		}
		return true;
	}

	public boolean isVillagerAlive() {
		return villagerAlive;
	}

	public void setVillagerAlive(boolean villagerAlive) {
		this.villagerAlive = villagerAlive;
	}

	public Villager getVillagerEntity() {
		return villagerEntity;
	}

	public void setVillagerEntity(Villager villagerEntity) {
		this.villagerEntity = villagerEntity;
	}

	public boolean isEliminated() {
		return eliminated;
	}

	public void setEliminated(boolean eliminated) {
		this.eliminated = eliminated;
	}
}
