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
		String skullOwner = "273485a7-6d51-5be3-8d1d-8c71bb9d01e4";
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTMyMDI2OGIyYjJiODlhOGY5ZDg0MDI2MTY5YWQyNWQ0ZTkzZjczODY5NmFjZWM1Y2E1ODcyNjU3ZTdlNjYifX19";
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
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
		CraftPlayer craft = (CraftPlayer) player;
		craft.getHandle().playerConnection.sendPacket(packet);
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
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) player;
						craft.getHandle().playerConnection.sendPacket(packet);
						r = null;
						this.cancel();
					} else {
						String msg = instance.getGameManager().getMain()
								.color("&9&l(!) &eVampire's Bow Cooldown: " + ticks + "s");
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) player;
						craft.getHandle().playerConnection.sendPacket(packet);
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
