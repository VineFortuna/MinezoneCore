package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ZombiePigmanClass extends BaseClass {

	private static final int STARTING_POKEBALLS = 5;
	private static final int POKEBALLS_PER_KILL = 1;
	private static final double POKEBALL_SPEED = 2.0;
	private static final int POKEBALL_SLOT = 1;

	private final ItemStack weapon;
	private final Ability pokeballAbility = new Ability("&a&lPokeball", player);

	public ZombiePigmanClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY5OGEzY2ZkZjhjMTNlZTY2MzQxNDBmOTQ1YjcxZDJlNDg4ZmY0ODVlMTBjMzNhZTI1ODIxZDgyZDg0OGE3MyJ9fX0=",
				"FF9999",
				"6E4021",
				"FF9999",
				6,
				"ZombiePigman"
		);

		weapon = ItemHelper.setDetails(new ItemStack(Material.GOLD_SWORD), "&e&lSword");
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
		ItemHelper.setUnbreakable(weapon);
	}

	@Override
	public ClassType getType() {
		return ClassType.ZombiePigman;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public void setItems(Inventory playerInv) {
		for (Entity e : player.getWorld().getEntities())
			if (e instanceof PigZombie)
				if (e.getName().contains(player.getName()))
					e.remove();

		playerInv.setItem(0, weapon);
		playerInv.setItem(POKEBALL_SLOT, getPokeballs(STARTING_POKEBALLS));
	}

	private ItemStack getPokeballs(int amount) {
		return ItemHelper.setDetails(
				ItemHelper.createMonsterEgg(EntityType.PIG_ZOMBIE, amount),
				pokeballAbility.getAbilityNameRightClickMessage(),
				"&7Throw it to summon a ZombiePigman where it",
				"&7lands, that attacks the nearest enemy",
				"",
				ItemHelper.gainOnKillLine(POKEBALLS_PER_KILL, "pokeball", "pokeballs")
		);
	}

	private ItemStack getBarrier() {
		return ItemHelper.setDetails(new ItemStack(Material.BARRIER),
				"&c&lOut of Pokeballs!",
				"",
				"&7Each kill regenerates " + POKEBALLS_PER_KILL + " pokeball");
	}

	@Override
	public void killEvent(Player damagerPlayer) {
		if (!instance.classes.containsKey(damagerPlayer) || checkIfDead(player, instance)) return;

		damagerPlayer.sendMessage(ChatColorHelper.color("&2&l(!) &rYou got a kill and gained &e"
				+ POKEBALLS_PER_KILL + " &rextra " + pokeballAbility.getAbilityName()));

		// Replace the barrier if there is one, or add to the pokeballs left
		ItemStack current = damagerPlayer.getInventory().getItem(POKEBALL_SLOT);
		if (current == null || current.getType() != Material.MONSTER_EGG)
			damagerPlayer.getInventory().setItem(POKEBALL_SLOT, getPokeballs(POKEBALLS_PER_KILL));
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

		if (item.getAmount() == 1) player.getInventory().setItem(POKEBALL_SLOT, getBarrier());
		else item.setAmount(item.getAmount() - 1);

		ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				Location hitLoc = this.getBaseProj().getEntity().getLocation();
				player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
				spawnPigman(hitLoc);
			}
		}, ItemHelper.createMonsterEgg(EntityType.PIG_ZOMBIE, 1));

		instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(POKEBALL_SPEED));
	}

	@SuppressWarnings("deprecation")
	private void spawnPigman(Location location) {
		PigZombie pigman = (PigZombie) player.getWorld().spawnCreature(location, EntityType.PIG_ZOMBIE);
		pigman.getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));
		pigman.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eZombiePigman"));
		pigman.setAngry(true);
		pigman.setTarget(instance.getNearestPlayer(player, pigman, 150));
		player.playSound(player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 1, 1);
	}
}
