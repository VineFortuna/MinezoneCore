package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class VillagerClass extends BaseClass {

	private int cooldownSec;

	public VillagerClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcxYjhiMmFlN2ZiMjc4MmRiZWU5M2E3ZTY3OTc4M2M1MGQ1YTg4NDA0NTcwOGEyMTU5NDE3ODVkN2MzY2NkIn19fQ",
				"6E504B",
				"6E504B",
				"828282",
				8,
				"Villager"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Villager;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ItemStack getAttackWeapon() {
		return ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.EMERALD), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		villager.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.BAKED_POTATO, 1), "", "",
						instance.getGameManager().getMain().color("&7Gives players 1 of 3 things:"),
						instance.getGameManager().getMain().color("   &r3 sec Blindness I"),
						instance.getGameManager().getMain().color("   &r3 sec Slowness II"),
						instance.getGameManager().getMain().color("   &r4 sec Weakness I")));
		playerInv.setItem(2, instance.getItemToDrop());
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Villager
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (5000 - villager.getTime()) / 1000 + 1;

			if (villager.getTime() < 5000) {
				String msg = instance.getGameManager().getMain()
						.color("&2Baked Potato &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "potato.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &2Baked Potato");
				getActionBarManager().setActionBar(player, "potato.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.BAKED_POTATO
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				if (villager.getTime() < 5000) {
					int seconds = (5000 - villager.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Yooo you gotta wait "
							+ ChatColor.YELLOW + seconds + " more seconds ");
				} else {
					villager.restart();
					for (Player gamePlayer : instance.players)
						gamePlayer.playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 1, 1);
					
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();
								player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
								Random r = new Random();
								int chance = r.nextInt(100);

								for (Player gamePlayer : this.getNearby(2.5)) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
											if (chance >= 0 && chance <= 40)
												gamePlayer.addPotionEffect(
														new PotionEffect(PotionEffectType.BLINDNESS, 75, 0));
											else if (chance > 40 && chance <= 79)
												gamePlayer.addPotionEffect(
														new PotionEffect(PotionEffectType.SLOW, 75, 1));
											else
												gamePlayer.addPotionEffect(
														new PotionEffect(PotionEffectType.WEAKNESS, 90, 0));
										}
									} else if (gamePlayer != player) {
										if (chance >= 0 && chance <= 40)
											gamePlayer.addPotionEffect(
													new PotionEffect(PotionEffectType.BLINDNESS, 75, 0));
										else if (chance > 40 && chance <= 79)
											gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 75, 1));
										else
											gamePlayer.addPotionEffect(
													new PotionEffect(PotionEffectType.WEAKNESS, 90, 0));
									}
								}
								for (Player gamePlayer : instance.players) {
									gamePlayer.playSound(hitLoc, Sound.SPLASH2, 2, 1);
									gamePlayer.playEffect(hitLoc, Effect.SPLASH, 1);
								}

							}
						}

					}, new ItemStack(Material.BAKED_POTATO));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				}
			}
		}
	}

}
