package anthony.villagerdefense.defense;

import anthony.SuperCraftBrawl.Timer;
import anthony.util.ItemHelper;
import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Level 3 giveable ability item: warns the defending team about nearby
 * enemies. 1.8 has no Glowing potion effect, so "reveal" is a chat
 * call-out plus a sound cue rather than a visual highlight.
 */
public class VillageBellMechanic {

	public static final String NAME = "Village Bell";
	private static final long COOLDOWN_MS = 60_000L;
	private static final double RADIUS = 20.0;

	private final VDGameInstance instance;
	private final Map<Integer, Timer> cooldowns = new HashMap<>();

	public VillageBellMechanic(VDGameInstance instance) {
		this.instance = instance;
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.NOTE_BLOCK), "&6" + NAME,
				"&7Right-click to warn your team",
				"&7about nearby enemies.");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.NOTE_BLOCK) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains(NAME);
	}

	public void activate(Player player, VDTeam team) {
		Timer cooldown = cooldowns.computeIfAbsent(team.getSiteId(), id -> new Timer());

		if (cooldown.getTime() < COOLDOWN_MS) {
			long secondsLeft = (COOLDOWN_MS - cooldown.getTime()) / 1000 + 1;
			player.sendMessage(instance.getManager().getMain().color("&c&l(!) &rVillage Bell is on cooldown for &e"
					+ secondsLeft + "s&r."));
			return;
		}

		cooldown.restart();

		Location center = team.getVillagerEntity() != null
				? team.getVillagerEntity().getLocation() : player.getLocation();

		List<String> nearby = new ArrayList<>();
		for (Player enemy : instance.getWorld().getPlayers()) {
			if (team.isMember(enemy)) continue;
			double distance = enemy.getLocation().distance(center);
			if (distance > RADIUS) continue;

			nearby.add("&e" + enemy.getName() + " &r(" + (int) distance + "m)");
			enemy.playSound(enemy.getLocation(), Sound.NOTE_PLING, 1f, 1f);
		}

		String prefix = instance.getManager().getMain().color("&6&l(!) &eVillage Bell: &r");
		String result = nearby.isEmpty() ? "No enemies nearby." : "Enemies nearby - " + String.join("&r, ", nearby);

		for (UUID uuid : team.getMembers()) {
			Player member = Bukkit.getPlayer(uuid);
			if (member != null) member.sendMessage(prefix + instance.getManager().getMain().color(result));
		}
	}
}
