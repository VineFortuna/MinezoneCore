package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Random;

public class DarkSethBlingClass extends BaseClass implements Listener {

	public boolean usedTp = false;
	public ItemStack teleporterItem;

	public DarkSethBlingClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.5;
		instance.getGameManager().getMain().getServer().getPluginManager().registerEvents(this,
				instance.getGameManager().getMain());
	}

	public ItemStack makeNavy(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.NAVY);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("SethBling");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeNavy(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeNavy(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeNavy(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.usedTp = false; // To reset each life
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.COMMAND),
						"" + ChatColor.RED + ChatColor.BOLD + "Item Stealer", "",
						ChatColor.GRAY + "Steal an item from one of your opponents"));
		this.teleporterItem = ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
				"" + ChatColor.RED + ChatColor.BOLD + "Item Teleporter", "",
				ChatColor.GRAY + "Teleport to a recent lightning drop");
		playerInv.setItem(2, teleporterItem);
	}

	private Player getRandomPlayer(Player cant) {
		ArrayList<Player> cloned = new ArrayList<>(instance.players);
		cloned.remove(cant);
		Random r = new Random();
		return cloned.get(r.nextInt(cloned.size()));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack plItem = event.getItem();

		if (plItem != null) {
			if (plItem.getType() == Material.COMMAND
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				Random rand = new Random();
				Player target = this.getRandomPlayer(player);

				if (!this.doesPlayerContainItems(target.getInventory())) {
					player.sendMessage(instance.getGameManager().getMain()
							.color("&2&l(!) &rNo item was found at this player! Please try again."));
					return;
				}
				ArrayList<Integer> slots = new ArrayList<Integer>();
				Inventory inv = target.getInventory();
				for (int i = 0; i < inv.getSize(); i++) {
					for (ItemStack itemDrop : instance.getAllItemDrops()) {
						if (inv.getItem(i) != null && inv.getItem(i).isSimilar(itemDrop)) {
							slots.add(i);
							break;
						}
					}
				}

				if (slots.isEmpty())
					Bukkit.getLogger().severe("Something went wrong!");

				int i = rand.nextInt(slots.size());
				ItemStack skeppy = inv.getItem(slots.get(i));
				inv.clear(slots.get(i));
				slots.clear();
				
				String displayName = skeppy.getItemMeta().getDisplayName();
				if (displayName == null)
					displayName = WordUtils.capitalizeFully(skeppy.getType().name().replace('_', ' '));

				player.getInventory().addItem(skeppy);
				player.sendMessage(instance.getGameManager().getMain()
						.color("&2&l(!) &rYou were given a &e" + displayName));
				target.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &rWhoops! Your &e"
						+ displayName + " &ritem was stolen by &e" + player.getName()));
				if (plItem.getAmount() == 1)
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				else
					plItem.setAmount(plItem.getAmount() - 1);
			} else if (plItem.getType() == Material.NETHER_STAR && plItem.isSimilar(teleporterItem)
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (instance.recentDrop == null || !instance.isInBounds(instance.recentDrop)) {
					player.sendMessage(
							instance.getGameManager().getMain().color("&c&l(!) &rThere are no drops you can pickup!"));
				} else if (!(player.isOnGround())) {
					player.sendMessage(instance.getGameManager().getMain()
							.color("&c&l(!) &rYou have to be on the ground to use this!"));
				} else {
					player.teleport(instance.recentDrop);
					player.getInventory().remove(player.getItemInHand());
					player.sendMessage(instance.getGameManager().getMain().color(
							"&2&l(!) &rYou teleported to the recently spawned item! (Could be good or bad luck idk lol)"));
					this.usedTp = true;
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.DarkSethBling;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.COAL_BLOCK),
						"" + ChatColor.DARK_GRAY + "Dark Command Block"), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 2);
		return item;
	}

	private boolean doesPlayerContainItems(Inventory inv) {
		for (int i = 0; i < inv.getSize(); i++) {
			for (ItemStack itemDrop : instance.getAllItemDrops()) {
				if (inv.getItem(i) != null && inv.getItem(i).isSimilar(itemDrop)) {
					return true;
				}
			}
		}
		return false;
	}

}