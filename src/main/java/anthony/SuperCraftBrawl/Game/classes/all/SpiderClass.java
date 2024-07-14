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

public class SpiderClass extends BaseClass {

	public SpiderClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	public ItemStack makePurple(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.RED);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDQyZGM4MWZlZDdhYzdjMmJkNTI0MmQ0N2QyMDRiMDQxNzM4ZWViYjA0MjNmODMxNWVmOGNmZWYxOTNmMjA2YSJ9fX0=";
		ItemStack playerskull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makePurple(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makePurple(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makePurple(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 2));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 2));
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;

		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (instance.duosMap != null)
				if (instance.team.get(p).equals(instance.team.get(player)))
					return;

			p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0, true));
		}
	}
	
	@Override
	public void TakeDamage(EntityDamageEvent event) {
		if (instance.getGameManager().spawnProt.containsKey(player))
			return;
		
		for (Player gamePlayer : instance.players)
			gamePlayer.playSound(player.getLocation(), Sound.SPIDER_DEATH, 1, 1);
	}

	private void abilityMsg() {
		player.sendMessage("");
		player.sendMessage(instance.getGameManager().getMain()
				.color("&e&lCLASS TIP> &rCertain chance to infect other players with Poison I by hitting them!"));
		player.sendMessage("");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.SPIDER_EYE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			this.abilityMsg();
			event.setCancelled(true);
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Spider;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.SPIDER_EYE),
						instance.getGameManager().getMain()
						.color("&cSpider Eye &7(Right Click)")), Enchantment.DAMAGE_ALL, 2), Enchantment.KNOCKBACK, 1);
		return item;
	}
}
