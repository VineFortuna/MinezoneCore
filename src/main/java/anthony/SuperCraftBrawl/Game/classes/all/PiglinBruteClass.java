package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PiglinBruteClass extends BaseClass {

	private static final int STARTING_GOLD_BALLS = 5;
	private static final int GOLD_BALLS_PER_KILL = 2;
	private static final double GOLD_BALL_DAMAGE = 4.0;
	private static final double GOLD_BALL_RADIUS = 3.0;
	private static final double GOLD_BALL_SPEED = 2.5;

	private final ItemStack weapon;
	private final ItemStack goldBallsItem;
	private final ItemStack barrierItem;
	private final Ability goldBallsAbility = new Ability("&e&lGold Balls", player);

	public PiglinBruteClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzMDBlOTAyNzM0OWM0OTA3NDk3NDM4YmFjMjllM2E0Yzg3YTg0OGM1MGIzNGMyMTI0MjcyN2I1N2Y0ZTFjZiJ9fX0=",
				"302F35",
				"302F35",
				"F29271",
				6,
				"PiglinBrute"
		);

		// Weapon
		weapon = ItemHelper.setDetails(new ItemStack(Material.GOLD_AXE), "&e&lAxe");
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 3);
		ItemHelper.setUnbreakable(weapon);

		// Gold Balls
		goldBallsItem = ItemHelper.setDetails(
				new ItemStack(Material.GOLD_BLOCK),
				goldBallsAbility.getAbilityNameRightClickMessage(),
				"&7Throw DEADLY gold balls!",
				"",
				ItemHelper.gainOnKillLine(GOLD_BALLS_PER_KILL, "gold ball", "gold balls")
		);

		// Barrier, for when there are no gold balls left
		barrierItem = ItemHelper.setDetails(
				new ItemStack(Material.BARRIER),
				"&c&lOut of Gold Balls!",
				"",
				"&7Each kill regenerates " + GOLD_BALLS_PER_KILL + " gold balls"
		);
	}

	@Override
	public void setItems(Inventory playerInv) {
		ItemStack goldBalls = goldBallsItem.clone();
		goldBalls.setAmount(STARTING_GOLD_BALLS);

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, goldBalls);
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null || item.getType() != Material.GOLD_BLOCK) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

		event.setCancelled(true);

		if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
		else player.getInventory().setItem(1, barrierItem);

		ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				if (hit != null && hit.getGameMode() == GameMode.SPECTATOR) return;

				Location hitLoc = this.getBaseProj().getEntity().getLocation();
				player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

				for (Player gamePlayer : this.getNearby(GOLD_BALL_RADIUS)) {
					if (gamePlayer == player) continue; // The gold balls don't hurt the one who throws them
					if (instance.duosMap != null && instance.team.get(gamePlayer).equals(instance.team.get(player)))
						continue;
					gamePlayer.damage(GOLD_BALL_DAMAGE, player);
				}

				player.getWorld().playSound(hitLoc, Sound.EXPLODE, 2, 1);
				player.getWorld().playEffect(hitLoc, Effect.EXPLOSION_HUGE, 1);
			}
		}, new ItemStack(Material.GOLD_BLOCK));

		instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(GOLD_BALL_SPEED));
	}

	@Override
	public void killEvent(Player damagerPlayer) {
		if (!instance.classes.containsKey(damagerPlayer) || checkIfDead(player, instance)) return;

		damagerPlayer.sendMessage(instance.color("&2&l(!) &rYou got a kill and gained &e"
				+ GOLD_BALLS_PER_KILL + " &rextra &eGold Balls"));

		ItemStack goldBalls = goldBallsItem.clone();
		goldBalls.setAmount(GOLD_BALLS_PER_KILL);

		// Replace the barrier if there is one, or add to the gold balls left
		ItemStack current = damagerPlayer.getInventory().getItem(1);
		if (current == null || current.getType() != Material.GOLD_BLOCK)
			damagerPlayer.getInventory().setItem(1, goldBalls);
		else
			damagerPlayer.getInventory().addItem(goldBalls);
	}

	@Override
	public ClassType getType() {
		return ClassType.PiglinBrute;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
