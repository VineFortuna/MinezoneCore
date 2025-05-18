package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BrewingStandClass extends BaseClass {

	private ItemStack weapon;
	private ItemStack brewingItem;
	private ItemStack barrierItem;
	private final Ability brewAbility = new Ability("&e&lBrewing", 3, player);
	private final PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 16 * 20, 7, true);
	private final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 12 * 20, 1, true);
	private final PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 8 * 20, 0, true);
	private final PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 12 * 20, 0, false, true);
	private final PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 2, true);

	String potionDisplayNamePrefix;
	private boolean hasSentBrewedMessage;

	private BukkitRunnable runnable;

	public BrewingStandClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU0M2JiZDkwNTcxYjFlMzVhYTAzOWE1ZWJhZDY1ZjQxNDI3YzhiODg3MWRkZjc2NzU4MGYzYTViMTAyMmZiZiJ9fX0=",
				"FFB81A",
				"FFA236",
				"756B6D",
				6,
				"BrewingStand"
		);

		initializeItems();
	}

	private void initializeItems() {
		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.BLAZE_ROD),
				"&e&lBlaze Core"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Brewing Ability
		String jumpString = "&a&oJump &e" + (jump.getAmplifier() + 1) + " &7for &e" + jump.getDuration() / 20 + "s";
		String speedString = "&b&oSpeed &e" + (speed.getAmplifier() + 1) + " &7for &e" + speed.getDuration() / 20 + "s";
		String regenString = "&d&oRegeneration &e" + (regeneration.getAmplifier() + 1) + " &7for &e" + regeneration.getDuration() / 20 + "s";
		String strengthString = "&4&oStrength &e" + (strength.getAmplifier() + 1) + " &7for &e" + strength.getDuration() / 20 + "s";
		String resistanceString = "&f&oResistance &e" + (resistance.getAmplifier() + 1) + " &7for &e" + resistance.getDuration() / 20 + "s";

		brewingItem = ItemHelper.setDetails(
				new ItemStack(Material.BREWING_STAND_ITEM),
				brewAbility.getAbilityNameRightClickMessage(),
				"&7Hit enemies to gain blaze powder",
				"&7Use it to brew potions",
				"",
				"&71 Powder - " + jumpString,
				"&72 Powder - " + speedString,
				"&73 Powder - " + strengthString,
				"&74 Powder - " + resistanceString,
				"&75 Powder - " + regenString
		);

		// Barrier
		barrierItem = ItemHelper.setDetails(
				new ItemStack(Material.BARRIER),
				"&cNo blaze powder yet!"
		);
	}

	/*
	 * This function sets the items for the player's kit when they respawn
	 */
	@Override
	public void SetItems(Inventory playerInv) {
		brewAbility.getCooldownInstance().reset();
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, brewingItem);
		playerInv.setItem(8, barrierItem);
	}

	/*
	 * This function runs through every tick of a game but when doing the ticks
	 * modulo 100, it will run through this code every 5 seconds of the game
	 *
	 */
	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) {
//			if (runnable != null) runnable.cancel();
			return;
		}

		if (!brewAbility.isReady()) {
			int remainingTime = (int) (brewAbility.getCooldownInstance().getRemainingCooldownSeconds() + 1);

			String msg = ChatColorHelper.color( "&7Brewing " + potionDisplayNamePrefix + " &7in &e" + remainingTime + "s");
			getActionBarManager().setActionBar(player, "brewing.cooldown", msg, 2);

			hasSentBrewedMessage = false;
		} else {
			if (hasSentBrewedMessage) return;
			String msg = ChatColorHelper.color("&7Brewed " + potionDisplayNamePrefix);
			getActionBarManager().setActionBar(player, "brewing.cooldown", msg, 2);
			hasSentBrewedMessage = true;
		}
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (!brewAbility.isReady()) return;
		if (!(event.getEntity() instanceof Player)) return;
		checkToAddBlazePowder(event);
	}

	private void checkToAddBlazePowder(EntityDamageByEntityEvent event) {
		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
						&& heldItem != null
						&& heldItem.equals(weapon);

		if (!isWeaponMelee) return;

		addBlazePowder();
	}

	private void addBlazePowder() {
		ItemStack slot9 = player.getInventory().getItem(8);

		if (slot9 == null) return;

		if (slot9.getType() == Material.BARRIER) {
			player.getInventory().setItem(8, new ItemStack(Material.BLAZE_POWDER));
		} else {
			if (slot9.getAmount() < 5) {
				player.getInventory().addItem(new ItemStack(Material.BLAZE_POWDER));
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;

		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

		if (!(item.equals(brewingItem))) return;
		if (!brewAbility.isReady()) return;
		if (player.getInventory().getItem(8).getType() == Material.BARRIER) {
			brewAbility.sendCustomMessage("&c&l(!) &rNo blaze powder yet");
			return;
		}
		brewAbility.use();
		brewPotion();
	}

	private void brewPotion() {
		int blazePowderAmount = player.getInventory().getItem(8).getAmount();
		player.getInventory().setItem(8, barrierItem);
		potionDisplayNamePrefix = getDisplayNamePrefix(blazePowderAmount);

		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (!isPlayerAlive()) {
					cancel();
					return;
				}

				givePotion(blazePowderAmount);
			}
		};
		runnable.runTaskLater(instance.getGameManager().getMain(), (long) (brewAbility.getCooldownDurationSeconds() * 20L));
	}

	/**
	 * This function gives a potion to the player depending on how much blaze powder
	 * they have in their inventory 8th slot
	 */
	private void givePotion(int blazePowderAmount) {
		ItemStack potionItem = createPotion(blazePowderAmount);
		if (potionItem == null) return;
		player.getInventory().addItem(potionItem);
		SoundManager.playSoundToPlayer(player, Sound.SWIM, 1, 0.5f);
	}

	private ItemStack createPotion(int blazePowderAmount) {
		PotionEffect effect = getPotionEffect(blazePowderAmount);
		if (effect == null) return null;

		String displayNamePrefix = getDisplayNamePrefix(blazePowderAmount);
		return createPotionWithDynamicName(effect, displayNamePrefix);
	}

	private PotionEffect getPotionEffect(int blazePowderAmount) {
		switch (blazePowderAmount) {
			case 1: return jump;
			case 2: return speed;
			case 3: return strength;
			case 4: return resistance;
			case 5: return regeneration;
			default: return null;
		}
	}

	private String getDisplayNamePrefix(int blazePowderAmount) {
		switch (blazePowderAmount) {
			case 1: return "&a&lJump";
			case 2: return "&b&lSpeed";
			case 3: return "&4&lStrength";
			case 4: return "&f&lResistance";
			case 5: return "&d&lRegen";
			default: return "";
		}
	}

	private ItemStack createPotionWithDynamicName(PotionEffect effect, String displayNamePrefix) {
		// Dynamically generate the display name with amplifier and duration
		String displayName = displayNamePrefix + " " + getRomanNumeral(effect.getAmplifier() + 1) +
				" &7(" + (effect.getDuration() / 20) + " sec)";

		// Create the potion item with the dynamic display name
		return ItemHelper.setDetails(
				ItemHelper.createPotionItem(effect, true, true),
				displayName
		);
	}

	private String getRomanNumeral(int number) {
		switch (number) {
			case 1: return "I";
			case 2: return "II";
			case 3: return "III";
			case 4: return "IV";
			case 5: return "V";
			case 6: return "VI";
			case 7: return "VII";
			case 8: return "VIII";
			case 9: return "IX";
			case 10: return "X";
			default: return "" + number; // Fallback for numbers > 10
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.BrewingStand;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	private ItemStack getBrewingStand() {
		ItemStack brewingStand = ItemHelper.setDetails(new ItemStack(Material.BREWING_STAND_ITEM),
				color("&eBrewing Stand"),
				"",
				color("&rHit players to obtain Brewing items"),
				color("&rthen right click to get a potion"),

				color("&7 - 1 Powder: &eSlowness II (15 sec)"),
				color("&7 - 2 Powder: &eJump 8 (10 sec)"),
				color("&7 - 3 Powder: &eSpeed II (20 sec)"),
				color("&7 - 4 Powder: &eRegen III (5 sec)"),
				color("&7 - 5 Powder: &eResistance I (15 sec)"),
				color("&7 - 6 Powder: &eStrength I (5 sec)"));
		return brewingStand;
	}

	private String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
