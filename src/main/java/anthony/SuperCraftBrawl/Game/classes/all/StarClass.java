package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class StarClass extends BaseClass {

	private int count = 10;
	private boolean isSpawned = false;

	public StarClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTMzNTM1ZjA1OGY4ZDY1ZjMwNTU3YzdjNDY2NjkxMWNjYWMzZDI4NmI1ZTgzMDllMmIzNjM2Y2Y2Mjc1NmM3In19fQ==",
				"000000",
				6,
				"Star"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Star;
	}

	@Override
	public void Tick(int gameTicks) {
		if (isSpawned) {
			if (count > 0) {
				if (gameTicks % 20 == 0) {
					for (Entity en : player.getWorld().getEntities()) {
						if (en instanceof Pig) {
							List<Entity> near = player.getNearbyEntities(5.0, 5.0, 5.0);

							for (Entity e : near) {
								if (e instanceof Pig) {
									if (e.getName().contains(player.getName())) {
										if (player.getHealth() <= 18.0) {
											player.setHealth(player.getHealth() + 2.0);
											player.sendMessage(instance.getGameManager().getMain()
													.color("&e&l(!) &rJeffrey just gave you an extra 2 hearts!"));
											count--;
										} else if (player.getHealth() > 18.0 && player.getHealth() < 20.0) {
											double p = 20.0 - player.getHealth();
											NumberFormat fmt = new DecimalFormat("#.##");
											player.sendMessage(instance.getGameManager().getMain().color(
													"&e&l(!) &rJeffrey just gave you an extra " + fmt.format(p) + " hearts!"));
											count--;
										} else
											player.sendMessage(instance.getGameManager().getMain()
													.color("&c&l(!) &rYou are already full of health!"));
									}
								}
							}
						}
					}
				}
			} else {
				player.sendMessage(
						instance.getGameManager().getMain().color("&c&l(!) &rYour pet Jeffrey's heal power ran out :("));
				isSpawned = false; // To reset
			}
		}
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ItemStack getAttackWeapon() {
		return ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.NETHER_STAR), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		count = 10; // To reset each life
		isSpawned = false; // Also here too
		for (Entity e : player.getWorld().getEntities()) {
			if (e instanceof Pig && e.getName().contains(player.getName())) {
				e.remove();
			}
		}
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(ItemHelper.createMonsterEgg(EntityType.PIG, 1),
						instance.getGameManager().getMain().color("&e&lJeffrey Pokeball"), "",
						instance.getGameManager().getMain().color("&7Spawn your Jeffrey to heal you!"),
						instance.getGameManager().getMain().color("   &rMax 10 heal uses"),
						instance.getGameManager().getMain().color("   &rHeals 2 hearts per sec")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.MONSTER_EGG) {
				event.setCancelled(true);
				Entity pig = player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.PIG);
				pig.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.RESET + "Jeffrey");
				pig.setCustomNameVisible(true);
				player.sendMessage(instance.getGameManager().getMain().color(
						"&e&l(!) &rYou spawned in Jeffrey! You need to be within 5 blocks of him to regenerate"));
				isSpawned = true;
				player.getInventory().remove(Material.MONSTER_EGG);
			} else if (item.getType() == Material.NETHER_STAR
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);

			}
		}
	}

}
