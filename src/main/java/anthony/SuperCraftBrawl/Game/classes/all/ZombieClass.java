package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class ZombieClass extends BaseClass {

	public ZombieClass(GameInstance instance, Player player) {
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
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.ZOMBIE.ordinal());
		playerEquip.setHelmet(getHelmet(playerskull));
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
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.ROTTEN_FLESH),
				"" + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "Zombie Army"));
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;

		Random rand = new Random();
		int chance = rand.nextInt(6);

		if (chance == 0) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (instance.duosMap != null)
					if (instance.team.get(p).equals(instance.team.get(player)))
						return;

				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0, true));
			}
		} else if (chance == 1) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (instance.duosMap != null)
					if (instance.team.get(p).equals(instance.team.get(player)))
						return;

				p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, true));
			}
		} else if (chance == 2) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (instance.duosMap != null)
					if (instance.team.get(p).equals(instance.team.get(player)))
						return;

				p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0, true));
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.ROTTEN_FLESH
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			event.setCancelled(true);
			player.getInventory().remove(Material.ROTTEN_FLESH);
			for (int i = 0; i < 3; i++) {
				Zombie zombie = (Zombie) player.getWorld().spawnCreature(player.getLocation(), EntityType.ZOMBIE);
				zombie.setBaby(true);
				zombie.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Baby Zombie");
				zombie.setTarget(instance.getNearestPlayer(player, 100, 100, 100));
			}
			player.sendMessage(instance.getGameManager().getMain().color("&e&l(!) &rSpawning army of &eBaby Zombies!"));
			player.playSound(player.getLocation(), Sound.ZOMBIE_HURT, 1, 1);
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Zombie;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.IRON_SPADE),
						instance.getGameManager().getMain().color("&2&lGrave Digger")), Enchantment.DAMAGE_ALL, 1),
				Enchantment.KNOCKBACK, 1);
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}
}
