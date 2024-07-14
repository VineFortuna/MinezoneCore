package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PigClass extends BaseClass {

	private ItemStack pork = ItemHelper.addEnchant(
			ItemHelper.addEnchant(new ItemStack(Material.PORK), Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);
	private ItemStack grilledPork = ItemHelper.addEnchant(ItemHelper.addEnchant(
			ItemHelper.addEnchant(new ItemStack(Material.GRILLED_PORK), Enchantment.DAMAGE_ALL, 1),
			Enchantment.FIRE_ASPECT, 1), Enchantment.KNOCKBACK, 1);
	private boolean speed = false;
	private boolean fire = false;

	public PigClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	public ItemStack makePink(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.fromRGB(255, 105, 180));
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNhNTM4Zjc4NzA0OGRiYTI3ZGNkYmJjYjcyZDJmNTc4Zjg1NzczMTY4ZDcyNDY2MjY2ZTc1NWY0NzFjODkifX19";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makePink(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makePink(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makePink(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void Tick(int gameTicks) {
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			if (!(player.getFireTicks() > 0)) {
				if (player.getInventory().contains(this.grilledPork)) {
					player.getInventory().remove(Material.GRILLED_PORK);
					player.getInventory().addItem(this.pork);
					this.speed = true;
					this.fire = false;
				}
				if (!(player.getInventory().contains(this.pork))) {
					player.getInventory().remove(Material.PORK);
					player.getInventory().addItem(this.pork);
					this.speed = true;
					this.fire = false;
				}
			} else {
				if (!(player.getInventory().contains(this.grilledPork))) {
					player.getInventory().remove(Material.PORK);
					player.getInventory().addItem(this.grilledPork);
					this.fire = true;
					this.speed = false;
				}
			}
		} else if (this.speed == true && !(player.getFireTicks() > 0)) {
			if (player.getInventory().contains(this.pork)) {
				player.getInventory().remove(Material.PORK);
				player.getInventory().addItem(this.getAttackWeapon());
				this.speed = false;
			}
		} else if (this.fire == true) {
			if (player.getInventory().contains(this.grilledPork)) {
				player.getInventory().remove(Material.GRILLED_PORK);
				player.getInventory().addItem(this.getAttackWeapon());
				this.fire = false;
			}

		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.speed = false; // To reset
		this.fire = false;
		playerInv.setItem(0, this.getAttackWeapon());
	}

	@Override
	public void TakeDamage(EntityDamageEvent event) {
		if (instance.getGameManager().spawnProt.containsKey(player))
			return;

		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if (e.getDamager() instanceof Player) {
				Player k = (Player) e.getDamager();
				if (instance.getGameManager().spawnProt.containsKey(k))
					return;
				if (instance.classes.containsKey(k)) {
					if (instance.classes.get(k).getLives() <= 0) {
						return;
					}
				}
				if (instance.HasSpectator(k))
					return;
			}
			if (((LivingEntity) event.getEntity()).hasPotionEffect(PotionEffectType.SPEED)) {
				((LivingEntity) event.getEntity()).removePotionEffect(PotionEffectType.SPEED);
				((LivingEntity) event.getEntity())
						.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true));
			} else {
				((LivingEntity) event.getEntity())
						.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, true));
			}
			event.getEntity()
					.sendMessage(instance.getGameManager().getMain().color("&6&l(!) &rOuch! You gained some speed"));

			for (Player gamePlayer : instance.players)
				gamePlayer.playSound(player.getLocation(), Sound.PIG_DEATH, 1, 1);
		}
	}

	private void abilityMsg() {
		player.sendMessage("");
		player.sendMessage(instance.getGameManager().getMain().color(
				"&e&lCLASS TIP> &rWhen set on fire, you'll recieve a cooked porkchop for as long as you're on fire for, with Fire Aspect I"));
		player.sendMessage("");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.PORK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			this.abilityMsg();
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Pig;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.PORK),
						instance.getGameManager().getMain().color("&rRaw Pork &7(Right Click)")),
				Enchantment.DAMAGE_ALL, 4), Enchantment.KNOCKBACK, 1);
		return item;
	}
}
