package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.CooldownNatowski;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SilverfishClass extends BaseClass {

	private static final int MAX_WALL_AMOUNT = 2;
	private static final int WALL_HEIGHT = 3;
	private static final int WALL_WIDTH = 3;
	/** How far in front of the player the wall is placed when aiming at the air. */
	private static final int WALL_CLOSE_DISTANCE = 3;
	private static final int WALL_FAR_DISTANCE = 7;
	/** Time between two walls, so a quick double click doesn't use both of them. It has no message. */
	private static final double WALL_DELAY_SECONDS = 0.25;
	private static final long WALL_START_BREAKING_TICKS = 2 * 20;
	private static final long WALL_BREAKING_DELAY_TICKS = 4;
	private static final long SILVERFISH_DESPAWN_TICKS = 10 * 20;
	private static final Material[] REPLACEABLE_BLOCKS = {
			Material.AIR, Material.GRASS, Material.LONG_GRASS, Material.VINE,
			Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA
	};

	private final ItemStack weapon;
	private final ItemStack wallItem;
	private final ItemStack barrierItem;
	/** The cooldown is the time it takes to regenerate one wall, the amount of the item is how many are left. */
	private final Ability wallAbility = new Ability("&7&lWall", 10, player);
	private final CooldownNatowski wallDelay = new CooldownNatowski(WALL_DELAY_SECONDS);
	private final Random random = new Random();
	/** Gives every wall its own metadata key on its blocks, so walls placed on top of each other never break each other's blocks. */
	private int wallCounter = 0;

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
				"&eLeft Click&7 to place &ofurther",
				"&eRight Click&7 to place &ocloser"
		);
		wallItem.setDurability((short) 2);

		// Barrier
		barrierItem = ItemHelper.setDetails(
				new ItemStack(Material.BARRIER),
				"&c&lOut of Walls!"
		);
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		handleAddingWallItem();
		wallAbility.updateActionBar(player, this);
	}

	/** Regenerates one wall every time the cooldown is over, until the stack is full again. */
	private void handleAddingWallItem() {
		ItemStack item = player.getInventory().getItem(1);
		if (item == null) return;

		// Handle barrier case
		if (item.getType() == Material.BARRIER && wallAbility.isReady()) {
			player.getInventory().setItem(1, wallItem.clone());
			wallAbility.use();
			SoundManager.playItemPickup(player);
			return;
		}

		// Only proceed if it's a wall item
		if (!item.isSimilar(wallItem)) return;

		int currentAmount = item.getAmount();
		if (currentAmount < MAX_WALL_AMOUNT && wallAbility.isReady()) {
			int newAmount = currentAmount + 1;
			item.setAmount(newAmount);
			SoundManager.playItemPickup(player);
			if (newAmount == MAX_WALL_AMOUNT) return;
			wallAbility.use();
		}
	}

	@Override
	public void setItems(Inventory playerInv) {
		wallAbility.getCooldownInstance().reset();
		wallDelay.reset();

		// Settings Items
		ItemStack initialWallItem = wallItem.clone();
		initialWallItem.setAmount(MAX_WALL_AMOUNT);

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, initialWallItem);

		for (Entity en : player.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(player.getName()))
					en.remove();
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null || !item.isSimilar(wallItem)) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;
		if (!wallDelay.isReady()) return;

		Location base = getWallBase(action, event.getClickedBlock());
		if (base == null) return;

		wallDelay.use();
		new SilverfishWall(++wallCounter).build(base);

		// One wall used. The first one used from a full stack starts the regeneration
		int remainingWalls = item.getAmount() - 1;
		item.setAmount(remainingWalls);
		if (remainingWalls == 0) player.getInventory().setItem(1, barrierItem);
		if (remainingWalls == MAX_WALL_AMOUNT - 1) wallAbility.use();
	}

	/** Where the wall starts, or null when the click doesn't place one (a left click on a block). */
	private Location getWallBase(Action action, Block clickedBlock) {
		switch (action) {
			case RIGHT_CLICK_BLOCK:
				return clickedBlock.getLocation();
			case LEFT_CLICK_AIR:
				return getLocationInFront(WALL_FAR_DISTANCE);
			case RIGHT_CLICK_AIR:
				return getLocationInFront(WALL_CLOSE_DISTANCE);
			default:
				return null;
		}
	}

	/** The spot the given amount of blocks ahead of where the player looks, at the height of their feet. */
	private Location getLocationInFront(int distance) {
		Location eyeLocation = player.getEyeLocation();
		Vector direction = eyeLocation.getDirection();
		return eyeLocation.add(direction.multiply(distance)).subtract(0, player.getEyeHeight(), 0);
	}

	@Override
	public ClassType getType() {
		return ClassType.Silverfish;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	/** A wall of infested blocks that breaks block by block, each one spawning a silverfish. */
	private class SilverfishWall {
		/** Only the blocks this wall placed carry this key, so breaking one wall never touches another. */
		private final String metadataKey;
		/** The blocks of the wall, in the order they will break. */
		private final List<Location> wallBlocks = new ArrayList<>();
		/** The silverfish this wall spawned, the only ones it removes when it is over. */
		private final List<Silverfish> spawnedSilverfish = new ArrayList<>();

		private SilverfishWall(int wallId) {
			this.metadataKey = player.getDisplayName() + "Silverfish Block " + wallId;
		}

		private void build(Location base) {
			int[] step = getWallStep(player.getLocation().getYaw());

			for (int y = 1; y <= WALL_HEIGHT; y++) {
				for (int i = -(WALL_WIDTH / 2); i <= WALL_WIDTH / 2; i++) {
					Location blockLocation = base.clone().add(i * step[0], y, i * step[1]);
					if (setRandomSilverfishBlock(blockLocation)) wallBlocks.add(blockLocation);
				}
			}

			if (wallBlocks.isEmpty()) return;
			Collections.shuffle(wallBlocks, random);
			startBreaking();
		}

		/**
		 * The wall runs sideways to where the player looks, in one of 8 directions. This is how far it moves
		 * on the X and Z axes for each block of its width.
		 */
		private int[] getWallStep(float yaw) {
			int direction = Math.round(((yaw % 360 + 360) % 360) / 45f) % 8; // 0 is south, then clockwise
			switch (direction) {
				case 1: case 5: return new int[]{1, 1};  // looking south west or north east
				case 2: case 6: return new int[]{0, 1};  // looking west or east
				case 3: case 7: return new int[]{1, -1}; // looking north west or south east
				default: return new int[]{1, 0};         // looking south or north
			}
		}

		/**
		 * Turns the block into one a silverfish can spawn from: stone, cobblestone, or a stone brick
		 * (normal, mossy, cracked or chiseled). Only blocks that can be replaced are, and they get this
		 * wall's metadata so it knows which ones are its own.
		 *
		 * @return whether the block was replaced
		 */
		@SuppressWarnings("deprecation")
		private boolean setRandomSilverfishBlock(Location location) {
			Block block = location.getBlock();
			if (!Arrays.asList(REPLACEABLE_BLOCKS).contains(block.getType())) return false;

			switch (random.nextInt(8)) {
				case 0: block.setType(Material.STONE); break;
				case 1: block.setType(Material.COBBLESTONE); break;
				case 2: block.setType(Material.SMOOTH_BRICK); break;
				case 3: setSmoothBrick(block, 3); break; // Chiseled
				case 4: setSmoothBrick(block, 2); break; // Cracked
				default: setSmoothBrick(block, 1); break; // Mossy
			}

			block.setMetadata(metadataKey, new FixedMetadataValue(instance.getGameManager().getMain(), true));
			return true;
		}

		@SuppressWarnings("deprecation")
		private void setSmoothBrick(Block block, int data) {
			block.setType(Material.SMOOTH_BRICK);
			block.setData((byte) data);
		}

		/** Breaks the blocks one at a time in random order after a moment, then removes the silverfish. */
		private void startBreaking() {
			instance.trackClassTask(new BukkitRunnable() {
				private int broken = 0;

				@Override
				public void run() {
					breakWallBlock(wallBlocks.get(broken));
					if (++broken >= wallBlocks.size()) cancel();
				}
			}.runTaskTimer(instance.getGameManager().getMain(), WALL_START_BREAKING_TICKS, WALL_BREAKING_DELAY_TICKS));

			instance.trackClassTask(Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
				for (Silverfish silverfish : spawnedSilverfish)
					silverfish.remove();
				spawnedSilverfish.clear();
			}, SILVERFISH_DESPAWN_TICKS));
		}

		@SuppressWarnings("deprecation")
		private void breakWallBlock(Location location) {
			Block block = location.getBlock();
			if (!block.hasMetadata(metadataKey)) return;

			block.removeMetadata(metadataKey, instance.getGameManager().getMain());
			block.setType(Material.AIR);

			SoundManager.playSoundToAll(player, location, Sound.DIG_STONE, 1, 1);
			for (int i = 0; i < 8; i++) {
				location.getWorld().playEffect(location, Effect.TILE_BREAK, Material.STONE.getId());
				location.getWorld().playEffect(location, Effect.TILE_BREAK, Material.COBBLESTONE.getId());
			}
			spawnSilverfish(location);
		}

		@SuppressWarnings("deprecation")
		private void spawnSilverfish(Location location) {
			Silverfish silverfish = (Silverfish) player.getWorld().spawnCreature(location, EntityType.SILVERFISH);
			customizeSilverfish(silverfish);
			spawnedSilverfish.add(silverfish);

			Player target = instance.getNearestPlayer(player, silverfish, 150);
			if (target != null) silverfish.setTarget(target);
		}

		private void customizeSilverfish(Creature mob) {
			mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999 * 20, 1, false, false));
			mob.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, 4));
			mob.setRemoveWhenFarAway(false);
			mob.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eSilverfish"));
			mob.setCustomNameVisible(true);
		}
	}
}
