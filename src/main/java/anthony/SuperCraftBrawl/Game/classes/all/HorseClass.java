package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class HorseClass extends BaseClass {
	private final ItemStack weapon;
	private final ItemStack jumpItem;
	private final Ability jumpAbility = new Ability("&6&lJump", 3, player);
	private static final double JUMP_ABILITY_HEIGHT = 1.6;
	private final PotionEffect absorption = new PotionEffect(PotionEffectType.ABSORPTION, 9999999 * 20, 0, false, false);
	private final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1, false, true);
	private final PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 8 * 20, 1, false, true);
	private final PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 12 * 20, 0, false, true);
	private final ItemStack goldenCarrotItem;
	private final ItemStack goldenAppleItem;
	private final ItemStack notchAppleItem;

	public HorseClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDJlYjk2N2FiOTRmZGQ0MWE2MzI1ZjEyNzdkNmRjMDE5MjI2ZTVjZjM0OTc3ZWVlNjk1OTdmYWZjZjVlIn19fQ",
				"4E321B",
				"4E321B",
				"300E06",
				6,
				"Horse"
		);

		String absorptionString = "&6&oAbsorption &e" + (absorption.getAmplifier() + 1);
		String speedString = "&b&oSpeed &e" + (speed.getAmplifier() + 1) + " &7for &e" + speed.getDuration() / 20 + "s";
		String strengthString = "&4&oStrength &e" + (strength.getAmplifier() + 1) + " &7for &e" + strength.getDuration() / 20 + "s";
		String resistanceString = "&f&oResistance &e" + (resistance.getAmplifier() + 1) + " &7for &e" + resistance.getDuration() / 20 + "s";

				// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.HAY_BLOCK),
				"&6&lHay Bale",
				"",
				"&7On kill, receive one of 3 treats:",
				"&7▶ " + speedString,
				"&7▶ " + strengthString,
				"&7▶ " + resistanceString,
				"",
				"&7All treats give " + absorptionString
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3); // Sharpness 3
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1); // Knockback 1

		// Jump Ability
		jumpItem = ItemHelper.setDetails(new ItemStack(Material.SADDLE),
				jumpAbility.getAbilityNameRightClickMessage(),
				"&7Jump high in the air",
				"",
				jumpAbility.getOnGroundItemMessage()
		);

		// Treat ability
			// Speed Treat
		goldenCarrotItem = ItemHelper.setDetails(new ItemStack(Material.GOLDEN_CARROT),
				"&b&lSpeed Treat",
				"&7▶ " + speedString,
				"&7▶ " + absorptionString
		);

			// Strength treat
		goldenAppleItem = ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE),
				"&4&lStrength Treat",
				"&7▶ " + strengthString,
				"&7▶ " + absorptionString
		);

			// Resistance treat
		notchAppleItem = ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE),
				"&f&lResistance Treat",
				"&7▶ " + resistanceString,
				"&7▶ " + absorptionString
		);
		notchAppleItem.setDurability((short) 1);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		jumpAbility.updateActionBar(player, this);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		// Resetting Jump Ability CD
		jumpAbility.getCooldownInstance().reset();

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, jumpItem);

		giveRandomTreat(player);
	}

	@Override
	public void classesEvent(Player damagerPlayer, BaseClass baseClass) {
		if (isPlayerAlive()) {
			giveRandomTreat(damagerPlayer);
			damagerPlayer.sendMessage(ChatColorHelper.color("&2&l(!) &r&eYou got rewarded with a special treat"));
		}
	}

	private void giveRandomTreat(Player player) {
		Random random = new Random();
		int randomNumber = random.nextInt(100);

		// Percentage chances for each treat
		int carrotPercentage = 33;
		int goldenApplePercentage = 33;
		int notchApplePercentage = 33;

		// Determining treat based on the number range
		if (randomNumber < carrotPercentage) {
			player.getInventory().addItem(goldenCarrotItem);
		} else if (randomNumber < carrotPercentage + goldenApplePercentage) {
			player.getInventory().addItem(goldenAppleItem);
		} else {
			player.getInventory().addItem(notchAppleItem);
		}
	}

	@SuppressWarnings("deprecation") // isOnGround() method
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				// JUMP ABILITY
				if (item.equals(jumpItem)) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
						// If ability is on cooldown
						if (!jumpAbility.isReady()) return;
						// If ability is available
						else {
							// If player is not on the ground
							if (!player.isOnGround()) {
								jumpAbility.sendCustomMessage(jumpAbility.getOnGroundChatMessage());
							}
							// If player is on the ground
							else {
								// Setting cooldown
								jumpAbility.use();
								// Jump Ability logic
								player.setVelocity(new Vector(0, JUMP_ABILITY_HEIGHT, 0));
								// Playing sound
								SoundManager.playSoundToAll(player, Sound.HORSE_ANGRY, 1, 1);
							}
						}
					}
				}
				// EATING CARROT TREAT ITEM
				if (item.getType().equals(goldenCarrotItem.getType()) && (item.getItemMeta().equals(goldenCarrotItem.getItemMeta()))) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

						if (player.getFoodLevel() != 20) {
							return;
						}
						player.setFoodLevel(19);
						Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
							player.setFoodLevel(20);
						}, 20L);

					}
				}
			}
		}
	}

	/**
	 * Listen to when a player consume an item
	 * Cancel the effects of golden apples
	 * Add the wanted effects to the player
	 */
	@Override
	public void onConsumingItem(PlayerItemConsumeEvent event) {
		ItemStack item = event.getItem();
		ItemMeta itemMeta = item.getItemMeta();

		if (!itemMeta.getDisplayName().toLowerCase().contains("treat")) return;
		// Canceling item effects
		event.setCancelled(true);

		if (item.isSimilar(goldenCarrotItem)) handleGoldenCarrot();
		if (item.isSimilar(goldenAppleItem)) handleGoldenApple();
		if (item.isSimilar(notchAppleItem)) handleNotchApple();
	}

	private void handleGoldenCarrot() {
		player.getInventory().removeItem(goldenCarrotItem);
		player.addPotionEffect(absorption, true);
		player.addPotionEffect(speed);
	}

	private void handleGoldenApple() {
		player.getInventory().removeItem(goldenAppleItem);
		player.addPotionEffect(absorption, true);
		player.addPotionEffect(strength);
	}

	private void handleNotchApple() {
		player.getInventory().removeItem(notchAppleItem);
		player.addPotionEffect(absorption, true);
		player.addPotionEffect(resistance, true);
	}

	@Override
	public ClassType getType() {
		return ClassType.Horse;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}