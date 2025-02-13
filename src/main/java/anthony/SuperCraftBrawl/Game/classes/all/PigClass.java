package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class PigClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack speedPork;
	private final ItemStack cookedPork;
	private final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 5 * 20, 2, false, true);

	public PigClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjUwMzM3NzlmODc2MTFmOGU3MWM1YjAyYjkxYjQwYmNhNWMxYzk5YWZhNzUyYWJkNjM2YTQ5NWY5NTNiNjQ2In19fQ==",
				"FF9999",
				6,
				"Pig"
		);

		String[] lore = new String[]{
				"",
				"&7When damaged, receive &b&oSpeed &e" + (speed.getAmplifier() + 1) + " &7for &e" + speed.getDuration() / 20 + "s",
				"&7While set on fire, receive &c&oFire Aspect"
		};

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.PORK),
				"&d&lPork",
				lore
			);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Weapon
		speedPork = ItemHelper.setDetails(
				new ItemStack(Material.PORK),
				"&d&lPork",
				lore
		);
		speedPork.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		speedPork.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Cooked Pork
		cookedPork = ItemHelper.setDetails(
				new ItemStack(Material.GRILLED_PORK),
				"&6&lCooked Pork",
				lore
		);
		cookedPork.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
		cookedPork.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		cookedPork.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		Inventory inventory = player.getInventory();
		boolean hasSpeedEffect = player.hasPotionEffect(speed.getType());
		boolean hasSpeedPork= inventory.contains(speedPork);
		boolean hasCookedPork = inventory.contains(cookedPork);
		boolean isBurning = player.getFireTicks() > 0;

		if (isBurning) {
			if (!hasCookedPork) inventory.setItem(0, cookedPork);
			return;
		}

		if (hasSpeedEffect) {
			if (!hasSpeedPork) inventory.setItem(0, speedPork);
			return;
		}

		if (inventory.contains(weapon)) return;
		inventory.setItem(0, weapon);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, weapon);
	}

	@Override
	public void TakeDamage(EntityDamageEvent event) {
		if (!isPlayerAlive()) return;
		if (instance.getGameManager().spawnProt.containsKey(player)) return;
		if (!(event.getEntity().equals(player))) return;
		addBestEffect(player, speed);
		SoundManager.playSoundToAll(player, Sound.PIG_DEATH, 1, 1);
	}

	private void addBestEffect(Player player, PotionEffect potionEffect) {
		Collection<PotionEffect> potionEffectCollection = player.getActivePotionEffects();

		PotionEffect existingEffect = potionEffectCollection.stream()
				.filter(effect -> effect.getType().equals(potionEffect.getType()))
				.findFirst()
				.orElse(null);

		if (existingEffect != null) {
			if (existingEffect.getAmplifier() > potionEffect.getAmplifier()) return;
			if (existingEffect.getDuration() > potionEffect.getDuration()) return;
		}
		player.addPotionEffect(potionEffect, true);
    }

	@Override
	public ClassType getType() {
		return ClassType.Pig;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
