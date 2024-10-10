package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class GuardianClass extends BaseClass {

	public GuardianClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	@Override
	public ClassType getType() {
		return null;
	}

	public ItemStack makeGray(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.GRAY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTU2YjM3NzgzODYxZTZhNDRkNTM3ZTMyZDg2NTUzNzMxYWU0MzM3OTExNWRiMzRjMjY2ZTUyZmEzY2FiIn19fQ==";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeGray(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGray(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGray(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.PRISMARINE_SHARD), Enchantment.DAMAGE_ALL, 4),
				Enchantment.KNOCKBACK, 2);
		return item;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.TNT), "Defense"));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK
				|| event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			ItemStack item = player.getInventory().getItemInHand();
			if (item != null && item.getType() == Material.TNT) {
				event.setCancelled(true);
				TNTPrimed tnt = (TNTPrimed) event.getPlayer().getWorld().spawnEntity(
						event.getPlayer().getEyeLocation().add(event.getPlayer().getLocation().getDirection()),
						EntityType.PRIMED_TNT);
				tnt.setVelocity(player.getLocation().getDirection().multiply(1.0));
				tnt.setFuseTicks(40);
				tnt.setMetadata("thrower",
						new FixedMetadataValue(instance.getGameManager().getMain(), player.getUniqueId().toString()));

				Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
					if (!tnt.isDead()) {
						Location tntLoc = tnt.getLocation();
						for (Player p : world.getPlayers()) {
							if (p != player && p.getLocation().distance(tntLoc) < 2) {
								tnt.teleport(p.getLocation());
								tnt.setFuseTicks(5);
								return;
							}
						}
					}
				}, 1);
			}
		}
	}
}
