package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class WitherSkeletonClass extends BaseClass {

	private static final int BOW_SLOT = 1;
	private static final int ARROW_SLOT = 35;

	/** What the Nether Star and the bow both apply on a hit. */
	private static final PotionEffect WITHER = new PotionEffect(PotionEffectType.WITHER, 5 * 20, 0, true);

	// Bow
	private static final int BOW_COOLDOWN_SECONDS = 9;
	/** How often the bar of the bow, which shows the cooldown, is updated. */
	private static final int BOW_BAR_UPDATE_TICKS = 5;
	private static final PotionEffect BOW_SLOWNESS = new PotionEffect(PotionEffectType.SLOW, 10 * 20, 0);
	private static final int RARE_SHOT_CHANCE_PERCENT = 10;
	private static final double NORMAL_SHOT_DAMAGE = 9.0;
	/** The rare shot does double the damage of a normal one. */
	private static final double RARE_SHOT_DAMAGE = NORMAL_SHOT_DAMAGE * 2;
	/** An arrow that lands blasts the players this close to it. */
	private static final double BLAST_DAMAGE = 8.0;
	private static final double BLAST_RANGE_HORIZONTAL = 2.5;
	private static final double BLAST_RANGE_VERTICAL = 2.0;

	/** The extra damage of a bow hit, and the colors of the creeper face that shows which shot it was. */
	private enum Shot {
		NORMAL(NORMAL_SHOT_DAMAGE, Color.GREEN, Color.LIME),
		RARE(RARE_SHOT_DAMAGE, Color.RED, Color.MAROON);

		private final double damage;
		private final Color color;
		private final Color fade;

		Shot(double damage, Color color, Color fade) {
			this.damage = damage;
			this.color = color;
			this.fade = fade;
		}
	}

	private final ItemStack weapon;
	private final ItemStack bow;
	private final Ability bowAbility = new Ability("&8&lBow", BOW_COOLDOWN_SECONDS, player);
	private final Random random = new Random();
	/** The arrows that were shot and haven't landed yet. */
	private final List<Arrow> arrowsInFlight = new ArrayList<>();

	public WitherSkeletonClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjVlYzk2NDY0NWE4ZWZhYzc2YmUyZjE2MGQ3Yzk5NTYzNjJmMzJiNjUxNzM5MGM1OWMzMDg1MDM0ZjA1MGNmZiJ9fX0=",
				"363636",
				6,
				"WitherSkeleton"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.NETHER_STAR),
				"&8&lNether Star",
				"",
				"&7Apply &8&oWither &e" + (WITHER.getAmplifier() + 1) + " &7for &e" + WITHER.getDuration() / 20 + "s"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Bow
		bow = ItemHelper.setDetails(
				new ItemStack(Material.BOW),
				bowAbility.getAbilityNameRightClickMessage(),
				"&7Apply &8&oWither &e" + (WITHER.getAmplifier() + 1) + " &7for &e" + WITHER.getDuration() / 20 + "s",
				"",
				"&7Has a &e" + RARE_SHOT_CHANCE_PERCENT + "% &7chance of doing double damage",
				"",
				"&7Shooting gives you &3&oSlowness&r &e" + (BOW_SLOWNESS.getAmplifier() + 1) + " &7for &e"
						+ BOW_SLOWNESS.getDuration() / 20 + "s"
		);
		bow.addUnsafeEnchantment(Enchantment.DURABILITY, 1000);
		bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		ItemHelper.setHideFlags(bow, true);
	}

	@Override
	public void setItems(Inventory playerInv) {
		bowAbility.getCooldownInstance().reset();
		arrowsInFlight.clear();

		playerInv.setItem(0, weapon);
		playerInv.setItem(BOW_SLOT, bow);
		playerInv.setItem(ARROW_SLOT, new ItemStack(Material.ARROW));
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		bowAbility.updateActionBar(player, this);
		if (gameTicks % BOW_BAR_UPDATE_TICKS == 0) updateBowBar();
		blastLandedArrows();
	}

	/**
	 * The durability bar of the bow is the cooldown: empty when the arrow was just shot, full when it's ready. The bar
	 * is changed on the bow itself, without giving the player a new item, like the Instagib drop does with its hoe.
	 * But any change of the item in hand makes the client drop a bow that is being drawn, so while it is drawn the bar
	 * waits, which lets the player draw it before the cooldown is over.
	 */
	private void updateBowBar() {
		ItemStack current = player.getInventory().getItem(BOW_SLOT);
		if (current == null || current.getType() != Material.BOW) return;
		if (isDrawingBow()) return;

		double remaining = bowAbility.getCooldownInstance().getRemainingCooldownMillis() / (BOW_COOLDOWN_SECONDS * 1000.0);
		short durability = (short) Math.round(Material.BOW.getMaxDurability() * remaining);
		if (current.getDurability() != durability) current.setDurability(durability); // No change, nothing sent
	}

	/** Whether the player is using the item in hand, which for the bow means they are drawing it (v1_8_R3's isUsingItem). */
	private boolean isDrawingBow() {
		return ((CraftPlayer) player).getHandle().bS();
	}

	private void blastLandedArrows() {
		Iterator<Arrow> arrows = arrowsInFlight.iterator();

		while (arrows.hasNext()) {
			Arrow arrow = arrows.next();
			if (!arrow.isValid()) {
				arrows.remove();
			} else if (arrow.isOnGround()) {
				arrows.remove();
				blast(arrow);
				arrow.remove();
			}
		}
	}

	private void blast(Arrow arrow) {
		arrow.getWorld().playEffect(arrow.getLocation(), Effect.EXPLOSION_HUGE, 1);

		for (Entity entity : arrow.getNearbyEntities(BLAST_RANGE_HORIZONTAL, BLAST_RANGE_VERTICAL, BLAST_RANGE_HORIZONTAL)) {
			if (!(entity instanceof Player) || checkIfDead((Player) entity, instance)) continue;

			Player hit = (Player) entity;
			hit.damage(BLAST_DAMAGE, player);
			hit.addPotionEffect(WITHER);
		}
	}

	@Override
	public void projectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Arrow)) return;

		if (!bowAbility.isReady()) {
			event.setCancelled(true);
			return;
		}

		bowAbility.use();
		arrowsInFlight.add((Arrow) event.getEntity());
		SoundManager.playSoundToAll(player, Sound.WITHER_SHOOT, 1, 1);
		player.addPotionEffect(BOW_SLOWNESS);
	}

	@Override
	public void doDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getEntity() == player) return;
		Player hit = (Player) event.getEntity();

		if (event.getDamager() instanceof Arrow) {
			Shot shot = random.nextInt(100) < RARE_SHOT_CHANCE_PERCENT ? Shot.RARE : Shot.NORMAL;
			hit.damage(shot.damage, player);
			hit.addPotionEffect(WITHER);
			showShot(hit.getLocation(), shot);
		} else if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
				&& isPlayerAlive() && weapon.equals(player.getInventory().getItemInHand())) {
			hit.addPotionEffect(WITHER);
		}
	}

	/** A firework with a creeper face goes up from where the arrow hit, the color shows which shot it was. */
	private void showShot(Location location, Shot shot) {
		location.getWorld().playEffect(location, Effect.EXPLOSION_HUGE, 1);

		// Power 0 is the shortest flight there is, so it explodes right away, low to the ground
		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower(0);
		meta.addEffect(FireworkEffect.builder()
				.with(FireworkEffect.Type.CREEPER)
				.withColor(shot.color)
				.withFade(shot.fade)
				.trail(true)
				.flicker(false)
				.build());
		firework.setFireworkMeta(meta);
	}

	@Override
	public ClassType getType() {
		return ClassType.WitherSkeleton;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
