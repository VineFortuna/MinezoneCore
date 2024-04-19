package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EnderdragonClass extends BaseClass {

	private int cooldownSec;

	public EnderdragonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.6;
	}

	public ItemStack makeBlack(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.BLACK);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlY2MwNDA3ODVlNTQ2NjNlODU1ZWYwNDg2ZGE3MjE1NGQ2OWJiNGI3NDI0YjczODFjY2Y5NWIwOTVhIn19fQ==";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeBlack(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeBlack(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlack(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL, 5),
				"" + ChatColor.BLACK + ChatColor.BOLD + "Teleporters"));
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 0));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.WEAKNESS)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 0));

		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Enderdragon
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (10000 - pearlTimer.getTime()) / 1000 + 1;

			if (pearlTimer.getTime() < 10000) {
				String msg = instance.getGameManager().getMain()
						.color("&c&lTeleporter &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "teleport.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &c&lTeleporter");
				getActionBarManager().setActionBar(player, "teleport.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		/*
		 * ItemStack item = event.getItem(); if (item != null && item.getType() ==
		 * Material.STONE_SWORD && (event.getAction() == Action.RIGHT_CLICK_AIR ||
		 * event.getAction() == Action.RIGHT_CLICK_BLOCK)) { double boosterStrength =
		 * 1.4; for (Player gamePlayer : instance.players)
		 * gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1); Vector
		 * vel = player.getLocation().getDirection().multiply(boosterStrength);
		 * player.setVelocity(vel); }
		 */
	}

	@Override
	public ClassType getType() {
		return ClassType.Enderdragon;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(new ItemStack(Material.STONE_SWORD), Enchantment.DURABILITY, 1000);
		return item;
	}

}
