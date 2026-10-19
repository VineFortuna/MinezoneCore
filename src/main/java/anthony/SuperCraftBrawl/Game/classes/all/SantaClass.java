package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SantaClass extends BaseClass {

	private static final int REINDEER_FLIGHT_TICKS = 6 * 20;
	private static final int REINDEER_WARNING_TICKS = 3 * 20;
	private static final double REINDEER_SPEED = 0.7; // blocks per tick
	private static final double REINDEER_VERTICAL_SPEED = 0.5; // blocks per tick
	private static final int SNEAK_HOLD_TICKS = 2; // sneak counts as held this many ticks after the last exit attempt
	private static final Field RIDER_JUMP_FIELD = findRiderJumpField();

	private int cookieUses;
	private Horse reindeer;
	private BukkitTask reindeerTask;
	private int flightTicks;
	private int lastSneakTick;

	public SantaClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19",
				"FF0000", 6, "Santa");
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	public ItemStack horseSpawnEgg() {
		@SuppressWarnings("deprecation")
		ItemStack spawnEgg = new ItemStack(Material.MONSTER_EGG, 1, (short) EntityType.HORSE.getTypeId());
		ItemMeta meta = spawnEgg.getItemMeta();
		meta.setDisplayName(instance.color("&eSanta's Reindeer"));
		spawnEgg.setItemMeta(meta);
		return spawnEgg;
	}

	@Override
	public void setItems(Inventory playerInv) {
		this.cookieUses = 0;

		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.MILK_BUCKET), instance.color("&r&lSanta's Milk"), "",
						instance.color("&7Right click to remove effects including"),
						instance.color("&7cookie effects")));
		playerInv.setItem(2, horseSpawnEgg());
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = player.getItemInHand();

		if (item != null && item.getType() == Material.MONSTER_EGG) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				ItemMeta meta = item.getItemMeta();

				// Check if the spawn egg is customized for the horse
				if (meta != null && meta.getDisplayName() != null && meta.getDisplayName().contains("Reindeer")) {
					event.setCancelled(true); // Cancel the default spawn egg behavior
					player.getInventory().clear(player.getInventory().getHeldItemSlot());

					spawnReindeer();
				}
			}
		} else if (item != null && item.getType() == Material.COOKIE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (this.cookieUses == 0) {
					player.playSound(player.getLocation(), Sound.EAT, 1, 1);
					player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999999, 0));
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 0));
					player.sendMessage(instance.color("&2&l(!) &c&lHO HO HO! &rDat cookie was delicious"));
					player.sendMessage(instance.color("&2&l(!) &rYou were given &eStrength 1 & Slow 1"));
					this.cookieUses++;
				} else if (this.cookieUses == 1) {
					player.playSound(player.getLocation(), Sound.EAT, 1, 1);
					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
					player.removePotionEffect(PotionEffectType.SLOW);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 1));
					player.sendMessage(instance.color("&2&l(!) &c&lHO HO HO! &rDat cookie was delicious"));
					player.sendMessage(instance.color("&2&l(!) &rYou were given &eResistance 1 & Slow 2"));
					this.cookieUses++;
				} else if (this.cookieUses == 2) {
					player.playSound(player.getLocation(), Sound.EAT, 1, 1);
					player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 3));
					player.removePotionEffect(PotionEffectType.SLOW);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 2));
					player.sendMessage(instance.color("&2&l(!) &c&lHO HO HO! &rDat cookie was delicious"));
					player.sendMessage(instance.color("&2&l(!) &rYou were given &eJump 4 & Slow 3"));
					this.cookieUses++;
				} else {
					player.sendMessage(
							instance.color("&2&l(!) &c&lHO HO OH NOOO! &rI don't think I can eat anymore for now!"));
				}
			}
		} else if (item != null && item.getType() == Material.MILK_BUCKET) {
			event.setCancelled(true);
			if (player.getGameMode() != GameMode.SPECTATOR) {
				List<PotionEffectType> effectsToRemove = new ArrayList<>();
				for (PotionEffect effect : player.getActivePotionEffects()) {
					// Check if the potion effect is NOT one of the allowed ones
					if (effect.getType() != PotionEffectType.SLOW
							&& effect.getType() != PotionEffectType.INCREASE_DAMAGE // Strength
							&& effect.getType() != PotionEffectType.JUMP
							&& effect.getType() != PotionEffectType.DAMAGE_RESISTANCE) {
						// Add to the list of effects to remove
						effectsToRemove.add(effect.getType());
					}
				}

				// Remove unwanted potion effects after iteration
				for (PotionEffectType effectType : effectsToRemove) {
					player.removePotionEffect(effectType);
				}
				player.setFireTicks(0);
				this.cookieUses = 0;
				player.sendMessage(
						instance.color("&2&l(!) &rYou feel refreshed! All effects have been reset from cookie too"));
			}
		}
	}

	/*
	 * Spawns Santa's flying Reindeer and mounts the player on it. While riding:
	 * WASD flies horizontally (relative to where you face), Space flies up, Sneak flies down,
	 * and letting go of every key hovers in place. Sneaking never dismounts during the flight.
	 */
	private void spawnReindeer() {
		if (reindeer != null)
			return;

		Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
		horse.setTamed(true);
		horse.setAdult();
		horse.setVariant(Horse.Variant.HORSE);
		horse.setColor(Horse.Color.DARK_BROWN);
		horse.setStyle(Horse.Style.NONE);
		horse.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Reindeer");
		horse.setOwner(player);
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		horse.setPassenger(player);

		reindeer = horse;
		flightTicks = 0;
		lastSneakTick = Integer.MIN_VALUE / 2;

		// The rider's keys arrive in steer packets every tick and are stored on the NMS player
		EntityPlayer rider = ((CraftPlayer) player).getHandle();

		reindeerTask = instance.trackClassTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (flightTicks++ >= REINDEER_FLIGHT_TICKS || !horse.isValid() || horse.getPassenger() != player
						|| instance.state != GameState.STARTED || instance.classes.get(player) != SantaClass.this
						|| player.getGameMode() == GameMode.SPECTATOR) {
					endReindeerFlight();
					return;
				}

				float forward = Math.signum(rider.ba);
				float strafe = Math.signum(rider.aZ); // positive = A (left)
				int vertical = (isJumping(rider) ? 1 : 0) - (flightTicks - lastSneakTick <= SNEAK_HOLD_TICKS ? 1 : 0);

				double yaw = Math.toRadians(player.getLocation().getYaw());
				double dirX = -Math.sin(yaw);
				double dirZ = Math.cos(yaw);
				Vector horizontal = new Vector(dirX * forward + dirZ * strafe, 0, dirZ * forward - dirX * strafe);
				if (horizontal.lengthSquared() > 1) horizontal.normalize();

				horse.setVelocity(horizontal.multiply(REINDEER_SPEED).setY(vertical * REINDEER_VERTICAL_SPEED));

				// Flying never builds fall damage for the reindeer or Santa
				horse.setFallDistance(0);
				player.setFallDistance(0);

				// Magic trail + sleigh bells
				ParticleEffect.FIREWORKS_SPARK.display(horse.getLocation());
				// Warning beeps for the last 3 seconds before the reindeer disappears
				if (flightTicks >= REINDEER_FLIGHT_TICKS - REINDEER_WARNING_TICKS && flightTicks % 10 == 0) {
					horse.getWorld().playSound(horse.getLocation(), Sound.NOTE_PLING, 0.6f, 2.0f);
				}
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 1, 1));
	}

	/** Ends the flight: stops the controller, drops Santa off and removes the reindeer. Safe to call twice. */
	public void endReindeerFlight() {
		if (reindeer == null)
			return;

		Horse horse = reindeer;
		reindeer = null; // clear first so the eject below isn't blocked by shouldBlockReindeerExit
		reindeerTask.cancel();
		horse.eject();
		horse.remove();
		player.setFallDistance(0);
	}

	/*
	 * Called from GameManager's VehicleExitEvent. Vanilla dismounts a rider every tick they hold
	 * Sneak, so blocking that exit keeps Santa mounted and doubles as our "Sneak is held" signal.
	 */
	public boolean shouldBlockReindeerExit(Entity vehicle) {
		if (reindeer == null || vehicle != reindeer || !reindeer.isValid() || player.isDead())
			return false;

		lastSneakTick = flightTicks;
		return true;
	}

	private static boolean isJumping(EntityPlayer rider) {
		try {
			return RIDER_JUMP_FIELD != null && RIDER_JUMP_FIELD.getBoolean(rider);
		} catch (IllegalAccessException e) {
			return false;
		}
	}

	// EntityLiving.aY is the protected "jump key held" flag set from the rider's steer packet (1.8.8 mapping)
	private static Field findRiderJumpField() {
		try {
			Field field = EntityLiving.class.getDeclaredField("aY");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Santa;
	}

	@Override
	public void setNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.setUnbreakable(ItemHelper.addEnchant(
				ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.COOKIE), instance.color("&6&lDelicious Cookie"),
								"", instance.color("&r- &71st Use: Strength 1 & Slow 1"),
								instance.color("&r- &72nd Use: Resistance 1 & Slow 2"),
								instance.color("&r- &73rd Use: Jump 4 & Slow 3")),
						Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 1));
		return item;
	}
}
