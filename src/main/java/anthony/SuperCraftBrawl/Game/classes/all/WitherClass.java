package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class WitherClass extends BaseClass {

	private int count = 0;
	private BukkitRunnable witherBow;

	public WitherClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	@Override
	public ClassType getType() {
		return ClassType.Wither;
	}

	public ItemStack makeGray(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.BLACK);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODg2ZGMwY2ZjYWVlY2ZlMWFiNjkxNDZlNGQ0ZjExOTA4MzcwNzZhNjdkZWMxMzVmYWJkYTYyNzFmMzc1ZDAxZiJ9fX0=";
		ItemStack witherHelmet = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(witherHelmet);
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
		playerInv.setItem(0,
				ItemHelper.addEnchant(
						ItemHelper.addEnchant(new ItemStack(Material.NETHER_STAR), Enchantment.DAMAGE_ALL, 2),
						Enchantment.KNOCKBACK, 1));
		playerInv.setItem(1,
				ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.DURABILITY, 1000),
						Enchantment.ARROW_INFINITE, 1));
		playerInv.setItem(2, new ItemStack(Material.ARROW));
		count = 0;
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 95, 0, true));
		}
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntityType() == EntityType.ARROW) {
			event.setCancelled(true);

			if (witherBow == null) {
				WitherSkull skull = player.launchProjectile(WitherSkull.class);
				skull.setIsIncendiary(false);
				count++;
			}

			if (count == 10) {
				if (witherBow == null) {
					witherBow = new BukkitRunnable() {
						int ticks = 5;

						@Override
						public void run() {
							if (ticks <= 5 && ticks > 0) {
								String msg = instance.getGameManager().getMain()
										.color("&9&l(!) &eWither's Bow Cooldown: " + ticks + "s");
								getActionBarManager().setActionBar(player, "wither.cooldown", msg, 2);
							} else if (ticks == 0) {
								witherBow = null;
								this.cancel();
								String msg = instance.getGameManager().getMain()
										.color("&9&l(!) &eYou can now use Wither's Bow");
								getActionBarManager().setActionBar(player, "wither.cooldown", msg, 2);
								count = 0;
							}

							ticks--;
						}

					};
					witherBow.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
				}
			}
		}
	}
	
	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.NETHER_STAR), Enchantment.DAMAGE_ALL, 2),
				Enchantment.KNOCKBACK, 1);
		return item;
	}

}