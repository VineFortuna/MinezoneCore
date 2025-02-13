package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class ZombieClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack hordeItem;
	private final Ability hordeAbility = new Ability("&2&lHorde Call", player);
	private final PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 3, false, true);
	private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 5 * 20, 0, false, true);
	private final PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, 5 * 20, 0, false, true);

	public ZombieClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZmYzg1NGJiODRjZjRiNzY5NzI5Nzk3M2UwMmI3OWJjMTA2OTg0NjBiNTFhNjM5YzYwZTVlNDE3NzM0ZTExIn19fQ==",
				"00ADAD",
				"4A4191",
				"686868",
				6,
				"Zombie"
		);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.IRON_SPADE),
				"&2&lGrave Digger",
				"",
				"&7Inflict one of 3 effects on enemies:",
				"&7▶ &2&oPoison &e" + (poison.getAmplifier() + 1) + " &7for &e" + poison.getDuration() / 20 + "s",
				"&7▶ &3&oSlowness &e" + (slowness.getAmplifier() + 1) + " &7for &e" + slowness.getDuration() / 20 + "s",
				"&7▶ &f&oWeakness &e" + (weakness.getAmplifier() + 1) + " &7for &e" + weakness.getDuration() / 20 + "s"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		ItemHelper.setUnbreakable(weapon);

		// Horde Ability
		hordeItem = ItemHelper.setDetails(
				new ItemStack(Material.ROTTEN_FLESH),
				hordeAbility.getAbilityNameRightClickMessage(),
				"&7Summon a horde of 3 baby zombies"
		);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		for (Entity en : player.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(player.getName()))
					en.remove();

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, hordeItem);
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;

		Random rand = new Random();
		int chance = rand.nextInt(3);

		if (chance == 0) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (instance.duosMap != null)
					if (instance.team.get(p).equals(instance.team.get(player)))
						return;

				p.addPotionEffect(slowness);
			}
		} else if (chance == 1) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (instance.duosMap != null)
					if (instance.team.get(p).equals(instance.team.get(player)))
						return;

				p.addPotionEffect(poison);
			}
		} else if (chance == 2) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (instance.duosMap != null)
					if (instance.team.get(p).equals(instance.team.get(player)))
						return;

				p.addPotionEffect(weakness);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();

		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;

		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

		if (item.equals(hordeItem)) {
			event.setCancelled(true);
			useHordeAbility();
		}
	}

	private void useHordeAbility() {
		player.getInventory().remove(Material.ROTTEN_FLESH);
		for (int i = 0; i < 3; i++) {
			Zombie zombie = (Zombie) player.getWorld().spawnCreature(player.getLocation(), EntityType.ZOMBIE);
			zombie.setBaby(true);
			zombie.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &eBaby Zombie"));
			zombie.setTarget(instance.getNearestPlayer(player, zombie, 150));
		}
		player.playSound(player.getLocation(), Sound.ZOMBIE_HURT, 1, 1);
	}

	@Override
	public ClassType getType() {
		return ClassType.Zombie;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
