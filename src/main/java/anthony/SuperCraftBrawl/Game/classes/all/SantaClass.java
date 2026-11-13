package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Timer;
import anthony.util.ItemHelper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class SantaClass extends BaseClass {

	private static final int REINDEER_FLIGHT_TICKS = 6 * 20;
	private static final int MILK_COOLDOWN_MILLIS = 5000;

	private int cookieUses;
	private Timer milkCooldown = new Timer();
	private final ReindeerFlight reindeer;

	public SantaClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19",
				"FF0000", 6, "Santa");
		baseVerticalJump = 0.9; // a lower double jump (the default is 1.0)
		reindeer = new ReindeerFlight(player, REINDEER_FLIGHT_TICKS,
				() -> instance.state == GameState.STARTED && instance.classes.get(player) == this
						&& player.getGameMode() != GameMode.SPECTATOR);
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
		this.milkCooldown = new Timer(); // ready again on every life

		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.MILK_BUCKET), instance.color("&r&lSanta's Milk"), "",
						instance.color("&7Right click to remove effects including"),
						instance.color("&7cookie effects (5s cooldown)")));
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
					player.removePotionEffect(PotionEffectType.SLOW);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 1));
					player.sendMessage(instance.color("&2&l(!) &c&lHO HO HO! &rDat cookie was delicious"));
					player.sendMessage(instance.color("&2&l(!) &rYou were given &eStrength 1 & Slow 2"));
					this.cookieUses++;
				} else if (this.cookieUses == 1) {
					player.playSound(player.getLocation(), Sound.EAT, 1, 1);
					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
					player.removePotionEffect(PotionEffectType.SLOW);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 3));
					player.sendMessage(instance.color("&2&l(!) &c&lHO HO HO! &rDat cookie was delicious"));
					player.sendMessage(instance.color("&2&l(!) &rYou were given &eResistance 1 & Slow 4"));
					this.cookieUses++;
				} else {
					player.sendMessage(
							instance.color("&2&l(!) &c&lHO HO OH NOOO! &rI don't think I can eat anymore for now!"));
				}
			}
		} else if (item != null && item.getType() == Material.MILK_BUCKET) {
			event.setCancelled(true);
			if (player.getGameMode() != GameMode.SPECTATOR) {
				int remaining = MILK_COOLDOWN_MILLIS - milkCooldown.getTime();
				if (remaining > 0) {
					player.sendMessage(instance.color("&c&l(!) &rYour Santa's Milk is on cooldown for &e"
							+ (int) Math.ceil(remaining / 1000.0) + "s"));
					return;
				}
				milkCooldown.restart();

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
	 * Spawns Santa's flying Reindeer and mounts the player on it (see ReindeerFlight for the controls).
	 */
	private void spawnReindeer() {
		if (reindeer.isFlying())
			return;

		instance.trackClassTask(reindeer.start(instance.getGameManager().getMain()));
	}

	/** Ends the flight: drops Santa off and removes the reindeer. Safe to call twice. */
	public void endReindeerFlight() {
		reindeer.end();
	}

	/** Called from GameManager's VehicleExitEvent: Sneak flies down instead of dismounting. */
	public boolean shouldBlockReindeerExit(Entity vehicle) {
		return reindeer.shouldBlockExit(vehicle);
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
								"", instance.color("&r- &71st Use: Strength 1 & Slow 2"),
								instance.color("&r- &72nd Use: Resistance 1 & Slow 4")),
						Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 1));
		return item;
	}
}
