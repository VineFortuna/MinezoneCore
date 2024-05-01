package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WolfClass extends BaseClass {

	private List<Wolf> wolves = new ArrayList<Wolf>();

	public WolfClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	@Override
	public ClassType getType() {
		return ClassType.Wolf;
	}

	public ItemStack makeGray(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.GRAY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjE5Y2MwNDdhM2ExYzJhNmZjZjVlMjNkNzk4OTUwOTQ5ZjBlYTc2YTU1Mzc3MDJjODBlNTQ1NDA5ZjBiODc0NiJ9fX0=";
		ItemStack skull = ItemHelper.createSkullTexture(texture);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		
		playerEquip.setHelmet(skull);
		playerEquip.setChestplate(makeGray(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGray(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGray(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack bone = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.BONE), Enchantment.DAMAGE_ALL, 4), Enchantment.KNOCKBACK,
				1);
		if (instance.classes.containsKey(player)) {
			BaseClass bc = instance.classes.get(player);

			if (bc != null) {
				if (bc.getLives() == 1) {
					bone = ItemHelper.addEnchant(
							ItemHelper.addEnchant(new ItemStack(Material.BONE), Enchantment.DAMAGE_ALL, 5),
							Enchantment.KNOCKBACK, 2);
				}

			}
		}
		return bone;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void TakeDamage(EntityDamageEvent event) {
		if (instance.getGameManager().spawnProt.containsKey(player)) {
			event.setCancelled(true);
			return;
		}

		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if (e.getDamager() instanceof Player) {
				Player k = (Player) e.getDamager();
				if (instance.getGameManager().spawnProt.containsKey(k)) {
					event.setCancelled(true);
					return;
				}
				if (instance.getGameManager().spawnProt.containsKey(player)) {
					event.setCancelled(true);
					return;
				}
				if (instance.classes.containsKey(k)) {
					if (instance.classes.get(k).getLives() <= 0) {
						event.setCancelled(true);
						return;
					}
				}
				if (instance.HasSpectator(k)) {
					event.setCancelled(true);
					return;
				}

				// Checks if any wolves have been spawned then sets them angry to the enemy that
				// hit the player
				if (!(this.wolves.isEmpty())) {
					for (Wolf w : this.wolves) {
						if (w != null && !(w.isDead())) {
							w.setTarget(k);
							w.setAngry(true);
						}
					}
				}
			}
		}
	}
	
	

	@Override
	public void SetItems(Inventory playerInv) {
		if (!(this.wolves.isEmpty())) // Gets rid of the wolves in the list when player dies
			for (Entity e : player.getWorld().getEntities())
				if (e instanceof Wolf)
					this.wolves.remove(e);

		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.MONSTER_EGG),
				"" + ChatColor.RESET + ChatColor.ITALIC + ChatColor.DARK_GRAY + ChatColor.BOLD + "Wolf Army"));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			ItemMeta meta = item.getItemMeta();
			if (item.getType() == Material.MONSTER_EGG
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (meta != null && meta.getDisplayName().contains("Wolf Army")) {
					player.getInventory().clear(player.getInventory().getHeldItemSlot()); // Gets rid of player's spawn
																							// egg
					for (int i = 0; i < 3; i++) {
						@SuppressWarnings("deprecation")
						Wolf wolf = (Wolf) player.getWorld().spawnCreature(player.getLocation(), EntityType.WOLF);
						wolf.setCustomName(
								"" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Wolf Army");
						this.wolves.add(wolf);
					}
				}
			} else if (item.getType() == Material.BONE
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				int range = 20;
				Location endLoc = player.getEyeLocation();
				BlockIterator b = new BlockIterator(player.getEyeLocation(), 0, range);

				while (b.hasNext()) {
					Block block = b.next();
					endLoc = block.getLocation();

					if (block.getType().isSolid())
						break;
				}

				Vector dir = player.getEyeLocation().getDirection();
				double maxDist = endLoc.distance(player.getEyeLocation());

				for (double t = 1; t < maxDist; t += 0.5) {
					ParticleEffect.BLOCK_CRACK.display(player.getEyeLocation().add(dir.clone().multiply(t)), 0.0F,
							0.0F, 0.0F, 0.0F, 1, new BlockTexture(Material.STONE));
				}

				for (Player p : instance.players) {
					if (p != player) {
						Vector d = p.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
						double dist = d.dot(dir);

						if (dist < maxDist) {
							Location closest = player.getEyeLocation().add(dir.clone().multiply(dist));

							if (closest.distanceSquared(p.getLocation().add(0, 1, 0)) <= 1.5 * 1.5) {
								if (instance.duosMap != null) {
									if (!(instance.team.get(p).equals(instance.team.get(player)))) {
										player.setVelocity(dir.clone().multiply(dist / 3.8));
									}
								} else {
									player.setVelocity(dir.clone().multiply(dist / 3.8));
								}
							}
						}
					}
				}
			}
		}
	}

}
