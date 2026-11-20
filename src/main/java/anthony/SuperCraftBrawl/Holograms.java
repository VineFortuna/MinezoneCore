package anthony.SuperCraftBrawl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private boolean showingFlagWars = false;

	/** Same vertical spacing the original SCB-only hologram used - kept fixed so swapping stat sets never shifts/resizes the column. */
	private static final double[] STAT_LINE_Y = {107, 106.7, 106.3, 106.0, 105.7, 105.4, 105.1};

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
		spawnStatLines(scbLines());
		destroyBoards();
		alternateStats();
	}

	/**
	 * Swaps the personal stats hologram between SCB and Flag Wars every 10 seconds, never both
	 * at once - same idea the Leaderboard Settings toggle uses for the leaderboard holograms,
	 * just automatic here instead of click-driven.
	 */
	private void alternateStats() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!player.isOnline() || player.getWorld() != main.getLobbyWorld()) {
					cancel();
					return;
				}

				showingFlagWars = !showingFlagWars;
				destroyStatLines();
				spawnStatLines(showingFlagWars ? flagWarsLines() : scbLines());
			}
		}.runTaskTimer(main, 200L, 200L);
	}

	private List<String> scbLines() {
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) return Collections.emptyList();

		return Arrays.asList(
				"&e" + player.getName() + "'s Stats",
				"&6&lSUPER CRAFT BROS",
				"&rLevel: &a" + data.level,
				"&rWins: &a" + data.wins,
				"&rFlawless Wins: &a" + data.flawlessWins,
				"&rKills: &a" + data.kills,
				"&rMatch MVPs: &a" + data.matchMvps
		);
	}

	private List<String> flagWarsLines() {
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) return Collections.emptyList();

		return Arrays.asList(
				"&e" + player.getName() + "'s Stats",
				"&6&lFLAG WARS",
				"&rLevel: &a" + data.level,
				"&rWins: &a" + data.fwWins,
				"&rMatch MVPs: &a" + data.fwMatchMvps,
				"&rFlags Captured: &a" + data.fwFlagsCaptured,
				"&rKills: &a" + data.fwKills
		);
	}

	private void spawnStatLines(List<String> lines) {
		if (lines.isEmpty()) return;
		WorldServer s = ((CraftWorld) main.getLobbyWorld()).getHandle();

		for (int i = 0; i < lines.size() && i < STAT_LINE_Y.length; i++) {
			Location loc = new Location(main.getLobbyWorld(), 180.5, STAT_LINE_Y[i], 650.5);
			EntityArmorStand stand = new EntityArmorStand(s);

			stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
			stand.setCustomName(color(lines.get(i)));
			stand.setCustomNameVisible(true);
			stand.setGravity(false);
			stand.setInvisible(true);
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			playerStats.add(stand);
		}
	}

	private void destroyStatLines() {
		for (EntityArmorStand stand : playerStats) {
			PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(stand.getId());
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroyPacket);
		}
		playerStats.clear();
	}

	/*
	 * This method gets rid of all the holograms from player if they aren't in the
	 * lobby
	 */
	public void destroyBoards() {
		BukkitRunnable r = new BukkitRunnable() {

			@Override
			public void run() {
				if (player.getWorld() != main.getLobbyWorld()) {
					if (main.holograms.containsKey(player))
						main.holograms.remove(player);

					destroyStatLines();
				}
			}
		};
		r.runTaskTimer(main, 0, 1);
	}

    public void destroy(Player player) {
        Holograms h = main.holograms.remove(player);
        if (h != null) h.destroyStatLines();

        EntityArmorStand stand = main.msHologram.remove(player);
        if (stand != null) {
            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(stand.getId());
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroy);
        }
    }

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}

}
