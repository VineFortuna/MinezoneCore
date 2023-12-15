package anthony.SuperCraftBrawl.Game.classes.all;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;

public class WitchClass extends BaseClass {

	private boolean used = false;
	private ItemStack broom = ItemHelper.addEnchant(
			ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WHEAT, 4),
					"" + ChatColor.BLACK + ChatColor.BOLD + "Magic Broom"), Enchantment.DAMAGE_ALL, 3),
			Enchantment.KNOCKBACK, 1);
	private int cooldown = 0;
	private int regenBrooms = 0;

	public WitchClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
	}

	public ItemStack makePurple(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.PURPLE);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String skullOwner = "0f4fb80c-2382-5f0f-b7d0-151cf18fd846";
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzMxY2Q4OWY2NWVhZTExNTU2NWVjMTRjNDQxMGY3M2I0YjQ5ZGYxYWE4Yzg0ODQzN2NhODQwM2NlNjQ2YTJkMSJ9fX0=";
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		GameProfile profile = new GameProfile(UUID.fromString(skullOwner), null);
		profile.getProperties().put("textures", new Property("textures", texture));
		Field profileField = null;

		try {
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		skull.setItemMeta(meta);
		playerEquip.setHelmet(skull);
		playerEquip.setChestplate(makePurple(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makePurple(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makePurple(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof ThrownPotion) {
			ThrownPotion potion = (ThrownPotion) event.getEntity();

			if (potion.getShooter() instanceof Player) {
				Player player = (Player) potion.getShooter();
				if (player.getInventory().getHeldItemSlot() == 1) {
					// Adjust the velocity of the potion
					Vector velocity = potion.getVelocity();
					potion.setVelocity(velocity.multiply(1.3)); // Increase the multiplier to adjust the throwing
																// distance
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Tick(int gameTicks) {
		if (player.getInventory().getItem(1) == null || player.getInventory().getItem(1).getType() == Material.AIR) {
			player.getInventory().setItem(1, new ItemStack(Material.BARRIER));
			this.cooldown = 3;
		}

		if (gameTicks % 20 == 0 && this.cooldown != 0) {
			this.cooldown--;

			if (this.cooldown == 0) {
				ItemStack weakness = new ItemStack(Material.POTION, 1);
				Potion pot2 = new Potion(1);
				pot2.setType(PotionType.WEAKNESS);
				pot2.setSplash(true);
				pot2.apply(weakness);

				ItemStack damage = new ItemStack(Material.POTION, 1);
				Potion pot3 = new Potion(1);
				pot3.setType(PotionType.INSTANT_DAMAGE);
				pot3.setSplash(true);
				pot3.apply(damage);

				ItemStack poison = new ItemStack(Material.POTION, 1);
				Potion pot9 = new Potion(1);
				pot9.setType(PotionType.POISON);
				pot9.setSplash(true);
				PotionMeta meta2 = (PotionMeta) poison.getItemMeta();
				meta2.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 300, 0), true);
				poison.setItemMeta(meta2);
				pot9.apply(poison);

				ItemStack slowness = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						"" + ChatColor.RED + ChatColor.BOLD + "Slowness");
				Potion pot5 = new Potion(3);
				pot5.setType(PotionType.SLOWNESS);
				pot5.setSplash(true);
				pot5.apply(slowness);

				ItemStack[] itemList = { weakness, damage, poison, slowness };
				Random rand = new Random();
				int randomNum = rand.nextInt(itemList.length);
				player.getInventory().setItem(1, new ItemStack(itemList[randomNum]));
			}
		}

		if (gameTicks % 20 == 0 && regenBrooms != 4) {
			if (player.isOnGround()) {
				BaseClass bc = instance.classes.get(player);
				if (bc != null && bc.getLives() <= 0)
					return;

				regenBrooms++;
				player.getInventory().setItem(0,
						ItemHelper.addEnchant(ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.WHEAT, regenBrooms),
										"" + ChatColor.BLACK + ChatColor.BOLD + "Magic Broom"),
								Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1));
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.used = false; // To reset each life
		this.cooldown = 0; // Also same
		this.regenBrooms = 4; // ALSO SAME LMFAO!!!

		ItemStack weakness = new ItemStack(Material.POTION, 1);
		Potion pot2 = new Potion(1);
		pot2.setType(PotionType.WEAKNESS);
		pot2.setSplash(true);
		pot2.apply(weakness);

		ItemStack damage = new ItemStack(Material.POTION, 1);
		Potion pot3 = new Potion(1);
		pot3.setType(PotionType.INSTANT_DAMAGE);
		pot3.setSplash(true);
		pot3.apply(damage);

		ItemStack poison = new ItemStack(Material.POTION, 1);
		Potion pot9 = new Potion(1);
		pot9.setType(PotionType.POISON);
		pot9.setSplash(true);
		PotionMeta meta2 = (PotionMeta) poison.getItemMeta();
		meta2.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 300, 0), true);
		poison.setItemMeta(meta2);
		pot9.apply(poison);

		ItemStack slowness = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				"" + ChatColor.RED + ChatColor.BOLD + "Slowness");
		Potion pot5 = new Potion(3);
		pot5.setType(PotionType.SLOWNESS);
		pot5.setSplash(true);
		pot5.apply(slowness);

		playerInv.setItem(0, this.getAttackWeapon());
		ItemStack[] itemList = { weakness, damage, poison, slowness };
		Random rand = new Random();
		int randomNum = rand.nextInt(itemList.length);
		playerInv.setItem(1, new ItemStack(itemList[randomNum]));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.WHEAT
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			ItemMeta meta = item.getItemMeta();

			if (meta != null && meta.getDisplayName().contains("Magic")) {
				int amount = item.getAmount();
				if (amount > 1) {
					event.setCancelled(true);
					player.setVelocity(new Vector(0, 1, 0).multiply(1.2D));
					this.used = true;
					amount--;
					item.setAmount(amount);
					regenBrooms = amount;
					
					for (Player gamePlayer : instance.players)
						gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Witch;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		return this.broom;
	}

}
