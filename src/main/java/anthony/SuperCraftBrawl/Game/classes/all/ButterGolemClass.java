package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ButterGolemClass extends BaseClass {

	public ButterGolemClass(GameInstance instance, Player player) {
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
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZhMjYyMjIxYjUwOWY5YWNjZDliYzMwNWFiNGVkY2NiNWMyMDQ4MjExYTdhYjRlMDg4YTY1M2VkMzA2ZGMzIn19fQ==";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeYellow(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeYellow(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeYellow(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public ItemStack getButterBall() {
		return ItemHelper.setDetails(new ItemStack(Material.GOLD_BLOCK, 1), ChatColor.GREEN + "Butter Balls",
				ChatColor.YELLOW + "Right click to throw DEADLY butter balls!");
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		ItemStack butter = getButterBall();
		butter.setAmount(5);
		playerInv.setItem(1, butter);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.GOLD_BLOCK) {
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
								player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

								for (Player gamePlayer : this.getNearby(3.0)) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
											EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
													DamageCause.VOID, 5.5);
											instance.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											gamePlayer.damage(5.5, player);
										}
									} else {
										EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
												DamageCause.VOID, 5.5);
										instance.getGameManager().getMain().getServer().getPluginManager()
												.callEvent(damageEvent);
										gamePlayer.damage(5.5, player);
									}
								}
								for (Player gamePlayer : instance.players) {
									gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
									// gamePlayer.getWorld().createExplosion(hit.getLocation().getX(),
									// hit.getLocation().getY(), hit.getLocation().getZ(), 3, false, false);
									gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_HUGE, 1);
								}

							}
						}

					}, new ItemStack(Material.GOLD_BLOCK));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.5D));
				}
				event.setCancelled(true);
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.ButterGolem;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.GOLD_AXE), ChatColor.YELLOW + "Butter Axe"),
				Enchantment.KNOCKBACK, 3);
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}

}
