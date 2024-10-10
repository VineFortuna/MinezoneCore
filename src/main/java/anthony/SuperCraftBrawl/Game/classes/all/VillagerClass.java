package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import anthony.SuperCraftBrawl.gui.VillagerAbilityGUI;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VillagerClass extends BaseClass {

	private ItemStack weapon;
	private int emeraldsCount;


	public VillagerClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcxYjhiMmFlN2ZiMjc4MmRiZWU5M2E3ZTY3OTc4M2M1MGQ1YTg4NDA0NTcwOGEyMTU5NDE3ODVkN2MzY2NkIn19fQ",
				"6E504B",
				"6E504B",
				"828282",
				6,
				"Villager"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Villager;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (instance.duosMap != null)
				if (instance.team.get(p).equals(instance.team.get(player)))
					return;

			if (instance.getGameManager().spawnProt.containsKey(p)
					|| instance.getGameManager().spawnProt.containsKey(player))
				return;

			BaseClass bc = instance.classes.get(player);
			if (bc != null && bc.getLives() <= 0)
				return;

			emeraldsCount++;
			weapon.setAmount(emeraldsCount);
			player.getInventory().setItem(0, weapon);
		}
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		// Resetting Emeralds on Death
		emeraldsCount = 0;

		// Weapon
		ItemStack weapon = ItemHelper.setDetails(new ItemStack(Material.EMERALD),
				"&aTrade Ability",
				"&fHit enemies to gain emeralds",
				"&fTrade emeralds into items");
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		this.weapon = weapon;

		// Settings Items
		playerInv.setItem(0, weapon);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item != null) {
			// TRADE ABILITY
			if (item.equals(weapon)) {
				// Check Right CLick
				if (action == Action.RIGHT_CLICK_AIR ||
						action == Action.RIGHT_CLICK_BLOCK) {
					new VillagerAbilityGUI(
							instance.getGameManager().getMain(),
							instance,
							this
					).inv.open(player);
				}
			}
		}
	}

	public int getEmeraldsCount() {
		return emeraldsCount;
	}
	public void setEmeraldsCount(int emeraldsCount) {
		this.emeraldsCount = emeraldsCount;
	}
}