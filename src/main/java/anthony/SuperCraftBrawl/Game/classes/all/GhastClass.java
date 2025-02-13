package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GhastClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack bowItem;
	private static final int MAX_ARROWS_AMOUNT = 4;
	private static final int ON_KILL_ARROWS_AMOUNT = 2;

	public GhastClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY4OGU2MTY0MmEwYjY4NjQzZjRiYTM2OTJmZTIwNjYyMmI0ZDlhN2QzOTY1YmEwYmUxMzI5YzIxMzJkIn19fQ==",
				"FFFFFF",
				6,
				"Ghast"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.GHAST_TEAR),
				"&f&lTear"
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
		bowItem.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
		ItemHelper.setUnbreakable(bowItem);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		// Add barrier
		Inventory inventory = player.getInventory() ;
		if (!(inventory.contains(Material.ARROW))) inventory.setItem(8, new ItemStack(Material.BARRIER));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, bowItem);
		playerInv.setItem(8, new ItemStack(Material.ARROW, MAX_ARROWS_AMOUNT));
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
		boolean isSelfShot = false;

		if (entityDamager.getType().equals(EntityType.ARROW)) {
			lowerArrowDamage(event);
			// Check if arrow hit was a bow boost
			if (event.getEntity().equals(player)) {
				isSelfShot = true;
				Arrow arrow = (Arrow) entityDamager;
				arrow.setFireTicks(0);
			}
		}

		// Check if the attack is the melee weapon
		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
				&& heldItem != null
				&& heldItem.equals(weapon);

		if (!isWeaponMelee && !isSelfShot) return;
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
		return ClassType.Ghast;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

}
