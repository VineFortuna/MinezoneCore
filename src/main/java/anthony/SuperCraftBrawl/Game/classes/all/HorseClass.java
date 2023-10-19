package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.ArrayList; 
import java.util.List;
import java.util.Random;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.classes.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import org.bukkit.util.Vector;

public class HorseClass extends BaseClass {
	private ItemStack weapon;
	private ItemStack saddle;
	private final Ability jumpAbility = new Ability("Jump Ability", 6, player);
	private final double jumpAbilityHeight = 1.6;
	// Creating Treats
	private List<ItemStack> treatsItemsList = new ArrayList<>();

	// Golden Carrot
	private ItemStack goldenCarrotTreat = ItemHelper.create(Material.GOLDEN_CARROT, "&9SPEED Treat");

	// Golden Apple Treat
	private ItemStack goldenAppleTreat = ItemHelper.create(Material.GOLDEN_APPLE, "&4STRENGTH Treat");

	// Enchanted Golden Apple Treat
	private ItemStack goldenEnchantedAppleTreat = ItemHelper.create(Material.GOLDEN_APPLE, "&6FIRE RESISTANCE Treat");


	public HorseClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;

		treatsItemsList.add(goldenCarrotTreat);
		treatsItemsList.add(goldenAppleTreat);
		goldenEnchantedAppleTreat.setDurability((short) 1);
		treatsItemsList.add(goldenEnchantedAppleTreat);

		// TODO Auto-generated constructor stub
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		// Head (helmet)
		ItemStack playerHead = ItemHelper.createSkullHeadPlayer(1, "kuba432110", "&6Horse Head");

		// Chestplate
		ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.ORANGE, "&6Horse Chestplate");
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Leggings
		ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.ORANGE, "&6Horse Leggings");

		// Boots
		ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.ORANGE, "&6Horse Boots");
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Setting Armor
		playerEquip.setHelmet(playerHead);
		playerEquip.setChestplate(chestplate);
		playerEquip.setLeggings(leggings);
		playerEquip.setBoots(boots);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		// Weapon
		ItemStack weapon = ItemHelper.create(Material.HAY_BLOCK, ChatColorHelper.color("&6Hay Bale"));
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3); // Sharpness 3
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1); // Knockback 1

		this.weapon = weapon;

		// Jump Ability
		ItemStack saddle = ItemHelper.create(Material.SADDLE, ChatColorHelper.color("&eJump Ability") + ChatColorHelper.color("&7Right click to do a high jump!"));

		this.saddle = saddle;

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, saddle);
	}

	@SuppressWarnings("deprecation") // isOnGround() method
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				// JUMP ABILITY
				if (item.equals(saddle)) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
						// If ability is on cooldown
						if (!jumpAbility.isReady()) {
							jumpAbility.sendPlayerRemainingCooldownChatMessage();
						}
						// If ability is available
						else {
							// If player is not on the ground
							if (!player.isOnGround()) {
								jumpAbility.sendPlayerCustomUseAbilityChatMessage("&c&l(!) &rYou have to be on the ground to use &6" + jumpAbility.getAbilityName());
							}
							// If player is on the ground
							else {
								// Setting cooldown
								jumpAbility.use();
								// Sending return message
								jumpAbility.sendPlayerUseAbilityChatMessage();
								// Jump Ability logic
								player.setVelocity(new Vector(0, jumpAbilityHeight, 0));

								// Playing sound
								for (Player gamePlayer : instance.players)
									gamePlayer.playSound(player.getLocation(), Sound.HORSE_ANGRY, 1, 1);
								
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void Death(PlayerDeathEvent e) {
		super.Death(e);

		if (player.equals(e.getEntity().getKiller())) {
			TreatGiver treatGiver = new TreatGiver(this, treatsItemsList);

			treatGiver.giveRandomTreat(player);

			player.sendMessage(ChatColorHelper.color("&2&l(!) &r&eYou got rewarded with a special treat"));
		}
	}

	/**
	 * Listen to when a player consume an item
	 * Used to listen to when the player eats the specific food items that are used as treats
	 * Cancel the effects of items i.e. golden apples
	 * Add the wanted effects to the player
	 */
	public void eatTreatItem(PlayerItemConsumeEvent e) {
		ItemStack item = e.getItem();

		// When Eating Treats
			// Golden Carrot
		if (item == goldenCarrotTreat) {
			e.setCancelled(true);

			List<PotionEffect> potionEffects = new ArrayList<>();

			// Speed 2 for 10 seconds
			potionEffects.add(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1, false, true));

			// Absorption 1 (2 hearts) for 1 minute
			potionEffects.add(new PotionEffect(PotionEffectType.ABSORPTION, 60 * 20, 0, false, false));

			player.addPotionEffects(potionEffects);

			potionEffects.clear();
		}
		// Golden Apple
		else if (item == goldenAppleTreat) {
			e.setCancelled(true);

			List<PotionEffect> potionEffects = new ArrayList<>();

			// Strength 1 for 6 seconds
			potionEffects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6 * 20, 0, false, true));

			// Absorption 1 (2 hearts) for 1 minute
			potionEffects.add(new PotionEffect(PotionEffectType.ABSORPTION, 60 * 20, 0, false, false));

			player.addPotionEffects(potionEffects);

			potionEffects.clear();
		}
		// Enchanted Golden Apple
		else if (item == goldenEnchantedAppleTreat) {
			e.setCancelled(true);

			List<PotionEffect> potionEffects = new ArrayList<>();

			// Fire Resistance for 1 minute
			potionEffects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60 * 20, 1, false, true));

			// Absorption 1 (2 hearts) for 1 minute
			potionEffects.add(new PotionEffect(PotionEffectType.ABSORPTION, 60 * 20, 0, false, false));

			player.addPotionEffects(potionEffects);

			potionEffects.clear();
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Horse;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}


	private class TreatGiver {
		private List<ItemStack> treatsItemsList;
		private int goldenCarrotPercentageChance = 50;
		private int goldenApplePercentageChance = 30;
		private int goldenAppleEnchantedPercentageChance = 20;
		private HorseClass horseClass;
		private ItemStack randomizedTreat;

		public TreatGiver(HorseClass horseClass, List<ItemStack> treatsItemsList) {
			this.horseClass = horseClass;
			this.treatsItemsList = treatsItemsList;
		}

		// Setting treats percentage chances
		public void setTreatItemsPercentages(int goldenCarrotPercentageChance, int goldenApplePercentageChance, int goldenAppleEnchantedPercentageChance) {
			this.goldenCarrotPercentageChance = goldenCarrotPercentageChance;
			this.goldenApplePercentageChance = goldenApplePercentageChance;
			this.goldenAppleEnchantedPercentageChance = goldenAppleEnchantedPercentageChance;
		}

		// Randomizing and giving treat logic
		public void giveRandomTreat(Player player) {
			Random random = new Random();
			int randomNumber = random.nextInt(100) + 1;

			int totalPercentageChance = goldenCarrotPercentageChance + goldenApplePercentageChance + goldenAppleEnchantedPercentageChance;
			int cumulativePercentageChance = 0;

			for (ItemStack item : treatsItemsList) {
				int percentageChance = 0;

				if (item.equals(treatsItemsList.get(0))) {
					percentageChance = goldenCarrotPercentageChance;
				} else if (item.equals(treatsItemsList.get(1))) {
					percentageChance = goldenApplePercentageChance;
				} else if (item.equals(treatsItemsList.get(2))) {
					percentageChance = goldenAppleEnchantedPercentageChance;
				}

				cumulativePercentageChance += percentageChance;

				// Checking witch treat was randomized
				if (randomNumber <= (cumulativePercentageChance * 100 / totalPercentageChance)) {
					// Adding randomized treat to the player inventory
					player.getInventory().addItem(item);
					randomizedTreat = item;
					// Exit the loop after an item has been given
					break;
				}
			}
		}
	}
}