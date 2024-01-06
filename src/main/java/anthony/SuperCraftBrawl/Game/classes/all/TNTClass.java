package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class TNTClass extends BaseClass {

	private int cooldownSec;

	public TNTClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	public ItemStack makeRed(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.RED);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(new ItemStack(Material.TNT));
		playerEquip.setChestplate(makeRed(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeRed(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeRed(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public ItemStack getTNT() {
		return ItemHelper.setDetails(new ItemStack(Material.TNT, 1), "", "",
				instance.getManager().getMain().color("&7Spawn TNT to a random player around you"));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		tntItem.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, this.getTNT());
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.TNT
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (10000 - tntItem.getTime()) / 1000 + 1;

			if (tntItem.getTime() < 10000) {
				String msg = instance.getManager().getMain()
						.color("&c&lTNT &rregenerates in: &e" + this.cooldownSec + "s");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			} else {
				String msg = instance.getManager().getMain().color("&rYou can use &c&lTNT");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.TNT && item.getAmount() == 1) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (tntItem.getTime() < 10000) {
					int seconds = (10000 - tntItem.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Your TNT is still regenerating for " + ChatColor.YELLOW + seconds + " more seconds ");
				} else {
					List<Entity> near = player.getNearbyEntities(20.0D, 25.0D, 20.0D);
					for (Entity entity : near) {
						if (entity instanceof Player) {
							Player p = (Player) entity;

							if (p != null && instance.classes.containsKey(p)
									&& instance.classes.get(p).getLives() > 0) {
								if (instance.duosMap != null)
									if (instance.team.get(p).equals(instance.team.get(player)))
										if (near.contains(p))
											near.remove(p);

								if (p.getGameMode() != GameMode.SPECTATOR) {
									p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
									TNTPrimed tnt = p.getWorld().spawn(p.getLocation().add(0, 5, 0), TNTPrimed.class);
									tnt.setFuseTicks(40);
									player.sendMessage(instance.getManager().getMain()
											.color("&e&l(!) &rSpawning a TNT at &e" + p.getName() + "'s &rlocation"));
									tntItem.restart();
									return;
								}
							}
						}
					}

					player.sendMessage(
							instance.getManager().getMain().color("&c&l(!) &rNo nearby players have been found!"));
				}
			}
		}

	}

	@Override
	public ClassType getType() {
		return ClassType.TNT;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1);
		ItemMeta meta = sword.getItemMeta();
		meta.spigot().setUnbreakable(true);
		sword.setItemMeta(meta);
		return sword;
	}

}
