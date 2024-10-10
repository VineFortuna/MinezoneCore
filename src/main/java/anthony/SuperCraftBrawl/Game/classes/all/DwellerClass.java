package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class DwellerClass extends BaseClass {

	private boolean usedBone = false;

	public DwellerClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTAyNzVlMmJmN2Q2N2JlNzA4Yzc5ZDJhMDE5MjI0ODI2YjcwNzJkZjA1Mjk0ODFkNzUwYWIxOTRlZGYxODRhIn19fQ==",
				"A3A3A3",
				8,
				"Dweller"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Dweller;
	}



	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ItemStack getAttackWeapon() {
		return ItemHelper
				.addEnchant(
						ItemHelper
								.addEnchant(
										ItemHelper.setDetails(new ItemStack(Material.BONE), "", "",
												instance.getGameManager().getMain()
														.color("&7Once a life use to throw bone"),
												instance.getGameManager().getMain().color("&7& do insane damage!")),
										Enchantment.DAMAGE_ALL, 4),
						Enchantment.KNOCKBACK, 1);
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		usedBone = false; // To reset each life
		playerInv.setItem(0, this.getAttackWeapon());
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (usedBone == false) {
			if (item != null && item.getType() == Material.BONE
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					usedBone = true;
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@SuppressWarnings("deprecation")
						@Override
						public void onHit(Player hit) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

							for (Player gamePlayer : this.getNearby(3.0)) {
								@SuppressWarnings("deprecation")
								EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
										DamageCause.PROJECTILE, 8.0);
								instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
								gamePlayer.damage(8.0, player);
							}
							for (Player gamePlayer : instance.players) {
								gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
								gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_HUGE, 1);
							}

						}

					}, new ItemStack(Material.BONE));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(3.0D));
				}
				event.setCancelled(true);
			}
		}
	}

}
