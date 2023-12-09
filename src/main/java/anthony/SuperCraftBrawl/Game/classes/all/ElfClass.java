package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
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

public class ElfClass extends BaseClass {

	private boolean cake = false;

	public ElfClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	@Override
	public ClassType getType() {
		return ClassType.Elf;
	}

	public ItemStack makeGreen(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.GREEN);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setChestplate(makeGreen(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGreen(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGreen(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.CAKE), "" + ChatColor.GREEN + "Delicious Cake",
						"", "" + ChatColor.GRAY + "Right click to heal"), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
		return item;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.cake = false; //To reset each life
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.SUGAR, 5),
				"" + ChatColor.RESET + ChatColor.GREEN + "Sugar Bombs"));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		
		if (item != null && item.getType() == Material.SUGAR) {
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
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

							for (Player gamePlayer : this.getNearby(3.0)) {
								EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
										DamageCause.PROJECTILE, 6.0);
								instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
								gamePlayer.damage(6.0, player);
								gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 2, true));
							}
							for (Player gamePlayer : instance.players) {
								gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
								gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
							}

						}

					}, new ItemStack(Material.SUGAR));
					instance.getManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.5D));
				}
				event.setCancelled(true);
			}
		} else if (item.getType() == Material.CAKE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (this.cake == false) {
					if (player.getHealth() >= 18.0) {
						player.sendMessage(instance.getManager().getMain()
								.color("&c&l(!) &rDon't waste your cake like that!"));
					} else {
						int uses = 7;
						this.cake = true;
						while (uses != 0) {
							uses--;
							if (player.getHealth() >= 19.0) {
								player.setHealth(20.0);
								break;
							}
							
							player.setHealth(player.getHealth() + 1.0);
						}
						player.sendMessage(instance.getManager().getMain()
								.color("&e&l(!) &rWow that was a delicious cake! I'm ready to fight again"));
					}
				} else
					player.sendMessage(
							instance.getManager().getMain().color("&c&l(!) &rYou already used your cake this life"));
			}
		}
	}

}
