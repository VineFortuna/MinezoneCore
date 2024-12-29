package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
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

public class GhastClass extends BaseClass {

	private int cooldown = 0;

	public GhastClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY4OGU2MTY0MmEwYjY4NjQzZjRiYTM2OTJmZTIwNjYyMmI0ZDlhN2QzOTY1YmEwYmUxMzI5YzIxMzJkIn19fQ==",
				"FFFFFF",
				6,
				"Ghast"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
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
			if (this.cooldown == 0) {
				player.getWorld().playSound(player.getLocation(), Sound.GHAST_SCREAM, 0.8f, 1);
				this.cooldown = 2;
			} else if (this.cooldown > 0) {
				event.setCancelled(true);
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.cooldown = 0;
		playerInv.setItem(0,
				ItemHelper.addEnchant(
						ItemHelper.addEnchant(new ItemStack(Material.GHAST_TEAR), Enchantment.DAMAGE_ALL, 2),
						Enchantment.KNOCKBACK, 1));

		ItemStack bow = new ItemStack(Material.BOW);
		bow.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
		bow.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		bow.addUnsafeEnchantment(Enchantment.DURABILITY, 10000);
		playerInv.setItem(1, bow);
		playerInv.setItem(35, new ItemStack(Material.ARROW));
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
		ItemStack item = ItemHelper.setUnbreakable(ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.BOW), ChatColor.GRAY + "Ghast Bow",
						ChatColor.GRAY + "", ChatColor.YELLOW + ""), Enchantment.ARROW_FIRE, 1),
				Enchantment.ARROW_INFINITE, 1), Enchantment.ARROW_KNOCKBACK, 1));
		return item;
	}

}
