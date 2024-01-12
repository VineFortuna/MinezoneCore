package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
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
import org.bukkit.util.Vector;

public class NinjaClass extends BaseClass {

	private Cooldown shurikenCooldown = new Cooldown(200);
	private int dashCooldown = 12 * 1000;
	private int cooldownSec;
	private int regenStars = 0;
	private int starsCooldown = 0;
	private boolean usedAllStars = false;
	private ItemStack barrier = new ItemStack(Material.BARRIER);

	public NinjaClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	public ItemStack makeBlack(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.BLACK);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("_fergul_");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeBlack(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeBlack(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlack(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public ItemStack getShuriken() {
		return ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR, 1), ChatColor.GRAY + "Shuriken", "",
				ChatColor.YELLOW + "Right click to throw a deadly star!");
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.GHAST_TEAR) {
			if (player.getLocation().distanceSquared(event.getEntity().getLocation()) > 1.0)
				event.setCancelled(true);
			else {
				for (Player gamePlayer : instance.players)
					gamePlayer.playSound(event.getEntity().getLocation(), Sound.BAT_DEATH, 1, 2);
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
//		ninja.startTime = 10000; //Reset cooldown
		this.regenStars = 0;
		this.starsCooldown = 0;
		this.usedAllStars = false;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv
				.setItem(1,
						ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.GHAST_TEAR), ChatColor.GRAY + "Wakizashi",
										"",
										"" + ChatColor.RESET + ChatColor.GRAY
												+ "Only does damage if 1 block away from enemies"),
								Enchantment.DAMAGE_ALL, 8));
		ItemStack shuriken = getShuriken();
		shuriken.setAmount(5);
		playerInv.setItem(2, shuriken);

	}

	private int getNumberOfShurikens() {
		int count = 0;
		for (ItemStack item : player.getInventory().getContents())
			if (item != null && item.getType() == Material.NETHER_STAR)
				count += item.getAmount();
		return count;
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getLives() > 0) {
			if (gameTicks % 20 == 0) {
				if (this.starsCooldown != 0) {
					this.starsCooldown--;
				} else {
					if (this.usedAllStars == true && this.starsCooldown == 0) {
						if (this.regenStars != 5) {
							player.getInventory().remove(this.barrier);
							player.getInventory().addItem(getShuriken());
							this.regenStars++;
						} else {
							this.usedAllStars = false;
							this.regenStars = 0;
							this.starsCooldown = 0;
						}
					}
				}
				if (this.usedAllStars == false && player.getInventory().getItem(2) == null
						|| player.getInventory().getItem(1).getType() == Material.AIR) {
					this.usedAllStars = true;
					player.getInventory().setItem(2, this.barrier);
					this.starsCooldown = 3;
				}
			}

			if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Ninja
					&& instance.classes.get(player).getLives() > 0) {
				this.cooldownSec = (dashCooldown - ninja.getTime()) / 1000 + 1;

				if (ninja.getTime() < dashCooldown) {
					String msg = instance.getGameManager().getMain()
							.color("&7Katana Dash &rregenerates in: &e" + this.cooldownSec + "s");
					getActionBarManager().setActionBar(player, "dash.cooldown", msg, 2);
				} else {
					String msg = instance.getGameManager().getMain().color("&rYou can use &7Katana Dash");
					getActionBarManager().setActionBar(player, "dash.cooldown", msg, 2);
				}
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.STICK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (ninja.getTime() < dashCooldown) {
				int seconds = (dashCooldown - ninja.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Your Katana boost is on cooldown for " + ChatColor.YELLOW + seconds + " more seconds ");
			} else {
				ninja.restart();
				double boosterStrength = 1.4;
				for (Player gamePlayer : instance.players)
					gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
				Vector vel = player.getLocation().getDirection().multiply(boosterStrength);
				player.setVelocity(vel);
			}
		} else if (item != null && item.getType() == Material.NETHER_STAR && item.isSimilar(getShuriken())
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
						|| event.getAction() == Action.LEFT_CLICK_AIR
						|| event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (shurikenCooldown.useAndResetCooldown()) {
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);
						ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								if (instance.duosMap != null)
									if (instance.team.get(hit).equals(instance.team.get(player)))
										return;

								player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
								hit.damage(2.0, player);
								for (Player gamePlayer : instance.players)
									gamePlayer.playSound(hit.getLocation(), Sound.EXPLODE, 2, 1);

							}

						}, new ItemStack(Material.NETHER_STAR));
						instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(3.0D));
					}
					event.setCancelled(true);
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Ninja;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.STICK), ChatColor.GRAY + "Katana", "",
								ChatColor.YELLOW + "Right click to boost the way you're looking!"),
						Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);
		return item;
	}

}
