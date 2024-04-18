package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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

import java.lang.reflect.Field;
import java.util.UUID;

public class ChickenClass extends BaseClass {

	public ChickenClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
	}

	public ItemStack makeYellow(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.YELLOW);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDg5ZmVmMmVmNGY4MWVlYzZkMDdiYWVmNmM0YWVhNzRlNDQyZGNlNzJhMDFkZTk2NGViY2JhYzhhOGQ4MmM3NyJ9fX0=";
		ItemStack skull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(skull);
		playerEquip.setChestplate(makeYellow(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeYellow(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeYellow(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public ItemStack getEggs() {
		return ItemHelper.setDetails(new ItemStack(Material.EGG, 10), ChatColor.YELLOW + "Explosive Eggs", "",
				ChatColor.GRAY + "Right click to throw DEADLY eggs!");
		//
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, this.getEggs());

	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.EGG) {
			ItemMeta meta = item.getItemMeta();

			if (meta.getDisplayName().contains("Easter"))
				return;

			event.setCancelled(true);
			if (player.getGameMode() != GameMode.SPECTATOR) {
				for (Player gamePlayer : instance.players)
					gamePlayer.playSound(player.getLocation(), Sound.CHICKEN_HURT, 1, 1);
				
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
								Location hitLoc = this.getBaseProj().getEntity().getLocation();
								player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

								for (Player gamePlayer : this.getNearby(3.0)) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
											EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
													DamageCause.PROJECTILE, 4.0);
											instance.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											gamePlayer.damage(4.0, player);
										}
									} else {
										EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
												DamageCause.PROJECTILE, 4.0);
										instance.getGameManager().getMain().getServer().getPluginManager()
												.callEvent(damageEvent);
										gamePlayer.damage(4.0, player);
									}
								}
								for (Player gamePlayer : instance.players) {
									gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
									gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_HUGE, 1);
								}
							}

						}

					}, new ItemStack(Material.EGG));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.5D));
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Chicken;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1);
		ItemMeta meta = sword.getItemMeta();
		meta.spigot().setUnbreakable(true);
		sword.setItemMeta(meta);
		return sword;
	}

}
