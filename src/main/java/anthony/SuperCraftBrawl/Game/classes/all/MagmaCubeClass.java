package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MagmaCubeClass extends BaseClass {

	private static final int STARTING_POKEBALLS = 7;
	private static final int POKEBALLS_PER_KILL = 2;
	private static final double POKEBALL_SPEED = 2.0;
	/** The size of the spawned magma cube: 1 is small, 2 is medium and 4 is big, so 3 is in between the last two. */
	private static final int MAGMA_CUBE_SIZE = 3;
	/** A spawn can fail, for example when it is cancelled, so it is tried again this many times. */
	private static final int SPAWN_ATTEMPTS = 50;

	private final ItemStack weapon;
	private final Ability pokeballAbility = new Ability("&4&lPokeball", 5, player);

	public MagmaCubeClass(GameInstance instance, Player player) {
		super(instance, player);
		this.baseVerticalJump = 1.2;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFhNmM0NWIyY2Y3OTc1Yjk1ZmJjY2U0ZWQ5YjA2NDZhYzAwY2I5Y2M5ZjY2ZGM1YzI0ZTgxZDJjOTFlZTdjMSJ9fX0=",
				"260000",
				6,
				"MagmaCube"
		);

		weapon = ItemHelper.setDetails(new ItemStack(Material.WOOD_SWORD), "&4&lSword");
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		ItemHelper.setUnbreakable(weapon);
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		pokeballAbility.updateActionBar(player, this);
	}

	@Override
	public void setItems(Inventory playerInv) {
		pokeballAbility.getCooldownInstance().reset(); // Each life
		for (Entity en : player.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(player.getName()))
					en.remove();
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, getPokeballs(STARTING_POKEBALLS));
	}

	private ItemStack getPokeballs(int amount) {
		return ItemHelper.createMonsterEgg(EntityType.MAGMA_CUBE, amount, pokeballAbility.getAbilityNameRightClickMessage());
	}

	private ItemStack getBarrier() {
		return ItemHelper.setDetails(new ItemStack(Material.BARRIER),
				"&c&lOut of Pokeballs!",
				"",
				"&7Each kill regenerates " + POKEBALLS_PER_KILL + " pokeballs");
	}

	@Override
	public void killEvent(Player damagerPlayer) {
		if (!instance.classes.containsKey(damagerPlayer) || checkIfDead(player, instance)) return;

		damagerPlayer.sendMessage(ChatColorHelper.color("&2&l(!) &rYou got a kill and gained &e"
				+ POKEBALLS_PER_KILL + " &rextra &4&lPokeballs"));

		// Replace the barrier if there is one, or add to the pokeballs left
		ItemStack current = damagerPlayer.getInventory().getItem(1);
		if (current == null || current.getType() != Material.MONSTER_EGG)
			damagerPlayer.getInventory().setItem(1, getPokeballs(POKEBALLS_PER_KILL));
		else
			damagerPlayer.getInventory().addItem(getPokeballs(POKEBALLS_PER_KILL));
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null || item.getType() != Material.MONSTER_EGG) return;
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

		if (!item.isSimilar(getPokeballs(1))) return;

		if (!pokeballAbility.isReady()) {
			event.setCancelled(true);
			pokeballAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}
		pokeballAbility.use();

		if (item.getAmount() == 1) player.getInventory().setItem(1, getBarrier());
		else item.setAmount(item.getAmount() - 1);

		ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				SoundManager.playSuccessfulHit(player);
				spawnMagmaCube(this.getBaseProj().getEntity().getLocation());
			}
		}, ItemHelper.createMonsterEgg(EntityType.MAGMA_CUBE, 1));

		instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(POKEBALL_SPEED));
	}

	@SuppressWarnings("deprecation")
	private void spawnMagmaCube(Location location) {
		MagmaCube magmaCube = (MagmaCube) player.getWorld().spawnCreature(location, EntityType.MAGMA_CUBE);
		for (int attempt = 1; attempt < SPAWN_ATTEMPTS && (magmaCube.isDead() || !magmaCube.isValid()); attempt++)
			magmaCube = (MagmaCube) player.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE);

		magmaCube.setSize(MAGMA_CUBE_SIZE);
		magmaCube.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eMagmaCube"));
		magmaCube.setCustomNameVisible(true);
	}

	@Override
	public ClassType getType() {
		return ClassType.MagmaCube;
	}
}
