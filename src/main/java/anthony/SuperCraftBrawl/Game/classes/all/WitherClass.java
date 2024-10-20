package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WitherClass extends BaseClass {

	private int count = 0;
	private BukkitRunnable witherBow;

	public WitherClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRmMzI4ZjUwNDQxMjliNWQxZjk2YWZmZDFiOGMwNWJjZGU2YmQ4ZTc1NmFmZjVjNTAyMDU4NWVlZjhhM2RhZiJ9fX0=",
				"1F1F1F",
				6,
				"Wither"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Wither;
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
		playerInv.setItem(0,
				ItemHelper.addEnchant(
						ItemHelper.addEnchant(new ItemStack(Material.NETHER_STAR), Enchantment.DAMAGE_ALL, 2),
						Enchantment.KNOCKBACK, 1));
		playerInv.setItem(1,
				ItemHelper.setUnbreakable(ItemHelper.addEnchant(new ItemStack(Material.BOW),
						Enchantment.ARROW_INFINITE, 1)));
		playerInv.setItem(35, new ItemStack(Material.ARROW));
		count = 0;
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 95, 0, true));
		}
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntityType() == EntityType.ARROW) {
			event.setCancelled(true);

			if (witherBow == null) {
				WitherSkull skull = player.launchProjectile(WitherSkull.class);
				skull.setIsIncendiary(false);
				count++;
			}

			if (count == 10) {
				if (witherBow == null) {
					witherBow = new BukkitRunnable() {
						int ticks = 5;

						@Override
						public void run() {
							if (ticks <= 5 && ticks > 0) {
								String msg = instance.getGameManager().getMain()
										.color("&9&l(!) &eWither's Bow Cooldown: " + ticks + "s");
								getActionBarManager().setActionBar(player, "wither.cooldown", msg, 2);
							} else if (ticks == 0) {
								witherBow = null;
								this.cancel();
								String msg = instance.getGameManager().getMain()
										.color("&9&l(!) &eYou can now use Wither's Bow");
								getActionBarManager().setActionBar(player, "wither.cooldown", msg, 2);
								count = 0;
							}

							ticks--;
						}

					};
					witherBow.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
				}
			}
		}
	}
	
	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.NETHER_STAR), Enchantment.DAMAGE_ALL, 2),
				Enchantment.KNOCKBACK, 1);
		return item;
	}

}