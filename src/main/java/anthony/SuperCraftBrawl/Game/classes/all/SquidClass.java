package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SquidClass extends BaseClass {
	private int cooldownSec = 0;
	private long inkCooldown;

	private final ItemStack weapon;
	private final Ability inkAbility = new Ability("&1&lInk", 10, player);
	private static final double INK_ABILITY_RANGE = 10;
	private static final double INK_ABILITY_DURATION = 2;
	private final PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, (int) (INK_ABILITY_DURATION * 20), 0, false, true);

	public SquidClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDA4YTljODYzNDkyMTVjYjk0NjM2YWFmYzViYzY2NDRlODI5YTI4MzczYzU0NWZmZGNhOWZlZWQ1OTRiZjNhIn19fQ==",
				"516575",
				6,
				"Squid"
		);
		playerHead.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);

		// Weapon
		String rangeDisplay = ItemHelper.formatDouble(INK_ABILITY_RANGE);

		weapon = ItemHelper.setDetails(
				new ItemStack(Material.INK_SACK),
				inkAbility.getAbilityNameRightClickMessage(),
				"",
				"&7Blind enemies around you",
				"",
				"&8&oBlindness &7for &e" + blindness.getDuration() / 20 + "s",
				"&7Range: &a" + rangeDisplay + " &7blocks"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, weapon);
	}
	
	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		inkAbility.updateActionBar(player, this);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;

		if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
			// Ink Ability
			if (item.equals(weapon)) {
				if (!inkAbility.isReady()) return;

				searchForPlayers();
			}
		}
	}

	private void searchForPlayers() {
		boolean foundPlayers = false;

		for (Entity entity : player.getWorld().getNearbyEntities(
				player.getLocation(),
				INK_ABILITY_RANGE,
				INK_ABILITY_RANGE,
				INK_ABILITY_RANGE
		)) {
			if (entity instanceof Player && !entity.equals(player)) {
				Player playerInRange = (Player) entity;
				if (isPlayerAlive()) {
					useInkAbility(playerInRange);
					foundPlayers = true;
				}
			}
		}
		if (foundPlayers) {
			player.getWorld().playEffect(player.getLocation(), Effect.SPLASH, 20);
			player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 1f, 1f);
		} else player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
	}

	private void useInkAbility(Player playerInRange) {
		inkAbility.use();
		playerInRange.addPotionEffect(blindness);
		spawnInkParticles(playerInRange);
	}

	private void spawnInkParticles(Player playerInRange) {
		Location playerLocation = playerInRange.getEyeLocation();
		double radius = 1.5;
		int particleCount = 20;

		for (int i = 0; i < particleCount; i++) {
			double angle = 2 * Math.PI * i / particleCount;
			double x = radius * Math.cos(angle);
			double z = radius * Math.sin(angle);

			Location particleLoc = playerLocation.clone().add(x, 0, z);

			playerInRange.getWorld().spigot().playEffect(particleLoc, Effect.SMOKE, 0, 0, 0, 0, 0, 0, 1, 30);
			player.getWorld().spigot().playEffect(particleLoc, Effect.SMOKE, 0, 0, 0, 0, 0, 0, 1, 30);
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Squid;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
