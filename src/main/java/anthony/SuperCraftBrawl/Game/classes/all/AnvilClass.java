package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class AnvilClass extends BaseClass {

    private final ItemStack weapon;
    private final ItemStack stompItem;
    private final Ability stompAbility = new Ability("&8&lGoomba Stomp", 10, player);
	private static final double MAX_STOMP_DAMAGE = 8 * 2.0;
	private static final double STOMP_X_Z_RANGE = 3;
	private static final double STOMP_Y_RANGE = 1;
	private boolean stompUsed;
	private double stompDamage = 0;
	private int initialHeight;

	public AnvilClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				Material.IRON_BLOCK,
				null,
				"373638",
				6,
				"Anvil"
		);

        // Weapon
        weapon = ItemHelper.setDetails(
                new ItemStack(Material.WOOD_SWORD),
                "&8&lGoomba Sword"
        );
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        ItemHelper.setUnbreakable(weapon);

        // Stomp Ability

		String displayRangeWide = ItemHelper.formatDouble(STOMP_X_Z_RANGE);
		String displayRangeY = ItemHelper.formatDouble(STOMP_Y_RANGE + 1);

        stompItem = ItemHelper.setDetails(
                new ItemStack(Material.ANVIL),
                stompAbility.getAbilityNameRightClickMessage(),
                "&7Slam down on your enemies",
                "",
                "&7The damage increases with height",
				"&7Range: &e" + displayRangeWide + " &7blocks wide",
				"&7Range: &e" + displayRangeY + " &7blocks up and down"
        );
	}

	public void SetItems(Inventory playerInv) {
        stompAbility.getCooldownInstance().reset();
		this.stompUsed = false;
		this.stompDamage = 0;
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, stompItem);
	}

	public void Tick(int gameTicks) {
        if (!isPlayerAlive()) return;
        stompAbility.updateActionBar(player, this);
		if (!stompUsed) return;
		if (!player.isOnGround()) return;

		stompUsed = false;
		stompDamage = calculateDamage(player.getLocation().getBlockY());
		applyStompEffectToNearbyPlayers();
		playEffectsForAllPlayers();
		stompDamage = 0;
	}

	private void applyStompEffectToNearbyPlayers() {
		List<Entity> nearbyEntities = player.getNearbyEntities(STOMP_X_Z_RANGE, STOMP_Y_RANGE, STOMP_X_Z_RANGE);
		for (Entity entity : nearbyEntities) {
			if (!(entity instanceof Player)) continue;
			Player playerInRange = (Player) entity;
			if (playerInRange == player) continue;
			applyStompToPlayer(playerInRange);
		}
	}

	private void applyStompToPlayer(Player playerInRange) {
		playerInRange.setVelocity((new Vector(0, 1, 0)).multiply(0.5D));
		// Damage
		EntityDamageEvent damageEvent = new EntityDamageEvent(
				playerInRange, EntityDamageEvent.DamageCause.CUSTOM, stompDamage
		);

		instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
		double currentHealth = playerInRange.getHealth();
//		stompDamage = Math.min(stompDamage, currentHealth);
		double finalHealth = currentHealth - stompDamage;
//		player.sendMessage("Stomp Damage: " + stompDamage);
//		player.sendMessage("Current Health: " + currentHealth);
//		player.sendMessage("Final Health Before Check: " + finalHealth);
//		playerInRange.damage(0, player);
		if (finalHealth <= 0) {
//			if (finalHealth == 0) player.sendMessage("Equals Zero");
//			else player.sendMessage("Below Zero");
			playerInRange.setHealth(0);
		} else {
			playerInRange.damage(0, player);
			playerInRange.setHealth(finalHealth);
		}
	}

	private void playEffectsForAllPlayers() {
		for (Player gamePlayer : instance.players) {
			gamePlayer.playEffect(player.getLocation(), Effect.TILE_BREAK, 1);
		}
		SoundManager.playSoundToAll(player, Sound.ANVIL_LAND, 1, 1);
	}

	public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

		if (!item.equals(stompItem)) return;
        if (!stompAbility.isReady()) return;
        if (player.isOnGround()) {
			stompAbility.sendCustomMessage("&c&l(!) You can't be on ground to use " + stompAbility.getAbilityName());
            return;
        }

        useStompAbility();
        stompAbility.use();
        this.stompUsed = true;
	}

    private void useStompAbility() {
		initialHeight = player.getLocation().getBlockY();
		performStompAbility(player);
    }

	private double calculateDamage(int currentHeight) {
		int deltaHeight = initialHeight - currentHeight;
		double damage;
		if (deltaHeight <= 12) {
			damage = deltaHeight * 0.4;
		} else {
			damage = deltaHeight * 0.33;
		}
//		player.sendMessage("CurrentHeight: " + currentHeight);
//		player.sendMessage("DeltaHeight: " + deltaHeight);
//		player.sendMessage("Damage: " + damage);
		return Math.min(damage, MAX_STOMP_DAMAGE);
	}

	private void performStompAbility(Player player) {
		player.setVelocity((new Vector(0.0D, -1.5D, 0.0D)).multiply(1.0D));
		player.playEffect(player.getLocation(), Effect.TILE_BREAK, 1);
	}

    public ClassType getType() {
        return ClassType.Anvil;
    }

    public ItemStack getAttackWeapon() {
        return weapon;
    }
}
