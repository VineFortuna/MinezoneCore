package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * The Grinch's fake Lightning Drops (see GrinchClass and its Counterfeit Loot).
 * <p>
 * A fake drop is the real drop's item, identical in every way a player can see, with a hidden tag on it that says it
 * is fake and which Grinch threw it. The tag lives in the item's NBT, so it follows the item into an inventory and
 * through every use. Each real drop's code checks {@link #isFake} and does the sneaky thing instead, see the
 * handlers in GameManager, plus FakeLootListener for the drops that have no handler of their own (potions, apples,
 * the Hammer).
 * <p>
 * Nothing in here keeps a player, game or world alive: the entity sets are weak.
 */
public final class FakeLoot {

	/** The drops with no fake version: the Witch Pokeball (none was asked for). */
	private static final Set<GameLootDrops> NOT_FAKEABLE = EnumSet.of(GameLootDrops.WITCH_EGG);

	/** Whether this drop has a fake version. */
	public static boolean isFakeable(GameLootDrops drop) {
		return drop != null && !NOT_FAKEABLE.contains(drop);
	}

	private static final Random RANDOM = new Random();

	private static final String TAG_FAKE = "scbFakeLoot";
	private static final String TAG_OWNER = "scbFakeOwner";

	// Fake projectiles and TNT, and fake drops on the ground whose pick up the Grinch was already told about.
	// Weak so an entity that is gone (and its world) isn't kept alive by this.
	private static final Set<Entity> MARKED = Collections.newSetFromMap(new WeakHashMap<Entity, Boolean>());
	private static final Set<Entity> NOTIFIED = Collections.newSetFromMap(new WeakHashMap<Entity, Boolean>());
	// When each fake pearl was thrown, and how many times its thrower had died by then. The values are numbers, so
	// they can't keep their key alive.
	private static final Map<Entity, Long> THROWN_AT = new WeakHashMap<>();
	private static final Map<Entity, Integer> DEATHS_AT_THROW = new WeakHashMap<>();

	/** Effects that count as good for a fake Milk to take away. */
	private static final Set<PotionEffectType> GOOD_EFFECTS = new HashSet<>(Arrays.asList(
			PotionEffectType.SPEED, PotionEffectType.FAST_DIGGING, PotionEffectType.INCREASE_DAMAGE,
			PotionEffectType.JUMP, PotionEffectType.REGENERATION, PotionEffectType.DAMAGE_RESISTANCE,
			PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY,
			PotionEffectType.NIGHT_VISION, PotionEffectType.HEALTH_BOOST, PotionEffectType.ABSORPTION,
			PotionEffectType.SATURATION));

	/** An effect with more time left than this is a class's permanent one (from its kit), not one from an item. */
	private static final int PERMANENT_EFFECT_TICKS = 20 * 60 * 20;

	private FakeLoot() {
	}

	// ---------------------------------------------------------------- the tag

	/** A copy of this item marked as fake, owned by the Grinch who threw it. The original is left alone. */
	public static ItemStack mark(ItemStack item, UUID owner) {
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
		tag.setBoolean(TAG_FAKE, true);
		tag.setString(TAG_OWNER, owner.toString());
		nms.setTag(tag);
		return CraftItemStack.asBukkitCopy(nms);
	}

	public static boolean isFake(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return false;
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
		return nms != null && nms.hasTag() && nms.getTag().getBoolean(TAG_FAKE);
	}

	/** The Grinch who threw this fake, or null if it isn't one. */
	public static UUID ownerOf(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return null;
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
		if (nms == null || !nms.hasTag() || !nms.getTag().hasKey(TAG_OWNER))
			return null;
		try {
			return UUID.fromString(nms.getTag().getString(TAG_OWNER));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	// ---------------------------------------------------------------- which drop to fake

	/**
	 * A random drop to fake, rolled like a real lightning drop (rarer ones less often), out of the drops that have a
	 * fake version and are enabled in the game's settings. If the settings leave none, any fakeable drop.
	 */
	public static GameLootDrops pickDrop(Set<GameLootDrops> enabled) {
		Set<GameLootDrops> pool = EnumSet.complementOf(EnumSet.copyOf(NOT_FAKEABLE));
		if (enabled != null && !enabled.isEmpty()) {
			Set<GameLootDrops> allowed = EnumSet.copyOf(pool);
			allowed.retainAll(enabled);
			if (!allowed.isEmpty())
				pool = allowed;
		}
		return GameLootDrops.getDrop(pool);
	}

	/**
	 * What a fake Present opens into: 2 fake drops (one Common/Uncommon, one Rare/Legendary like the real one), thrown
	 * by the same Grinch as the Present, instead of the real drops.
	 */
	public static ItemStack[] openPresent(Set<GameLootDrops> enabled, UUID owner) {
		ItemStack[] drops = GameLootDrops.openPresent(enabled, NOT_FAKEABLE);
		for (int i = 0; i < drops.length; i++)
			drops[i] = mark(drops[i], owner != null ? owner : new UUID(0L, 0L));
		return drops;
	}

	/** How a fake Instagib is fired. */
	public enum InstagibAim {
		/** Where the player looks, like the real one. */
		NORMAL,
		/** The opposite way: the beam, the damage and the launch all go behind them. */
		BACKWARDS,
		/** Straight up. */
		UPWARDS
	}

	/** Three times out of four a fake Instagib goes backwards or up (those two equally often), otherwise it works. */
	public static InstagibAim instagibAim() {
		int roll = RANDOM.nextInt(8);
		if (roll < 2) return InstagibAim.NORMAL; // 2 in 8
		return roll < 5 ? InstagibAim.BACKWARDS : InstagibAim.UPWARDS; // 3 in 8 each
	}

	/** Two times out of three: how often the fakes that only sometimes do their sneaky thing do it. */
	public static boolean sneaky() {
		return RANDOM.nextInt(3) < 2;
	}

	/** Whether this player is in Spectator mode. */
	public static boolean isSpectating(Player player) {
		return player.getGameMode() == GameMode.SPECTATOR;
	}

	/**
	 * Whether this player is out of the game, so no fake ever pushes them around or does anything to them: in
	 * Spectator mode, waiting to respawn, or eliminated. An eliminated player (no lives left) is NOT in Spectator
	 * mode: they stay in Adventure mode, flying and hidden, so the game mode alone doesn't tell.
	 */
	public static boolean isOut(Player player, BaseClass baseClass) {
		return isSpectating(player) || (baseClass != null && (baseClass.isDead || baseClass.getLives() <= 0));
	}

	// ---------------------------------------------------------------- marking entities (snowballs, pearls, TNT)

	public static void markEntity(Entity entity) {
		MARKED.add(entity);
	}

	public static boolean isMarked(Entity entity) {
		return entity != null && MARKED.contains(entity);
	}

	// ---------------------------------------------------------------- picking one up

	/**
	 * A player is about to pick up a drop. Returns true if that must be cancelled: the Grinch who threw a fake can't
	 * take it back. When anyone else picks a fake up, the Grinch is told (once per drop).
	 */
	public static boolean handlePickup(Core main, Player picker, Item drop) {
		ItemStack stack = drop.getItemStack();
		if (!isFake(stack))
			return false;

		UUID owner = ownerOf(stack);
		if (picker.getUniqueId().equals(owner))
			return true;

		if (NOTIFIED.add(drop)) {
			Player grinch = owner == null ? null : Bukkit.getPlayer(owner);
			if (grinch != null && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
				grinch.sendMessage(main.color("&2&l(!) &e" + picker.getName() + " &rpicked up your fake "
						+ stack.getItemMeta().getDisplayName() + "&r!"));
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- effects the fakes share

	/**
	 * The fake Milk: takes away ONE of the good effects that came from items (Speed, Regeneration, Absorption...),
	 * picked at random, but never a class's permanent ones. Does nothing if they have none.
	 */
	public static void removeOneGoodEffect(Player player) {
		List<PotionEffect> removable = new ArrayList<>();
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (GOOD_EFFECTS.contains(effect.getType()) && effect.getDuration() < PERMANENT_EFFECT_TICKS)
				removable.add(effect);
		}
		if (!removable.isEmpty())
			player.removePotionEffect(removable.get(new Random().nextInt(removable.size())).getType());
	}

	/** The fake Fire Flower: plants a poppy on the ground where it landed. */
	@SuppressWarnings("deprecation")
	public static void plantPoppy(Location where) {
		Block block = where.getBlock();
		if (block.getType().isSolid())
			block = block.getRelative(BlockFace.UP);

		// Fall to the ground if it landed in the air (a block or two)
		for (int i = 0; i < 4 && block.getType() == Material.AIR; i++) {
			Block below = block.getRelative(BlockFace.DOWN);
			if (below.getType().isSolid()) {
				// No physics update, or a flower on stone would pop straight off as an item
				block.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 0, false);
				return;
			}
			block = below;
		}
	}

	/**
	 * The fake ender pearl that is teleporting this player, or null if it is a normal one (see FakeLootListener,
	 * which marks the pearls a Grinch's fake drop threw). The pearl is still in the world while its teleport event runs.
	 */
	public static EnderPearl findFakePearl(Player player, Location landing) {
		for (EnderPearl pearl : landing.getWorld().getEntitiesByClass(EnderPearl.class)) {
			if (player.equals(pearl.getShooter()) && isMarked(pearl)
					&& pearl.getLocation().distanceSquared(landing) <= 4.0)
				return pearl;
		}
		return null;
	}

	/**
	 * Remembers when a fake pearl was thrown, since its delay counts from the throw and not from the landing, and how
	 * many times its thrower had died, so a thrower who dies before the teleport is never teleported.
	 */
	public static void markThrown(Entity entity, int throwerDeaths) {
		THROWN_AT.put(entity, System.currentTimeMillis());
		DEATHS_AT_THROW.put(entity, throwerDeaths);
	}

	/** How many times the pearl's thrower had died when they threw it. */
	public static int deathsAtThrow(Entity entity) {
		Integer deaths = DEATHS_AT_THROW.get(entity);
		return deaths == null ? 0 : deaths;
	}

	/** When the pearl was thrown (milliseconds), or 0 if it isn't known. */
	public static long thrownAt(Entity entity) {
		Long time = THROWN_AT.get(entity);
		return time == null ? 0L : time;
	}
}
