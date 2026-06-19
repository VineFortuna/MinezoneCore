package anthony.villagerdefense;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

/**
 * All VillagerDefense-only event handling. Every handler bails immediately
 * if the player isn't in the active VD instance, exactly mirroring how
 * SuperCraftBros' PlayerListener gates on getGameManager().GetInstanceOfPlayer().
 * Registered as its own Listener so it never interferes with SCB's.
 */
public class VDPlayerListener implements Listener {

	private final VDGameManager vdGameManager;

	public VDPlayerListener(VDGameManager vdGameManager) {
		this.vdGameManager = vdGameManager;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(event.getPlayer());
		if (instance == null) return;

		instance.recordBlockChange(event.getBlockReplacedState());
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(event.getPlayer());
		if (instance == null) return;

		instance.recordBlockChange(event.getBlock().getState());
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;

		Player player = (Player) event.getEntity();
		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;

		if (instance.getState() != VDGameState.IN_PROGRESS) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(player);
		if (instance != null) {
			instance.removePlayer(player);
			return;
		}

		VDGameInstance spectating = vdGameManager.getInstanceOfSpectator(player);
		if (spectating != null) {
			spectating.removeSpectator(player);
		}
	}
}
