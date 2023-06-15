package anthony.SuperCraftBrawl.Game.classes.all;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class SilverfishClass extends BaseClass {

	public SilverfishClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	public ItemStack makePink(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.GRAY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String skullOwner = "62c23398-f60c-5c15-8a3c-94aeed5f4c42";
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTgxNGNmNWM0YzdmZmViMjA3NTU1ODU3NjJjYjhiOTc2OWNlYzU4Y2E5OTcwY2FhOTAzNzBjZTI3YjcxNGVhNiJ9fX0=";
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
		playerEquip.setChestplate(makePink(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makePink(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makePink(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		for (Entity en : player.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(player.getName()))
					en.remove();

		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.MONSTER_EGG),
						"" + ChatColor.RESET + ChatColor.GRAY + ChatColor.BOLD + "Silverfish Army", "",
						instance.getManager().getMain().color("&7Spawn silverfishes at enemies locations!")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.MONSTER_EGG) {
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				player.getInventory().remove(Material.MONSTER_EGG);
				for (Player gamePlayer : instance.players) {
					if (gamePlayer != player) {
						if (instance.duosMap != null) {
							if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
								for (int i = 0; i < 5; i++) {
									@SuppressWarnings("deprecation")
									Silverfish silverfish = (Silverfish) gamePlayer.getWorld()
											.spawnCreature(gamePlayer.getLocation(), EntityType.SILVERFISH);
									silverfish.setCustomName("" + ChatColor.RED + player.getName() + "'s "
											+ ChatColor.YELLOW + "Silverfish");
								}
							}
						} else {
							for (int i = 0; i < 5; i++) {
								@SuppressWarnings("deprecation")
								Silverfish silverfish = (Silverfish) gamePlayer.getWorld()
										.spawnCreature(gamePlayer.getLocation(), EntityType.SILVERFISH);
								silverfish.setCustomName("" + ChatColor.RED + player.getName() + "'s "
										+ ChatColor.YELLOW + "Silverfish");
							}
						}
					}
				}
				player.sendMessage(
						instance.getManager().getMain().color("&e&l(!) &eSpawning army of Silverfish to all players!"));
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Silverfish;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack hoe = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.IRON_HOE), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
		ItemMeta meta = hoe.getItemMeta();
		meta.spigot().setUnbreakable(true);
		hoe.setItemMeta(meta);
		return hoe;
	}
}
