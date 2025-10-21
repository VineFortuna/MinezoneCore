package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import static sun.jvm.hotspot.oops.CellTypeState.ref;

public class NinjaClass extends BaseClass {

	private Cooldown shurikenCooldown = new Cooldown(200);
	private int dashCooldown = 12 * 1000;
	private int cooldownSec;
	private int regenStars = 0;
	private int starsCooldown = 0;
	private boolean usedAllStars = false;
	private ItemStack barrier = new ItemStack(Material.BARRIER);

	public NinjaClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODc1ZGZlZTI5ZTMxNjY4M2VhZWE3MGNlMzc2MzIyYWFhZGViNmVjY2I1ZTk5ZGVhMjY3MmY3NDQ1ZGQ4MWMzIn19fQ==",
				"424242",
				6,
				"Ninja"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	public ItemStack getShuriken() {
		return ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR, 1), ChatColor.GRAY + "Shuriken", "",
				ChatColor.YELLOW + "Right click to throw a deadly star!");
	}

    @Override
    public void DoDamage(EntityDamageByEntityEvent event) {
        ItemStack hand = player.getItemInHand();
        if (hand == null || hand.getType() != Material.GHAST_TEAR) return;

        final Entity target = event.getEntity();
        final Location eye = player.getEyeLocation();
        final Vector dir = eye.getDirection().normalize();
        final Vector start = eye.toVector();
        final Vector end   = start.clone().add(dir.clone().multiply(1.2));
        final Location tl = target.getLocation();
        final double halfWidth = 0.3;
        final double height    = 1.8;

        final Vector aabbMin = new Vector(tl.getX() - halfWidth, tl.getY(),          tl.getZ() - halfWidth);
        final Vector aabbMax = new Vector(tl.getX() + halfWidth, tl.getY() + height, tl.getZ() + halfWidth);

        if (!segmentIntersectsAABB(start, end, aabbMin, aabbMax)) {
            event.setCancelled(true);
            return;
        }

        player.getWorld().playSound(target.getLocation(), Sound.BAT_DEATH, 1f, 2f);
    }

    /**
     * Robust segment–AABB test (slab method) without refs.
     * Returns true if segment [p0,p1] intersects axis-aligned box [min,max].
     */
    private static boolean segmentIntersectsAABB(Vector p0, Vector p1, Vector min, Vector max) {
        final double EPS = 1e-9;
        double tMin = 0.0;
        double tMax = 1.0;
        Vector d = p1.clone().subtract(p0);

        if (Math.abs(d.getX()) < EPS) {
            if (p0.getX() < min.getX() || p0.getX() > max.getX()) return false;
        } else {
            double inv = 1.0 / d.getX();
            double t1 = (min.getX() - p0.getX()) * inv;
            double t2 = (max.getX() - p0.getX()) * inv;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (tMax < tMin) return false;
        }

        if (Math.abs(d.getY()) < EPS) {
            if (p0.getY() < min.getY() || p0.getY() > max.getY()) return false;
        } else {
            double inv = 1.0 / d.getY();
            double t1 = (min.getY() - p0.getY()) * inv;
            double t2 = (max.getY() - p0.getY()) * inv;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (tMax < tMin) return false;
        }

        if (Math.abs(d.getZ()) < EPS) {
            if (p0.getZ() < min.getZ() || p0.getZ() > max.getZ()) return false;
        } else {
            double inv = 1.0 / d.getZ();
            double t1 = (min.getZ() - p0.getZ()) * inv;
            double t2 = (max.getZ() - p0.getZ()) * inv;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (tMax < tMin) return false;
        }

        return tMax >= 0.0 && tMin <= 1.0 && tMax >= tMin;
    }

    @Override
	public void SetItems(Inventory playerInv) {
		ninja.startTime = System.currentTimeMillis() - 100000;
		this.regenStars = 0;
		this.starsCooldown = 0;
		this.usedAllStars = false;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv
				.setItem(1,
						ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.GHAST_TEAR), ChatColor.GRAY + "Wakizashi",
										"",
										"" + ChatColor.RESET + ChatColor.GRAY
												+ "Only deals damage within 1.2 blocks from enemies"),
								Enchantment.DAMAGE_ALL, 8));
		ItemStack shuriken = getShuriken();
		shuriken.setAmount(5);
		playerInv.setItem(2, shuriken);

	}

	private int getNumberOfShurikens() {
		int count = 0;
		for (ItemStack item : player.getInventory().getContents())
			if (item != null && item.getType() == Material.NETHER_STAR)
				count += item.getAmount();
		return count;
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		if (instance.state == GameState.ENDED) return;

		if (instance.classes.containsKey(player) && instance.classes.get(player).getLives() > 0) {
			if (gameTicks % 20 == 0 && !checkIfDead(player, instance)) {
				if (this.starsCooldown != 0) {
					this.starsCooldown--;
				} else {
					if (this.usedAllStars) {
						if (this.regenStars != 5) {
							player.getInventory().remove(this.barrier);
							player.getInventory().addItem(getShuriken());
							this.regenStars++;
						} else {
							this.usedAllStars = false;
							this.regenStars = 0;
                        }
					}
				}
				if (!this.usedAllStars &&
						(player.getInventory().getItem(2) == null ||
								player.getInventory().getItem(1) == null ||
								player.getInventory().getItem(1).getType() == Material.AIR)) {

					this.usedAllStars = true;
					player.getInventory().setItem(2, this.barrier);
					this.starsCooldown = 3;
				}
			}

			if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Ninja
					&& instance.classes.get(player).getLives() > 0) {
				this.cooldownSec = (dashCooldown - ninja.getTime()) / 1000 + 1;

				if (ninja.getTime() < dashCooldown) {
					String msg = instance.getGameManager().getMain()
							.color("&7Katana Dash &rregenerates in: &e" + this.cooldownSec + "s");
					getActionBarManager().setActionBar(player, "dash.cooldown", msg, 2);
				} else {
					String msg = instance.getGameManager().getMain().color("&rYou can use &7Katana Dash");
					getActionBarManager().setActionBar(player, "dash.cooldown", msg, 2);
				}
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.STICK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (ninja.getTime() < dashCooldown) {
				int seconds = (dashCooldown - ninja.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Your Katana boost is on cooldown for " + ChatColor.YELLOW + seconds + " more seconds ");
			} else {
				ninja.restart();
				double boosterStrength = 1.4;
				for (Player gamePlayer : instance.players)
					gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
				Vector vel = player.getLocation().getDirection().multiply(boosterStrength);
				player.setVelocity(vel);
			}
		} else if (item != null && item.getType() == Material.NETHER_STAR && item.isSimilar(getShuriken())
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
						|| event.getAction() == Action.LEFT_CLICK_AIR
						|| event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if (!checkIfDead(player, instance)) {
				if (shurikenCooldown.useAndResetCooldown()) {
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);
						ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								if (hit != null) {
									if (instance.duosMap != null)
										if (instance.team.get(hit).equals(instance.team.get(player)))
											return;

									player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
									hit.damage(2.0, player);
									for (Player gamePlayer : instance.players)
										gamePlayer.playSound(hit.getLocation(), Sound.EXPLODE, 2, 1);

								}
							}

						}, new ItemStack(Material.NETHER_STAR));
						instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(3.0D));
						player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.8f, 4);
					}
					event.setCancelled(true);
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Ninja;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.STICK), ChatColor.GRAY + "Katana", "",
								ChatColor.YELLOW + "Right click to boost the way you're looking!"),
						Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);
		return item;
	}

}
