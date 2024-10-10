package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class GrimReaperClass extends BaseClass {

	public GrimReaperClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.6;
	}

	public ItemStack makeRed(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.RED);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY4OGU2MTY0MmEwYjY4NjQzZjRiYTM2OTJmZTIwNjYyMmI0ZDlhN2QzOTY1YmEwYmUxMzI5YzIxMzJkIn19fQ==";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");

		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeRed(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeRed(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeRed(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, new ItemStack(Material.REDSTONE));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0));
	}
	
	private boolean checkIfDead(Player player) {
		if (player.getGameMode() == GameMode.SPECTATOR)
			return true;
		else if (instance.classes.get(player) != null && instance.classes.get(player).getLives() <= 0)
			return true;
		
		return false;
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.REDSTONE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				Location center = player.getLocation();

	            // Circle radius and the height fix to ensure the particles are on the ground
	            int radius = 10;
	            double heightOffset = 0.1;

	            // Create the circle and display particles
	            new BukkitRunnable() {
	                int counter = 0;

	                @Override
	                public void run() {
	                    if (counter >= 5 || checkIfDead(player)) {
	                        cancel(); // Stop the task after 5 seconds or if player dies/leaves
	                        return;
	                    }

	                    // Spawn particles inside the entire circle
	                    for (int i = 0; i < 100; i++) { // Increase this number for denser particles
	                        double angle = Math.random() * 2 * Math.PI; // Random angle
	                        double distance = Math.random() * radius; // Random distance within the radius

	                        double x = distance * Math.cos(angle);
	                        double z = distance * Math.sin(angle);

	                        Location particleLocation = center.clone().add(x, heightOffset, z);
	                        player.getWorld().playEffect(particleLocation, Effect.SMOKE, 0);
	                    }

	                    // Check for players in the radius and apply poison
	                    for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
	                        if (nearbyPlayer != player && nearbyPlayer.getLocation().distance(center) <= radius) {
	                            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 5 * 20, 1));
	                        }
	                    }

	                    counter++;
	                }
	            }.runTaskTimer(instance.getGameManager().getMain(), 0, 20); // Runs every second for 5 seconds
	        }
	    }
	}

	@Override
	public ClassType getType() {
		return ClassType.GrimReaper;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.DIAMOND_HOE),
						"" + ChatColor.RED + ChatColor.BOLD + "Scythe"), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 1);
		return item;
	}

}
