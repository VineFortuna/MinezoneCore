package anthony.SuperCraftBrawl.gui;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.RankManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class CosmeticsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private RankManager rm;

	public CosmeticsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(4, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Cosmetics").build();
		this.main = main;
	}

	public RankManager getRankManager() {
		return rm;
	}

	public ItemStack colorArmor(ItemStack armour, Color c) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(c);
		armour.setItemMeta(lm);
		return armour;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		ItemStack wheat = ItemHelper.setDetails(new ItemStack(Material.WHEAT),
				"" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
		ItemStack melon = ItemHelper.setDetails(new ItemStack(Material.MELON),
				"" + ChatColor.RESET + ChatColor.YELLOW + "Melons");
		ItemStack santa = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta1 = (SkullMeta) santa.getItemMeta();

		meta1.setOwner("Santa_");
		meta1.setDisplayName("");

		santa.setItemMeta(meta1);

		contents.set(0, 4, ClickableItem.of(
				ItemHelper.setDetails(santa, "" + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "Santa Outfit"),
				e -> {
					if (data != null) {
						if (data.santaoutfit == 1) {
							if (!(main.so.containsKey(player))) {
								if (main.ao.containsKey(player)) {
									main.ao.remove(player);
									player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "You have unequipped " + ChatColor.YELLOW + "Astronaut Outfit");
								}
								main.so.put(player, true);
								player.getInventory().setHelmet(santa);
								player.getInventory().setChestplate(
										colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.RED));
								player.getInventory()
										.setLeggings(colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.RED));
								player.getInventory()
										.setBoots(colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.RED));
								player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ "You have equipped " + ChatColor.RED + ChatColor.BOLD + "Santa Outfit");
							} else {
								main.so.remove(player);
								player.getInventory().setHelmet(new ItemStack(Material.AIR));
								player.getInventory().setChestplate(new ItemStack(Material.AIR));
								player.getInventory().setLeggings(new ItemStack(Material.AIR));
								player.getInventory().setBoots(new ItemStack(Material.AIR));
								player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ "You have unequipped " + ChatColor.RED + ChatColor.BOLD + "Santa Outfit");
							}
						} else {
							player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have not unlocked this cosmetic yet!");
						}
					}

					inv.close(player);
				}));

		contents.set(1, 3, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WHEAT),
				"" + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + "Magic Broom", "",
				"" + ChatColor.RESET + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN" + ChatColor.RESET + "+ exclusive!"),
				e -> {
					if (player.hasPermission("scb.wheat")) {
						if (!(player.getInventory().contains(wheat))) {
							player.getInventory().setItem(5, wheat);
							player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have equipped " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
							inv.close(player);
						} else if (player.getInventory().contains(wheat)) {
							player.getInventory().remove(wheat);
							player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have unequipped " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
							inv.close(player);
						}
					} else {
						player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You need the rank " + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN " + ChatColor.RESET
								+ "to use this item!");
					}
				}));

		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("SethBling");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		contents.set(2, 3, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.LEATHER_CHESTPLATE),
				"" + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + "Astronaut Outfit"), e -> {
					if (data.astronaut == 1) {
						if (!(main.ao.containsKey(player))) {
							main.ao.put(player, true);
							player.getInventory().setHelmet(new ItemStack(Material.GLASS));
							player.getInventory()
									.setChestplate(colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.WHITE));
							player.getInventory()
									.setLeggings(colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.WHITE));
							player.getInventory()
									.setBoots(colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.WHITE));
							player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have equipped " + ChatColor.YELLOW + "Astronaut Outfit");
						} else {
							main.ao.remove(player);
							player.getInventory().setHelmet(new ItemStack(Material.AIR));
							player.getInventory().setChestplate(new ItemStack(Material.AIR));
							player.getInventory().setLeggings(new ItemStack(Material.AIR));
							player.getInventory().setBoots(new ItemStack(Material.AIR));
							player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have unequipped " + ChatColor.YELLOW + "Astronaut Outfit");
						}
					} else {
						player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You have not unlocked this cosmetic yet!");
					}
					inv.close(player);
				}));
		contents.set(2, 5, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.GOLD_BARDING),
				"" + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + "Paintball Gun", "", "" + ChatColor.RESET
						+ "You have " + ChatColor.YELLOW + data.paintball + ChatColor.RESET + " Paintballs"),
				e -> {
					if (data.paintball > 0) {
						if (!(player.getInventory().contains(Material.GOLD_BARDING))) {
							ItemStack p = ItemHelper.setDetails(new ItemStack(Material.GOLD_BARDING, data.paintball),
									"" + ChatColor.RESET + ChatColor.GREEN + "Paintball Gun", "",
									"" + ChatColor.RESET + ChatColor.GRAY + "Right click to shoot a paintball!");
							player.getInventory().setItem(5, p);
							player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have equipped " + ChatColor.GREEN + "Paintball Gun");
						} else {
							player.getInventory().remove(Material.GOLD_BARDING);
							player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have unequipped " + ChatColor.GREEN + "Paintball Gun");
						}
					} else {
						player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You do not have enough paintballs!");
					}
					inv.close(player);
				}));
		contents.set(1, 5, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.MELON),
				"" + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + "Melons", "",
				"" + ChatColor.RESET + "You have " + ChatColor.YELLOW + data.melon + ChatColor.RESET + " Melons"),
				e -> {
					if (data.melon > 0) {
						if (!(player.getInventory().contains(melon))) {
							player.getInventory().setItem(5, melon);
							player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have equipped " + ChatColor.YELLOW + "Melons");
							inv.close(player);
						} else if (player.getInventory().contains(melon)) {
							player.getInventory().remove(melon);
							player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have unequipped " + ChatColor.YELLOW + "Melons");
						}
					} else {
						player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You do not have enough melons!");
					}
					inv.close(player);
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
