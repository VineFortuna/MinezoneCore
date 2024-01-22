package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class GhastClass extends BaseClass {

	private int cooldown = 0;

	public GhastClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
	}

	public ItemStack makeWhite(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.WHITE);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY4OGU2MTY0MmEwYjY4NjQzZjRiYTM2OTJmZTIwNjYyMmI0ZDlhN2QzOTY1YmEwYmUxMzI5YzIxMzJkIn19fQ==";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeWhite(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeWhite(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeWhite(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Ghast
				&& instance.classes.get(player).getLives() > 0) {
			if (this.cooldown <= 2 && this.cooldown != 0) {
				String msg = instance.getGameManager().getMain()
						.color("&c&lGhast Fireball &rin: &e" + this.cooldown + "s");
				getActionBarManager().setActionBar(player, "fireball.cooldown", msg, 2);
				if (gameTicks % 20 == 0) {
					this.cooldown--;
					
					if (this.cooldown <= 0) {
						player.getInventory().addItem(new ItemStack(Material.ARROW));
					}
				}
			}
			if (this.cooldown <= 0) {
				String msg = instance.getGameManager().getMain().color("&rYou can use &c&lGhast Fireball");
				getActionBarManager().setActionBar(player, "fireball.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		Entity e = event.getEntity();

		if (e instanceof Arrow) {
			for (Player gamePlayer : Bukkit.getOnlinePlayers()) //Play Ghast sound when shoot arrows
				gamePlayer.playSound(player.getLocation(), Sound.GHAST_SCREAM, 2, 2);
			
			if (this.cooldown == 0)
				this.cooldown = 2;
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.cooldown = 0;
		playerInv.setItem(0,
				ItemHelper.addEnchant(ItemHelper.addEnchant(
						ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_FIRE, 1),
						Enchantment.DURABILITY, 10000), Enchantment.ARROW_KNOCKBACK, 1));
		playerInv.setItem(1,
				ItemHelper.addEnchant(
						ItemHelper.addEnchant(new ItemStack(Material.GHAST_TEAR), Enchantment.DAMAGE_ALL, 2),
						Enchantment.KNOCKBACK, 1));
		playerInv.setItem(2, new ItemStack(Material.ARROW));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

	@Override
	public ClassType getType() {
		return ClassType.Ghast;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.BOW), ChatColor.GRAY + "Ghast Bow",
						ChatColor.GRAY + "", ChatColor.YELLOW + ""), Enchantment.ARROW_FIRE, 1),
				Enchantment.ARROW_INFINITE, 1), Enchantment.DURABILITY, 10000), Enchantment.ARROW_KNOCKBACK, 1);
		return item;
	}

}
