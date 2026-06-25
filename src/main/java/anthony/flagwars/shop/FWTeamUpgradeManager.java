package anthony.flagwars.shop;

import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWTeam;
import anthony.flagwars.map.FWTeamSite;
import anthony.flagwars.resources.FWEconomy;
import anthony.flagwars.resources.FWResourceType;
import anthony.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hypixel Bedwars-style team upgrades bought from the shop's Upgrades tab:
 * Forge speeds up that team's generators, Sharpened Swords / Reinforced
 * Armor permanently enchant swords/armor the team is given, Heal Pool gives
 * passive regen near the team's own flag, and Team Level is the old
 * tiered villager-upgrade track (now bought here too, since there's no
 * entity left to right-click) that gates shop items and defense mechanics.
 */
public class FWTeamUpgradeManager {

	private static final double HEAL_POOL_RADIUS = 8.0;
	private static final long HEAL_POOL_INTERVAL_TICKS = 4L * 20L;

	private static final Set<Material> SWORDS = EnumSet.of(
			Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
			Material.GOLD_SWORD, Material.DIAMOND_SWORD);
	private static final Set<Material> ARMOR = EnumSet.of(
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
			Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
			Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
			Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS,
			Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);
	/** Just the boots, out of ARMOR above - Feather Falling only ever touches this slot. */
	private static final Set<Material> BOOTS = EnumSet.of(
			Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLD_BOOTS, Material.DIAMOND_BOOTS);
	/** Fixed at Feather Falling II once bought - it's a one-shot Upgrades tab purchase, not a tiered enchant like Sharpened Swords/Reinforced Armor. */
	private static final int FEATHER_FALLING_LEVEL = 2;
	/** Every weapon/tool material a player can be given - these never lose durability (Fishing Rod covers the Grappling Hook, Bow covers the Teleport Bow). */
	private static final Set<Material> UNBREAKABLE_TYPES = EnumSet.of(
			Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD,
			Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE,
			Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE,
			Material.SHEARS, Material.BOW, Material.FISHING_ROD);

	private final FWGameInstance instance;
	private BukkitTask healPoolTask;

	public FWTeamUpgradeManager(FWGameInstance instance) {
		this.instance = instance;
	}

	public boolean purchase(FWTeam team, FWTeamUpgrade upgrade, Player buyer) {
		int next = team.getUpgradeTier(upgrade) + 1;
		if (next > upgrade.getMaxTier()) return false;

		Map<FWResourceType, Integer> cost = upgrade.costForTier(next);
		if (cost == null || !FWEconomy.withdraw(buyer, cost)) return false;

		team.setUpgradeTier(upgrade, next);
		instance.TellAll(instance.getManager().getMain().color("&6&l(!) &eTeam " + team.getName()
				+ "&r upgraded &e" + upgrade.getDisplayName() + " &rto tier &e" + next + "&r!"));
		reapplyToCurrentGear(team);
		return true;
	}

	/**
	 * Sharpened Swords / Reinforced Armor used to only take effect on gear handed out after the
	 * purchase (next shop buy, next respawn) - this re-enchants whatever the team is already
	 * holding/wearing right now too, so the upgrade is felt immediately.
	 */
	private void reapplyToCurrentGear(FWTeam team) {
		for (UUID uuid : team.getMembers()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) continue;

			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null) applyUpgrades(team, item);
			}
			for (ItemStack item : player.getInventory().getArmorContents()) {
				if (item != null) applyUpgrades(team, item);
			}
			player.updateInventory();
		}
	}

	/** Applies the team's currently-given enchant tiers (sword/armor) to an item about to be handed to a player. */
	public ItemStack applyUpgrades(FWTeam team, ItemStack item) {
		if (SWORDS.contains(item.getType())) {
			int tier = team.getUpgradeTier(FWTeamUpgrade.SHARPENED_SWORDS);
			if (tier > 0) item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, tier);
		} else if (ARMOR.contains(item.getType())) {
			int tier = team.getUpgradeTier(FWTeamUpgrade.REINFORCED_ARMOR);
			if (tier > 0) item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, tier);

			if (BOOTS.contains(item.getType()) && team.getUpgradeTier(FWTeamUpgrade.FEATHER_FALLING) > 0) {
				item.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, FEATHER_FALLING_LEVEL);
			}
		}

		if (UNBREAKABLE_TYPES.contains(item.getType())) {
			ItemHelper.setUnbreakable(item);
		}

		return item;
	}

	/** Iron amount per drop for the team's current Forge tier. */
	public int ironAmountFor(FWTeam team) {
		switch (team.getUpgradeTier(FWTeamUpgrade.FORGE)) {
			case 4: return 3;
			case 2:
			case 3: return 2;
			default: return 1;
		}
	}

	/** Gold generator interval (seconds) for the team's current Forge tier - lower is faster. */
	public int goldIntervalSecondsFor(FWTeam team) {
		switch (team.getUpgradeTier(FWTeamUpgrade.FORGE)) {
			case 4: return 1;
			case 3: return 2;
			case 2: return 3;
			default: return 4;
		}
	}

	public void start() {
		if (healPoolTask != null) return;
		healPoolTask = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), this::healPoolTick,
				HEAL_POOL_INTERVAL_TICKS, HEAL_POOL_INTERVAL_TICKS);
	}

	public void stop() {
		if (healPoolTask != null) {
			healPoolTask.cancel();
			healPoolTask = null;
		}
	}

	private void healPoolTick() {
		for (FWTeam team : instance.getTeams().values()) {
			if (team.getUpgradeTier(FWTeamUpgrade.HEAL_POOL) <= 0) continue;

			FWTeamSite site = instance.getMapConfig().getTeamSite(team.getSiteId());
			Location flagLoc = site.getFlagLoc();
			if (flagLoc == null) continue;

			for (UUID uuid : team.getMembers()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) continue;
				if (player.getLocation().distanceSquared(flagLoc) > HEAL_POOL_RADIUS * HEAL_POOL_RADIUS) continue;

				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
						(int) HEAL_POOL_INTERVAL_TICKS + 20, 0, false, false));
			}
		}
	}
}
