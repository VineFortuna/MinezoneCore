package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class SilverfishClass extends BaseClass {
	private ItemStack weapon;
	private ItemStack wallItem;
	private int cooldownSec = 0;
	private int wallCooldown = 10 * 1000;

	public SilverfishClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5MWRhYjgzOTFhZjVmZGE1NGFjZDJjMGIxOGZiZDgxOWI4NjVlMWE4ZjFkNjIzODEzZmE3NjFlOTI0NTQwIn19fQ==",
				"717E81",
				6,
				"Silverfish"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Silverfish
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (wallCooldown - wallAbility.getTime()) / 1000 + 1;

			if (wallAbility.getTime() < wallCooldown) {
				String msg = instance.getGameManager().getMain()
						.color("&7Wall Ability &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "wall.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &7Wall Ability");
				getActionBarManager().setActionBar(player, "wall.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		wallAbility.startTime = System.currentTimeMillis() - 100000;
		ItemStack weapon = ItemHelper.create(Material.IRON_HOE, ChatColorHelper.color("&7Silverfish Weapon"));
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
		this.weapon = weapon;

		// Wall Ability
		ItemStack wallItem = ItemHelper.setDetails(new ItemStack(Material.SMOOTH_BRICK),
				"&7&lWall Ability",
				"&fCreate a wall of silverfishes",
				"&e&nLeft Click&r&7 to place a wall &ofurther",
				"&e&nRight Click&r&7 to place a wall &ocloser");
		wallItem.setDurability((short) 2);

		this.wallItem = wallItem;

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, wallItem);

		for (Entity en : player.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(player.getName()))
					en.remove();
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item != null) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				// WALL ABILITY
				if (item.equals(wallItem)) {
					// Check Right or Left Button Click
					if (action == Action.RIGHT_CLICK_AIR
							|| action == Action.RIGHT_CLICK_BLOCK
							|| action == Action.LEFT_CLICK_AIR
							|| action == Action.LEFT_CLICK_BLOCK) {
						// If ability is on cooldown
						if (wallAbility.getTime() < wallCooldown) {
							int seconds = (wallCooldown - wallAbility.getTime()) / 1000 + 1;
							event.setCancelled(true);
							player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your Wall Ability is still on cooldown for " + ChatColor.YELLOW + seconds + "s");
						} else {
							wallAbility.restart();
							SilverfishWall createWall = new SilverfishWall(3, 3, player, 2, 0.2);
							// Wall logic
								// When right or left click on block
							if (action == Action.RIGHT_CLICK_BLOCK) {
								createWall.buildWallClickedBlock(event.getClickedBlock());

							}    // When right click on air
							else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
								createWall.buildWallClickedAir(2);

							}	// When left click on air
							else if (event.getAction() == Action.LEFT_CLICK_AIR) {
								createWall.buildWallClickedAir(6);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Silverfish;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	private class SilverfishWall {
		int wallHeight; // Platform Height
		int wallWidth; // Platform Width
		Player player;
		Location playerLocation;
		Vector playerDirection;
		double startBreakingWallTime;
		double delayBetweenBreakingBlocks;
		List<Location> wallLocations = new ArrayList<>();
		private List<Block> originalBlocks = new ArrayList<>();
		private List<Material> replacedBlocks = new ArrayList<Material>();
		List<Integer> randomizedIndexes = new ArrayList<>();

		public SilverfishWall(int wallHeight, int wallWidth, Player player, double startBreakingWallTime, double delayBetweenBreakingBlocks) {
			this.player = player;
			this.playerLocation = player.getLocation();
			this.playerDirection = playerLocation.getDirection();
			this.wallHeight = wallHeight;
			this.wallWidth = wallWidth;
			this.startBreakingWallTime = startBreakingWallTime;
			this.delayBetweenBreakingBlocks = delayBetweenBreakingBlocks;
		}

		// Right-Click Block Action
		public void buildWallClickedBlock(Block clickedBlock) {
			Location clickedLocation = clickedBlock.getLocation();

			// Creating wall
			buildWall(clickedLocation);
			// Breaking wall
			breakWall();
		}

		// Right-Click Air Action
		public void buildWallClickedAir(int distanceFromPlayer) {

			double yawRadians = Math.toRadians(playerLocation.getYaw());
			double xOffset = -distanceFromPlayer * Math.sin(yawRadians);
			double zOffset = distanceFromPlayer * Math.cos(yawRadians);
			Location wallLocation = playerLocation.clone().add(xOffset, -1, zOffset);

			buildWall(wallLocation);
			breakWall();
		}

		/**
		 * Checks the player yaw to determine which of the 4 directions the wall is going to be built.
		 * Returns an int from 1 to 4.
		 *
		 */
		public int getPlayerSightDirection() {
			float yaw = player.getLocation().getYaw();

			// Normalizing yaw to the range of 0 to 180
			if (yaw < 0) {
				if (yaw < -180) {
					yaw += 360;
				} else {
					yaw += 180;
				}
			} else if (yaw > 180) {
				yaw -= 180;
			}

			// Getting absolute value of the normalized yaw
			float absoluteYaw = Math.abs(yaw);

			// Returning 1 of the 4 possible wall building directions
			if ((absoluteYaw >= 0 && absoluteYaw < 30) || (absoluteYaw >= 150 && absoluteYaw <= 180)) {
				return 1;
			} else if ((absoluteYaw >= 30 && absoluteYaw < 60)) {
				return 2;
			} else if ((absoluteYaw >= 60 && absoluteYaw < 120)) {
				return 3;
			} else if ((absoluteYaw >= 120 && absoluteYaw < 150)) {
				return 4;
			} else
				return 0;
		}

		/**
		 * Randomize one of the possible blocks a silverfish can spawn from.
		 * Possible blocks: Cobblestone, Stone, Stone Brick and Mossy, Cracked and Chiseled stone bricks.
		 * Replaced blocks: Air, Grass, Long Grass, Vine, Water, Stationary Water, Lava, Stationary Lava.
		 * Sets the block type and data to the one randomized.
		 * Sets the block metadata to differ from.
		 *
		 * @param location The location the randomized block will be gotten from.
		 */
		public void setRandomSilverfishBlock(Location location) {
			Block block = location.getBlock();

			// Checks if block is one of the possible replaced materials
			if (block.getType() == Material.AIR
					|| block.getType() == Material.GRASS
					|| block.getType() == Material.LONG_GRASS
					|| block.getType() == Material.VINE
					|| block.getType() == Material.WATER
					|| block.getType() == Material.STATIONARY_WATER
					|| block.getType() == Material.LAVA
					|| block.getType() == Material.STATIONARY_LAVA) {

				// Randomizes a number from 1 to 8
				Random random = new Random();
				int randomNumber = random.nextInt(8) + 1;

				switch (randomNumber) {
					case 1:
						// Stone
						block.setType(Material.STONE);
						break;
					case 2:
						// Cobblestone
						block.setType(Material.COBBLESTONE);
						break;
					case 3:
						// Stone Brick
						block.setType(Material.SMOOTH_BRICK);
						break;
					case 4:
						// Chiseled Stone Brick
						block.setType(Material.SMOOTH_BRICK);
						block.setData((byte) 3);
						break;
					case 5:
						// Cracked Stone Brick
						block.setType(Material.SMOOTH_BRICK);
						block.setData((byte) 2);
						break;
					case 6:
					case 7:
					case 8:
						// Mossy Stone Brick
						block.setType(Material.SMOOTH_BRICK);
						block.setData((byte) 1);
						break;
				}
				// Setting block metadata
				block.setMetadata(player.getDisplayName() + "Silverfish Block", new FixedMetadataValue(instance.getGameManager().getMain(), true));
			}
		}

		/**
		 *
		 */
		public void setWallBlocksLocations(Location location, int wallDirection) {
			// Y coordinate
			for (int y = 1; y <= wallHeight; y++) {
				// X and Y coordinates
				for (int i = (-wallWidth / 2); i <= (wallWidth / 2); i++) {
					int x = i;
					int z = 0;

					switch (wallDirection) {
						case 1:
							break;
						case 2:
							z = x;
							break;
						case 3:
							z = x;
							x = 0;
							break;
						case 4:
							z = -x;
							break;
					}

					Location wallLocation = location.clone().add(x, y, z);
					Block block = wallLocation.getBlock();
					originalBlocks.add(block); // Store the original block
					replacedBlocks.add(block.getType()); // Initially, the replaced block is the same as the original block
					wallLocations.add(wallLocation);
				}
			}
		}

		/**
		 *
		 *
		 */
		public void loopThroughWallBlocks(Consumer<Location> behavior, double delayBetweenBlocks) {
			double t = 0;

			for (Location blockLocation : wallLocations) {
				t++;

				Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
					behavior.accept(blockLocation);
				}, (long) ((delayBetweenBlocks * t) * 20));

			}
		}

		/*
		 *
		 */
		public void buildWall(Location location) {
			setWallBlocksLocations(location, getPlayerSightDirection());
			loopThroughWallBlocks(this::setRandomSilverfishBlock, 0);
		}

		private Location getRandomWallBlockLocation() {
			if (wallLocations.isEmpty()) {
				return null;
			}

			Random random = new Random();
			int randomIndex;

			do {
				randomIndex = random.nextInt(wallLocations.size());
			} while (randomizedIndexes.contains(randomIndex));

			randomizedIndexes.add(randomIndex);

			return wallLocations.get(randomIndex);
		}

		/**
		 * Removes the all blocks after a predetermined amount of seconds.
		 * Checks if blocks from a location have the wall block metadata.
		 * Removes the metadata from those blocks.
		 * Sets those blocks to Air.
		 *
		 * @param location The location the randomized block will be gotten from.
		 *
		 */
		public void breakWallBlock(Location location) {
			Location randomizedBlockLocation = getRandomWallBlockLocation();

			if (randomizedBlockLocation == null) {
				return;
			}

			Block block = randomizedBlockLocation.getBlock();

			// Removing Block
			Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
				// Checks if block has the custom set metadata
				if (block.hasMetadata(player.getDisplayName() + "Silverfish Block")) {
					// Removes the custom set metadata
					block.removeMetadata(player.getDisplayName() + "Silverfish Block", instance.getGameManager().getMain());
					// Sets the block to Air
					block.setType(Material.AIR);

//					// Restore the original block type
//					int index = randomizedIndexes.get(randomizedIndexes.size() - 1);
//					block.setType(replacedBlocks.get(index));
//
//					// Restoring the replaced block
//					replacedBlocks.remove(replacedBlocks.size() - 1);

					// Playing digging stone sound to all players
					SoundManager.playSoundToAllGamePlayersFromALocation(instance, randomizedBlockLocation, Sound.DIG_STONE, 1, 1);

					// Adding Stone/Cobblestone block breaking particles
					for (Player gamePlayer : instance.players) {
						for (int i = 0; i < 8; i++) {
							gamePlayer.playEffect(randomizedBlockLocation, Effect.TILE_BREAK, 1);
							gamePlayer.playEffect(randomizedBlockLocation, Effect.TILE_BREAK, 4);
						}
					}
				}

				// Spawning Silverfish
				spawnSilverfish(randomizedBlockLocation);

				// Removing broken block from the list
				wallLocations.remove(randomizedBlockLocation);
//				randomizedIndexes.remove(randomizedIndexes.size() - 1);

				// Break wall after a defined delay
			}, (long) (startBreakingWallTime * 20));
		}

		public void breakWall() {
			loopThroughWallBlocks(this::breakWallBlock, delayBetweenBreakingBlocks);
			despawnSilverfish();
		}

		public void spawnSilverfish(Location location) {
			Silverfish silverfish = (Silverfish) player.getWorld().spawnCreature(location, EntityType.SILVERFISH);

			silverfish.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, 4));
			silverfish.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 4));

			// Customizing Creeper
			customizeSilverfish(silverfish, player);
			silverfish.setTarget(instance.getNearestPlayer(player, 150));
		}

		public void despawnSilverfish() {
			Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
				for (Entity en : player.getWorld().getEntities())
					if (!(en instanceof Player))
						if (en.getType().equals(EntityType.SILVERFISH)) {
							if (en.getName().contains(player.getName())) {
								if (en.getName().contains(ChatColorHelper.color("&eSilverfish")));
								en.remove();
							}
						}
				// Silverfish despawn after half the cooldown of the wall
			}, (long) 10 * 20);
		}

		private void customizeSilverfish(Creature mob, Player player) {
			// Adding resistance, so it will not suffocate in the wall and die
//			mob.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, 4));
//			mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 4));

			// Setting Mob to not de-spawn when far away
			mob.setRemoveWhenFarAway(false);
			// Setting Mob Name to owner's
			mob.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eSilverfish"));
			// Setting Custom name visible
			mob.setCustomNameVisible(true);
		}
	}
}