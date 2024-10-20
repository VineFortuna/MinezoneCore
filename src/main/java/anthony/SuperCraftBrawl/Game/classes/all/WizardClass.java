package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class WizardClass extends BaseClass {

	private boolean clicked = false;
	private boolean fireball = false;
	private int fireballs = 3;
	private boolean blindness = false;
	private int cooldownSec;

	public WizardClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.3;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM4NTY0ZTI4YWJhOTgzMDFkYmRhNWZhZmQ4NmQxZGE0ZTJlYWVlZjEyZWE5NGRjZjQ0MGI4ODNlNTU5MzExYyJ9fX0=",
				"C178E8",
				"C178E8",
				"965905",
				6,
				"Wizard"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Wizard;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ItemStack getAttackWeapon() {
		return player.getInventory().getItem(0);
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Wizard
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (3000 - wizard.getTime()) / 1000 + 1;

			if (wizard.getTime() < 3000) {
				String msg = "" + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "Fireballs" + ChatColor.RESET
						+ " regenerates in: " + ChatColor.YELLOW + cooldownSec + "s";
				getActionBarManager().setActionBar(player, "wizard.cooldown", msg, 2);
			} else {
				if (fireball) {
					String msg = "" + ChatColor.RESET + "You can use " + ChatColor.RED + ChatColor.BOLD + "Fireballs";
					getActionBarManager().setActionBar(player, "wizard.cooldown", msg, 2);
				}
			}
		}
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		clicked = false; // To reset each life
		fireball = false;
		blindness = false;
		fireballs = 3;
		playerInv.setItem(0, ItemHelper.setDetails(new ItemStack(Material.STICK),
				"" + ChatColor.RESET + "Magic Wand " + ChatColor.GRAY + "(Right Click)"));
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		Random rand = new Random();
		int chance = rand.nextInt(9);

		if (blindness) {
			if (chance <= 5 && chance > 0) {
				if (event.getEntity() instanceof LivingEntity) {
					((LivingEntity) event.getEntity())
							.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 65, 2, true));
				}
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (player.getGameMode() != GameMode.SPECTATOR) {
			if (item != null) {
				if (item.getType() == Material.STICK) {
					if (clicked == false) {
						player.getInventory().remove(Material.STICK);
						clicked = true;
						Random r = new Random();
						int chance = r.nextInt(3);

						if (chance == 0) {
							for (Player gamePlayer : instance.players) {
								if (gamePlayer != player) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
											gamePlayer.getWorld().strikeLightningEffect(gamePlayer.getLocation());
											gamePlayer.setFireTicks(100);
										}
									} else {
										gamePlayer.getWorld().strikeLightningEffect(gamePlayer.getLocation());
										gamePlayer.setFireTicks(100);
									}
								}
							}
							player.getInventory()
									.setItem(0,
											ItemHelper.addEnchant(
													ItemHelper.setDetails(new ItemStack(Material.STICK),
															"" + ChatColor.RESET + "Magic Wand"),
													Enchantment.DAMAGE_ALL, 4));
							fireball = true;
							player.sendMessage(instance.getGameManager().getMain()
									.color("&e&l(!) &rI cast spell.. Fireball fireball fireball!!"));
						} else if (chance == 1) {
							for (Player gamePlayer : instance.players) {
								if (gamePlayer != player) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
											gamePlayer
													.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 120, 0));
										}
									} else {
										gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 120, 0));
									}
								}
							}
							player.getInventory()
									.setItem(0,
											ItemHelper
													.addEnchant(
															ItemHelper.addEnchant(
																	ItemHelper.setDetails(new ItemStack(Material.STICK),
																			"" + ChatColor.RESET + "Magic Wand"),
																	Enchantment.DAMAGE_ALL, 3),
															Enchantment.KNOCKBACK, 2));
							blindness = true;
							player.sendMessage(instance.getGameManager().getMain()
									.color("&e&l(!) &rI cast spell.. Let my enemy see darkness"));
						} else if (chance == 2) {
							for (Player gamePlayer : instance.players) {
								if (gamePlayer != player) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
											gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
										}
									} else {
										gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
									}
								}
							}
							player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
							player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 0));
							player.getInventory()
									.setItem(0,
											ItemHelper
													.addEnchant(
															ItemHelper.addEnchant(
																	ItemHelper.setDetails(new ItemStack(Material.STICK),
																			"" + ChatColor.RESET + "Magic Wand"),
																	Enchantment.DAMAGE_ALL, 3),
															Enchantment.KNOCKBACK, 1));
							player.sendMessage(instance.getGameManager().getMain()
									.color("&e&l(!) &rI cast spell.. Speedy speedy jumpy jumpy!"));
						}
					} else {
						if (fireball) {
							if (event.getAction() == Action.RIGHT_CLICK_AIR
									|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
								if (wizard.getTime() < 3000) {
									int seconds = (3000 - wizard.getTime()) / 1000 + 1;
									event.setCancelled(true);
									player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "Your Fireballs are still regenerating for " + ChatColor.YELLOW + seconds
											+ "s");
								} else {
									wizard.restart();
									player.launchProjectile(SmallFireball.class);
								}
							}
						}
					}
				}
			}
		}
	}

}
