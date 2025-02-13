package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class SkeletonClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack bowItem;
	private final Ability arrowAbility = new Ability("&f&lAttack Arrow", 5, player);
	private static final int MAX_ARROWS_AMOUNT = 6;
	private static final int ON_KILL_ARROWS_AMOUNT = 3;

	public SkeletonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjI3OTVjM2M2ZjM2ZDY3ZGVjZjlhMzE5NWUxMjgwNDBiZWM1MjI2YjA1NWYyYjE2ZDQ2ZmExOWE5MTgwZTAyMyJ9fX0=",
				"D6D6D6",
				6,
				"Skeleton"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.BONE),
				arrowAbility.getAbilityNameRightClickMessage(),
				"",
				"&7Quickly shoot an arrow",
				"",
				"&7If you hit an enemy, gain an arrow"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Bow
		bowItem = ItemHelper.setDetails(
				new ItemStack(Material.BOW),
				"&f&lBow &7(Right click)",
				"",
				"&7Gain arrows by hitting enemies",
				"&7Gain &e" + ON_KILL_ARROWS_AMOUNT + " &7arrows on kill",
				"",
				"&7You can only hold up to &e" + MAX_ARROWS_AMOUNT + " &7arrows"
		);
		bowItem.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
		ItemHelper.setUnbreakable(bowItem);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		// Cooldown bar
		arrowAbility.updateActionBarWhite(player, this);
		// Add barrier
		Inventory inventory = player.getInventory() ;
		if (!(inventory.contains(Material.ARROW))) inventory.setItem(8, new ItemStack(Material.BARRIER));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		arrowAbility.getCooldownInstance().reset();
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, bowItem);
		playerInv.setItem(8, new ItemStack(Material.ARROW, MAX_ARROWS_AMOUNT));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		Action action = event.getAction();

		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;

		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

		if (item.equals(weapon)) {
			if (!arrowAbility.isReady()) return;

			useArrowAbility();
			arrowAbility.use();
		}
	}

	private void useArrowAbility() {
		Arrow arrow = player.launchProjectile(Arrow.class);
		String string = arrowAbility.getAbilityName();
		arrow.setMetadata(
				string,
				new FixedMetadataValue(instance.getGameManager().getMain(), string)
		);
		player.getWorld().playSound(player.getLocation(), Sound.SKELETON_HURT, 1, 1);
	}

	@Override
	public void classesEvent(Player damagerPlayer, BaseClass baseClass) {
		super.classesEvent(damagerPlayer, baseClass);

		handleAddingArrow(ON_KILL_ARROWS_AMOUNT);
	}

	@Override
    public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (!(event.getEntity() instanceof Player)) return;

		// Check if the attack is an arrow
		Entity entityDamager = event.getDamager();
		boolean isAttackArrow = false;
		boolean isSelfShot = false;

		if (entityDamager.getType().equals(EntityType.ARROW)) {
			lowerArrowDamage(event);
			// Check if arrow hit was a bow boost
			if (event.getEntity().equals(player))
				isSelfShot = true;
			// Check if arrow is from the ability
			Arrow arrow = (Arrow) entityDamager;
			if (arrow.hasMetadata(arrowAbility.getAbilityName())) {
				isAttackArrow = true;
			}
		}

		// Check if the attack is the melee weapon
		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
				&& heldItem != null
				&& heldItem.equals(weapon);

		if (!isWeaponMelee && !isAttackArrow && !isSelfShot) return;
		handleAddingArrow(1);
	}

	private void handleAddingArrow(int amountToAdd) {
		Inventory inventory = player.getInventory();
		int arrowSlot = inventory.first(Material.ARROW);

		if (arrowSlot == -1) {
			inventory.setItem(8, new ItemStack(Material.ARROW, amountToAdd));
			return;
		}

		ItemStack arrow = inventory.getItem(inventory.first(Material.ARROW));

		int arrowAmount = arrow.getAmount();
		if (arrowAmount >= MAX_ARROWS_AMOUNT) return;

		int newAmount = Math.min(arrowAmount + amountToAdd, MAX_ARROWS_AMOUNT);
		int amountToActuallyAdd = newAmount - arrowAmount;

		if (amountToActuallyAdd > 0) {
			inventory.addItem(new ItemStack(Material.ARROW, amountToActuallyAdd));
		}
	}


	private void lowerArrowDamage(EntityDamageByEntityEvent event) {
		Arrow arrow = (Arrow) event.getDamager();
		double arrowVelocity = arrow.getVelocity().length();
		double modifiedDamage = event.getDamage() * arrowVelocity;

		if (modifiedDamage >= 10)
			modifiedDamage = 7.50;

		event.setDamage(modifiedDamage);
	}

	@Override
	public ClassType getType() {
		return ClassType.Skeleton;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}