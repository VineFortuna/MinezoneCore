package anthony.villagerdefense.defense;

import anthony.SuperCraftBrawl.Timer;
import anthony.util.ItemHelper;
import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Level 3 giveable ability item: spawns a fake Villager near the real one
 * for a limited time. Purely visual - there's no game-given tell, attackers
 * have to actually pay attention rather than just swinging at whichever
 * villager they see first.
 */
public class DecoyVillagerMechanic {

	public static final String NAME = "Decoy Villager";
	private static final long COOLDOWN_MS = 60_000L;
	private static final long DURATION_TICKS = 30L * 20L;

	private final VDGameInstance instance;
	private final Map<Integer, Timer> cooldowns = new HashMap<>();
	private final Set<Villager> decoys = new HashSet<>();

	public DecoyVillagerMechanic(VDGameInstance instance) {
		this.instance = instance;
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(ItemHelper.createMonsterEgg(EntityType.VILLAGER, 1), "&d" + NAME,
				"&7Right-click to place a fake",
				"&7villager near your base.");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.MONSTER_EGG) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains(NAME);
	}

	public void activate(Player player, VDTeam team) {
		Timer cooldown = cooldowns.computeIfAbsent(team.getSiteId(), id -> new Timer());

		if (cooldown.getTime() < COOLDOWN_MS) {
			long secondsLeft = (COOLDOWN_MS - cooldown.getTime()) / 1000 + 1;
			player.sendMessage(instance.getManager().getMain().color("&c&l(!) &rDecoy Villager is on cooldown for &e"
					+ secondsLeft + "s&r."));
			return;
		}

		cooldown.restart();

		Villager decoy = (Villager) instance.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
		decoy.setRemoveWhenFarAway(false);
		decoys.add(decoy);

		Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
			if (!decoy.isDead()) decoy.remove();
			decoys.remove(decoy);
		}, DURATION_TICKS);

		instance.TellAll(instance.getManager().getMain().color("&d&l(!) &eTeam " + team.getName()
				+ "&r placed a Decoy Villager!"));
	}

	public boolean isDecoy(Entity entity) {
		return decoys.contains(entity);
	}

	public void stopAll() {
		for (Villager decoy : decoys) {
			decoy.remove();
		}
		decoys.clear();
	}
}
