package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

public class WitchClass extends BaseClass {

	private static final int MAX_BROOMS = 4;
	private static final int MIN_BROOMS_TO_USE = 2;
	private static final int BROOMS_REGENERATED = 1;
	private static final double BROOM_UP_VELOCITY = 1.2;
	private static final int POTION_COOLDOWN_SECONDS = 3;
	private static final double POTION_THROW_MULTIPLIER = 1.3;

	/** The potions in the order they come in, over and over. */
	private enum WitchPotion {
		POISON("&2&lPoison", "&2Poison", PotionType.POISON, 0, 15),
		DAMAGE("&4&lDamage", "&cInstant Damage", PotionType.INSTANT_DAMAGE, 0, 0),
		SLOWNESS("&3&lSlowness", "&3Slowness", PotionType.SLOWNESS, 1, 8),
		WEAKNESS("&8&lWeakness", "&fWeakness", PotionType.WEAKNESS, 3, 8);

		private final String name;
		/** How the potion is named in the lore that lists the order. */
		private final String cycleName;
		private final PotionType type;
		private final int amplifier;
		private final int durationSeconds;

		WitchPotion(String name, String cycleName, PotionType type, int amplifier, int durationSeconds) {
			this.name = name;
			this.cycleName = cycleName;
			this.type = type;
			this.amplifier = amplifier;
			this.durationSeconds = durationSeconds;
		}
	}

	private final ItemStack weapon;
	private final ItemStack[] potionItems = new ItemStack[WitchPotion.values().length];
	/** Shown while the next potion is on its way, one for each potion since it says which one comes. */
	private final ItemStack[] barrierItems = new ItemStack[WitchPotion.values().length];
	private final Ability broomAbility = new Ability("&5&lMagic Broom", player);

	private int regenBrooms = 0;
	/** Seconds left until the next potion comes. */
	private int potionCooldown = 0;
	/** The potion that comes next. */
	private WitchPotion nextPotion = WitchPotion.POISON;

	public WitchClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlMTNkMTg0NzRmYzk0ZWQ1NWFlYjcwNjk1NjZlNDY4N2Q3NzNkYWMxNmY0YzNmODcyMmZjOTViZjlmMmRmYSJ9fX0=",
				"47236B",
				6,
				"Witch"
		);

		weapon = createBroom(MAX_BROOMS);

		// The potions all say in which order they come
		StringBuilder cycle = new StringBuilder();
		for (WitchPotion potion : WitchPotion.values()) {
			if (cycle.length() > 0) cycle.append("&7 » ");
			cycle.append(potion.cycleName);
		}

		for (WitchPotion potion : WitchPotion.values()) {
			// The effect info of each potion is written as lore (showPotionEffects), the client's own is hidden
			potionItems[potion.ordinal()] = ItemHelper.showPotionEffects(ItemHelper.setDetails(
					ItemHelper.createPotionItem(potion.type, potion.amplifier, potion.durationSeconds, true, true, false),
					potion.name + " &7(Right Click)",
					"",
					"&7Cycle orderly through potions:",
					cycle.toString()
			));

			barrierItems[potion.ordinal()] = ItemHelper.setDetails(
					new ItemStack(Material.BARRIER),
					"&cNext: " + potion.name,
					"",
					"&7A new potion every &e" + POTION_COOLDOWN_SECONDS + "s"
			);
		}
	}

	/** The Magic Broom as a stack of the given size. Each stack of it is one flight, and it is also the weapon. */
	private ItemStack createBroom(int amount) {
		ItemStack broom = ItemHelper.setDetails(new ItemStack(Material.WHEAT, amount),
				broomAbility.getAbilityNameRightClickMessage(),
				"",
				"&7Fly upwards into the air",
				"",
				"&7You need at least &e" + MIN_BROOMS_TO_USE + "&7 stacks to use",
				"",
				"&7Regenerates &e" + BROOMS_REGENERATED + "&7 every &osecond&r&7 while on ground");
		broom.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		broom.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		return broom;
	}

	/** The next potion of the cycle, and the one after it becomes the next. */
	private ItemStack takeNextPotion() {
		ItemStack potion = potionItems[nextPotion.ordinal()].clone();
		nextPotion = WitchPotion.values()[(nextPotion.ordinal() + 1) % WitchPotion.values().length];
		return potion;
	}

	@Override
	public void projectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof ThrownPotion)) return;

		ThrownPotion potion = (ThrownPotion) event.getEntity();
		if (!(potion.getShooter() instanceof Player)) return;

		// Throws the potion further
		if (((Player) potion.getShooter()).getInventory().getHeldItemSlot() == 1)
			potion.setVelocity(potion.getVelocity().multiply(POTION_THROW_MULTIPLIER));
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		// The potion was thrown: the barrier takes its place until the next one comes
		ItemStack potionSlot = player.getInventory().getItem(1);
		if (potionSlot == null || potionSlot.getType() == Material.AIR) {
			player.getInventory().setItem(1, barrierItems[nextPotion.ordinal()]);
			potionCooldown = POTION_COOLDOWN_SECONDS;
		}

		if (gameTicks % 20 != 0) return;

		if (potionCooldown != 0 && --potionCooldown == 0)
			player.getInventory().setItem(1, takeNextPotion());

		if (regenBrooms < MAX_BROOMS && player.isOnGround()) {
			regenBrooms = Math.min(MAX_BROOMS, regenBrooms + BROOMS_REGENERATED);
			player.getInventory().setItem(0, createBroom(regenBrooms));
		}
	}

	@Override
	public void setItems(Inventory playerInv) {
		// Every life starts over
		potionCooldown = 0;
		regenBrooms = MAX_BROOMS;
		nextPotion = WitchPotion.POISON;

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, takeNextPotion());
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null || item.getType() != Material.WHEAT) return;
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null || !meta.getDisplayName().contains("Magic")) return;
		if (item.getAmount() < MIN_BROOMS_TO_USE) return;

		event.setCancelled(true);
		player.setVelocity(new Vector(0, 1, 0).multiply(BROOM_UP_VELOCITY));
		item.setAmount(item.getAmount() - 1);
		regenBrooms = item.getAmount();

		for (Player gamePlayer : instance.players)
			gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
	}

	@Override
	public ClassType getType() {
		return ClassType.Witch;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

}
