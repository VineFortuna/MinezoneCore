package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;

public class BlazeClass extends BaseClass {

	private int cooldownSec;

	public BlazeClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.Blaze;
	}

	public ItemStack makeGreen(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.ORANGE);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU5Njk4MmMzZGJhN2Y2NzRjZmI5M2RkMzllMTcxM2E4ZWMxMjk5MDQ3M2FjYmZkODVhMThmZDkwOTE4ZGE0MSJ9fX0=";
		ItemStack skull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(skull);
		playerEquip.setChestplate(makeGreen(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGreen(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGreen(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		blazeRod.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_INFINITE, 1),
						Enchantment.DURABILITY, 1000));
		playerInv.setItem(2, ItemHelper.setDetails(new ItemStack(Material.MOB_SPAWNER),
				instance.getGameManager().getMain().color("&6&lBlaze Army &7(Right Click)")));
		playerInv.setItem(3, new ItemStack(Material.ARROW));
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntityType() == EntityType.ARROW) {
			event.setCancelled(true);
			SmallFireball fireball = player.launchProjectile(SmallFireball.class);
			fireball.setIsIncendiary(false);
		}
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Blaze
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (15000 - blazeRod.getTime()) / 1000 + 1;

			if (blazeRod.getTime() < 15000) {
				String msg = instance.getGameManager().getMain()
						.color("&e&lBlaze Rod &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "blaze.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &e&lBlaze Rod");
				getActionBarManager().setActionBar(player, "blaze.cooldown", msg, 2);
			}
		}
	}

	private void spawnBlazes(Location loc, float yaw) {
		List<Blaze> blazes = new ArrayList<>();
		player.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &rYou spawned &6&lBlaze Army"));
		Blaze b = (Blaze) player.getWorld().spawnCreature(loc, EntityType.BLAZE);
		b.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Blaze Army");
		blazes.add(b);

		if (yaw > 45 && yaw <= 135) {
			loc = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(),
					player.getLocation().getZ() + 1);
			b = (Blaze) player.getWorld().spawnCreature(loc, EntityType.BLAZE);
			b.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Blaze Army");
			blazes.add(b);
			loc = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(),
					player.getLocation().getZ() - 1);
			b = (Blaze) player.getWorld().spawnCreature(loc, EntityType.BLAZE);
			b.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Blaze Army");
			blazes.add(b);
		} else if (yaw <= -45 && yaw > -135) {
			loc = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(),
					player.getLocation().getZ() + 1);
			b = (Blaze) player.getWorld().spawnCreature(loc, EntityType.BLAZE);
			b.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Blaze Army");
			blazes.add(b);
			loc = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(),
					player.getLocation().getZ() - 1);
			b = (Blaze) player.getWorld().spawnCreature(loc, EntityType.BLAZE);
			b.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Blaze Army");
			blazes.add(b);
		} else {
			loc = new Location(player.getWorld(), player.getLocation().getX() + 1, player.getLocation().getY(),
					player.getLocation().getZ());
			b = (Blaze) player.getWorld().spawnCreature(loc, EntityType.BLAZE);
			b.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Blaze Army");
			blazes.add(b);
			loc = new Location(player.getWorld(), player.getLocation().getX() - 1, player.getLocation().getY(),
					player.getLocation().getZ());
			b = (Blaze) player.getWorld().spawnCreature(loc, EntityType.BLAZE);
			b.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Blaze Army");
			blazes.add(b);
		}

		//After 25 seconds, remove all the blazes that were spawned
		int delay = 25 * 20;
		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), new Runnable() {
			@Override
			public void run() {
				for (Blaze b : blazes)
					if (b != null && !(b.isDead()))
						b.remove();
			}
		}, delay);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.MOB_SPAWNER) {
			event.setCancelled(true);
			if (player.getGameMode() != GameMode.SPECTATOR) {
				Location loc = player.getLocation();
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
				float yaw = loc.getYaw();
				spawnBlazes(loc, yaw);
			}
		} else if (item != null && item.getType() == Material.BLAZE_ROD
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (blazeRod.getTime() < 15000) {
				int seconds = (15000 - blazeRod.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your Blaze Rod is on cooldown for "
						+ ChatColor.YELLOW + seconds + " more seconds ");
			} else {
				blazeRod.restart();
				SmallFireball fireball = player.launchProjectile(SmallFireball.class);
				fireball.setIsIncendiary(false);
				player.setVelocity(new Vector(0, 1.6, 0).multiply(1.0D));
				fireball = player.launchProjectile(SmallFireball.class);
				fireball.setIsIncendiary(false);

				for (int i = 0; i < 4; i++) {
					for (Player gamePlayer : instance.players)
						gamePlayer.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 1);
					blazeEvent(player);
				}
			}
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(ItemHelper.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.BLAZE_ROD), "",
								instance.getGameManager().getMain().color("&7Right click to shoot up"),
								instance.getGameManager().getMain().color("&7& shoot fireballs!")),
						Enchantment.DAMAGE_ALL, 1), Enchantment.FIRE_ASPECT, 1), Enchantment.KNOCKBACK, 2);
		return item;
	}

	private void blazeEvent(Player player) { // Fireball launch when right click blaze rod
		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), new Runnable() {
			@Override
			public void run() {
				SmallFireball fireball = player.launchProjectile(SmallFireball.class);
				fireball.setIsIncendiary(false);
			}
		}, 10L);
	}
}