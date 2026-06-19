package anthony.villagerdefense;

import anthony.SuperCraftBrawl.gui.ConfirmationGUI;
import anthony.villagerdefense.resources.VDResourceType;
import anthony.villagerdefense.shop.VDShopGUI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

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
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Player) {
			Player player = (Player) entity;
			VDGameInstance instance = vdGameManager.getInstanceOfPlayer(player);
			if (instance == null || instance.getState() != VDGameState.IN_PROGRESS) return;

			if (!event.isCancelled() && event.getFinalDamage() >= player.getHealth() - 0.2) {
				event.setCancelled(true);
				instance.handlePlayerDowned(player);
			}
			return;
		}

		VDGameInstance instance = vdGameManager.getActiveInstance();
		if (instance == null || instance.getState() != VDGameState.IN_PROGRESS) return;

		VDTeam team = instance.getVillagerManager().findTeamByObjective(entity);
		if (team == null) return;

		LivingEntity villager = (LivingEntity) entity;
		if (event.isCancelled() || event.getFinalDamage() < villager.getHealth() - 0.2) return;

		event.setCancelled(true);
		instance.getVillagerManager().destroyObjective(team, resolveKiller(event.getDamager()));
	}

	private Player resolveKiller(Entity damager) {
		if (damager instanceof Player) return (Player) damager;
		if (damager instanceof Projectile) {
			Object shooter = ((Projectile) damager).getShooter();
			if (shooter instanceof Player) return (Player) shooter;
		}
		return null;
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != VDGameState.IN_PROGRESS) return;

		VDTeam team = instance.findTeamOf(player);
		if (team == null) return;

		ItemStack stack = event.getItem().getItemStack();
		VDResourceType type = VDResourceType.fromMaterial(stack.getType());
		if (type == null) return;

		event.setCancelled(true);
		team.addResource(type, stack.getAmount());
		event.getItem().remove();
	}

	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;

		Entity clicked = event.getRightClicked();

		VDTeam objectiveTeam = instance.getVillagerManager().findTeamByObjective(clicked);
		if (objectiveTeam != null) {
			event.setCancelled(true);
			VDTeam playerTeam = instance.findTeamOf(player);

			if (playerTeam == objectiveTeam) {
				new ConfirmationGUI(
						vdGameManager.getMain(),
						"Upgrade Villager?",
						p -> instance.getVillagerManager().upgrade(objectiveTeam, p),
						p -> {
						}
				).inv.open(player);
			} else {
				player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rThis isn't your villager!"));
			}
			return;
		}

		Integer shopSiteId = instance.getVillagerManager().findSiteByShopkeeper(clicked);
		if (shopSiteId != null) {
			event.setCancelled(true);
			VDTeam playerTeam = instance.findTeamOf(player);
			if (playerTeam != null) {
				new VDShopGUI(vdGameManager.getMain(), playerTeam).open(player);
			}
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
