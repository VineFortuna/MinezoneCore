package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WitherClass extends BaseClass {

	private int skullCounter;
	private final ItemStack weapon;
	private final ItemStack bow;
	private final Ability skullAbility = new Ability("&8&lSkull", 5, player);
	private static final int SKULL_AMOUNT = 10;
	private final PotionEffect wither = new PotionEffect(PotionEffectType.WITHER, 5 * 20, 0, true);

	public WitherClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRmMzI4ZjUwNDQxMjliNWQxZjk2YWZmZDFiOGMwNWJjZGU2YmQ4ZTc1NmFmZjVjNTAyMDU4NWVlZjhhM2RhZiJ9fX0=",
				"1F1F1F",
				6,
				"Wither"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.NETHER_STAR),
				"&8&lWither Star",
				"",
				"&7Apply &8&oWither &e" + (wither.getAmplifier() + 1) + " &7for &e" + wither.getDuration() / 20 + "s"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Bow
		bow = ItemHelper.setDetails(
				new ItemStack(Material.BOW),
				skullAbility.getAbilityName() + " Shooter &7(Right Click)",
				"&7Shoot up to &e" + SKULL_AMOUNT + " &7skulls before recharging",
				"&7Skulls explode and apply &8&oWither &e" + (wither.getAmplifier() + 1) + " &7for &e" + wither.getDuration() / 20 + "s",
				"",
				"&7You can spam this ability"
		);
		bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		ItemHelper.setUnbreakable(bow);
		ItemHelper.setHideFlags(bow, true);
	}

	@Override
	public ClassType getType() {
		return ClassType.Wither;
	}

	@Override
	public void SetItems(Inventory playerInv) {
		skullAbility.getCooldownInstance().reset();
		skullCounter = SKULL_AMOUNT;
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, bow);
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		if (!skullAbility.isReady()) {
			int remainingTime = (int) (skullAbility.getCooldownInstance().getRemainingCooldownSeconds() + 1);
			String msg = ChatColorHelper.color(skullAbility.getAbilityName() + " Shooter &rregenerates in &e" + remainingTime + "s");
			getActionBarManager().setActionBar(player, "wither.cooldown", msg, 2);
		} else {
			if (skullCounter == 0) return;
			String plural =  skullCounter > 1 ? "s" : "";
			String msg = ChatColorHelper.color("You can use &e" + skullCounter + " &rmore " + skullAbility.getAbilityName() + plural);
			getActionBarManager().setActionBar(player, "wither.cooldown", msg, 2);
		}
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		checkToApplyWither(event);
	}

	private void checkToApplyWither(EntityDamageByEntityEvent event) {
		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

		boolean isWitherSkull = event.getDamager() != null && event.getDamager().getType().equals(EntityType.WITHER_SKULL);
		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
				&& heldItem != null
				&& heldItem.equals(weapon);

		if (isWitherSkull || isWeaponMelee) {
			if (event.getEntity() instanceof Player) {
				Player damagedPlayer = (Player) event.getEntity();
				if (damagedPlayer == player) return;
				damagedPlayer.addPotionEffect(wither);
			}
		}
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntityType() == EntityType.ARROW) {
			event.setCancelled(true);

			if (skullAbility.isReady()) {
				WitherSkull skull = player.launchProjectile(WitherSkull.class);
				skull.setIsIncendiary(false);
				skullCounter--;
				if (skullCounter == 0) {
					skullAbility.use();
					skullCounter = SKULL_AMOUNT;
				}
			}
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}