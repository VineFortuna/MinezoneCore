package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class SummonerClass extends BaseClass {

	private static final int STARTING_SUMMONS = 7;
	private static final int SUMMONS_PER_KILL = 1;
	private static final double SUMMON_SPEED = 2.0;

	/** The mobs a Summons Call can spawn. The color is the one the Summons Call has while it is the next one. */
	private enum SummonMob {
		ZOMBIE(EntityType.ZOMBIE, "&2", "&7Spawns an armored Zombie"),
		CREEPER(EntityType.CREEPER, "&a", "&7Spawns a charged Creeper", "&7Be careful!"),
		SKELETON(EntityType.SKELETON, "&7", "&7Spawns a Skeleton with Flame");

		private final EntityType type;
		private final String color;
		private final String[] description;

		SummonMob(EntityType type, String color, String... description) {
			this.type = type;
			this.color = color;
			this.description = description;
		}
	}

	private final ItemStack weapon;
	private final ItemStack barrierItem;
	private final Ability summonAbility = new Ability("&2&lSummons Call", 6, player);
	private final Random random = new Random();
	/** The mob the next summon spawns. It is shown on the egg. */
	private SummonMob nextMob = SummonMob.ZOMBIE;

	public SummonerClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTA2ODZhMGQ5MzliYzlhY2YxY2UxZjY2OGQ0MDg0ZTdlNjRkNjE1NTUzZmIxYzM2ZjUxMjdiNDFjOGNiNzkxMSJ9fX0=",
				"482B56",
				6,
				"Summoner"
		);

		// Weapon
		weapon = ItemHelper.create(Material.ENCHANTED_BOOK, "&d&lBook of Wisdom");
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Barrier, for when there are no summons left
		barrierItem = ItemHelper.setDetails(
				new ItemStack(Material.BARRIER),
				"&c&lOut of Summons!",
				"",
				"&7Each kill regenerates " + SUMMONS_PER_KILL + " summon"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Summoner;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		String msg = new ActionBarManager.AbilityActionBar(this, getActionBarManager())
				.createAbilityMessage(summonAbility, null, false);
		getActionBarManager().setActionBar(player, "summoner.cooldown", msg, 2);
	}

	@Override
	public void setItems(Inventory playerInv) {
		summonAbility.getCooldownInstance().reset();

		nextMob = SummonMob.values()[random.nextInt(SummonMob.values().length)];

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, createSummonItem(STARTING_SUMMONS));
	}

	/** The egg of the next mob. The amount is the summons left, and the name is colored after the mob. */
	private ItemStack createSummonItem(int amount) {
		summonAbility.setAbilityName(nextMob.color + "&lSummons Call");

		return ItemHelper.setDetails(
				ItemHelper.createMonsterEgg(nextMob.type, amount),
				summonAbility.getAbilityNameRightClickMessage(),
				nextMob.description
		);
	}

	private boolean isSummonItem(ItemStack item) {
		return item != null && item.getType() == Material.MONSTER_EGG
				&& item.hasItemMeta() && item.getItemMeta().hasDisplayName()
				&& item.getItemMeta().getDisplayName().contains("Summons Call");
	}

	/** The slot of the Summons Call, or of the barrier that replaces it. */
	private int findSummonSlot() {
		PlayerInventory inventory = player.getInventory();
		for (int slot = 0; slot < 36; slot++) {
			ItemStack item = inventory.getItem(slot);
			if (isSummonItem(item) || (item != null && item.getType() == Material.BARRIER && item.isSimilar(barrierItem)))
				return slot;
		}
		return 1;
	}

	@Override
	public void killEvent(Player damagerPlayer) {
		if (!instance.classes.containsKey(damagerPlayer) || checkIfDead(player, instance)) return;

		int slot = findSummonSlot();
		ItemStack current = player.getInventory().getItem(slot);

		// Adds to the summons left, or replaces the barrier
		if (isSummonItem(current)) current.setAmount(current.getAmount() + SUMMONS_PER_KILL);
		else player.getInventory().setItem(slot, createSummonItem(SUMMONS_PER_KILL));

		damagerPlayer.sendMessage(ChatColorHelper.color("&2&l(!) &rYou got a kill and gained &e"
				+ SUMMONS_PER_KILL + " &rextra &2&lSummons Call"));
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (!isSummonItem(item)) return;
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

		if (!summonAbility.isReady()) {
			summonAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}

		summon(nextMob);
		summonAbility.use();

		// The next summon is another random mob, which the egg and its name show
		int summonsLeft = item.getAmount() - 1;
		int slot = player.getInventory().getHeldItemSlot();
		if (summonsLeft == 0) {
			player.getInventory().setItem(slot, barrierItem);
			return;
		}

		nextMob = SummonMob.values()[random.nextInt(SummonMob.values().length)];
		player.getInventory().setItem(slot, createSummonItem(summonsLeft));
	}

	/** Throws an egg, which spawns the mob where it lands. */
	private void summon(SummonMob mob) {
		ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				Location hitLoc = this.getBaseProj().getEntity().getLocation();
				SoundManager.playSuccessfulHit(player);

				switch (mob) {
					case ZOMBIE: spawnZombie(hitLoc); break;
					case CREEPER: spawnCreeper(hitLoc); break;
					case SKELETON: spawnSkeleton(hitLoc); break;
				}
			}
		}, new ItemStack(Material.MONSTER_EGG));

		instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(SUMMON_SPEED));
	}

	@SuppressWarnings("deprecation")
	private <T extends Creature> T spawnMob(Location location, EntityType type, String name) {
		@SuppressWarnings("unchecked")
		T mob = (T) player.getWorld().spawnCreature(location, type);
		mob.setRemoveWhenFarAway(false);
		mob.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &e" + name));
		mob.setCustomNameVisible(true);
		mob.setTarget(instance.getNearestPlayer(player, mob, 150));
		return mob;
	}

	private void spawnZombie(Location location) {
		Zombie zombie = spawnMob(location, EntityType.ZOMBIE, "Zombie");
		zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, false, false));

		// Full unbreakable leather armor and an unbreakable diamond sword
		EntityEquipment equipment = zombie.getEquipment();
		equipment.setHelmet(ItemHelper.setUnbreakable(new ItemStack(Material.LEATHER_HELMET)));
		equipment.setChestplate(ItemHelper.setUnbreakable(new ItemStack(Material.LEATHER_CHESTPLATE)));
		equipment.setLeggings(ItemHelper.setUnbreakable(new ItemStack(Material.LEATHER_LEGGINGS)));
		equipment.setBoots(ItemHelper.setUnbreakable(new ItemStack(Material.LEATHER_BOOTS)));
		equipment.setItemInHand(ItemHelper.setUnbreakable(new ItemStack(Material.DIAMOND_SWORD)));
	}

	private void spawnCreeper(Location location) {
		Creeper creeper = spawnMob(location, EntityType.CREEPER, "Creeper");
		creeper.setPowered(true);
	}

	private void spawnSkeleton(Location location) {
		Skeleton skeleton = spawnMob(location, EntityType.SKELETON, "Skeleton");

		// An unbreakable bow with Flame, and unbreakable gold armor
		ItemStack flameBow = new ItemStack(Material.BOW);
		flameBow.addEnchantment(Enchantment.ARROW_FIRE, 1);

		EntityEquipment equipment = skeleton.getEquipment();
		equipment.setItemInHand(ItemHelper.setUnbreakable(flameBow));
		equipment.setHelmet(ItemHelper.setUnbreakable(new ItemStack(Material.GOLD_HELMET)));
		equipment.setChestplate(ItemHelper.setUnbreakable(new ItemStack(Material.GOLD_CHESTPLATE)));
	}
}
