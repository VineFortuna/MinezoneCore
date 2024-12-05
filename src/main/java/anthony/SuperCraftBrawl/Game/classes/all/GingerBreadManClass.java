package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
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

public class GingerBreadManClass extends BaseClass {

	private ItemStack chocChips = ItemHelper.setDetails(new ItemStack(Material.INK_SACK, 1, (short) 3),
			ChatColor.RED + "Chocolate Chips", "", instance.color("&7Aim at players to knock back"),
			instance.color("&7& give blindness!"));

	public GingerBreadManClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk0MDQzYWIxNjIzOGUzYTFhODhhMmVlZjQ5MGIwZWY4MGY5NTM4MGY3MzhkMWExZWNjNTMzNGRhZDZhYjE1In19fQ==",
				"D2691E", 6, "GingerBreadMan");
	}

	/// give @p
	/// minecraft:player_head[profile={id:[I;-1018782635,-411413609,-1156889585,1818606862],properties:[{name:"textures",value:"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19"}]},minecraft:lore=['{"text":"https://namemc.com/skin/83d9eb9c21c9b152"}']]

	/// give @p
	/// minecraft:player_head[profile={id:[I;-1988614113,583621619,-1936790339,-1531314711],properties:[{name:"textures",value:"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk0MDQzYWIxNjIzOGUzYTFhODhhMmVlZjQ5MGIwZWY4MGY5NTM4MGY3MzhkMWExZWNjNTMzNGRhZDZhYjE1In19fQ=="}]},minecraft:lore=['{"text":"https://namemc.com/skin/ba6e4fb32fe347c9"}']]

	@Override
	public ClassType getType() {
		return ClassType.GingerBreadMan;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetNameTag() {

	}

	// Setting items
	@Override
	public void SetItems(Inventory playerInv) {
		this.chocChips.setAmount(5);
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, this.chocChips);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == this.chocChips.getType() && (event.getAction() == Action.RIGHT_CLICK_AIR
					|| (event.getAction() == Action.RIGHT_CLICK_BLOCK))) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);

						ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
							Vector direction = player.getLocation().getDirection();

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
														DamageCause.PROJECTILE, 5.0);
												instance.getGameManager().getMain().getServer().getPluginManager()
														.callEvent(damageEvent);
												gamePlayer.damage(5.0, player);
												gamePlayer.addPotionEffect(
														new PotionEffect(PotionEffectType.BLINDNESS, 4 * 20, 1));

												Vector v = direction;
												v.setY(1.0);
												gamePlayer.setVelocity(v);
											}
										} else if (gamePlayer != player){
											EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
													DamageCause.PROJECTILE, 5.0);
											instance.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											gamePlayer.damage(5.0, player);
											gamePlayer.addPotionEffect(
													new PotionEffect(PotionEffectType.BLINDNESS, 4 * 20, 1));

											Vector v = direction;
											v.setY(1.0);
											gamePlayer.setVelocity(v);
										}
									}

									for (Player gamePlayer : instance.players) {
										gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
										gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_HUGE, 1);
									}
								}

							}

						}, new ItemStack(this.chocChips));
						instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				}
			}
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack weapon = ItemHelper.create(Material.COOKIE, ChatColor.BLACK + "Cookie Slap");
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
		return weapon;
	}
}
