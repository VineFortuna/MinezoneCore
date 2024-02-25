package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class FadeClass extends BaseClass {

	private ItemStack string = ItemHelper.setDetails(new ItemStack(Material.STRING),
			"" + ChatColor.RESET + "Fade Ability");
	private int cooldownSec = 0;

	public FadeClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.15;
	}

	@Override
	public ClassType getType() {
		return ClassType.Fade;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack head = new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData());

		ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.BLACK);
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.BLACK);

		ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.BLACK);
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		playerEquip.setHelmet(head);
		playerEquip.setChestplate(chestplate);
		playerEquip.setLeggings(leggings);
		playerEquip.setBoots(boots);
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack melee = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.STAINED_CLAY, 1, (short) 15), Enchantment.DAMAGE_ALL, 4),
				Enchantment.KNOCKBACK, 1);
		return melee;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Fade
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (30000 - fadeAbility.getTime()) / 1000 + 1;

			if (fadeAbility.getTime() < 30000) {
				String msg = instance.getGameManager().getMain()
						.color("&rFade Ability regenerates in: &e" + cooldownSec + "s");
				getActionBarManager().setActionBar(player, "slimeball.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use Fade Ability");
				getActionBarManager().setActionBar(player, "slimeball.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		fadeAbility.startTime = System.currentTimeMillis() - 100000;
		fadeAbilityActive = false;
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, string);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.equals(this.string)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
						|| event.getAction() == Action.LEFT_CLICK_AIR
						|| event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (fadeAbility.getTime() < 30000) {
					int seconds = (30000 - fadeAbility.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage(instance.getGameManager().getMain()
							.color("&r&l(!) &rYour Fade Ability is on cooldown for &e" + seconds + "s"));
				} else {
					fadeAbility.restart();
					doFadeAbility();
				}
			}
		}
	}

	/*
	 * This method will make the player fade out of existence for a short period of
	 * time
	 */
	private void doFadeAbility() {
		player.sendMessage(
				instance.getGameManager().getMain().color("&r&l(!) &rYou are now fading out of existence..."));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 180, 0));
		fadeAbilityActive = true;

		BukkitRunnable r = new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (player.getGameMode() == GameMode.SPECTATOR) {
					fadeAbilityActive = false;
					for (Player gamePlayer : instance.players)
						gamePlayer.showPlayer(player);
					this.cancel();
				}
				if (ticks == 0) {
					player.getInventory().setHelmet(new ItemStack(Material.AIR));
				} else if (ticks == 1) {
					player.getInventory().setChestplate(new ItemStack(Material.AIR));
				} else if (ticks == 2) {
					player.getInventory().setLeggings(new ItemStack(Material.AIR));
				} else if (ticks == 3) {
					player.getInventory().setBoots(new ItemStack(Material.AIR));
					for (Player gamePlayer : instance.players)
						gamePlayer.hidePlayer(player);
					
					finishAbility();
					this.cancel();
				}

				ticks++;
			}
		};
		r.runTaskTimer(instance.getGameManager().getMain(), 0, 5);
	}

	private void finishAbility() {
		BukkitRunnable r = new BukkitRunnable() {

			@Override
			public void run() {
				for (Player gamePlayer : instance.players)
					gamePlayer.showPlayer(player);
				SetArmour(player.getEquipment());
				player.sendMessage(
						instance.getGameManager().getMain().color("&r&l(!) &rYou are now visible to all players"));
				fadeAbilityActive = false;
			}
		};
		r.runTaskLater(instance.getGameManager().getMain(), 20 * 8);
	}

}
