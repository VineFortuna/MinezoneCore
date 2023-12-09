package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;

public class GingerBreadManClass extends BaseClass {

	public GingerBreadManClass(GameInstance instance, Player player) {
		super(instance, player);
		this.baseVerticalJump = 1.1;
	}

	@Override
	public ClassType getType() {
		return ClassType.GingerBreadMan;
	}

	public ItemStack makeGreen(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.BLACK);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("GingerbreadMan");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeGreen(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGreen(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGreen(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		return ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.COOKIE), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 1);
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		ItemStack cocoa = ItemHelper.setDetails(new ItemStack(Material.INK_SACK, 7),
				"" + ChatColor.RESET + ChatColor.RED + "Chocolate Chips");
		cocoa.setDurability((short) 3);
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, cocoa);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.INK_SACK) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				int amount = item.getAmount();
				if (amount > 0) {
					amount--;
					if (amount == 0)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(amount);
					
					Vector direction = player.getLocation().getDirection();
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

							for (Player gamePlayer : this.getNearby(3.0)) {
								if (instance.duosMap != null) {
									if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
										EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
												DamageCause.VOID, 5.0);
										instance.getManager().getMain().getServer().getPluginManager()
												.callEvent(damageEvent);
										gamePlayer.damage(5.0, player);
										Vector v = direction;
										v.setY(1.0);
										gamePlayer.setVelocity(v);
										gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 2, true));
									}
								} else {
									EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
											DamageCause.VOID, 6.0);
									instance.getManager().getMain().getServer().getPluginManager()
											.callEvent(damageEvent);
									gamePlayer.damage(6.0, player);
									Vector v = direction;
									v.setY(1.0);
									gamePlayer.setVelocity(v);
									gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 2, true));
								}
							}
							for (Player gamePlayer : instance.players) {
								gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
								gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_HUGE, 1);
							}

						}

					}, new ItemStack(Material.INK_SACK));
					instance.getManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.5D));
				}
				event.setCancelled(true);
			}
		}
	}

}
