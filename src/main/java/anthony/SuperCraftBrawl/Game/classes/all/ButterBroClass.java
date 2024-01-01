package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ButterBroClass extends BaseClass {

	private Cooldown shurikenCooldown = new Cooldown(200);

	public ButterBroClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	public ItemStack makeYellow(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.YELLOW);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("SkyDoesMinecraft");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeYellow(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeYellow(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeYellow(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public ItemStack getFlowerCharge() {
		return ItemHelper.setDetails(new ItemStack(Material.RED_ROSE, 1),
				instance.getGameManager().getMain().color("&c&lFlower Charges"), "",
				instance.getGameManager().getMain().color("&7Right click to throw explosive/firey charges!"));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0,
				ItemHelper.addEnchant(
						ItemHelper.addEnchant(new ItemStack(Material.GOLD_INGOT), Enchantment.KNOCKBACK, 2),
						Enchantment.DAMAGE_ALL, 3));
		ItemStack f = getFlowerCharge();
		f.setAmount(10);
		playerInv.setItem(1, f);

	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item.getType() == Material.RED_ROSE) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (shurikenCooldown.useAndResetCooldown()) {
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);
						ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
							@SuppressWarnings("deprecation")
							@Override
							public void onHit(Player hit) {
								if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
									player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

									for (Player gamePlayer : this.getNearby(3.0)) {
										if (instance.duosMap != null) {
											if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
												EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
														DamageCause.PROJECTILE, 4.0);
												instance.getGameManager().getMain().getServer().getPluginManager()
														.callEvent(damageEvent);
												gamePlayer.damage(4.0, player);
												gamePlayer.setFireTicks(80);
											}
										} else {
											EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
													DamageCause.PROJECTILE, 4.0);
											instance.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											gamePlayer.damage(4.0, player);
											gamePlayer.setFireTicks(80);
										}
									}
								}
								for (Player gamePlayer : instance.players) {
									gamePlayer.playSound(hit.getLocation(), Sound.EXPLODE, 2, 1);
									gamePlayer.playEffect(hit.getLocation(), Effect.EXPLOSION_LARGE, 1);
									// gamePlayer.getWorld().createExplosion(hit.getLocation().getX(),
									// hit.getLocation().getY(), hit.getLocation().getZ(), 3, false, false);
								}

							}

						}, new ItemStack(Material.FIREBALL));
						instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
					event.setCancelled(true);
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.ButterBro;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.GOLD_INGOT), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
		return item;
	}

}
