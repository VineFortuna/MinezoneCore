package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;

public class MagmaCubeClass extends BaseClass {

	public MagmaCubeClass(GameInstance instance, Player player) {
		super(instance, player);
		this.baseVerticalJump = 1.3;
	}

	@Override
	public ClassType getType() {
		return ClassType.MagmaCube;
	}

	public ItemStack makeBlack(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.BLACK);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("MagmaCube");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeBlack(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeBlack(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlack(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.DAMAGE_ALL, 1),
				Enchantment.KNOCKBACK, 1), Enchantment.DURABILITY, 999999);
		return item;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		for (Entity en : player.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(player.getName()))
					en.remove();
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.MONSTER_EGG, 7),
				instance.getManager().getMain().color("&e&lMagmaCube Pokeball")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.MONSTER_EGG
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				int amount = item.getAmount();

				if (amount > 0) {
					if (amount == 1)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else {
						amount--;
						item.setAmount(amount);
					}
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
							MagmaCube en = (MagmaCube) player.getWorld().spawnCreature(hitLoc, EntityType.MAGMA_CUBE);
							en.setSize(4);
							en.setCustomName(
									"" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "MagmaCube");
							EntityDamageEvent damageEvent = new EntityDamageEvent(player, DamageCause.PROJECTILE, 3.0);
							instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
							player.damage(3.0);
							player.sendMessage(instance.getManager().getMain()
									.color("&e&l(!) &rYou gave up some of your health to spawn a MagmaCube"));
						}

					}, new ItemStack(Material.MONSTER_EGG));
					instance.getManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				}
			}
		}
	}

}
