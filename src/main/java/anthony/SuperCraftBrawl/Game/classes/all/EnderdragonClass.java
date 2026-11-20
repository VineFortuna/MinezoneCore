package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.HealTask;
import anthony.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class EnderdragonClass extends BaseClass {

	// Teleporter
	private static final int TELEPORTER_SLOT = 1;
	private static final int STARTING_TELEPORTERS = 3;
	private static final int TELEPORTERS_PER_KILL = 1;

	private static final PotionEffect WEAKNESS = new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1, false, false);

	private final ItemStack weapon;
	/** No cooldown, so it only gives the ability its name. */
	private final Ability teleportAbility = new Ability("&5&lTeleporter", player);

	public EnderdragonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.3;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U0ZDM1YTJmNmJkNzM5NjA1ZmE0ZWFhNmUxNWQwMzgwMDlmM2YyMGIxYTIwNDYyODAxNjA1ODczNmU3Yjk1ZSJ9fX0=",
				"000000",
				6,
				"EnderDragon"
		);

		// Weapon
		String healingDisplay = ItemHelper.formatDouble(HealTask.MAX_HEALS * HealTask.HEAL_AMOUNT / 2);
		String durationDisplay = ItemHelper.formatDouble(HealTask.TOTAL_DURATION_TICKS / 20.0);
		String rangeDisplay = ItemHelper.formatDouble(HealTask.HEAL_RANGE);

		weapon = ItemHelper.setUnbreakable(ItemHelper.setDetails(
				new ItemStack(Material.STONE_SWORD),
				"&8&lEndblade",
				"&7On kill, summon an end crystal",
				"&7that heals you over time",
				"",
				"&7Heals &e" + healingDisplay + " &c❤ &7over &a" + durationDisplay + "&as",
				"&7Range: &a" + rangeDisplay + " &7blocks"
		));
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	private ItemStack getTeleporters(int amount) {
		return ItemHelper.setDetails(
				new ItemStack(Material.ENDER_PEARL, amount),
				teleportAbility.getAbilityNameRightClickMessage(),
				ItemHelper.gainOnKillLine(TELEPORTERS_PER_KILL, "teleporter", "teleporters")
		);
	}

	private ItemStack getBarrier() {
		return ItemHelper.setDetails(
				new ItemStack(Material.BARRIER),
				"&c&lOut of Teleporters!",
				"",
				"&7Each kill regenerates " + TELEPORTERS_PER_KILL + (TELEPORTERS_PER_KILL == 1 ? " teleporter" : " teleporters")
		);
	}

	@Override
	public void setItems(Inventory playerInv) {
		playerInv.setItem(0, weapon);
		playerInv.setItem(TELEPORTER_SLOT, getTeleporters(STARTING_TELEPORTERS));
		player.addPotionEffect(WEAKNESS);
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		if (!player.hasPotionEffect(PotionEffectType.WEAKNESS)) player.addPotionEffect(WEAKNESS);

		// A pearl that was the last one leaves the slot empty, and a barrier takes its place
		if (player.getInventory().getItem(TELEPORTER_SLOT) == null) {
			player.getInventory().setItem(TELEPORTER_SLOT, getBarrier());
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.EnderDragon;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public void killEvent(Player damagerPlayer) {
		if (!instance.classes.containsKey(damagerPlayer) || checkIfDead(player, instance)) return;

		spawnHealingCrystal(damagerPlayer);
		gainTeleporter(damagerPlayer);
	}

	private void spawnHealingCrystal(Player killer) {
		Location killerLocation = killer.getLocation();
		EnderCrystal crystal = (EnderCrystal) killerLocation.getWorld().spawnEntity(killerLocation, EntityType.ENDER_CRYSTAL);
		HealTask task = new HealTask(killer, crystal, instance);
		// On the main thread: it heals the player and removes the crystal, which isn't safe from another thread
		BukkitTask bukkit = instance.trackClassTask(
				Bukkit.getScheduler().runTaskTimer(instance.getGameManager().getMain(), task, 0, 2L));
		task.set(bukkit);
	}

	private void gainTeleporter(Player killer) {
		killer.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &rYou got a kill and gained &e"
				+ TELEPORTERS_PER_KILL + " &rextra " + teleportAbility.getAbilityName()));

		// Adds to the pearls left, or replaces the barrier
		ItemStack current = killer.getInventory().getItem(TELEPORTER_SLOT);
		if (current == null || current.getType() != Material.ENDER_PEARL) {
			killer.getInventory().setItem(TELEPORTER_SLOT, getTeleporters(TELEPORTERS_PER_KILL));
		} else {
			current.setAmount(current.getAmount() + TELEPORTERS_PER_KILL);
		}
	}
}
