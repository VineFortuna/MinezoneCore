package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.util.ChatColorHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class HerobrineClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack despairItem;
	private final Ability despairAbility = new Ability("&b&lDiamond of Despair", DESPAIR_ABILITY_COOLDOWN / 1000, player);
	private static final double DESPAIR_ABILITY_COOLDOWN = 15 * 1000;
	private static final double DESPAIR_ABILITY_RANGE = 10;
	private final PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, 5 * 20, 2, true, true);
	private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 4 * 20, 1, true, true);


	public HerobrineClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM1YmRkN2VmZjExYzg3ZDUyYTExM2MyZWZiNGNhNDU3NzVlNTY3MzVkYzRiMzhkN2ZhMWRiNzA4NDU4In19fQ==",
				null,
				6,
				"Herobrine"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.GOLD_SWORD),
				"&e&lHerobrine Sword"
		);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
		ItemHelper.setUnbreakable(weapon);

		// Despair Ability
		String radiusDisplay = ItemHelper.formatDouble(DESPAIR_ABILITY_RANGE);

		despairItem = ItemHelper.setDetails(
				new ItemStack(Material.DIAMOND),
				despairAbility.getAbilityNameRightClickMessage(),
				"&7Inflict one of 3 effects on enemies:",
				"&7▶ &3&oSlowness &e" + (slowness.getAmplifier() + 1) + " &7for &e" + slowness.getDuration() / 20 + "s",
				"&7▶ &2&oPoison &e" + (poison.getAmplifier() + 1) + " &7for &e" + poison.getDuration() / 20 + "s",
				"&7▶ Strike a lightning, setting them on &c&oFire",
				"",
				"&7Range: &a" + radiusDisplay + " &7blocks"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		herobrine.startTime = System.currentTimeMillis() - 100000;
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, despairItem);
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Herobrine
				&& instance.classes.get(player).getLives() > 0) {
			int cooldownSec = (int) ((DESPAIR_ABILITY_COOLDOWN - herobrine.getTime()) / 1000 + 1);

			if (herobrine.getTime() < DESPAIR_ABILITY_COOLDOWN) {
				String msg = instance.getGameManager().getMain()
						.color("&b&lDiamond of Despair &rregenerates in: &e" + cooldownSec + "s");
				getActionBarManager().setActionBar(player, "herobrine.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &b&lDiamond of Despair");
				getActionBarManager().setActionBar(player, "herobrine.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.DIAMOND
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (herobrine.getTime() < DESPAIR_ABILITY_COOLDOWN) {
				int seconds = (int) ((DESPAIR_ABILITY_COOLDOWN - herobrine.getTime()) / 1000 + 1);
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Your Diamond of Despair is still regenerating for " + ChatColor.YELLOW + seconds
						+ " more seconds ");
			} else {
				for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), DESPAIR_ABILITY_RANGE, DESPAIR_ABILITY_RANGE, DESPAIR_ABILITY_RANGE)) {
					if (entity instanceof Player && !entity.equals(player)) {
						Player playerInRange = (Player) entity;
						if (!checkIfDead(playerInRange, instance) && !instance.HasSpectator(playerInRange)) {
							useDespairAbility(playerInRange);
							return;
						}
					}
				}
				player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
			}
		}
	}

	private void useDespairAbility(Player playerInRange) {
		herobrine.restart();
		Random rand = new Random();
		int chance = rand.nextInt(3);

		if (chance == 0) {
			playerInRange.addPotionEffect(slowness);
			playerInRange.sendMessage(ChatColorHelper.color("&2&l(!) &e" + player.getName() + " &rslowed you!"));
		}
		else if (chance == 1) {
			playerInRange.addPotionEffect(poison);
			playerInRange.sendMessage(ChatColorHelper.color("&2&l(!) &e" + player.getName() + " &rpoisoned you!"));
		}
		else if (chance == 2) {
			playerInRange.setFireTicks(80);
			instance.getMapWorld().strikeLightningEffect(playerInRange.getLocation());
			playerInRange.sendMessage(ChatColorHelper.color("&2&l(!) &e" + player.getName() + " &rset you on fire!"));
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Herobrine;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
