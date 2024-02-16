package anthony.SuperCraftBrawl.Game.classes.all;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.BlockWoodButton;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class SlimeClass extends BaseClass {

	private int cooldownSec;

	public SlimeClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.25;
	}

	public ItemStack makeYellow(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.YELLOW);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI1ODk5MDBmNDkwNzM3ZjU1OGQ5NzJhZGViYjQ0YjFhZjk2N2ZlOGQwMzJmMTc4NTU4ZDkzNzcxY2E3YzUzZSJ9fX0=";
		ItemStack skull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(skull);
		playerEquip.setChestplate(makeYellow(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeYellow(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeYellow(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Slime
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (5000 - slimeBall.getTime()) / 1000 + 1;

			if (slimeBall.getTime() < 5000) {
				String msg = instance.getGameManager().getMain()
						.color("&a&lGooey Grenade &rregenerates in: &e" + cooldownSec + "s");
				getActionBarManager().setActionBar(player, "slimeball.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &a&lGooey Grenade");
				getActionBarManager().setActionBar(player, "slimeball.cooldown", msg, 2);
			}
		}
	}

	public ItemStack getSlimeBall() {
		return ItemHelper.setDetails(new ItemStack(Material.SLIME_BALL, 1),
				"" + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + "Gooey Grenade");
	}

	@Override
	public void SetItems(Inventory playerInv) {
		slimeBall.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, this.getAttackWeapon());
		ItemStack slime = getSlimeBall();
		slime.setAmount(1);
		playerInv.setItem(1, slime);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.SLIME_BALL
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
						|| event.getAction() == Action.LEFT_CLICK_AIR
						|| event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (slimeBall.getTime() < 5000) {
					int seconds = (5000 - slimeBall.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Your goo is still regenerating for " + ChatColor.YELLOW + seconds + " more seconds ");
				} else {
					slimeBall.restart();
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@Override
						public void onHit(Player hit) {
							if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();

								for (Player gamePlayer : this.getNearby(3.0)) {
									if (instance.duosMap != null) {
										if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
											@SuppressWarnings("deprecation")
											EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
													DamageCause.VOID, 5.0);
											instance.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											gamePlayer.damage(5.0, player);
										}
									} else {
										EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
												DamageCause.VOID, 5.0);
										instance.getGameManager().getMain().getServer().getPluginManager()
												.callEvent(damageEvent);
										gamePlayer.damage(5.0, player);
									}
								}
								for (Player gamePlayer : instance.players) {
									gamePlayer.playSound(hitLoc, Sound.SLIME_ATTACK, 2, 1);
									gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
								}
							}

						}

					}, new ItemStack(Material.SLIME_BALL));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				}
				event.setCancelled(true);
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Slime;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.WOOD_SWORD),
						"" + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + "Gooey Sword"),
				Enchantment.DURABILITY, 10000);
		return item;
	}
}