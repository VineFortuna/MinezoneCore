package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.gui.CraftableItemsGUI;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class SteveClass extends BaseClass {

	private ItemStack stonePick = ItemHelper.addEnchant(
			ItemHelper.setDetails(new ItemStack(Material.STONE_PICKAXE),
					instance.getGameManager().getMain().color("&rStone Pickaxe &7(Right Click)")),
			Enchantment.DURABILITY, 10000);
	private ItemStack gold = new ItemStack(Material.GOLD_INGOT, goldAmt);
	private ItemStack coal = new ItemStack(Material.COAL, coalAmt);
	private ItemStack diamond = new ItemStack(Material.DIAMOND, diaAmt);
	private boolean kb1 = false;
	private boolean kb2 = false;
	private int kb = 1;
	private int sharp = 1;
	private int fortune = 0;
	//private int blind = 0;

	public SteveClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
	}

	public ItemStack makeBlack(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.BLACK);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal()));
		playerEquip.setChestplate(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 3));
		playerEquip.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		playerEquip.setBoots(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 2));
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, stonePick);
		playerInv.setItem(1, new ItemStack(Material.WORKBENCH));

		if (goldAmt > 0)
			playerInv.setItem(2, new ItemStack(Material.GOLD_INGOT, goldAmt));
		if (coalAmt > 0)
			playerInv.setItem(3, new ItemStack(Material.COAL, coalAmt));
		if (diaAmt > 0)
			playerInv.setItem(4, new ItemStack(Material.DIAMOND, diaAmt));
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;

		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			if (attacker.getInventory().getItemInHand().getType() == Material.STONE_PICKAXE) {
				Random rand = new Random();
				int chance = rand.nextInt(3);
				if (chance == 0) {
					if (!(attacker.getInventory().contains(new ItemStack(Material.COAL, 4)))) {
						attacker.getInventory().addItem(new ItemStack(Material.COAL));
						coalAmt++;
					} else
						attacker.sendMessage("Reached maximum number of coal");
				} else if (chance == 1)
					if (!(attacker.getInventory().contains(new ItemStack(Material.GOLD_INGOT, 4)))) {
						attacker.getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
						goldAmt++;
					} else
						attacker.sendMessage("Reached maximum number of gold");
				else if (chance == 2)
					if (!(attacker.getInventory().contains(new ItemStack(Material.DIAMOND, 4)))) {
						attacker.getInventory().addItem(new ItemStack(Material.DIAMOND));
						diaAmt++;
					} else
						attacker.sendMessage("Reached maximum number of diamond");

				if (instance.blindness == 1) {
					Random rand1 = new Random();
					int chance1 = rand1.nextInt(10);

					if (chance1 == 1 || chance1 == 5 || chance1 == 7) {
						if (event.getEntity() instanceof LivingEntity) {
							((LivingEntity) event.getEntity())
									.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true));
						}
					}
				} else if (instance.blindness == 2) {
					Random rand2 = new Random();
					int chance2 = rand2.nextInt(10);

					if (chance2 == 1 || chance2 == 5 || chance2 == 2) {
						if (event.getEntity() instanceof LivingEntity) {
							((LivingEntity) event.getEntity())
									.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 65, 1, true));
						}
					}
				} else if (instance.blindness == 3) {
					Random rand3 = new Random();
					int chance3 = rand3.nextInt(10);

					if (chance3 == 1 || chance3 == 5 || chance3 == 7 || chance3 == 4) {
						if (event.getEntity() instanceof LivingEntity) {
							((LivingEntity) event.getEntity())
									.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 2, true));
						}
					}
				}
			}
		}
	}

	private void abilityMsg() {
		player.sendMessage("");
		player.sendMessage(instance.getGameManager().getMain().color(
				"&e&lCLASS TIP> &rHit other players to gain items, and when you have 4 of an item, use the crafting table to craft and then right click that block you crafted to give you cool things!"));
		player.sendMessage("");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.WORKBENCH
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			new CraftableItemsGUI(instance.getGameManager().getMain()).inv.open(player);
		} else if (item != null && item.getType() == Material.STONE_PICKAXE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			this.abilityMsg();
		} else if (item != null && item.getType() == Material.GOLD_BLOCK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (kb == 1) {
				ItemHelper.addEnchant(stonePick, Enchantment.KNOCKBACK, kb);
				player.getInventory().setItem(0, stonePick);
				player.getInventory().remove(new ItemStack(Material.GOLD_BLOCK));
				player.sendMessage("Your pickaxe is now Knockback " + kb);
				kb++;
			} else if (kb == 2) {
				ItemHelper.addEnchant(stonePick, Enchantment.KNOCKBACK, kb);
				player.getInventory().setItem(0, stonePick);
				player.getInventory().remove(new ItemStack(Material.GOLD_BLOCK));
				player.sendMessage("Your pickaxe is now Knockback " + kb);
				kb++;
			} else {
				player.sendMessage("Reached maximum Knockback Level");
			}
		} else if (item != null && item.getType() == Material.DIAMOND_BLOCK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (sharp == 1) {
				ItemHelper.addEnchant(stonePick, Enchantment.DAMAGE_ALL, sharp);
				player.getInventory().setItem(0, stonePick);
				player.getInventory().remove(new ItemStack(Material.DIAMOND_BLOCK));
				player.sendMessage("Your pickaxe is now Sharpness " + sharp);
				sharp++;
			} else if (sharp == 2) {
				ItemHelper.addEnchant(stonePick, Enchantment.DAMAGE_ALL, sharp);
				player.getInventory().setItem(0, stonePick);
				player.getInventory().remove(new ItemStack(Material.DIAMOND_BLOCK));
				player.sendMessage("Your pickaxe is now Sharpness " + sharp);
				sharp++;
			} /*
				 * else if (sharp == 3) { ItemHelper.addEnchant(stonePick,
				 * Enchantment.DAMAGE_ALL, sharp); player.getInventory().setItem(0, stonePick);
				 * player.getInventory().remove(new ItemStack(Material.DIAMOND_BLOCK));
				 * player.sendMessage("Your pickaxe is now Sharpness " + sharp); sharp++; }
				 */ else {
				player.sendMessage("Reached maximum Sharpness Level");
			}
		} /*
			 * else if (item != null && item.getType() == Material.IRON_BLOCK &&
			 * (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() ==
			 * Action.RIGHT_CLICK_BLOCK)) { if (fortune == 0) { fortune++;
			 * ItemHelper.addEnchant(woodPick, Enchantment.LOOT_BONUS_BLOCKS, fortune);
			 * player.getInventory().setItem(0, woodPick); player.getInventory().remove(new
			 * ItemStack(Material.IRON_BLOCK));
			 * player.sendMessage("Your pickaxe is now Fortune " + fortune); } else if
			 * (fortune == 1) { fortune++; ItemHelper.addEnchant(woodPick,
			 * Enchantment.LOOT_BONUS_BLOCKS, fortune); player.getInventory().setItem(0,
			 * woodPick); player.getInventory().remove(new ItemStack(Material.IRON_BLOCK));
			 * player.sendMessage("Your pickaxe is now Fortune " + fortune); } else if
			 * (fortune == 2) { fortune++; ItemHelper.addEnchant(woodPick,
			 * Enchantment.LOOT_BONUS_BLOCKS, fortune); player.getInventory().setItem(0,
			 * woodPick); player.getInventory().remove(new ItemStack(Material.IRON_BLOCK));
			 * player.sendMessage("Your pickaxe is now Fortune " + fortune); } else {
			 * player.sendMessage("Reached maximum Fortune Level"); } }
			 */ else if (item != null && item.getType() == Material.COAL_BLOCK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (instance.blindness == 0) {
				instance.blindness++;
				player.getInventory().remove(new ItemStack(Material.COAL_BLOCK));
				player.sendMessage("You can now give other players Blindness " + instance.blindness);
			} else if (instance.blindness == 1) {
				instance.blindness++;
				player.getInventory().remove(new ItemStack(Material.COAL_BLOCK));
				player.sendMessage("You can now give other players Blindness " + instance.blindness);
			} else if (instance.blindness == 2) {
				instance.blindness++;
				player.getInventory().remove(new ItemStack(Material.COAL_BLOCK));
				player.sendMessage("You can now give other players Blindness " + instance.blindness);
			} else {
				player.sendMessage("Reached maximum Blindness Level");
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Steve;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = this.stonePick;
		return item;
	}
}
