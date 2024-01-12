package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SummonerClass extends BaseClass {
	private ItemStack weapon;
	private ItemStack summonMobItem;
	private BukkitRunnable egg;
	int count = 0;

	public SummonerClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.Summoner;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		// Head (helmet)
		ItemStack playerHead = ItemHelper.createSkullHeadPlayer(1, "Slimess", "&5Summoner Head");

		// Chestplate
		ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.PURPLE, "&5Summoner's Chestplate");
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Leggings
		ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.PURPLE, "&5Summoner Leggings");

		// Boots
		ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.PURPLE, "&5Summoner Boots");
		boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Setting armor
		playerEquip.setHelmet(playerHead);
		playerEquip.setChestplate(chestplate);
		playerEquip.setLeggings(leggings);
		playerEquip.setBoots(boots);
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public void SetItems(Inventory playerInv) {
		count = 0;

		// Weapon
		ItemStack weapon = ItemHelper.create(Material.ENCHANTED_BOOK,"&0Book of Wisdom");
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3); // Sharpness 3
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);  // Knockback 1

		this.weapon = weapon;

		// Summon Mob
		ItemStack summonMobItem = ItemHelper.create(Material.MONSTER_EGG, "&2&lSummons Call");

		this.summonMobItem = summonMobItem;

		// Setting items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, summonMobItem);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null) {
			if (item.equals(summonMobItem)
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (egg != null)
					return;
				else if (count >= 7) {
					player.sendMessage(instance.getGameManager().getMain()
							.color("&c&l(!) &rYou have used the max amount of Spawn Eggs"));
					return;
				}

				Random r = new Random();
				int chance = r.nextInt(3) + 1;

				// Spawn Zombie
				if (chance == 1) {
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							// Playing Sound
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

							// Spawning Zombie
							Zombie en = (Zombie) player.getWorld().spawnCreature(hitLoc, EntityType.ZOMBIE);
							// Customizing Zombie
								// Setting Zombie to not de-spawn when far away
							en.setRemoveWhenFarAway(false);
								// Setting Zombie name to owner's
							en.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eZombie"));
								// Setting Custom name visible
							en.setCustomNameVisible(true);
								// Setting Zombie higher speed
							en.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, false, false));
								// Setting full unbreakable leather armor
							EntityEquipment equipment = en.getEquipment();

									// Helmet
							ItemStack helmet = ItemHelper.create(Material.LEATHER_HELMET);
							ItemHelper.setUnbreakable(helmet);
									// Chestplate
							ItemStack chestplate = ItemHelper.create(Material.LEATHER_CHESTPLATE);
							ItemHelper.setUnbreakable(chestplate);
									// Leggings
							ItemStack leggings = ItemHelper.create(Material.LEATHER_LEGGINGS);
							ItemHelper.setUnbreakable(leggings);
									// Boots
							ItemStack boots = ItemHelper.create(Material.LEATHER_BOOTS);
							ItemHelper.setUnbreakable(boots);

							equipment.setHelmet(helmet);
							equipment.setChestplate(chestplate);
							equipment.setLeggings(leggings);
							equipment.setBoots(boots);

								// Setting unbreakable diamond sword
							ItemStack sword = ItemHelper.create(Material.DIAMOND_SWORD);
							ItemHelper.setUnbreakable(sword);

							equipment.setItemInHand(sword);

							// Damage Summoner when spawns mob
//							EntityDamageEvent damageEvent = new EntityDamageEvent(player, DamageCause.PROJECTILE, 2.0);
//							instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
//							player.damage(2.0);
						}

					}, new ItemStack(Material.MONSTER_EGG));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
					// Spawn Creeper
				} else if (chance == 2) {
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							// Playing Sound
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

							// Spawning Creeper
							Creeper en = (Creeper) player.getWorld().spawnCreature(hitLoc, EntityType.CREEPER);
							// Customizing Creeper
								// Setting Creeper to not de-spawn when far away
							en.setRemoveWhenFarAway(false);
								// Setting Creeper Name to owner's
							en.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eCreeper"));
								// Setting Custom name visible
							en.setCustomNameVisible(true);
								// Setting to Charged Creeper
								en.setPowered(true);
						}

					}, new ItemStack(Material.MONSTER_EGG));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
					// Spawn Skeleton
				} else if (chance == 3) {
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							// Playing Sound
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

							// Spawning Skeleton
							Skeleton en = (Skeleton) player.getWorld().spawnCreature(hitLoc, EntityType.SKELETON);
							// Customizing Skeleton
								// Setting Skeleton to not de-spawn when far away
							en.setRemoveWhenFarAway(false);
								// Setting Skeleton Name to owner's
							en.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eSkeleton"));
								// Setting Custom name visible
							en.setCustomNameVisible(true);
								// Setting Bow
							EntityEquipment equipment = en.getEquipment();
									// Setting Flame
							ItemStack flameBow = ItemHelper.create(Material.BOW);
							flameBow.addEnchantment(Enchantment.ARROW_FIRE, 1);
									// Setting Unbreakable
							ItemHelper.setUnbreakable(flameBow);
							equipment.setItemInHand(flameBow);

								// Setting unbreakable gold armor
									// Helmet
							ItemStack helmet = ItemHelper.create(Material.GOLD_HELMET);
							ItemHelper.setUnbreakable(helmet);
									// Chestplate
							ItemStack chestplate = ItemHelper.create(Material.GOLD_CHESTPLATE);
							ItemHelper.setUnbreakable(chestplate);

							equipment.setHelmet(helmet);
							equipment.setChestplate(chestplate);
						}

					}, new ItemStack(Material.MONSTER_EGG));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				}
				count++;

				if (egg == null) {
					egg = new BukkitRunnable() {
						int ticks = 5;

						@Override
						public void run() {
							if (ticks <= 5 && ticks > 0) {
								String msg = instance.getGameManager().getMain()
										.color("&9&l(!) &eRandom Spawn Egg Cooldown: " + ticks + "s");
								getActionBarManager().setActionBar(player, "random.egg.cooldown", msg, 2);
							} else {
								egg = null;
								this.cancel();
							}

							ticks--;
						}
					};
					egg.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
				}
			}
		}
	}

}
