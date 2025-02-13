package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class RabbitClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack upgradedWeaponItem;
	private final PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, 3 * 20, 1, false, true);
	private final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 999999999, 1, false, false);
	private final PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 999999999, 2, false, false);
	Random random = new Random();

	public RabbitClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2QxMTY5YjI2OTRhNmFiYTgyNjM2MDk5MjM2NWJjZGE1YTEwYzg5YTNhYTJiNDhjNDM4NTMxZGQ4Njg1YzNhNyJ9fX0=",
				"967C60",
				6,
				"Rabbit"
		);

		String[] lore = new String[] {
				"",
				"&7Hitting enemies have a 1 in 3 chance",
				"&7to empower your kick",
				"",
				"&7Empowered kick send players away and",
				"&7apply &3&oSlowness &e" + (slowness.getAmplifier() + 1) + " &7for &e" + slowness.getDuration() / 20 + "s"
		};

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.RABBIT_FOOT),
				"&a&lKick",
				lore
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Upgraded Weapon
		upgradedWeaponItem = ItemHelper.setDetails(
				new ItemStack(Material.RABBIT_FOOT),
				"&a&lHyper Kick",
				lore
		);
		upgradedWeaponItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
		upgradedWeaponItem.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, weapon);
		player.addPotionEffect(speed);
		player.addPotionEffect(jump);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(speed);
		if (!(player.getActivePotionEffects().contains(PotionEffectType.JUMP)))
			player.addPotionEffect(jump);
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
						&& heldItem != null
						&& (heldItem.equals(weapon) || heldItem.equals(upgradedWeaponItem));

		if (!isWeaponMelee) return;
		if (!(event.getEntity() instanceof Player)) return;

		triggerChanceToUpgradeWeapon(event);
	}

	private void triggerChanceToUpgradeWeapon(EntityDamageByEntityEvent event) {
		int upgradeChance = random.nextInt(3);
		onUpgradeAbility(upgradeChance, event);
	}

	private void onUpgradeAbility(int upgradeChance, EntityDamageByEntityEvent event) {
		Player damagedPlayer = (Player) event.getEntity();
		if (!(player.getInventory().contains(upgradedWeaponItem))) {
			if (upgradeChance == 0) {
				player.getInventory().setItem(0, upgradedWeaponItem);
				Vector direction = player.getLocation().getDirection();
				direction.setY(1.0);
				damagedPlayer.setVelocity(direction);
				player.sendMessage(ChatColorHelper.color("&2&l(!) &rWeapon upgraded!"));
				SoundManager.playSoundToAll(player, Sound.CAT_HISS, 1, 1);
			}
		} else {
			damagedPlayer.addPotionEffect(slowness);
			player.getInventory().setItem(0, weapon);
			player.sendMessage(ChatColorHelper.color("&2&l(!) &rWeapon downgraded :("));
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Rabbit;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
