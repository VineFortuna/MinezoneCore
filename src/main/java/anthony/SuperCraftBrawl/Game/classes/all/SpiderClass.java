package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpiderClass extends BaseClass {

	private final ItemStack weapon;
	private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 4 * 20, 0, true);
	private final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 9999999, 2, false);


	public SpiderClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg3YTk2YThjMjNiODNiMzJhNzNkZjA1MWY2Yjg0YzJlZjI0ZDI1YmE0MTkwZGJlNzRmMTExMzg2MjliNWFlZiJ9fX0=",
				"4C453B",
				"5C0000",
				"4C453B",
				6,
				"Spider"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.SPIDER_EYE),
				"&c&lEye",
				"",
				"&7Apply &2&oPoison &e" + (poison.getAmplifier() + 1) + " &7for &e" + poison.getDuration() / 20 + "s"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0,weapon);
		player.addPotionEffect(speed);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(speed);
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;

		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
				&& heldItem != null
				&& heldItem.equals(weapon);

		if (!isWeaponMelee) return;
		Entity damagedEntity = event.getEntity();
		if (!(damagedEntity instanceof Player)) return;

		Player damagedPlayer = (Player) damagedEntity;
		damagedPlayer.addPotionEffect(poison);
	}

	@Override
	public ClassType getType() {
		return ClassType.Spider;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
