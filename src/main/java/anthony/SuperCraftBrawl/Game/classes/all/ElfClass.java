package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ElfClass extends BaseClass {

	private boolean isUsed = false;

	public ElfClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.15;
		createArmor(null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGRlNjkzZjEyYjY4MjJmYWQ1ZTZmMjgzYzU1YzM4NWJmZjI1NDhhZTRiMWIyOTQzYWQwNWI1N2VmNWQzOTNiYiJ9fX0=",
				"228B22", 6, "Elf");
	}


	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.isUsed = false;
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, getSugarBombs());
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));

		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Zombie
				&& instance.classes.get(player).getLives() > 0) {
			if (this.isUsed) {
				String msg = instance.getGameManager().getMain().color("&cYou used your &2&lElf's Cake &cthis life!");
				getActionBarManager().setActionBar(player, "elfCake", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &2&lElf's Cake");
				getActionBarManager().setActionBar(player, "elfCake", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.SUGAR
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
						|| event.getAction() == Action.LEFT_CLICK_AIR
						|| event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				int amount = item.getAmount();
				if (amount > 0) {
					amount--;
					if (amount == 0)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(amount);

					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();

								for (Player gamePlayer : this.getNearby(3.0)) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
											@SuppressWarnings("deprecation")
											EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
													DamageCause.VOID, 5.5);
											instance.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											gamePlayer.damage(5.5, player);
											gamePlayer.addPotionEffect(
													new PotionEffect(PotionEffectType.CONFUSION, 190, 2));
											gamePlayer.setVelocity(new Vector(0, 0.6, 0).multiply(1.0D));
										}
									} else if (gamePlayer != player) {
										EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
												DamageCause.VOID, 5.5);
										instance.getGameManager().getMain().getServer().getPluginManager()
												.callEvent(damageEvent);
										gamePlayer.damage(5.5, player);
										gamePlayer
												.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 190, 2));
										gamePlayer.setVelocity(new Vector(0, 0.6, 0).multiply(1.0D));
									}
								}
								for (Player gamePlayer : instance.players) {
									gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
									gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
								}
							}
						}

					}, new ItemStack(Material.SUGAR));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				}
			}
		} else if (item != null && item.getType() == Material.CAKE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (this.isUsed == false) {
					double health = player.getHealth();

					if (health >= 19.5) {
						player.sendMessage(instance.color("&c&l(!) &rProbably should save that when you need health!"));
						return;
					}

					if (health >= 10.0) {
						player.setHealth(20.0);
					} else {
						health += 10.0;
						player.setHealth(health);
					}

					player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 110, 0));

					for (Player gamePlayer : instance.players) {
						gamePlayer.playSound(player.getLocation(), Sound.EAT, 2, 2);
					}

					this.isUsed = true;
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Elf;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.setUnbreakable(
						ItemHelper.addEnchant(ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.CAKE), instance.color("&2&lElf's Cake"),
										"", instance.color("&7Right click to heal! 1 use per life")),
								Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1));
		return item;
	}

	public ItemStack getSugarBombs() {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.SUGAR, 7), instance.color("&cSugar Bombs"), "",
						instance.color("&7Aim at other players to explode"), instance.color("&7them & give nausea!"));
		return item;
	}

}
