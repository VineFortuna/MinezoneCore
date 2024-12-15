package anthony.SuperCraftBrawl.gui.cosmetics;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class GadgetsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public GadgetsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Gadgets").parent(parent).build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		// Icon Items
		// Broom
		List<String> broomList = new ArrayList<>();
		broomList.add(ChatColor.DARK_GRAY + "Fly around like a Witch!");
		ItemStack broom = ItemHelper.create(Material.WHEAT,
				ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Magic Broom", broomList);

		// Melon
		List<String> melonList = new ArrayList<>();
		melonList.add(ChatColor.DARK_GRAY + "A delicious melon that gives you...");
		melonList.add(ChatColor.DARK_GRAY + "                  Superpowers!");
		melonList.add("");
		melonList.add(ChatColor.RESET + "You have " + ChatColor.YELLOW + data.melon + ChatColor.RESET + " Melons");
		ItemStack melon = ItemHelper.create(Material.MELON, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Melons",
				melonList);

		// Paintball
		List<String> paintballList = new ArrayList<>();
		paintballList.add(ChatColor.DARK_GRAY + "Shoot paintballs as you want");
		paintballList.add("");
		paintballList.add(
				ChatColor.RESET + "You have " + ChatColor.YELLOW + data.paintball + ChatColor.RESET + " Paintballs");
		ItemStack paintball = ItemHelper.create(Material.GOLD_BARDING,
				ChatColor.YELLOW.toString() + ChatColor.BOLD + "Paintball Gun", paintballList);

		// Fishing
		ItemStack fishingRod = main.getFishingRod(player);

		List<String> snow = new ArrayList<>();
		ItemStack snowball = ItemHelper.create(Material.SNOW_BALL,
				main.color("&r&lSnow Particles"), snow);

		List<String> snowman = new ArrayList<>();
		snow.add(ChatColor.DARK_GRAY + "Snowman Pet");
		snow.add("");
		snow.add(main.color("&e&lUNLOCKED"));
		ItemStack snowmanPet = ItemHelper.create(Material.MONSTER_EGG,
				ChatColor.YELLOW.toString() + ChatColor.BOLD + "Snowman Pet", snowman);

		
		List<String> candyCaneSwirl = new ArrayList<>();
		candyCaneSwirl.add(main.color("&c&lCandy &r&lCane &c&lSwirl"));
		ItemStack candyCane = ItemHelper.createSkullTexture(
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM4M2E0MmU4MmNkNmE3MGUyMTZkOWE4YzJmZjZmMWU1ZTViMjU2Y2VhM2I4Y2QyMjU0NzIzOTNhYTNlY2E1YSJ9fX0=");

		// Setting Items
		contents.fillBorders(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		// Broom Gadget
		contents.set(1, 1, ClickableItem.of(broom, e -> {
			if (player.hasPermission("scb.wheat")) {
				if (!(player.getInventory().contains(broom))) {
					player.getInventory().setItem(5, broom);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have equipped " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
					inv.close(player);
				} else if (player.getInventory().contains(broom)) {
					player.getInventory().remove(broom);
					player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You have unequipped " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Magic Broom");
					inv.close(player);
				}
			} else {
				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
						+ ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN " + ChatColor.RESET + "to use this item!");
			}
		}));

		// Paintball Gadget
		contents.set(1, 2, ClickableItem.of(paintball, e -> {
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

		// Melon Gadget
		contents.set(1, 3, ClickableItem.of(melon, e -> {
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
		// Fishing Rod
		contents.set(1, 4, ClickableItem.of(fishingRod, e -> {
			if (!(player.getInventory().contains(fishingRod))) {
				player.getInventory().setItem(5, fishingRod);
				player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have equipped " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Fishing Rod");
				inv.close(player);
			} else if (player.getInventory().contains(fishingRod)) {
				player.getInventory().remove(fishingRod);
				player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have unequipped " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Fishing Rod");
				inv.close(player);
			}
		}));

		contents.set(1, 5, ClickableItem.of(snowball, e -> {
			if (data.snowParticles == 1) {
				if (!(main.getListener().snowParticlePlayers.contains(player))) {
					player.sendMessage(main.color("&r&l(!) &rYou equipped &eSnow Particles &rgadget"));
					main.getListener().snowParticlePlayers.add(player);
				} else {
					player.sendMessage(main.color("&r&l(!) &rYou removed &eSnow Particles &rgadget"));
					main.getListener().snowParticlePlayers.remove(player);
				}
			} else {
				player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this gadget yet!"));
			}
		}));

		contents.set(1, 6, ClickableItem.of(snowmanPet, e -> {
			// if (data.snowParticles == 1) {
			/*if (!(main.getListener().snowmanPetPlayers.contains(player))) {
				player.sendMessage(main.color("&r&l(!) &rYou equipped &eSnowman &rpet"));
				main.getListener().snowmanPetPlayers.add(player);
				main.getListener().snowmanPet(player);
			} else {
				player.sendMessage(main.color("&r&l(!) &rYou removed &eSnowman &rpet"));
				main.getListener().snowmanPetPlayers.remove(player);
			}*/
			// } else {
			player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this gadget yet!"));
			// }
		}));

		contents.set(1, 7, ClickableItem.of(candyCane, e -> {
			// if (data.snowParticles == 1) {
			/*if (!(main.getListener().candyCaneSwirlPlayers.contains(player))) {
				player.sendMessage(main.color("&r&l(!) &rYou equipped &eCandy Cane Swirl &gadget"));
				main.getListener().candyCaneSwirlPlayers.add(player);
				main.getListener().candyCaneSwirlCosmetic(player);
			} else {
				player.sendMessage(main.color("&r&l(!) &rYou removed &eCandy Cane Swirl &gadget"));
				main.getListener().candyCaneSwirlPlayers.remove(player);
			}*/
			// } else {
			player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this gadget yet!"));
			// }
		}));

		contents.set(2, 8, ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}));
	}

//	//give @p minecraft:player_head[profile={id:[I;-853038783,-424126035,-1902419453,-1029693902],properties:[{name:"textures",value:"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQwZmJjN2E2YWQ4M2U5MjRkYjZjYTBjYTM0N2RjZjVmMmY0MzRmMzQ3NDJmODMyOTYwYTA0MDZmYmRiYjE4NyJ9fX0="}]},minecraft:lore=['{"text":"https://namemc.com/skin/944218dcc2d0316a"}']]

	private static final String ELF_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQwZmJjN2E2YWQ4M2U5MjRkYjZjYTBjYTM0N2RjZjVmMmY0MzRmMzQ3NDJmODMyOTYwYTA0MDZmYmRiYjE4NyJ9fX0=";

	private void equipElfSet(Player player) {
		// Give the player the Elf head
		ItemStack elfHead = getCustomSkull(ELF_TEXTURE);
		// Give dyed leather armor
		ItemStack chest = getDyedArmor(Material.LEATHER_CHESTPLATE, Color.GREEN, ChatColor.GREEN + "Elf Tunic");
		ItemStack legs = getDyedArmor(Material.LEATHER_LEGGINGS, Color.RED, ChatColor.RED + "Elf Pants");
		ItemStack boots = getDyedArmor(Material.LEATHER_BOOTS, Color.GREEN, ChatColor.GREEN + "Elf Boots");

		player.getInventory().setHelmet(elfHead);
		player.getInventory().setChestplate(chest);
		player.getInventory().setLeggings(legs);
		player.getInventory().setBoots(boots);
	}

	private ItemStack getDyedArmor(Material material, Color color, String name) {
		ItemStack item = new ItemStack(material);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(color);
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Creates a custom player head with the given base64 texture. Works on 1.8
	 * using reflection and GameProfile.
	 */
	private ItemStack getCustomSkull(String base64) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

		// Create a fake profile with the given texture
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", base64));

		try {
			Field profileField = skullMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(skullMeta, profile);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}

		skullMeta.setDisplayName(ChatColor.GREEN + "Elf Head");
		skull.setItemMeta(skullMeta);
		return skull;
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
