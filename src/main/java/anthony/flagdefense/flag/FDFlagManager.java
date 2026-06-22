package anthony.flagdefense.flag;

import anthony.SuperCraftBrawl.Core;
import anthony.flagdefense.FDGameInstance;
import anthony.flagdefense.FDTeam;
import anthony.flagdefense.map.FDMapConfig;
import anthony.flagdefense.map.FDTeamSite;
import anthony.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spawns and tracks each team's flag (a colored banner block at their base)
 * and shopkeeper Villager, and handles stealing/carrying/capturing/returning
 * flags. The shopkeeper is pinned in place every tick - no wandering, no
 * knockback; the flag is a static block so it needs no such pinning.
 *
 * A flag is never actually broken or mined: stealing is triggered by a plain
 * left-click (see FDPlayerListener#onLeftClickFlag), which routes straight
 * into attemptSteal and cancels the interact event - the block never takes
 * any damage, avoiding the known glitchiness of breaking/re-rendering 1.8
 * banner blocks on newer client versions, and matching "replace it with a
 * new one" immediately since it was never actually removed in the first place.
 */
public class FDFlagManager {

	private final FDGameInstance instance;
	private final Map<Integer, Location> flagLocs = new HashMap<>();
	private final Map<Integer, Villager> shopkeepers = new HashMap<>();
	private final Map<Entity, Location> pinned = new LinkedHashMap<>();
	private BukkitTask pinTask;

	/** carrierUUID -> the site id of the flag they're currently carrying. */
	private final Map<UUID, Integer> carrying = new HashMap<>();
	/** carrierUUID -> their real helmet, saved while a flag banner occupies that slot. */
	private final Map<UUID, ItemStack> savedHelmets = new HashMap<>();

	public FDFlagManager(FDGameInstance instance) {
		this.instance = instance;
	}

	public void spawnAll() {
		World world = instance.getWorld();
		FDMapConfig config = instance.getManager().getMapConfigManager().getConfig();

		for (FDTeam team : instance.getTeams().values()) {
			if (team.isEmpty()) continue;

			FDTeamSite site = config.getTeamSite(team.getSiteId());

			placeFlagBlock(site.getFlagLoc(), team);
			flagLocs.put(team.getSiteId(), site.getFlagLoc().clone());

			Villager shopkeeper = (Villager) world.spawnEntity(site.getShopLoc(), EntityType.VILLAGER);
			shopkeeper.setCustomName(instance.getManager().getMain().color("&aShopkeeper"));
			shopkeeper.setCustomNameVisible(true);
			shopkeeper.setRemoveWhenFarAway(false);
			shopkeepers.put(team.getSiteId(), shopkeeper);
			pinned.put(shopkeeper, site.getShopLoc().clone());
		}

		startPinning();
	}

	private void placeFlagBlock(Location loc, FDTeam team) {
		Block block = loc.getBlock();
		block.setType(Material.STANDING_BANNER);

		BlockState state = block.getState();
		if (state instanceof Banner) {
			Banner banner = (Banner) state;
			banner.setBaseColor(team.getColor());
			banner.update(true);
		}
	}

	/** Forces every tracked shopkeeper back to its spawn spot and zeroes velocity, so it never wanders or shows knockback. */
	private void startPinning() {
		if (pinTask != null) return;

		pinTask = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), () -> {
			for (Map.Entry<Entity, Location> entry : pinned.entrySet()) {
				Entity entity = entry.getKey();
				if (entity.isDead()) continue;

				Location pin = entry.getValue();
				Location current = entity.getLocation();
				if (current.getX() != pin.getX() || current.getY() != pin.getY() || current.getZ() != pin.getZ()) {
					entity.teleport(pin);
				}
				entity.setVelocity(new Vector(0, 0, 0));
			}
		}, 0L, 1L);
	}

	public void despawnAll() {
		if (pinTask != null) {
			pinTask.cancel();
			pinTask = null;
		}
		pinned.clear();

		for (Location loc : flagLocs.values()) {
			loc.getBlock().setType(Material.AIR);
		}
		flagLocs.clear();

		for (Villager shopkeeper : shopkeepers.values()) {
			shopkeeper.remove();
		}
		shopkeepers.clear();

		// In case the match ended mid-carry, give back any real helmets still held hostage.
		for (Map.Entry<UUID, ItemStack> entry : savedHelmets.entrySet()) {
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null) player.getInventory().setHelmet(entry.getValue());
		}
		carrying.clear();
		savedHelmets.clear();
	}

	public Integer findSiteByShopkeeper(Entity entity) {
		for (Map.Entry<Integer, Villager> entry : shopkeepers.entrySet()) {
			if (entry.getValue().equals(entity)) return entry.getKey();
		}
		return null;
	}

	/**
	 * Returns the site id of whichever tracked flag the player is aiming at within
	 * reach, or null if none. Standing banners render a tall, thin pole+cloth model
	 * that visually pokes above their actual block's hitbox, so vanilla's own click
	 * targeting (Bukkit's getClickedBlock()) reliably reports a hit on the lower
	 * portion but often misses (or hits something else entirely) when a player aims
	 * at the upper portion - that ray just passes through the empty air above the
	 * block instead of hitting anything. Checking the perpendicular distance from
	 * the player's look-ray to each flag's block center, instead of relying on the
	 * client/server's own hitbox-based targeting, sidesteps that mismatch entirely
	 * and makes the whole visual flag clickable top to bottom.
	 */
	public Integer siteIdLookingAt(Player player, double reach, double tolerance) {
		Location eye = player.getEyeLocation();
		Vector origin = eye.toVector();
		Vector direction = eye.getDirection().normalize();

		Integer closestSite = null;
		double closestDistance = tolerance;

		for (Map.Entry<Integer, Location> entry : flagLocs.entrySet()) {
			Location flagLoc = entry.getValue();
			if (!flagLoc.getWorld().equals(eye.getWorld())) continue;

			Vector toFlag = flagLoc.clone().add(0.5, 0.5, 0.5).toVector().subtract(origin);
			double t = toFlag.dot(direction);
			if (t < 0 || t > reach) continue;

			double distance = toFlag.subtract(direction.clone().multiply(t)).length();
			if (distance <= closestDistance) {
				closestDistance = distance;
				closestSite = entry.getKey();
			}
		}

		return closestSite;
	}

	public boolean isCarrying(Player player) {
		return carrying.containsKey(player.getUniqueId());
	}

	/**
	 * Called when a player left-clicks a flag block. Blocks self-steals, carrying
	 * two flags at once (only one helmet slot to hold one in), and re-capturing
	 * the same victim a second time - everyone else stays free to steal it.
	 */
	public void attemptSteal(int ownerSiteId, Player attacker) {
		Core main = instance.getManager().getMain();
		FDTeam ownerTeam = instance.getTeams().get(ownerSiteId);
		FDTeam attackerTeam = instance.findTeamOf(attacker);
		if (ownerTeam == null || attackerTeam == null) return;

		if (ownerTeam == attackerTeam) {
			attacker.sendMessage(main.color("&c&l(!) &rYou can't steal your own flag!"));
			return;
		}

		if (carrying.containsKey(attacker.getUniqueId())) {
			attacker.sendMessage(main.color("&c&l(!) &rYou're already carrying a flag! Capture or lose it first."));
			return;
		}

		if (attackerTeam.hasCapturedFrom(ownerSiteId)) {
			attacker.sendMessage(main.color("&c&l(!) &rYou've already captured this flag! Steal from someone else."));
			return;
		}

		carrying.put(attacker.getUniqueId(), ownerSiteId);
		savedHelmets.put(attacker.getUniqueId(), attacker.getInventory().getHelmet());
		attacker.getInventory().setHelmet(flagBannerItem(ownerTeam));

		instance.TellAll(main.color("&c&l(!) &e" + attacker.getName() + " &rstole &e" + ownerTeam.getName() + "&r's flag!"));
		instance.playSoundForAll(Sound.ENDERDRAGON_GROWL, 0.5f, 1.6f);
		instance.refreshScoreboards();
	}

	/** Called from FDPlayerListener#onPlayerMove once a carrier walks back into their own base's capture radius. */
	public void captureFlag(Player carrier) {
		Integer ownerSiteId = carrying.remove(carrier.getUniqueId());
		if (ownerSiteId == null) return;

		restoreHelmet(carrier);

		FDTeam carrierTeam = instance.findTeamOf(carrier);
		if (carrierTeam == null) return;
		FDTeam ownerTeam = instance.getTeams().get(ownerSiteId);

		carrierTeam.markCapturedFrom(ownerSiteId);
		carrierTeam.addCapturedFlag();

		int required = instance.getRequiredCaptures();
		Core main = instance.getManager().getMain();
		instance.TellAll(main.color("&6&l(!) &e" + carrier.getName() + " &rcaptured &e"
				+ (ownerTeam != null ? ownerTeam.getName() : "a") + "&r's flag! (&e" + carrierTeam.getCapturedFlags()
				+ "&7/&e" + required + "&r)"));
		instance.playSoundForAll(Sound.LEVEL_UP, 1f, 1f);
		carrier.sendTitle(main.color("&6&lFLAG CAPTURED"),
				main.color("&e" + carrierTeam.getCapturedFlags() + "&7/&e" + required));

		instance.refreshScoreboards();
		instance.onFlagCaptured(carrierTeam);
	}

	/** Called when a carrier dies (by their flag's owner, by a third party, or the void) or leaves the game before capturing. */
	public void returnCarriedFlag(Player carrier, Player killer) {
		Integer ownerSiteId = carrying.remove(carrier.getUniqueId());
		if (ownerSiteId == null) return;

		restoreHelmet(carrier);

		FDTeam ownerTeam = instance.getTeams().get(ownerSiteId);
		Core main = instance.getManager().getMain();

		if (ownerTeam != null && killer != null && ownerTeam.isMember(killer)) {
			instance.TellAll(main.color("&a&l(!) &e" + killer.getName() + " &rrecovered their flag from &e"
					+ carrier.getName() + "&r!"));
			instance.playSoundForAll(Sound.ANVIL_LAND, 0.7f, 1f);
		} else {
			instance.TellAll(main.color("&e&l(!) &r" + carrier.getName() + "'s stolen flag returned to &e"
					+ (ownerTeam != null ? ownerTeam.getName() : "its owner") + "&r's base."));
		}

		instance.refreshScoreboards();
	}

	private void restoreHelmet(Player player) {
		ItemStack saved = savedHelmets.remove(player.getUniqueId());
		if (player.isOnline()) player.getInventory().setHelmet(saved);
	}

	private ItemStack flagBannerItem(FDTeam ownerTeam) {
		ItemStack item = new ItemStack(Material.BANNER, 1);
		BannerMeta meta = (BannerMeta) item.getItemMeta();
		meta.setBaseColor(ownerTeam.getColor());
		item.setItemMeta(meta);

		Core main = instance.getManager().getMain();
		return ItemHelper.setDetails(item, main.color("&e" + ownerTeam.getName() + "'s Flag"),
				Collections.singletonList(main.color("&7Return it to your base to capture it!")));
	}
}
