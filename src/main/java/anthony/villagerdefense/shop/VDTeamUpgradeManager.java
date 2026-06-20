package anthony.villagerdefense.shop;

import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.resources.VDEconomy;
import anthony.villagerdefense.resources.VDResourceType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
 * Armor permanently enchant swords/armor the team is given, and Heal Pool
 * gives passive regen near the team's own villager.
 */
public class VDTeamUpgradeManager {

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

	private final VDGameInstance instance;
	private BukkitTask healPoolTask;

	public VDTeamUpgradeManager(VDGameInstance instance) {
		this.instance = instance;
	}

	public boolean purchase(VDTeam team, VDTeamUpgrade upgrade, Player buyer) {
		int next = team.getUpgradeTier(upgrade) + 1;
		if (next > upgrade.getMaxTier()) return false;

		Map<VDResourceType, Integer> cost = upgrade.costForTier(next);
		if (cost == null || !VDEconomy.withdraw(buyer, cost)) return false;

		team.setUpgradeTier(upgrade, next);
		instance.TellAll(instance.getManager().getMain().color("&6&l(!) &eTeam " + team.getName()
				+ "&r upgraded &e" + upgrade.getDisplayName() + " &rto tier &e" + next + "&r!"));
		return true;
	}

	/** Applies the team's currently-given enchant tiers (sword/armor) to an item about to be handed to a player. */
	public ItemStack applyUpgrades(VDTeam team, ItemStack item) {
		if (SWORDS.contains(item.getType())) {
			int tier = team.getUpgradeTier(VDTeamUpgrade.SHARPENED_SWORDS);
			if (tier > 0) item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, tier);
		} else if (ARMOR.contains(item.getType())) {
			int tier = team.getUpgradeTier(VDTeamUpgrade.REINFORCED_ARMOR);
			if (tier > 0) item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, tier);
		}
		return item;
	}

	/** Iron amount per drop for the team's current Forge tier. */
	public int ironAmountFor(VDTeam team) {
		switch (team.getUpgradeTier(VDTeamUpgrade.FORGE)) {
			case 4: return 3;
			case 2:
			case 3: return 2;
			default: return 1;
		}
	}

	/** Gold generator interval (seconds) for the team's current Forge tier - lower is faster. */
	public int goldIntervalSecondsFor(VDTeam team) {
		switch (team.getUpgradeTier(VDTeamUpgrade.FORGE)) {
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
		for (VDTeam team : instance.getTeams().values()) {
			if (team.getUpgradeTier(VDTeamUpgrade.HEAL_POOL) <= 0) continue;
			if (team.getVillagerEntity() == null) continue;

			for (UUID uuid : team.getMembers()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) continue;
				if (player.getLocation().distanceSquared(team.getVillagerEntity().getLocation())
						> HEAL_POOL_RADIUS * HEAL_POOL_RADIUS) continue;

				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
						(int) HEAL_POOL_INTERVAL_TICKS + 20, 0, false, false));
			}
		}
	}
}
