package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class BrewingStandClass extends BaseClass {

	private static final int MAX_BLAZE_POWDER = 5;
	private static final int BREWING_SLOT = 1;
	private static final int BLAZE_POWDER_SLOT = 8;
	private static final int SECRET_POTION_DURATION_MULTIPLIER = 5;
	private static final String SECRET_POTION_NAME = "&6&lSecret Potion";

	/** The potions that can be brewed. The amount of blaze powder used is the position in this list plus one. */
	private enum Brew {
		JUMP("&a", "Jump", "Jump", new PotionEffect(PotionEffectType.JUMP, 20 * 20, 7, true)),
		SPEED("&b", "Speed", "Speed", new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1, true)),
		STRENGTH("&4", "Strength", "Strength", new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10 * 20, 0, true)),
		RESISTANCE("&f", "Resistance", "Resistance", new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15 * 20, 0, false, true)),
		REGENERATION("&d", "Regen", "Regeneration", new PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 1, true));

		private final String color;
		private final String potionName;
		private final String effectName;
		private final PotionEffect effect;

		Brew(String color, String potionName, String effectName, PotionEffect effect) {
			this.color = color;
			this.potionName = potionName;
			this.effectName = effectName;
			this.effect = effect;
		}

		/** The colored name the potion is shown with, in the action bar and as its item name. */
		private String getPotionName() {
			return color + "&l" + potionName;
		}

		/** "Jump 8 for 20s", the way the Brewing item lists what each potion gives. */
		private String getEffectInfo() {
			return color + "&o" + effectName + " &e" + (effect.getAmplifier() + 1) + " &7for &e" + effect.getDuration() / 20 + "s";
		}
	}

	private final ItemStack weapon;
	private final ItemStack brewingItem;
	private final ItemStack barrierItem;
	private final Ability brewAbility = new Ability("&e&lBrewing", 2, player);

	private Brew brewing;
	private boolean hasSentBrewedMessage = true;

	public BrewingStandClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU0M2JiZDkwNTcxYjFlMzVhYTAzOWE1ZWJhZDY1ZjQxNDI3YzhiODg3MWRkZjc2NzU4MGYzYTViMTAyMmZiZiJ9fX0=",
				"FFB81A",
				"FFA236",
				"756B6D",
				6,
				"BrewingStand"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.BLAZE_ROD),
				"&e&lBlaze Core",
				"",
				"&7Hit enemies to gain blaze powder"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Brewing Ability
		List<String> brewingLore = new ArrayList<>();
		brewingLore.add("&7Use your blaze powder to brew:");
		brewingLore.add("");
		for (Brew brew : Brew.values())
			brewingLore.add("&7" + (brew.ordinal() + 1) + " Powder - " + brew.getEffectInfo());

		brewingItem = ItemHelper.setDetails(
				new ItemStack(Material.BREWING_STAND_ITEM),
				brewAbility.getAbilityNameRightClickMessage(),
				brewingLore.toArray(new String[0])
		);

		// Barrier
		barrierItem = ItemHelper.setDetails(
				new ItemStack(Material.BARRIER),
				"&cNo blaze powder yet!"
		);
	}

	@Override
	public void setItems(Inventory playerInv) {
		brewAbility.getCooldownInstance().reset();
		hasSentBrewedMessage = true;
		playerInv.setItem(0, weapon);
		playerInv.setItem(BREWING_SLOT, brewingItem);
		playerInv.setItem(BLAZE_POWDER_SLOT, barrierItem);
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		if (!brewAbility.isReady()) {
			int remainingTime = (int) (brewAbility.getCooldownInstance().getRemainingCooldownSeconds() + 1);

			String msg = ChatColorHelper.color(brewAbility.getAbilityName() + " " + brewing.getPotionName() + " &rin &e" + remainingTime + "s");
			getActionBarManager().setActionBar(player, "brewing.cooldown", msg, 2);

			hasSentBrewedMessage = false;
		} else {
			if (hasSentBrewedMessage) return;
			String msg = ChatColorHelper.color("&rBrewed " + brewing.getPotionName());
			getActionBarManager().setActionBar(player, "brewing.cooldown", msg, 2);
			hasSentBrewedMessage = true;
		}
	}

	@Override
	public void doDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (!brewAbility.isReady()) return;
		if (!(event.getEntity() instanceof Player)) return;

		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
						&& heldItem != null
						&& heldItem.equals(weapon);

		if (isWeaponMelee) addBlazePowder();
	}

	private void addBlazePowder() {
		ItemStack powderSlot = player.getInventory().getItem(BLAZE_POWDER_SLOT);

		if (powderSlot == null) return;

		if (powderSlot.getType() == Material.BARRIER) {
			player.getInventory().setItem(BLAZE_POWDER_SLOT, new ItemStack(Material.BLAZE_POWDER));
		} else if (powderSlot.getAmount() < MAX_BLAZE_POWDER) {
			player.getInventory().addItem(new ItemStack(Material.BLAZE_POWDER));
		}
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
		if (!item.equals(brewingItem)) return;
		if (!brewAbility.isReady()) return;

		if (player.getInventory().getItem(BLAZE_POWDER_SLOT).getType() == Material.BARRIER) {
			brewAbility.sendCustomMessage("&c&l(!) &rNo blaze powder yet");
			return;
		}

		brewAbility.use();
		brewPotion();
	}

	/** Uses up the blaze powder, and gives the potion it brews once the cooldown is over. */
	private void brewPotion() {
		int blazePowderAmount = player.getInventory().getItem(BLAZE_POWDER_SLOT).getAmount();
		player.getInventory().setItem(BLAZE_POWDER_SLOT, barrierItem);
		brewing = Brew.values()[blazePowderAmount - 1];

		final Brew brew = brewing;
		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
			if (!isPlayerAlive()) return;

			player.getInventory().addItem(createPotion(brew));
			SoundManager.playSoundToPlayer(player, Sound.SWIM, 1, 0.5f);
			mergeSecretPotion();
		}, (long) (brewAbility.getCooldownDurationSeconds() * 20L));
	}

	private ItemStack createPotion(Brew brew) {
		String displayName = brew.getPotionName() + " " + ItemHelper.getRomanNumeral(brew.effect.getAmplifier() + 1) +
				" &7(" + (brew.effect.getDuration() / 20) + " sec)";

		// The effect is written as lore, the client's own info on the potion is hidden
		return ItemHelper.showPotionEffects(ItemHelper.setDetails(
				ItemHelper.createPotionItem(brew.effect, true, true),
				displayName
		));
	}

	/** Having one of every potion merges one of each into a Secret Potion. The extra ones of a kind stay. */
	private void mergeSecretPotion() {
		PlayerInventory inventory = player.getInventory();

		int[] slots = new int[Brew.values().length];
		for (Brew brew : Brew.values()) {
			slots[brew.ordinal()] = findPotionSlot(brew);
			if (slots[brew.ordinal()] == -1) return;
		}

		for (int slot : slots) {
			ItemStack potion = inventory.getItem(slot);
			if (potion.getAmount() > 1) potion.setAmount(potion.getAmount() - 1);
			else inventory.setItem(slot, null);
		}

		moveItemsLeft();
		inventory.addItem(createSecretPotion());
		player.sendMessage(ChatColorHelper.color("&2&l(!) &rYour potions merged into a " + SECRET_POTION_NAME));
		SoundManager.playSoundToPlayer(player, Sound.LEVEL_UP, 1, 1.5f);
	}

	/** Moves the potions and other items after the brewing item to the left, so merging leaves no blank slots between them. */
	private void moveItemsLeft() {
		PlayerInventory inventory = player.getInventory();

		List<ItemStack> items = new ArrayList<>();
		for (int slot = BREWING_SLOT + 1; slot < 36; slot++) {
			if (slot == BLAZE_POWDER_SLOT) continue;
			ItemStack item = inventory.getItem(slot);
			if (item != null && item.getType() != Material.AIR) items.add(item);
		}

		int next = 0;
		for (int slot = BREWING_SLOT + 1; slot < 36; slot++) {
			if (slot == BLAZE_POWDER_SLOT) continue;
			inventory.setItem(slot, next < items.size() ? items.get(next++) : null);
		}
	}

	/**
	 * The first slot with a potion this brew made. It only has the one effect, and the same level and
	 * duration, so neither the Secret Potion nor a similar potion from a drop is mistaken for it.
	 */
	private int findPotionSlot(Brew brew) {
		ItemStack[] contents = player.getInventory().getContents();
		for (int slot = 0; slot < contents.length; slot++) {
			ItemStack item = contents[slot];
			if (item == null || item.getType() != Material.POTION) continue;

			List<PotionEffect> effects = ((PotionMeta) item.getItemMeta()).getCustomEffects();
			if (effects.size() != 1) continue;

			PotionEffect effect = effects.get(0);
			if (effect.getType().equals(brew.effect.getType())
					&& effect.getAmplifier() == brew.effect.getAmplifier()
					&& effect.getDuration() == brew.effect.getDuration()) return slot;
		}
		return -1;
	}

	/** A splash potion that looks like an Invisibility one, with every effect lasting several times longer. */
	private ItemStack createSecretPotion() {
		ItemStack potion = new ItemStack(Material.POTION);
		Potion appearance = new Potion(PotionType.INVISIBILITY);
		appearance.setSplash(true);
		appearance.apply(potion);

		PotionMeta meta = (PotionMeta) potion.getItemMeta();
		PotionEffect[] effects = new PotionEffect[Brew.values().length];
		for (Brew brew : Brew.values()) {
			PotionEffect effect = brew.effect;
			effects[brew.ordinal()] = new PotionEffect(effect.getType(),
					effect.getDuration() * SECRET_POTION_DURATION_MULTIPLIER, effect.getAmplifier(),
					effect.isAmbient(), effect.hasParticles());
			meta.addCustomEffect(effects[brew.ordinal()], true);
		}
		potion.setItemMeta(meta);

		return ItemHelper.showPotionEffects(ItemHelper.setDetails(potion, SECRET_POTION_NAME), effects);
	}

	@Override
	public ClassType getType() {
		return ClassType.BrewingStand;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
