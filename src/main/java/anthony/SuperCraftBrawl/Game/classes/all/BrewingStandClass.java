package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class BrewingStandClass extends BaseClass {

	private int cooldownSec;
	private int cooldownDuration = 10000;
	private boolean used = false;

	public BrewingStandClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.15;
	}

	public ItemStack makeYellowArmour(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.YELLOW);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		// String texture =
		// "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDg5ZmVmMmVmNGY4MWVlYzZkMDdiYWVmNmM0YWVhNzRlNDQyZGNlNzJhMDFkZTk2NGViY2JhYzhhOGQ4MmM3NyJ9fX0=";
		// ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");

		// playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeYellowArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeYellowArmour(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeYellowArmour(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	/*
	 * This function sets the items for the player's kit when they respawn
	 */
	@Override
	public void SetItems(Inventory playerInv) {
		this.used = false; // Reset each life Brewing Stand usage
		alexBrewingStand.startTime = System.currentTimeMillis() - 100000; // To reset cooldown each life
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.BARRIER), ChatColor.RED + "No potion item yet!"));
		playerInv.setItem(2, getBrewingStand());
		playerInv.setItem(8,
				ItemHelper.setDetails(new ItemStack(Material.BARRIER), ChatColor.RED + "No blaze powder yet!"));
	}

	/*
	 * This function runs through every tick of a game but when doing the ticks
	 * modulo 100, it will run through this code every 5 seconds of the game
	 */
	@Override
	public void Tick(int gameTicks) {
		checkIfEmpty();
		this.cooldownSec = (this.cooldownDuration - alexBrewingStand.getTime()) / 1000 + 1;
		cooldownActionBar(this.cooldownSec, this.cooldownDuration, alexBrewingStand, ClassType.BrewingStand,
				"brewingStand.cooldown", "Brewing Stand");
	}

	/*
	 * This function checks if the player's 2nd slot is empty and if so, add back
	 * the barrier till they get a new potion
	 */
	private void checkIfEmpty() {
		ItemStack item = player.getInventory().getItem(1);

		if (item == null || item.getType() == Material.AIR) {
			player.getInventory().setItem(1,
					ItemHelper.setDetails(new ItemStack(Material.BARRIER), ChatColor.RED + "No potion item yet!"));
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.BREWING_STAND_ITEM
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
						|| event.getAction() == Action.LEFT_CLICK_AIR
						|| event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				event.setCancelled(true);
				if (alexBrewingStand.getTime() < 10000) {
					int seconds = (10000 - alexBrewingStand.getTime()) / 1000 + 1;
					player.sendMessage(color("&c&l(!) &rYou can't brew for another &e" + seconds + "s"));
				} else {
					if (player.getInventory().getItem(8).getType() == Material.BARRIER)
						return;

					ItemStack blazePowder = player.getInventory().getItem(8);
					this.alexBrewingStand.restart();
					this.used = true;
					player.sendMessage(color("&r&l(!) &rBrewing potion..."));
					player.getInventory().setItem(8, ItemHelper.setDetails(new ItemStack(Material.BARRIER),
							ChatColor.RED + "No blaze powder yet!"));
					new BukkitRunnable() {
						@Override
						public void run() {
							if (used)
								potionsToGive(blazePowder);
						}
					}.runTaskLater(instance.getGameManager().getMain(), 80L);
				}
			}
		}
	}

	/**
	 * This function gives a potion to the player depending on how much blaze powder
	 * they have in their inventory 8th slot
	 */
	private void potionsToGive(ItemStack blazePowder) {
		ItemStack potion = null;
		Potion pot = new Potion(1);
		int amount = blazePowder.getAmount();

		if (blazePowder != null) {
			if (amount == 1) {
				potion = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						instance.getGameManager().getMain().color("&cSlowness II &7(15 sec)"));
				pot.setSplash(true);
				PotionMeta meta = (PotionMeta) potion.getItemMeta();
				pot.setType(PotionType.SLOWNESS);
				meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 300, 1), true);
				potion.setItemMeta(meta);
			} else if (amount == 2) {
				potion = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						instance.getGameManager().getMain().color("&6Jump IV &7(20 sec)"));
				pot.setSplash(true);
				PotionMeta meta = (PotionMeta) potion.getItemMeta();
				pot.setType(PotionType.JUMP);
				meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, 400, 3), true);
				potion.setItemMeta(meta);
			} else if (amount == 3) {
				potion = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						instance.getGameManager().getMain().color("&2Speed II &7(20 sec)"));
				pot.setSplash(true);
				PotionMeta meta = (PotionMeta) potion.getItemMeta();
				pot.setType(PotionType.SPEED);
				meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1), true);
				potion.setItemMeta(meta);
			} else if (amount == 4) {
				potion = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						instance.getGameManager().getMain().color("&bRegen III &7(5 sec)"));
				pot.setSplash(true);
				PotionMeta meta = (PotionMeta) potion.getItemMeta();
				pot.setType(PotionType.REGEN);
				meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 110, 2), true);
				potion.setItemMeta(meta);
			} else if (amount == 5) {
				potion = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						instance.getGameManager().getMain().color("&eStrength I &7(5 sec)"));
				pot.setSplash(true);
				PotionMeta meta = (PotionMeta) potion.getItemMeta();
				pot.setType(PotionType.STRENGTH);
				meta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 110, 0), true);
				potion.setItemMeta(meta);
			}

			pot.apply(potion);
			player.getInventory().setItem(1, potion);
			this.used = false; // Reset Brewing Stand usage
		}
	}

	/**
	 * When a person with Alex class hits a player, they will receive Brewing items
	 * but a random item from a pool of different items
	 */
	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0) // If player is dead/spectator don't give items
			return;

		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (instance.duosMap != null)
				if (instance.team.get(p).equals(instance.team.get(player)))
					return;

			Random r = new Random();
			int chance = r.nextInt(100);

			if (chance >= 0 && chance < 70) {
				ItemStack slot9 = player.getInventory().getItem(8);
				ItemStack slot2 = player.getInventory().getItem(1);

				if (slot2.getType() == Material.BARRIER) {
					if (slot9 != null && slot9.getType() == Material.BARRIER) {
						player.getInventory().setItem(8, new ItemStack(Material.BLAZE_POWDER));
					} else {
						if (slot9.getAmount() < 5)
							player.getInventory().addItem(new ItemStack(Material.BLAZE_POWDER));
					}
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.BrewingStand;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1);
		ItemMeta meta = sword.getItemMeta();
		meta.spigot().setUnbreakable(true);
		sword.setItemMeta(meta);
		return sword;
	}

	private ItemStack getBrewingStand() {
		ItemStack brewingStand = ItemHelper.setDetails(new ItemStack(Material.BREWING_STAND_ITEM),
				color("&eBrewing Stand"), "", color("&rHit players to obtain Brewing items"),
				color("&rthen right click to get a potion"), "", color("&7 - 1 Powder: &eSlowness II (15 sec)"),
				color("&7 - 2 Powder: &eJump IV (20 sec"), color("&7 - 3 Powder: &eSpeed II (20 sec)"),
				color("&7 - 4 Powder: &eRegen III (5 sec)"), color("&7 - 5 Powder: &eStrength I (5 sec)"));
		return brewingStand;
	}

	private String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}

}
