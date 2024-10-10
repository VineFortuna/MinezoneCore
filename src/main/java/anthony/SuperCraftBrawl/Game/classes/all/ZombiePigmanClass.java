package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ZombiePigmanClass extends BaseClass {

	public ZombiePigmanClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.ZombiePigman;
	}

	public ItemStack makeGreen(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.GREEN);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY5OGEzY2ZkZjhjMTNlZTY2MzQxNDBmOTQ1YjcxZDJlNDg4ZmY0ODVlMTBjMzNhZTI1ODIxZDgyZDg0OGE3MyJ9fX0=";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeGreen(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGreen(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGreen(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.GOLD_SWORD), Enchantment.KNOCKBACK, 2),
				Enchantment.DURABILITY, 1000);
		return item;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		for (Entity e : player.getWorld().getEntities())
			if (e instanceof PigZombie)
				if (e.getName().contains(player.getName()))
					e.remove();

		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.createMonsterEgg(EntityType.PIG_ZOMBIE, 5,
				instance.getGameManager().getMain().color("&2&lZombiePigman Pokeball")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.MONSTER_EGG) {
			ItemMeta meta = item.getItemMeta();

			if (meta != null && meta.getDisplayName().contains("ZombiePigman")) {
				int amount = item.getAmount();
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
						PigZombie en = (PigZombie) player.getWorld().spawnCreature(hitLoc, EntityType.PIG_ZOMBIE);
						EntityEquipment e = ((CraftLivingEntity) en).getEquipment();
						e.setItemInHand(new ItemStack(Material.GOLD_SWORD));
						en.setCustomName(
								"" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "ZombiePigman");
						en.setAngry(true);
						player.playSound(player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 1, 1);
						en.setTarget(instance.getNearestPlayer(player, 100, 100, 100));
					}

				}, ItemHelper.createMonsterEgg(EntityType.PIG_ZOMBIE, 1));
				instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
						player.getLocation().getDirection().multiply(2.0D));
			}
		}
	}

}
