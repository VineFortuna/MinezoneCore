package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class MagmaCubeClass extends BaseClass {

	private int cooldownSec = 0;

	public MagmaCubeClass(GameInstance instance, Player player) {
		super(instance, player);
		this.baseVerticalJump = 1.3;
	}

	@Override
	public ClassType getType() {
		return ClassType.MagmaCube;
	}

	public ItemStack makeBlack(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.BLACK);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texure = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFhNmM0NWIyY2Y3OTc1Yjk1ZmJjY2U0ZWQ5YjA2NDZhYzAwY2I5Y2M5ZjY2ZGM1YzI0ZTgxZDJjOTFlZTdjMSJ9fX0=";
		ItemStack playerskull = ItemHelper.createSkullTexture(texure, "");

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeBlack(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeBlack(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlack(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD),
				Enchantment.KNOCKBACK, 1), Enchantment.DURABILITY, 999999);
		return item;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.MagmaCube
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (5000 - magmaCube.getTime()) / 1000 + 1;

			if (magmaCube.getTime() < 5000) {
				String msg = instance.getGameManager().getMain()
						.color("&e&lMagmaCube Pokeball &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "magmacube.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &e&lMagmaCube Pokeball");
				getActionBarManager().setActionBar(player, "magmacube.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		magmaCube.startTime = System.currentTimeMillis() - 100000;
		this.cooldownSec = 0; // Reset each life
		for (Entity en : player.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(player.getName()))
					en.remove();
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.createMonsterEgg(EntityType.MAGMA_CUBE, 7,
		instance.getGameManager().getMain().color("&e&lMagmaCube Pokeball")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.MONSTER_EGG
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				ItemMeta meta = item.getItemMeta();

				if (meta != null && meta.getDisplayName().contains("MagmaCube")) {
					if (magmaCube.getTime() < 5000) {
						int seconds = (5000 - magmaCube.getTime()) / 1000 + 1;
						event.setCancelled(true);
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Brooo wait "
								+ ChatColor.YELLOW + seconds + ChatColor.RESET + " more seconds dawg");
					} else {
						magmaCube.restart();
						int amount = item.getAmount();

						if (amount > 0) {
							if (amount == 1)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else {
								amount--;
								item.setAmount(amount);
							}
							ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
								@Override
								public void onHit(Player hit) {
									Location hitLoc = this.getBaseProj().getEntity().getLocation();
									player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
									MagmaCube en = (MagmaCube) player.getWorld().spawnCreature(hitLoc,
											EntityType.MAGMA_CUBE);
									en.setSize(4);
									en.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW
											+ "MagmaCube");
								}

							}, ItemHelper.createMonsterEgg(EntityType.MAGMA_CUBE, 1));
							instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
									player.getLocation().getDirection().multiply(2.0D));
						}
					}
				}
			}
		}
	}

}
