package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SquidClass extends BaseClass {
	private final Cooldown boosterCooldown = new Cooldown(10000);
	private final Cooldown shurikenCooldown = new Cooldown(10000);
	private long inkCooldown;

	public SquidClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	public ItemStack makePurple(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.PURPLE);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("Squid");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makePurple(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makePurple(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makePurple(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.COAL),
				instance.getManager().getMain().color("&rInk &7(Right Click)")));
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		/*
		 * BaseClass bc = instance.classes.get(player); if (bc != null && bc.getLives()
		 * <= 0) return;
		 * 
		 * Random rand = new Random(); int chance = rand.nextInt(9);
		 * 
		 * if (chance == 5 || chance == 1 || chance == 3 || chance == 7) { if
		 * (event.getEntity() instanceof Player) { Player p = (Player)
		 * event.getEntity(); if (instance.duosMap != null) if
		 * (instance.team.get(p).equals(instance.team.get(player))) return;
		 * 
		 * p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 90, 0, true));
		 * } }
		 */
	}

	private void abilityMsg() {
		/*
		 * player.sendMessage(""); player.sendMessage(instance.getManager().getMain()
		 * .color("&e&lCLASS TIP> &rCertain chance to give Blindness I to other players by hitting them!"
		 * )); player.sendMessage("");
		 */
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
					int seconds = (int) ((inkCooldown - System.currentTimeMillis()) / 1000);
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
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.INK_SACK),
						instance.getManager().getMain().color("&rInk Sack &7(Right Click)")),
				Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 2);
		return item;
	}
}
