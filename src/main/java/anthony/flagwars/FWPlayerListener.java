package anthony.flagwars;

import anthony.SuperCraftBrawl.Game.classes.CooldownNatowski;
import anthony.SuperCraftBrawl.gui.StatsGUI;
import anthony.flagwars.items.GrapplingHookItem;
import anthony.flagwars.items.TeleportBowItem;
import anthony.flagwars.items.ThrowableTntItem;
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
import org.bukkit.entity.Fireball;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * All Flag Wars-only event handling. Every handler bails immediately
 * if the player isn't in the active FD instance, exactly mirroring how
 * SuperCraftBros' PlayerListener gates on getGameManager().GetInstanceOfPlayer().
 * Registered as its own Listener so it never interferes with SCB's.
 */
public class FWPlayerListener implements Listener {

	private static final double GRAPPLING_HOOK_COOLDOWN_SECONDS = 15.0;
	private static final double TELEPORT_BOW_COOLDOWN_SECONDS = 15.0;

	private final FWGameManager fwGameManager;
	private final Map<UUID, CooldownNatowski> grapplingHookCooldowns = new HashMap<>();
	private final Map<UUID, CooldownNatowski> teleportBowCooldowns = new HashMap<>();
	private final List<CooldownItem> cooldownItems;

	public FWPlayerListener(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
		this.cooldownItems = Arrays.asList(
				new CooldownItem(GrapplingHookItem.NAME, GrapplingHookItem::matches, grapplingHookCooldowns),
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
					"&c&l(!) &rYou can't build that close to a spawn or generator!"));
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
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();

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

			// Entity-caused damage (PvP, mobs) is handled by onEntityDamageByEntity,
			// which shares this same event - only handle the environmental causes
			// (fall, fire, lava, drowning, etc.) here so a kill is never registered twice.
			if (!(event instanceof EntityDamageByEntityEvent) && !event.isCancelled()
					&& event.getFinalDamage() >= player.getHealth() - 0.2) {
				event.setCancelled(true);
				instance.killPlayer(player, null);
			}
			return;
		}

		// Shopkeepers never take real damage.
		FWGameInstance instance = fwGameManager.getInstanceForWorld(entity.getWorld());
		if (instance == null) return;

		if (instance.getFlagManager().findSiteByShopkeeper(entity) != null) {
			event.setCancelled(true);
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

		Player attacker = resolveKiller(event.getDamager());

		// Spectators can't deal damage - same as SCB's own onEntityDamageByEntity spectator check.
		if (attacker != null && fwGameManager.getInstanceOfSpectator(attacker) != null) {
			event.setCancelled(true);
			return;
		}

		// No friendly fire - matters now that Duos puts more than one player on the same team.
		if (attacker != null && attacker != player) {
			FWTeam victimTeam = instance.findTeamOf(player);
			if (victimTeam != null && victimTeam == instance.findTeamOf(attacker)) {
				event.setCancelled(true);
				return;
			}
		}

		applyTeleportBow(event.getDamager(), player.getLocation());

		if (!event.isCancelled() && event.getFinalDamage() >= player.getHealth() - 0.2) {
			event.setCancelled(true);
			instance.killPlayer(player, attacker);
		}
	}

	private Player resolveKiller(Entity damager) {
		if (damager instanceof Player) return (Player) damager;
		if (damager instanceof Projectile) {
			Object shooter = ((Projectile) damager).getShooter();
			if (shooter instanceof Player) return (Player) shooter;
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
	 * timer) caused by another player or their projectile?
	 */
	private Player recentAttackerOf(Player player) {
		EntityDamageEvent lastCause = player.getLastDamageCause();
		if (!(lastCause instanceof EntityDamageByEntityEvent)) return null;

		Player attacker = resolveKiller(((EntityDamageByEntityEvent) lastCause).getDamager());
		return attacker != player ? attacker : null;
	}

	private static final String TELEPORT_BOW_METADATA = "fd_teleport_bow";
	private static final String TELEPORT_BOW_DIRECTION_METADATA = "fd_teleport_bow_direction";

	/**
	 * Shooting a player with the Teleport Bow teleports the shooter to them; shooting the
	 * ground works too, teleporting the shooter to wherever the arrow landed (see
	 * onProjectileHit below, same as a regular ender pearl). Metadata is consumed on first
	 * use so a block-hit and an entity-hit can never both fire for the same arrow.
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

		CooldownNatowski cooldown = teleportBowCooldowns.computeIfAbsent(shooter.getUniqueId(),
				id -> new CooldownNatowski(TELEPORT_BOW_COOLDOWN_SECONDS));

		if (!cooldown.isReady()) {
			shooter.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTeleport Bow is on cooldown for &e"
					+ cooldown.getRemainingCooldownSeconds() + "s&r!"));
			return;
		}
		cooldown.use();

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
		}
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

		CooldownNatowski cooldown = grapplingHookCooldowns.computeIfAbsent(player.getUniqueId(),
				id -> new CooldownNatowski(GRAPPLING_HOOK_COOLDOWN_SECONDS));

		PlayerFishEvent.State state = event.getState();

		// Block the cast itself while on cooldown, rather than letting them throw it for nothing.
		if (state == PlayerFishEvent.State.FISHING) {
			if (!cooldown.isReady()) {
				event.setCancelled(true);
				player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rGrappling Hook is on cooldown for &e"
						+ cooldown.getRemainingCooldownSeconds() + "s&r!"));
			}
			return;
		}

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

		cooldown.use();

		Vector toHook = hook.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
		player.setVelocity(toHook.multiply(2).add(new Vector(0, 0.8, 0)));
		player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 10);
	}

	private void throwTnt(Player player) {
		TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getEyeLocation(), org.bukkit.entity.EntityType.PRIMED_TNT);
		tnt.setVelocity(player.getLocation().getDirection().multiply(1.4));
		tnt.setFuseTicks(60);
		tnt.setMetadata(TNT_METADATA, new FixedMetadataValue(fwGameManager.getMain(), true));

		ItemStack handItem = player.getItemInHand();
		if (handItem.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			handItem.setAmount(handItem.getAmount() - 1);
		}
	}

	private static final String FIRE_CHARGE_METADATA = "fw_fire_charge";

	/**
	 * Hypixel's fireball damage/knockback isn't vanilla explosion physics at all - it's flat,
	 * distance-independent damage plus a hand-tuned velocity formula, ported straight from
	 * BedWars1058's FireballListener (the most widely deployed open-source Hypixel Bedwars clone)
	 * and its documented MainConfig defaults, rather than guessed at. Vanilla's own explosion
	 * falloff is both weaker on knockback and harsher on damage than this.
	 */
	private static final double FIRE_CHARGE_PROXIMITY_RADIUS = 3.0;
	private static final double FIRE_CHARGE_KNOCKBACK_HORIZONTAL = 1.0;
	private static final double FIRE_CHARGE_KNOCKBACK_VERTICAL = 0.65;
	private static final double FIRE_CHARGE_DAMAGE_SELF = 2.0;
	private static final double FIRE_CHARGE_DAMAGE_ENEMY = 2.0;
	private static final double FIRE_CHARGE_DAMAGE_TEAMMATE = 0.0;
	/** BedWars1058's exact "fireball.speed-multiplier" default - launchProjectile() alone gives it next to no speed, which is why it only ever seemed to "work" at point-blank/block range. */
	private static final double FIRE_CHARGE_SPEED_MULTIPLIER = 10.0;

	/**
	 * Hypixel's own fireball-proof set, straight off the Bedwars wiki: wool/clay/wood blow open,
	 * glass/end stone/obsidian don't - which is exactly why FWShopCatalog sells End Stone, Blast-
	 * Proof Glass and Obsidian at a premium over Wool in the first place.
	 */
	private static final Set<Material> FIRE_CHARGE_BLAST_PROOF = EnumSet.of(
			Material.GLASS, Material.THIN_GLASS, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE,
			Material.ENDER_STONE, Material.OBSIDIAN);

	/**
	 * A regular (Ghast-sized) Fireball, not the smaller Blaze one - tagged with metadata so both
	 * the impact/explosion handlers below and GameManager's global anti-grief explosion handlers
	 * (which otherwise blanket-cancel every Fireball/SmallFireball explosion server-wide) know to
	 * treat this one differently instead of just no-op'ing it. Yield is set to the same
	 * FIRE_CHARGE_PROXIMITY_RADIUS BedWars1058 reuses for its own explosion-size config, so block
	 * destruction matches the real radius instead of vanilla's much weaker default (~1.0).
	 *
	 * Deliberately NOT calling setIsIncendiary(false) here - BedWars1058's own source has this
	 * exact line commented out with a note that on versions below 1.12 (which includes our 1.8.8)
	 * it stops the fireball from exploding on hit at all. "No fire" is handled safely below via
	 * ExplosionPrimeEvent instead.
	 */
	private void throwFireCharge(Player player, ItemStack item) {
		Fireball fireball = player.launchProjectile(Fireball.class);

		// launchProjectile() alone leaves it crawling along at a near-zero default speed - close
		// enough to a wall that it's reached almost immediately, easy to mistake for "only works
		// at block range" when thrown into open air with nothing nearby to hit. Hypixel's own
		// fireball is fast and explicit about its direction (BedWars1058's Interact listener does
		// the exact same setDirection + setVelocity pair), so match that here.
		Vector direction = player.getEyeLocation().getDirection();
		fireball.setDirection(direction);
		fireball.setVelocity(direction.multiply(FIRE_CHARGE_SPEED_MULTIPLIER));

		fireball.setMetadata(FIRE_CHARGE_METADATA, new FixedMetadataValue(fwGameManager.getMain(), true));
		fireball.setYield((float) FIRE_CHARGE_PROXIMITY_RADIUS);

		if (item.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			item.setAmount(item.getAmount() - 1);
		}
		player.getWorld().playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1f, 1f);
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

	/**
	 * Lets shop/thrown TNT's explosion through (GameManager's global hooks blanket-clear every
	 * TNTPrimed's blockList otherwise - see the "fw_tnt" exemption added there) for blocks a
	 * player actually placed during the match, same rule onBlockBreak enforces for pickaxes - a
	 * TNT can drop someone's bridge but can't carve up the map's own terrain.
	 */
	@EventHandler
	public void onTntExplode(EntityExplodeEvent event) {
		if (!event.getEntity().hasMetadata(TNT_METADATA)) return;

		FWGameInstance instance = fwGameManager.getInstanceForWorld(event.getEntity().getWorld());
		if (instance == null) return;

		Iterator<Block> blocks = event.blockList().iterator();
		while (blocks.hasNext()) {
			Block block = blocks.next();
			if (!instance.isPlacedByPlayer(block)) {
				blocks.remove();
				continue;
			}
			instance.recordBlockChange(block.getState());
			instance.unmarkPlaced(block);
		}
	}

	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;

		Integer shopSiteId = instance.getFlagManager().findSiteByShopkeeper(event.getRightClicked());
		if (shopSiteId != null) {
			event.setCancelled(true);
			FWTeam playerTeam = instance.findTeamOf(player);
			if (playerTeam != null) {
				new FWShopGUI(instance, playerTeam).open(player);
			}
		}
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

		if (player.getLocation().getY() <= 0) {
			instance.killPlayerInVoid(player, recentAttackerOf(player));
			return;
		}

		if (instance.getFlagManager().isCarrying(player)) {
			checkForCapture(instance, player);
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
			home = site.isWithinBoundary(carrierLoc);
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
}
