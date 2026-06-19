package anthony.villagerdefense.defense;

import anthony.SuperCraftBrawl.Timer;
import anthony.util.ItemHelper;
import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Level 3 giveable ability item: raises a short wall in front of the
 * player for a few seconds, then automatically removes it.
 */
public class BarricadeMechanic {

	public static final String NAME = "Barricade";
	private static final long COOLDOWN_MS = 45_000L;
	private static final long DURATION_TICKS = 6L * 20L;

	private final VDGameInstance instance;
	private final Map<Integer, Timer> cooldowns = new HashMap<>();

	public BarricadeMechanic(VDGameInstance instance) {
		this.instance = instance;
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.COBBLESTONE, 1), "&7" + NAME,
				"&7Right-click to raise a short wall",
				"&7in front of you for a few seconds.");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.COBBLESTONE) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains(NAME);
	}

	public void activate(Player player, VDTeam team) {
		Timer cooldown = cooldowns.computeIfAbsent(team.getSiteId(), id -> new Timer());

		if (cooldown.getTime() < COOLDOWN_MS) {
			long secondsLeft = (COOLDOWN_MS - cooldown.getTime()) / 1000 + 1;
			player.sendMessage(instance.getManager().getMain().color("&c&l(!) &rBarricade is on cooldown for &e"
					+ secondsLeft + "s&r."));
			return;
		}

		cooldown.restart();

		BlockFace facing = faceOf(player.getLocation().getYaw());
		Block base = player.getLocation().add(facing.getModX() * 2, 0, facing.getModZ() * 2).getBlock();
		BlockFace side = (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) ? BlockFace.EAST : BlockFace.NORTH;

		List<Block> raised = new ArrayList<>();
		for (int offset = -1; offset <= 1; offset++) {
			Block column = base.getRelative(side.getModX() * offset, 0, side.getModZ() * offset);
			for (int dy = 0; dy <= 1; dy++) {
				Block block = column.getRelative(0, dy, 0);
				if (block.getType() == Material.AIR) {
					instance.recordBlockChange(block.getState());
					block.setType(Material.COBBLESTONE);
					raised.add(block);
				}
			}
		}

		instance.TellAll(instance.getManager().getMain().color("&7&l(!) &e" + player.getName()
				+ "&r raised a Barricade!"));

		Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
			for (Block block : raised) {
				if (block.getType() == Material.COBBLESTONE) block.setType(Material.AIR);
			}
		}, DURATION_TICKS);
	}

	private BlockFace faceOf(float yaw) {
		float normalized = yaw % 360;
		if (normalized < 0) normalized += 360;

		if (normalized >= 315 || normalized < 45) return BlockFace.SOUTH;
		if (normalized < 135) return BlockFace.WEST;
		if (normalized < 225) return BlockFace.NORTH;
		return BlockFace.EAST;
	}
}
