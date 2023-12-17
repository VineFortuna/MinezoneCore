package anthony.SuperCraftBrawl.Game.classes.all;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class BunnyClass extends BaseClass {

	public BunnyClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.15;
	}

	public ItemStack makeRed(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.GRAY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String skullOwner = "9893f716-732d-5be8-b530-84d714f12354";
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzRiMTJkZDJkOTljNDdlMTkxM2M0YjFkNGQ5ZmFmNjlhZDYxZTk1YWUyN2NkNGU5ZjlmZTVlMzBhNjM0M2ExNiJ9fX0=";
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
		playerEquip.setChestplate(makeRed(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeRed(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeRed(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0,
				ItemHelper.addEnchant(
						ItemHelper.addEnchant(new ItemStack(Material.CARROT_ITEM), Enchantment.DAMAGE_ALL, 3),
						Enchantment.KNOCKBACK, 1));
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.GOLDEN_CARROT), "", "",
						instance.getManager().getMain().color("&7Right click to gain:"),
						instance.getManager().getMain().color("   &r5 sec Regeneration II"),
						instance.getManager().getMain().color("   &r5 sec Speed V")));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 2));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 2));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 2));
		else if (!(player.getActivePotionEffects().contains(PotionEffectType.JUMP)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 2));
	}

	private void carrotEffect() {
		BukkitRunnable runTimer = new BukkitRunnable() {
			int ticks = 5;

			@Override
			public void run() {
				if (ticks == 5) {
					player.removePotionEffect(PotionEffectType.SPEED);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 110, 4));
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 110, 1));
				} else if (ticks == 0) {
					player.removePotionEffect(PotionEffectType.SPEED);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 2));
					this.cancel();
				}

				ticks--;
			}
		};
		runTimer.runTaskTimer(instance.getManager().getMain(), 0, 20);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.GOLDEN_CARROT
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			int amount = item.getAmount();
			if (amount > 0) {
				amount--;
				if (amount == 0)
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				else
					item.setAmount(amount);

				event.setCancelled(true);
			}
			carrotEffect();
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Bunny;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(
						ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.CARROT_ITEM),
										"" + ChatColor.RESET + "Carrot", ChatColor.GRAY + "", ChatColor.YELLOW + ""),
								Enchantment.DAMAGE_ALL, 3),
						Enchantment.KNOCKBACK, 1);
		return item;
	}
}
