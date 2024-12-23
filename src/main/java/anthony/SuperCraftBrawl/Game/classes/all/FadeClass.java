package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
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

public class FadeClass extends BaseClass {

	private ItemStack string = ItemHelper.setDetails(new ItemStack(Material.STRING),
			"" + ChatColor.RESET + "Fade Ability");
	private int cooldownSec = 0;
	private BukkitRunnable r;

	public FadeClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.15;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ3MTlmMjFjNWRmYzFjZDgyYWExM2M4N2NjZjhkNDY1MmVjOWUzMjliYjY5ZjM0MDllYmE2NTExYzlkZmMwMyJ9fX0=",
				"16161A",
				6,
				"Fade"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Fade;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
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
			this.cooldownSec = (25000 - fadeAbility.getTime()) / 1000 + 1;

			if (fadeAbility.getTime() < 25000) {
				String msg = instance.getGameManager().getMain()
						.color("&rFade Ability regenerates in: &e" + cooldownSec + "s");
				getActionBarManager().setActionBar(player, "fade.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use Fade Ability");
				getActionBarManager().setActionBar(player, "fade.cooldown", msg, 2);
			}
		}

		if (r != null) {
			if (player.getGameMode() == GameMode.SPECTATOR) {
				r.cancel();
				r = null;
				fadeAbilityActive = false;
				for (Player gamePlayer : instance.players)
					gamePlayer.showPlayer(player);
			} else if (checkIfDead(player, instance)) {
				r.cancel();
				r = null;
				fadeAbilityActive = false;
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
				if (fadeAbility.getTime() < 25000) {
					int seconds = (25000 - fadeAbility.getTime()) / 1000 + 1;
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
		player.getWorld().playSound(player.getLocation(), Sound.PORTAL_TRAVEL, 1, 1);
		player.sendMessage(
				instance.getGameManager().getMain().color("&r&l(!) &rYou are now fading out of existence..."));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 180, 0));
		fadeAbilityActive = true;

		r = new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (player.getGameMode() == GameMode.SPECTATOR) {
					fadeAbilityActive = false;
					for (Player gamePlayer : instance.players)
						gamePlayer.showPlayer(player);
					this.cancel();
					r = null;
				}

				if (checkIfDead(player, instance)) {
					fadeAbilityActive = false;
					this.cancel();
					r = null;
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

					this.cancel();
					r = null;
					finishAbility();
				}

				ticks++;
			}
		};
		r.runTaskTimer(instance.getGameManager().getMain(), 0, 5);
	}

	private void finishAbility() {
		r = new BukkitRunnable() {

			@Override
			public void run() {
				if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Fade
						&& instance.classes.get(player).getLives() > 0) {
					setArmor(player.getEquipment());
					player.sendMessage(
							instance.getGameManager().getMain().color("&r&l(!) &rYou are now visible to all players"));
					player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
				}
				for (Player gamePlayer : instance.players)
					gamePlayer.showPlayer(player);
				fadeAbilityActive = false;
			}
		};
		r.runTaskLater(instance.getGameManager().getMain(), 20 * 8);
	}
}
