package anthony.villagerdefense;

import anthony.SuperCraftBrawl.gui.ConfirmationGUI;
import anthony.villagerdefense.items.GrapplingHookItem;
import anthony.villagerdefense.items.TeleportBowItem;
import anthony.villagerdefense.items.ThrowableTntItem;
import anthony.villagerdefense.resources.VDResourceType;
import anthony.villagerdefense.shop.VDShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.UUID;

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

			applyTeleportBow(event.getDamager(), player);

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

	private static final String TELEPORT_BOW_METADATA = "vd_teleport_bow";

	/** Shooting a player with the Teleport Bow teleports the shooter to them. */
	private void applyTeleportBow(Entity damager, Player victim) {
		if (!(damager instanceof Arrow) || !damager.hasMetadata(TELEPORT_BOW_METADATA)) return;

		Object value = damager.getMetadata(TELEPORT_BOW_METADATA).get(0).value();
		if (!(value instanceof UUID)) return;

		Player shooter = Bukkit.getPlayer((UUID) value);
		if (shooter != null) {
			shooter.teleport(victim.getLocation());
		}
	}

	@EventHandler
	public void onShootBow(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player)) return;

		Player shooter = (Player) event.getEntity();
		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(shooter);
		if (instance == null || instance.getState() != VDGameState.IN_PROGRESS) return;

		if (TeleportBowItem.matches(event.getBow())) {
			event.getProjectile().setMetadata(TELEPORT_BOW_METADATA,
					new FixedMetadataValue(vdGameManager.getMain(), shooter.getUniqueId()));
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != VDGameState.IN_PROGRESS) return;

		ItemStack item = event.getItem();
		VDTeam team = instance.findTeamOf(player);

		if (GrapplingHookItem.matches(item)) {
			useGrapplingHook(player);
		} else if (ThrowableTntItem.matches(item)) {
			throwTnt(player);
		} else if (team != null && anthony.villagerdefense.defense.VillageBellMechanic.matches(item)) {
			instance.getDefenseManager().getVillageBell().activate(player, team);
		} else if (team != null && anthony.villagerdefense.defense.BarricadeMechanic.matches(item)) {
			instance.getDefenseManager().getBarricade().activate(player, team);
		} else if (team != null && anthony.villagerdefense.defense.DecoyVillagerMechanic.matches(item)) {
			instance.getDefenseManager().getDecoyVillager().activate(player, team);
		}
	}

	private void useGrapplingHook(Player player) {
		Block target = player.getTargetBlock(Collections.singleton(Material.AIR), 60);
		if (target == null) return;

		Vector toTarget = target.getLocation().add(0.5, 0.5, 0.5).subtract(player.getLocation()).toVector();
		double distance = toTarget.length();
		if (distance < 1) return;

		player.setVelocity(toTarget.normalize().multiply(Math.min(distance, 8) * 0.6));
	}

	private void throwTnt(Player player) {
		TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getEyeLocation(), org.bukkit.entity.EntityType.PRIMED_TNT);
		tnt.setVelocity(player.getLocation().getDirection().multiply(1.4));
		tnt.setFuseTicks(60);

		ItemStack handItem = player.getItemInHand();
		if (handItem.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			handItem.setAmount(handItem.getAmount() - 1);
		}
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
	public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
		if (sameBlock(event.getFrom(), event.getTo())) return;

		Player player = event.getPlayer();
		VDGameInstance instance = vdGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != VDGameState.IN_PROGRESS) return;

		instance.getDefenseManager().getSnareTrap().checkMovement(instance, player);
	}

	private boolean sameBlock(org.bukkit.Location a, org.bukkit.Location b) {
		return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
	}

	@EventHandler
	public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
		VDGameInstance instance = vdGameManager.getActiveInstance();
		if (instance == null) return;

		Entity entity = event.getEntity();

		Integer guardSite = instance.getDefenseManager().getZombieGuard().teamSiteOf(entity);
		if (guardSite != null) {
			instance.getDefenseManager().getZombieGuard().onGuardKilled(guardSite);
			return;
		}

		Integer golemSite = instance.getDefenseManager().getRepairGolem().teamSiteOf(entity);
		if (golemSite != null) {
			instance.getDefenseManager().getRepairGolem().onGolemKilled(golemSite);
		}
	}

	@EventHandler
	public void onEntityTarget(org.bukkit.event.entity.EntityTargetLivingEntityEvent event) {
		VDGameInstance instance = vdGameManager.getActiveInstance();
		if (instance == null || !(event.getTarget() instanceof Player)) return;

		Integer guardSite = instance.getDefenseManager().getZombieGuard().teamSiteOf(event.getEntity());
		if (guardSite == null) return;

		VDTeam guardTeam = instance.getTeams().get(guardSite);
		if (guardTeam != null && guardTeam.isMember((Player) event.getTarget())) {
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
