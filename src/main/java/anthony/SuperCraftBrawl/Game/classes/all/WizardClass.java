package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WizardClass extends BaseClass {

	private static final int SPELL_BOOK_COOLDOWN_SECONDS = 25;

	// Teleport
	private static final String TELEPORT_NAME = "&5&lTeleport";
	private static final int TELEPORT_COOLDOWN_SECONDS = 10;
	private static final int TELEPORT_RANGE_BLOCKS = 8;

	// Fireballs
	private static final String FIREBALLS_NAME = "&6&lFireballs";
	private static final int FIREBALL_COOLDOWN_SECONDS = 5;
	private static final double FIREBALL_SPEED = 2.5;
	private static final int FIREBALL_FIRE_TICKS = 100;
	private static final int FIREBALL_LIFETIME_TICKS = 100;

	// Speedy Speedy Jumpy Jumpy
	private static final PotionEffect SPEEDY_SPEED = new PotionEffect(PotionEffectType.SPEED, 999999999, 1);
	private static final PotionEffect SPEEDY_JUMP = new PotionEffect(PotionEffectType.JUMP, 999999999, 5);

	// Darkness: every hit has a 1 in DARKNESS_CHANCE chance to blind
	private static final int DARKNESS_CHANCE = 3;
	private static final PotionEffect DARKNESS_BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 1, true);

	/** The spells the Spell Book picks from. Each one turns the wand into its own wand. */
	private enum Spell {
		TELEPORT(TELEPORT_NAME, 4, 1,
				"&6&lWizard> &rA rift opens before me... Teleport!",
				"&7Right click to teleport forward",
				"",
				"&rRange: &a" + TELEPORT_RANGE_BLOCKS + " blocks"),
		FIREBALLS(FIREBALLS_NAME, 3, 2,
				"&6&lWizard> &rFlames answer my call... &rFireballs!",
				"&7Shoot fireballs, setting enemies on fire"),
		SPEEDY_JUMPY("&b&lSpeedy Jumpy", 3, 1,
				"&6&lWizard> &rLook at me... Speedy speedy jumpy jumpy!",
				"&7Gain &b&o" + ItemHelper.getPotionEffectName(SPEEDY_SPEED.getType()) + " &e" + (SPEEDY_SPEED.getAmplifier() + 1)
						+ " &7and &a&o" + ItemHelper.getPotionEffectName(SPEEDY_JUMP.getType()) + " &e" + (SPEEDY_JUMP.getAmplifier() + 1)),
		DARKNESS("&8&lDarkness", 3, 2,
				"&6&lWizard> &rShadows gather around my enemies... Darkness!",
				"&7Has a &e1/" + DARKNESS_CHANCE + " &7chance to apply",
				"&8&oBlindness&r &7for &e" + DARKNESS_BLINDNESS.getDuration() / 20 + "s");

		private final String announcement;
		private final ItemStack wand;

		Spell(String name, int sharpness, int knockback, String announcement, String... lore) {
			this.announcement = announcement;

			// A blank line, then the lore
			String[] lines = new String[lore.length + 1];
			lines[0] = "";
			System.arraycopy(lore, 0, lines, 1, lore.length);

			wand = ItemHelper.setDetails(new ItemStack(Material.STICK), "&6&lMagic Wand: " + name, lines);
			wand.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpness);
			wand.addUnsafeEnchantment(Enchantment.KNOCKBACK, knockback);
		}
	}

	private final ItemStack plainWand;
	private final ItemStack spellBook;
	private final Ability spellBookAbility = new Ability("&6&lSpell Book", SPELL_BOOK_COOLDOWN_SECONDS, player);
	private final Ability teleportAbility = new Ability(TELEPORT_NAME, TELEPORT_COOLDOWN_SECONDS, player);
	private final Ability fireballAbility = new Ability(FIREBALLS_NAME, FIREBALL_COOLDOWN_SECONDS, player);
	private final Random random = new Random();

	/** The wand in the first slot, the plain one until a spell is picked. */
	private ItemStack weapon;
	/** The spell that was picked, null until the first one. */
	private Spell spell;

	public WizardClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM4NTY0ZTI4YWJhOTgzMDFkYmRhNWZhZmQ4NmQxZGE0ZTJlYWVlZjEyZWE5NGRjZjQ0MGI4ODNlNTU5MzExYyJ9fX0=",
				"C178E8",
				"C178E8",
				"965905",
				6,
				"Wizard"
		);

		plainWand = ItemHelper.setDetails(
				new ItemStack(Material.STICK),
				"&6&lMagic Wand",
				"&7Use your spell book to cast a spell"
		);
		weapon = plainWand;

		spellBook = ItemHelper.setDetails(
				new ItemStack(Material.BOOK),
				spellBookAbility.getAbilityNameRightClickMessage(),
				"&7Cast one of the following spells:",
				"&7▸ &6&oFireballs",
				"&7▸ &5&oTeleport",
				"&7▸ &8&oDarkness",
				"&7▸ &b&oSpeedy Speedy Jumpy Jumpy"
		);
	}

	@Override
	public void setItems(Inventory playerInv) {
		removeSpellEffects();
		spell = null;
		weapon = plainWand;
		spellBookAbility.getCooldownInstance().reset();

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, spellBook);
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		spellBookAbility.updateActionBar(player, this);
		if (spell == Spell.TELEPORT) teleportAbility.updateActionBar(player, this);
		if (spell == Spell.FIREBALLS) fireballAbility.updateActionBar(player, this);

		// Keep speed and jump alive
		if (spell == Spell.SPEEDY_JUMPY) {
			if (!player.hasPotionEffect(PotionEffectType.SPEED)) player.addPotionEffect(SPEEDY_SPEED);
			if (!player.hasPotionEffect(PotionEffectType.JUMP)) player.addPotionEffect(SPEEDY_JUMP);
		}
	}

	@Override
	public void doDamage(EntityDamageByEntityEvent event) {
		if (spell != Spell.DARKNESS || !(event.getEntity() instanceof LivingEntity)) return;
		if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
		if (!weapon.equals(player.getInventory().getItemInHand())) return;

		if (random.nextInt(DARKNESS_CHANCE) == 0)
			((LivingEntity) event.getEntity()).addPotionEffect(DARKNESS_BLINDNESS);
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item == null || !isPlayerAlive()) return;
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		if (item.isSimilar(spellBook)) {
			event.setCancelled(true);
			useSpellBook();
		} else if (item.getType() == Material.STICK) {
			// The wand casts the spell that was picked, the Spell Book is what picks one
			event.setCancelled(true);

			if (spell == null) {
				player.sendMessage(instance.color("&c&l(!) &rUse your &6&lSpell Book &rto choose a spell first."));
			} else if (spell == Spell.TELEPORT) {
				useTeleport();
			} else if (spell == Spell.FIREBALLS) {
				useFireball();
			}
		}
	}

	// -----------------------------------------------------------------------
	//  Spell Book
	// -----------------------------------------------------------------------

	private void useSpellBook() {
		if (!spellBookAbility.isReady()) {
			spellBookAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}

		spellBookAbility.use();
		pickSpell();
	}

	/** Picks a spell other than the one the wizard has, and the wand turns into the wand of it. */
	private void pickSpell() {
		List<Spell> choices = new ArrayList<>(Arrays.asList(Spell.values()));
		choices.remove(spell);
		Spell next = choices.get(random.nextInt(choices.size()));

		removeSpellEffects();
		spell = next;
		weapon = next.wand;

		// The new spell can be cast right away
		teleportAbility.getCooldownInstance().reset();
		fireballAbility.getCooldownInstance().reset();

		if (spell == Spell.SPEEDY_JUMPY) {
			player.addPotionEffect(SPEEDY_SPEED);
			player.addPotionEffect(SPEEDY_JUMP);
		}

		instance.TellAll(instance.color(next.announcement));
		player.getInventory().setItem(0, weapon);
		player.getInventory().setItem(1, spellBook);
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.4f);
	}

	private void removeSpellEffects() {
		if (spell != Spell.SPEEDY_JUMPY) return;

		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.JUMP);
	}

	// -----------------------------------------------------------------------
	//  Teleport
	// -----------------------------------------------------------------------

	private void useTeleport() {
		if (!teleportAbility.isReady()) {
			teleportAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}

		// The last free block of the way the wizard looks, up to the range
		BlockIterator blocks = new BlockIterator(player.getEyeLocation(), 0, TELEPORT_RANGE_BLOCKS);
		Block lastFree = null;

		while (blocks.hasNext()) {
			Block block = blocks.next();
			if (block.getType().isSolid()) break;
			lastFree = block;
		}

		if (lastFree == null) {
			player.sendMessage(instance.color("&c&l(!) &rNo safe space to teleport to!"));
			return;
		}

		if (lastFree.getRelative(BlockFace.UP).getType().isSolid()) {
			player.sendMessage(instance.color("&c&l(!) &rNot enough room to teleport there!"));
			return;
		}

		Location origin = player.getLocation();
		Location destination = lastFree.getLocation().add(0.5, 0, 0.5);
		destination.setYaw(origin.getYaw());
		destination.setPitch(origin.getPitch());

		// Portal rings where the wizard leaves, and where the wizard arrives
		displayPortalRings(origin, 5, 10, 0.4, 0.3, 0.12, 0.35, 5);
		origin.getWorld().playSound(origin, Sound.ENDERMAN_TELEPORT, 1.0f, 0.75f);
		origin.getWorld().playSound(origin, Sound.PORTAL_TRAVEL, 0.3f, 1.8f);

		player.teleport(destination);

		displayPortalRings(destination, 3, 8, 0.45, 0.85, -0.20, 0.5, 3);
		destination.getWorld().playSound(destination, Sound.ENDERMAN_TELEPORT, 1.0f, 1.3f);

		teleportAbility.use();
	}

	/**
	 * Rings of portal particles going up from the location, each one with its own radius and turned a bit more than
	 * the last one, and a few more particles at the middle.
	 */
	private void displayPortalRings(Location location, int rings, int points, double heightStep, double startRadius,
			double radiusStep, double twistStep, int middleParticles) {
		World world = location.getWorld();

		for (int ring = 0; ring < rings; ring++) {
			double radius = startRadius + ring * radiusStep;
			double twist = ring * twistStep;

			for (int i = 0; i < points; i++) {
				double angle = 2 * Math.PI / points * i + twist;
				world.playEffect(location.clone().add(Math.cos(angle) * radius, ring * heightStep, Math.sin(angle) * radius),
						Effect.PORTAL, 1);
			}
		}

		for (int i = 0; i < middleParticles; i++)
			world.playEffect(location.clone().add(0, 1, 0), Effect.PORTAL, 1);
	}

	// -----------------------------------------------------------------------
	//  Fireballs
	// -----------------------------------------------------------------------

	/** Three fireballs side by side, in the way the wizard looks. */
	private void useFireball() {
		if (!fireballAbility.isReady()) {
			fireballAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}
		fireballAbility.use();

		Location chest = player.getLocation().add(0, 1.2, 0);
		Vector aim = player.getEyeLocation().getDirection();

		Vector right = aim.clone().crossProduct(new Vector(0, 1, 0));
		if (right.lengthSquared() < 0.001) right = new Vector(1, 0, 0); // aiming straight up or down
		right.normalize();

		shootFireball(chest.clone(), aim);
		shootFireball(chest.clone().add(right), aim);
		shootFireball(chest.clone().subtract(right), aim);

		SoundManager.playSoundToAll(player, Sound.GHAST_FIREBALL, 1, 1);
	}

	private void shootFireball(Location from, Vector direction) {
		final Vector velocity = direction.clone().normalize().multiply(FIREBALL_SPEED);

		ItemProjectile projectile = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				if (hit != null && hit.getGameMode() != GameMode.SPECTATOR)
					hit.setFireTicks(FIREBALL_FIRE_TICKS);
			}
		}, new ItemStack(Material.BLAZE_POWDER));

		instance.getGameManager().getProjManager().shootProjectile(projectile, from, velocity.clone());
		keepFlyingStraight(projectile, velocity);
	}

	/** The projectile would fall, so its velocity is set again every tick until it hits something or its time is up. */
	private void keepFlyingStraight(final ItemProjectile projectile, final Vector velocity) {
		instance.trackClassTask(new BukkitRunnable() {
			private int ticks = 0;

			@Override
			public void run() {
				if (projectile.getEntity() == null || !projectile.getEntity().isValid() || projectile.getEntity().isDead()) {
					cancel();
					return;
				}

				if (++ticks > FIREBALL_LIFETIME_TICKS) {
					projectile.getEntity().remove();
					cancel();
					return;
				}

				projectile.getEntity().setVelocity(velocity.clone());
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 1L, 1L));
	}

	@Override
	public ClassType getType() {
		return ClassType.Wizard;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
