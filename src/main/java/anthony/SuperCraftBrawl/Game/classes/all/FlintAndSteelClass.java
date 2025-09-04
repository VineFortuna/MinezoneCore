package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FlintAndSteelClass extends BaseClass {

	private static final double FUSION_ABILITY_DURATION = 10;
	private static final double FUSION_HIT_COMBO_WINDOW = 5;
	private final Ability fusionAbility = new Ability("&8&lFlint&7&&f&lSteel", player);
	private final ItemStack flintItem;
	private final ItemStack steelItem;
	private final ItemStack flintAndSteel;

	private BukkitRunnable runnable;
	private boolean isUsed = false;
	private int initialLives = 0;

	public FlintAndSteelClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTNlOTdmYWI0NzUzYjc1YmE1YjBjMDM4YmVkMzc3YjE2MmJhMjhiN2E1ZTI5MGFiZmQwMThhNTU4MWFjNTM4OCJ9fX0=",
				"F7F7F7",
				"303030",
				"303030",
				6,
				"Flint&Steel"
		);

		String flintName = "&8&lFlint";
		String steelName = "&f&lSteel";
		String durationDisplay = ItemHelper.formatDouble(FUSION_ABILITY_DURATION);
		String windowDisplay = ItemHelper.formatDouble(FUSION_HIT_COMBO_WINDOW);

		String[] lore = new String[]{
				"",
				"&7Combo players with " + flintName + " &7and then " + steelName,
				"&7to combine them and gain &c&oFire Aspect 1",
				"",
				"&7Hit combo window: &a" + windowDisplay + "s",
				fusionAbility.getAbilityName() + " &7lasts for &a" + durationDisplay + "&as"
		};

		// Flint
		flintItem = ItemHelper.setDetails(
				new ItemStack(Material.FLINT),
				flintName,
				lore
		);
		flintItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);

		// Steel

		steelItem = ItemHelper.setDetails(
				new ItemStack(Material.IRON_INGOT),
				steelName,
				lore
		);
		steelItem.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);

		// Flint&Steel
		flintAndSteel = ItemHelper.setDetails(
				new ItemStack(Material.FLINT_AND_STEEL),
				fusionAbility.getAbilityName()
		);
		flintAndSteel.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		flintAndSteel.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
		flintAndSteel.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void DoDamage2(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (instance.duosMap != null)
				if (instance.team.get(p).equals(instance.team.get(player)))
					return;

			if (flintUsed) {
				if (player.getInventory().getItemInHand().equals(steelItem)) {
					flintUsed = false;
					isUsed = false;
					BaseClass bc = instance.classes.get(player);
					if (bc != null)
						initialLives = bc.getLives();

					useFusionAbility();
				}
			}
		}
	}

	private void useFusionAbility() {
		runnable.cancel();
		runnable = null;
		// Feedback message
		player.sendMessage(ChatColorHelper.color(
				"&2&l(!) &rYou got " + fusionAbility.getAbilityName() + "&r for &e" + (int) FUSION_ABILITY_DURATION + "s"
		));
		// Settings items
		player.getInventory().remove(flintItem);
		player.getInventory().remove(steelItem);
		player.getInventory().setItem(0, flintAndSteel);
		player.getInventory().setItem(1, ItemHelper.setDetails(new ItemStack(Material.BARRIER), ""));

		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 1);
		spawnParticles();
		fusionAbilityRunnable();
	}

	private void spawnParticles() {
		Location location = player.getLocation().add(0, 1, 0); // Center at player's torso
		int particles = 2; // Number of particles in the circle
		double radius = 0.5; // Radius of the effect

		for (int i = 0; i < particles; i++) {
			double angle = 2 * Math.PI * i / particles;
			double x = radius * Math.cos(angle);
			double z = radius * Math.sin(angle);

			// Main flame effect
			player.getWorld().playEffect(
					location.clone().add(x, 0, z),
					Effect.MOBSPAWNER_FLAMES,
					0
			);

			// Add smoke particles for depth
			player.getWorld().playEffect(
					location.clone().add(-x, 0.5, -z),
					Effect.LARGE_SMOKE,
					0
			);
		}
	}

	public void fusionAbilityRunnable() {
		if (runnable == null) {
			runnable = new BukkitRunnable() {
				int duration = (int) FUSION_ABILITY_DURATION; // In seconds

				@Override
				public void run() {
					BaseClass bc = instance.classes.get(player);
					if (bc != null) {
						if ((initialLives != bc.getLives()) || instance.state == GameState.ENDED) {
							flintUsed = false;
							isUsed = false;
							runnable = null;
							this.cancel();
						}
					}
					if (duration == 0) {
						player.sendMessage(instance.getGameManager().getMain()
								.color("&2&l(!) &rYour " + fusionAbility.getAbilityName() + "&r ran out!"));
						player.getInventory().remove(flintAndSteel);
						player.getInventory().setItem(0, flintItem);
						player.getInventory().setItem(1, steelItem);
						runnable = null;

						String message = ChatColorHelper.color(fusionAbility.getAbilityName() + " &7ran out");
						getActionBarManager().setActionBar(player, "fns.cooldown", message, 2);

						this.cancel();
						return;
					}


					String message = ChatColorHelper.color(fusionAbility.getAbilityName() + " &7runs out in &e" + duration + "s");
					getActionBarManager().setActionBar(player, "fns.cooldown", message, 2);
					duration--;
				}

			};
			runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
		}
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (runnable == null) {
			if (!isUsed) {
				if (!flintUsed) {
					if (event.getEntity() instanceof Player) {
						Player p = (Player) event.getEntity();
						if (instance.getGameManager().spawnProt.containsKey(p)
								|| instance.getGameManager().spawnProt.containsKey(player))
							return;
							
						if (instance.duosMap != null)
							if (instance.team.get(p).equals(instance.team.get(player)))
								return;

						if (player.getInventory().getItemInHand().equals(flintItem)) {
							flintUsed = true;
							isUsed = true;
							if (runnable == null) {
								runnable = new BukkitRunnable() {
									int ticks = 0;

									@Override
									public void run() {
										if (ticks == FUSION_HIT_COMBO_WINDOW) {
											runnable = null;
											this.cancel();
											flintUsed = false;
											isUsed = false;
											return;
										}

										ticks++;
									}

								};
								runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		flintUsed = false; // Default state
		isUsed = false; // Default state
		playerInv.setItem(0, flintItem);
		playerInv.setItem(1, steelItem);
	}

	@Override
	public ClassType getType() {
		return ClassType.FlintAndSteel;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return this.flintItem;
	}
}
