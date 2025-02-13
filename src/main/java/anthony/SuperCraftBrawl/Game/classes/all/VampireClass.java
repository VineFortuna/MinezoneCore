package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class VampireClass extends BaseClass {

	private boolean hitPlayer = false;
	private boolean launched = false;
	private BukkitRunnable r;

	public VampireClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTI2YTk4ZDQwMzhlYWJhNDdlMDJlZWUxNTUxZGE5OTJhYTVhZDQ2NzA1YTc4MWY0NjE0NzA0MmQyOWNhZjEwNCJ9fX0=",
				"1A1A1A",
				"B20B14",
				"B20B14",
				6,
				"Vampire"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Vampire;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.launched = false;
		this.hitPlayer = false;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setUnbreakable(
						ItemHelper.addEnchant(new ItemStack(Material.BOW),
								Enchantment.ARROW_INFINITE, 1)));
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Arrow) {
			Arrow a = (Arrow) event.getDamager();
			if (a.getShooter() instanceof Player) {
				if (event.getEntity() instanceof Player) {
					Player p = (Player) event.getEntity();
					if (instance.duosMap != null)
						if (instance.team.get(p).equals(instance.team.get(player)))
							return;

					p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 4 * 20, 1));
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 4 * 20, 1));
					this.hitPlayer = true;
				}
			}
		}
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (r != null)
				event.setCancelled(true);

			this.launched = true;
			this.hitPlayer = false;
			cooldown();
		}
	}

	private void restart() {
		this.launched = false;
		this.hitPlayer = false;
		String msg = instance.getGameManager().getMain().color("&9&l(!) &rYou can now use &eVampire's Bow");
		getActionBarManager().setActionBar(player, "vampire.cooldown", msg, 2);
	}

	private void cooldown() {
		if (r == null) {
			r = new BukkitRunnable() {
				int ticks = 7;

				@Override
				public void run() {
					if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Vampire
							&& instance.classes.get(player).getLives() > 0) {
						if (hitPlayer) {
							restart();
							player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
							r = null;
							this.cancel();
						}
						if (ticks == 0) {
							restart();
							String msg = instance.getGameManager().getMain().color("&9&l(!) &rYou can now use &eVampire's Bow");
							getActionBarManager().setActionBar(player, "vampire.cooldown", msg, 2);
							r = null;
							this.cancel();
						} else {
							String msg = instance.getGameManager().getMain()
									.color("&9&l(!) &eVampire's Bow Cooldown: " + ticks + "s");
							getActionBarManager().setActionBar(player, "vampire.cooldown", msg, 2);
						}
					}
					ticks--;
				}
			};
			r.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
		}
	}

	private void abilityMsg() {
		player.sendMessage("");
		player.sendMessage(instance.getGameManager().getMain()
				.color("&e&lCLASS TIP> &rShoot players with your bow to infect them with Poison II for 4 seconds"));
		player.sendMessage("");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.GHAST_TEAR
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			this.abilityMsg();
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.GHAST_TEAR),
						instance.getGameManager().getMain().color("Ghast Tear &7(Right Click)")),
				Enchantment.DAMAGE_ALL, 2), Enchantment.KNOCKBACK, 1);
		return item;
	}

}
