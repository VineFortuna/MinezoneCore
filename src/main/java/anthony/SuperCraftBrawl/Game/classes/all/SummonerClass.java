package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class SummonerClass extends BaseClass {

	private BukkitRunnable egg;
	int count = 0;

	public SummonerClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.Summoner;
	}

	public ItemStack makePurple(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.PURPLE);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("Slimess");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makePurple(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makePurple(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makePurple(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK),
						instance.getManager().getMain().color("&0Book of Wisdom")), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 1);
		return item;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public void SetItems(Inventory playerInv) {
		count = 0;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.MONSTER_EGG),
				instance.getManager().getMain().color("&2&lRandom Mob Pokeball")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null) {
			if (item.getType() == Material.MONSTER_EGG
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (egg != null)
					return;
				else if (count >= 7) {
					player.sendMessage(instance.getManager().getMain()
							.color("&c&l(!) &rYou have used the max amount of Spawn Eggs"));
					return;
				}

				Random r = new Random();
				int chance = r.nextInt(3);

				if (chance == 0) {
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
							Entity en = (Zombie) player.getWorld().spawnCreature(hitLoc, EntityType.ZOMBIE);
							EntityEquipment e = ((CraftLivingEntity) en).getEquipment();
							e.setHelmet(new ItemStack(Material.LEATHER_HELMET));
							e.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
							e.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
							e.setBoots(new ItemStack(Material.LEATHER_BOOTS));
							e.setItemInHand(new ItemStack(Material.IRON_SWORD));

							en.setCustomName(
									"" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Zombie");
							EntityDamageEvent damageEvent = new EntityDamageEvent(player, DamageCause.PROJECTILE, 2.0);
							instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
							player.damage(2.0);
						}

					}, new ItemStack(Material.MONSTER_EGG));
					instance.getManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				} else if (chance == 1) {
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
							Creeper en = (Creeper) player.getWorld().spawnCreature(hitLoc, EntityType.CREEPER);
							en.setPowered(true);
							en.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW
									+ "Charged Creeper");
							EntityDamageEvent damageEvent = new EntityDamageEvent(player, DamageCause.PROJECTILE, 3.5);
							instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
							player.damage(4.0);
						}

					}, new ItemStack(Material.MONSTER_EGG));
					instance.getManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				} else if (chance == 2) {
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
							Skeleton en = (Skeleton) player.getWorld().spawnCreature(hitLoc, EntityType.SKELETON);
							EntityEquipment e = ((CraftLivingEntity) en).getEquipment();
							e.setHelmet(new ItemStack(Material.GOLD_HELMET));
							e.setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
							ItemStack item = ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_FIRE,
									1);
							e.setItemInHand(item);
							en.setCustomName(
									"" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Skeleton");
							EntityDamageEvent damageEvent = new EntityDamageEvent(player, DamageCause.PROJECTILE, 3.0);
							instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
							player.damage(3.0);
						}

					}, new ItemStack(Material.MONSTER_EGG));
					instance.getManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				}
				count++;

				if (egg == null) {
					egg = new BukkitRunnable() {
						int ticks = 5;

						@Override
						public void run() {
							if (ticks <= 5 && ticks > 0) {
								String msg = instance.getManager().getMain()
										.color("&9&l(!) &eRandom Spawn Egg Cooldown: " + ticks + "s");
								PacketPlayOutChat packet = new PacketPlayOutChat(
										ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
								CraftPlayer craft = (CraftPlayer) player;
								craft.getHandle().playerConnection.sendPacket(packet);
							} else {
								egg = null;
								this.cancel();
							}

							ticks--;
						}
					};
					egg.runTaskTimer(instance.getManager().getMain(), 0, 20);
				}
			}
		}
	}

}
