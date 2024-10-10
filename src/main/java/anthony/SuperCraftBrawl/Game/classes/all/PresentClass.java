package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Random;

public class PresentClass extends BaseClass {

	public PresentClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.Present;
	}

	public ItemStack makeGreen(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.ORANGE);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(getHelmet(new ItemStack(Material.CHEST)));
		playerEquip.setChestplate(makeGreen(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGreen(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGreen(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		return player.getInventory().getItem(0);
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		baseVerticalJump = 1.0;
		playerInv.setItem(0,
				ItemHelper.setDetails(new ItemStack(Material.CHEST, 1),
						"" + ChatColor.RESET + ChatColor.ITALIC + "Agressive Gift", "",
						"" + ChatColor.RESET + ChatColor.YELLOW + "Steals another player's main item"));
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.TRAPPED_CHEST, 1),
						"" + ChatColor.RESET + ChatColor.ITALIC + "Defensive Gift", "",
						"" + ChatColor.RESET + ChatColor.YELLOW + "Steals another player's armor"));
		playerInv.setItem(2,
				ItemHelper.setDetails(new ItemStack(Material.ENDER_CHEST, 1),
						"" + ChatColor.RESET + ChatColor.ITALIC + "Mythical Gift", "",
						"" + ChatColor.RESET + ChatColor.YELLOW + "Steals another player's effect or double jump!"));
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (player.getGameMode() != GameMode.SPECTATOR) {
			if (item != null) {
				if (item.getType() == Material.CHEST && (event.getAction() == Action.RIGHT_CLICK_AIR
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					if (aggressiveGift.getTime() < 3000) {
						int seconds = (3000 - aggressiveGift.getTime()) / 1000 + 1;
						event.setCancelled(true);
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Your Aggressive Gift is still regenerating for " + ChatColor.YELLOW + seconds + "s");
					} else {
						aggressiveGift.restart();
						ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
								BaseClass bc = instance.classes.get(hit);

								if (bc != null) {
									player.getInventory().setItem(0, bc.getAttackWeapon());
									player.sendMessage(instance.getGameManager().getMain()
											.color("&e&l(!) &rYou stole &e" + hit.getName() + "'s &rattack weapon: &e"
													+ bc.getAttackWeapon().getType().toString()));
								}
							}
						}, new ItemStack(Material.CHEST));
						instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				} else if (item.getType() == Material.TRAPPED_CHEST && (event.getAction() == Action.RIGHT_CLICK_AIR
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					if (defensiveGift.getTime() < 3000) {
						int seconds = (3000 - defensiveGift.getTime()) / 1000 + 1;
						event.setCancelled(true);
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Your Defensive Gift is still regenerating for " + ChatColor.YELLOW + seconds + "s");
					} else {
						defensiveGift.restart();
						ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
								BaseClass bc = instance.classes.get(hit);

								if (bc != null) {
									bc.LoadArmor(player);
									player.sendMessage(instance.getGameManager().getMain()
											.color("&e&l(!) &rYou stole &e" + hit.getName() + "'s &rarmor"));
								}
							}
						}, new ItemStack(Material.CHEST));
						instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				} else if (item.getType() == Material.ENDER_CHEST && (event.getAction() == Action.RIGHT_CLICK_AIR
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					if (mythicalGift.getTime() < 7000) {
						int seconds = (7000 - mythicalGift.getTime()) / 1000 + 1;
						event.setCancelled(true);
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Your Mythical Gift is still regenerating for " + ChatColor.YELLOW + seconds + "s");
					} else {
						mythicalGift.restart();
						ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
								BaseClass bc = instance.classes.get(hit);

								if (bc != null) {
									Random r = new Random();
									int chance = r.nextInt(2);

									if (chance == 0) {
										baseVerticalJump = bc.baseVerticalJump;
										player.sendMessage(instance.getGameManager().getMain()
												.color("&e&l(!) &rYou got &e" + hit.getName() + "'s &rDouble Jump!"));
									} else if (chance == 1) {
										for (PotionEffect type : hit.getActivePotionEffects()) {
											player.addPotionEffect(type);
											player.sendMessage(
													instance.getGameManager().getMain().color("&e&l(!) &rYou got one of &e"
															+ hit.getName() + "'s &rpotion effects!"));
											return;
										}
									}
								}
							}
						}, new ItemStack(Material.ENDER_CHEST));
						instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				}
			}
		}
	}

}
