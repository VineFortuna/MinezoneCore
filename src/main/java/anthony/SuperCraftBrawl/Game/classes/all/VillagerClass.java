package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.gui.VillagerAbilityGUI;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class VillagerClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack potatoItem;
	private final Ability tradeAbility = new Ability("&a&lTrade", player);
	private final Ability potatoAbility = new Ability("&6&lPotato Throw", player);
	private final PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 3, false, true);
	private final PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1, false, true);
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
				new ItemStack(Material.BAKED_POTATO, 4),
				potatoAbility.getAbilityNameRightClickMessage(),
				"&7Inflict one of 3 effects on enemies:",
				"&7▶ &3&oSlowness &e" + (slowness.getAmplifier() + 1) + " &7for &e" + slowness.getDuration() / 20 + "s",
				"&7▶ &f&oWeakness &e" + (weakness.getAmplifier() + 1) + " &7for &e" + weakness.getDuration() / 20 + "s"
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

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, potatoItem);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;

		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			// TRADE ABILITY
			if (item.isSimilar(weapon)) {
				// Check Right CLick
				new VillagerAbilityGUI(
						instance.getGameManager().getMain(),
						instance,
						this
				).inv.open(player);
			} else if (item.isSimilar(potatoItem)) {
				event.setCancelled(true);
				int amount = item.getAmount();
				if (amount > 0) {
					amount--;
					if (amount == 0)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(amount);
					
					player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 1, 1);
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@SuppressWarnings("deprecation")
						@Override
						public void onHit(Player hit) {
							if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();
								player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
								Random r = new Random();
								int randomNumber = r.nextInt(100);

								// Percentage chances for each effect
								int slownessPercentage = 50;
								int weaknessPercentage = 50;

								// Determining effect based on the number range
								PotionEffect effect;
								if (randomNumber < slownessPercentage) {
									effect = slowness;
								} else if (randomNumber < slownessPercentage + weaknessPercentage) {
									effect = weakness;
								} else {
									effect = null;
								}

								// Applying effect
								for (Player gamePlayer : this.getNearby(2.5)) {
									if (gamePlayer != player && !checkIfDead(player, instance)) {
											gamePlayer.addPotionEffect(effect);
									}
								}
								// Playing sound and effect
								player.getWorld().playSound(hitLoc, Sound.SPLASH2, 2, 1);
								player.getWorld().playEffect(hitLoc, Effect.SPLASH, 1);
							}
						}
					}, new ItemStack(Material.BAKED_POTATO));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				}
			}
		}
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