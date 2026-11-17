package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FireworkClass extends BaseClass {

	private static final int ROCKET_POWER = 8;
	private static final double ROCKET_UP_VELOCITY = 1;

	/** The effects a shot of the bow can infect with, and the color of the firework that goes off on the one hit. */
	private enum Infection {
		POISON("&2", new PotionEffect(PotionEffectType.POISON, 5 * 20, 1), Color.GREEN),
		BLINDNESS("&8", new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 0), Color.BLACK),
		SLOWNESS("&3", new PotionEffect(PotionEffectType.SLOW, 5 * 20, 2), Color.SILVER),
		WEAKNESS("&7", new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 3), Color.GRAY);

		private final String color;
		private final PotionEffect effect;
		private final Color fireworkColor;

		Infection(String color, PotionEffect effect, Color fireworkColor) {
			this.color = color;
			this.effect = effect;
			this.fireworkColor = fireworkColor;
		}
	}

	private final ItemStack bowItem;
	private final ItemStack rocketItem;
	private final Ability bowAbility = new Ability("&c&lBow", 5, player);
	private final Ability rocketAbility = new Ability("&c&lRocket", player);
	private final Random random = new Random();
	/** The Rocket can only be used once every life. */
	private boolean used = false;

	public FireworkClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzAyZjQ4ZjM0ZDIyZGVkNzQwNGY3NmU4YTEzMmFmNWQ3OTE5YzhkY2Q1MWRmNmU3YTg1ZGRmYWM4NWFiIn19fQ==",
				"C00000",
				"FFFFFF",
				"C00000",
				6,
				"Firework"
		);

		// Rocket Ability, which is also the weapon
		rocketItem = ItemHelper.setDetails(
				new ItemStack(Material.FIREWORK),
				rocketAbility.getAbilityNameRightClickMessage(),
				"&7Ride ur firework highhhh!!"
		);
		rocketItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		rocketItem.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		ItemHelper.setHideFlags(rocketItem, true);

		// Bow Ability
		List<String> bowLore = new ArrayList<>();
		bowLore.add("&7Shoot players to apply one of these effects:");
		for (Infection infection : Infection.values()) {
			PotionEffect effect = infection.effect;
			bowLore.add("&7▸ " + infection.color + "&o" + ItemHelper.getPotionEffectName(effect.getType())
					+ " &e" + (effect.getAmplifier() + 1) + " &7for &e" + effect.getDuration() / 20 + "s");
		}

		bowItem = ItemHelper.setDetails(
				new ItemStack(Material.BOW),
				bowAbility.getAbilityNameRightClickMessage(),
				bowLore.toArray(new String[0])
		);
		bowItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		ItemHelper.setUnbreakable(bowItem);
		ItemHelper.setHideFlags(bowItem, true);
	}

	@Override
	public ClassType getType() {
		return ClassType.Firework;
	}

	@Override
	public void setItems(Inventory playerInv) {
		this.used = false; // Reset each life
		bowAbility.getCooldownInstance().reset(); // Reset each life

		playerInv.setItem(0, rocketItem);
		playerInv.setItem(1, bowItem);
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	@Override
	public ItemStack getAttackWeapon() {
		return rocketItem;
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		bowAbility.updateActionBar(player, this);
	}

	@Override
	public void projectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Arrow)) return;

		if (!bowAbility.isReady()) {
			event.setCancelled(true);
			bowAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}
		bowAbility.use();
	}

	@Override
	public void doDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		if (!(event.getDamager() instanceof Arrow)) return;

		Player target = (Player) event.getEntity();
		Infection infection = Infection.values()[random.nextInt(Infection.values().length)];
		target.addPotionEffect(infection.effect);

		Firework firework = (Firework) target.getWorld().spawnEntity(target.getLocation(), EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower(0);
		meta.addEffect(FireworkEffect.builder().withColor(infection.fireworkColor).flicker(true).build());
		firework.setFireworkMeta(meta);
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null || item.getType() != Material.FIREWORK) return;
		event.setCancelled(true);
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

		if (used) {
			rocketAbility.sendCustomMessage("&c&l(!) &rYou already used your Firework ability this life!");
			return;
		}
		used = true;

		Location rocketLocation = player.getLocation().add(0.5, 1, 0.5);
		Firework firework = (Firework) rocketLocation.getWorld().spawnEntity(rocketLocation, EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower(ROCKET_POWER);
		meta.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());
		firework.setVelocity(new Vector(0, ROCKET_UP_VELOCITY, 0));
		firework.setFireworkMeta(meta);
		firework.setPassenger(player);

		player.sendMessage(ChatColorHelper.color("&2&l(!) &rFlyyy me to the moon!"));
	}
}
