package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.List;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

public class IceClass extends BaseClass {

	private int cooldownSec;

	public IceClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	@Override
	public ClassType getType() {
		return ClassType.Ice;
	}

	public ItemStack makeBlue(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.SILVER);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(new ItemStack(Material.PACKED_ICE));
		playerEquip.setChestplate(makeBlue(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeBlue(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlue(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 0));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = new ItemStack(Material.STONE_SWORD);
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		ice.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.WOOL),
						instance.getGameManager().getMain().color("&bFreeze Ray"), "",
						instance.getGameManager().getMain().color("&7Right click to shoot a player with freeze ray!")));
		playerInv.setItem(2,
				ItemHelper.setDetails(new ItemStack(Material.PACKED_ICE),
						instance.getGameManager().getMain().color("&bFreeze Bomb"), "",
						instance.getGameManager().getMain().color("&7Right click to freeze nearby enemies!")));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Ice
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (10 * 1000 - ice.getTime()) / 1000 + 1;

			if (ice.getTime() < 10 * 1000) {
				String msg = instance.getGameManager().getMain()
						.color("&b&lFreeze Ray &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "ice.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &b&lFreeze Ray");
				getActionBarManager().setActionBar(player, "ice.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.WOOL
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (ice.getTime() < 10000) {
					int seconds = (10000 - ice.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Broooo... You're still on cooldown for " + ChatColor.YELLOW + seconds + "s");
				} else {
					ice.restart();
					int range = 30;
					Location endLoc = player.getEyeLocation();
					BlockIterator b = new BlockIterator(player.getEyeLocation(), 0, range);

					while (b.hasNext()) {
						Block block = b.next();
						endLoc = block.getLocation();

						if (block.getType().isSolid())
							break;
					}

					Vector dir = player.getEyeLocation().getDirection();
					double maxDist = endLoc.distance(player.getEyeLocation());

					for (double t = 1; t < maxDist; t += 0.5) {
						ParticleEffect.BLOCK_CRACK.display(player.getEyeLocation().add(dir.clone().multiply(t)), 0.0F,
								0.0F, 0.0F, 0.0F, 1, new BlockTexture(Material.ICE));
					}

					for (Player p : instance.players) {
						p.playSound(player.getLocation(), Sound.GLASS, 1.0f, 1.0f);
						if (p != player) {
							Vector d = p.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
							double dist = d.dot(dir);

							if (dist < maxDist) {
								Location closest = player.getEyeLocation().add(dir.clone().multiply(dist));

								if (closest.distanceSquared(p.getLocation().add(0, 1, 0)) <= 1.5 * 1.5) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(p).equals(instance.team.get(player)))) {
											p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2 * 20, 0));
											EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.VOID,
													4.5);
											instance.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											p.damage(4.5, player);
										}
									} else {
										p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2 * 20, 0)); // Slowness
																												// 1 for
																												// 2
																												// seconds
										EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.VOID, 4.5);
										instance.getGameManager().getMain().getServer().getPluginManager()
												.callEvent(damageEvent);
										p.damage(4.5, player);
									}
								}
							}
						}
					}
				}
			} else if (item.getType() == Material.PACKED_ICE
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				List<Entity> nearby = player.getNearbyEntities(10.0, 10.0, 10.0);

				if (nearby.isEmpty()) {
					player.sendMessage(
							instance.getGameManager().getMain().color("&c&l(!) &rNo nearby players have been found :("));
					return;
				}

				for (Entity en : nearby) {
					if (en instanceof Player) {
						Player p = (Player) en;
						if (p.getGameMode() != GameMode.SPECTATOR) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 4));
							Firework firework = p.getWorld().spawn(p.getEyeLocation(), Firework.class);
							FireworkEffect effect = FireworkEffect.builder().flicker(true)
									.withColor(Color.BLUE, Color.WHITE).build();
							FireworkMeta meta = firework.getFireworkMeta();
							meta.clearEffects();
							meta.addEffect(effect);
							firework.setFireworkMeta(meta);
							firework.detonate();
						}
					}
				}
				player.sendMessage(
						instance.getGameManager().getMain().color("&2&l(!) &rYou have &b&lFrozen &rnearby players!"));
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
			}
		}
	}

	@Override
	public void Death(PlayerDeathEvent e) {
		super.Death(e);
		if (player.equals(e.getEntity().getKiller())) {
			if (player.getInventory().contains(Material.PACKED_ICE))
				return;
			// Regeneration ice bomb upon killing.
			player.getInventory()
					.addItem(ItemHelper.setDetails(new ItemStack(Material.PACKED_ICE),
							instance.getGameManager().getMain().color("&bFreeze Bomb"), "",
							instance.getGameManager().getMain().color("&7Right click to freeze nearby enemies!")));
		}
	}
}