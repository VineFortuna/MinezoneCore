package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.gui.CraftableItemsGUI;
import anthony.util.ItemHelper;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class SteveClass extends BaseClass {

	private final Map<UUID, Set<Block>> tempBlocks = new HashMap<>();

	public SteveClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzVhZDYyNjE4MTExMWU1NWViZmM5MDZhOGU3MDQwYzY2YjhlZmU5NGY3YzA3NDQ4ZDU3MTAwMTJkNjg0MzZjIn19fQ==",
				"00ADAD", "4A4191", "686868", 6, "Steve");
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, getPickaxe());
		playerInv.setItem(2, getUltAbility());
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.STONE_PICKAXE) {
				doPickaxeSmash(player);
			} else if (item.getType() == Material.IRON_DOOR) {
				doHouseBuilderUltimate(player);
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Steve;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.STONE_SWORD), Enchantment.KNOCKBACK, 1);
		return sword;
	}

	public ItemStack getPickaxe() {
		ItemStack pick = new ItemStack(Material.STONE_PICKAXE);
		return pick;
	}

	public ItemStack getUltAbility() {
		ItemStack door = new ItemStack(Material.IRON_DOOR);
		return door;
	}

	// ABILITIES:

	// =========================
	// Ability 1: Pickaxe Smash
	// Damages on landing + renders a radius ring
	// =========================
	private void doPickaxeSmash(final Player p) {
		// if (isOnCooldown(cdPickSmash, p, COOLDOWN_PICKSMASH)) return;

		final World w = p.getWorld();
		final double radius = 3.0;
		final double damage = 4.0; // 2 hearts

		// Small leap forward + up
		Vector dir = p.getLocation().getDirection().normalize();
		Vector launch = dir.multiply(0.9).setY(0.6);
		p.setVelocity(launch);

		w.playSound(p.getLocation(), Sound.ZOMBIE_WOODBREAK, 0.7f, 1.6f);
		w.spigot().playEffect(p.getLocation(), Effect.STEP_SOUND, Material.STONE.getId(), 0, 0, 0, 0, 1, 8, 32);

		// Poll until the player actually lands (max ~3s)
		new BukkitRunnable() {
			int ticks = 0;
			boolean airborneSeen = false;

			@Override
			public void run() {
				if (!p.isOnline()) {
					cancel();
					return;
				}
				ticks++;
				if (ticks > 60) {
					cancel();
					return;
				} // safety timeout (~3s at 20tps)

				// Mark as airborne once there's meaningful vertical speed
				if (Math.abs(p.getVelocity().getY()) > 0.05)
					airborneSeen = true;

				// Landing condition: we were airborne, now touching ground-ish and no longer
				// descending fast
				if (airborneSeen && isOnGround(p.getLocation()) && p.getVelocity().getY() <= 0.02) {
					Location impact = p.getLocation();

					// Visual ring to show radius (draws for ~0.5s)
					drawDamageRing(w, impact.clone().subtract(0, 0.05, 0), radius);

					// Damage + knockback nearby players
					for (Entity ent : w.getNearbyEntities(impact, radius, 2.0, radius)) {
						if (!(ent instanceof Player))
							continue;
						Player target = (Player) ent;
						if (target.equals(p))
							continue;

						// Knockback away from center
						Vector kb = target.getLocation().toVector().subtract(impact.toVector());
						if (kb.lengthSquared() > 0.0001) {
							kb.normalize().multiply(0.9).setY(0.4);
							target.setVelocity(kb);
						}
						target.damage(damage, p);

						// Hit puff on each target
						w.spigot().playEffect(target.getLocation().add(0, 0.6, 0), Effect.CLOUD, 0, 0, 0, 0, 0, 0.02f,
								8, 24);
					}

					// Center boom effects
					w.playSound(impact, Sound.ANVIL_LAND, 0.9f, 1.1f);
					w.spigot().playEffect(impact, Effect.EXPLOSION_LARGE, 0, 0, 0, 0, 0, 0.03f, 1, 32);

					cancel();
				}
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 2L, 1L); // start after 2 ticks, then every tick
	}

	/**
	 * Simple ground check for 1.8-compatible servers. True if block at feet or just
	 * below is solid/liquid (not air).
	 */
	private boolean isOnGround(Location loc) {
		Location feet = loc.clone();
		Block b0 = feet.getBlock();
		Block b1 = feet.clone().subtract(0, 1, 0).getBlock();
		// Treat any non-air (solid or liquid) as ground contact
		return (b0.getType() != Material.AIR) || (b1.getType() != Material.AIR);
	}

	/**
	 * Draws a temporary particle ring on the ground to show the impact radius. Uses
	 * only 1.8-friendly Effects.
	 */
	private void drawDamageRing(final World w, final Location center, final double radius) {
		// Draw over ~10 ticks so it reads clearly to players
		new BukkitRunnable() {
			int life = 0;

			@Override
			public void run() {
				life++;
				// Step size -> more points = smoother ring
				for (double deg = 0; deg < 360; deg += 6) {
					double rad = Math.toRadians(deg);
					double x = center.getX() + Math.cos(rad) * radius;
					double z = center.getZ() + Math.sin(rad) * radius;
					Location pLoc = new Location(w, x, center.getY(), z);

					// Slight y offset so it doesn't z-fight with the ground
					Location show = pLoc.clone().add(0, 0.02, 0);

					// Use coloured dust & step sound to be visible on most blocks in 1.8
					w.spigot().playEffect(show, Effect.COLOURED_DUST, 0, 0, 0, 0, 0, 1.0f, 0, 16); // red dust puff
					w.spigot().playEffect(show, Effect.STEP_SOUND, Material.STONE.getId(), 0, 0, 0, 0, 1, 0, 8);
				}
				if (life >= 10)
					cancel(); // ~0.5s
			}
		}.runTaskTimer(instance.getGameManager().getMain(), 0L, 1L);
	}

	// ======================================
	// Ability 3: House Builder (Ultimate)
	// 3x3 hut around player + proper iron door
	// Regen II 5s, despawn after 5s
	// ======================================
	private void doHouseBuilderUltimate(final Player p) {
		// if (isOnCooldown(cdUlt, p, COOLDOWN_ULT)) return;

		final World w = p.getWorld();
		final Location feet = p.getLocation();
		final int cx = feet.getBlockX();
		final int cy = feet.getBlockY();
		final int cz = feet.getBlockZ();

		// Regen II (5s)
		p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, true, true));

		// Track placed blocks so we can cleanly remove them
		final Set<Block> placedSet = tempBlocks.computeIfAbsent(p.getUniqueId(), k -> new HashSet<>());
		final List<Block> hut = new ArrayList<>();

		// --- 3x3 walls (height 3), hollow interior ---
		// Perimeter coords are where |dx|==1 or |dz|==1 on a 3x3 footprint centered at
		// (cx,cz)
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (Math.abs(dx) < 1 && Math.abs(dz) < 1)
					continue; // skip center column
				for (int dy = 0; dy < 3; dy++) { // y..y+2 walls
					Block b = w.getBlockAt(cx + dx, cy + dy, cz + dz);
					if (b.getType() == Material.AIR) {
						b.setType(Material.WOOD, false);
						hut.add(b);
					}
				}
			}
		}

		// --- 3x3 roof at y+3 ---
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				Block roof = w.getBlockAt(cx + dx, cy + 3, cz + dz);
				if (roof.getType() == Material.AIR) {
					roof.setType(Material.WOOD, false);
					hut.add(roof);
				}
			}
		}

		// --- Door opening on the "front" face based on player yaw ---
		BlockFace face = yawToFace(feet.getYaw()); // NORTH/EAST/SOUTH/WEST
		int fx = 0, fz = 0;
		switch (face) {
		case NORTH:
			fz = -1;
			break;
		case SOUTH:
			fz = 1;
			break;
		case WEST:
			fx = -1;
			break;
		default:
			fx = 1;
			break; // EAST
		}

		// Door bottom/top blocks at front center of perimeter
		Block doorBottom = w.getBlockAt(cx + fx, cy, cz + fz);
		Block doorTop = w.getBlockAt(cx + fx, cy + 1, cz + fz);

		// Make sure supporting block under door exists
		Block support = w.getBlockAt(cx + fx, cy - 1, cz + fz);
		if (support.getType() == Material.AIR) {
			support.setType(Material.WOOD, false);
			hut.add(support);
		}

		// Clear the doorway space if the wall placed there
		if (doorBottom.getType() != Material.AIR && doorBottom.getType() != Material.IRON_DOOR_BLOCK) {
			doorBottom.setType(Material.AIR, false);
		}
		if (doorTop.getType() != Material.AIR && doorTop.getType() != Material.IRON_DOOR_BLOCK) {
			doorTop.setType(Material.AIR, false);
		}

		// Place iron door (bottom then top) with correct facing
		if (doorBottom.getType() == Material.AIR && doorTop.getType() == Material.AIR) {
			// Bottom
			doorBottom.setType(Material.IRON_DOOR_BLOCK, false);
			BlockState bsBottom = doorBottom.getState();
			org.bukkit.material.Door dataBottom = new org.bukkit.material.Door(Material.IRON_DOOR_BLOCK);
			dataBottom.setFacingDirection(face); // faces outward
			bsBottom.setData(dataBottom);
			bsBottom.update(true, false);

			// Top
			doorTop.setType(Material.IRON_DOOR_BLOCK, false);
			BlockState bsTop = doorTop.getState();
			org.bukkit.material.Door dataTop = new org.bukkit.material.Door(Material.IRON_DOOR_BLOCK);
			dataTop.setTopHalf(true);
			dataTop.setFacingDirection(face);
			bsTop.setData(dataTop);
			bsTop.update(true, false);

			hut.add(doorBottom);
			hut.add(doorTop);
		}

		placedSet.addAll(hut);

		// Feedback
		w.playSound(feet, Sound.ANVIL_USE, 0.8f, 1.1f);
		p.sendMessage(
				ChatColor.GREEN + "House built! " + ChatColor.YELLOW + "Regeneration II" + ChatColor.GRAY + " for 5s.");

		// Despawn everything after 5 seconds
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Block b : hut) {
					if (b.getType() == Material.WOOD || b.getType() == Material.IRON_DOOR_BLOCK) {
						b.setType(Material.AIR, false);
					}
				}
				placedSet.removeAll(hut);
				w.playSound(feet, Sound.ZOMBIE_WOODBREAK, 0.8f, 0.8f);
				p.sendMessage(ChatColor.DARK_GREEN + "Your house crumbled.");
			}
		}.runTaskLater(instance.getGameManager().getMain(), 100L); // 5s
	}

	// Utility: convert yaw to cardinal BlockFace (1.8-safe)
	private BlockFace yawToFace(float yaw) {
		float rot = (yaw % 360 + 360) % 360;
		if (rot >= 45 && rot < 135)
			return BlockFace.WEST;
		if (rot >= 135 && rot < 225)
			return BlockFace.NORTH;
		if (rot >= 225 && rot < 315)
			return BlockFace.EAST;
		return BlockFace.SOUTH;
	}

}
