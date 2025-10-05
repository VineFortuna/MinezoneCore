package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.MysteryChestAnimation;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MysteryChestsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;
    private Location loc;

    public MysteryChestsGUI(Core main, Location loc) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(3, 9)
                .title("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "MysteryChest")
                .build();
        this.main = main;
        this.loc = loc;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        if (data != null) {
            if (data.mysteryChests > 0) {
                contents.set(0, 0, ClickableItem.of(
                        ItemHelper.setDetails(
                                new ItemStack(Material.ENDER_CHEST, data.mysteryChests),
                                "" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "MysteryChest",
                                "",
                                "" + ChatColor.RESET + ChatColor.YELLOW + "You have " + data.mysteryChests
                                        + (data.mysteryChests > 1 ? " Chests" : " Chest") + " to Open!"
                        ),
                        e -> {
                            // guard
                            if (data.mysteryChests <= 0) return;

                            inv.close(player);

                            // optional global re-entry guard
                            if (main.getGameManager() != null) {
                                if (main.getGameManager().chestCanOpen) {
                                    player.sendMessage(main.color("&c&l(!) &rPlease wait, a chest is already opening."));
                                    return;
                                }
                                main.getGameManager().chestCanOpen = true;
                            }

                            // nice display location centered above the block
                            Location displayLoc = new Location(
                                    player.getWorld(),
                                    loc.getX() + 0.5,
                                    loc.getY() + 1.0,
                                    loc.getZ() + 0.5
                            );

                            // start cinematic animation (handles rewards & cleanup)
                            new MysteryChestAnimation(main, player, data, displayLoc).start();

                            // consume one chest immediately so UI/hologram reflect it
                            data.mysteryChests--;
                            main.getDataManager().saveData(data);
                            main.getScoreboardManager().lobbyBoard(player);
                            hologram(player);
                        }
                ));
            } else {
                contents.set(0, 0, ClickableItem.of(
                        ItemHelper.setDetails(
                                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14),
                                "" + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + "No MysteryChests",
                                "",
                                "" + ChatColor.RESET + ChatColor.GRAY + "Craft MysteryChests or find them by playing",
                                "" + ChatColor.RESET + ChatColor.GRAY + "matches to unlock exciting cosmetics!"
                        ),
                        e -> {}
                ));
            }

            contents.set(1, 4, ClickableItem.of(
                    ItemHelper.setDetails(
                            new ItemStack(Material.WORKBENCH),
                            "" + ChatColor.RESET + ChatColor.YELLOW + "Craft MysteryChest",
                            "",
                            "" + ChatColor.RESET + ChatColor.RESET + "Click to craft 1 MysteryChest for " + ChatColor.YELLOW + "100 Tokens"
                    ),
                    e -> {
                        if (data.tokens >= 100) {
                            data.tokens -= 100;
                            data.mysteryChests++;
                            main.getScoreboardManager().lobbyBoard(player);
                            player.sendMessage(main.color("&9&l(!) &rYou crafted &e1 MysteryChest!"));
                            hologram(player);
                        } else {
                            player.sendMessage(main.color("&c&l(!) &rYou do not have enough to craft a MysteryChest!"));
                        }
                        inv.close(player);
                    }
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        // no-op
    }

    private void hologram(Player player) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        if (data != null && main.msHologram.get(player) != null) {
            World lobby = main.getLobbyWorld();
            if (player.getWorld() == lobby) {
                EntityArmorStand stand = main.msHologram.get(player);
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(stand.getId());
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroyPacket);

                // fixed hologram position (adjust if needed)
                loc = new Location(lobby, 194.520, 115.7, 641.500);

                WorldServer s = ((CraftWorld) loc.getWorld()).getHandle();
                stand = new EntityArmorStand(s);

                stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
                stand.setCustomName(main.color("&e&l" + data.mysteryChests + " &eto open!"));
                stand.setCustomNameVisible(true);
                stand.setGravity(false);
                stand.setInvisible(true);

                PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

                main.msHologram.put(player, stand);
                main.getDataManager().saveData(data);
            }
        }
    }
}