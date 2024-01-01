package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

import java.util.Random;

public class HunterClass extends BaseClass {

	private int count = 0;

	public HunterClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
	}

	@Override
	public ClassType getType() {
		return ClassType.Hunter;
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

		meta.setOwner("chopchopchopchop");
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
	public ItemStack getAttackWeapon() {
		ItemStack sword = ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.GOLD_SWORD),
				instance.getGameManager().getMain().color("&c&lFighter Sword")), Enchantment.KNOCKBACK, 2);
		ItemMeta meta = sword.getItemMeta();
		meta.spigot().setUnbreakable(true);
		sword.setItemMeta(meta);
		return sword;
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		Random rand = new Random();
		int chance = rand.nextInt(100);
		int chance2 = rand.nextInt(2);

		if (chance >= 0 && chance <= 60) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (instance.duosMap != null)
					if (instance.team.get(p).equals(instance.team.get(player)))
						return;

				if (instance.getGameManager().spawnProt.containsKey(p)
						|| instance.getGameManager().spawnProt.containsKey(player))
					return;

				BaseClass bc = instance.classes.get(player);
				if (bc != null && bc.getLives() <= 0)
					return;

				count++;
				player.getInventory()
						.addItem(ItemHelper.setDetails(new ItemStack(Material.REDSTONE),
								instance.getGameManager().getMain().color("&c&lBlood Lust"), "",
								instance.getGameManager().getMain().color("&7Get 5 of this to get an OP potion!")));

				if (count >= 5) {
					player.getInventory().remove(Material.REDSTONE);

					if (chance2 == 0) {
						player.sendMessage(instance.getGameManager().getMain()
								.color("&2&l(!) &rYour 5 Blood Lust rewarded you with a Strength I potion"));
						ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
								instance.getGameManager().getMain().color("&eStrength Potion &7(5 sec)"));
						Potion pot = new Potion(1);
						pot.setType(PotionType.STRENGTH);
						pot.setSplash(true);
						PotionMeta meta = (PotionMeta) item.getItemMeta();
						meta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 110, 0), true);
						item.setItemMeta(meta);
						pot.apply(item);
						player.getInventory().addItem(item);
						count = 0;
					} else {
						player.sendMessage(instance.getGameManager().getMain()
								.color("&2&l(!) &rYour 5 Blood Lust rewarded you with a Resistance I potion"));
						ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
								instance.getGameManager().getMain().color("&eResistance Potion &7(15 sec)"));
						Potion pot = new Potion(1);
						pot.setType(PotionType.FIRE_RESISTANCE);
						pot.setSplash(true);
						PotionMeta meta = (PotionMeta) item.getItemMeta();
						meta.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 0), true);
						item.setItemMeta(meta);
						pot.apply(item);
						player.getInventory().addItem(item);
						count = 0;
					}
				}
			}
		}
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		count = 0;
		player.sendMessage("" + ChatColor.BOLD + "===============================");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RED + ChatColor.BOLD
				+ ChatColor.UNDERLINE + "  Hunter:");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  Hit players to gain Blood Lust");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "===============================");
		player.getInventory().setItem(0, this.getAttackWeapon());
		player.getInventory().setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.FEATHER),
						instance.getGameManager().getMain().color("&b&lDash"),
						instance.getGameManager().getMain().color("&7A quick escape or attack")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.FEATHER
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				double boosterStrength = 1.6;
				for (Player gamePlayer : instance.players)
					gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
				Vector vel = player.getLocation().getDirection().multiply(boosterStrength);
				player.setVelocity(vel);
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
			}
		}
	}

}
