package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Banner;
import org.bukkit.material.Door;
import org.bukkit.material.Skull;
import org.bukkit.util.Vector;

public class EndermanClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack teleportItem;
	private final ItemStack blockItem;
	private final Ability teleportAbility = new Ability("&5&lTeleport", 10, player);
	private final Ability blockAbility = new Ability("&5&lBlock", 10, player);
	private final Ability blockThrowAbility = new Ability("&5&lBlock Throw", player);

	private ItemStack newItem = null;
	private boolean used = false;

	public EndermanClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E1OWJiMGE3YTMyOTY1YjNkOTBkOGVhZmE4OTlkMTgzNWY0MjQ1MDllYWRkNGU2YjcwOWFkYTUwYjljZiJ9fX0",
				"101010",
				6,
				"Enderman"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.EYE_OF_ENDER),
				"&5&lEnderman Soul"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Teleport Ability
		teleportItem = ItemHelper.setDetails(
				new ItemStack(Material.ENDER_PEARL, 10),
				teleportAbility.getAbilityNameRightClickMessage()
		);

		// Block Ability
		blockItem = ItemHelper.setDetails(
				new ItemStack(Material.STICK),
				blockAbility.getAbilityName() + " Pickup &7(Right click)",
				"&7Grab the block you're standing on",
				"&7You can throw it to damage and knock players",
				"",
				blockAbility.getOnGroundItemMessage()
		);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		used = false;
		teleportAbility.getCooldownInstance().reset();
		blockAbility.getCooldownInstance().reset();
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, blockItem);
		playerInv.setItem(2, teleportItem);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		// ActionBar
		ActionBarManager actionBarManager = this.getActionBarManager();
		ActionBarManager.AbilityActionBar abilityActionBar = new ActionBarManager.AbilityActionBar(this, actionBarManager);
		abilityActionBar.setActionBarAbility(player, teleportAbility, blockAbility);

		// Check if player didn't throw the eye of ender
		Inventory inventory = player.getInventory();
		if (inventory.contains(weapon)) return;
		player.getInventory().setItem(0, weapon);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;

		if (item.equals(weapon)) event.setCancelled(true);
		if (item.equals(blockItem)) {
			if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;
			if (used) {
				blockAbility.sendCustomMessage("&c&l(!) &rYou already have a block in your inventory!");
				return;
			}
			if (!player.isOnGround()) {
				blockAbility.sendCustomMessage(blockAbility.getOnGroundChatMessage());
				return;
			}
			if (!blockAbility.isReady()) return;
			getBlockAbility();
		}
		if (item.isSimilar(teleportItem)) {
			if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;
			if (!teleportAbility.isReady()) {
				event.setCancelled(true);
				return;
			}
			teleportAbility.use();
		}
		if (item.hasItemMeta() && item.isSimilar(newItem)) {
			throwBlockAbility();
		}
	}

	private void getBlockAbility() {
		Location playerLocation = player.getLocation();
		World playerWorld = player.getWorld();
		Block block = playerWorld.getBlockAt(playerLocation.getBlockX(), playerLocation.getBlockY() - 1, playerLocation.getBlockZ());
		Block blockInside = playerWorld.getBlockAt(playerLocation);

		if (blockInside.getType().isSolid() && playerLocation.getY() % 1 != 0) {
			newItem = new ItemStack(blockInside.getType(), 1);
		} else if (block.getType().isSolid()) {
			newItem = new ItemStack(block.getType(), 1);
		} else {
			player.sendMessage(instance.getGameManager().getMain()
					.color("&c&l(!) &rThere is no block under you. Please try again."));
			return;
		}
		if (newItem.getType() == Material.DOUBLE_STEP || newItem.getType() == Material.DOUBLE_STONE_SLAB2 ||
				newItem.getType() == Material.WOOD_DOUBLE_STEP)
			newItem.setType(Material.valueOf(newItem.getType().name().replace("DOUBLE_", "")));
		else if (newItem.getData() instanceof Door)
			newItem.setType(Material.WOOD_DOOR);
		else if (newItem.getData() instanceof Skull)
			newItem.setType(Material.SKULL_ITEM);
		else if (newItem.getType() == Material.SOIL)
			newItem.setType(Material.DIRT);
		else if (newItem.getType() == Material.BED_BLOCK)
			newItem.setType(Material.BED);
		else if (newItem.getData() instanceof Banner)
			newItem.setType(Material.BANNER);
		else if (newItem.getType() == Material.BREWING_STAND)
			newItem.setType(Material.BREWING_STAND_ITEM);
		else if (newItem.getType() == Material.CAULDRON)
			newItem.setType(Material.CAULDRON_ITEM);
		ItemHelper.setDetails(newItem, instance.getGameManager().getMain().color(
				"&e&lBlock"));
		ItemHelper.setDetails(newItem, blockThrowAbility.getAbilityNameLeftRightClickMessage());
		player.getInventory().addItem(newItem);
		player.sendMessage(
				instance.getGameManager().getMain().color("&r&l(!) &rYou picked up &e1 " +
						WordUtils.capitalizeFully(newItem.getType().name().replace('_', ' ').replaceAll("[0-9]", ""))));
		blockAbility.use();
		used = true;
	}

	private void throwBlockAbility() {
		Vector direction = player.getLocation().getDirection();
		player.getInventory().remove(newItem);
		ItemProjectile itemProjectile = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hitPlayer) {
				if (instance.duosMap != null)
					if (instance.team.get(hitPlayer).equals(instance.team.get(player)))
						return;

				if (hitPlayer == null) return;

				player.playSound(hitPlayer.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
				Location location = hitPlayer.getLocation();
				EntityDamageEvent damageEvent = new EntityDamageEvent(hitPlayer, DamageCause.PROJECTILE, 4.5);
				instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
				hitPlayer.damage(4.5, player);
				direction.setY(1.0);
				hitPlayer.setVelocity(direction);
				SoundManager.playSoundToAll(player, location, Sound.CHICKEN_EGG_POP, 1, 1);
			}

		}, new ItemStack(newItem));
		instance.getGameManager().getProjManager().shootProjectile(itemProjectile, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(2.0D));

		newItem = null;
		used = false;
	}

	@Override
	public ClassType getType() {
		return ClassType.Enderman;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
