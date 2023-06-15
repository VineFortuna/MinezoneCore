package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class EndermanClass extends BaseClass {

	private ItemStack stick;
	private ItemStack newItem = null;
	private boolean used = false;
	private int cooldownSec;

	public EndermanClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	public ItemStack makePurple(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.PURPLE);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("_Enderman");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makePurple(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makePurple(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makePurple(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		stick = ItemHelper.setDetails(new ItemStack(Material.STICK),
				instance.getManager().getMain().color("&c&lBlock Pickup"), "",
				instance.getManager().getMain().color("&7Right click to grab the block you're standing on"));
		used = false;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL, 10),
				"" + ChatColor.BLACK + ChatColor.BOLD + "Teleporters"));
		playerInv.setItem(2, stick);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getLives() > 0
				&& instance.classes.get(player).getType() == ClassType.Enderman)
			if (!(player.getInventory().contains(this.getAttackWeapon())))
				player.getInventory().setItem(0, this.getAttackWeapon()); // If some rare chance the player throws away
																			// their melee

		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Enderman
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (10000 - pearlTimer.getTime()) / 1000 + 1;

			if (pearlTimer.getTime() < 10000) {
				String msg = instance.getManager().getMain()
						.color("&c&lTeleporter &rregenerates in: &e" + this.cooldownSec + "s");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			} else {
				String msg = instance.getManager().getMain().color("&rYou can use &c&lTeleporter");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.EYE_OF_ENDER
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
						|| event.getAction() == Action.LEFT_CLICK_AIR
						|| event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			event.setCancelled(true);
		} else if (item != null && item.getType() == Material.STICK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (used == true) {
				player.sendMessage(
						instance.getManager().getMain().color("&c&l(!) &rYou already have a block in your inventory!"));
				return;
			} else if (!(player.isOnGround())) {
				player.sendMessage(
						instance.getManager().getMain().color("&r&l(!) &rYou must be on the ground to use this item!"));
				return;
			}

			if (enderman.getTime() < 10000) {
				int seconds = (10000 - enderman.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Your stick is still on cooldown for " + ChatColor.YELLOW + seconds + " more seconds ");
			} else {
				enderman.restart();
				Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(),
						player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());

				if (block.getType() != null && block.getType() != Material.AIR) {
					newItem = new ItemStack(block.getType(), 1);
					player.getInventory().addItem(newItem);
					player.sendMessage(
							instance.getManager().getMain().color("&r&l(!) &rYou picked up &e1 " + block.getType()));
					used = true;
				} else {
					enderman.restart();
					player.sendMessage(instance.getManager().getMain()
							.color("&c&l(!) &rYou tried picking up air or a null block. Please try again."));
				}
			}
		} else if (item != null && item.getType() == newItem.getType()) {
			Vector direction = player.getLocation().getDirection();
			player.getInventory().remove(newItem);
			ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
				@Override
				public void onHit(Player hit) {
					if (instance.duosMap != null)
						if (instance.team.get(hit).equals(instance.team.get(player)))
							return;

					player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
					Location loc = hit.getLocation();
					Vector v = direction;
					EntityDamageEvent damageEvent = new EntityDamageEvent(hit, DamageCause.PROJECTILE, 4.5);
					instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
					hit.damage(4.5, player);
					v.setY(1.0);
					hit.setVelocity(v);
					for (Player gamePlayer : instance.players)
						gamePlayer.playSound(loc, Sound.CHICKEN_EGG_POP, 1, 1);
				}

			}, new ItemStack(newItem));
			instance.getManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
					player.getLocation().getDirection().multiply(2.0D));

			newItem = null;
			used = false;
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Enderman;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER),
						"" + ChatColor.RED + ChatColor.BOLD + "Enderman Soul"), Enchantment.DAMAGE_ALL, 4),
				Enchantment.KNOCKBACK, 1);
		return item;
	}

}
