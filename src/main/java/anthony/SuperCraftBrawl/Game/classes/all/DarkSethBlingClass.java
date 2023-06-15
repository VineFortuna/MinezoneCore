package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class DarkSethBlingClass extends BaseClass implements Listener {

	public boolean usedTp = false;

	public DarkSethBlingClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.4;
		instance.getManager().getMain().getServer().getPluginManager().registerEvents(this,
				instance.getManager().getMain());
	}

	public ItemStack makeNavy(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.NAVY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("SethBling");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeNavy(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeNavy(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeNavy(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.usedTp = false; // To reset each life
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.COMMAND),
						"" + ChatColor.RED + ChatColor.BOLD + "Item Stealer", "",
						ChatColor.GRAY + "Steal an item from one of your opponents"));
		playerInv.setItem(2,
				ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
						"" + ChatColor.RED + ChatColor.BOLD + "Item Teleporter", "",
						ChatColor.GRAY + "Teleport to a recent lightning drop"));
	}

	private Player getRandomPlayer(Player cant) {
		ArrayList<Player> cloned = new ArrayList<>(instance.players);
		cloned.remove(cant);
		Random r = new Random();
		return cloned.get(r.nextInt(cloned.size()));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack plItem = event.getItem();

		if (plItem != null) {
			if (plItem.getType() == Material.COMMAND
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				Random rand = new Random();
				Player target = this.getRandomPlayer(player);

				ItemStack item5 = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						"" + ChatColor.RED + ChatColor.BOLD + "Slowness", "");
				Potion pot5 = new Potion(3);
				pot5.setType(PotionType.SLOWNESS);
				pot5.setSplash(true);
				pot5.apply(item5);

				ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						"" + ChatColor.YELLOW + ChatColor.BOLD + "Instant Heal");
				Potion pot = new Potion(1);
				pot.setType(PotionType.INSTANT_HEAL);
				pot.setSplash(true);
				pot.apply(item);

				ItemStack item2 = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						"" + ChatColor.GREEN + ChatColor.BOLD + "Speed Pot");
				Potion pot2 = new Potion(1);
				pot2.setType(PotionType.SPEED);
				pot2.setSplash(true);
				pot2.apply(item2);

				ItemStack broom = ItemHelper.setDetails(new ItemStack(Material.WHEAT, 4),
						instance.getManager().getMain().color("&0&lBroom"));

				ItemStack item6 = ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD, 1, (short) 250),
								"" + ChatColor.YELLOW + ChatColor.BOLD + "HAMMER", ChatColor.YELLOW + ""),
						Enchantment.KNOCKBACK, 10);
				item5.getDurability();

				ItemStack item7 = ItemHelper.setDetails(new ItemStack(Material.DIAMOND_HOE, 3, (short) 250),
						"" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Bazooka", ChatColor.YELLOW + "");
				item5.getDurability();

				ItemStack extraLife = ItemHelper.setDetails(new ItemStack(Material.PRISMARINE_SHARD),
						"" + ChatColor.RESET + ChatColor.BOLD + "Extra Life");
				ItemStack pearl = ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL),
						"" + ChatColor.RED + ChatColor.BOLD + "Teleporter");

				ItemStack item8 = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						"" + ChatColor.RED + ChatColor.BOLD + "Bomb");
				Potion pot8 = new Potion(1);
				pot8.setType(PotionType.INSTANT_DAMAGE);
				pot8.setSplash(true);
				pot8.apply(item8);

				ItemStack fireRes = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						"" + ChatColor.RED + ChatColor.BOLD + "Fire Resistance");
				Potion pot9 = new Potion(1);
				pot9.setType(PotionType.FIRE_RESISTANCE);
				pot9.apply(fireRes);

				ItemStack slowballs = ItemHelper.setDetails(new ItemStack(Material.SNOW_BALL, 8),
						"" + ChatColor.RED + ChatColor.BOLD + "Slowballs");

				ItemStack miniShield = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						"" + ChatColor.YELLOW + "Mini-Shield Potion");

				ItemStack blooper = ItemHelper.setDetails(new ItemStack(Material.RABBIT_FOOT),
						instance.getManager().getMain().color("&6&lBlooper"));
				ItemStack nuke = ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.TNT, 3),
						instance.getManager().getMain().color("&4&lNuke")), Enchantment.DAMAGE_ALL, 1);
				ItemStack bomb = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						instance.getManager().getMain().color("&4&lBomb"));
				ItemStack instagib = ItemHelper.setDetails(new ItemStack(Material.GOLD_HOE, 5, (short) 250),
						"" + ChatColor.GREEN + ChatColor.ITALIC + "Instagib", ChatColor.YELLOW + "");
				instagib.getDurability();

				Potion pot100 = new Potion(1);
				pot100.setType(PotionType.INSTANT_DAMAGE);
				pot100.setSplash(true);
				PotionMeta meta = (PotionMeta) bomb.getItemMeta();
				meta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 100, 1000), true);
				bomb.setItemMeta(meta);
				pot100.apply(bomb);

				Potion pot1000 = new Potion(1);
				pot1000.setType(PotionType.INSTANT_HEAL);
				pot1000.setSplash(true);
				PotionMeta potMeta = (PotionMeta) item.getItemMeta();
				potMeta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 0, 1), true);
				item.setItemMeta(potMeta);
				pot1000.apply(item);

				Potion speedPot = new Potion(1);
				speedPot.setType(PotionType.SPEED);
				speedPot.setSplash(true);
				PotionMeta speedMeta = (PotionMeta) item2.getItemMeta();
				speedMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1), true);
				item2.setItemMeta(speedMeta);
				speedPot.apply(item2);

				ItemStack bigShield = ItemHelper.setDetails(new ItemStack(Material.WATER_BUCKET),
						instance.getManager().getMain().color("&eShield Potion"));

				List<ItemStack> items = Arrays.asList(new ItemStack(Material.GOLDEN_APPLE),
						/*
						 * ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1), ""
						 * + ChatColor.BLACK + ChatColor.BOLD + "Notch Apple"),
						 */
						item5, item7, item7, item, item2, item5, item5, item2, item7,
						new ItemStack(Material.GOLDEN_APPLE), item6, item, extraLife, item,
						new ItemStack(Material.MILK_BUCKET), new ItemStack(Material.MILK_BUCKET),
						new ItemStack(Material.MILK_BUCKET), blooper, blooper, blooper, blooper, nuke, nuke, nuke, nuke,
						nuke, bomb, pearl, pearl, miniShield, miniShield, slowballs, slowballs, slowballs, fireRes,
						fireRes, bigShield, instagib, instagib, instagib, broom, broom);

				if (!this.doesPlayerContainItems(target.getInventory(), items)) {
					player.sendMessage(instance.getManager().getMain()
							.color("&2&l(!) &rNo item was found at this player! Please try again."));
					return;
				}
				ArrayList<Integer> slots = new ArrayList<Integer>();
				Inventory inv = target.getInventory();
				for (int i = 0; i < inv.getSize(); i++)
					if (inv.getItem(i) != null && items.contains(inv.getItem(i)))
						slots.add(i);

				if (slots.isEmpty())
					Bukkit.getLogger().severe("Something went wrong!");

				int i = rand.nextInt(slots.size());
				ItemStack skeppy = inv.getItem(slots.get(i));
				inv.clear(slots.get(i));
				slots.clear();

				player.getInventory().addItem(skeppy);
				player.sendMessage(instance.getManager().getMain()
						.color("&2&l(!) &rYou were given a &e" + skeppy.getItemMeta().getDisplayName()));
				target.sendMessage(instance.getManager().getMain().color("&2&l(!) &rWhoops! Your &e"
						+ skeppy.getItemMeta().getDisplayName() + " &ritem was stolen by &e" + player.getName()));
				if (plItem.getAmount() == 1)
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				else
					plItem.setAmount(plItem.getAmount() - 1);
			} else if (plItem.getType() == Material.NETHER_STAR
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (instance.recentDrop == null) {
					player.sendMessage(
							instance.getManager().getMain().color("&c&l(!) &rThere are no drops you can pickup!"));
				} else if (!(player.isOnGround())) {
					player.sendMessage(instance.getManager().getMain()
							.color("&c&l(!) &rYou have to be on the ground to use this!"));
				} else {
					player.teleport(instance.recentDrop);
					player.getInventory().remove(player.getItemInHand());
					player.sendMessage(instance.getManager().getMain().color(
							"&2&l(!) &rYou teleported to the recently spawned item! (Could be good or bad luck idk lol)"));
					this.usedTp = true;
					EntityDamageEvent damageEvent = new EntityDamageEvent(player, DamageCause.PROJECTILE, 6.5);
					instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
					player.damage(4.5, player);
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.DarkSethBling;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.COAL_BLOCK),
						"" + ChatColor.DARK_GRAY + "Dark Command Block"), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
		return item;
	}

	private boolean doesPlayerContainItems(Inventory inv, List<ItemStack> items) {
		for (ItemStack item : items) {
			if (inv.contains(item)) {
				return true;
			}
		}
		return false;
	}

}