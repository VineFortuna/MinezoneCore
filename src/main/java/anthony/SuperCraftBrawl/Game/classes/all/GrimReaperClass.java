package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class GrimReaperClass extends BaseClass {

	private int cooldownSec = 0, cooldownDuration = 10000;

	public GrimReaperClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;

		createArmor(null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWJiZWE3MTljNTlmODAzZmY4NjQwNWI5M2M2NTA3ODg0NWRiMTY2OWFlMTA0NDQ3ZDhhMGU1MDBjZmNhZTllNCJ9fX0=",
				"141419", 6, "GrimReaper");
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		ItemStack zombieEgg = ItemHelper.createMonsterEgg(EntityType.ZOMBIE, 1, "&2&lZOMBIE POKEBALL");

		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.REDSTONE),
						"" + ChatColor.RED + ChatColor.BOLD + "Spirit Shackles", "",
						"" + ChatColor.RESET + "Right-click to receieve strength & give",
						"" + ChatColor.RESET + "Slowness & Blindness to enemies in a",
						"" + ChatColor.RESET + "10 block radius"));
		playerInv.setItem(2, zombieEgg);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		this.cooldownSec = (this.cooldownDuration - grimReaper.getTime()) / 1000 + 1;
		cooldownActionBar(this.cooldownSec, this.cooldownDuration, grimReaper, ClassType.GrimReaper,
				"grimReaper.cooldown", "Spirit Shackles");

		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0));

		if (player.getInventory().getItem(2) == null || player.getInventory().getItem(2).getType() == Material.AIR) {
			if (checkIfSameClass(player) && checkIfDead(player) == false) {
				player.getInventory().setItem(2, new ItemStack(Material.BARRIER));
			}
		}
	}

	// This function checks if the player's class is still GrimReaper, like for
	// frenzy when your class changes
	private boolean checkIfSameClass(Player player) {
		if (instance.classes.get(player) != null && instance.classes.get(player).getType() == ClassType.GrimReaper)
			return true;

		return false;
	}

	private boolean checkIfDead(Player player) {
		if (player.getGameMode() == GameMode.SPECTATOR)
			return true;
		else if (instance.classes.get(player) != null && instance.classes.get(player).getLives() <= 0)
			return true;

		return false;
	}

	// This function plays a wolf growl when Grim Reaper's ability is used
	private void playAngrySound() {
		for (Player gamePlayer : instance.players) {
			gamePlayer.playSound(player.getLocation(), Sound.WOLF_GROWL, 1.0f, 1.0f);
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.REDSTONE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (grimReaper.getTime() < 10000) {
					int seconds = (10000 - grimReaper.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Your Spirit Shackles is on cooldown for " + ChatColor.YELLOW + seconds + "s");
				} else {
					grimReaper.restart();
					playAngrySound();
					player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 4 * 20, 0));

					// Circle radius and the height fix to ensure the particles are on the ground
					int radius = 10;
					double heightOffset = 0.1;

					// Create the circle and display particles
					new BukkitRunnable() {
						int counter = 0;
						Location center = player.getLocation();

						@Override
						public void run() {
							center = player.getLocation();
							if (counter >= 5 || checkIfDead(player)) {
								cancel(); // Stop the task after 5 seconds or if player dies/leaves
								return;
							}

							// Spawn particles inside the entire circle
							for (int i = 0; i < 100; i++) { // Increase this number for denser particles
								double angle = Math.random() * 2 * Math.PI; // Random angle
								double distance = Math.random() * radius; // Random distance within the radius

								double x = distance * Math.cos(angle);
								double z = distance * Math.sin(angle);

								Location particleLocation = center.clone().add(x, heightOffset, z);
								player.getWorld().playEffect(particleLocation, Effect.SMOKE, 0);
							}

							// Check for players in the radius and apply poison
							for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
								if (nearbyPlayer != player && nearbyPlayer.getLocation().distance(center) <= radius) {
									nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 3));
									nearbyPlayer
											.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1));
								}
							}

							counter++;
						}
					}.runTaskTimer(instance.getGameManager().getMain(), 0, 20); // Runs every second for 5 seconds
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.GrimReaper;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.DIAMOND_HOE),
								"" + ChatColor.RED + ChatColor.BOLD + "Scythe", "",
								"" + ChatColor.RESET + "Getting kills regens your Zombie Pokeball"),
						Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);
		return item;
	}

}
