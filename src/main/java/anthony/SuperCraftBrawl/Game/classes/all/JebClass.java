package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

public class JebClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack pushItem;
	private final Ability pullAbility = new Ability("&8&lJeb's Call", 10, player);
	private static final double PUSH_ABILITY_RANGE = 25;

	public JebClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlN2Y3OTdlOTJhOTk1NmU5MTUxYjM1YmJhZWMwMTIzNjVhOTAyY2U4OTc5MGRhYjVhNDc3ODliZWQ5NzE5MCJ9fX0=",
				"543727",
				"D6C374",
				"543727",
				6,
				"Jeb"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.STONE_SWORD),
				"&8&lJeb's Sword"
		);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		ItemHelper.setUnbreakable(weapon);

		// Push Ability
		String displayRange = ItemHelper.formatDouble(PUSH_ABILITY_RANGE);

		pushItem = ItemHelper.setDetails(
				new ItemStack(Material.STONE),
				pullAbility.getAbilityNameRightClickMessage(),
				"&7Shoot a beam that pushes enemies",
				"",
				"&7Stronger at close distances",
				"&7Range: &a" + displayRange + " &7blocks"
		);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		pullAbility.getCooldownInstance().reset();
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, pushItem);
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1));
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		if (!(player.getActivePotionEffects().contains(PotionEffectType.WEAKNESS)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1));

		pullAbility.updateActionBar(player, this);

		if (!pullAbility.isReady()) return;
		int stoneSlot = player.getInventory().first(Material.STONE);
		if (player.getInventory().getItem(stoneSlot).getDurability() == (short) 0) return;
		player.getInventory().getItem(stoneSlot).setDurability((short) 0);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;
		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;
		// Push Ability
		if (item.equals(pushItem)) {
			if (!pullAbility.isReady()) return;

			usePushAbility();
			item.setDurability((short) 5);
			pullAbility.use();
		}

		if (item.getType() == Material.STONE) {
			if (item.getDurability() != (short) 5) return;
			int remainingCooldown = (int) pullAbility.getCooldownInstance().getRemainingCooldownSeconds();
			pullAbility.sendCustomMessage("&l&c(!) &rYou're still on cooldown for &e" + remainingCooldown + "s"
			);
		}
	}

	private void usePushAbility() {
		Location endLocation = findEndLocation();
		SoundManager.playSoundToAll(player, Sound.DIG_STONE, 1, 1);
		displayParticlesAlongPath(endLocation);
		applyPushEffectToPlayers(endLocation);
	}

	private Location findEndLocation() {
		Location startLocation = player.getEyeLocation();
		BlockIterator blockIterator = new BlockIterator(startLocation, 0, (int) PUSH_ABILITY_RANGE);
		Location endLocation = startLocation;

		while (blockIterator.hasNext()) {
			Block block = blockIterator.next();
			endLocation = block.getLocation();

			if (block.getType().isSolid()) {
				break;
			}
		}

		return endLocation;
	}

	private void displayParticlesAlongPath(Location endLocation) {
		Vector direction = player.getEyeLocation().getDirection();
		double maxDistance = endLocation.distance(player.getEyeLocation());

		for (double t = 1; t < maxDistance; t += 0.5) {
			ParticleEffect.BLOCK_CRACK.display(
					player.getEyeLocation().add(direction.clone().multiply(t)),
					0.0F, 0.0F, 0.0F, 0.0F, 1,
					new BlockTexture(Material.STONE)
			);
		}
	}

	private void applyPushEffectToPlayers(Location endLocation) {
		Vector direction = player.getEyeLocation().getDirection();
		double maxDistance = endLocation.distance(player.getEyeLocation());

		for (Player targetPlayer : instance.players) {
			if (targetPlayer == player) continue;
			Vector playerVector = targetPlayer.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
			double distance = playerVector.dot(direction);

			if (distance < maxDistance) {
				Location closestPoint = player.getEyeLocation().add(direction.clone().multiply(distance));

				if (closestPoint.distanceSquared(targetPlayer.getLocation().add(0, 1, 0)) <= 1.5 * 1.5) {
					if (shouldApplyPushEffect(targetPlayer)) {
						applyPushEffect(targetPlayer, direction, distance);
					}
				}
			}
		}
	}

	private void applyPushEffect(Player targetPlayer, Vector direction, double distance) {
		boolean isOnGround = targetPlayer.isOnGround();
		Vector velocity = getVelocity(isOnGround, direction, distance);
		targetPlayer.setVelocity(velocity);
		targetPlayer.playSound(targetPlayer.getLocation(), Sound.DIG_STONE, 1, 1);
	}

	private Vector getVelocity(Boolean isOnGround, Vector direction, double distance) {
		Vector velocity = direction.clone();

		double minVelocity;
		double maxVelocity;

		if (isOnGround) {
			minVelocity = 2.0;
			maxVelocity = 3.5;
		} else {
			minVelocity = 1.0;
			maxVelocity = 2.5;
		}

		double velocityMagnitude = calculateVelocityMagnitude(minVelocity, maxVelocity, distance);
		velocity.multiply(velocityMagnitude);

		return velocity;
	}

	private double calculateVelocityMagnitude(double minVelocity, double maxVelocity, double distance) {
		double magnitude;

		magnitude = maxVelocity * (1 - (distance / PUSH_ABILITY_RANGE));

		magnitude = Math.min(magnitude, maxVelocity);
		magnitude = Math.max(magnitude, minVelocity);

		return magnitude;
	}

	private boolean shouldApplyPushEffect(Player targetPlayer) {
		if (instance.duosMap != null) {
			return !instance.team.get(targetPlayer).equals(instance.team.get(player));
		}
		return true;
	}

	@Override
	public ClassType getType() {
		return ClassType.Jeb;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
