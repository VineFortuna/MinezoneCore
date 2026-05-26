package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HunterClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack bloodLustItem;
	private final ItemStack dashItem;
	private static final int MAX_BLOODLUST_AMOUNT = 8;
	private final PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20, 0, true);
	private final PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15 * 20, 1, false, true);

	private int bloodLust = 0;

	public HunterClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGI0YWNjM2YyNmU1NGI5MDA0MWZlOTVjNGU2MmI4ZTdiNjNhZWVmN2E5ZmMzOWNkOWJjN2U4ZWI5MWEzMzQ3MCJ9fX0=",
				"F0FBFB",
				6,
				"Hunter"
		);

		// Weapon
		String strengthString = "&4&oStrength &e" + (strength.getAmplifier() + 1) + " &7for &e" + strength.getDuration() / 20 + "s";
		String resistanceString = "&f&oResistance &e" + (resistance.getAmplifier() + 1) + " &7for &e" + resistance.getDuration() / 20 + "s";

		List<String> lore = new ArrayList<>();
		lore.add("&7At &a" + MAX_BLOODLUST_AMOUNT + " &7Bloodlust, gain one of two potions:");
		lore.add(strengthString);
		lore.add(resistanceString);

		weapon = ItemHelper.setDetails(
				new ItemStack(Material.GOLD_SWORD),
				"&6&lHunter's Sword",
				"",
				"&7Hit enemies to gain Bloodlust",
				"",
				lore.get(0),
				lore.get(1),
				lore.get(2)
		);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
		ItemHelper.setUnbreakable(weapon);

		// Dash Item
		dashItem = ItemHelper.setDetails(
				new ItemStack(Material.FEATHER),
				"&6&lDash",
				"&7A fast escape or ambush"
		);
		ItemHelper.setGlowing(dashItem, true);

		// BloodLust Item
		bloodLustItem = ItemHelper.setDetails(
				new ItemStack(Material.REDSTONE),
				"&4&lBloodLust",
				lore.get(0),
				lore.get(1),
				lore.get(2)
		);
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (!(event.getEntity() instanceof Player)) return;

		checkToAddBloodLust(event);
	}

	private void checkToAddBloodLust(EntityDamageByEntityEvent event) {
		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
						&& heldItem != null
						&& heldItem.equals(weapon);

		if (!isWeaponMelee) return;

		bloodLust++;
		bloodLustItem.setAmount(bloodLust);
		player.getInventory().setItem(8, bloodLustItem);

		if (bloodLust < 8) return;
		player.getInventory().remove(Material.REDSTONE);

		Random rand = new Random();
		int chance = rand.nextInt(2);

		ItemStack potion;
		String potionString;

		if (chance == 0) {
			potion = createPotionWithDynamicName(strength, "&4&lStrength");
			potionString = "&4Strength";
		} else {
			potion = createPotionWithDynamicName(resistance, "&f&lResistance");
			potionString = "&1Resistance";
		}

		player.getInventory().addItem(potion);
		bloodLust = 0;

		SoundManager.playSuccessfulHit(player);
		player.sendMessage(ChatColorHelper.color(
				"&2&l(!) &rYour Bloodlust rewarded you with a " + potionString + "&7 potion"
		));
	}

	private ItemStack createPotionWithDynamicName(PotionEffect effect, String displayNamePrefix) {
		// Dynamically generate the display name with amplifier and duration
		String displayName = displayNamePrefix + " " + ItemHelper.getRomanNumeral(effect.getAmplifier() + 1) +
				" &7(" + (effect.getDuration() / 20) + " sec)";

		// Create the potion item with the dynamic display name
		return ItemHelper.setDetails(
				ItemHelper.createPotionItem(effect, true, false),
				displayName
		);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		bloodLust = 0;
		hunterDash = true;
		player.sendMessage("" + ChatColor.BOLD + "===============================");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RED + ChatColor.BOLD
				+ ChatColor.UNDERLINE + "  Hunter:");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  Hit players to gain Bloodlust");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "===============================");
		player.getInventory().setItem(0, weapon);
		player.getInventory().setItem(1, dashItem);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null) {
			if (item.getType() == Material.FEATHER
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (hunterDash) {
					double boosterStrength = 1.6;
					for (Player gamePlayer : instance.players)
						gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
					Vector vel = player.getLocation().getDirection().multiply(boosterStrength);
					player.setVelocity(vel);
					hunterDash = false;
					ItemStack dash = ItemHelper.setDetails(new ItemStack(Material.FEATHER),
							instance.getGameManager().getMain().color("&b&lDash"),
							instance.getGameManager().getMain().color("&7A quick escape or attack"));
					player.getInventory().setItem(1, dash);
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You already used your dash");
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Hunter;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
