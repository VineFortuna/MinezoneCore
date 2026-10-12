package anthony.flagwars;

import anthony.SuperCraftBrawl.Game.classes.CooldownNatowski;
import anthony.SuperCraftBrawl.gui.StatsGUI;
import anthony.util.SoundManager;
import anthony.flagwars.items.BridgeEggItem;
import anthony.flagwars.items.FreezeRayItem;
import anthony.flagwars.items.GrapplingHookItem;
import anthony.flagwars.items.TeleportBowItem;
import anthony.flagwars.items.ThrowableTntItem;
import anthony.flagwars.items.UnstableBridgeItem;
import anthony.flagwars.map.FWTeamSite;
import anthony.flagwars.resources.FWResourceGenerator;
import anthony.flagwars.shop.FWShopGUI;
import anthony.flagwars.vote.FWLeaveGameGUI;
import anthony.flagwars.vote.FWSpectatorGUI;
import anthony.flagwars.vote.FWVoteGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * All Flag Wars-only event handling. Every handler bails immediately
 * if the player isn't in the active FD instance, exactly mirroring how
 * SuperCraftBros' PlayerListener gates on getGameManager().GetInstanceOfPlayer().
 * Registered as its own Listener so it never interferes with SCB's.
 */
public class FWPlayerListener implements Listener {

	private static final double TELEPORT_BOW_COOLDOWN_SECONDS = 30.0;

	private final FWGameManager fwGameManager;
	private final Map<UUID, CooldownNatowski> teleportBowCooldowns = new HashMap<>();
	private final List<CooldownItem> cooldownItems;

	public FWPlayerListener(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
		this.cooldownItems = Arrays.asList(
				new CooldownItem(TeleportBowItem.NAME, TeleportBowItem::matches, teleportBowCooldowns)
		);
		startCooldownActionBar();
	}

	/** One item this listener tracks a per-player cooldown for and shows in the combined action bar below. */
	private static final class CooldownItem {
		final String name;
		final Predicate<ItemStack> matcher;
		final Map<UUID, CooldownNatowski> cooldowns;

		CooldownItem(String name, Predicate<ItemStack> matcher, Map<UUID, CooldownNatowski> cooldowns) {
			this.name = name;
			this.matcher = matcher;
			this.cooldowns = cooldowns;
		}
	}

	private static final long RECENT_ATTACKER_WINDOW_MILLIS = 10_000L;
	private final Map<UUID, RecentAttack> recentAttackers = new HashMap<>();
	/** Players whose next FALL-damage event should be cancelled because it comes from an ender pearl teleport. */
	private final Set<UUID> noPearlDamage = new HashSet<>();
	/** Players currently airborne from a grappling hook launch — fall damage is suppressed until they land. */
	private final Set<UUID> grapplingNoFallDamage = new HashSet<>();

	/** Who hit a player most recently (PvP, TNT, Fire Charge) and when - see recentAttackerOf below. */
	private static final class RecentAttack {
		final UUID attackerId;
		final long timestamp;

		RecentAttack(UUID attackerId, long timestamp) {
			this.attackerId = attackerId;
			this.timestamp = timestamp;
		}
	}

	/**
	 * Constant action bar listing every cooldown item a player is carrying (Grappling Hook,
	 * Teleport Bow), joined with "&8|" so more than one shows at once - same underlying
	 * ActionBarManager#setActionBar SCB's own class abilities use, refreshed every tick with a
	 * short TTL so it never flickers.
	 */
	private void startCooldownActionBar() {
		Bukkit.getScheduler().runTaskTimer(fwGameManager.getMain(), () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
				if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) continue;

				List<String> segments = new ArrayList<>();
				for (CooldownItem item : cooldownItems) {
					if (!hasItem(player, item.matcher)) continue;

					CooldownNatowski cooldown = item.cooldowns.get(player.getUniqueId());
					segments.add(cooldown == null || cooldown.isReady()
							? "&a&l" + item.name + ": &aReady"
							: "&a&l" + item.name + ": &cin " + (cooldown.getRemainingCooldownSeconds() + 1) + "s");
				}
				if (segments.isEmpty()) continue;

				fwGameManager.getMain().getActionBarManager().setActionBar(player, "fd.cooldowns",
						fwGameManager.getMain().color(String.join(" &8| ", segments)), 2);
			}
		}, 0L, 1L);
	}

	private boolean hasItem(Player player, Predicate<ItemStack> matcher) {
		for (ItemStack item : player.getInventory().getContents()) {
			if (matcher.test(item)) return true;
		}
		return false;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(event.getPlayer());
		if (instance == null) return;

		if (instance.getMapConfig().isProtectedFromBuilding(event.getBlock().getLocation())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(fwGameManager.getMain().color(
					"&c&l(!) &rYou can't build in important areas!"));
			return;
		}

		// A regular chest (unlike an ender chest) can't be opened at all once something solid sits
		// directly on top of it - blocks a base's chest permanently if left unchecked.
		if (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.CHEST) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(fwGameManager.getMain().color(
					"&c&l(!) &rYou can't build on top of a chest!"));
			return;
		}

		/*
		 * Hypixel's shop TNT auto-primes on placement instead of sitting inert
		 * (players are never given flint and steel) - mirrors the same
		 * EntityType.PRIMED_TNT spawn throwTnt() below uses for the thrown variant.
		 */
		if (event.getBlock().getType() == Material.TNT) {
			event.setCancelled(true);
			TNTPrimed tnt = (TNTPrimed) event.getBlock().getWorld().spawnEntity(
					event.getBlock().getLocation().add(0.5, 0, 0.5), org.bukkit.entity.EntityType.PRIMED_TNT);
			tnt.setFuseTicks(80);
			tnt.setMetadata(TNT_METADATA, new FixedMetadataValue(fwGameManager.getMain(), true));
			tnt.setMetadata(TNT_OWNER_METADATA, new FixedMetadataValue(fwGameManager.getMain(), event.getPlayer().getUniqueId()));

			// Cancelling the placement means vanilla's own consumption never kicks in either -
			// without this, "placing" shop TNT never actually cost anything (infinite TNT).
			ItemStack handItem = event.getItemInHand();
			if (handItem.getAmount() <= 1) {
				event.getPlayer().setItemInHand(null);
			} else {
				handItem.setAmount(handItem.getAmount() - 1);
			}
			return;
		}

		instance.recordBlockChange(event.getBlockReplacedState());
		instance.markPlaced(event.getBlock());
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(event.getPlayer());
		if (instance == null) return;

		Block target = event.getBlockClicked().getRelative(event.getBlockFace());
		if (instance.getMapConfig().isProtectedFromBuilding(target.getLocation())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(fwGameManager.getMain().color(
					"&c&l(!) &rYou can't build in important areas!"));
			return;
		}

		// Remove the empty bucket vanilla gives back after placing water - single use in Bedwars style.
		Player bucketPlayer = event.getPlayer();
		Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(), () -> {
			ItemStack inHand = bucketPlayer.getItemInHand();
			if (inHand != null && inHand.getType() == Material.BUCKET) {
				bucketPlayer.setItemInHand(null);
			}
		}, 1L);
	}

	@EventHandler
	public void onLiquidFlow(BlockFromToEvent event) {
		FWGameInstance instance = fwGameManager.getInstanceForWorld(event.getBlock().getWorld());
		if (instance == null) return;

		Block to = event.getToBlock();

		if (instance.getMapConfig().isProtectedFromBuilding(to.getLocation())) {
			event.setCancelled(true);
			return;
		}

		// Stop liquid from displacing any map block (buttons, levers, signs, etc.)
		// that wasn't placed by a player during this match.
		Material toType = to.getType();
		boolean toLiquid = toType == Material.WATER || toType == Material.STATIONARY_WATER
				|| toType == Material.LAVA || toType == Material.STATIONARY_LAVA;
		if (toType != Material.AIR && !toLiquid && !instance.isPlacedByPlayer(to)) {
			event.setCancelled(true);
		}
	}

	/**
	 * Bedwars rule: the map's own terrain can't be broken - only blocks a player
	 * placed during the match. Flags are never broken at all - stealing happens
	 * via a left-click instead (see onPlayerInteract) - so a flag block falling
	 * through to this generic "not placed by a player" rule is exactly what
	 * should happen if a break is ever attempted on one.
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(event.getPlayer());
		if (instance == null) return;

		if (!instance.isPlacedByPlayer(event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(fwGameManager.getMain().color(
					"&c&l(!) &rYou can only break blocks placed by players!"));
			return;
		}

		instance.recordBlockChange(event.getBlock().getState());
		instance.unmarkPlaced(event.getBlock());
	}

	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;

		Integer siteId = instance.getFlagManager().findSiteByShopkeeper(event.getRightClicked());
		if (siteId == null) return;

		event.setCancelled(true);
		FWTeam team = instance.findTeamOf(player);
		if (team != null) {
			new FWShopGUI(instance, team).open(player);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();

		FWGameInstance shopInstance = fwGameManager.getInstanceForWorld(entity.getWorld());
		if (shopInstance != null && shopInstance.getFlagManager().findSiteByShopkeeper(entity) != null) {
			event.setCancelled(true);
			return;
		}

		if (entity instanceof Player) {
			Player player = (Player) entity;

			// Spectators are GameMode.ADVENTURE (mirroring SCB), not vanilla SPECTATOR, so they can
			// still take fall/fire/etc. damage unless explicitly blocked here.
			if (fwGameManager.getInstanceOfSpectator(player) != null) {
				event.setCancelled(true);
				return;
			}

			FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
			if (instance == null) return;

			if (instance.getState() != FWGameState.IN_PROGRESS) {
				event.setCancelled(true);
				return;
			}

			if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
				if (noPearlDamage.remove(player.getUniqueId())) {
					event.setCancelled(true);
					return;
				}
				if (grapplingNoFallDamage.remove(player.getUniqueId())) {
					event.setCancelled(true);
					return;
				}
			}

			// Entity-caused damage (PvP, mobs) is handled by onEntityDamageByEntity,
			// which shares this same event - only handle the environmental causes
			// (fall, fire, lava, drowning, etc.) here so a kill is never registered twice.
			// Credits whoever last hit this player (recentAttackerOf) instead of a plain,
			// attacker-less death - e.g. knocked off a ledge and finished off by the fall itself.
			if (!(event instanceof EntityDamageByEntityEvent) && !event.isCancelled()
					&& event.getFinalDamage() >= player.getHealth() - 0.2) {
				event.setCancelled(true);
				instance.killPlayer(player, recentAttackerOf(player));
			}
			return;
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;

		Player player = (Player) entity;
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		/*
		 * The Fire Charge's own impact handling (onProjectileHit -> applyFireChargeImpact) replaces
		 * vanilla's hit damage entirely with Hypixel's flat, distance-independent numbers - so the
		 * collision damage NMS would otherwise deal here on contact is cancelled unconditionally,
		 * for every victim (self/teammate/enemy alike), to avoid a double hit.
		 */
		if (event.getDamager().hasMetadata(FIRE_CHARGE_METADATA)) {
			event.setCancelled(true);
			return;
		}

		// TNT player damage is handled by applyTntImpact (called from onTntExplode) with flat
		// Hypixel-style numbers and custom knockback - cancel vanilla distance-based damage here.
		if (event.getDamager().hasMetadata(TNT_METADATA)) {
			event.setCancelled(true);
			return;
		}

		Player attacker = resolveKiller(event.getDamager());

		// Spectators can't deal damage - same as SCB's own onEntityDamageByEntity spectator check.
		if (attacker != null && fwGameManager.getInstanceOfSpectator(attacker) != null) {
			event.setCancelled(true);
			return;
		}

		// Spawn protection: protected players can't deal or receive damage (same as SCB).
		if (attacker != null && fwGameManager.isSpawnProtected(attacker)) {
			event.setCancelled(true);
			SoundManager.playNMSSoundToPlayer(attacker, "mob.guardian.elder.hit", 1, 1.6f);
			return;
		}
		if (fwGameManager.isSpawnProtected(player)) {
			event.setCancelled(true);
			SoundManager.playNMSSoundToPlayer(player, "mob.guardian.elder.hit", 1, 1.6f);
			return;
		}

		// No friendly fire - matters now that Duos puts more than one player on the same team.
		if (attacker != null && attacker != player) {
			FWTeam victimTeam = instance.findTeamOf(player);
			if (victimTeam != null && victimTeam == instance.findTeamOf(attacker)) {
				event.setCancelled(true);
				return;
			}

			// Remembered so a later, attacker-less death (fall, fire, lava...) within the
			// window can still be credited to them - see recentAttackerOf.
			recentAttackers.put(player.getUniqueId(), new RecentAttack(attacker.getUniqueId(), System.currentTimeMillis()));
		}

		applyTeleportBow(event.getDamager(), player.getLocation());

		if (!event.isCancelled() && event.getFinalDamage() >= player.getHealth() - 0.2) {
			event.setCancelled(true);
			// Self-damage (e.g. your own TNT) shouldn't credit you a kill against yourself -
			// same no-self-credit rule applyFireChargeImpact already applies.
			instance.killPlayer(player, attacker != player ? attacker : null);
		}
	}

	private Player resolveKiller(Entity damager) {
		if (damager instanceof Player) return (Player) damager;
		if (damager instanceof Projectile) {
			Object shooter = ((Projectile) damager).getShooter();
			if (shooter instanceof Player) return (Player) shooter;
		}
		// TNTPrimed's own getSource() is whoever ignited it with flint and steel - always null for
		// the shop/thrown TNT this mode spawns directly via World#spawnEntity, so the placer/thrower
		// is tagged with metadata instead (see TNT_OWNER_METADATA, set in onBlockPlace/throwTnt).
		if (damager.hasMetadata(TNT_OWNER_METADATA)) {
			Object value = damager.getMetadata(TNT_OWNER_METADATA).get(0).value();
			if (value instanceof UUID) return Bukkit.getPlayer((UUID) value);
		}
		return null;
	}

	private static final double GENERATOR_SPLIT_RADIUS = 1.5;

	/**
	 * Real Hypixel "gen splitting" isn't a deliberate server feature - it's players abusing
	 * vanilla item pickup so more than one of them gets credit for the same stack. Recreating
	 * that outcome on purpose instead: whoever actually triggers the vanilla pickup keeps getting
	 * it normally below, but any teammate standing within reach of the item at that same moment
	 * also gets their own independent copy added straight to their inventory - so two (or more)
	 * teammates standing at the generator together both walk away with resources, no positioning
	 * tricks required.
	 */
	@EventHandler
	public void onPickupGeneratorItem(PlayerPickupItemEvent event) {
		Item item = event.getItem();
		if (!item.hasMetadata(FWResourceGenerator.GENERATOR_ITEM_METADATA)) return;

		Player picker = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(picker);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		FWTeam team = instance.findTeamOf(picker);
		if (team == null) return;

		ItemStack template = item.getItemStack().clone();
		Location itemLoc = item.getLocation();

		for (UUID uuid : team.getMembers()) {
			if (uuid.equals(picker.getUniqueId())) continue;

			Player teammate = Bukkit.getPlayer(uuid);
			if (teammate == null || !teammate.getWorld().equals(item.getWorld())) continue;
			if (teammate.getLocation().distanceSquared(itemLoc) > GENERATOR_SPLIT_RADIUS * GENERATOR_SPLIT_RADIUS) continue;

			teammate.getInventory().addItem(template.clone());
		}
	}

	/**
	 * Same check SCB's BaseClass does before crediting a "doomed to fall" void kill: was this
	 * player's most recent recorded damage (Bukkit's own getLastDamageCause(), not a custom
	 * timer) caused by another player or their projectile? Falls back to the timestamped
	 * recentAttackers map (trackedRecentAttacker) when that doesn't resolve - getLastDamageCause()
	 * is no help for a death whose own EntityDamageEvent IS the lethal hit (e.g. fatal fall
	 * damage), since CraftBukkit stamps that very event onto getLastDamageCause() before this
	 * plugin's handler for it ever runs, overwriting whatever PvP hit preceded it.
	 */
	private Player recentAttackerOf(Player player) {
		EntityDamageEvent lastCause = player.getLastDamageCause();
		if (lastCause instanceof EntityDamageByEntityEvent) {
			Player attacker = resolveKiller(((EntityDamageByEntityEvent) lastCause).getDamager());
			if (attacker != null && attacker != player) return attacker;
		}
		return trackedRecentAttacker(player);
	}

	/** Reads the recentAttackers map populated by onEntityDamageByEntity/applyFireChargeImpact, honoring RECENT_ATTACKER_WINDOW_MILLIS. */
	private Player trackedRecentAttacker(Player victim) {
		RecentAttack attack = recentAttackers.get(victim.getUniqueId());
		if (attack == null || System.currentTimeMillis() - attack.timestamp > RECENT_ATTACKER_WINDOW_MILLIS) return null;

		Player attacker = Bukkit.getPlayer(attack.attackerId);
		return attacker != null && attacker != victim ? attacker : null;
	}

	private static final String TELEPORT_BOW_METADATA = "fd_teleport_bow";
	private static final String TELEPORT_BOW_DIRECTION_METADATA = "fd_teleport_bow_direction";

	/**
	 * Shooting a player with the Teleport Bow teleports the shooter to them; shooting the
	 * ground works too, teleporting the shooter to wherever the arrow landed (see
	 * onProjectileHit below, same as a regular ender pearl). Metadata is consumed on first
	 * use so a block-hit and an entity-hit can never both fire for the same arrow.
	 * Cooldown is already consumed at shoot time (onShootBow), so a miss still costs the cooldown.
	 */
	private void applyTeleportBow(Entity arrow, Location teleportTo) {
		if (!(arrow instanceof Arrow) || !arrow.hasMetadata(TELEPORT_BOW_METADATA)) return;

		Object value = arrow.getMetadata(TELEPORT_BOW_METADATA).get(0).value();
		Location shotFrom = arrow.hasMetadata(TELEPORT_BOW_DIRECTION_METADATA)
				? (Location) arrow.getMetadata(TELEPORT_BOW_DIRECTION_METADATA).get(0).value()
				: null;
		arrow.removeMetadata(TELEPORT_BOW_METADATA, fwGameManager.getMain());
		arrow.removeMetadata(TELEPORT_BOW_DIRECTION_METADATA, fwGameManager.getMain());
		if (!(value instanceof UUID)) return;

		Player shooter = Bukkit.getPlayer((UUID) value);
		if (shooter == null) return;

		// Face the direction the arrow was shot in, not wherever it landed/whoever it hit was facing.
		Location destination = teleportTo.clone();
		if (shotFrom != null) {
			destination.setYaw(shotFrom.getYaw());
			destination.setPitch(shotFrom.getPitch());
		}
		shooter.teleport(destination);
	}

	@EventHandler
	public void onShootBow(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player)) return;

		Player shooter = (Player) event.getEntity();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(shooter);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		if (!TeleportBowItem.matches(event.getBow())) return;

		CooldownNatowski cooldown = teleportBowCooldowns.computeIfAbsent(shooter.getUniqueId(),
				id -> new CooldownNatowski(TELEPORT_BOW_COOLDOWN_SECONDS));

		if (!cooldown.isReady()) {
			event.setCancelled(true);
			shooter.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTeleport Bow is on cooldown for &e"
					+ cooldown.getRemainingCooldownSeconds() + "s&r!"));
			return;
		}

		cooldown.use();

		event.getProjectile().setMetadata(TELEPORT_BOW_METADATA,
				new FixedMetadataValue(fwGameManager.getMain(), shooter.getUniqueId()));
		event.getProjectile().setMetadata(TELEPORT_BOW_DIRECTION_METADATA,
				new FixedMetadataValue(fwGameManager.getMain(), shooter.getLocation().clone()));
	}

	/** Teleport Bow hitting a block instead of a player - same metadata-tagged arrow, just resolved here instead of onEntityDamageByEntity. */
	@EventHandler
	public void onProjectileHit(org.bukkit.event.entity.ProjectileHitEvent event) {
		applyTeleportBow(event.getEntity(), event.getEntity().getLocation());

		if (event.getEntity().hasMetadata(FIRE_CHARGE_METADATA) && event.getEntity() instanceof Fireball) {
			applyFireChargeImpact((Fireball) event.getEntity());
		}
	}

	private static final double FLAG_CLICK_REACH = 5.0;

	/**
	 * Either click steals a flag - no mining required. Avoids actually breaking
	 * the banner block at all (1.8 banner blocks are known to glitch out when
	 * broken/viewed by newer client versions), and is a snappier feel for the
	 * steal anyway. Cancelled unconditionally so a left-click swing never starts
	 * any real block damage.
	 *
	 * Checks LEFT/RIGHT_CLICK_BLOCK and LEFT/RIGHT_CLICK_AIR and ignores whatever
	 * block (if any) vanilla decided was clicked - banners render taller than
	 * their actual block hitbox, so aiming at the upper part of the visible flag
	 * often doesn't register a clicked block at all. FWFlagManager#siteIdLookingAt
	 * does its own aim check instead (the flag's own block plus the block above
	 * it), so the whole flag is clickable top to bottom with no horizontal slop
	 * onto neighboring blocks.
	 *
	 * Runs at LOW priority (before onPlayerInteract's NORMAL) and cancels the
	 * event on a successful steal, so right-clicking a flag while holding a Fire
	 * Charge/Throwable TNT/Village Bell/etc. steals it instead of also using
	 * that item - onPlayerInteract checks isCancelled() to honor that.
	 */
	@EventHandler(priority = org.bukkit.event.EventPriority.LOW)
	public void onClickFlag(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.LEFT_CLICK_BLOCK && action != Action.LEFT_CLICK_AIR
				&& action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		Integer ownerSiteId = instance.getFlagManager().siteIdLookingAt(player, FLAG_CLICK_REACH);
		if (ownerSiteId == null) return;

		// Don't cancel the event if it's the player's own flag — they should still be
		// able to mine blocks around it. attemptSteal already handles the rejection message.
		FWTeam playerTeam = instance.findTeamOf(player);
		FWTeam ownerTeam = instance.getTeams().get(ownerSiteId);
		if (playerTeam != null && playerTeam == ownerTeam) return;

		event.setCancelled(true);
		instance.getFlagManager().attemptSteal(ownerSiteId, player);
	}

	/** Player inventory's armor row is always slots 36-39 (boots/leggings/chestplate/helmet). */
	private static final int HELMET_SLOT = 39;

	/**
	 * A carried flag banner sits in the helmet slot (see FWFlagManager#attemptSteal) only as a
	 * visible "you're holding it" marker - it's never meant to be a removable item, not even for
	 * staff, so this has no op bypass unlike most other protections.
	 */
	@EventHandler
	public void onHelmetClickWhileCarryingFlag(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		if (event.getSlotType() != InventoryType.SlotType.ARMOR || event.getSlot() != HELMET_SLOT) return;

		Player player = (Player) event.getWhoClicked();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		if (instance.getFlagManager().isCarrying(player)) {
			event.setCancelled(true);
		}
	}

	/** Prevent the crafting result slot from ever populating for players in a match. */
	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent event) {
		if (!(event.getView().getPlayer() instanceof Player)) return;
		if (fwGameManager.getInstanceOfPlayer((Player) event.getView().getPlayer()) == null) return;
		event.getInventory().setResult(null);
	}

	/** Block interaction with the crafting grid slots and result slot during a match (not the rest of the inventory). */
	@EventHandler
	public void onCraftingGridClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		InventoryType type = event.getInventory().getType();
		if (type != InventoryType.CRAFTING && type != InventoryType.WORKBENCH) return;
		InventoryType.SlotType slot = event.getSlotType();
		if (slot != InventoryType.SlotType.CRAFTING && slot != InventoryType.SlotType.RESULT) return;
		if (fwGameManager.getInstanceOfPlayer((Player) event.getWhoClicked()) == null) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		// Deliberately NOT checking event.isCancelled() here - Bukkit's own PlayerInteractEvent
		// javadoc says it "will fire as cancelled if the vanilla behavior is to do nothing (e.g
		// interacting with air)", and a Fire Charge/TNT has zero vanilla behavior with nothing in
		// front of it, so EVERY air-throw was being auto-cancelled and silently swallowed by that
		// check - it only ever looked like it "worked" when aimed at a block close enough to
		// register RIGHT_CLICK_BLOCK instead. Re-deriving the flag-steal check directly instead
		// (the actual thing this used to be guarding against) fixes that without losing the guard.
		if (instance.getFlagManager().siteIdLookingAt(player, FLAG_CLICK_REACH) != null) return;

		ItemStack item = event.getItem();

		if (ThrowableTntItem.matches(item)) {
			throwTnt(player);
		} else if (item != null && item.getType() == Material.FIREBALL) {
			throwFireCharge(player, item);
		} else if (FreezeRayItem.matches(item)) {
			event.setCancelled(true);
			shootFreezeRay(player, instance);
		} else if (BridgeEggItem.matches(item)) {
			event.setCancelled(true);
			launchBridgeEgg(player, instance);
		} else if (UnstableBridgeItem.matches(item)) {
			event.setCancelled(true);
			launchUnstableBridge(player, instance);
		}
	}

	/**
	 * Hypixel-style punch-to-deposit: left-clicking a chest or ender chest deposits the
	 * held item stack into it instead of doing nothing. Ender chests use the player's own
	 * personal ender chest inventory (consistent with how resetAndSendHome clears it).
	 */
	@EventHandler
	public void onPunchChestDeposit(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

		Block block = event.getClickedBlock();
		if (block == null) return;

		boolean isChest = block.getType() == Material.CHEST;
		boolean isEnderChest = block.getType() == Material.ENDER_CHEST;
		if (!isChest && !isEnderChest) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		event.setCancelled(true);

		ItemStack held = player.getItemInHand();
		if (held == null || held.getType() == Material.AIR) return;

		if (isSwordOrTool(held.getType())) {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rYou can't store swords or tools in a chest!"));
			return;
		}

		org.bukkit.inventory.Inventory target = isEnderChest
				? player.getEnderChest()
				: ((org.bukkit.block.Chest) block.getState()).getInventory();

		Material type = held.getType();
		org.bukkit.inventory.Inventory inv = player.getInventory();
		int totalDeposited = 0;

		for (int slot = 0; slot < inv.getSize(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (stack == null || stack.getType() != type) continue;

			Map<Integer, ItemStack> leftover = target.addItem(stack.clone());
			if (leftover.isEmpty()) {
				inv.setItem(slot, null);
				totalDeposited += stack.getAmount();
			} else {
				int remaining = leftover.get(0).getAmount();
				int deposited = stack.getAmount() - remaining;
				totalDeposited += deposited;
				if (deposited > 0) stack.setAmount(remaining);
				break; // chest is full, no point continuing
			}
		}

		if (totalDeposited > 0) {
			player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 1.2f);
		} else {
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rThe chest is full!"));
		}
	}

	/**
	 * Swords, pickaxes, axes, spades and hoes - Bedwars-style: these can't be stashed away in a chest.
	 * Shears don't follow the "_MATERIAL" naming pattern the other tools do (Material.SHEARS has no
	 * tier prefix), so they're checked explicitly rather than falling out of the suffix match.
	 */
	private boolean isSwordOrTool(Material type) {
		if (type == Material.SHEARS) return true;
		String name = type.name();
		return name.endsWith("_SWORD") || name.endsWith("_PICKAXE") || name.endsWith("_AXE")
				|| name.endsWith("_SPADE") || name.endsWith("_HOE");
	}

	/** Pre-game lobby items (Vote / My Profile / Leave Game), given out by FWLobbyItems while WAITING or STARTING. */
	@EventHandler
	public void onLobbyItemInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;
		if (instance.getState() != FWGameState.WAITING && instance.getState() != FWGameState.STARTING) return;

		ItemStack item = event.getItem();
		if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) return;

		String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

		if (item.getType() == Material.PAPER && name.contains("Vote")) {
			new FWVoteGUI(instance).open(player);
		} else if (item.getType() == Material.SKULL_ITEM && name.contains("My Profile")) {
			new StatsGUI(fwGameManager.getMain()).inv.open(player);
		} else if (item.getType() == Material.BARRIER && name.contains("Leave Game")) {
			new FWLeaveGameGUI(fwGameManager.getMain(), fwGameManager).inv.open(player);
		}
	}

	/** Spectator's Compass (spectate a player) + Leave - mirrors SCB's own spectator items, but spectators aren't tracked via getInstanceOfPlayer so they need their own gate (no WAITING/STARTING restriction either - spectating works at any match state). */
	@EventHandler
	public void onSpectatorItemInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfSpectator(player);
		if (instance == null) return;

		ItemStack item = event.getItem();
		if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) return;

		String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

		if (item.getType() == Material.COMPASS && name.contains("Spectate")) {
			new FWSpectatorGUI(instance).open(player);
		} else if (item.getType() == Material.BARRIER && name.contains("Leave")) {
			new FWLeaveGameGUI(fwGameManager.getMain(), fwGameManager).inv.open(player);
		}
	}

	/**
	 * Blocks/doors/pressure plates/buttons/levers/chests - any of it - same blanket cancel SCB's
	 * own itemInteract uses for spectators. Runs regardless of item type; onSpectatorItemInteract
	 * above doesn't check isCancelled(), so the Compass/Barrier dispatch still works fine.
	 */
	@EventHandler
	public void onSpectatorInteractBlocked(PlayerInteractEvent event) {
		if (fwGameManager.getInstanceOfSpectator(event.getPlayer()) != null) {
			event.setCancelled(true);
		}
	}

	/** Spectators can't pick up items off the ground either. */
	@EventHandler
	public void onSpectatorPickupBlocked(PlayerPickupItemEvent event) {
		if (fwGameManager.getInstanceOfSpectator(event.getPlayer()) != null) {
			event.setCancelled(true);
		}
	}

	/**
	 * Same grapple mechanic as SuperCraftBros' FishermanClass#onFish: cast the
	 * rod like a normal fishing rod, and once the hook embeds in (or right next
	 * to) a block, yank the player toward it. Far more reliable than a manual
	 * line-of-sight raycast since it rides on vanilla's own hook physics.
	 */
	@EventHandler
	public void onFish(PlayerFishEvent event) {
		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		if (!GrapplingHookItem.matches(player.getItemInHand())) return;

		PlayerFishEvent.State state = event.getState();
		if (state != PlayerFishEvent.State.FAILED_ATTEMPT && state != PlayerFishEvent.State.IN_GROUND) return;

		FishHook hook = event.getHook();
		Block block = hook.getLocation().getBlock();
		boolean grapple = block.getType() != Material.AIR;

		if (!grapple) {
			for (BlockFace face : BlockFace.values()) {
				if (block.getRelative(face).getType() != Material.AIR) {
					grapple = true;
					break;
				}
			}
		}
		if (!grapple) return;

		// Consume one use: rebuild the item so lore and durability bar both update
		int usesLeft = player.getItemInHand().getAmount() - 1;
		if (usesLeft == 0) {
			player.setItemInHand(null);
		} else {
			player.setItemInHand(GrapplingHookItem.create(usesLeft));
		}

		Vector toHook = hook.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
		player.setVelocity(toHook.multiply(2).add(new Vector(0, 0.8, 0)));
		player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 10);
		grapplingNoFallDamage.add(player.getUniqueId());
	}

	/**
	 * Real Hypixel Bridge Egg: no waiting for a thrown egg to actually fly and land - the whole
	 * bridge appears immediately, in the exact direction the player is looking (see
	 * buildBridgeInLookDirection). No egg projectile is even launched.
	 */
	private void launchBridgeEgg(Player player, FWGameInstance instance) {
		buildBridgeInLookDirection(player, instance);

		player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1f, 1.6f);

		ItemStack handItem = player.getItemInHand();
		if (handItem.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			handItem.setAmount(handItem.getAmount() - 1);
		}
	}

	/**
	 * Builds a flat 3-wide line of the thrower's team-colored Wool from their feet out to wherever
	 * they're looking - raytraced to the first solid block in the way, or BRIDGE_EGG_MAX_LENGTH (its
	 * max range) if nothing's in range. Looking basically straight up/down naturally builds nothing
	 * at all, since totalDist bails out below before any blocks get placed (the raytrace lands
	 * almost directly below/above the thrower, leaving ~0 horizontal distance to bridge).
	 * Solid blocks (map terrain and player-placed alike) are never replaced.
	 */
	@SuppressWarnings("deprecation")
	private void buildBridgeInLookDirection(Player shooter, FWGameInstance instance) {
		FWTeam team = instance.findTeamOf(shooter);
		if (team == null) return;

		Location eye = shooter.getEyeLocation();
		Vector dir = eye.getDirection();

		Location impact = eye.clone().add(dir.clone().multiply(BRIDGE_EGG_MAX_LENGTH));
		BlockIterator iter = new BlockIterator(eye, 0, BRIDGE_EGG_MAX_LENGTH);
		while (iter.hasNext()) {
			Block hit = iter.next();
			if (hit.getType().isSolid()) {
				impact = hit.getLocation().add(0.5, 0.5, 0.5);
				break;
			}
		}

		Location shooterFeet = shooter.getLocation();

		// XZ direction from thrower toward impact
		double dx = impact.getX() - shooterFeet.getX();
		double dz = impact.getZ() - shooterFeet.getZ();
		double totalDist = Math.sqrt(dx * dx + dz * dz);
		if (totalDist < 0.01) return;

		double bridgeDist = Math.min(totalDist, BRIDGE_EGG_MAX_LENGTH);
		dx /= totalDist;
		dz /= totalDist;

		// Perpendicular vector for the 3-wide bridge
		double px = -dz;
		double pz = dx;

		org.bukkit.World world = shooter.getWorld();

		// Interpolate Y from the thrower's feet to the impact point, proportionally
		// capped if bridgeDist < totalDist so the slope matches the actual look angle
		double startY = shooterFeet.getY();
		double endY   = startY + (impact.getY() - startY) * (bridgeDist / totalDist);

		byte woolData = team.getColor().getWoolData();
		BlockTexture woolTex = new BlockTexture(Material.WOOL, woolData);
		// Track placed XZ positions so the 0.5 sub-step sampling doesn't double-place
		Set<Long> visited = new HashSet<>();

		// Step at 0.5 to guarantee no grid cell is skipped on any diagonal angle
		for (double t = 1.0; t <= bridgeDist; t += 0.5) {
			double cx   = shooterFeet.getX() + dx * t;
			double cz   = shooterFeet.getZ() + dz * t;
			int    blockY = (int) Math.floor(startY + (endY - startY) * (t / bridgeDist));

			for (int w = -1; w <= 1; w++) {
				int bx = (int) Math.floor(cx + px * w);
				int bz = (int) Math.floor(cz + pz * w);

				long key = ((long) bx << 32) | (bz & 0xFFFFFFFFL);
				if (!visited.add(key)) continue;

				Block block = world.getBlockAt(bx, blockY, bz);
				if (block.getType().isSolid()) continue;
				if (instance.getMapConfig().isProtectedFromBuilding(block.getLocation())) continue;

				instance.recordBlockChange(block.getState());
				block.setTypeIdAndData(Material.WOOL.getId(), woolData, true);
				instance.markPlaced(block);

				// 3 wool-crack particles per block - visible but won't spike packet counts
				Location center = block.getLocation().add(0.5, 0.5, 0.5);
				ParticleEffect.BLOCK_CRACK.display(center, 0.3f, 0.2f, 0.3f, 0f, 3, woolTex);
			}
		}
	}

	private static final int UNSTABLE_BRIDGE_LENGTH = 10;
	private static final long UNSTABLE_BRIDGE_BUILD_STEP_TICKS = 2L;
	private static final int UNSTABLE_BRIDGE_COLLAPSE_DELAY_TICKS = 5 * 20;
	private static final long UNSTABLE_BRIDGE_COLLAPSE_STEP_TICKS = 2L;
	/** Upper-half slab (WOOD_STEP data bit 0x8) so it sits flush against the underside of the block it's supporting, instead of a bottom-half slab leaving a visible half-block gap. */
	private static final byte UNSTABLE_BRIDGE_SUPPORT_DATA = 8;
	/** Tags a FallingBlock this feature spawned itself, so onFallingSandOrGravelLand can recognize it precisely instead of only guessing off material. */
	private static final String UNSTABLE_BRIDGE_FALLING_METADATA = "fw_unstable_bridge_falling";
	/** Safety net: force-clears a collapsed tile's FallingBlock if it somehow never lands (stuck geometry, pushed off-map, etc.) so a bridge block can never linger as a stray, un-broken entity. */
	private static final long UNSTABLE_BRIDGE_FALLING_TIMEOUT_TICKS = 3 * 20L;

	/** Sand, Red Sand (Material.SAND data 1) and Gravel - picked in lockstep by index, one random material per block. */
	private static final Material[] UNSTABLE_BRIDGE_MATERIALS = {Material.SAND, Material.SAND, Material.GRAVEL};
	private static final byte[] UNSTABLE_BRIDGE_DATA = {0, 1, 0};

	/**
	 * Snaps a player's yaw to the nearest of the 8 horizontal directions - the 4 cardinals plus the
	 * 4 diagonals in between - as a (dx, dz) unit step. Diagonal steps are (±1, ±1), which is still
	 * a perfectly regular grid (each row advances one full block on both axes at once), so the
	 * bridge stays a clean 3-wide rectangle at any of the 8 angles instead of gapping out at an
	 * arbitrary free angle.
	 */
	private int[] facingDirectionOf(Player player) {
		float yaw = ((player.getLocation().getYaw() % 360) + 360) % 360;
		int sector = Math.round(yaw / 45f) % 8;
		switch (sector) {
			case 0: return new int[]{0, 1};    // south
			case 1: return new int[]{-1, 1};   // southwest
			case 2: return new int[]{-1, 0};   // west
			case 3: return new int[]{-1, -1};  // northwest
			case 4: return new int[]{0, -1};   // north
			case 5: return new int[]{1, -1};   // northeast
			case 6: return new int[]{1, 0};    // east
			default: return new int[]{1, 1};   // southeast
		}
	}

	/**
	 * Unstable Bridge: builds a flat 3-wide, 10-long bridge straight out in the direction the
	 * player is facing, one row at a time (see buildUnstableBridgeStep), then 5 seconds after the
	 * last row goes down, collapses it row by row (see collapseUnstableBridgeStep). Solid blocks
	 * and protected areas are skipped for both the bridge cell and the support slab beneath it,
	 * same as Bridge Egg.
	 */
	private void launchUnstableBridge(Player player, FWGameInstance instance) {
		Location feet = player.getLocation();
		org.bukkit.World world = player.getWorld();
		int[] dir = facingDirectionOf(player);
		int dx = dir[0], dz = dir[1];
		int px = -dz, pz = dx; // perpendicular, for the 3-wide span

		int baseX = feet.getBlockX();
		int baseZ = feet.getBlockZ();
		int baseY = feet.getBlockY();

		List<List<Block>> rows = new ArrayList<>();
		for (int step = 1; step <= UNSTABLE_BRIDGE_LENGTH; step++) {
			int cx = baseX + dx * step;
			int cz = baseZ + dz * step;

			List<Block> row = new ArrayList<>();
			for (int w = -1; w <= 1; w++) {
				Block block = world.getBlockAt(cx + px * w, baseY, cz + pz * w);
				Block support = block.getRelative(BlockFace.DOWN);
				if (block.getType().isSolid() || support.getType().isSolid()) continue;
				if (instance.getMapConfig().isProtectedFromBuilding(block.getLocation())) continue;
				if (instance.getMapConfig().isProtectedFromBuilding(support.getLocation())) continue;
				row.add(block);
			}
			rows.add(row);
		}

		ItemStack handItem = player.getItemInHand();
		if (handItem.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			handItem.setAmount(handItem.getAmount() - 1);
		}

		player.getWorld().playSound(player.getLocation(), Sound.DIG_SAND, 1f, 1f);
		buildUnstableBridgeStep(instance, rows, 0);
	}

	/**
	 * Places the real Sand/Red Sand/Gravel directly - a Wood Slab placed one block underneath each
	 * one is what actually keeps it from falling, since a solid block directly below (even a
	 * partial one like a slab) is genuine, ordinary vanilla support - no event-cancelling trick
	 * needed, and no risk of it silently failing to work like BlockPhysicsEvent cancellation did.
	 * The slab is placed as its upper half (UNSTABLE_BRIDGE_SUPPORT_DATA) so it sits flush against
	 * the underside of the block it's holding up - a default bottom-half slab would leave a visible
	 * (and walkable-into) half-block gap between the two.
	 * collapseUnstableBridgeStep knocks the slab back out from under each row once it's time to fall.
	 */
	@SuppressWarnings("deprecation")
	private void buildUnstableBridgeStep(FWGameInstance instance, List<List<Block>> rows, int rowIndex) {
		if (rowIndex >= rows.size()) {
			Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(),
					() -> collapseUnstableBridgeStep(instance, rows, 0), UNSTABLE_BRIDGE_COLLAPSE_DELAY_TICKS);
			return;
		}

		List<Block> row = rows.get(rowIndex);
		for (Block block : row) {
			Block support = block.getRelative(BlockFace.DOWN);
			instance.recordBlockChange(support.getState());
			support.setTypeIdAndData(Material.WOOD_STEP.getId(), UNSTABLE_BRIDGE_SUPPORT_DATA, true);
			instance.markPlaced(support);

			int pick = ThreadLocalRandom.current().nextInt(UNSTABLE_BRIDGE_MATERIALS.length);
			instance.recordBlockChange(block.getState());
			block.setTypeIdAndData(UNSTABLE_BRIDGE_MATERIALS[pick].getId(), UNSTABLE_BRIDGE_DATA[pick], true);
			instance.markPlaced(block);
		}
		if (!row.isEmpty()) {
			row.get(0).getWorld().playSound(row.get(0).getLocation(), Sound.DIG_SAND, 0.6f, 1.2f);
		}

		Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(),
				() -> buildUnstableBridgeStep(instance, rows, rowIndex + 1), UNSTABLE_BRIDGE_BUILD_STEP_TICKS);
	}

	/**
	 * Collapses one row at a time, in the same near-to-far order the bridge was built, every block
	 * in a row falling together in the same tick.
	 *
	 * Both the support slab AND the tile itself are cleared to air right here, unconditionally -
	 * this used to just knock the slab out and rely on vanilla's own gravity check noticing and
	 * converting the tile into a FallingBlock on its own. That worked most of the time, but not
	 * reliably: occasionally the tile never got picked up by gravity at all (most consistently when
	 * the bridge sat right at ground level, i.e. only a block or so of fall distance) and was left
	 * behind as a real, solid, un-broken block sitting where a "collapsed" bridge should be open air
	 * - reported as invisible/uncollapsed blocking terrain. Clearing both blocks ourselves guarantees
	 * the tile's original location is genuinely open the instant this runs, no physics propagation
	 * required. A FallingBlock is still spawned in its place (spawnCollapsingBridgeBlock) purely for
	 * the visual/audio "it's collapsing" effect - see that method for how its own cleanup is made
	 * equally reliable.
	 */
	@SuppressWarnings("deprecation")
	private void collapseUnstableBridgeStep(FWGameInstance instance, List<List<Block>> rows, int rowIndex) {
		if (rowIndex >= rows.size()) return;

		for (Block block : rows.get(rowIndex)) {
			Block support = block.getRelative(BlockFace.DOWN);
			if (support.getType() == Material.WOOD_STEP) {
				instance.unmarkPlaced(support);
				support.setType(Material.AIR);
			}

			Material tileMaterial = block.getType();
			if (tileMaterial != Material.SAND && tileMaterial != Material.GRAVEL) continue; // player already broke/changed this tile

			byte tileData = block.getData();
			Location spawnAt = block.getLocation().add(0.5, 0.5, 0.5);

			instance.unmarkPlaced(block);
			block.setType(Material.AIR);

			spawnCollapsingBridgeBlock(block.getWorld(), spawnAt, tileMaterial, tileData);
		}

		Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(),
				() -> collapseUnstableBridgeStep(instance, rows, rowIndex + 1), UNSTABLE_BRIDGE_COLLAPSE_STEP_TICKS);
	}

	/**
	 * Spawns a real, manually-tagged FallingBlock for the visual/audio collapse effect - tagging it
	 * ourselves (UNSTABLE_BRIDGE_FALLING_METADATA) instead of guessing by material means
	 * onFallingSandOrGravelLand recognizes exactly this entity and nothing else. A short timeout is
	 * also scheduled as a safety net: if the entity somehow never lands (stuck on odd geometry,
	 * pushed off-map, whatever), it gets force-broken anyway instead of quietly wandering around
	 * forever - the same failure mode that used to leave stray un-broken tiles now just can't happen,
	 * on top of the tile's original spot already having been cleared unconditionally by the caller.
	 */
	private void spawnCollapsingBridgeBlock(org.bukkit.World world, Location loc, Material material, byte data) {
		FallingBlock fb = world.spawnFallingBlock(loc, material, data);
		fb.setDropItem(false);
		fb.setMetadata(UNSTABLE_BRIDGE_FALLING_METADATA, new FixedMetadataValue(fwGameManager.getMain(), true));

		Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(), () -> {
			if (!fb.isDead()) breakFallingBridgeBlock(fb);
		}, UNSTABLE_BRIDGE_FALLING_TIMEOUT_TICKS);
	}

	/** Shared by onFallingSandOrGravelLand and spawnCollapsingBridgeBlock's timeout - same BLOCK_CRACK + BlockTexture pattern used everywhere else in this file for block-break-style effects. */
	private void breakFallingBridgeBlock(FallingBlock fb) {
		Material material = fb.getMaterial();
		byte data = fb.getBlockData();
		Location loc = fb.getLocation().add(0.5, 0.5, 0.5);

		ParticleEffect.BLOCK_CRACK.display(loc, 0.35f, 0.35f, 0.35f, 0.08f, 18, new BlockTexture(material, data));
		loc.getWorld().playSound(loc, material == Material.GRAVEL ? Sound.DIG_GRAVEL : Sound.DIG_SAND, 1f, 1f);

		fb.remove();
	}

	/**
	 * Lands only Unstable Bridge's own FallingBlocks (tagged by spawnCollapsingBridgeBlock) - broken
	 * into particles instead of settling back into a real block. Scoped by metadata rather than by
	 * material/world now that the entity is spawned (and tagged) explicitly ourselves, so this can't
	 * mistake unrelated falling sand/gravel elsewhere on the map for a collapsing bridge tile.
	 */
	@EventHandler
	public void onFallingSandOrGravelLand(EntityChangeBlockEvent event) {
		if (!(event.getEntity() instanceof FallingBlock)) return;
		FallingBlock fb = (FallingBlock) event.getEntity();
		if (!fb.hasMetadata(UNSTABLE_BRIDGE_FALLING_METADATA)) return;

		event.setCancelled(true);
		breakFallingBridgeBlock(fb);
	}

	private void throwTnt(Player player) {
		TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getEyeLocation(), org.bukkit.entity.EntityType.PRIMED_TNT);
		tnt.setVelocity(player.getLocation().getDirection().multiply(0.95));
		tnt.setFuseTicks(60);
		tnt.setMetadata(TNT_METADATA, new FixedMetadataValue(fwGameManager.getMain(), true));
		tnt.setMetadata(TNT_OWNER_METADATA, new FixedMetadataValue(fwGameManager.getMain(), player.getUniqueId()));

		ItemStack handItem = player.getItemInHand();
		if (handItem.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			handItem.setAmount(handItem.getAmount() - 1);
		}
	}

	private static final int BRIDGE_EGG_MAX_LENGTH = 20;

	private static final String FIRE_CHARGE_METADATA = "fw_fire_charge";

	/**
	 * Hypixel's fireball damage/knockback isn't vanilla explosion physics at all - it's flat,
	 * distance-independent damage plus a hand-tuned velocity formula, ported straight from
	 * BedWars1058's FireballListener (the most widely deployed open-source Hypixel Bedwars clone)
	 * and its documented MainConfig defaults, rather than guessed at. Vanilla's own explosion
	 * falloff is both weaker on knockback and harsher on damage than this.
	 */
	private static final double FIRE_CHARGE_PROXIMITY_RADIUS    = 3.0;
	private static final double FIRE_CHARGE_KNOCKBACK_HORIZONTAL = 1.2;
	private static final double FIRE_CHARGE_KNOCKBACK_VERTICAL   = 0.65;
	private static final double FIRE_CHARGE_DAMAGE_SELF    = 2.0;
	private static final double FIRE_CHARGE_DAMAGE_ENEMY   = 3.0;
	private static final double FIRE_CHARGE_DAMAGE_TEAMMATE = 0.0;
	/**
	 * Hypixel's own fireball-proof set, straight off the Bedwars wiki: wool/clay/wood blow open,
	 * glass/end stone/obsidian don't - which is exactly why FWShopCatalog sells End Stone, Blast-
	 * Proof Glass and Obsidian at a premium over Wool in the first place.
	 */
	private static final Set<Material> FIRE_CHARGE_BLAST_PROOF = EnumSet.of(
			Material.GLASS, Material.THIN_GLASS, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE,
			Material.ENDER_STONE, Material.OBSIDIAN);

	private void throwFireCharge(Player player, ItemStack item) {
		Fireball fireball = player.launchProjectile(org.bukkit.entity.LargeFireball.class);
		fireball.setVelocity(fireball.getVelocity().multiply(1.4));
		fireball.setMetadata(FIRE_CHARGE_METADATA, new FixedMetadataValue(fwGameManager.getMain(), true));
		fireball.setYield((float) FIRE_CHARGE_PROXIMITY_RADIUS);

		if (item.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			item.setAmount(item.getAmount() - 1);
		}
		player.getWorld().playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1f, 1f);
	}

	private static final double FREEZE_RAY_BEAM_RADIUS  = 0.6;
	private static final double FREEZE_RAY_HIT_RADIUS_SQ = 2.0 * 2.0;
	private static final int    FREEZE_RAY_RANGE         = 10;

	private void shootFreezeRay(Player shooter, FWGameInstance instance) {
		Location eye = shooter.getEyeLocation();
		Vector dir = eye.getDirection().normalize();

		// Trace to the first solid block so particles don't clip through walls
		double maxDist = FREEZE_RAY_RANGE;
		BlockIterator iter = new BlockIterator(eye, 0, FREEZE_RAY_RANGE);
		while (iter.hasNext()) {
			org.bukkit.block.Block block = iter.next();
			if (block.getType().isSolid()) {
				maxDist = eye.distance(block.getLocation().add(0.5, 0.5, 0.5));
				break;
			}
		}

		// Perpendicular vectors for the cylindrical ice-particle beam (wider than IceClass: 0.35 → 0.6)
		Vector right = dir.clone().crossProduct(new Vector(0, 1, 0));
		if (right.lengthSquared() < 0.001) right = new Vector(1, 0, 0);
		right.normalize();
		Vector up = dir.clone().crossProduct(right).normalize();
		BlockTexture iceTex = new BlockTexture(Material.ICE);

		for (double t = 1.0; t <= maxDist; t += 0.5) {
			Location center = eye.clone().add(dir.clone().multiply(t));
			ParticleEffect.BLOCK_CRACK.display(center, 0f, 0f, 0f, 0f, 1, iceTex);
			ParticleEffect.BLOCK_CRACK.display(center.clone().add(right.clone().multiply( FREEZE_RAY_BEAM_RADIUS)), 0f, 0f, 0f, 0f, 1, iceTex);
			ParticleEffect.BLOCK_CRACK.display(center.clone().add(right.clone().multiply(-FREEZE_RAY_BEAM_RADIUS)), 0f, 0f, 0f, 0f, 1, iceTex);
			ParticleEffect.BLOCK_CRACK.display(center.clone().add(up.clone().multiply(    FREEZE_RAY_BEAM_RADIUS)), 0f, 0f, 0f, 0f, 1, iceTex);
			ParticleEffect.BLOCK_CRACK.display(center.clone().add(up.clone().multiply(   -FREEZE_RAY_BEAM_RADIUS)), 0f, 0f, 0f, 0f, 1, iceTex);
		}

		shooter.getWorld().playSound(shooter.getLocation(), Sound.GLASS, 1f, 0.8f);

		FWTeam shooterTeam = instance.findTeamOf(shooter);
		for (FWTeam team : instance.getTeams().values()) {
			if (team == shooterTeam) continue;
			for (UUID uuid : team.getMembers()) {
				Player target = Bukkit.getPlayer(uuid);
				if (target == null || target.getGameMode() == GameMode.SPECTATOR) continue;

				// Project target body-center onto the ray; reject if behind shooter or past max dist
				Vector toTarget = target.getLocation().add(0, 1, 0).toVector().subtract(eye.toVector());
				double dist = toTarget.dot(dir);
				if (dist <= 0 || dist > maxDist) continue;

				Location closest = eye.clone().add(dir.clone().multiply(dist));
				if (closest.distanceSquared(target.getLocation().add(0, 1, 0)) > FREEZE_RAY_HIT_RADIUS_SQ) continue;

				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 4 * 20, 1));
				target.damage(4.5, shooter);

				spawnFreezeRayHitEffect(target);
				Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(), () ->
						target.playSound(target.getLocation(), Sound.GLASS, 1f, 0.75f), 5L);
			}
		}

		ItemStack held = shooter.getItemInHand();
		if (held != null && held.getAmount() <= 1) {
			shooter.setItemInHand(null);
		} else if (held != null) {
			held.setAmount(held.getAmount() - 1);
		}
	}

	private void spawnFreezeRayHitEffect(Player target) {
		Location head = target.getEyeLocation().add(0, 0.3, 0);
		BlockTexture iceTex = new BlockTexture(Material.ICE);
		int points = 12;
		double radius = 0.65;
		for (int i = 0; i < points; i++) {
			double angle = (2 * Math.PI / points) * i;
			Location loc = head.clone().add(radius * Math.cos(angle), 0, radius * Math.sin(angle));
			ParticleEffect.BLOCK_CRACK.display(loc, 0f, 0.25f, 0f, 0.08f, 3, iceTex);
		}
	}

	/** Safe (on 1.8) replacement for setIsIncendiary(false) - see throwFireCharge's note above. */
	@EventHandler
	public void onFireChargePrime(org.bukkit.event.entity.ExplosionPrimeEvent event) {
		if (!event.getEntity().hasMetadata(FIRE_CHARGE_METADATA)) return;
		event.setFire(false);
	}

	/**
	 * Every player within FIRE_CHARGE_PROXIMITY_RADIUS of the impact gets knocked back and takes
	 * flat damage based on their relation to the shooter - self/teammate/enemy - same three-way
	 * split BedWars1058 uses. Lethal hits go through instance.killPlayer directly instead of
	 * player.damage(), and non-lethal ones stamp a synthetic EntityDamageByEntityEvent onto
	 * getLastDamageCause() afterward, so this still plugs into the same kill-credit path
	 * (recentAttackerOf/killPlayerInVoid) a normal hit would.
	 */
	private void applyFireChargeImpact(Fireball fireball) {
		if (!(fireball.getShooter() instanceof Player)) return;
		Player source = (Player) fireball.getShooter();

		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(source);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		Location impact = fireball.getLocation();
		FWTeam sourceTeam = instance.findTeamOf(source);

		for (Entity nearby : impact.getWorld().getNearbyEntities(impact,
				FIRE_CHARGE_PROXIMITY_RADIUS, FIRE_CHARGE_PROXIMITY_RADIUS, FIRE_CHARGE_PROXIMITY_RADIUS)) {
			if (!(nearby instanceof Player)) continue;
			Player player = (Player) nearby;

			knockBackFromFireCharge(player, impact);

			double damage;
			if (player == source) {
				damage = FIRE_CHARGE_DAMAGE_SELF;
			} else if (sourceTeam != null && sourceTeam == instance.findTeamOf(player)) {
				damage = FIRE_CHARGE_DAMAGE_TEAMMATE;
			} else {
				damage = FIRE_CHARGE_DAMAGE_ENEMY;
			}
			if (damage <= 0) continue;

			if (damage >= player.getHealth() - 0.2) {
				instance.killPlayer(player, player == source ? null : source);
			} else {
				player.damage(damage);
				player.setLastDamageCause(new EntityDamageByEntityEvent(
						fireball, player, EntityDamageEvent.DamageCause.PROJECTILE, damage));
				if (player != source) {
					recentAttackers.put(player.getUniqueId(), new RecentAttack(source.getUniqueId(), System.currentTimeMillis()));
				}
			}
		}
	}

	/**
	 * BedWars1058's exact formula: push away from the impact point horizontally, with a fixed
	 * strong vertical kick whenever the impact's below the player (the common "aim at your own
	 * feet" case) instead of a raw distance-based falloff - that fixed kick is what makes the jump
	 * reliable instead of fiddly.
	 */
	private void knockBackFromFireCharge(Player player, Location impact) {
		Vector diff = impact.toVector().subtract(player.getLocation().toVector());
		if (diff.lengthSquared() < 1.0E-6) diff = new Vector(0, -0.01, 0);

		Vector normalized = diff.normalize();
		Vector horizontal = normalized.clone().multiply(-FIRE_CHARGE_KNOCKBACK_HORIZONTAL);

		double y = normalized.getY();
		if (y < 0) y += 1.5;
		y = y <= 0.5 ? FIRE_CHARGE_KNOCKBACK_VERTICAL * 1.5 : y * FIRE_CHARGE_KNOCKBACK_VERTICAL * 1.5;

		player.setVelocity(horizontal.setY(y));
	}

	/**
	 * The shop's Speed/Jump/Invisibility potions are splashable (thrown, not drunk), and vanilla
	 * splash potions apply to every living entity caught in the burst - including the shopkeeper
	 * Villager standing right next to their own shop. A splashed Invisibility Potion was making the
	 * shopkeeper turn invisible. Zeroing out just the Villager's intensity leaves the effect on any
	 * actual players caught in the same splash untouched.
	 */
	@EventHandler
	public void onPotionSplashShopkeeper(PotionSplashEvent event) {
		for (LivingEntity affected : event.getAffectedEntities()) {
			if (affected instanceof Villager && fwGameManager.getInstanceForWorld(affected.getWorld()) != null) {
				event.setIntensity(affected, 0.0);
			}
		}
	}

	/**
	 * Lets the Fire Charge's explosion through (GameManager's global hooks skip anything tagged
	 * fw_fire_charge) for blocks a player actually placed during the match - same rule onBlockBreak
	 * enforces for pickaxes, so a fireball can drop someone's bridge but can't carve up the map's
	 * own terrain - and additionally exempts Hypixel's fireball-proof materials even if a player
	 * placed them, since buying End Stone/Blast-Proof Glass/Obsidian over Wool is supposed to mean
	 * something. Entity damage/knockback is handled separately in applyFireChargeImpact - this is
	 * block destruction only.
	 */
	@EventHandler
	public void onFireChargeExplode(EntityExplodeEvent event) {
		if (!event.getEntity().hasMetadata(FIRE_CHARGE_METADATA)) return;

		FWGameInstance instance = fwGameManager.getInstanceForWorld(event.getEntity().getWorld());
		if (instance == null) return;

		Iterator<Block> blocks = event.blockList().iterator();
		while (blocks.hasNext()) {
			Block block = blocks.next();
			if (!instance.isPlacedByPlayer(block) || FIRE_CHARGE_BLAST_PROOF.contains(block.getType())) {
				blocks.remove();
				continue;
			}
			instance.recordBlockChange(block.getState());
			instance.unmarkPlaced(block);
		}
	}

	private static final String TNT_METADATA = "fw_tnt";
	/** UUID of whoever placed/threw this TNT - see resolveKiller, which uses this to credit a TNT kill since TNTPrimed#getSource() only ever reflects a flint-and-steel ignition. */
	private static final String TNT_OWNER_METADATA = "fw_tnt_owner";

	private static final double TNT_KNOCKBACK_RADIUS     = 4.0;
	private static final double TNT_KNOCKBACK_HORIZONTAL = 1.5;
	private static final double TNT_KNOCKBACK_VERTICAL   = 0.9;
	private static final double TNT_DAMAGE               = 2.0; // 1 heart

	/**
	 * Lets shop/thrown TNT's explosion through (GameManager's global hooks blanket-clear every
	 * TNTPrimed's blockList otherwise - see the "fw_tnt" exemption added there) for blocks a
	 * player actually placed during the match, same rule onBlockBreak enforces for pickaxes - a
	 * TNT can drop someone's bridge but can't carve up the map's own terrain.
	 * Also applies Hypixel-style flat damage and strong knockback to nearby players (vanilla
	 * TNT entity damage is cancelled in onEntityDamageByEntity to avoid a double hit).
	 */
	@EventHandler
	public void onTntExplode(EntityExplodeEvent event) {
		if (!event.getEntity().hasMetadata(TNT_METADATA)) return;

		FWGameInstance instance = fwGameManager.getInstanceForWorld(event.getEntity().getWorld());
		if (instance == null) return;

		Iterator<Block> blocks = event.blockList().iterator();
		while (blocks.hasNext()) {
			Block block = blocks.next();
			if (!instance.isPlacedByPlayer(block) || FIRE_CHARGE_BLAST_PROOF.contains(block.getType())) {
				blocks.remove();
				continue;
			}
			instance.recordBlockChange(block.getState());
			instance.unmarkPlaced(block);
		}

		applyTntImpact(event.getEntity(), instance);
	}

	private void applyTntImpact(Entity tntEntity, FWGameInstance instance) {
		UUID ownerUuid = null;
		if (tntEntity.hasMetadata(TNT_OWNER_METADATA)) {
			Object value = tntEntity.getMetadata(TNT_OWNER_METADATA).get(0).value();
			if (value instanceof UUID) ownerUuid = (UUID) value;
		}
		Player source = ownerUuid != null ? Bukkit.getPlayer(ownerUuid) : null;
		FWTeam sourceTeam = source != null ? instance.findTeamOf(source) : null;

		Location impact = tntEntity.getLocation();
		for (Entity nearby : impact.getWorld().getNearbyEntities(impact,
				TNT_KNOCKBACK_RADIUS, TNT_KNOCKBACK_RADIUS, TNT_KNOCKBACK_RADIUS)) {
			if (!(nearby instanceof Player)) continue;
			Player player = (Player) nearby;
			if (fwGameManager.getInstanceOfPlayer(player) != instance) continue;

			knockBackFromTnt(player, impact);

			// No friendly fire - same rule as all other damage sources in the mode.
			if (source != null && source != player && sourceTeam != null
					&& sourceTeam == instance.findTeamOf(player)) continue;

			if (TNT_DAMAGE >= player.getHealth() - 0.2) {
				instance.killPlayer(player, source != player ? source : null);
			} else {
				player.damage(TNT_DAMAGE);
				player.setLastDamageCause(new EntityDamageByEntityEvent(
						tntEntity, player, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, TNT_DAMAGE));
				if (source != null && source != player) {
					recentAttackers.put(player.getUniqueId(), new RecentAttack(source.getUniqueId(), System.currentTimeMillis()));
				}
			}
		}
	}

	private void knockBackFromTnt(Player player, Location impact) {
		Vector diff = impact.toVector().subtract(player.getLocation().toVector());
		if (diff.lengthSquared() < 1.0E-6) diff = new Vector(0, -0.01, 0);

		Vector normalized = diff.normalize();
		Vector horizontal = normalized.clone().multiply(-TNT_KNOCKBACK_HORIZONTAL);

		double y = normalized.getY();
		if (y < 0) y += 1.5;
		y = y <= 0.5 ? TNT_KNOCKBACK_VERTICAL * 1.5 : y * TNT_KNOCKBACK_VERTICAL * 1.5;

		player.setVelocity(horizontal.setY(y));
	}

	@EventHandler
	public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
		if (sameBlock(event.getFrom(), event.getTo())) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null || instance.getState() != FWGameState.IN_PROGRESS) return;

		// Respawning players are GameMode.SPECTATOR - skip void/capture checks
		// for them so a mid-respawn ghost can't re-trigger either of them.
		if (player.getGameMode() != GameMode.SURVIVAL) return;

		if (player.isOnGround()) grapplingNoFallDamage.remove(player.getUniqueId());

		if (player.getLocation().getY() <= 0) {
			instance.killPlayerInVoid(player, recentAttackerOf(player));
			return;
		}

		if (instance.getFlagManager().isCarrying(player)) {
			checkForCapture(instance, player);
		}

		if (!fwGameManager.isSpawnProtected(player)) {
			instance.getTrapManager().checkEntry(player);
		}
	}

	/**
	 * A carrier banks the capture once they're back home. If staff have set up that island's
	 * boundary (see /fwsetup boundary1/boundary2), anywhere inside it counts; otherwise this
	 * falls back to the old small radius around the flag's own spot.
	 */
	private void checkForCapture(FWGameInstance instance, Player carrier) {
		FWTeam ownTeam = instance.findTeamOf(carrier);
		if (ownTeam == null) return;

		FWTeamSite site = instance.getMapConfig().getTeamSite(ownTeam.getSiteId());
		Location carrierLoc = carrier.getLocation();

		boolean home;
		if (site.hasBoundary()) {
			// Y is intentionally excluded: a carrier jumping or in the air above their
			// base's footprint should capture just like one standing on the ground.
			Location c1 = site.getBoundaryCorner1();
			Location c2 = site.getBoundaryCorner2();
			double minX = Math.min(c1.getX(), c2.getX()), maxX = Math.max(c1.getX(), c2.getX());
			double minZ = Math.min(c1.getZ(), c2.getZ()), maxZ = Math.max(c1.getZ(), c2.getZ());
			home = carrierLoc.getWorld().equals(c1.getWorld())
					&& carrierLoc.getX() >= minX && carrierLoc.getX() <= maxX
					&& carrierLoc.getZ() >= minZ && carrierLoc.getZ() <= maxZ;
		} else {
			Location flagLoc = site.getFlagLoc();
			home = flagLoc != null
					&& carrierLoc.distanceSquared(flagLoc) <= FWGameConstants.FLAG_CAPTURE_RADIUS * FWGameConstants.FLAG_CAPTURE_RADIUS;
		}

		if (home) {
			instance.getFlagManager().captureFlag(carrier);
		}
	}

	private boolean sameBlock(org.bukkit.Location a, org.bukkit.Location b) {
		return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String cmd = event.getMessage().split(" ")[0].toLowerCase();
		if (!cmd.equals("/fly")) return;

		Player player = event.getPlayer();
		if (fwGameManager.getInstanceOfPlayer(player) == null) return;

		event.setCancelled(true);
		player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rYou cannot use this in a game!"));
	}

	@EventHandler
	public void onDropResourceAirborne(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (player.isOnGround() || !hasVoidBelow(player)) return;
		if (fwGameManager.getInstanceOfPlayer(player) == null) return;
		if (isResource(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
		}
	}

	/**
	 * Dropping the starter Wood Sword is blocked outright - a player should never be able to end up
	 * with no sword at all. Dropping any real (Stone/Iron/Diamond) sword tier is allowed, but the
	 * now-empty slot is immediately refilled with a fresh Wood Sword carrying the team's current
	 * Sharpened Swords enchant (applyUpgrades - the same call giveStarterKit uses), so a dropped
	 * sword never leaves the owner defenseless, just downgraded back to the base tier.
	 *
	 * Only refills if the player has no sword left at all: picking the dropped tier back up and
	 * dropping it again would otherwise stack up extra Wood Swords, one per drop, since each drop
	 * blindly handed out a new one regardless of the Wood Sword already sitting from a prior drop.
	 */
	@EventHandler
	public void onDropSword(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;

		ItemStack dropped = event.getItemDrop().getItemStack();
		if (!dropped.getType().name().endsWith("_SWORD")) return;

		if (dropped.getType() == Material.WOOD_SWORD) {
			event.setCancelled(true);
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rYou can't drop your default sword!"));
			return;
		}

		FWTeam team = instance.findTeamOf(player);
		if (team == null) return;

		if (hasAnySword(player)) return;

		ItemStack replacement = instance.getTeamUpgradeManager().applyUpgrades(team, new ItemStack(Material.WOOD_SWORD));
		player.getInventory().setItem(player.getInventory().getHeldItemSlot(), replacement);
	}

	/** True if the player's inventory already holds a sword of any tier - used to avoid handing out a second Wood Sword on drop. */
	private boolean hasAnySword(Player player) {
		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null && item.getType().name().endsWith("_SWORD")) return true;
		}
		return false;
	}

	/** Wood → Stone → Iron → Diamond, same order the shop enforces (FWShopGUI#SWORD_TIER_ORDER). */
	private int swordTier(Material type) {
		switch (type) {
			case WOOD_SWORD: return 0;
			case STONE_SWORD: return 1;
			case IRON_SWORD: return 2;
			case DIAMOND_SWORD: return 3;
			default: return -1;
		}
	}

	/**
	 * Walking over a dropped sword only picks it up if it's an actual upgrade over whatever the
	 * player's already carrying - same "no downgrades" rule the shop enforces on purchases. A
	 * lower-or-equal tier sword is left alone on the ground instead of cluttering the inventory
	 * with a second sword.
	 *
	 * When the outgoing sword isn't the untracked starter Wood Sword, it's kept - added back to
	 * the inventory as a spare - rather than discarded, so a real tier a player already paid for
	 * never just vanishes because they picked up something better.
	 */
	@EventHandler
	public void onPickupSword(PlayerPickupItemEvent event) {
		Item itemEntity = event.getItem();
		ItemStack picked = itemEntity.getItemStack();
		if (!picked.getType().name().endsWith("_SWORD")) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;

		ItemStack[] contents = player.getInventory().getContents();
		int currentSlot = -1;
		ItemStack currentSword = null;
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && item.getType().name().endsWith("_SWORD")) {
				currentSlot = i;
				currentSword = item;
				break;
			}
		}

		int currentTier = currentSword != null ? swordTier(currentSword.getType()) : -1;
		if (swordTier(picked.getType()) <= currentTier) {
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);
		itemEntity.remove();

		FWTeam team = instance.findTeamOf(player);
		ItemStack upgraded = team != null
				? instance.getTeamUpgradeManager().applyUpgrades(team, picked.clone())
				: picked.clone();

		if (currentSlot != -1) {
			player.getInventory().setItem(currentSlot, upgraded);
		} else {
			player.getInventory().addItem(upgraded);
		}

		if (currentSword != null && currentSword.getType() != Material.WOOD_SWORD) {
			player.getInventory().addItem(currentSword);
		}
	}

	/** Wood → Stone → Iron → Diamond, same ladder FWGameInstance's pickaxeForTier/pickaxeTierOf use. */
	private int pickaxeTier(Material type) {
		switch (type) {
			case WOOD_PICKAXE: return 0;
			case STONE_PICKAXE: return 1;
			case IRON_PICKAXE: return 2;
			case DIAMOND_PICKAXE: return 3;
			default: return -1;
		}
	}

	/** Wood → Stone → Iron → Diamond, same ladder FWGameInstance's axeForTier/axeTierOf use. */
	private int axeTier(Material type) {
		switch (type) {
			case WOOD_AXE: return 0;
			case STONE_AXE: return 1;
			case IRON_AXE: return 2;
			case DIAMOND_AXE: return 3;
			default: return -1;
		}
	}

	/** Same "no downgrades, keep the outgoing real tier as a spare" rule as onPickupSword, applied to pickaxes. */
	@EventHandler
	public void onPickupPickaxe(PlayerPickupItemEvent event) {
		Item itemEntity = event.getItem();
		ItemStack picked = itemEntity.getItemStack();
		if (!picked.getType().name().endsWith("_PICKAXE")) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;

		ItemStack[] contents = player.getInventory().getContents();
		int currentSlot = -1;
		ItemStack currentPickaxe = null;
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && item.getType().name().endsWith("_PICKAXE")) {
				currentSlot = i;
				currentPickaxe = item;
				break;
			}
		}

		int currentTier = currentPickaxe != null ? pickaxeTier(currentPickaxe.getType()) : -1;
		if (pickaxeTier(picked.getType()) <= currentTier) {
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);
		itemEntity.remove();

		FWTeam team = instance.findTeamOf(player);
		ItemStack upgraded = team != null
				? instance.getTeamUpgradeManager().applyUpgrades(team, picked.clone())
				: picked.clone();

		if (currentSlot != -1) {
			player.getInventory().setItem(currentSlot, upgraded);
		} else {
			player.getInventory().addItem(upgraded);
		}

		if (currentPickaxe != null && currentPickaxe.getType() != Material.WOOD_PICKAXE) {
			player.getInventory().addItem(currentPickaxe);
		}
	}

	/** Same "no downgrades, keep the outgoing real tier as a spare" rule as onPickupSword, applied to axes. */
	@EventHandler
	public void onPickupAxe(PlayerPickupItemEvent event) {
		Item itemEntity = event.getItem();
		ItemStack picked = itemEntity.getItemStack();
		if (!picked.getType().name().endsWith("_AXE")) return;

		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;

		ItemStack[] contents = player.getInventory().getContents();
		int currentSlot = -1;
		ItemStack currentAxe = null;
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && item.getType().name().endsWith("_AXE")) {
				currentSlot = i;
				currentAxe = item;
				break;
			}
		}

		int currentTier = currentAxe != null ? axeTier(currentAxe.getType()) : -1;
		if (axeTier(picked.getType()) <= currentTier) {
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);
		itemEntity.remove();

		FWTeam team = instance.findTeamOf(player);
		ItemStack upgraded = team != null
				? instance.getTeamUpgradeManager().applyUpgrades(team, picked.clone())
				: picked.clone();

		if (currentSlot != -1) {
			player.getInventory().setItem(currentSlot, upgraded);
		} else {
			player.getInventory().addItem(upgraded);
		}

		if (currentAxe != null && currentAxe.getType() != Material.WOOD_AXE) {
			player.getInventory().addItem(currentAxe);
		}
	}

	/**
	 * Closes the same "swords/tools can't be stored" rule around a chest's own inventory GUI, not
	 * just the punch-to-deposit shortcut (see onPunchChestDeposit/isSwordOrTool) - covers direct
	 * placement, shift-clicking one up from the player's own inventory, and hotbar-swapping one in.
	 * Dragging isn't covered - swords/tools aren't stackable, so there's nothing a drag achieves
	 * here that a plain click doesn't already cover.
	 */
	@EventHandler
	public void onChestStoreClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		InventoryType type = event.getInventory().getType();
		if (type != InventoryType.CHEST && type != InventoryType.ENDER_CHEST) return;

		Player player = (Player) event.getWhoClicked();
		if (fwGameManager.getInstanceOfPlayer(player) == null) return;

		ItemStack movingIntoChest = null;
		if (event.getClickedInventory() == event.getInventory()) {
			movingIntoChest = event.getCursor();
		} else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
			movingIntoChest = event.getCurrentItem();
		} else if (event.getClick() == ClickType.NUMBER_KEY) {
			movingIntoChest = player.getInventory().getItem(event.getHotbarButton());
		}

		if (movingIntoChest != null && movingIntoChest.getType() != Material.AIR && isSwordOrTool(movingIntoChest.getType())) {
			event.setCancelled(true);
			player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rYou can't store swords or tools in a chest!"));
		}
	}

	@EventHandler
	public void onClickResourceAirborne(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		Player player = (Player) event.getWhoClicked();
		if (player.isOnGround() || !hasVoidBelow(player)) return;
		if (fwGameManager.getInstanceOfPlayer(player) == null) return;
		if (isResource(event.getCurrentItem()) || isResource(event.getCursor())) {
			event.setCancelled(true);
		}
	}

	/** Clears both Bukkit's own lastDamageCause and the internal recent-attacker entry for this player, called on respawn so a previous kill can't be credited twice. */
	public void clearAttackerHistory(Player player) {
		player.setLastDamageCause(null);
		recentAttackers.remove(player.getUniqueId());
	}

	/** Returns true if there are no solid blocks between the player's feet and Y=0 — i.e. they are over open void. */
	private boolean hasVoidBelow(Player player) {
		Location loc = player.getLocation();
		int x = loc.getBlockX();
		int z = loc.getBlockZ();
		for (int y = loc.getBlockY() - 1; y >= 0; y--) {
			if (loc.getWorld().getBlockAt(x, y, z).getType().isSolid()) return false;
		}
		return true;
	}

	private boolean isResource(ItemStack item) {
		if (item == null) return false;
		Material m = item.getType();
		return m == Material.IRON_INGOT || m == Material.GOLD_INGOT
				|| m == Material.EMERALD || m == Material.DIAMOND;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance != null) {
			instance.removePlayer(player);
			return;
		}

		FWGameInstance spectating = fwGameManager.getInstanceOfSpectator(player);
		if (spectating != null) {
			spectating.removeSpectator(player);
		}
	}

	@EventHandler
	public void onEnderPearlTeleport(PlayerTeleportEvent event) {
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
		Player player = event.getPlayer();
		if (fwGameManager.getInstanceOfPlayer(player) == null
				&& fwGameManager.getInstanceOfSpectator(player) == null) return;
		noPearlDamage.add(player.getUniqueId());
		Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(), () ->
				noPearlDamage.remove(player.getUniqueId()), 2L);
	}
}
