package anthony.SuperCraftBrawl.gui;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.Animation;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;

public class MysteryChestsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private Location loc;

	public MysteryChestsGUI(Core main, Location loc) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Mystery Chest").build();
		this.main = main;
		this.loc = loc;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		Location newLoc = new Location(player.getWorld(), loc.getX() + 1, loc.getY() + 2, loc.getZ() + 0.5);

		if (data != null) {
			if (data.mysteryChests > 0) {
				contents.set(0, 0, ClickableItem.of(ItemHelper.setDetails(
								new ItemStack(Material.ENDER_CHEST, data.mysteryChests),
								"" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "MysteryChest", "",
								"" + ChatColor.RESET + ChatColor.YELLOW + "You have " + data.mysteryChests
								+ (data.mysteryChests > 1 ?" Chests" : " Chest") + " to Open!"),
						e -> {
							if (data.mysteryChests > 0) {
								inv.close(player);
								main.getGameManager().chestCanOpen = true;
								ArmorStand stand = newLoc.getWorld().spawn(newLoc, ArmorStand.class);
								stand.setVisible(false);
								stand.setGravity(false);
								stand.setArms(true);
								stand.setItemInHand(new ItemStack(Material.CHEST));
								@SuppressWarnings("deprecation")
								int animate = Bukkit.getScheduler().scheduleAsyncRepeatingTask(main, new Animation(stand),
										0, 1);
								
								new BukkitRunnable() {
									@Override
									public void run() {
										Random r = new Random();
										int chance = r.nextInt(100);
										
										if (chance >= 0 && chance <= 5) {
											if (data.astronaut == 0) {
												data.astronaut = 1;
												player.sendMessage(
														main.color("&9&l(!) &rYou unlocked &eAstronaut Outfit!"));
												player.sendTitle(main.color("&e&lUNLOCKED"),
														main.color("&eAstronaut Outfit"));
											} else {
												player.sendMessage(main.color(
														"&9&l(!) &rYou recieved &e25 Tokens &rfor a duplicate item"));
												player.sendTitle(main.color("&c&lDUPLICATE"),
														main.color("&eAstronaut Outfit"));
												data.tokens += 25;
											}
											Location newLoc = new Location(player.getWorld(), loc.getX() + 0.5,
													loc.getY() + 1, loc.getZ() + 0.5);
											helper(newLoc);
											helper(newLoc);
											helper(newLoc);
											helper(newLoc);
											helper(newLoc);
										} else if (chance > 5 && chance <= 20) {
											if (data.santaoutfit == 0) {
												data.santaoutfit = 1;
												player.sendMessage(main.color("&9&l(!) &rYou unlocked &c&lSanta Outfit!"));
												player.sendTitle(main.color("&e&lUNLOCKED"),
														main.color("&c&lSanta Outfit"));
											} else {
												player.sendMessage(main.color(
														"&9&l(!) &rYou recieved &e50 Tokens &rfor a duplicate item"));
												player.sendTitle(main.color("&c&lDUPLICATE"),
														main.color("&c&lSanta Outfit"));
												data.tokens += 50;
											}
											Location newLoc = new Location(player.getWorld(), loc.getX() + 0.5,
													loc.getY() + 1, loc.getZ() + 0.5);
											Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc,
													EntityType.FIREWORK);
											FireworkMeta fwm = fw.getFireworkMeta();
											
											fwm.setPower(1);
											fwm.addEffect(
													FireworkEffect.builder().withColor(Color.RED).flicker(true).build());
											
											fw.setFireworkMeta(fwm);
										} else if (chance > 20 && chance <= 40) {
											data.melon += 14;
											player.sendMessage(main.color("&9&l(!) &rYou unlocked &e14 Melons!"));
											player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&e14 Melons"));
											Location newLoc = new Location(player.getWorld(), loc.getX() + 0.5,
													loc.getY() + 1, loc.getZ() + 0.5);
											Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc,
													EntityType.FIREWORK);
											FireworkMeta fwm = fw.getFireworkMeta();
											
											fwm.setPower(1);
											fwm.addEffect(
													FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());
											
											fw.setFireworkMeta(fwm);
										} else if (chance > 40 && chance <= 60) {
											data.melon += 20;
											player.sendMessage(main.color("&9&l(!) &rYou unlocked &e20 Melons!"));
											player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&e20 Melons"));
											Location newLoc = new Location(player.getWorld(), loc.getX() + 0.5,
													loc.getY() + 1, loc.getZ() + 0.5);
											Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc,
													EntityType.FIREWORK);
											FireworkMeta fwm = fw.getFireworkMeta();
											
											fwm.setPower(1);
											fwm.addEffect(
													FireworkEffect.builder().withColor(Color.ORANGE).flicker(true).build());
											
											fw.setFireworkMeta(fwm);
										} else if (chance > 60 && chance <= 80) {
											data.paintball += 23;
											player.sendMessage(main.color("&9&l(!) &rYou unlocked &e23 Paintballs!"));
											player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&e23 Paintballs"));
											Location newLoc = new Location(player.getWorld(), loc.getX() + 0.5,
													loc.getY() + 1, loc.getZ() + 0.5);
											Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc,
													EntityType.FIREWORK);
											FireworkMeta fwm = fw.getFireworkMeta();
											
											fwm.setPower(1);
											fwm.addEffect(
													FireworkEffect.builder().withColor(Color.BLUE).flicker(true).build());
											
											fw.setFireworkMeta(fwm);
										} else {
											data.paintball += 17;
											player.sendMessage(main.color("&9&l(!) &rYou unlocked &e17 Paintballs!"));
											player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&e17 Paintballs"));
											Location newLoc = new Location(player.getWorld(), loc.getX() + 0.5,
													loc.getY() + 1, loc.getZ() + 0.5);
											Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc,
													EntityType.FIREWORK);
											FireworkMeta fwm = fw.getFireworkMeta();
											
											fwm.setPower(1);
											fwm.addEffect(
													FireworkEffect.builder().withColor(Color.BLUE).flicker(true).build());
											
											fw.setFireworkMeta(fwm);
										}
										
										Bukkit.getScheduler().cancelTask(animate);
										// newLoc.getWorld().spawnEntity(newLoc, fw.getType());
										stand.remove();
										main.getGameManager().chestCanOpen = false;
										data.mysteryChests--;
										
										if (data != null && main.msHologram.get(player) != null) {
											if (player.getWorld() == main.getLobbyWorld()) {
												EntityArmorStand stand = main.msHologram.get(player);
												PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(
														stand.getId());
												((CraftPlayer) player).getHandle().playerConnection
														.sendPacket(destroyPacket);
												loc = new Location(main.getLobbyWorld(), 194.520, 115.7, 641.500);
												
												WorldServer s = ((CraftWorld) loc.getWorld()).getHandle();
												stand = new EntityArmorStand(s);
												
												stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
												stand.setCustomName(
														main.color("&e&l" + data.mysteryChests + " &eto open!"));
												stand.setCustomNameVisible(true);
												stand.setGravity(false);
												stand.setInvisible(true);
												PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(
														stand);
												((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
												main.msHologram.put(player, stand);
												main.getDataManager().saveData(data);
											}
										}
									}
								}.runTaskLater(main, 100);
							}
						}));
			} else {
				contents.set(0, 0, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14),
						"" + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "No MysteryChests", "",
						"" + ChatColor.RESET + ChatColor.GRAY + "Craft MysteryChests or find them by playing",
						"" + ChatColor.RESET + ChatColor.GRAY + "matches to unlock exciting cosmetics!"), e-> {}));
			}
			contents.set(1, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WORKBENCH),
					"" + ChatColor.RESET + ChatColor.YELLOW + "Craft MysteryChest", "", "" + ChatColor.RESET
							+ ChatColor.RESET + "Click to craft 1 MysteryChest for " + ChatColor.YELLOW + "100 Tokens"),
					e -> {
						if (data.tokens >= 100) {
							data.tokens -= 100;
							data.mysteryChests++;
							main.LobbyBoard(player);
							player.sendMessage(main.color("&9&l(!) &rYou crafted &e1 MysteryChest!"));
						} else
							player.sendMessage(main.color("&c&l(!) &rYou do not have enough to craft a MysteryChest!"));

						inv.close(player);
					}));
		}

	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

	private void helper(Location newLoc) {
		Bukkit.getScheduler().runTaskLater(main, () -> {
			Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc, EntityType.FIREWORK);
			FireworkMeta fwm = fw.getFireworkMeta();

			fwm.setPower(1);
			fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());

			fw.setFireworkMeta(fwm);
		}, 15);
	}

}
