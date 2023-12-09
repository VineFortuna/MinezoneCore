package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class SantaClass extends BaseClass {

	private int cookiesEaten = 0;
	private ItemStack rudolph = ItemHelper.createMonsterEgg(EntityType.HORSE, 1, "&c&lRudolph");

	public SantaClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.3;
	}

	public ItemStack makeRed(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.RED);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("Santa");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeRed(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeRed(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeRed(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.cookiesEaten = 0;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, new ItemStack(Material.MILK_BUCKET));
		playerInv.setItem(2, this.rudolph);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			ItemMeta meta = item.getItemMeta();
			if (item.getType() == Material.COOKIE
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (this.cookiesEaten == 0)
					player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999999, 0));
				else if (this.cookiesEaten == 1)
					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
				else if (this.cookiesEaten == 2)
					player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 3));
				else {
					player.sendMessage(
							instance.getManager().getMain().color("&c&l(!) &rYou have used all your cookies!"));
					return;
				}

				player.sendMessage(instance.getManager().getMain()
						.color("&2&l(!) &rYou ate a cookie and gained an effect! (not just positive tho"));
				player.playSound(player.getLocation(), Sound.EAT, 1, 1);
				player.removePotionEffect(PotionEffectType.SLOW);
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, this.cookiesEaten));
				this.cookiesEaten++;
			} else if (item.getType() == Material.MILK_BUCKET
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				if (santa.getTime() < 15000) {
					int seconds = (15000 - santa.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Slow down Santa! Your Milk is still regenerating for " + ChatColor.YELLOW + seconds
							+ "s");
				} else {
					santa.restart();
					Iterator<PotionEffect> iterator = player.getActivePotionEffects().iterator();

					while (iterator.hasNext()) {
						PotionEffect potionEffect = iterator.next();
						if (potionEffect.getType() != PotionEffectType.SLOW
								&& potionEffect.getType() != PotionEffectType.INCREASE_DAMAGE
								&& potionEffect.getType() != PotionEffectType.DAMAGE_RESISTANCE
								&& potionEffect.getType() != PotionEffectType.JUMP) {
							iterator.remove();
						}
					}

					player.setFireTicks(0);
					player.sendMessage(
							instance.getManager().getMain().color("&2&l(!) &rHO HO HO! Dat milk be tastin' hella gud"));
				}
			} else if (item.getType() == this.rudolph.getType()) {
				if (meta != null && meta.getDisplayName() != null) {
					if (meta.getDisplayName().contains("Rudolph")) {
						final Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(),
								EntityType.HORSE);
						horse.setTamed(true);
						horse.setOwner(player);
						horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
						horse.setCustomName(
								"" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Rudolph");
						player.getInventory().clear(player.getInventory().getHeldItemSlot());

						player.teleport(horse.getLocation());
						horse.setPassenger(player);

						Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), new BukkitRunnable() {
							@Override
							public void run() {
								if (!horse.isDead()) {
									horse.eject();
									horse.remove();
								}
							}
						}, 15 * 20);
					}
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Santa;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.COOKIE), Enchantment.KNOCKBACK, 1), Enchantment.DAMAGE_ALL,
				3);
		return item;
	}

}