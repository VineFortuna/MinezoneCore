package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.gui.VillagerAbilityGUI;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class VillagerClass extends BaseClass {

	private ItemStack weapon;
	private int emeraldsCount;


	public VillagerClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcxYjhiMmFlN2ZiMjc4MmRiZWU5M2E3ZTY3OTc4M2M1MGQ1YTg4NDA0NTcwOGEyMTU5NDE3ODVkN2MzY2NkIn19fQ",
				"6E504B",
				"6E504B",
				"828282",
				6,
				"Villager"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Villager;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (instance.duosMap != null)
				if (instance.team.get(p).equals(instance.team.get(player)))
					return;
			
			if (instance.getGameManager().spawnProt.containsKey(p)
					|| instance.getGameManager().spawnProt.containsKey(player))
				return;
			
			BaseClass bc = instance.classes.get(player);
			if (bc != null && bc.getLives() <= 0)
				return;
			
			emeraldsCount++;
			weapon.setAmount(emeraldsCount);
			player.getInventory().setItem(0, weapon);
		}
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		// Resetting Emeralds on Death
		emeraldsCount = 0;

		// Weapon
		ItemStack weapon = ItemHelper.setDetails(new ItemStack(Material.EMERALD),
				"&aTrade Ability &7(Right Click)",
				"&fHit enemies to gain emeralds",
				"&fTrade emeralds for items");
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		this.weapon = weapon;

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.BAKED_POTATO, 4), "", "",
						instance.getGameManager().getMain().color("&7Gives players one of the following:"),
						instance.getGameManager().getMain().color("   &r3 sec Blindness I"),
						instance.getGameManager().getMain().color("   &r3 sec Slowness II"),
						instance.getGameManager().getMain().color("   &r4 sec Weakness I")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();
		
		if (item != null && action == Action.RIGHT_CLICK_AIR ||
				action == Action.RIGHT_CLICK_BLOCK) {
			// TRADE ABILITY
			if (item.equals(weapon)) {
				// Check Right CLick
				new VillagerAbilityGUI(
						instance.getGameManager().getMain(),
						instance,
						this
				).inv.open(player);
			} else if (item.getType() == Material.BAKED_POTATO) {
				event.setCancelled(true);
				int amount = item.getAmount();
				if (amount > 0) {
					amount--;
					if (amount == 0)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(amount);
					
					player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 1, 1);
					ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
						@SuppressWarnings("deprecation")
						@Override
						public void onHit(Player hit) {
							if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();
								player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
								Random r = new Random();
								int chance = r.nextInt(100);
								
								for (Player gamePlayer : this.getNearby(2.5)) {
									if (gamePlayer != player && !checkIfDead(player, instance)) {
										if (chance <= 40)
											gamePlayer.addPotionEffect(
													new PotionEffect(PotionEffectType.BLINDNESS, 75, 0));
										else if (chance <= 79)
											gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 75, 1));
										else
											gamePlayer.addPotionEffect(
													new PotionEffect(PotionEffectType.WEAKNESS, 90, 0));
									}
								}
								player.getWorld().playSound(hitLoc, Sound.SPLASH2, 2, 1);
								player.getWorld().playEffect(hitLoc, Effect.SPLASH, 1);
							}
						}
					}, new ItemStack(Material.BAKED_POTATO));
					instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
							player.getLocation().getDirection().multiply(2.0D));
				}
			}
		}
	}

	public int getEmeraldsCount() {
		return emeraldsCount;
	}
	public void setEmeraldsCount(int emeraldsCount) {
		this.emeraldsCount = emeraldsCount;
	}
}