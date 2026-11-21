package anthony.flagwars;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;

/**
 * Everything a Flag Wars spectator must not be able to touch.
 *
 * Spectators are GameMode.ADVENTURE and only hidden, not vanilla spectators (see
 * FWGameInstance#addSpectator), so the server still treats them as ordinary players: they can
 * punch fireballs back, hit mobs, right-click villagers, open inventories and so on. Blocks,
 * pickups, damage taken and block clicks are already blocked in FWPlayerListener; this covers the
 * entity side. Every handler checks for a spectator first and does nothing for anyone else, so a
 * normal player or another gamemode is never affected.
 */
public class FWSpectatorListener implements Listener {

	private final FWGameManager fwGameManager;

	public FWSpectatorListener(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
	}

	private boolean isSpectator(Entity entity) {
		return entity instanceof Player && fwGameManager.getInstanceOfSpectator((Player) entity) != null;
	}

	/**
	 * A spectator can't hurt anything: players, mobs, the shopkeeper, armor stands, and fireballs,
	 * which are knocked back by a punch (that goes through this event too, with the fireball as the
	 * victim). FWPlayerListener only looked at hits on players, so everything else got through.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorHit(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager instanceof Projectile) {
			ProjectileSource shooter = ((Projectile) damager).getShooter();
			if (shooter instanceof Entity) damager = (Entity) shooter;
		}
		if (isSpectator(damager)) event.setCancelled(true);
	}

	/** No right-clicking villagers (trading), animals, item frames, armor stands, boats or minecarts. */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorInteractEntity(PlayerInteractEntityEvent event) {
		if (isSpectator(event.getPlayer())) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorArmorStand(PlayerArmorStandManipulateEvent event) {
		if (isSpectator(event.getPlayer())) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorBreakHanging(HangingBreakByEntityEvent event) {
		if (isSpectator(event.getRemover())) event.setCancelled(true);
	}

	/**
	 * Nothing from the world opens for a spectator (a chest, a horse's saddle, a trade window).
	 * The plugin's own menus (Spectate, Leave) belong to the player, not to a block or a mob, so
	 * they still open.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorOpenInventory(InventoryOpenEvent event) {
		if (!isSpectator(event.getPlayer())) return;

		InventoryHolder holder = event.getInventory().getHolder();
		boolean fromTheWorld = holder instanceof BlockState
				|| holder instanceof DoubleChest
				|| (holder instanceof Entity && !(holder instanceof Player));
		if (fromTheWorld) event.setCancelled(true);
	}

	/**
	 * A spectator's own inventory is just the Compass and the Leave barrier in fixed slots, so
	 * moving anything around in it (or dragging it, or crafting) is shut off. The Spectate and
	 * Leave menus are chests, so they aren't touched here.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorInventoryClick(InventoryClickEvent event) {
		if (isSpectator(event.getWhoClicked()) && event.getInventory().getType() == InventoryType.CRAFTING) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorInventoryDrag(InventoryDragEvent event) {
		if (isSpectator(event.getWhoClicked()) && event.getInventory().getType() == InventoryType.CRAFTING) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorDrop(PlayerDropItemEvent event) {
		if (isSpectator(event.getPlayer())) event.setCancelled(true);
	}

	/** A thrown potion's burst gives nothing to a spectator hovering in it (Speed, Jump, Invisibility...). */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpectatorSplash(PotionSplashEvent event) {
		// Copied first: setIntensity edits the very collection getAffectedEntities hands back
		for (LivingEntity affected : new ArrayList<>(event.getAffectedEntities())) {
			if (isSpectator(affected)) event.setIntensity(affected, 0.0);
		}
	}
}
