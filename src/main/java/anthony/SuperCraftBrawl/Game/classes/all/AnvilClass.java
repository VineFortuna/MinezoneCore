package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.List;

import anthony.SuperCraftBrawl.ChatColorHelper;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class AnvilClass extends BaseClass {

	private boolean used = false;
	private int cooldownSec;
	private int num = 0;
	private int stompAbilityCooldown = 10 * 1000;

	public AnvilClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.Anvil;
	}

	public ItemStack makeGray(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.GRAY);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(new ItemStack(Material.IRON_BLOCK));
		playerEquip.setChestplate(makeGray(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGray(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGray(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1);
		ItemMeta meta = sword.getItemMeta();
		meta.spigot().setUnbreakable(true);
		sword.setItemMeta(meta);
		return sword;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
//		anvil.startTime = 15000;
		this.used = false; // To reset each life
		this.num = 0; // Same here
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.ANVIL),
						instance.getManager().getMain().color("&e&lGoomba Stomp!"),
						"" + ChatColor.RESET + ChatColor.GRAY + "Right click in air to slam down on your opponents!"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Tick(int gameTicks) {
		if (used) {
			if (player.isOnGround()) {
				this.used = false;
				List<Entity> players = player.getNearbyEntities(3.0, 1.0, 3.0);
				for (Entity e : players) {
					if (e instanceof Player) {
						Player gamePlayer = (Player) e;
						if (gamePlayer != player) {
							gamePlayer.setVelocity(new Vector(0, 1, 0).multiply(0.5D));
							if (this.num >= 15)
								this.num = 15;

							EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer, DamageCause.VOID,
									this.num);
							instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
							gamePlayer.damage(this.num, player);
							this.num = 0; // To reset
						}
					}
				}
				for (Player gamePlayer : instance.players) {
					gamePlayer.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1);
					player.playEffect(player.getLocation(), Effect.TILE_BREAK, 1);
				}
			}
		}

		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Anvil
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (15000 - anvil.getTime()) / 1000 + 1;

			if (anvil.getTime() < 15000) {
				String msg = "" + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + "Goomba Stomp "
						+ ChatColor.RESET + " regenerates in: " + ChatColor.YELLOW + cooldownSec + "s";
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			} else {
				String msg = "" + ChatColor.RESET + "You can use " + ChatColor.YELLOW + ChatColor.BOLD + "Goomba Stomp";
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

		if (item != null && item.getType() == Material.ANVIL) {
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (anvil.getTime() < 15000) {
					int seconds = (15000 - anvil.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Your Goomba Stomp is still on cooldown for " + ChatColor.YELLOW + seconds
							+ " more seconds ");
				} else {
					anvil.restart();
					if (!(player.isOnGround())) {
						int y = 1;
						Block b = player.getLocation().getBlock();
						boolean c = false;

						while (c == false) {
							Location loc = new Location(player.getWorld(), b.getX(), b.getY() - y, b.getZ());
							Block b2 = loc.getBlock();

							if (b2.getY() <= 50)
								return;

							if (b2 != null && b2.getType() != null && b2.getType() == Material.AIR) {
								y++;
							} else {
								b = loc.getBlock();
								c = true;
								break;
							}
						}
						double maxHeight = 10.0; // Maximum height for maximum damage
						double heightRatio = (double) y / maxHeight;
						double damage = 20.0 * heightRatio;

						if (damage > 20.0) {
							damage = 20.0; // Cap the damage at 20.0
						}

						this.num = (int) Math.ceil(damage);
						player.setVelocity(new Vector(0, -1.5, 0).multiply(1.0D));
						player.playEffect(player.getLocation(), Effect.TILE_BREAK, 1);
						this.used = true;
					} else {
						player.sendMessage(
								instance.getManager().getMain().color("&c&l(!) &rYou cannot use this on the ground!"));
					}
				}
			}
		}
	}

}
