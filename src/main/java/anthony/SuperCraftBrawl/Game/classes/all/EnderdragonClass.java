package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EnderdragonClass extends BaseClass {

	private int cooldownSec;

	public EnderdragonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.6;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U0ZDM1YTJmNmJkNzM5NjA1ZmE0ZWFhNmUxNWQwMzgwMDlmM2YyMGIxYTIwNDYyODAxNjA1ODczNmU3Yjk1ZSJ9fX0=",
				"000000",
				6,
				"EnderDragon"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
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
		ItemStack item = ItemHelper.setUnbreakable(new ItemStack(Material.STONE_SWORD));
		return item;
	}

}
