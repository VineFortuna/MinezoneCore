package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
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
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class EnderdragonClass extends BaseClass {

	private int cooldownSec;

	public EnderdragonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.5;
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

		meta.setOwner("Ender_dragon");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeBlack(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 3)));
		playerEquip.setLeggings(makeBlack(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlack(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 2)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.addItem(this.getAttackWeapon());
		playerInv.addItem(ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL, 5),
				instance.getManager().getMain().color("&c&lTeleporter")));
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
				String msg = instance.getManager().getMain()
						.color("&c&lTeleporter &rregenerates in: &e" + this.cooldownSec + "s");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			} else {
				String msg = instance.getManager().getMain().color("&rYou can use &c&lTeleporter");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
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
