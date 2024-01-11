package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import java.util.ArrayList;
import java.util.Random;
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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

public class DarkSethBlingClass extends BaseClass implements Listener {
	
	public boolean usedTp = false;
	public ItemStack teleporterItem;

	public DarkSethBlingClass(GameInstance instance, Player player) {
		super(instance, player);
		this.baseVerticalJump = 1.5D;
		instance.getGameManager().getMain().getServer().getPluginManager().registerEvents(this,
				(Plugin) instance.getGameManager().getMain());
	}

	public ItemStack makeNavy(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.NAVY);
		armour.setItemMeta((ItemMeta) lm);
		return armour;
	}

	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();
		meta.setOwner("SethBling");
		meta.setDisplayName("");
		playerskull.setItemMeta((ItemMeta) meta);
		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeNavy(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeNavy(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeNavy(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public void SetItems(Inventory playerInv) {
		this.usedTp = false;
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.COMMAND),
						"" + ChatColor.RED + ChatColor.BOLD + "Item Stealer",
						new String[] { "", ChatColor.GRAY + "Steal an item from one of your opponents" }));
		this.teleporterItem = ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
				"" + ChatColor.RED + ChatColor.BOLD + "Item Teleporter",
				new String[] { "", ChatColor.GRAY + "Teleport to a recent lightning drop" });
		playerInv.setItem(2, this.teleporterItem);
	}

	private Player getRandomPlayer(Player cant) {
		ArrayList<Player> cloned = new ArrayList<>(this.instance.players);
		cloned.remove(cant);
		Random r = new Random();
		return cloned.get(r.nextInt(cloned.size()));
	}

	public void UseItem(PlayerInteractEvent event) {
		ItemStack plItem = event.getItem();
		if (plItem != null)
			if (plItem.getType() == Material.COMMAND
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				Random rand = new Random();
				Player target = getRandomPlayer(this.player);
				if (!doesPlayerContainItems((Inventory) target.getInventory())) {
					this.player.sendMessage(this.instance.getGameManager().getMain()
							.color("&2&l(!) &rNo item was found at this player! Please try again."));
					return;
				}
				ArrayList<Integer> slots = new ArrayList<>();
				PlayerInventory playerInventory = target.getInventory();
				int i;
				for (i = 0; i < playerInventory.getSize(); i++) {
					for (ItemStack itemDrop : this.instance.getAllItemDrops()) {
						if (playerInventory.getItem(i) != null && playerInventory.getItem(i).isSimilar(itemDrop)) {
							slots.add(Integer.valueOf(i));
							break;
						}
					}
				}
				if (slots.isEmpty())
					Bukkit.getLogger().severe("Something went wrong!");
				i = rand.nextInt(slots.size());
				ItemStack skeppy = playerInventory.getItem(((Integer) slots.get(i)).intValue());
				playerInventory.clear(((Integer) slots.get(i)).intValue());
				slots.clear();
				String displayName = skeppy.getItemMeta().getDisplayName();
				if (displayName == null)
					displayName = WordUtils.capitalizeFully(skeppy.getType().name().replace('_', ' '));
				this.player.getInventory().addItem(new ItemStack[] { skeppy });
				this.player.sendMessage(
						this.instance.getGameManager().getMain().color("&2&l(!) &rYou were given a &e" + displayName));
				target.sendMessage(this.instance.getGameManager().getMain().color("&2&l(!) &rWhoops! Your &e"
						+ displayName + " &ritem was stolen by &e" + this.player.getName()));
				if (plItem.getAmount() == 1) {
					this.player.getInventory().clear(this.player.getInventory().getHeldItemSlot());
				} else {
					plItem.setAmount(plItem.getAmount() - 1);
				}
			} else if (plItem.getType() == Material.NETHER_STAR && plItem.isSimilar(this.teleporterItem)
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (this.instance.recentDrop == null || !this.instance.isInBounds(this.instance.recentDrop)) {
					this.player.sendMessage(this.instance.getGameManager().getMain()
							.color("&c&l(!) &rThere are no drops you can pickup!"));
				} else if (!this.player.isOnGround()) {
					this.player.sendMessage(this.instance.getGameManager().getMain()
							.color("&c&l(!) &rYou have to be on the ground to use this!"));
				} else {
					this.player.teleport(this.instance.recentDrop);
					this.player.getInventory().remove(this.player.getItemInHand());
					this.player.sendMessage(this.instance.getGameManager().getMain().color(
							"&2&l(!) &rYou teleported to the recently spawned item! (Could be good or bad luck idk lol)"));
					this.usedTp = true;
				}
			}
	}

	public ClassType getType() {
		return ClassType.DarkSethBling;
	}

	public void SetNameTag() {
	}

	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.COAL_BLOCK),
								"" + ChatColor.DARK_GRAY + "Dark Command Block", new String[0]),
						Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 2);
		return item;
	}

	private boolean doesPlayerContainItems(Inventory inv) {
		for (int i = 0; i < inv.getSize(); i++) {
			for (ItemStack itemDrop : this.instance.getAllItemDrops()) {
				if (inv.getItem(i) != null && inv.getItem(i).isSimilar(itemDrop))
					return true;
			}
		}
		return false;
	}
}