package anthony.flagdefense;

import anthony.flagdefense.flag.TeamLevel;
import anthony.flagdefense.shop.FDTeamUpgrade;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Runtime state for one base in a match. Modeled as a roster with a
 * capacity (rather than a single owner) so duos/squads can reuse this
 * unchanged later - this map just configures capacity 1 per team (solos).
 */
public class FDTeam {

	private final int siteId;
	private final String name;
	private final DyeColor color;
	private final int capacity;
	private final List<UUID> members = new ArrayList<>();

	private TeamLevel level = TeamLevel.LEVEL_1;
	private final Map<FDTeamUpgrade, Integer> upgradeTiers = new EnumMap<>(FDTeamUpgrade.class);

	/** True once every member has left mid-match - the base sits idle (no generator/defense upkeep) but its flag stays capturable. */
	private boolean inactive = false;

	private int capturedFlags = 0;
	private final Set<Integer> capturedFromSites = new HashSet<>();

	public FDTeam(int siteId, String name, DyeColor color, int capacity) {
		this.siteId = siteId;
		this.name = name;
		this.color = color;
		this.capacity = capacity;
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

	public TeamLevel getLevel() {
		return level;
	}

	public void setLevel(TeamLevel level) {
		this.level = level;
	}

	public int getUpgradeTier(FDTeamUpgrade upgrade) {
		return upgradeTiers.getOrDefault(upgrade, upgrade.getBaseTier());
	}

	public void setUpgradeTier(FDTeamUpgrade upgrade, int tier) {
		upgradeTiers.put(upgrade, tier);
	}

	public void resetUpgrades() {
		upgradeTiers.clear();
	}

	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	public int getCapturedFlags() {
		return capturedFlags;
	}

	public void addCapturedFlag() {
		capturedFlags++;
	}

	/** Once a team has captured a given opponent's flag, they can never capture that same opponent's flag again. */
	public boolean hasCapturedFrom(int siteId) {
		return capturedFromSites.contains(siteId);
	}

	public void markCapturedFrom(int siteId) {
		capturedFromSites.add(siteId);
	}

	public void resetFlagProgress() {
		capturedFlags = 0;
		capturedFromSites.clear();
	}
}
