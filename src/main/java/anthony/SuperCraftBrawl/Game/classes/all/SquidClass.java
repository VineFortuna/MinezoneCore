package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;
import java.util.Random;

public class SquidClass extends BaseClass {

	// Ink
	/** Seconds until one more ink is back. */
	private static final double INK_COOLDOWN = 4;
	private static final int MAX_INK_AMOUNT = 4;
	private static final int INK_PELLETS = 8;
	private static final double INK_PELLET_SPEED = 1.5;
	/** Pellets and their trails are gone after this long, so the ink is a short range weapon. */
	private static final int INK_PELLET_LIFETIME_TICKS = 10;
	/** How far a pellet can be off the aim for every block it flies, in a circle around the aim. */
	private static final double INK_SPREAD = 0.2;
	/** How fast the squid dashes away from where it looks. */
	private static final double INK_DASH_SPEED = 0.9;
	/** The least upward speed of a dash from the ground, since the friction of the ground stops one along it right away. */
	private static final double INK_DASH_MIN_LIFT = 0.25;
	/** The ink is black and dark gray, mixed. The black is not exactly 0, which the client draws as red. */
	private static final Color[] INK_COLORS = {new Color(1, 1, 1), new Color(64, 64, 64)};
	/** The trail of a pellet is dashes with gaps between them, which makes it use few particles. */
	private static final double INK_TRAIL_DASH_LENGTH = 0.4;
	private static final double INK_TRAIL_DASH_PERIOD = 3.5;
	private static final int INK_BURST_PARTICLES = 10;
	private static final PotionEffect BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 0, false, true);

	/** The weapon is also the Ink ability, and the amount of it is the ink left. */
	private final ItemStack weapon;
	/** Takes the place of the weapon while there is no ink left. */
	private final ItemStack outOfInkWeapon;
	private final Ability inkAbility = new Ability("&1&lInk", INK_COOLDOWN, player);
	private final Random random = new Random();
	private int ink = MAX_INK_AMOUNT;

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
		String regenerationDisplay = ItemHelper.formatDouble(INK_COOLDOWN);

		weapon = ItemHelper.setDetails(
				new ItemStack(Material.INK_SACK),
				inkAbility.getAbilityNameRightClickMessage(),
				"&7Shoot a spread of ink pellets that",
				"&7apply &8&o" + ItemHelper.getPotionEffectName(BLINDNESS.getType()) + " &7for &e" + BLINDNESS.getDuration() / 20 + "s &7on hit",
				"",
				"&7You dash opposite to where you look",
				"",
				"&7Regenerates &e1 &7every &a" + regenerationDisplay + "&as"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);

		outOfInkWeapon = ItemHelper.setDetails(
				new ItemStack(Material.INK_SACK),
				"&c&lOut of Ink!",
				"",
				"&7Ink regenerates every &a" + regenerationDisplay + "&as"
		);
		outOfInkWeapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		outOfInkWeapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
	}

	@Override
	public void setItems(Inventory playerInv) {
		// Every life starts with all the ink
		ink = MAX_INK_AMOUNT;
		inkAbility.getCooldownInstance().reset();
		showInk(playerInv);
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		regenerateInk();
		inkAbility.updateActionBar(player, this);
	}

	/** One more ink every INK_COOLDOWN seconds, until it is all there. */
	private void regenerateInk() {
		if (ink >= MAX_INK_AMOUNT || !inkAbility.isReady()) return;

		ink++;
		if (ink < MAX_INK_AMOUNT) inkAbility.use(); // The next one starts coming back
		showInk(player.getInventory());
		player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 1.5f);
	}

	/** Puts the weapon in the first slot with the ink that is left as its amount, or the one for no ink. */
	private void showInk(Inventory inventory) {
		ItemStack shown = ink > 0 ? weapon.clone() : outOfInkWeapon;
		if (ink > 0) shown.setAmount(ink);
		inventory.setItem(0, shown);
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null || player.getGameMode() == GameMode.SPECTATOR) return;
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
		if (!item.isSimilar(weapon) && !item.isSimilar(outOfInkWeapon)) return;

		event.setCancelled(true);
		if (ink <= 0) return;

		useInkAbility();
	}

	private void useInkAbility() {
		// The first shot after a full load starts the ink coming back
		if (ink == MAX_INK_AMOUNT) inkAbility.use();
		ink--;
		showInk(player.getInventory());

		Location eye = player.getEyeLocation();
		Vector aim = eye.getDirection();

		shootPellets(eye, aim);
		showInkBurst(eye, aim);
		player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 1f, 1f);

		player.setVelocity(dashVelocity(aim, player.isOnGround()));
	}

	/** Away from where the squid looks: looking forward it dashes back, looking down it dashes up. */
	static Vector dashVelocity(Vector aim, boolean onGround) {
		Vector dash = aim.clone().multiply(-INK_DASH_SPEED);
		if (onGround && dash.getY() < INK_DASH_MIN_LIFT) dash.setY(INK_DASH_MIN_LIFT);
		return dash;
	}

	/**
	 * Every pellet goes to a random spot of a circle around the aim, so together they cover a roundish area that
	 * is small near the squid and wider further away: easy to hit with up close.
	 */
	private void shootPellets(Location from, Vector aim) {
		for (int i = 0; i < INK_PELLETS; i++) {
			// The square root keeps the pellets evenly spread over the circle instead of bunched in the middle
			double angle = random.nextDouble() * 2 * Math.PI;
			double offset = INK_SPREAD * Math.sqrt(random.nextDouble());

			instance.getGameManager().getProjManager().shootProjectile(
					createPellet(), from, pelletDirection(aim, angle, offset).multiply(INK_PELLET_SPEED));
		}
	}

	/** The way a pellet flies: the aim turned a bit towards the point of a circle around it, at this angle and distance from its middle. */
	static Vector pelletDirection(Vector aim, double angle, double offset) {
		// Perpendicular to the aim, the offset goes along these. Aiming (nearly) straight up or down, sideways is
		// worked out from another axis, since "up" is no use for that then.
		Vector helper = Math.abs(aim.getY()) > 0.99 ? new Vector(1, 0, 0) : new Vector(0, 1, 0);
		Vector right = aim.clone().crossProduct(helper).normalize();
		Vector up = right.clone().crossProduct(aim).normalize();

		return aim.clone()
				.add(right.multiply(Math.cos(angle) * offset))
				.add(up.multiply(Math.sin(angle) * offset))
				.normalize();
	}

	private ItemProjectile createPellet() {
		ItemProjectile pellet = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				if (hit == null || !isEnemy(hit)) return;

				// No damage, it only hurts them for the knockback, like a snowball
				hit.damage(0, player);
				hit.addPotionEffect(BLINDNESS);
			}
		}, new ItemStack(Material.INK_SACK), INK_PELLET_LIFETIME_TICKS);
		pellet.setTrailColor(INK_COLORS);
		pellet.setTrailDashes(INK_TRAIL_DASH_LENGTH, INK_TRAIL_DASH_PERIOD);
		return pellet;
	}

	/** A puff of black in front of the squid where the pellets come out of. */
	private void showInkBurst(Location from, Vector aim) {
		for (int i = 0; i < INK_BURST_PARTICLES; i++) {
			Vector scatter = new Vector(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).multiply(0.5);
			Location point = from.clone().add(aim.clone().multiply(0.6 + random.nextDouble())).add(scatter);

			ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
			particle.setColor(INK_COLORS[random.nextInt(INK_COLORS.length)]);
			particle.setLocation(point);
			particle.setOffset(0.0f, 0.0f, 0.0f);
			particle.display(instance.players);
		}
	}

	private boolean isEnemy(Player other) {
		return other != player
				&& !checkIfDead(other, instance)
				&& !instance.HasSpectator(other)
				&& !instance.getGameManager().spawnProt.containsKey(other)
				&& (instance.duosMap == null || !instance.team.get(other).equals(instance.team.get(player)));
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
