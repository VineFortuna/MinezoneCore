package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.gui.VillagerAbilityGUI;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VillagerClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack potatoItem;
	private final Ability tradeAbility = new Ability("&a&lTrade", player);
	private final Ability potatoAbility = new Ability("&6&lPotato Throw", 4, player);
	private static final int MAX_POTATO_AMOUNT = 4;
	private int emeraldsCount;

	public VillagerClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcxYjhiMmFlN2ZiMjc4MmRiZWU5M2E3ZTY3OTc4M2M1MGQ1YTg4NDA0NTcwOGEyMTU5NDE3ODVkN2MzY2NkIn19fQ",
				"6E504B",
				"6E504B",
				"828282",
				6,
				"Villager"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.EMERALD),
				tradeAbility.getAbilityNameRightClickMessage(),
				"",
				"&7Hit enemies to gain emeralds",
				"&7Trade emeralds for items"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Potato Ability
		potatoItem = ItemHelper.setDetails(
				new ItemStack(Material.BAKED_POTATO),
				potatoAbility.getAbilityNameRightClickMessage(),
				"&7Throw at enemies to damage and",
				"&7knock them like a snowball",
				"",
				"&7Allows you to get combos easier"
		);
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (!(event.getEntity() instanceof Player)) return;

		checkToAddEmerald(event);
	}

	private void checkToAddEmerald(EntityDamageByEntityEvent event) {
		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
				&& heldItem != null
				&& heldItem.equals(weapon);

		if (!isWeaponMelee) return;

		emeraldsCount++;
		weapon.setAmount(emeraldsCount);
		player.getInventory().setItem(0, weapon);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		// Resetting Emeralds on Death
		emeraldsCount = 0;
		weapon.setAmount(1);
		// Resetting Potatoes on Death
		potatoAbility.getCooldownInstance().reset();
		ItemStack initialPotatoItem = potatoItem.clone();
		initialPotatoItem.setAmount(MAX_POTATO_AMOUNT);

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, initialPotatoItem);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		handleAddingPotato();
		updateActionBarMessage();
	}

	private void updateActionBarMessage() {
		ItemStack potatoStack = player.getInventory().getItem(1);
		if (potatoStack == null) return;

		int potatoes = potatoStack.getType() == Material.BARRIER ? 0 : potatoStack.getAmount() ;
		long timeLeft = potatoAbility.getCooldownInstance().getRemainingCooldownMillis();

		String message = ChatColorHelper.color("&6&lPotatoes: &r&e" + potatoes + "/" + MAX_POTATO_AMOUNT);;
		if (potatoes < MAX_POTATO_AMOUNT) {
			// Regenerating case
			int secondsLeft = (int) (timeLeft > 0 ? (timeLeft / 1000) + 1 : 0);
			String regenText = "&8 ┃ &fRegenerating in &e" + secondsLeft + "s";

			message += ChatColorHelper.color(regenText);
		}

		getActionBarManager().setActionBar(player, "villager.potato", message, 2);
	}

	private void handleAddingPotato() {
		ItemStack item = player.getInventory().getItem(1);
		if (item == null) return;

		// Handle barrier case
		if (item.getType() == Material.BARRIER && potatoAbility.isReady()) {
			player.getInventory().setItem(1, potatoItem.clone());
			potatoAbility.use();
			player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 1.5f);
			return;
		}

		// Only proceed if it's a potato item
		if (!item.isSimilar(potatoItem)) return;

		// Add potato if we have less than 4 and timer has passed
		int currentAmount = item.getAmount();
		if (currentAmount < MAX_POTATO_AMOUNT && potatoAbility.isReady()) {
			item.setAmount(currentAmount + 1);
			potatoAbility.use();
			player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 1.5f);
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;

		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			// Trade Ability
			if (item.isSimilar(weapon)) {
				// Check Right CLick
				new VillagerAbilityGUI(
						instance.getGameManager().getMain(),
						instance,
						this
				).inv.open(player);
			// Potato Ability
			} else if (item.isSimilar(potatoItem)) {
				event.setCancelled(true);
				usePotatoThrowAbility(item);
			}
		}
	}

	private void usePotatoThrowAbility(ItemStack item) {
		int amount = item.getAmount();

		if (amount > 0) {
			amount--;

			if (amount == 0)
				player.getInventory().setItem(1, new ItemStack(Material.BARRIER));
			else {
				item.setAmount(amount);
			}

			if (amount == 3) {
				potatoAbility.use();
			}

			player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 1, 1);
			throwPotatoProjectile();
		}
	}

	private void throwPotatoProjectile() {
		ItemProjectile projectile = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@SuppressWarnings("deprecation")
			@Override
			public void onHit(Player playerHit) {
				if (playerHit == null) return;
				playerHit.damage(0, player);
			}
		}, new ItemStack(Material.BAKED_POTATO));
		instance.getGameManager().getProjManager().shootProjectile(projectile, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(1.5D));
	}

	public int getEmeraldsCount() {
		return emeraldsCount;
	}
	public void setEmeraldsCount(int emeraldsCount) {
		this.emeraldsCount = emeraldsCount;
	}

	@Override
	public ClassType getType() {
		return ClassType.Villager;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}