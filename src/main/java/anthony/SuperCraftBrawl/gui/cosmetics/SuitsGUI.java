package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class SuitsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	private static final String ELF_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQwZmJjN2E2YWQ4M2U5MjRkYjZjYTBjYTM0N2RjZjVmMmY0MzRmMzQ3NDJmODMyOTYwYTA0MDZmYmRiYjE4NyJ9fX0=";

	public SuitsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Suits").parent(parent).build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		// Icon Items
		// Santa Outfit
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";
		ItemStack santaHead = ItemHelper.createSkullTexture(texture,
				ChatColor.RED.toString() + ChatColor.BOLD + "Santa Outfit");

		// Astronaut Outfit
		ItemStack astronautHead = ItemHelper.create(Material.GLASS,
				ChatColor.YELLOW.toString() + ChatColor.BOLD + "Astronaut Outfit");

		texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjlhYzgwNGEyYzVhOGVhNTdlZjY5NjU3YWI2NDM0N2QxZWQzNmIzNGNhNzBhMjE4ZjZhNjNkNWI2YWEyZmU5ZiJ9fX0=";
		ItemStack pirateHead = ItemHelper.createSkullTexture(texture, "&3&lPirate Outfit", "", "&aFishing reward!");

		ItemStack elfHead = ItemHelper.createSkullTexture(ELF_TEXTURE, "&a&lElf Outfit", "", "&cChristmas 2024 exclusive");

		// Golden Outfit
		ItemStack goldenHead = ItemHelper.create(Material.GOLD_HELMET, main.color("&6&lGolden Outfit"), "",
				main.color(Rank.VIP.getTag() + "&r+ exclusive"));

		// Freddy Outfit
		final int basketsFoundForLore = (main.getHalloweenManager() != null)
				? main.getHalloweenManager().getFoundCount(player.getUniqueId())
				: 0;
		
		texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiMjdjY2I0ZjEyNjQwZjFiNThlYTYyZDkwY2RhY2U0NGMwZjJkYTlmMzkwOGUyNWViMTZiZGI1YmJiNWE2NSJ9fX0=";
		ItemStack freddyOutfit = ItemHelper.createSkullTexture(texture,
				main.color("&4&6Freddy Outfit"), "",
				main.color("&7Unlock by finding 6 baskets in the lobby!"),
				main.color("&7Progress: &e" + Math.min(basketsFoundForLore, 6) + "&7/6"),
				"",
				main.color("&cHalloween 2025 exclusive"));

		// Setting Items
		contents.fillBorders(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		// Astronaut Outfit
		contents.set(1, 1, ClickableItem.of(astronautHead, e -> {
			if (data.astronaut == 1) {
				if (!(main.ao.containsKey(player))) {
					removeOutfits(player);
					main.ao.put(player, true);
					player.getInventory().setHelmet(astronautHead);
					player.getInventory().setChestplate(ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE,
							Color.WHITE, ChatColor.WHITE + "Astronaut Outfit"));
					player.getInventory().setLeggings(ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS,
							Color.WHITE, ChatColor.WHITE + "Astronaut Outfit"));
					player.getInventory().setBoots(ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.WHITE,
							ChatColor.WHITE + "Astronaut Outfit"));
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have equipped " + ChatColor.YELLOW + "Astronaut Outfit");
				} else {
					removeOutfits(player);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have unequipped " + ChatColor.YELLOW + "Astronaut Outfit");
					main.getListener().resetArmor(player);
				}
			} else {
				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have not unlocked this cosmetic yet!");
			}
			inv.close(player);
		}));

		// Santa Outfit
		contents.set(1, 2, ClickableItem.of(santaHead, e -> {
			if (data != null) {
				if (data.santaoutfit == 1) {
					if (!(main.so.containsKey(player))) {
						removeOutfits(player);
						main.so.put(player, true);
						player.getInventory().setHelmet(santaHead);
						player.getInventory().setChestplate(ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE,
								Color.RED, ChatColor.RED + "Santa Outfit"));
						player.getInventory().setLeggings(ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS,
								Color.RED, ChatColor.RED + "Santa Outfit"));
						player.getInventory().setBoots(ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.RED,
								ChatColor.RED + "Santa Outfit"));
						player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You have equipped " + ChatColor.RED + ChatColor.BOLD + "Santa Outfit");
					} else {
						removeOutfits(player);
						player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You have unequipped " + ChatColor.RED + ChatColor.BOLD + "Santa Outfit");
						main.getListener().resetArmor(player);
					}
				} else {
					player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have not unlocked this cosmetic yet!");
				}
			}

			inv.close(player);
		}));
		contents.set(1, 3, ClickableItem.of(pirateHead, e -> {
			if (data.rewardLevel >= 6) {
				if (!(main.po.containsKey(player))) {
					removeOutfits(player);
					main.po.put(player, true);
					player.getInventory().setHelmet(pirateHead);
					player.getInventory().setChestplate(ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE,
							Color.GREEN, ChatColor.DARK_AQUA + "Pirate Outfit"));
					player.getInventory().setLeggings(ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS,
							Color.MAROON, ChatColor.DARK_AQUA + "Pirate Outfit"));
					player.getInventory().setBoots(ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.BLACK,
							ChatColor.DARK_AQUA + "Pirate Outfit"));
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have equipped " + ChatColor.DARK_AQUA + "Pirate Outfit");
				} else {
					removeOutfits(player);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have unequipped " + ChatColor.DARK_AQUA + "Pirate Outfit");
					main.getListener().resetArmor(player);
				}
			} else {
				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have not unlocked this cosmetic yet!");
			}
			inv.close(player);
		}));

		contents.set(1, 4, ClickableItem.of(elfHead, e -> {
			if (data.elfCosmetic == 1) {
				if (!(main.getListener().elfCosmeticPlayers.contains(player))) {
					player.sendMessage(main.color("&9&l(!) &rYou have equipped &aElf Outfit"));
					removeOutfits(player);
					main.getListener().elfCosmeticPlayers.add(player);
					ItemStack chest = getDyedArmor(Material.LEATHER_CHESTPLATE, Color.GREEN,
							ChatColor.GREEN + "Elf Outfit");
					ItemStack legs = getDyedArmor(Material.LEATHER_LEGGINGS, Color.RED, ChatColor.GREEN + "Elf Outfit");
					ItemStack boots = getDyedArmor(Material.LEATHER_BOOTS, Color.GREEN, ChatColor.GREEN + "Elf Outfit");

					player.getInventory().setHelmet(elfHead);
					player.getInventory().setChestplate(chest);
					player.getInventory().setLeggings(legs);
					player.getInventory().setBoots(boots);
				} else {
					removeOutfits(player);
					player.sendMessage(main.color("&9&l(!) &rYou have unequipped &aElf Outfit"));
					main.getListener().resetArmor(player);
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this cosmetic yet!"));
			}
			inv.close(player);
		}));

		contents.set(1, 5, ClickableItem.of(goldenHead, e -> {
			if (player.hasPermission("scb.vip")) {
				if (!(main.getListener().goldenOutfitPlayers.contains(player))) {
					player.sendMessage(main.color("&9&l(!) &rYou have equipped &6Golden Outfit"));
					removeOutfits(player);
					main.getListener().goldenOutfitPlayers.add(player);
					ItemStack helmet = ItemHelper.create(Material.GOLD_HELMET, main.color("&6Golden Outfit"));
					ItemStack chest = ItemHelper.create(Material.GOLD_CHESTPLATE, main.color("&6Golden Outfit"));
					ItemStack legs = ItemHelper.create(Material.GOLD_LEGGINGS, main.color("&6Golden Outfit"));
					ItemStack boots = ItemHelper.create(Material.GOLD_BOOTS, main.color("&6Golden Outfit"));

					player.getInventory().setHelmet(helmet);
					player.getInventory().setChestplate(chest);
					player.getInventory().setLeggings(legs);
					player.getInventory().setBoots(boots);
				} else {
					removeOutfits(player);
					player.sendMessage(main.color("&9&l(!) &rYou have unequipped &6Golden Outfit"));
					main.getListener().resetArmor(player);
				}
			} else {
				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
						+ Rank.VIP.getTag() + ChatColor.RESET + " to use this!");
			}
			inv.close(player);
		}));

		contents.set(1, 6, ClickableItem.of(freddyOutfit, e -> {
			if (!(main.getListener().freddyOutfitPlayers.contains(player))) {
				if (main.getListener().getHalloweenEventProgress(player) >= 6) {
					removeOutfits(player);
					createArmor(player,
							"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiMjdjY2I0ZjEyNjQwZjFiNThlYTYyZDkwY2RhY2U0NGMwZjJkYTlmMzkwOGUyNWViMTZiZGI1YmJiNWE2NSJ9fX0",
							"664C33", 6, "Freddy");
					main.getListener().freddyOutfitPlayers.add(player);
					player.sendMessage(main.color("&9&l(!) &rYou have equipped &6&lFreddy&r outfit"));
				} else {
					player.sendMessage(main.color("&c&l(!) &rYou need &e6/10 &rbaskets to use this!"));
					return;
				}
			} else {
				removeOutfits(player);
				player.sendMessage(main.color("&9&l(!) &rYou have unequipped &6&lFreddy&r outfit"));
				main.getListener().resetArmor(player);
			}
			inv.close(player);
		}));

		contents.set(2, 8, ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}));
	}

	private ItemStack getDyedArmor(Material material, Color color, String name) {
		ItemStack item = new ItemStack(material);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(color);
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

	private void removeOutfits(Player player) {
		main.ao.remove(player);
		main.so.remove(player);
		main.po.remove(player);
		main.getListener().elfCosmeticPlayers.remove(player);
		main.getListener().goldenOutfitPlayers.remove(player);
		main.getListener().freddyOutfitPlayers.remove(player);
	}

	/**
	 * Create a themed armor set: - Custom head texture (base64) - Dyed leather
	 * chest/legs/boots from hex color (e.g., "664C33") - Optional glow (if
	 * glowLevel > 0)
	 *
	 * @param giveTo    Player to equip (or null to just build items)
	 * @param headB64   Mojang textures Base64 for the head
	 * @param hex       RGB hex without '#', e.g. "664C33"
	 * @param glowLevel any >0 value will add a subtle enchant glint
	 * @param setName   display name base, e.g. "Freddy"
	 * @return items in order: helmet, chest, legs, boots
	 */
	public static ItemStack[] createArmor(Player giveTo, String headB64, String hex, int glowLevel, String setName) {
		// Parse hex → Bukkit Color
		Color leatherColor = fromHex(hex);

		// Helmet (custom skull)
		ItemStack helmet = customHead(headB64, "§6§l" + setName + " Head", "§7Part of the §6" + setName + "§7 set");

		// Leather pieces
		ItemStack chest = dyedLeather(Material.LEATHER_CHESTPLATE, leatherColor, "§6§l" + setName + " Chestplate");
		ItemStack legs = dyedLeather(Material.LEATHER_LEGGINGS, leatherColor, "§6§l" + setName + " Leggings");
		ItemStack boots = dyedLeather(Material.LEATHER_BOOTS, leatherColor, "§6§l" + setName + " Boots");

		// Optional glow
		if (glowLevel > 0) {
			addGlow(helmet);
			addGlow(chest);
			addGlow(legs);
			addGlow(boots);
		}

		// Unbreakable (Spigot 1.8 supports this via spigot() API)
		makeUnbreakable(helmet);
		makeUnbreakable(chest);
		makeUnbreakable(legs);
		makeUnbreakable(boots);

		ItemStack[] set = new ItemStack[] { helmet, chest, legs, boots };

		// Equip if player provided
		if (giveTo != null) {
			PlayerInventory inv = giveTo.getInventory();
			inv.setHelmet(helmet);
			inv.setChestplate(chest);
			inv.setLeggings(legs);
			inv.setBoots(boots);
			giveTo.updateInventory();
		}

		return set;
	}

	// ---------- helpers ----------

	private static Color fromHex(String hex) {
		String s = hex.replace("#", "");
		int rgb = Integer.parseInt(s, 16);
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = (rgb) & 0xFF;
		return Color.fromRGB(r, g, b);
	}

	private static ItemStack dyedLeather(Material type, Color c, String name) {
		ItemStack item = new ItemStack(type);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(c);
		meta.setDisplayName(name);
		meta.setLore(Collections.singletonList("§7Event Cosmetic"));
		// hide attributes if available (1.8+)
		try {
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
		} catch (Throwable ignored) {
		}
		item.setItemMeta(meta);
		return item;
	}

	private static ItemStack customHead(String b64, String name, String... lore) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setDisplayName(name);
		if (lore != null && lore.length > 0)
			meta.setLore(Arrays.asList(lore));

		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", b64));
		try {
			Field f = meta.getClass().getDeclaredField("profile");
			f.setAccessible(true);
			f.set(meta, profile);
		} catch (Exception ignored) {
		}
		// hide attributes if available
		try {
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
		} catch (Throwable ignored) {
		}
		skull.setItemMeta(meta);
		return skull;
	}

	private static void addGlow(ItemStack item) {
		try {
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			item.setItemMeta(meta);
		} catch (Throwable ignored) {
		}
		item.addUnsafeEnchantment(Enchantment.DURABILITY, 1); // subtle glint
	}

	private static void makeUnbreakable(ItemStack item) {
		try {
			ItemMeta meta = item.getItemMeta();
			// Spigot 1.8 way
			meta.spigot().setUnbreakable(true);
			// Hide unbreakable flag if supported
			try {
				meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			} catch (Throwable ignored) {
			}
			item.setItemMeta(meta);
		} catch (Throwable ignored) {
		}
	}
}
