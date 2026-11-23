package anthony.flagwars;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Blocks a trap places for a few seconds, like the Cage Trap's iron bars and wool floor.
 * They count as player placed so they can be broken, and every change goes into the undo log so
 * the arena still restores if the match ends before they clear.
 */
public class FWTemporaryBlocks {

	private static final class Batch {
		final List<Block> blocks;
		final Material material;
		BukkitTask task;

		Batch(List<Block> blocks, Material material) {
			this.blocks = blocks;
			this.material = material;
		}
	}

	private final FWGameInstance instance;
	/** Batches that haven't cleared yet. Each one removes itself when it ends. */
	private final Set<Batch> active = new HashSet<>();
	/** Every block of the active batches, so a break can tell a trap block from one a player placed. */
	private final Set<Block> tracked = new HashSet<>();

	public FWTemporaryBlocks(FWGameInstance instance) {
		this.instance = instance;
	}

	/**
	 * Puts the block on every air block in the list and takes them back after the delay. Anything
	 * that isn't air is left alone. Returns how many were placed.
	 */
	@SuppressWarnings("deprecation")
	public int place(Collection<Block> candidates, Material material, byte data, long ticks) {
		List<Block> placed = new ArrayList<>();
		for (Block block : candidates) {
			if (block.getType() != Material.AIR) continue;

			instance.recordBlockChange(block.getState());
			block.setTypeIdAndData(material.getId(), data, true);
			instance.markPlaced(block);
			tracked.add(block);
			placed.add(block);
		}
		if (placed.isEmpty()) return 0;

		final Batch batch = new Batch(placed, material);
		active.add(batch);
		batch.task = Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), new Runnable() {
			@Override
			public void run() {
				active.remove(batch);
				remove(batch);
			}
		}, ticks);
		return placed.size();
	}

	/** Clears the blocks that are still there. Ones a player already broke, or that something replaced, are skipped. */
	private void remove(Batch batch) {
		for (Block block : batch.blocks) {
			tracked.remove(block);
			if (block.getType() != batch.material) continue;
			block.setType(Material.AIR);
			instance.unmarkPlaced(block);
		}
	}

	/**
	 * Called when a player breaks a block. Returns true if it was one of these trap blocks, and forgets it,
	 * so the caller can remove it without dropping an item.
	 */
	public boolean take(Block block) {
		return tracked.remove(block);
	}

	/** Takes everything back right now and cancels the timers. Used when the match ends. */
	public void clearAll() {
		for (Batch batch : active) {
			if (batch.task != null) batch.task.cancel();
			remove(batch);
		}
		active.clear();
		tracked.clear();
	}
}
