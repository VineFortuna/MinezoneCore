package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class CloudClass extends BaseClass {

	private static final int STORM_SLOT = 8;
	/** The top left slot of the inventory, where a plain copy of each wool starts, to read what every storm does. */
	private static final int GUIDE_FIRST_SLOT = 9;
	private static final int STORMCALL_COOLDOWN_SECONDS = 15;
	/** Up to this many blocks above the ground the sword casts Blizzard, over it Lightning. */
	private static final double LOW_AIR_HEIGHT = 3;
	private static final PotionEffect JUMP_BOOST = new PotionEffect(PotionEffectType.JUMP, 999999999, 1, false, false);

	private static final double BREEZE_RANGE = 10;
	private static final double BREEZE_PUSH_SPEED = 2.0;
	/** Before the push is scaled to its speed, the way it points: this much up against the way away from the player. */
	private static final double BREEZE_PUSH_UP = 2;
	private static final int BREEZE_RINGS = 5;
	private static final double BREEZE_RING_GROWTH = 2;

	private static final int BLIZZARD_TICKS = 100;
	private static final int BLIZZARD_WAVE_TICKS = 5;
	private static final int SNOWBALLS_PER_WAVE = 8;
	private static final double SNOWBALL_SPEED = 2.0;

	private static final double TNT_RAIN_RANGE = 15;
	private static final int TNT_RAIN_WAVES = 5;
	private static final int TNT_RAIN_WAVE_TICKS = 20;
	private static final int TNT_RAIN_HEIGHT = 20;
	private static final int TNT_FUSE_TICKS = 60;
	private static final PotionEffect TNT_RAIN_SLOWNESS = new PotionEffect(PotionEffectType.SLOW, 140, 2);

	private static final double LIGHTNING_RANGE = 20;
	private static final double LIGHTNING_RADIUS = 3;
	private static final double LIGHTNING_DAMAGE = 5.0;
	private static final int LIGHTNING_FIRE_TICKS = 80;

	/**
	 * What the sword casts, picked by what the player is doing. The wool in the last slot shows which one it is, and
	 * it glows while casting it would hit something. The lore is the description, a blank line, the condition, and the
	 * range and duration where it has them.
	 */
	private enum Storm {
		TNT_RAIN("&4&lTNT Rain", DyeColor.RED,
				"&7Rain TNT on every enemy in range,",
				"&7applying &3&o" + ItemHelper.getPotionEffectName(TNT_RAIN_SLOWNESS.getType()) + " &e"
						+ (TNT_RAIN_SLOWNESS.getAmplifier() + 1) + " &7for &e" + TNT_RAIN_SLOWNESS.getDuration() / 20 + "s",
				"",
				"&7Condition: &eSneaking",
				"&7Range: &a" + ItemHelper.formatDouble(TNT_RAIN_RANGE) + " &7blocks",
				"&7Fuse time: &a" + ItemHelper.formatDouble(TNT_FUSE_TICKS / 20.0) + "&as",
				"&7Duration: &a" + ItemHelper.formatDouble(TNT_RAIN_WAVES * TNT_RAIN_WAVE_TICKS / 20.0) + "&as"),
		BREEZE("&b&lBreeze", DyeColor.CYAN,
				"&7Blow nearby enemies away from",
				"&7you and into the air",
				"",
				"&7Condition: &eOn the ground",
				"&7Range: &a" + ItemHelper.formatDouble(BREEZE_RANGE) + " &7blocks"),
		BLIZZARD("&f&lBlizzard", DyeColor.WHITE,
				"&7Throw snowballs all around you that help you combo and apply",
				"&3&o" + ItemHelper.getPotionEffectName(GameLootDrops.SLOWBALL_EFFECT.getType()) + " &e"
						+ (GameLootDrops.SLOWBALL_EFFECT.getAmplifier() + 1) + " &7for &e"
						+ GameLootDrops.SLOWBALL_EFFECT.getDuration() / 20 + "s &7on hit",
				"",
				"&7Condition: &eUp to " + ItemHelper.formatDouble(LOW_AIR_HEIGHT) + " blocks above ground",
				"&7Duration: &a" + ItemHelper.formatDouble(BLIZZARD_TICKS / 20.0) + "&as"),
		LIGHTNING("&e&lLightning", DyeColor.YELLOW,
				"&7Strike lightning where you aim,",
				"&7damaging and setting enemies",
				"&c&oon fire",
				"",
				"&7Condition: &eOver " + ItemHelper.formatDouble(LOW_AIR_HEIGHT) + " blocks above ground",
				"&7Range: &a" + ItemHelper.formatDouble(LIGHTNING_RANGE) + " &7blocks");

		private final ItemStack item;
		/** The same wool enchanted, which is what the slot shows while casting it would hit something. */
		private final ItemStack glowingItem;

		@SuppressWarnings("deprecation")
		Storm(String name, DyeColor color, String... lore) {
			item = ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, color.getWoolData()), name, lore);
			glowingItem = ItemHelper.setGlowing(item.clone(), true);
		}
	}

	private final ItemStack weapon;
	/** Shown instead of the wool while Stormcall recharges. */
	private final ItemStack barrierItem;
	private final Ability stormcall = new Ability("&3&lStormcall", STORMCALL_COOLDOWN_SECONDS, player);

	/** The wool in the storm slot, null while the barrier is there instead. */
	private Storm shownStorm = Storm.BREEZE;
	/** Whether that wool is the glowing one. */
	private boolean shownGlowing = false;

	public CloudClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDQ4Mjg3NzM3MTU0NDZlNDczZjVhMTY0OTMzYTVkODgyM2RmYjg1OGFmOTA0OTAxMzVjNDE3ZWZlMTY1OTgxZCJ9fX0=",
				"AECBFF",
				6,
				"Cloud"
		);

		weapon = ItemHelper.setUnbreakable(ItemHelper.setDetails(
				new ItemStack(Material.WOOD_SWORD),
				"&3&lStormbringer &7(Right Click)",
				"&7Cast different abilities depending on:",
				"&7▸ &bBreeze: &7While on the ground",
				"&7▸ &fBlizzard: &7While up to &e" + ItemHelper.formatDouble(LOW_AIR_HEIGHT) + " &7blocks above ground",
				"&7▸ &eLightning: &7While over &e" + ItemHelper.formatDouble(LOW_AIR_HEIGHT) + "&7 blocks above ground",
				"&7▸ &4TNT Rain: &7While sneaking"
		));

		barrierItem = ItemHelper.setDetails(
				new ItemStack(Material.BARRIER),
				"&c&lOn Cooldown!",
				"",
				"&7Stormcall recharges every &e" + STORMCALL_COOLDOWN_SECONDS + "s"
		);
	}

	@Override
	public void setItems(Inventory playerInv) {
		// Every life starts ready. The wool is fixed on the next tick if the player isn't on the ground
		stormcall.getCooldownInstance().reset();
		shownStorm = Storm.BREEZE;
		shownGlowing = false;

		playerInv.setItem(0, weapon);
		playerInv.setItem(STORM_SLOT, shownStorm.item);
		player.addPotionEffect(JUMP_BOOST);

		// A plain copy of each wool, in the order the sword lists them
		Storm[] guide = {Storm.BREEZE, Storm.BLIZZARD, Storm.LIGHTNING, Storm.TNT_RAIN};
		for (int i = 0; i < guide.length; i++)
			playerInv.setItem(GUIDE_FIRST_SLOT + i, guide[i].item);
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		stormcall.updateActionBar(player, this);
		if (!player.hasPotionEffect(PotionEffectType.JUMP)) player.addPotionEffect(JUMP_BOOST);

		// The slot shows what the sword would cast right now, or the barrier while Stormcall recharges.
		// The wool glows while casting it would hit something
		Storm storm = stormcall.isReady() ? currentStorm() : null;
		boolean glowing = storm != null && wouldHitSomething(storm);
		if (storm == shownStorm && glowing == shownGlowing) return;

		shownStorm = storm;
		shownGlowing = glowing;
		player.getInventory().setItem(STORM_SLOT, storm == null ? barrierItem : glowing ? storm.glowingItem : storm.item);
	}

	/** Enemies in range for TNT Rain, a block in range for Lightning. The others have nothing to wait for. */
	private boolean wouldHitSomething(Storm storm) {
		switch (storm) {
			case TNT_RAIN:
				return !enemiesWithin(player.getLocation(), TNT_RAIN_RANGE).isEmpty();
			case LIGHTNING:
				return getAimedBlock() != null;
			default:
				return false;
		}
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack item = event.getItem();
		if (item == null || item.getType() != Material.WOOD_SWORD || !isPlayerAlive()) return;

		if (!stormcall.isReady()) {
			stormcall.sendPlayerRemainingCooldownChatMessage();
			return;
		}

		switch (currentStorm()) {
			case TNT_RAIN:
				if (!tntRain()) return;
				break;
			case BREEZE:
				breeze();
				break;
			case BLIZZARD:
				blizzard();
				break;
			case LIGHTNING:
				if (!lightning()) return;
				break;
		}
		stormcall.use();
	}

	/** Sneaking comes first, then standing on the ground, then how high up the player is. */
	private Storm currentStorm() {
		if (player.isSneaking()) return Storm.TNT_RAIN;
		if (player.isOnGround()) return Storm.BREEZE;
		return isHighInAir() ? Storm.LIGHTNING : Storm.BLIZZARD;
	}

	/** Whether the player's feet are more than LOW_AIR_HEIGHT blocks above the closest ground below them. */
	private boolean isHighInAir() {
		Location feet = player.getLocation();
		int bottom = (int) Math.ceil(feet.getY() - LOW_AIR_HEIGHT - 1);

		for (int y = feet.getBlockY(); y >= bottom; y--)
			if (feet.getWorld().getBlockAt(feet.getBlockX(), y, feet.getBlockZ()).getType().isSolid())
				return false;
		return true;
	}

	private void breeze() {
		SoundManager.playSoundToAll(player, Sound.GHAST_FIREBALL, 0.8f, 1);
		displayBreezeRings();

		Location center = player.getLocation();
		for (Player enemy : enemiesWithin(center, BREEZE_RANGE)) {
			Vector away = enemy.getLocation().subtract(center).toVector();
			enemy.setVelocity(away.setY(BREEZE_PUSH_UP).normalize().multiply(BREEZE_PUSH_SPEED));
		}
	}

	/** Rings of clouds spread out from the player. */
	private void displayBreezeRings() {
		instance.trackClassTask(new BukkitRunnable() {
			int rings = 0;
			double radius = 1;

			@Override
			public void run() {
				if (rings++ >= BREEZE_RINGS || !isPlayerAlive()) {
					cancel();
					return;
				}

				Location center = player.getLocation().add(0, 0.2, 0);
				int points = Math.max((int) (Math.PI * radius * 2), 16);
				for (int i = 0; i < points; i++) {
					double angle = 2 * Math.PI * i / points;
					ParticleEffect.CLOUD.display(center.clone().add(radius * Math.cos(angle), 0, radius * Math.sin(angle)));
				}
				radius += BREEZE_RING_GROWTH;
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 0, 1));
	}

	private void blizzard() {
		instance.trackClassTask(new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= BLIZZARD_TICKS || !isPlayerAlive()) {
					cancel();
					return;
				}

				throwSnowballRing();
				ticks += BLIZZARD_WAVE_TICKS;
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 0, BLIZZARD_WAVE_TICKS));
	}

	/**
	 * Snowballs fly out in a ring: the one in front goes wherever the player aims, and the others are turned
	 * around it from there, so the whole ring follows the cursor from one wave to the next.
	 */
	private void throwSnowballRing() {
		Vector aim = player.getEyeLocation().getDirection();

		for (int i = 0; i < SNOWBALLS_PER_WAVE; i++) {
			double angle = 2 * Math.PI * i / SNOWBALLS_PER_WAVE;
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			Vector direction = new Vector(aim.getX() * cos + aim.getZ() * sin, aim.getY(), aim.getZ() * cos - aim.getX() * sin);
			player.launchProjectile(Snowball.class, direction.multiply(SNOWBALL_SPEED));
		}
	}

	/**
	 * The enemies in range when it is cast get TNT dropped on them, wherever they run to. Returns false, and uses
	 * nothing, if there is nobody in range.
	 */
	private boolean tntRain() {
		final List<UUID> targets = new ArrayList<>();
		for (Player enemy : enemiesWithin(player.getLocation(), TNT_RAIN_RANGE))
			targets.add(enemy.getUniqueId());

		if (targets.isEmpty()) {
			player.sendMessage(instance.color("&c&l(!) &rNo nearby players have been found!"));
			return false;
		}

		instance.trackClassTask(new BukkitRunnable() {
			int waves = 0;

			@Override
			public void run() {
				if (waves++ >= TNT_RAIN_WAVES) {
					cancel();
					return;
				}

				for (UUID id : targets) {
					Player target = Bukkit.getPlayer(id);
					if (target == null || !isEnemy(target)) continue;

					TNTPrimed tnt = target.getWorld().spawn(target.getLocation().add(0, TNT_RAIN_HEIGHT, 0), TNTPrimed.class);
					tnt.setFuseTicks(TNT_FUSE_TICKS);
					target.addPotionEffect(TNT_RAIN_SLOWNESS);
				}
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 0, TNT_RAIN_WAVE_TICKS));
		return true;
	}

	/** The block the player aims at, or null if there is none in the range of Lightning. */
	private Block getAimedBlock() {
		Block aimed = player.getTargetBlock((HashSet<Byte>) null, (int) LIGHTNING_RANGE);
		return aimed.getType() == Material.AIR ? null : aimed;
	}

	/** Strikes the block the player aims at. Returns false, and uses nothing, if there is no block in range. */
	private boolean lightning() {
		Block aimed = getAimedBlock();
		if (aimed == null) {
			player.sendMessage(instance.color("&c&l(!) &rNo block in range to strike"));
			return false;
		}

		Location strike = aimed.getLocation().add(0.5, 1, 0.5);
		strike.getWorld().strikeLightningEffect(strike);

		for (Player enemy : enemiesWithin(strike, LIGHTNING_RADIUS)) {
			enemy.damage(LIGHTNING_DAMAGE, player);
			enemy.setFireTicks(LIGHTNING_FIRE_TICKS);
		}
		return true;
	}

	private List<Player> enemiesWithin(Location center, double range) {
		List<Player> enemies = new ArrayList<>();

		for (Player other : instance.players)
			if (isEnemy(other) && other.getWorld() == center.getWorld()
					&& other.getLocation().distanceSquared(center) <= range * range)
				enemies.add(other);
		return enemies;
	}

	/** Someone still playing in this game, not on the caster's duos team, and not protected from the spawn. */
	private boolean isEnemy(Player other) {
		return other != player
				&& instance.players.contains(other)
				&& !checkIfDead(other, instance)
				&& !instance.getGameManager().spawnProt.containsKey(other)
				&& (instance.duosMap == null || !instance.team.get(other).equals(instance.team.get(player)));
	}

	@Override
	public ClassType getType() {
		return ClassType.Cloud;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
