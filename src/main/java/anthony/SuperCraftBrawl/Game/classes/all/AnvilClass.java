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
						instance.getGameManager().getMain().color("&e&lGoomba Stomp!"),
						"" + ChatColor.RESET + ChatColor.GRAY + "Right click in air to slam down on your opponents!"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Tick(int gameTicks) {
		if (used) {
			if (player.isOnGround()) {
				this.used = false;
				List<Entity> players = player.getNearbyEntities(3.0, 1.0, 3.0);
				for (Entity entity : players) {
					if (entity instanceof Player) {
						Player gamePlayer = (Player) entity;
						if (gamePlayer != player) {
							gamePlayer.setVelocity(new Vector(0, 1, 0).multiply(0.5D));
//							if (this.num >= 15)
//								this.num = 15;

							EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer, DamageCause.MAGIC, this.num);
//							instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
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
			this.cooldownSec = (stompAbilityCooldown - anvil.getTime()) / 1000 + 1;

			if (anvil.getTime() < stompAbilityCooldown) {
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
				if (anvil.getTime() < stompAbilityCooldown) {
					int seconds = (stompAbilityCooldown - anvil.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Your Goomba Stomp is still on cooldown for " + ChatColor.YELLOW + seconds
							+ " more seconds ");
				} else {
					anvil.restart();

					stompAbility();
					this.used = true;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void stompAbility() {
		if ((player.isOnGround())) {
			player.sendMessage(ChatColorHelper.color("&c&l(!) &rYou cannot use this on the ground!"));
			return;
		}

		int maxHeight = 20;
		int currentHeight = calculateFallHeight(player);

		this.num = (int) calculateDamage(currentHeight, maxHeight);
		applyStompEffects(player);
	}

	private int calculateFallHeight(Player player) {
		int y = 1;
		Block block = player.getLocation().getBlock();

		// Loop while blockType is Air
		while (block.getType() == Material.AIR && block.getY() > 50) {
			y++;
			block = block.getRelative(BlockFace.DOWN);
		}

		return y;
	}

	private double calculateDamage(int currentHeight, int maxHeight) {
			double heightRatio = (double) currentHeight / maxHeight;
			double maxDamage = 19.0; // Set the maximum damage to 6 hearts
			double damage = maxDamage * heightRatio;

			// Debug messages
			System.out.println("Height Ratio: " + heightRatio);
			System.out.println("Calculated Damage: " + damage);

			return Math.min(damage, maxDamage); // Cap the damage at the maximum value
	}

	private void applyStompEffects(Player player) {
		player.setVelocity(new Vector(0, -1.5, 0).multiply(1.0D));
		player.playEffect(player.getLocation(), Effect.TILE_BREAK, 1);
		// Perform any additional logic related to the stomp ability
	}
}
