package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BedrockClass extends BaseClass {

	// Invincibility
	private static final double INVINCIBILITY_DURATION = 3;
	private static final double INVINCIBILITY_COOLDOWN = 15;

	// Lava
	private static final double LAVA_COOLDOWN = 15;
	/** Short on purpose: a lava source first updates after 30 ticks, so it is gone before it can turn into anything else. */
	private static final long LAVA_DURATION_TICKS = 20;

	private final ItemStack weapon;
	private final ItemStack lavaItem;
	private final Ability invincibilityAbility = new Ability("&8&lInvincibility", INVINCIBILITY_COOLDOWN, player);
	private final Ability lavaAbility = new Ability("&6&lLava", LAVA_COOLDOWN, player);

	private ItemStack[] originalArmor;
	private BukkitRunnable invincibility;
	/** The air blocks that have lava in them until it is taken away again. */
	private final Set<Block> lavaBlocks = new HashSet<>();

	public BedrockClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZjY2ZlNTA5NmEzMzViOWFiNzhhYjRmNzc4YWU0OTlmNGNjYWI0ZTJjOTVmYTM0OTIyN2ZkMDYwNzU5YmFhZiJ9fX0=",
				"404040",
				6,
				"Bedrock"
		);

		// Weapon, which is also the Invincibility ability
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.BEDROCK),
				invincibilityAbility.getAbilityNameRightClickMessage(),
				"",
				"&7Become invincible to all damage",
				"",
				"&7You can not hit players or mobs",
				"",
				"&7Duration: &a" + ItemHelper.formatDouble(INVINCIBILITY_DURATION) + "&as"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		// Lava Ability
		lavaItem = ItemHelper.setDetails(
				new ItemStack(Material.LAVA_BUCKET),
				lavaAbility.getAbilityNameRightClickMessage(),
				"&7Place lava on the face of",
				"&7the block you click",
				"",
				"&7Duration: &a" + ItemHelper.formatDouble(LAVA_DURATION_TICKS / 20.0) + "&as"
		);
	}

	@Override
	public void setItems(Inventory playerInv) {
		bedrockInvincibility = false; // To reset each life
		invincibilityAbility.getCooldownInstance().reset();
		lavaAbility.getCooldownInstance().reset();

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, lavaItem);
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;

		// Both cooldowns in one action bar
		ActionBarManager.AbilityActionBar abilityActionBar = new ActionBarManager.AbilityActionBar(this, getActionBarManager());
		abilityActionBar.setActionBarAbility(player, invincibilityAbility, lavaAbility);
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null || player.getGameMode() == GameMode.SPECTATOR) return;
		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

		if (item.isSimilar(weapon)) {
			useInvincibilityAbility();
		} else if (item.isSimilar(lavaItem)) {
			event.setCancelled(true); // The bucket places nothing of its own
			useLavaAbility(event);
		}
	}

	// -----------------------------------------------------------------------
	//  Invincibility
	// -----------------------------------------------------------------------

	private void useInvincibilityAbility() {
		if (!invincibilityAbility.isReady() || invincibility != null) return;

		invincibilityAbility.use();
		String durationDisplay = ItemHelper.formatDouble(INVINCIBILITY_DURATION);
		invincibilityAbility.sendCustomMessage("&d&l(!) &rYou are now invincible for &e" + durationDisplay + " &rseconds");
		invincibilityAbility.sendCustomMessage("&d&l(!) &rYou are also unable to hit players or mobs");
		bedrockInvincibility = true;
		SoundManager.playSoundToAll(player, Sound.ZOMBIE_INFECT, 1, 1);

		invincibility = new BukkitRunnable() {
			int duration = (int) INVINCIBILITY_DURATION;
			boolean armorModified = false;

			@Override
			public void run() {
				if (duration == 0) {
					// Invincibility is over
					invincibility = null;
					this.cancel();
					bedrockInvincibility = false;
					if (armorModified) restoreOriginalArmor();
				} else if (duration == 1) {
					// A second before it is over
					SoundManager.playSoundToAll(player, Sound.ZOMBIE_UNFECT, 1, 1);
				} else if (!armorModified) {
					modifyArmorDuringInvincibility();
					armorModified = true;
				}

				duration--;
			}
		};
		instance.trackClassTask(invincibility.runTaskTimer(instance.getGameManager().getMain(), 0, 20));
	}

	private void modifyArmorDuringInvincibility() {
		originalArmor = player.getInventory().getArmorContents();

		player.getInventory().setChestplate(ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.WHITE, "&rBedrock Chestplate"));
		player.getInventory().setLeggings(ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.WHITE, "&rBedrock Leggings"));
		player.getInventory().setBoots(ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.WHITE, "&rBedrock Boots"));
	}

	private void restoreOriginalArmor() {
		player.getInventory().setArmorContents(originalArmor);
	}

	// -----------------------------------------------------------------------
	//  Lava
	// -----------------------------------------------------------------------

	private void useLavaAbility(PlayerInteractEvent event) {
		if (!lavaAbility.isReady()) return;
		if (instance.getGameManager().spawnProt.containsKey(player)) return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			lavaAbility.sendCustomMessage("&c&l(!) &rClick a block to place lava");
			return;
		}

		// Placed like a lava bucket: in the block next to the face that was clicked, and the clicked block stays
		Block target = event.getClickedBlock().getRelative(event.getBlockFace());
		if (target.getType() != Material.AIR) {
			lavaAbility.sendCustomMessage("&c&l(!) &rThere is no room to place lava there");
			return;
		}

		// Every lava is taken away by its own task, so one can never be left behind by another one. The task is made
		// before anything else can fail
		lavaBlocks.add(target);
		BukkitTask removeLater = Bukkit.getScheduler().runTaskLater(
				instance.getGameManager().getMain(), () -> removeLava(target), LAVA_DURATION_TICKS);
		instance.trackClassTask(removeLater);

		target.setType(Material.LAVA);
		lavaAbility.use();
		SoundManager.playSoundToAll(player, target.getLocation(), Sound.LAVA_POP, 1, 1);
	}

	/** Takes the lava away from that block, if it is still there. */
	private void removeLava(Block block) {
		if (!lavaBlocks.remove(block)) return;

		// By now it can have become lava that stands still, or obsidian or cobblestone if there was water next to it
		Material type = block.getType();
		if (type == Material.LAVA || type == Material.STATIONARY_LAVA
				|| type == Material.OBSIDIAN || type == Material.COBBLESTONE) {
			block.setType(Material.AIR);
		}
	}

	@Override
	public void GameEnd() {
		bedrockInvincibility = false;
		for (Block block : new ArrayList<>(lavaBlocks)) removeLava(block);
	}

	@Override
	public ClassType getType() {
		return ClassType.Bedrock;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
