package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PotatoClass extends BaseClass {

	private ItemStack potato = new ItemStack(Material.POTATO_ITEM);
	private ItemStack bakedPotato = new ItemStack(Material.BAKED_POTATO);
	private ItemStack poisonPotato = new ItemStack(Material.POISONOUS_POTATO);
	private int sharpness = 4;
	public boolean bp = false;

	public PotatoClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Y0NjI0ZWJmN2Q0MTlhMTFlNDNlZDBjMjAzOGQzMmNkMDlhZDFkN2E2YzZlMjBmNjMzOWNiY2ZlMzg2ZmQxYyJ9fX0=",
				"FFB314",
				6,
				"Potato"
		);
	}

	public Core getMain() {
		return instance.getGameManager().getMain();
	}

	@Override
	public ClassType getType() {
		return ClassType.Potato;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0));
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.sharpness = 4; // Default sharpness when spawn
		playerInv.setItem(0, ItemHelper.addEnchant(ItemHelper.addEnchant(potato, Enchantment.DAMAGE_ALL, sharpness),
				Enchantment.KNOCKBACK, 2));
	}
	
	@Override
	public void Tick(int gameTicks) {
		if (player.hasPotionEffect(PotionEffectType.POISON)) {
			if (!(player.getInventory().contains(poisonPotato))) {
				player.getInventory()
						.setItem(0,
								ItemHelper.addEnchant(
										ItemHelper.addEnchant(poisonPotato,
												Enchantment.DAMAGE_ALL, sharpness), Enchantment.KNOCKBACK, 1));
				player.sendMessage(instance.getGameManager().getMain()
						.color("&r&l(!) &rYou recieved a poisonous potato for being poisoned"));
			}
		}
	}
	
	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;
		if (player.getInventory().contains(Material.POISONOUS_POTATO)) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (instance.duosMap != null)
					if (instance.team.get(p).equals(instance.team.get(player)))
						return;
				
				p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, true));
			}
		}
	}

	@Override
	public void TakeDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) {
			if (player.getFireTicks() > 0) {
				if (!(player.getInventory().contains(bakedPotato))) {
					player.getInventory()
							.setItem(0,
									ItemHelper.addEnchant(
											ItemHelper.addEnchant(ItemHelper.addEnchant(bakedPotato,
													Enchantment.DAMAGE_ALL, sharpness), Enchantment.KNOCKBACK, 1),
											Enchantment.FIRE_ASPECT, 1));
					player.sendMessage(instance.getGameManager().getMain()
							.color("&r&l(!) &rYou recieved a baked potato for being on fire"));
				}
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.POTATO_ITEM
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (sharpness <= 4 && sharpness >= 2) {
					sharpness--;
					player.getInventory().removeItem(potato);
					player.removePotionEffect(PotionEffectType.SPEED);
					
					new BukkitRunnable() {
						
						@Override
						public void run() {
							player.getInventory().setItem(0, ItemHelper.addEnchant(new ItemStack(Material.POTATO_ITEM),
									Enchantment.DAMAGE_ALL, sharpness));
							player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 4 - sharpness));
						}
					}.runTaskLater(instance.getGameManager().getMain(), 1);
					
					player.sendMessage(instance.getGameManager().getMain()
							.color("&6&l(!) &rYou gave up a level of Sharpness for some speed"));
				}
			} else if (item.getType() == Material.BAKED_POTATO
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (sharpness <= 4 && sharpness >= 2) {
					sharpness--;
					player.getInventory().removeItem(bakedPotato);
					player.removePotionEffect(PotionEffectType.SPEED);
					
					new BukkitRunnable() {
						
						@Override
						public void run() {
							player.getInventory()
									.setItem(0,
											ItemHelper.addEnchant(
													ItemHelper.addEnchant(new ItemStack(Material.BAKED_POTATO),
															Enchantment.DAMAGE_ALL, sharpness),
													Enchantment.FIRE_ASPECT, 1));
							player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 4 - sharpness));
						}
					}.runTaskLater(instance.getGameManager().getMain(), 1);
					
					player.sendMessage(instance.getGameManager().getMain()
							.color("&6&l(!) &rYou gave up a level of Sharpness for some speed"));
				}
			} else if (item.getType() == Material.POISONOUS_POTATO
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (sharpness <= 4 && sharpness >= 2) {
					sharpness--;
					player.getInventory().removeItem(poisonPotato);
					player.removePotionEffect(PotionEffectType.SPEED);
					
					new BukkitRunnable() {
						
						@Override
						public void run() {
							player.getInventory()
									.setItem(0,
											ItemHelper.addEnchant(
													new ItemStack(Material.POISONOUS_POTATO),
													Enchantment.DAMAGE_ALL, sharpness));
							player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 4 - sharpness));
						}
					}.runTaskLater(instance.getGameManager().getMain(), 1);
					
					player.sendMessage(instance.getGameManager().getMain()
							.color("&6&l(!) &rYou gave up a level of Sharpness for some speed"));
				}
			}
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = player.getInventory().getItem(0);
		return item;
	}

}
