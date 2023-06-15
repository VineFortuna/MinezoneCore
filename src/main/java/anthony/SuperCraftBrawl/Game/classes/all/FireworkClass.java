package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class FireworkClass extends BaseClass {

	private boolean used = false;
	private int cooldown = 0;
	private String msg = "";
	private PacketPlayOutChat packet;
	private CraftPlayer craft = (CraftPlayer) player;
	private Random r = new Random();

	public FireworkClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.17;
	}

	@Override
	public ClassType getType() {
		return ClassType.Firework;
	}

	public ItemStack makeWhite(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.WHITE);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(new ItemStack(Material.COAL_BLOCK));
		playerEquip.setChestplate(makeWhite(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeWhite(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeWhite(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.used = false; // Reset each life
		this.cooldown = 0; // Reset each life
		this.msg = "";

		playerInv.setItem(0, this.getAttackWeapon());
		playerInv
				.setItem(1,
						ItemHelper.addEnchant(ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.BOW),
										"" + ChatColor.RED + ChatColor.BOLD + "Firework Bow", "",
										instance.getManager().getMain()
												.color("&7Shoot players and infect with either:"),
										instance.getManager().getMain().color("   &r6 sec Poison II"),
										instance.getManager().getMain().color("   &r5 sec Blindness II"),
										instance.getManager().getMain().color("   &r5 sec Slowness III"),
										instance.getManager().getMain().color("   &r10 sec Weakness II")),
								Enchantment.ARROW_INFINITE, 1), Enchantment.DURABILITY, 1000));
		playerInv.setItem(2, new ItemStack(Material.ARROW));

		msg = instance.getManager().getMain().color("&9&l(!) &eYou can use &c&lFirework Bow");
		packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
		craft.getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(
						ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.FIREWORK), "", "",
										instance.getManager().getMain()
												.color("&7Right click to ride ur firework highhhh!!")),
								Enchantment.DAMAGE_ALL, 3),
						Enchantment.KNOCKBACK, 1);
		return item;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public void Tick(int gameTicks) {
		if (gameTicks % 20 == 0)
			if (this.cooldown != 0) {
				this.cooldown--;
				msg = instance.getManager().getMain()
						.color("&9&l(!) &c&lFirework Bow &ecooldown: " + this.cooldown + "s");
				packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
				craft.getHandle().playerConnection.sendPacket(packet);

				if (this.cooldown == 0) {
					msg = instance.getManager().getMain().color("&9&l(!) &eYou can use &c&lFirework Bow");
					packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
					craft.getHandle().playerConnection.sendPacket(packet);
				}
			}
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		Entity e = event.getEntity();

		if (e instanceof Arrow) {
			if (this.cooldown == 0) {
				this.cooldown = 5;
			} else if (this.cooldown > 0) {
				event.setCancelled(true);
				player.sendMessage(instance.getManager().getMain().color(
						"&c&l(!) &rYour &c&lFirework Bow &ris still on cooldown for &e" + this.cooldown + " seconds"));
			}
		}
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player target = (Player) event.getEntity();
			if (event.getDamager() instanceof Arrow) {
				int chance = r.nextInt(4);

				if (chance == 0) {
					target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 130, 1));
					Firework fw = (Firework) target.getLocation().getWorld().spawnEntity(target.getLocation(),
							EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();

					fwm.setPower(1);
					fwm.addEffect(FireworkEffect.builder().withColor(Color.GREEN).flicker(true).build());
					fw.setFireworkMeta(fwm);
				} else if (chance == 1) {
					target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
					Firework fw = (Firework) target.getLocation().getWorld().spawnEntity(target.getLocation(),
							EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();

					fwm.setPower(1);
					fwm.addEffect(FireworkEffect.builder().withColor(Color.BLACK).flicker(true).build());
					fw.setFireworkMeta(fwm);
				} else if (chance == 2) {
					target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
					Firework fw = (Firework) target.getLocation().getWorld().spawnEntity(target.getLocation(),
							EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();

					fwm.setPower(1);
					fwm.addEffect(FireworkEffect.builder().withColor(Color.SILVER).flicker(true).build());
					fw.setFireworkMeta(fwm);
				} else if (chance == 3) {
					target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
					Firework fw = (Firework) target.getLocation().getWorld().spawnEntity(target.getLocation(),
							EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();

					fwm.setPower(1);
					fwm.addEffect(FireworkEffect.builder().withColor(Color.GRAY).flicker(true).build());
					fw.setFireworkMeta(fwm);
				}
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();

		if (item != null && item.getType() == Material.FIREWORK) {
			event.setCancelled(true);
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (!this.used) {
					this.used = true;
					Location newLoc = new Location(player.getWorld(), player.getLocation().getX() + 0.5,
							player.getLocation().getY() + 1, player.getLocation().getZ() + 0.5);
					Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc, EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();

					fwm.setPower(8);
					fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());
					Vector velocity = new Vector(0, 1, 0);
					fw.setVelocity(velocity);
					fw.setFireworkMeta(fwm);
					fw.setPassenger(player);
					player.sendMessage(instance.getManager().getMain().color("&2&l(!) &rFlyyy me to the moon!"));
				} else
					player.sendMessage(instance.getManager().getMain()
							.color("&c&l(!) &rYou already used your Firework ability this life!"));
			}
		}
	}
}
