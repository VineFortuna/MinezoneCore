package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.Random;

public class ShulkerClass extends BaseClass {

	private static final int BULLET_COOLDOWN_SECONDS = 7;
	/** The enemy goes up for this long, and explodes when it is over. */
	private static final int LEVITATION_TICKS = 20;
	private static final java.awt.Color TRAIL_COLOR = new java.awt.Color(170, 60, 255);

	private final Ability bulletAbility = new Ability("&5&lShulker Bullet", BULLET_COOLDOWN_SECONDS, player);
	private final Random random = new Random();
	private Vector dir;

	public ShulkerClass(GameInstance instance, Player player) {
		super(instance, player);
		this.baseVerticalJump = 1.2;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzY2NDA1MzBkOThkYjkzNGZjNWI5NTVlYTIzYzExYzgwYzRmZGFkMDYxMDAxZThhMjkxM2UzODM5MGRmNjlhNiJ9fX0=",
				"9E759E",
				6,
				"Jeb"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Shulker;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.setDetails(
				new ItemStack(Material.STAINED_CLAY, 1, (byte) DyeColor.PURPLE.getData()),
				"&5&lShell"
		);
		ItemHelper.addEnchant(item, Enchantment.DAMAGE_ALL, 3);
		return ItemHelper.addEnchant(item, Enchantment.KNOCKBACK, 1);
	}

	private ItemStack createBow() {
		ItemStack bow = ItemHelper.setDetails(
				new ItemStack(Material.BOW),
				bulletAbility.getAbilityNameRightClickMessage(),
				"&7Levitate and explode the shot enemy"
		);
		ItemHelper.addEnchant(bow, Enchantment.ARROW_INFINITE, 1);
		ItemHelper.setUnbreakable(bow);
		return ItemHelper.setHideFlags(bow, true);
	}

	@Override
	public void setNameTag() {

	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		bulletAbility.updateActionBar(player, this);
	}

	@Override
	public void setItems(Inventory playerInv) {
		bulletAbility.getCooldownInstance().reset(); // Reset each life
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, createBow());
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	@Override
	public void projectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Arrow)) return;

		if (!bulletAbility.isReady()) {
			event.setCancelled(true);
			bulletAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}

		bulletAbility.use();
		this.dir = player.getLocation().getDirection();
	}

	@Override
	public void doDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (event.getDamager() instanceof Arrow) {
				event.setCancelled(true);
				if (instance.getGameManager().spawnProt.containsKey(p)) return;
				Location loc = p.getLocation();
				Vector v = this.dir;
				v.setY(1.5);
				p.setVelocity(v);
				for (Player gamePlayer : instance.players)
					gamePlayer.playSound(loc, Sound.EXPLODE, 1, 1);

				showLevitationTrail(p);

				Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
					for (Player gamePlayer : instance.players) {
						gamePlayer.playEffect(p.getLocation(), Effect.EXPLOSION_HUGE, 1);
						gamePlayer.playSound(loc, Sound.EXPLODE, 3, 1);
					}

					EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.VOID, 10.0);
					instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
					p.damage(10.0, player);
				}, LEVITATION_TICKS);
			}
		}
	}

	/** A light trail of purple dust follows the enemy while they go up, a single particle each tick. */
	private void showLevitationTrail(final Player target) {
		instance.trackClassTask(new BukkitRunnable() {
			private int ticks = 0;

			@Override
			public void run() {
				if (ticks++ >= LEVITATION_TICKS || !target.isOnline() || !instance.players.contains(target)) {
					cancel();
					return;
				}

				Location feet = target.getLocation().add(
						(random.nextDouble() - 0.5) * 0.3, 0.3, (random.nextDouble() - 0.5) * 0.3);

				ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
				particle.setColor(TRAIL_COLOR);
				particle.setLocation(feet);
				particle.setOffset(0.0f, 0.0f, 0.0f);
				particle.display(instance.players);
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 0, 1));
	}

	@Override
	public void useItem(PlayerInteractEvent event) {

	}

}
