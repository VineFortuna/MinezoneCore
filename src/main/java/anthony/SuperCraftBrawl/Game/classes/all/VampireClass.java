package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VampireClass extends BaseClass {

	/** How much health every hit of the Fang gives back, in health points. */
	private static final double FANG_HEAL = 1;

	private final ItemStack weapon;
	private final ItemStack bowItem;
	private final Ability bowAbility = new Ability("&4&lBow", 7, player);
	private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 4 * 20, 1);

	public VampireClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTI2YTk4ZDQwMzhlYWJhNDdlMDJlZWUxNTUxZGE5OTJhYTVhZDQ2NzA1YTc4MWY0NjE0NzA0MmQyOWNhZjEwNCJ9fX0=",
				"1A1A1A",
				"B20B14",
				"B20B14",
				6,
				"Vampire"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.GHAST_TEAR),
				"&f&lFang",
				"",
				"&7Hits steal health from players and",
				"&7mobs, healing you for &e" + ItemHelper.formatDouble(FANG_HEAL / 2) + " &c❤"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Bow Ability
		bowItem = ItemHelper.setDetails(
				new ItemStack(Material.BOW),
				bowAbility.getAbilityNameRightClickMessage(),
				"&7Apply &2&o" + ItemHelper.getPotionEffectName(poison.getType()) + "&r &e" + (poison.getAmplifier() + 1)
						+ " &7for &e" + poison.getDuration() / 20 + "s",
				"",
				"&7Landing a shot restores the cooldown"
		);
		bowItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		ItemHelper.setUnbreakable(bowItem);
		ItemHelper.setHideFlags(bowItem, true);
	}

	@Override
	public ClassType getType() {
		return ClassType.Vampire;
	}

	@Override
	public void setItems(Inventory playerInv) {
		bowAbility.getCooldownInstance().reset();

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, bowItem);
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		bowAbility.updateActionBar(player, this);
	}

	@Override
	public void doDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Arrow) {
			if (!(event.getEntity() instanceof Player)) return;

			Player hit = (Player) event.getEntity();
			if (instance.duosMap != null && instance.team.get(hit).equals(instance.team.get(player))) return;

			hit.addPotionEffect(poison);
			// Landing a shot restores the cooldown of the bow
			bowAbility.getCooldownInstance().reset();
		} else {
			healFromFang(event);
		}
	}

	/** Hitting any mob steals health too, whoever it belongs to: the summons of enemies, of teammates and the own ones. */
	@Override
	public void doDamageToMob(EntityDamageByEntityEvent event) {
		healFromFang(event);
	}

	private void healFromFang(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
		if (!weapon.equals(player.getInventory().getItemInHand())) return;

		player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + FANG_HEAL));
	}

	@Override
	public void projectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Arrow)) return;

		if (!bowAbility.isReady()) {
			event.setCancelled(true);
			return;
		}
		bowAbility.use();
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
