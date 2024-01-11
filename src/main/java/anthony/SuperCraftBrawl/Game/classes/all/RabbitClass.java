package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

public class RabbitClass extends BaseClass {

	private ItemStack rabbitFoot = ItemHelper
			.addEnchant(
					ItemHelper.addEnchant(
							ItemHelper.setDetails(new ItemStack(Material.RABBIT_FOOT),
									"" + ChatColor.RESET + "Rabbit Foot", ChatColor.GRAY + "", ChatColor.YELLOW + ""),
							Enchantment.DAMAGE_ALL, 4),
					Enchantment.KNOCKBACK, 3);

	public RabbitClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	public ItemStack makeGray(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.GRAY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String skullOwner = "7174988f-8556-5f96-901c-c70760731a3a";
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWI4OTY5MmQxOGFkYjk2NDJiZTI2Y2UzNjA5NmNhNTcyMDYxMWEwYzU2Njg0YjgzY2RmMGJkYzRkOGRiYzAyZCJ9fX0=";
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		GameProfile profile = new GameProfile(UUID.fromString(skullOwner), null);
		profile.getProperties().put("textures", new Property("textures", texture));
		Field profileField = null;

		try {
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		skull.setItemMeta(meta);
		playerEquip.setHelmet(skull);
		playerEquip.setChestplate(makeGray(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGray(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGray(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 2));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
		if (!(player.getActivePotionEffects().contains(PotionEffectType.JUMP)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 2));
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		Random r = new Random();
		int chance = r.nextInt(100);
		int chance2 = r.nextInt(3);

		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;

		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (instance.duosMap != null)
				if (instance.team.get(p).equals(instance.team.get(player)))
					return;

			if (chance2 == 0)
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 65, 1, true));

			if (!(player.getInventory().contains(this.rabbitFoot))) {
				if (chance >= 0 && chance <= 40) {
					player.getInventory().remove(Material.RABBIT_FOOT);
					player.getInventory().addItem(this.rabbitFoot);
					Vector dir = player.getLocation().getDirection();
					dir.setY(1.0);
					p.setVelocity(dir);
					player.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &rWeapon upgraded!"));
					player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 3));
				}
			} else {
				player.getInventory().remove(Material.RABBIT_FOOT);
				player.getInventory().addItem(this.getAttackWeapon());
				player.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &rWeapon downgraded :("));
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 2));
			}
		}
	}

	private void abilityMsg() {
		player.sendMessage("");
		player.sendMessage(instance.getGameManager().getMain().color(
				"&e&lCLASS TIP> &rRandom chance when hitting other players to shoot them up & back, and have Knockback III applied on your weapon for 1 hit"));
		player.sendMessage("");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.RABBIT_FOOT
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				this.abilityMsg();
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Rabbit;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(
						ItemHelper
								.addEnchant(
										ItemHelper.setDetails(new ItemStack(Material.RABBIT_FOOT),
												instance.getGameManager().getMain()
														.color("&rRabbit's Foot &7(Right Click)")),
										Enchantment.DAMAGE_ALL, 3),
						Enchantment.KNOCKBACK, 1);
		return item;
	}
}
