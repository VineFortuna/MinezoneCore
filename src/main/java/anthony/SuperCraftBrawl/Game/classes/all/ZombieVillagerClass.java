package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import org.bukkit.*;
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

public class ZombieVillagerClass extends BaseClass {

	public ZombieVillagerClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.ZombieVillager;
	}

	public ItemStack makeColor(ItemStack armour, Color c) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(c);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ1YzExZTAzMjcwMzU2NDljYTA2MDBlZjkzODkwMGUyNWZkMWUzODAxNzQyMmJjOTc0MGU0Y2RhMmNiYTg5MiJ9fX0=";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeColor(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.PURPLE));
		playerEquip.setLeggings(makeColor(new ItemStack(Material.LEATHER_LEGGINGS), Color.GRAY));
		playerEquip.setBoots(makeColor(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4),
				Color.GRAY));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.ROTTEN_FLESH), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
		return item;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.POISONOUS_POTATO, 7), "", "",
						instance.getGameManager().getMain().color("&7Throw at players to infect with:"),
						instance.getGameManager().getMain().color("   &r4 sec Poison III")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.POISONOUS_POTATO
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				int amount = item.getAmount();
				amount--;
				item.setAmount(amount);
				if (amount <= 0)
					player.getInventory().clear(player.getInventory().getHeldItemSlot());

				ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
					@SuppressWarnings("deprecation")
					@Override
					public void onHit(Player hit) {
						if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
							Location hitLoc = this.getBaseProj().getEntity().getLocation();
							player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

							for (Player gamePlayer : this.getNearby(2.5)) {
								if (instance.duosMap != null) {
									if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
										gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 2));
									}
								} else if (gamePlayer != player) {
									gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 2));
								}
							}

							for (Player gamePlayer : instance.players) {
								gamePlayer.playSound(hitLoc, Sound.SPLASH2, 2, 1);
								gamePlayer.playEffect(hitLoc, Effect.SPLASH, 1);
							}
						}

					}

				}, new ItemStack(Material.POISONOUS_POTATO));
				instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
						player.getLocation().getDirection().multiply(2.0D));
			}
		}
	}

}
