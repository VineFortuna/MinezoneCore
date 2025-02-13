package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class WitchClass extends BaseClass {

	ItemStack weaknessItem;
	ItemStack damageItem;
	ItemStack poisonItem;
	ItemStack slownessItem;
	ItemStack[] potionsList;

	private boolean used = false;
	private ItemStack weapon = ItemHelper.addEnchant(
			ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WHEAT, 4),
					"" + ChatColor.BLACK + ChatColor.BOLD + "Magic Broom"), Enchantment.DAMAGE_ALL, 3),
			Enchantment.KNOCKBACK, 1);
	private int cooldown = 0;
	private int regenBrooms = 0;

	public WitchClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlMTNkMTg0NzRmYzk0ZWQ1NWFlYjcwNjk1NjZlNDY4N2Q3NzNkYWMxNmY0YzNmODcyMmZjOTViZjlmMmRmYSJ9fX0=",
				"47236B",
				6,
				"Witch"
		);

		weaknessItem = ItemHelper.setDetails(
				ItemHelper.createPotionItem(
						PotionType.WEAKNESS,
						3,
						5,
						true,
						true,
						false),
				"&8&lWeakness"
		);

		damageItem = ItemHelper.setDetails(
				ItemHelper.createPotionItem(
						PotionType.INSTANT_DAMAGE,
						0,
						0,
						true,
						true,
						false),
				"&4&lDamage"
		);

		poisonItem = ItemHelper.setDetails(
				ItemHelper.createPotionItem(
						PotionType.POISON,
						0,
						15,
						true,
						true,
						false),
				"&2&lPoison"
		);

		slownessItem = ItemHelper.setDetails(
		ItemHelper.createPotionItem(
				PotionType.SLOWNESS,
				0,
				5,
				true,
				true,
				false),
				"&3&lSlowness"
		);

		potionsList = new ItemStack[]{weaknessItem, damageItem, poisonItem, slownessItem};
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof ThrownPotion) {
			ThrownPotion potion = (ThrownPotion) event.getEntity();

			if (potion.getShooter() instanceof Player) {
				Player player = (Player) potion.getShooter();
				if (player.getInventory().getHeldItemSlot() == 1) {
					// Adjust the velocity of the potion
					Vector velocity = potion.getVelocity();
					potion.setVelocity(velocity.multiply(1.3)); // Increase the multiplier to adjust the throwing
																// distance
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Witch
				&& instance.classes.get(player).getLives() > 0) {
			if (player.getInventory().getItem(1) == null || player.getInventory().getItem(1).getType() == Material.AIR) {
				player.getInventory().setItem(1, new ItemStack(Material.BARRIER));
				this.cooldown = 3;
			}
			
			if (gameTicks % 20 == 0 && this.cooldown != 0) {
				this.cooldown--;
				
				if (this.cooldown == 0) {
					Random rand = new Random();
					int randomNum = rand.nextInt(potionsList.length);
					player.getInventory().setItem(1, new ItemStack(potionsList[randomNum]));
				}
			}
			
			if (gameTicks % 20 == 0 && regenBrooms != 4) {
				if (player.isOnGround()) {
					BaseClass bc = instance.classes.get(player);
					if (bc != null && bc.getLives() <= 0)
						return;
					
					regenBrooms++;
					player.getInventory().setItem(0,
							ItemHelper.addEnchant(ItemHelper.addEnchant(
									ItemHelper.setDetails(new ItemStack(Material.WHEAT, regenBrooms),
											"" + ChatColor.BLACK + ChatColor.BOLD + "Magic Broom"),
									Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1));
				}
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.used = false; // To reset each life
		this.cooldown = 0; // Also same
		this.regenBrooms = 4; // ALSO SAME LMFAO!!!

		playerInv.setItem(0, this.getAttackWeapon());
		Random rand = new Random();
		int randomNum = rand.nextInt(potionsList.length);
		playerInv.setItem(1, new ItemStack(potionsList[randomNum]));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.WHEAT
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			ItemMeta meta = item.getItemMeta();

			if (meta != null && meta.getDisplayName().contains("Magic")) {
				int amount = item.getAmount();
				if (amount > 1) {
					event.setCancelled(true);
					player.setVelocity(new Vector(0, 1, 0).multiply(1.2D));
					this.used = true;
					amount--;
					item.setAmount(amount);
					regenBrooms = amount;
					
					for (Player gamePlayer : instance.players)
						gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Witch;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

}
