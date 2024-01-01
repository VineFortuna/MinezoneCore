package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
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

public class SantaClass extends BaseClass {

	private int cookiesEaten = 0;
	private boolean strength = false, resistance = false, jump = false;
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
		this.strength = false;
		this.resistance = false;
		this.jump = false;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.MILK_BUCKET), "Delicious Milk"));
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
				if (cookie.getTime() < 3000) {
					int seconds = (3000 - cookie.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Slow down Santa! Try again in "
							+ ChatColor.YELLOW + seconds + "s");
				} else {
					cookie.restart();
					if (this.cookiesEaten == 0) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999999, 0));
						strength = true;
					} else if (this.cookiesEaten == 1) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
						this.resistance = true;
					} else if (this.cookiesEaten == 2) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 3));
						this.jump = true;
					} else {
						player.sendMessage(instance.getGameManager().getMain()
								.color("&c&l(!) &rYou have used all your delicious cookies!"));
						return;
					}

					player.sendMessage(instance.getGameManager().getMain()
							.color("&2&l(!) &rYou ate a cookie and gained an effect! (not just positive tho"));
					player.playSound(player.getLocation(), Sound.EAT, 1, 1);
					player.removePotionEffect(PotionEffectType.SLOW);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, this.cookiesEaten));
					this.cookiesEaten++;
				}
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
					for (PotionEffect type : player.getActivePotionEffects())
						player.removePotionEffect(type.getType());

					checkActiveEffects();
					player.playSound(player.getLocation(), Sound.WATER, 1, 1);
					player.setFireTicks(0);
					player.sendMessage(
							instance.getGameManager().getMain().color("&2&l(!) &rHO HO HO! Dat milk be tastin' hella gud"));
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

						Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), new BukkitRunnable() {
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

	private void checkActiveEffects() {
		if (this.strength)
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999999, 0));
		if (this.resistance)
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
		if (this.jump)
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 3));
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, this.cookiesEaten));
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
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.COOKIE), "Delicious Cookie", "",
						ChatColor.RESET + "1st Use: Strength 1, Slow 1",
						ChatColor.RESET + "2nd Use: Resistance 1, Slow 2", ChatColor.RESET + "3rd Use: Jump 4, Slow 3"),
				Enchantment.KNOCKBACK, 1), Enchantment.DAMAGE_ALL, 3);
		return item;
	}

}