package anthony.flagdefense.defense;

import anthony.SuperCraftBrawl.Timer;
import anthony.util.ItemHelper;
import anthony.flagdefense.FDGameInstance;
import anthony.flagdefense.FDTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Level 3 giveable ability item: places a fake banner near the player for a
 * limited time. Purely visual - non-flag banners aren't tracked by
 * FDFlagManager, so breaking one just hits the generic "you can only break
 * blocks placed by players" rule and does nothing - same "no game-given
 * tell" design as the original mechanic this replaces (attackers have to
 * actually pay attention rather than just breaking whichever banner they see).
 */
public class DecoyFlagMechanic {

	public static final String NAME = "Decoy Flag";
	private static final long COOLDOWN_MS = 60_000L;
	private static final long DURATION_TICKS = 30L * 20L;

	private final FDGameInstance instance;
	private final Map<Integer, Timer> cooldowns = new HashMap<>();
	private final Set<Block> decoys = new HashSet<>();

	public DecoyFlagMechanic(FDGameInstance instance) {
		this.instance = instance;
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.BANNER), "&d" + NAME,
				"&7Right-click to place a fake",
				"&7flag near your base.");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.BANNER) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains(NAME);
	}

	public void activate(org.bukkit.entity.Player player, FDTeam team) {
		Timer cooldown = cooldowns.computeIfAbsent(team.getSiteId(), id -> new Timer());

		if (cooldown.getTime() < COOLDOWN_MS) {
			long secondsLeft = (COOLDOWN_MS - cooldown.getTime()) / 1000 + 1;
			player.sendMessage(instance.getManager().getMain().color("&c&l(!) &rDecoy Flag is on cooldown for &e"
					+ secondsLeft + "s&r."));
			return;
		}

		cooldown.restart();

		Block block = player.getLocation().getBlock();
		instance.recordBlockChange(block.getState());
		block.setType(Material.STANDING_BANNER);

		BlockState state = block.getState();
		if (state instanceof Banner) {
			Banner banner = (Banner) state;
			banner.setBaseColor(team.getColor());
			banner.update(true);
		}
		decoys.add(block);

		Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
			if (block.getType() == Material.STANDING_BANNER) block.setType(Material.AIR);
			decoys.remove(block);
		}, DURATION_TICKS);

		instance.TellAll(instance.getManager().getMain().color("&d&l(!) &eTeam " + team.getName()
				+ "&r placed a Decoy Flag!"));
	}

	public void stopAll() {
		for (Block block : decoys) {
			if (block.getType() == Material.STANDING_BANNER) block.setType(Material.AIR);
		}
		decoys.clear();
	}
}
