package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

public class IrongolemClass extends BaseClass {

	private int cooldownSec;

	public IrongolemClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	public ItemStack makeGray(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.GRAY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ2NTJjOTVmYzViZGY3ZWQwM2M1NjdlOTBmZjYyNWJlMDI4YWQ4NDg2M2QzMjcxZDZlNmMxYWEzMDhmMzEzZiJ9fX0=";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeGray(new ItemStack(Material.LEATHER_CHESTPLATE)));
		playerEquip.setLeggings(makeGray(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGray(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.RED_ROSE, 1),
						"" + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "Rose of Elevation", "",
						instance.getGameManager().getMain().color("&7Throw at enemies to levitate & do damage!")));
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.IronGolem
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (5000 - golem.getTime()) / 1000 + 1;

			if (golem.getTime() < 5000) {
				String msg = instance.getGameManager().getMain()
						.color("&c&lRose of Elevation &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "rose.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &c&lRose of Elevation");
				getActionBarManager().setActionBar(player, "rose.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.RED_ROSE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (golem.getTime() < 5000) {
				int seconds = (5000 - golem.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Your Rose of Elevation is still regenerating for " + ChatColor.YELLOW + seconds
						+ " more seconds ");
			} else {
				golem.restart();
				Vector direction = player.getLocation().getDirection();
				ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
					@Override
					public void onHit(Player hit) {
						if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
							if (instance.duosMap != null)
								if (instance.team.get(hit).equals(instance.team.get(player)))
									return;

							if (hit != null) {
								@SuppressWarnings("deprecation")
								EntityDamageEvent damageEvent = new EntityDamageEvent(hit, DamageCause.VOID, 5.5);
								instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
								hit.damage(5.5, player);
								Location loc = hit.getLocation();
								Vector v = direction;
								v.setY(1.0);
								hit.setVelocity(v);
								for (Player gamePlayer : instance.players)
									gamePlayer.playSound(loc, Sound.EXPLODE, 1, 1);
							}
						}
					}

				}, new ItemStack(Material.RED_ROSE));
				instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
						player.getLocation().getDirection().multiply(2.5D));
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.IronGolem;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack axe = ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.IRON_AXE),
				"" + ChatColor.RED + ChatColor.BOLD + "Power Axe"), Enchantment.KNOCKBACK, 2);
		ItemMeta meta = axe.getItemMeta();
		meta.spigot().setUnbreakable(true);
		axe.setItemMeta(meta);
		return axe;
	}

}
