package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.UUID;

public class BeeClass extends BaseClass {

	private BukkitRunnable weak;

	public BeeClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.4;
	}

	@Override
	public ClassType getType() {
		return ClassType.Bee;
	}

	public ItemStack makeYellow(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.YELLOW);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU1NGIyMTdmMjBmNWNmZjE0YWI0NGRkMjhhMWU5M2VmM2EyYTJiZGQzMjU2ZTlmOWYzMzk0NmU3MDEwYTc3OCJ9fX0=";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeYellow(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 3)));
		playerEquip.setLeggings(makeYellow(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeYellow(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 2)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.GLOWSTONE_DUST),
						instance.getGameManager().getMain().color("&eNectar"), "",
						instance.getGameManager().getMain().color("&7Right click to gain energy")),
				Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);

		return item;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, getAttackWeapon());
		ItemStack pollen = ItemHelper.setDetails(new ItemStack(Material.RED_ROSE, 1, (short) 6),
				instance.getGameManager().getMain().color("Pollen"), "",
				instance.getGameManager().getMain().color("&7Right click to get nutrients"));
		playerInv.setItem(1, pollen);
		playerInv.setItem(2,
				ItemHelper.setDetails(new ItemStack(Material.STICK), instance.getGameManager().getMain().color("&eStinger"),
						instance.getGameManager().getMain().color("&7Hit a player with this to give poison!")));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.state == GameState.ENDED || player.getGameMode() == GameMode.SPECTATOR)
			if (weak != null)
				weak.cancel();
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();

			if (p.getItemInHand().getType() == Material.STICK) {
				if (event.getEntity() instanceof Player) {
					Player p2 = (Player) event.getEntity();
					if (instance.getGameManager().spawnProt.containsKey(p)
							|| instance.getGameManager().spawnProt.containsKey(p2))
						return;

					if (instance.duosMap != null)
						if (instance.team.get(p2).equals(instance.team.get(p)))
							return;

					p2.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 2, true));
					p2.playSound(p2.getLocation(), Sound.SILVERFISH_HIT, 1, 1);
					p.playSound(p2.getLocation(), Sound.SILVERFISH_HIT, 1, 1);
				}

				p.getInventory().clear(p.getInventory().getHeldItemSlot());
				p.sendMessage(instance.getGameManager().getMain()
						.color("&2&l(!) &rYou used your stinger and now you're weak for 5 seconds"));

				if (weak == null) {
					weak = new BukkitRunnable() {
						int ticks = 5;

						@Override
						public void run() {
							if (ticks == 0) {
								weak = null;
								this.cancel();

								if (instance.classes.containsKey(player)
										&& instance.classes.get(player).getType() == ClassType.Bee) {
									p.getInventory().clear(0);
									p.getInventory().addItem(getAttackWeapon());
								}
							} else if (ticks == 5) {
								p.getInventory().clear(0);
								p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1, true));
								ItemStack item = ItemHelper.addEnchant(ItemHelper
										.addEnchant(new ItemStack(Material.GLOWSTONE_DUST), Enchantment.DAMAGE_ALL, 2),
										Enchantment.KNOCKBACK, 2);
								p.getInventory().addItem(item);
							}

							ticks--;
						}

					};
					weak.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
				}
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.GLOWSTONE_DUST
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					if (bee.getTime() < 20000) {
						int seconds = (20000 - bee.getTime()) / 1000 + 1;
						event.setCancelled(true);
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Your Nectar is still regenerating for " + ChatColor.YELLOW + seconds
								+ " more seconds ");
					} else {
						bee.restart();
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 110, 0));
						player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 110, 0));
						player.sendMessage(instance.getGameManager().getMain()
								.color("&2&l(!) &rYour &eNectar &rhas given you more energy for 5 seconds"));
					}
				}
			} else if (item.getType() == Material.RED_ROSE
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 65, 2));
					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 0));
					player.sendMessage(instance.getGameManager().getMain()
							.color("&2&l(!) &rYour Pollen has given you nutrients for 3 seconds"));
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				}
			}
		}
	}

}
