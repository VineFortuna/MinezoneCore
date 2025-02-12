package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BedrockClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack lavaItem;
	private final Ability invincibilityAbility = new Ability("&8&lInvincibility", INVINCIBILITY_COOLDOWN, player);
	private final Ability lavaAbility = new Ability("&6&lLava", player);
	private static final double INVINCIBILITY_DURATION = 3;
	private static final double INVINCIBILITY_COOLDOWN = 12;
	private static final double LAVA_ABILITY_RANGE = 10;

	private ItemStack[] originalArmor;

	private BukkitRunnable lava;
	private BukkitRunnable bedrock;
	private List<BlockState> blockList;
	private List<Block> blockList2;

	public BedrockClass(GameInstance instance, Player player) {
		super(instance, player);
		this.blockList = new ArrayList<>();
		this.blockList2 = new ArrayList<>();
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZjY2ZlNTA5NmEzMzViOWFiNzhhYjRmNzc4YWU0OTlmNGNjYWI0ZTJjOTVmYTM0OTIyN2ZkMDYwNzU5YmFhZiJ9fX0=",
				"404040",
				6,
				"Bedrock"
		);

		// Weapon
		String durationDisplay = ItemHelper.formatDouble(INVINCIBILITY_DURATION);

		weapon = ItemHelper.setDetails(
				new ItemStack(Material.BEDROCK),
				invincibilityAbility.getAbilityNameRightClickMessage(),
				"",
				"&7Be invincible to all damage",
				"&7You can not hit other players",
				"",
				"&7Duration: &a" + durationDisplay + "s"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3); // Sharpness 3
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1); // Knockback 1

		// Lava
		String rangeDisplay = ItemHelper.formatDouble(LAVA_ABILITY_RANGE);

		lavaItem = ItemHelper.setDetails(new ItemStack(
				Material.LAVA_BUCKET),
				lavaAbility.getAbilityNameRightClickMessage(),
				"&7Pour lava on your opponents",
				"",
				"&7Range: &a" + rangeDisplay + " &7blocks"
		);
	}

	public void modifyArmorDuringInvincibility() {
		// Getting original armor
		originalArmor = player.getInventory().getArmorContents();

		// Invincibility armor
		// Chestplate
		ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.WHITE, "&rBedrock Chestplate");

		// Leggings
		ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.WHITE, "&rBedrock Leggings");

		// Boots
		ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.WHITE, "&rBedrock Boots");

		// Setting Armor
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);
	}

	public void restoreOriginalArmor() {
		player.getInventory().setArmorContents(originalArmor);
	}

	// Setting items
	@Override
	public void SetItems(Inventory playerInv) {
		this.bedrockInvincibility = false; // To reset each life
		invincibilityAbility.getCooldownInstance().reset();

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, lavaItem);
	}

	@Override
	public void Tick(int gameTicks) {
		// ActionBar
		if (isPlayerAlive()) invincibilityAbility.updateActionBar(player, this);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				// BEDROCK INVINCIBILITY ABILITY
				if (item.equals(weapon)) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
						// If ability is on cooldown
						if (invincibilityAbility.isReady()) {
							// Setting cooldown
							invincibilityAbility.use();
							// Sending return message
							String durationDisplay = ItemHelper.formatDouble(INVINCIBILITY_DURATION);
							invincibilityAbility.sendCustomMessage("&d&l(!) &rYou are now invincible for &e" + durationDisplay + " &rseconds");
							invincibilityAbility.sendCustomMessage("&d&l(!) &rYou are also unable to hit other players");
							// Setting invincibility
							BaseClass bc = instance.classes.get(player);
							bc.bedrockInvincibility = true;

							// Playing sound
							SoundManager.playSoundToAll(player, Sound.ZOMBIE_INFECT, 1, 1);

							// Setting invincibility runnable
							if (bedrock == null) {
								bedrock = new BukkitRunnable() {
									// Setting invincibility/runnable duration
									int duration = (int) INVINCIBILITY_DURATION;
									boolean armorModified = false; // Flag: armor is modified

									@Override
									public void run() {
										// When invincibility is over
										if (duration == 0) {
											bedrock = null;
											this.cancel();
											bedrockInvincibility = false;

											// Restoring armor to original
											if (armorModified) {
												restoreOriginalArmor();
											}
										} else if (duration == 1) {
											// Playing sound 1s before invincibility is over
											SoundManager.playSoundToAll(player, Sound.ZOMBIE_UNFECT, 1, 1);
										} else {
											if (!armorModified) {
												// Modifying armor when invincibility starts
												modifyArmorDuringInvincibility();
												armorModified = true;
											}
										}

										duration--;
									}
								};
								bedrock.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
							}
						}
					}
				}
				// LAVA ABILITY
				if (item.equals(lavaItem)) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
						boolean foundPlayers = false;

						for (Entity entity : player.getWorld().getNearbyEntities(
								player.getLocation(),
								LAVA_ABILITY_RANGE,
								LAVA_ABILITY_RANGE,
								LAVA_ABILITY_RANGE
						)) {
							if (entity instanceof Player && !entity.equals(player)) {
								Player playerInRange = (Player) entity;
								if (!checkIfDead(playerInRange, instance) && !instance.HasSpectator(playerInRange)) {
									useLavaAbility(playerInRange);
									foundPlayers = true;
								}
							}
						}
						if (foundPlayers) SoundManager.playSoundToPlayer(player, Sound.LAVA_POP, 1, 1);
						else player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
					}
				}
			}
		}
	}

	private void useLavaAbility(Player playerInRange) {
		player.getInventory().clear(player.getInventory().getHeldItemSlot());

		Block block = instance.getMapWorld().getBlockAt(
				playerInRange.getLocation().getBlockX(),
				playerInRange.getLocation().getBlockY() + 1,
				playerInRange.getLocation().getBlockZ());
		blockList.add(block.getState());
		blockList2.add(block);
		block.setType(Material.LAVA);

		if (lava == null) {
			lava = new BukkitRunnable() {
				int ticks = 1;

				@Override
				public void run() {
					if (ticks == 0 || instance.state == GameState.ENDED) {
						this.cancel();
					}
					ticks--;
				}

				@Override
				public void cancel() {
					int count = 0;
					for (Block blocks : blockList2) {
						BlockState blockState = blocks.getState();
						blockState.setType(blockList.get(count).getType());
						blockState.setData(blockList.get(count).getData());
						blockState.update(true);
						count++;
					}
					lava = null;
				}
			};
			lava.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
		}
	}

	@Override
	public void GameEnd() {
		if (lava != null) {
			this.bedrockInvincibility = false;
			lava.cancel();
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Bedrock;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
