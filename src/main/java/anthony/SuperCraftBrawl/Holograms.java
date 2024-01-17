package anthony.SuperCraftBrawl;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.playerdata.PlayerData;

public class Holograms {

	private Core main;
	private Player player;
	private List<EntityArmorStand> playerStats;

	/*
	 * This entire class keeps track of all the holograms the player should have in
	 * the lobby
	 */
	public Holograms(Core main, Player player) {
		this.main = main;
		this.player = player;
		this.playerStats = new ArrayList<EntityArmorStand>();
		giveHolograms(); // To give holograms to player in lobby when joining the server
	}

	private void giveHolograms() {
		scbPlayerStats();
		destroyBoards();
	}

	private void scbPlayerStats() {
		PlayerData data = main.getDataManager().getPlayerData(player);

		if (data != null) {
			Location loc = new Location(main.getLobbyWorld(), 193.500, 106, 660.500);
			WorldServer s = ((CraftWorld) loc.getWorld()).getHandle();
			EntityArmorStand stand = new EntityArmorStand(s);

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName(color("&e" + player.getName() + "'s Stats"));
			stand.setCustomNameVisible(true);
			stand.setGravity(false);
			stand.setInvisible(true);
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			playerStats.add(stand);

			loc = new Location(main.getLobbyWorld(), 193.500, 105.7, 660.500);
			stand = new EntityArmorStand(s);

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName("" + ChatColor.AQUA + ChatColor.BOLD + "SuperCraftBlocks");
			stand.setCustomNameVisible(true);
			stand.setGravity(false);
			stand.setInvisible(true);
			packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			playerStats.add(stand);

			loc = new Location(main.getLobbyWorld(), 193.500, 105.3, 660.500);
			stand = new EntityArmorStand(s);

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName(color("&eLevel: &r" + data.level));
			stand.setCustomNameVisible(true);
			stand.setGravity(false);
			stand.setInvisible(true);
			packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			playerStats.add(stand);

			loc = new Location(main.getLobbyWorld(), 193.500, 105.0, 660.500);
			stand = new EntityArmorStand(s);

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName(color("&eWins: &r" + data.wins));
			stand.setCustomNameVisible(true);
			stand.setGravity(false);
			stand.setInvisible(true);
			packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			playerStats.add(stand);

			loc = new Location(main.getLobbyWorld(), 193.500, 104.7, 660.500);
			stand = new EntityArmorStand(s);

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName(color("&eFlawless Wins: &r" + data.flawlessWins));
			stand.setCustomNameVisible(true);
			stand.setGravity(false);
			stand.setInvisible(true);
			packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			playerStats.add(stand);

			loc = new Location(main.getLobbyWorld(), 193.500, 104.4, 660.500);
			stand = new EntityArmorStand(s);

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName(color("&eKills: &r" + data.kills));
			stand.setCustomNameVisible(true);
			stand.setGravity(false);
			stand.setInvisible(true);
			packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			playerStats.add(stand);

			loc = new Location(main.getLobbyWorld(), 193.500, 104.1, 660.500);
			stand = new EntityArmorStand(s);

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName(color("&eMatch MVPs: &r" + data.matchMvps));
			stand.setCustomNameVisible(true);
			stand.setGravity(false);
			stand.setInvisible(true);
			packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			playerStats.add(stand);
		}
	}

	/*
	 * This method gets rid of all the holograms from player if they aren't in the
	 * lobby
	 */
	private void destroyBoards() {
		BukkitRunnable r = new BukkitRunnable() {

			@Override
			public void run() {
				if (player.getWorld() != main.getLobbyWorld()) {
					if (main.holograms.containsKey(player))
						main.holograms.remove(player);

					for (EntityArmorStand stand : playerStats) {
						PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(stand.getId());
						((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroyPacket);
					}
					playerStats.clear();
				}
			}
		};
		r.runTaskTimer(main, 0, 1);
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}

}
