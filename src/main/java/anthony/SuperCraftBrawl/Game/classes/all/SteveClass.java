package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SteveClass extends BaseClass {

	private static final int ORES_PER_UPGRADE = 5;
	private static final int MAX_UPGRADE_TIER = 3;

	/** The coal upgrade puts this effect on enemies with every pickaxe hit. Tier 1 is amplifier 0, tier 2 is amplifier 1, and so on. */
	private static final PotionEffectType COAL_EFFECT = PotionEffectType.SLOW;
	private static final int COAL_EFFECT_SECONDS = 3;

	/** The pickaxe starts as the first one. The diamond upgrade at tier N turns it into entry N. */
	private static final Material[] PICKAXE_MATERIALS = {
			Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE
	};
	private static final String[] PICKAXE_MATERIAL_NAMES = {"&6&oWooden", "&8&oStone", "&f&oIron", "&b&oDiamond"};

	/** How long a mined ore stays on the ground before it disappears. */
	private static final long ORE_DROP_TICKS = 20L;

	private enum Ore {
		COAL("Coal", "&8", Material.COAL, null),
		GOLD("Gold", "&6", Material.GOLD_INGOT, Enchantment.KNOCKBACK),
		DIAMOND("Diamond", "&b", Material.DIAMOND, null);

		private final String label;
		private final String color;
		private final Material item;
		/** What this ore's upgrade puts on the pickaxe, or null when it does something else (coal slows enemies, diamond changes the material). */
		private final Enchantment enchantment;

		Ore(String label, String color, Material item, Enchantment enchantment) {
			this.label = label;
			this.color = color;
			this.item = item;
			this.enchantment = enchantment;
		}
	}

	private final Random random = new Random();
	private final ItemStack pickaxe;
	/** The description of the pickaxe. The upgrades are listed above it, like enchantments, and a blank line separates them. */
	private final List<String> pickaxeLore;
	private final ItemStack craftingTable;

	/** Both are indexed by Ore#ordinal(). Ores are kept through deaths, like the upgrades. */
	private final int[] ores = new int[Ore.values().length];
	private final int[] tiers = new int[Ore.values().length];

	public SteveClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzVhZDYyNjE4MTExMWU1NWViZmM5MDZhOGU3MDQwYzY2YjhlZmU5NGY3YzA3NDQ4ZDU3MTAwMTJkNjg0MzZjIn19fQ==",
				"00ADAD",
				"4A4191",
				"686868",
				6,
				"Steve"
		);

		// Weapon
		pickaxe = ItemHelper.setDetails(
				new ItemStack(PICKAXE_MATERIALS[0]),
				"&3&lI am Steve!",
				"&7Hit enemies to mine &8&oCoal&7, &6&oGold&r &7and &b&oDiamonds&r&7",
				"&7and use them to upgrade your pickaxe"
		);
		ItemHelper.setUnbreakable(pickaxe);
		pickaxeLore = pickaxe.getItemMeta().getLore();

		// Crafting
		craftingTable = ItemHelper.setDetails(
				new ItemStack(Material.WORKBENCH),
				"&f&lCrafting &7(Right Click)",
				"&7Craft your ores into permanent upgrades,",
				"&7with each type having " + MAX_UPGRADE_TIER + " different tiers:",
				oreLine(Ore.COAL, "&3&o" + ItemHelper.getPotionEffectName(COAL_EFFECT) + " &r" + tierList()
						+ " &7on hit for &e" + COAL_EFFECT_SECONDS + "s"),
				oreLine(Ore.GOLD, "&oKnockback&r " + tierList()),
				oreLine(Ore.DIAMOND, materialList() + " &7Pickaxes")
		);
	}

	private static String oreLine(Ore ore, String upgrade) {
		return "&7▸ " + ore.color + ORES_PER_UPGRADE + " " + ore.label + " &7- " + upgrade;
	}

	/** "&e1&7, &e2 &7and &e3", one entry per upgrade tier. */
	private static String tierList() {
		StringBuilder list = new StringBuilder();
		for (int tier = 1; tier <= MAX_UPGRADE_TIER; tier++) {
			if (tier > 1) list.append(tier == MAX_UPGRADE_TIER ? " &7and " : "&7, ");
			list.append("&e").append(tier);
		}
		return list.toString();
	}

	/** The materials the diamond upgrade goes through, past the one the pickaxe starts as. */
	private static String materialList() {
		StringBuilder list = new StringBuilder();
		for (int tier = 1; tier <= MAX_UPGRADE_TIER; tier++) {
			if (tier > 1) list.append(tier == MAX_UPGRADE_TIER ? " &7and " : "&7, ");
			list.append(PICKAXE_MATERIAL_NAMES[tier]);
		}
		return list.toString();
	}

	@Override
	public void setItems(Inventory playerInv) {
		playerInv.setItem(0, pickaxe);
		playerInv.setItem(1, craftingTable);
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		// Only ores whose upgrade isn't maxed yet. Once all are maxed there is nothing to show, and
		// the bar (which only lasts 2 ticks per set) simply stops being refreshed.
		StringBuilder oreAmounts = new StringBuilder();
		for (Ore ore : Ore.values()) {
			if (tier(ore) >= MAX_UPGRADE_TIER) continue;
			if (oreAmounts.length() > 0) oreAmounts.append(" &7┃ ");
			oreAmounts.append(ore.color).append(amount(ore));
		}
		if (oreAmounts.length() == 0) return;

		getActionBarManager().setActionBar(player, "steve.ores", instance.color(oreAmounts.toString()), 2);
	}

	@Override
	public void doDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
		if (!(event.getEntity() instanceof Player)) return;

		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
		if (heldItem == null || heldItem.getType() != pickaxe.getType()) return;

		Player victim = (Player) event.getEntity();

		if (instance.duosMap != null)
			if (instance.team.get(victim).equals(instance.team.get(player)))
				return;

		if (tier(Ore.COAL) > 0) victim.addPotionEffect(coalEffect(tier(Ore.COAL)));
		mineOre(victim);
	}

	private PotionEffect coalEffect(int tier) {
		return new PotionEffect(COAL_EFFECT, COAL_EFFECT_SECONDS * 20, tier - 1, false, true);
	}

	private int tier(Ore ore) {
		return tiers[ore.ordinal()];
	}

	private int amount(Ore ore) {
		return ores[ore.ordinal()];
	}

	/**
	 * Gives one random ore. An ore whose upgrade is maxed is never mined, and neither is one you
	 * already hold enough of for the next tier (craft it first).
	 */
	private void mineOre(Player victim) {
		List<Ore> minable = new ArrayList<>();
		for (Ore ore : Ore.values())
			if (tier(ore) < MAX_UPGRADE_TIER && amount(ore) < ORES_PER_UPGRADE)
				minable.add(ore);
		if (minable.isEmpty()) return;

		Ore ore = minable.get(random.nextInt(minable.size()));
		ores[ore.ordinal()]++;
		playMineAnimation(victim.getLocation().add(0, 1, 0), ore);
	}

	/** Stone breaking particles, and the ore pops out of the enemy for a moment, but can't be picked up. */
	private void playMineAnimation(Location at, Ore ore) {
		World world = at.getWorld();
		world.playEffect(at, Effect.STEP_SOUND, Material.STONE);

		final Item drop = world.dropItem(at, new ItemStack(ore.item));
		drop.setPickupDelay(Integer.MAX_VALUE);
		drop.setVelocity(new Vector(
				(random.nextDouble() - 0.5) * 0.4,
				0.25 + random.nextDouble() * 0.2,
				(random.nextDouble() - 0.5) * 0.4));

		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), new Runnable() {
			@Override
			public void run() {
				drop.remove();
			}
		}, ORE_DROP_TICKS);
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null || item.getType() != Material.WORKBENCH) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;
		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

		event.setCancelled(true);
		craftUpgrades();
	}

	/** Turns every ore you have enough of into its next upgrade. */
	private void craftUpgrades() {
		boolean crafted = false;
		for (Ore ore : Ore.values()) {
			if (tier(ore) >= MAX_UPGRADE_TIER || amount(ore) < ORES_PER_UPGRADE) continue;

			ores[ore.ordinal()] -= ORES_PER_UPGRADE;
			tiers[ore.ordinal()]++;
			applyUpgrade(ore);
			crafted = true;
		}

		if (crafted) {
			player.playSound(player.getLocation(), Sound.ANVIL_USE, 1, 1);
			return;
		}

		boolean maxed = true;
		for (Ore ore : Ore.values())
			if (tier(ore) < MAX_UPGRADE_TIER) maxed = false;

		player.sendMessage(instance.color(maxed
				? "&c&l(!) &rYour pickaxe is fully upgraded!"
				: "&c&l(!) &rYou need &e" + ORES_PER_UPGRADE + " &rof an ore to craft an upgrade!"));
		SoundManager.playErrorSound(player);
	}

	private void applyUpgrade(Ore ore) {
		int tier = tier(ore);

		// The item in the inventory is a copy, so find it by its type before that changes
		int slot = player.getInventory().first(pickaxe.getType());

		switch (ore) {
			case COAL:
				player.sendMessage(instance.color("&2&l(!) &rYour pickaxe now applies &3&o"
						+ ItemHelper.getPotionEffectName(COAL_EFFECT) + " &e" + tier));
				break;
			case GOLD:
				pickaxe.addUnsafeEnchantment(ore.enchantment, tier);
				player.sendMessage(instance.color("&2&l(!) &rYour pickaxe now has &6&oKnockback &e" + tier));
				break;
			default:
				pickaxe.setType(PICKAXE_MATERIALS[tier]);
				player.sendMessage(instance.color("&2&l(!) &rYour pickaxe is now " + PICKAXE_MATERIAL_NAMES[tier]));
		}

		updatePickaxeLore();
		player.getInventory().setItem(slot == -1 ? 0 : slot, pickaxe);
	}

	/**
	 * Lists the coal upgrade on the pickaxe like an enchantment would be: gray, above the description. Once
	 * the pickaxe has an enchantment or the coal upgrade, a blank line separates them from the description.
	 */
	private void updatePickaxeLore() {
		List<String> lore = new ArrayList<>();
		if (tier(Ore.COAL) > 0)
			lore.add(ChatColorHelper.color("&7" + ItemHelper.getPotionEffectName(COAL_EFFECT)
					+ " " + ItemHelper.getRomanNumeral(tier(Ore.COAL))));
		if (tier(Ore.COAL) > 0 || tier(Ore.GOLD) > 0) lore.add("");
		lore.addAll(pickaxeLore);

		ItemMeta meta = pickaxe.getItemMeta();
		meta.setLore(lore);
		pickaxe.setItemMeta(meta);
	}

	@Override
	public ClassType getType() {
		return ClassType.Steve;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return pickaxe;
	}
}
