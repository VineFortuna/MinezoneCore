package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
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

public class NotchClass extends BaseClass {

	private int cooldownSec;

	public NotchClass(GameInstance instance, Player player) {
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

		meta.setOwner("Notch");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4));
		playerEquip.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		playerEquip.setBoots(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		notch.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.STONE_SWORD),
						"" + ChatColor.BLACK + ChatColor.BOLD + "Notch's Sword"), Enchantment.KNOCKBACK, 1),
				Enchantment.DURABILITY, 10000));
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.EMERALD), "" + ChatColor.GRAY + "#RightClick"));
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 0));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Notch
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (10000 - notch.getTime()) / 1000 + 1;

			if (notch.getTime() < 10000) {
				String msg = instance.getGameManager().getMain()
						.color("&0Collape X-Axis &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "notch.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &0Collape X-Axis");
				getActionBarManager().setActionBar(player, "notch.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		if (item.getType() == Material.EMERALD
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			player.getInventory().clear(player.getInventory().getHeldItemSlot());
			Random r = new Random();
			int chance = r.nextInt(100);

			if (chance >= 0 && chance <= 85) {
				player.getInventory()
						.addItem(ItemHelper.setDetails(new ItemStack(Material.GRASS),
								"" + ChatColor.BLACK + "Collapse X-Axis", "",
								instance.getGameManager().getMain().color("&7Pull enemies when aiming at them!"),
								instance.getGameManager().getMain().color("   &rRange: &e20 blocks")));
				player.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &rYou were given &0Collapse X-Axis"));
			} else {
				player.getInventory()
						.addItem(ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
								"" + ChatColor.YELLOW + "Teleport a Player", "",
								instance.getGameManager().getMain().color("&7Teleport a random player to you!")));
				player.sendMessage(
						instance.getGameManager().getMain().color("&2&l(!) &rYou were given &eTeleport a Player"));
			}
		} else if (item.getType() == Material.GRASS
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (notch.getTime() < 10000) {
				int seconds = (10000 - notch.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Bro... You're still on cooldown for " + ChatColor.YELLOW + seconds + " more seconds ");
			} else {
				notch.restart();
				int range = 25;
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
					ParticleEffect.BLOCK_CRACK.display(player.getEyeLocation().add(dir.clone().multiply(t)), 0.0F, 0.0F,
							0.0F, 0.0F, 1, new BlockTexture(Material.GRASS));
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
										p.setVelocity(dir.clone().multiply(-dist / 3.5));
									}
								} else {
									p.setVelocity(dir.clone().multiply(-dist / 3.5));
								}
							}
						}
					}
				}
			}
		} else if (item.getType() == Material.NETHER_STAR
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (!(player.isOnGround())) {
				player.sendMessage(
						instance.getGameManager().getMain().color("&c&l(!) &rYou need to be on the ground to use this!"));
				return;
			}
			Random random = new Random();
			Player gamePlayer = null;
			boolean check = false;

			while (check == false) {
				gamePlayer = instance.players.get(random.nextInt(instance.players.size()));

				if (gamePlayer != player && gamePlayer.getGameMode() != GameMode.SPECTATOR)
					if (instance.classes.containsKey(gamePlayer) && instance.classes.get(gamePlayer).getLives() > 0)
						check = true;
			}

			gamePlayer.teleport(player);
			gamePlayer.sendMessage(
					instance.getGameManager().getMain().color("&2&l(!) &rYou got teleported to &e" + player.getName()));
			player.sendMessage(instance.getGameManager().getMain()
					.color("&2&l(!) &rYou teleported &e" + gamePlayer.getName() + "&r to you!"));
			player.getInventory().clear(player.getInventory().getHeldItemSlot());
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Notch;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.STONE_SWORD),
						"" + ChatColor.BLACK + ChatColor.BOLD + "Notch's Sword"), Enchantment.KNOCKBACK, 1),
				Enchantment.DURABILITY, 10000);
		return item;
	}
}