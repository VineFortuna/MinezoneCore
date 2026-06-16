package anthony.SuperCraftBrawl.mysterychest;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.MysteryChestsGUI;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class MysteryChestManager implements Listener {

    private Core main;
    public boolean chestCanOpen = false;

    public MysteryChestManager(Core main) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void mysteryChest(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.CHEST) return;

        if (player.getWorld() == main.getLobbyWorld()) {
            e.setCancelled(true);

            if (!chestCanOpen) {
                new MysteryChestsGUI(main, e.getClickedBlock().getLocation()).inv.open(player);
            } else {
                player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                        + "This MysteryChest is already in use!");
            }
        }
    }

    public void removeAndAddMysteryStand(Player player) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        if (data == null) return;

        World lobby = main.getLobbyWorld();
        if (lobby == null) return;

        if (player.getWorld() != lobby) return;

        EntityArmorStand oldStand = main.msHologram.get(player);

        if (oldStand != null) {
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(oldStand.getId());
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroyPacket);
        }

        Location loc = new Location(lobby, 198.5, 105.2, 650.5);

        WorldServer worldServer = ((CraftWorld) lobby).getHandle();
        EntityArmorStand newStand = new EntityArmorStand(worldServer);

        newStand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        newStand.setCustomName(main.color("&a" + data.mysteryChests + " &rto open!"));
        newStand.setCustomNameVisible(true);
        newStand.setGravity(false);
        newStand.setInvisible(true);

        PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(newStand);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(spawnPacket);

        main.msHologram.put(player, newStand);
    }
}