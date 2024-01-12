package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
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

public class JebClass extends BaseClass {

	private int cooldownSec;

	public JebClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
	}

	public ItemStack makeGray(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.GRAY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("jeb_");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4));
		playerEquip.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		playerEquip.setBoots(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4));
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 0));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		jeb.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.STONE, 1), "" + ChatColor.GRAY + "Jeb's Call"));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Jeb
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (10000 - jeb.getTime()) / 1000 + 1;

			if (jeb.getTime() < 10000) {
				String msg = instance.getGameManager().getMain()
						.color("&7Jeb's Call &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "jeb.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &7Jeb's Call");
				getActionBarManager().setActionBar(player, "jeb.cooldown", msg, 2);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();

		if (item != null) {
			if (item.getType() == Material.STONE
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (jeb.getTime() < 10000) {
					int seconds = (10000 - jeb.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Jeb's always in a rush man.. Please wait for " + ChatColor.YELLOW + seconds
							+ " more seconds gosh damn");
				} else {
					jeb.restart();
					int range = 25;
					Location endLoc = player.getEyeLocation();
					BlockIterator b = new BlockIterator(player.getEyeLocation(), 0, range);
					player.playSound(player.getLocation(), Sound.DIG_STONE, 1, 1);

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
								0.0F, 0.0F, 0.0F, 1, new BlockTexture(Material.STONE));
					}

					for (Player p : instance.players) {
						if (p != player) {
							Vector d = p.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
							double dist = d.dot(dir);

							if (dist < maxDist) {
								Location closest = player.getEyeLocation().add(dir.clone().multiply(dist));

								if (closest.distanceSquared(p.getLocation().add(0, 1, 0)) <= 1.5 * 1.5) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(p).equals(instance.team.get(player)))) {
											p.setVelocity(dir.clone().multiply(dist / 4.3));
											p.playSound(p.getLocation(), Sound.DIG_STONE, 1, 1);
										}
									} else {
										p.setVelocity(dir.clone().multiply(dist / 4.3));
										p.playSound(p.getLocation(), Sound.DIG_STONE, 1, 1);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Jeb;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.STONE_SWORD),
						"" + ChatColor.BLACK + ChatColor.BOLD + "Jeb's Sword"), Enchantment.KNOCKBACK, 1),
				Enchantment.DURABILITY, 10000);
		return item;
	}
}
