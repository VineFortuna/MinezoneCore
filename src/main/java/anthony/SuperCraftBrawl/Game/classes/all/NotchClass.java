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

public class NotchClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack pullItem;
	private final Ability pullAbility = new Ability("&3&lCollapse X-Axis", 10, player);
	private static final double PULL_ABILITY_RANGE = 25;


	public NotchClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTlkNDhkYWMyMDI1NDQ1Y2NlZjhiYzJiYWY1NjZlZDlmZWMyM2Q5MWZkNWQyMmNiN2I5YmE2MzIyYjI5ZiJ9fX0=",
				"744832",
				"8E8E8E",
				"262626",
				6,
				"Notch"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.STONE_SWORD),
				"&3&lNotch's Sword"
		);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		ItemHelper.setUnbreakable(weapon);

		// Pull Ability
		String displayRange = ItemHelper.formatDouble(PULL_ABILITY_RANGE);

		pullItem = ItemHelper.setDetails(
				new ItemStack(Material.GRASS),
				pullAbility.getAbilityNameRightClickMessage(),
				"&7Shoot a beam that pulls enemies",
				"",
				"&7Stronger at farther distances",
				"&7Range: &a" + displayRange + " &7blocks"
		);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		pullAbility.getCooldownInstance().reset();
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, pullItem);
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1));
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		if (!(player.getActivePotionEffects().contains(PotionEffectType.WEAKNESS)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1));

		pullAbility.updateActionBar(player, this);

		if (!player.getInventory().contains(Material.DIRT)) return;
		if (!pullAbility.isReady()) return;
		int dirtSlot = player.getInventory().first(Material.DIRT);
		player.getInventory().getItem(dirtSlot).setType(Material.GRASS);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;
		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;
		// Pull Ability
		if (item.equals(pullItem)) {
			if (!pullAbility.isReady()) return;

			usePullAbility();
			item.setType(Material.DIRT);
			pullAbility.use();
		}

		if (item.getType() == Material.DIRT) {
			int remainingCooldown = (int) pullAbility.getCooldownInstance().getRemainingCooldownSeconds();
			pullAbility.sendCustomMessage("&l&c(!) &rYou're still on cooldown for &e" + remainingCooldown + "s"
			);
		}
	}

	private void usePullAbility() {
		Location endLocation = findEndLocation();
	 	SoundManager.playSoundToAll(player, Sound.DIG_GRASS, 1, 1);
		displayParticlesAlongPath(endLocation);
		applyPullEffectToPlayers(endLocation);
	}

	private Location findEndLocation() {
		Location startLocation = player.getEyeLocation();
		BlockIterator blockIterator = new BlockIterator(startLocation, 0, (int) PULL_ABILITY_RANGE);
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
					new BlockTexture(Material.GRASS)
			);
		}
	}

	private void applyPullEffectToPlayers(Location endLocation) {
		Vector direction = player.getEyeLocation().getDirection();
		double maxDistance = endLocation.distance(player.getEyeLocation());

		for (Player targetPlayer : instance.players) {
			if (targetPlayer == player) continue;
			if (instance.getGameManager().spawnProt.containsKey(targetPlayer)) continue;
			Vector playerVector = targetPlayer.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
			double distance = playerVector.dot(direction);

			if (distance < maxDistance) {
				Location closestPoint = player.getEyeLocation().add(direction.clone().multiply(distance));

				if (closestPoint.distanceSquared(targetPlayer.getLocation().add(0, 1, 0)) <= 1.5 * 1.5) {
					if (shouldApplyPullEffect(targetPlayer)) {
						applyPullEffectReworkTest(targetPlayer, direction, distance);
					}
				}
			}
		}
	}

	private void applyPullEffectReworkTest(Player targetPlayer, Vector direction, double distance) {
		boolean isOnGround = targetPlayer.isOnGround();
		Vector velocity = getVelocity(isOnGround, direction, distance);
		targetPlayer.setVelocity(velocity);
		targetPlayer.playSound(targetPlayer.getLocation(), Sound.DIG_STONE, 1, 1);
	}

	private Vector getVelocity(Boolean isOnGround, Vector direction, double distance) {
		Vector velocity = direction.clone().multiply(-1);

		double minVelocity;
		double maxVelocity;

		if (isOnGround) {
			if (distance <= 8) {
				minVelocity = 2.5;
			} else {
				minVelocity = 3.5;
			}
			maxVelocity = 6.5;
		} else {
			minVelocity = 1.0;
			maxVelocity = 3.5;
		}

		double velocityMagnitude = calculateVelocityMagnitude(minVelocity, maxVelocity, distance);
		velocity.multiply(velocityMagnitude);

		return velocity;
	}

	private double calculateVelocityMagnitude(double minVelocity, double maxVelocity, double distance) {
		double magnitude;

		magnitude = maxVelocity * (distance / PULL_ABILITY_RANGE);

		magnitude = Math.min(magnitude, maxVelocity);
		magnitude = Math.max(magnitude, minVelocity);
//		// Debug message
//		player.sendMessage("Magnitude: " + magnitude + ", Distance: " + distance);

		return magnitude;
	}

	private boolean shouldApplyPullEffect(Player targetPlayer) {
		if (instance.duosMap != null) {
			return !instance.team.get(targetPlayer).equals(instance.team.get(player));
		}
		return true;
	}

	@Override
	public ClassType getType() {
		return ClassType.Notch;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}