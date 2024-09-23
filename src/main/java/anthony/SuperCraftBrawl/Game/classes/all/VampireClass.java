package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.UUID;

public class VampireClass extends BaseClass {

	private boolean hitPlayer = false;
	private boolean launched = false;
	private BukkitRunnable r;

	public VampireClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
	}

	@Override
	public ClassType getType() {
		return ClassType.Vampire;
	}

	public ItemStack makeGray(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.GRAY);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTI2YTk4ZDQwMzhlYWJhNDdlMDJlZWUxNTUxZGE5OTJhYTVhZDQ2NzA1YTc4MWY0NjE0NzA0MmQyOWNhZjEwNCJ9fX0=";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeGray(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGray(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGray(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.launched = false;
		this.hitPlayer = false;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.DURABILITY, 1000),
						Enchantment.ARROW_INFINITE, 1));
		playerInv.setItem(2, new ItemStack(Material.ARROW));
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Arrow) {
			Arrow a = (Arrow) event.getDamager();
			if (a.getShooter() instanceof Player) {
				if (event.getEntity() instanceof Player) {
					Player p = (Player) event.getEntity();
					if (instance.duosMap != null)
						if (instance.team.get(p).equals(instance.team.get(player)))
							return;

					p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1));
					this.hitPlayer = true;
				}
			}
		}
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (r != null)
				event.setCancelled(true);

			this.launched = true;
			this.hitPlayer = false;
			cooldown();
		}
	}

	private void restart() {
		this.launched = false;
		this.hitPlayer = false;
		String msg = instance.getGameManager().getMain().color("&9&l(!) &rYou can now use &eVampire's Bow");
		getActionBarManager().setActionBar(player, "vampire.cooldown", msg, 2);
	}

	private void cooldown() {
		if (r == null) {
			r = new BukkitRunnable() {
				int ticks = 7;

				@Override
				public void run() {
					if (hitPlayer == true) {
						restart();
						player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
						r = null;
						this.cancel();
					}

					if (ticks == 0) {
						restart();
						String msg = instance.getGameManager().getMain().color("&9&l(!) &rYou can now use &eVampire's Bow");
						getActionBarManager().setActionBar(player, "vampire.cooldown", msg, 2);
						r = null;
						this.cancel();
					} else {
						String msg = instance.getGameManager().getMain()
								.color("&9&l(!) &eVampire's Bow Cooldown: " + ticks + "s");
						getActionBarManager().setActionBar(player, "vampire.cooldown", msg, 2);
					}

					ticks--;
				}
			};
			r.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
		}
	}

	private void abilityMsg() {
		player.sendMessage("");
		player.sendMessage(instance.getGameManager().getMain()
				.color("&e&lCLASS TIP> &rShoot players with your bow to infect them with Poison II for 5 seconds"));
		player.sendMessage("");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.GHAST_TEAR
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			this.abilityMsg();
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.GHAST_TEAR),
						instance.getGameManager().getMain().color("Ghast Tear &7(Right Click)")),
				Enchantment.DAMAGE_ALL, 2), Enchantment.KNOCKBACK, 1);
		return item;
	}

}
