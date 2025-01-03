package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;

public class AnvilClass extends BaseClass {
	private boolean used = false;
	private int cooldownSec;
	private int num = 0;
	private int stompAbilityCooldown = 10000;

	public AnvilClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				Material.IRON_BLOCK,
				null,
				"373638",
				6,
				"Anvil"
		);
	}

	public ClassType getType() {
		return ClassType.Anvil;
	}

	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	public ItemStack getAttackWeapon() {
		ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1);
		ItemMeta meta = sword.getItemMeta();
		meta.spigot().setUnbreakable(true);
		sword.setItemMeta(meta);
		return sword;
	}

	public void SetNameTag() {
	}

	public void SetItems(Inventory playerInv) {
		anvil.startTime = System.currentTimeMillis() - 100000;
		this.used = false;
		this.num = 0;
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.ANVIL),
				this.instance.getGameManager().getMain().color("&e&lGoomba Stomp!"), new String[] { "" + ChatColor.RESET
						+ ChatColor.GRAY + "Right click in air to slam down on your opponents!" }));
	}

	public void Tick(int gameTicks) {
		if (this.used && this.player.isOnGround()) {
			this.used = false;
			List<Entity> players = this.player.getNearbyEntities(3.0D, 1.0D, 3.0D);
			for (Entity entity : players) {
				if (entity instanceof Player) {
					Player gamePlayer = (Player) entity;
					if (gamePlayer != this.player) {
						gamePlayer.setVelocity((new Vector(0, 1, 0)).multiply(0.5D));
						EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
								EntityDamageEvent.DamageCause.MAGIC, this.num);
						gamePlayer.damage(this.num, this.player);
						this.num = 0;
					}
				}
			}
			for (Player gamePlayer : this.instance.players) {
				this.player.playEffect(this.player.getLocation(), Effect.TILE_BREAK, 1);
			}
			player.getWorld().playSound(this.player.getLocation(), Sound.ANVIL_LAND, 1.0F, 1.0F);
		}
		if (this.instance.classes.containsKey(this.player)
				&& this.instance.classes.get(this.player).getType() == ClassType.Anvil
				&& this.instance.classes.get(this.player).getLives() > 0) {
			this.cooldownSec = (this.stompAbilityCooldown - this.anvil.getTime()) / 1000 + 1;
			if (this.anvil.getTime() < this.stompAbilityCooldown) {
				String msg = "" + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + "Goomba Stomp "
						+ ChatColor.RESET + "regenerates in: " + ChatColor.YELLOW + this.cooldownSec + "s";
				getActionBarManager().setActionBar(player, "anvil.cooldown", msg, 2);
			} else {
				String msg = "" + ChatColor.RESET + "You can use " + ChatColor.YELLOW + ChatColor.BOLD + "Goomba Stomp";
				getActionBarManager().setActionBar(player, "anvil.cooldown", msg, 2);
			}
		}
	}

	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.ANVIL
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
			if (this.anvil.getTime() < this.stompAbilityCooldown) {
				int seconds = (this.stompAbilityCooldown - this.anvil.getTime()) / 1000 + 1;
				event.setCancelled(true);
				this.player.sendMessage(
						"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your Goomba Stomp is still on cooldown for "
								+ ChatColor.YELLOW + seconds + " more seconds ");
			} else {
				this.anvil.restart();
				stompAbility();
				this.used = true;
			}
	}

	private void stompAbility() {
		if (this.player.isOnGround()) {
			this.player.sendMessage(ChatColorHelper.color("&c&l(!) &rYou cannot use this on the ground!"));
			return;
		}
		int maxHeight = 18;
		int currentHeight = calculateFallHeight(this.player);
		this.num = (int) calculateDamage(currentHeight, maxHeight);
		applyStompEffects(this.player);
	}

	private int calculateFallHeight(Player player) {
		int y = 1;
		Block block = player.getLocation().getBlock();
		while (block.getType() == Material.AIR && block.getY() > 50) {
			y++;
			block = block.getRelative(BlockFace.DOWN);
		}
		return y;
	}

	private double calculateDamage(int currentHeight, int maxHeight) {
		double heightRatio = currentHeight / maxHeight;
		double maxDamage = 19.0D;
		double damage = maxDamage * heightRatio;
		return Math.min(damage, maxDamage);
	}

	private void applyStompEffects(Player player) {
		player.setVelocity((new Vector(0.0D, -1.5D, 0.0D)).multiply(1.0D));
		player.playEffect(player.getLocation(), Effect.TILE_BREAK, 1);
	}
}
