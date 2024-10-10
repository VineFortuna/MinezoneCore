package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class HerobrineClass extends BaseClass {

	private int cooldownSec;

	public HerobrineClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM1YmRkN2VmZjExYzg3ZDUyYTExM2MyZWZiNGNhNDU3NzVlNTY3MzVkYzRiMzhkN2ZhMWRiNzA4NDU4In19fQ==",
				null,
				8,
				"Herobrine"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		herobrine.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.DIAMOND),
						"" + ChatColor.RESET + ChatColor.BOLD + "Diamond of Despair", "",
						instance.getGameManager().getMain().color("&7Right click to send effects on enemies!")));
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 1));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Herobrine
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (20000 - herobrine.getTime()) / 1000 + 1;

			if (herobrine.getTime() < 20000) {
				String msg = instance.getGameManager().getMain()
						.color("&b&lDiamond of Despair &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "herobrine.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &b&lDiamond of Despair");
				getActionBarManager().setActionBar(player, "herobrine.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.DIAMOND
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (herobrine.getTime() < 20000) {
				int seconds = (20000 - herobrine.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Your Diamond of Despair is still regenerating for " + ChatColor.YELLOW + seconds
						+ " more seconds ");
			} else {
				herobrine.restart();
				Random rand = new Random();
				int chance = rand.nextInt(3);

				if (chance == 0) {
					for (Player gamePlayers : instance.players) {
						if (gamePlayers != player) {
							if (instance.duosMap != null) {
								if (!(instance.team.get(gamePlayers).equals(instance.team.get(player)))) {
									gamePlayers.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
								}
							} else {
								gamePlayers.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
							}
						}
						gamePlayers.sendMessage(instance.getGameManager().getMain()
								.color("&2&l(!) &e" + player.getName() + " &rslowed all players!"));
					}
				} else if (chance == 1) {
					for (Player gamePlayers : instance.players) {
						if (gamePlayers != player) {
							if (instance.duosMap != null) {
								if (!(instance.team.get(gamePlayers).equals(instance.team.get(player)))) {
									gamePlayers.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
								}
							} else {
								gamePlayers.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
							}
						}
						gamePlayers.sendMessage(instance.getGameManager().getMain()
								.color("&2&l(!) &e" + player.getName() + " &rpoisoned all players!"));
					}
				} else if (chance == 2) {
					for (Player gamePlayers : instance.players) {
						if (gamePlayers != player) {
							if (instance.duosMap != null) {
								if (!(instance.team.get(gamePlayers).equals(instance.team.get(player)))) {
									if (instance.classes.containsKey(gamePlayers)
											&& instance.classes.get(gamePlayers).getLives() > 0) {
										gamePlayers.setFireTicks(90);
										instance.getMapWorld().strikeLightningEffect(gamePlayers.getLocation());
									}
								}
							} else {
								if (instance.classes.containsKey(gamePlayers)
										&& instance.classes.get(gamePlayers).getLives() > 0) {
									gamePlayers.setFireTicks(90);
									instance.getMapWorld().strikeLightningEffect(gamePlayers.getLocation());
								}
							}
						}
						gamePlayers.sendMessage(instance.getGameManager().getMain()
								.color("&2&l(!) &e" + player.getName() + " &rset their enemies on fire!"));
					}
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Herobrine;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.GOLD_SWORD), Enchantment.KNOCKBACK, 2);
		ItemMeta meta = sword.getItemMeta();
		meta.spigot().setUnbreakable(true);
		sword.setItemMeta(meta);
		return sword;
	}
}
