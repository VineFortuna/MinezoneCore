package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SilverfishClass extends BaseClass {
	private final ItemStack weapon;
	private final ItemStack wallItem;
	private final Ability wallAbility = new Ability("&7&lWall", 10, player);
	private static final Material[] REPLACEABLE_BLOCKS = {
			Material.AIR, Material.GRASS, Material.LONG_GRASS, Material.VINE,
			Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA
	};

	public SilverfishClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5MWRhYjgzOTFhZjVmZGE1NGFjZDJjMGIxOGZiZDgxOWI4NjVlMWE4ZjFkNjIzODEzZmE3NjFlOTI0NTQwIn19fQ==",
				"717E81",
				6,
				"Silverfish"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.SHEARS),
				"&7&lChitin Bite"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);

		// Wall Ability
		wallItem = ItemHelper.setDetails(
				new ItemStack(Material.SMOOTH_BRICK),
				wallAbility.getAbilityNameLeftRightClickMessage(),
				"&7Create a wall of infested stone",
				"&7that slowly breaks, spawning silverfish",
				"",
				"&e&nLeft Click&7 to place a wall &ofurther",
				"&e&nRight Click&7 to place a wall &ocloser"
		);
		wallItem.setDurability((short) 2);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		wallAbility.updateActionBar(player, this);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		wallAbility.getCooldownInstance().reset();

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

		if (item == null) return;
		// Wall Ability
		if (!item.equals(wallItem)) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;
		if (!wallAbility.isClickAction(action)) return;
		if (!wallAbility.isReady()) return;

		SilverfishWall wall = new SilverfishWall(3, 3, player, 2, 0.2);
		handleWallPlacement(action, event.getClickedBlock(), wall);
		wallAbility.use();
	}

	private void handleWallPlacement(Action action, Block clickedBlock, SilverfishWall wall) {
		// Block (Right-Click)
		if (action == Action.RIGHT_CLICK_BLOCK) {
			wall.buildWallOnBlock(clickedBlock);
		}
		// Air (Left-Click)
		else if (action == Action.LEFT_CLICK_AIR) {
			wall.buildWallOnAir(7);
		}
		// Air (Right-Click)
		else if (action == Action.RIGHT_CLICK_AIR) {
			wall.buildWallOnAir(3);
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Silverfish;
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
		double startBreakingWallTime;
		double delayBetweenBreakingBlocks;
		List<Location> wallLocations = new ArrayList<>();
		private List<Block> originalBlocks = new ArrayList<>();
		private List<Material> replacedBlocks = new ArrayList<Material>();
		List<Integer> randomizedIndexes = new ArrayList<>();

		public SilverfishWall(int wallHeight, int wallWidth, Player player, double startBreakingWallTime, double delayBetweenBreakingBlocks) {
			this.player = player;
			this.playerLocation = player.getLocation();
			this.wallHeight = wallHeight;
			this.wallWidth = wallWidth;
			this.startBreakingWallTime = startBreakingWallTime;
			this.delayBetweenBreakingBlocks = delayBetweenBreakingBlocks;
		}

		public void buildWallOnBlock(Block clickedBlock) {
			buildWall(clickedBlock.getLocation());
			breakWall();
		}

		// Right-Click Air Action
		public void buildWallOnAir(int distance) {
			// Get player's eye location and direction vector
			Location eyeLocation = player.getEyeLocation();
			Vector direction = eyeLocation.getDirection();

			// Calculate spawn location using the direction vector
			Location spawnLocation = eyeLocation.add(direction.multiply(distance));

			// Adjust height to be relative to player's feet instead of eyes
			spawnLocation.subtract(0, player.getEyeHeight(), 0);

			buildWall(spawnLocation);
			breakWall();
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
			if (!Arrays.asList(REPLACEABLE_BLOCKS).contains(block.getType())) return;

			// Randomizes a number from 1 to 8
			Random random = new Random();
			int variant = random.nextInt(8) + 1;

			switch (variant) {
				case 1: block.setType(Material.STONE); break;
				case 2: block.setType(Material.COBBLESTONE); break;
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
				default:
					// Mossy Stone Brick
					block.setType(Material.SMOOTH_BRICK);
					block.setData((byte) 1);
					break;
			}
			// Setting block metadata
			block.setMetadata(player.getDisplayName() + "Silverfish Block",
					new FixedMetadataValue(instance.getGameManager().getMain(), true));
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
			double delayTicks = (long) (delayBetweenBlocks * 20);

			for (int i = 0; i < wallLocations.size(); i++) {
				Location blockLocation = wallLocations.get(i);
				long currentDelay = (long) (i * delayTicks);

				Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(),
						() -> behavior.accept(blockLocation), currentDelay);
			}
		}

		/*
		 *
		 */
		public void buildWall(Location location) {
			setWallBlocksLocations(location, getPlayerSightDirection());
			loopThroughWallBlocks(this::setRandomSilverfishBlock, 0);
		}

		/**
		 * Checks the player yaw to determine which of the 4 directions the wall is going to be built.
		 * Returns an int from 1 to 4.
		 *
		 */
		public int getPlayerSightDirection() {
			float yaw = Math.abs(playerLocation.getYaw() % 360);
			if (yaw < 0) yaw += 360;

			// Returning 1 of the 4 possible wall building directions
			if ((yaw >= 0 && yaw < 30) || (yaw >= 150 && yaw <= 180)) return 1;
			if ((yaw >= 30 && yaw < 60)) return 2;
			if ((yaw >= 60 && yaw < 120)) return 3;
			if ((yaw >= 120 && yaw < 150)) return 4;
			return 0;
		}

		private Location getRandomWallBlockLocation() {
			if (wallLocations.isEmpty()) {
				return null;
			}

			Random random = new Random();
			List<Integer> availableIndexes = IntStream.range(0, wallLocations.size())
					.boxed()
					.filter(i -> !randomizedIndexes.contains(i))
					.collect(Collectors.toList());

			if (availableIndexes.isEmpty()) {
				return null;
			}

			int randomIndex = availableIndexes.get(random.nextInt(availableIndexes.size()));
			randomizedIndexes.add(randomIndex);
			return wallLocations.get(randomIndex);
		}

		public void breakWall() {
			loopThroughWallBlocks(this::breakWallBlock, delayBetweenBreakingBlocks);
			despawnSilverfish();
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

			if (randomizedBlockLocation == null) return;

			Block block = randomizedBlockLocation.getBlock();
			String metadataKey = player.getDisplayName() + "Silverfish Block";

			// Removing Block
			Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
				// Checks if block has the custom set metadata
				if (block.hasMetadata(metadataKey)) {
					block.removeMetadata(metadataKey, instance.getGameManager().getMain());
					block.setType(Material.AIR);

//					// Restore the original block type
//					int index = randomizedIndexes.get(randomizedIndexes.size() - 1);
//					block.setType(replacedBlocks.get(index));
//
//					// Restoring the replaced block
//					replacedBlocks.remove(replacedBlocks.size() - 1);

					// Playing digging stone sound to all players
					SoundManager.playSoundToAll(player, randomizedBlockLocation, Sound.DIG_STONE, 1, 1);


					playBlockBreakEffects(randomizedBlockLocation);
					spawnSilverfish(randomizedBlockLocation);
					// Removing broken block from the list
					wallLocations.remove(randomizedBlockLocation);
				}
			}, (long) (startBreakingWallTime * 20));
		}

		private void playBlockBreakEffects(Location randomizedBlockLocation) {
			// Adding Stone/Cobblestone block breaking particles
			for (Player gamePlayer : instance.players) {
				for (int i = 0; i < 8; i++) {
					gamePlayer.playEffect(randomizedBlockLocation, Effect.TILE_BREAK, 1);
					gamePlayer.playEffect(randomizedBlockLocation, Effect.TILE_BREAK, 4);
				}
			}
		}

		public void spawnSilverfish(Location location) {
			Silverfish silverfish = (Silverfish) player.getWorld().spawnCreature(location, EntityType.SILVERFISH);
			customizeSilverfish(silverfish, player);

			Player target = instance.getNearestPlayer(player, silverfish, 150);
			if (target != null) {
				silverfish.setTarget(target);
			}
		}

		public void despawnSilverfish() {
			Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
				for (Entity entity : player.getWorld().getEntities()) {
					if (entity.getType() == EntityType.SILVERFISH &&
							entity.getCustomName() != null &&
							entity.getCustomName().contains(player.getName())) {
						entity.remove();
					}
				}
			}, 10 * 20L);
		}

		private void customizeSilverfish(Creature mob, Player player) {
			mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999 * 20, 1, false, false));
			mob.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, 4));
			mob.setRemoveWhenFarAway(false);
			mob.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eSilverfish"));
			mob.setCustomNameVisible(true);
		}
	}
}