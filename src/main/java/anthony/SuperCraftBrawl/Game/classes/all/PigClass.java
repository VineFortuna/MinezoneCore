package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PigClass extends BaseClass {

	private ItemStack pork = ItemHelper.addEnchant(
			ItemHelper.addEnchant(ItemHelper.setUnbreakable(new ItemStack(Material.PORK)), Enchantment.DAMAGE_ALL, 4),
			Enchantment.KNOCKBACK, 1);
	private ItemStack speedPork = ItemHelper.addEnchant(
			ItemHelper.addEnchant(ItemHelper.setUnbreakable(new ItemStack(Material.PORK)), Enchantment.DAMAGE_ALL, 3),
			Enchantment.KNOCKBACK, 1);
	private ItemStack firePork = ItemHelper.addEnchant(ItemHelper.addEnchant(
			ItemHelper.addEnchant(ItemHelper.setUnbreakable(new ItemStack(Material.GRILLED_PORK)), Enchantment.DAMAGE_ALL, 3),
			Enchantment.KNOCKBACK, 1), Enchantment.FIRE_ASPECT, 1);

	public PigClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.05;
		createArmor(null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjUwMzM3NzlmODc2MTFmOGU3MWM1YjAyYjkxYjQwYmNhNWMxYzk5YWZhNzUyYWJkNjM2YTQ5NWY5NTNiNjQ2In19fQ==",
				"FF9999", 6, "Pig");
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
	}

	@Override
	public void Tick(int gameTicks) {
		System.out.println("Re-applying speed");
	    if (!isPlayerAlive() && instance.classes.get(player).getType() != ClassType.Pig) return;

	    System.out.println("Re-applying speed 2!");
	    // Fire overrides speed
	    boolean onFire = player.getFireTicks() > 0; // 1.8-friendly "is burning" check
	    boolean hasSpeed = player.hasPotionEffect(PotionEffectType.SPEED);

	    ItemStack desired = onFire
	            ? this.firePork
	            : (hasSpeed ? this.speedPork : this.pork);

	    if (desired == null) return; // defensive

	    PlayerInventory inv = player.getInventory();
	    ItemStack current = inv.getItem(0);

	    boolean needsSwap =
	        current == null ||
	        !current.isSimilar(desired) ||              // same type/meta?
	        current.getAmount() != desired.getAmount(); // keep amount consistent

	    if (needsSwap) {
	        inv.setItem(0, desired.clone()); // avoid mutating your stored reference
	    }
	}

	@Override
	public void TakeDamage(EntityDamageEvent event) {
		if (checkSpeedPork(event)) {
			SoundManager.playSoundToAll(player, Sound.PIG_DEATH, 1, 1);
			giveSpeedPork();
		}
	}

	private boolean checkSpeedPork(EntityDamageEvent event) {
		if (!isPlayerAlive())
			return false;
		else if (instance.getGameManager().spawnProt.containsKey(player))
			return false;
		if (!(event.getEntity().equals(player)))
			return false;

		return true;
	}

	private void giveSpeedPork() {
	    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 2));

	    // Don't override firePork if the player is currently burning
	    if (player.getFireTicks() > 0) return;

	    PlayerInventory inv = player.getInventory();
	    ItemStack current = inv.getItem(0);

	    if (current == null || !current.isSimilar(this.speedPork) ||
	        current.getAmount() != this.speedPork.getAmount()) {
	        inv.setItem(0, this.speedPork.clone());
	    }
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

	@Override
	public ClassType getType() {
		return ClassType.Pig;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		return this.pork;
	}

}

/*
 * createArmor( null,
 * "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjUwMzM3NzlmODc2MTFmOGU3MWM1YjAyYjkxYjQwYmNhNWMxYzk5YWZhNzUyYWJkNjM2YTQ5NWY5NTNiNjQ2In19fQ==",
 * "FF9999", 6, "Pig" );
 */