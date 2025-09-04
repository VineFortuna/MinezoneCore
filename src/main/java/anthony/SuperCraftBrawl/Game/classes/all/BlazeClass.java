package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlazeClass extends BaseClass {

	private int fireballCounter;
	private final ItemStack weapon;
	private final ItemStack fireballItem;
	private final ItemStack armyItem;
	private final Ability riseAbility = new Ability("&6&lRise", 15, player);
	private final Ability fireballAbility = new Ability("&6&lFireball", 5, player);
	private final Ability armyAbility = new Ability("&6&lArmy", player);
	private static final double ARMY_ABILITY_DURATION = 25;
	private static final int FIREBALL_AMOUNT = 10;

	public BlazeClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU5Njk4MmMzZGJhN2Y2NzRjZmI5M2RkMzllMTcxM2E4ZWMxMjk5MDQ3M2FjYmZkODVhMThmZDkwOTE4ZGE0MSJ9fX0=",
				"FC9513",
				"FCAA00",
				"FCBF00",
				6,
				"Blaze"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.BLAZE_ROD),
				riseAbility.getAbilityNameRightClickMessage(),
				"",
				"&7Launch fireballs and send yourself up"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		weapon.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);

		// Fireball Launcher Ability
		fireballItem = ItemHelper.setDetails(
				new ItemStack(Material.BOW),
				fireballAbility.getAbilityName() + " Shooter &7(Right click)",
				"&7Shoot up to &e" + FIREBALL_AMOUNT + " &7fireballs before recharging",
				"&7Fireballs do damage and set fire",
				"",
				"&7You can spam this ability"
		);
		fireballItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		ItemHelper.setUnbreakable(fireballItem);
		ItemHelper.setHideFlags(fireballItem, true);

		// Army Ability
		String durationDisplay = ItemHelper.formatDouble(ARMY_ABILITY_DURATION);

		armyItem = ItemHelper.setDetails(
				new ItemStack(Material.MOB_SPAWNER),
				armyAbility.getAbilityNameRightClickMessage(),
				"&7Summon 3 Blazes to help you fight",
				"",
				"&7Duration: &a" + durationDisplay + "&as"
		);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		riseAbility.getCooldownInstance().reset();
		fireballAbility.getCooldownInstance().reset();
		fireballCounter = FIREBALL_AMOUNT;
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, fireballItem);
		playerInv.setItem(2, armyItem);
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		// Fireball Launcher Ability Message
		String fireballMessage;
		if (!fireballAbility.isReady()) {
			int remainingTime = (int) (fireballAbility.getCooldownInstance().getRemainingCooldownSeconds() + 1);
			fireballMessage = ChatColorHelper.color(fireballAbility.getAbilityName() + "s &rregenerate in &e" + remainingTime + "s");
		} else {
			if (fireballCounter == 0) return;
			String plural =  fireballCounter > 1 ? "s" : "";
			fireballMessage = ChatColorHelper.color("&e" + fireballCounter + " &rmore " + fireballAbility.getAbilityName() + plural);
		}

		// Rise Ability Message
		String riseMessage;
		if (!riseAbility.isReady()) {
			int remainingTime = (int) riseAbility.getCooldownInstance().getRemainingCooldownSeconds();
			riseMessage = ChatColorHelper.color(riseAbility.getAbilityName() + " &rregenerates in &e" + (remainingTime + 1) + "s");
		} else {
			riseMessage = ChatColorHelper.color("&rYou can use " + riseAbility.getAbilityName());
		}

		String wholeMessage = riseMessage + ChatColor.DARK_GRAY + " ┃ " + fireballMessage;
		getActionBarManager().setActionBar(player, "blaze.cooldown", wholeMessage, 2);

	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntityType() == EntityType.ARROW) {
			event.setCancelled(true);

			if (fireballAbility.isReady()) {
				SmallFireball fireball = player.launchProjectile(SmallFireball.class);
				fireball.setIsIncendiary(false);
				fireballCounter--;
				if (fireballCounter == 0) {
					fireballAbility.use();
					fireballCounter = FIREBALL_AMOUNT;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;

		if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
			// Fireball Ability
			if (item.equals(weapon)) {
				if (!riseAbility.isReady()) return;

				useRiseAbility();
				riseAbility.use();
			}

			if (item.equals(armyItem)) useArmyAbility();
		}
	}

	private void useRiseAbility() {
		launchFireballs(2, 0);
		launchFireballs(4, 10);
        launchPlayerUp();
	}

	private void launchPlayerUp() {
		player.setVelocity(new Vector(0, 1.6, 0).multiply(1.0D));
	}

	private void launchFireballs(int count, long delay) {
		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
			for (int i= 0; i < count; i++) {
				launchFireball();
				playBlazeEffect();
			}
		}, delay);
	}

	private void launchFireball() {
		SmallFireball fireball = player.launchProjectile(SmallFireball.class);
		fireball.setIsIncendiary(false);
	}

	private void playBlazeEffect() {
		for (Player gamePlayer : instance.players) gamePlayer.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 1);
	}

	private void useArmyAbility() {
		// Spawning Blazes
		Location location = player.getLocation();
		spawnBlazes(location);
		// Removing item
		int slot = player.getInventory().first(armyItem);
		player.getInventory().clear(slot);
		// Playing sound
		player.playSound(player.getLocation(), Sound.FIZZ, 1, 0);
	}

	private void spawnBlazes(Location loc) {
		List<Blaze> blazes = new ArrayList<>();
		armyAbility.sendCustomMessage("&2&l(!) &rYou spawned your &6&lBlaze Army");

		// Helper method to spawn a Blaze with given location
		Consumer<Location> spawnBlaze = (spawnLoc) -> {
			Blaze blaze = (Blaze) player.getWorld().spawnCreature(spawnLoc, EntityType.BLAZE);
			blaze.setCustomName(ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Blaze Army");
			blaze.setTarget(instance.getNearestPlayer(player, blaze, 150));
			blazes.add(blaze);
		};

		// Spawn blazes at offsets
		spawnBlaze.accept(new Location(player.getWorld(), loc.getX(), loc.getY(), loc.getZ() + 1));
		spawnBlaze.accept(new Location(player.getWorld(), loc.getX(), loc.getY(), loc.getZ() - 1));
		spawnBlaze.accept(new Location(player.getWorld(), loc.getX() + 1, loc.getY(), loc.getZ()));


		// Remove all blazes after duration
		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
			blazes.stream()
					.filter(b -> b != null && !b.isDead())
					.forEach(Blaze::remove);
			armyAbility.sendCustomMessage("&2&l(!) &rYour &6&lBlaze Army &rdespawned");
		}, (long) (ARMY_ABILITY_DURATION * 20));
	}


	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public ClassType getType() {
		return ClassType.Blaze;
	}
}