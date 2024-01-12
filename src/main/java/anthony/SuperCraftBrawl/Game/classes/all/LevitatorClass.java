package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

public class LevitatorClass extends BaseClass {

	private int cooldown = 0;
	private Vector dir;

	public LevitatorClass(GameInstance instance, Player player) {
		super(instance, player);
		this.baseVerticalJump = 1.2;
	}

	@Override
	public ClassType getType() {
		return ClassType.Levitator;
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

		meta.setOwner("_Shulker_");
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
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.STAINED_CLAY, 1, (byte) DyeColor.PURPLE.getData()),
						Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 1);
		return item;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Levitator
				&& instance.classes.get(player).getLives() > 0) {
			if (this.cooldown > 0) {
				String msg = instance.getGameManager().getMain()
						.color("&9&lLevitator Bow &rArrow regenerates in: &e" + this.cooldown + "s");
				getActionBarManager().setActionBar(player, "levitator.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &9&lLevitator Bow");
				getActionBarManager().setActionBar(player, "levitator.cooldown", msg, 2);
			}
		}

		if (gameTicks % 20 == 0)
			if (cooldown != 0)
				cooldown--;
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.cooldown = 0; // Reset each life
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv
				.setItem(1,
						ItemHelper.addEnchant(ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.BOW),
										"" + ChatColor.DARK_PURPLE + "Levitator Bow", "",
										instance.getGameManager().getMain()
												.color("&7Levitate your opponents by shooting them!")),
								Enchantment.ARROW_INFINITE, 1), Enchantment.DURABILITY, 1000));
		playerInv.setItem(2, new ItemStack(Material.ARROW));
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		Entity e = event.getEntity();

		if (e instanceof Arrow) {
			if (this.cooldown == 0) {
				this.cooldown = 7;
				Arrow a = (Arrow) e;
				Player p = (Player) a.getShooter();
				this.dir = p.getLocation().getDirection();
			} else if (this.cooldown > 0) {
				event.setCancelled(true);
				player.sendMessage(instance.getGameManager().getMain().color(
						"&c&l(!) &rYour &eLevitator Bow &ris still on cooldown for &e" + this.cooldown + " seconds"));
			}
		}
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (event.getDamager() instanceof Arrow) {
				event.setCancelled(true);
				Location loc = p.getLocation();
				Vector v = this.dir;
				v.setY(1.5);
				p.setVelocity(v);
				for (Player gamePlayer : instance.players)
					gamePlayer.playSound(loc, Sound.EXPLODE, 1, 1);

				Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
					for (Player gamePlayer : instance.players) {
						gamePlayer.playEffect(p.getLocation(), Effect.EXPLOSION_HUGE, 1);
						gamePlayer.playSound(loc, Sound.EXPLODE, 3, 1);
					}

					EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.VOID, 10.0);
					instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
					p.damage(10.0, player);
				}, 20);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

}
