package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SquidClass extends BaseClass {
	private int cooldownSec = 0;
	private long inkCooldown;

	public SquidClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDA4YTljODYzNDkyMTVjYjk0NjM2YWFmYzViYzY2NDRlODI5YTI4MzczYzU0NWZmZGNhOWZlZWQ1OTRiZjNhIn19fQ==",
				"516575",
				6,
				"Squid"
		);
		playerHead.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.COAL),
				instance.getGameManager().getMain().color("&rInk &7(Right Click)")));
	}
	
	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Squid
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (int) ((inkCooldown - System.currentTimeMillis()) / 1000) + 1;

			if (inkCooldown > System.currentTimeMillis()) {
				String msg = instance.getGameManager().getMain()
						.color("&rInk regenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "squid.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use Ink");
				getActionBarManager().setActionBar(player, "squid.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (item == null)
				return;
			switch (item.getType()) {
			case INK_SACK:
				event.setCancelled(true);
				break;
			case COAL:
				if (inkCooldown > System.currentTimeMillis()) {
					int seconds = (int) ((inkCooldown - System.currentTimeMillis()) / 1000) + 1;
					player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your Ink is still on cooldown for "
							+ ChatColor.YELLOW + seconds + " more seconds ");
					return;
				}
				inkCooldown = System.currentTimeMillis() + (50L * 200);
				player.getWorld().playEffect(player.getLocation(), Effect.SPLASH, 20);
				player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 1f, 1f);
				for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 10D, 10D, 10D)) {
					if (e instanceof Player && !e.equals(player)) {
						Player p = (Player) e;
						if (p.getGameMode() != GameMode.SPECTATOR) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 75, 0));
							Location playerLocation = p.getEyeLocation();
							double radius = 2.0;
							int particleCount = 20;
							
							for (int i = 0; i < particleCount; i++) {
								double angle = 2 * Math.PI * i / particleCount;
								double x = radius * Math.cos(angle);
								double z = radius * Math.sin(angle);
								
								Location particleLoc = playerLocation.clone().add(x, 0, z);
								
								p.getWorld().spigot().playEffect(particleLoc, Effect.SMOKE, 0, 0, 0, 0, 0, 0, 1, 30);
								player.getWorld().spigot().playEffect(particleLoc, Effect.SMOKE, 0, 0, 0, 0, 0, 0, 1, 30);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Squid;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.INK_SACK), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
		return item;
	}
}
