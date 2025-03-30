package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CreeperClass extends BaseClass {

	private ItemStack weapon;
	private ItemStack potionItem;
	private ItemStack tntItem;
	private ItemStack suicideItem;
	private final Ability potionAbility = new Ability("&a&lDamage Potion", 3, player);
	private final Ability tntAbility = new Ability("&a&lDestructionators", 10, player);
	private final Ability suicideAbility = new Ability("&a&lSelf Explode", 1, player);

	public CreeperClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWZmOGY2ZDAwZDViMDczODc1ODRmMTE3YzY2ZDY5OGM5MGM2OWNlZGIwMWE2ZTY5ZGJiMDI3NzFjNzMwMmQxNiJ9fX0=",
				"62E04A",
				6,
				"Creeper"
		);

		initializeItems();
	}

	private void initializeItems() {
		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.SULPHUR),
				"&a&lCreeper Essence"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Potion ability
		potionItem = ItemHelper.setDetails(
				new ItemStack(Material.POTION),
				potionAbility.getAbilityNameRightClickMessage(),
				"",
				"&7Throw a damage potion far away"
		);
		Potion potion = new Potion(PotionType.INSTANT_DAMAGE);
		potion.setSplash(true);
		potion.apply(potionItem);

		// Tnt ability
		tntItem = ItemHelper.setDetails(
				new ItemStack(Material.TNT),
				tntAbility.getAbilityNameRightClickMessage(),
				"&7Spawn TNT at your location"
		);

		// Suicide Ability
		suicideItem = ItemHelper.setDetails(
				new ItemStack(Material.STONE_BUTTON),
				suicideAbility.getAbilityNameRightClickMessage(),
				"&7Explode yourself, doing damage to &oeveryone"
		);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		tntAbility.getCooldownInstance().reset();
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, potionItem);
		playerInv.setItem(2, tntItem);
		playerInv.setItem(3, suicideItem);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		tntAbility.updateActionBar(player,this);
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof ThrownPotion)) return;
		ThrownPotion potion = (ThrownPotion) entity;
		if (!potion.getItem().isSimilar(potionItem)) return;
		if (!(potion.getShooter() instanceof Player)) return;
		Player shooter = (Player) potion.getShooter();
		potionAbility.use();
		throwPotionFurther(potion);
		setBarrierPotion(shooter);
	}

	private void throwPotionFurther(ThrownPotion potion) {
		Vector velocity = potion.getVelocity();
		potion.setVelocity(velocity.multiply(1.3));
	}

	private void setBarrierPotion(Player shooter) {
		shooter.getInventory().remove(potionItem);
		// Set the barrier after 1 tick (fix I found othewise the barrier won't be set at all)
		Bukkit.getScheduler().runTaskLater(
				instance.getGameManager().getMain(),
				() -> shooter.getInventory().setItem(1, new ItemStack(Material.BARRIER)),
				1L
		);

		// Set the potion after the cooldown
		Bukkit.getScheduler().runTaskLater(
				instance.getGameManager().getMain(),
				() -> {
					shooter.getInventory().remove(Material.BARRIER);
					shooter.getInventory().setItem(1, potionItem);
				},
				(long) (potionAbility.getCooldownDurationSeconds() * 20)
		);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		Action action = event.getAction();

		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;

		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

		if (item.equals(tntItem)) {
			if (!tntAbility.isReady()) return;
			spawnTnt();
			tntAbility.use();
			return;
		}

		if (item.equals(suicideItem)) {
			if (instance.getGameManager().spawnProt.containsKey(player)) return;
			if (!suicideAbility.isReady()) return;
			useSuicideAbility();
			suicideAbility.use();
		}
	}

	private void spawnTnt() {
		TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
		tnt.setFuseTicks(40);
		SoundManager.playSoundToAll(player, Sound.FUSE, 1, 1);
	}

	private void useSuicideAbility() {
		TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
		tnt.setFuseTicks(0);
	}

	@Override
	public ClassType getType() {
		return ClassType.Creeper;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
