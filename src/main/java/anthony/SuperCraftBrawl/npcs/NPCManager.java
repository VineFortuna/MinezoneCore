package anthony.SuperCraftBrawl.npcs;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameManager;
import anthony.SuperCraftBrawl.gui.GameSelectorGUI;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.skin.MineSkinFetcher;
import net.jitse.npclib.api.skin.Skin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

public class NPCManager implements Listener {

	private Core main;
	private NPCLib npcLib;

	// NPCS HERE:
	private NPC scb, skywars, scbDuos, parkour, scbModes;

	public NPCManager(Core main) {
		this.main = main;
		this.npcLib = new NPCLib(main);
		load();
		Bukkit.getPluginManager().registerEvents(this, main);
	}

	private void load() {
		int skinId = 277513;

		// if (main.lobbyWorld.getName().equals("lobbies")) {
		Skin skin2 = new Skin(
				"ewogICJ0aW1lc3RhbXAiIDogMTYzOTc5ODIxNjM4OCwKICAicHJvZmlsZUlkIiA6ICI1NWEyZTcyZTAxNjE0Yzg1YjE5MWEwYjIyN2ZmNzU4YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXRoQmxpbmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWEzZTg3OTcxYTZmYjhlM2Q5NzZjN2RhYjYzMjU3MmVlMjAwZDI4MmE0NGVkYTVkMGEyYTNjNDE1MjQ2NGQyZiIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc5MTI3OTBmZjE2NGI5MzE5NmYwOGJhNzFkMGU2MjEyOTMwNDc3NmQwZjM0NzMzNGY4YTZlYWU1MDlmOGE1NiIKICAgIH0KICB9Cn0=",
				"tSB+tKhVQKKut1lm4VETD1l/ulDTI40F2f//L3d6pMZP2LGRp/B8BNbvqYjWdzY80aqw9t2zlvkAGQGFLOF1QPlCoA+OgG9kSS+OP7CZhM3fzTLjpIVyQQtKxCTJuh664Oj4JuzgfEYq/Yc7emUj7/asazPlNvgpY0BbS0pxXrhiN47UoNYjkxoBuyOX+uIkhmRzO+bQH1rRrMMdY6CYgubyz6QJXNJqg4zBgeRaUZHbtxlpy6FRcQPrvteacznCyfFa/cpkbbVLMPD9NQxCuB4bt29ygfVAgcO6+o5O/1JMJfA3yKpp0xuayZ6ILgIRzIBPP1943IAmegspjH8cs08Hhu7G9HuP7fyhvE3e8jMGzhhoeFmB4nZXDJxzT5ALsHjhzEzZFs3VazdiiaJy+7GwkqxQD6Y5NNUrMuHhvLo3nK9vs0kLjS/aQhBl4CkRIaqA3fJoE/ctpniJ5KVyaoSrF5YOn3QFp4qUg9JFr0KU7Ld3L2bcPwSFEIU7V3nIi7Kza9yx2JDOXOzmApfdDz1pYRB/g3D7WdVk1rRb/xPVUTlAjss4b9ijLhIHhcKGQyNl2dXPiIVnlx+FmJACnR40YZExLXEiFeutfUw1T8IP/Qqkg5158sKfqRPLwhvCqAjjqbDYtscPrNB1ApQCHTaL+UepPei+TFeHproVUHU=");
		MineSkinFetcher.fetchSkinFromIdAsync(skinId, skin -> {
			scb = npcLib.createNPC(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "SUPER CRAFT BLOCKS",
					main.color("&7Click to connect!"),
					"" + ChatColor.AQUA + GameManager.playercount.getOrDefault("scb-1", 0) + " Players"));
			scb.setLocation(new Location(main.getLobbyWorld(), 0.474, 51, 7.559, -179, -2));
			scb.setSkin(skin);
			scb.create();

			scbDuos = npcLib.createNPC(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "SUPER CRAFT BLOCKS",
					main.color("&7&lDuos"), main.color("&7Click to connect!")));
			scbDuos.setLocation(new Location(main.getLobbyWorld(), 186.462, 113, 649.534, 179, -0));
			scbDuos.setSkin(skin2);
			scbDuos.create();

			scbModes = npcLib.createNPC(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "JOIN GAME", "",
					main.color("&7Click to join a SCB game")));
			scbModes.setLocation(new Location(main.getLobbyWorld(), 192.506, 113, 649.530, 179, -0));
			scbModes.setSkin(skin2);
			scbModes.create();

			parkour = npcLib.createNPC(
					Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + ChatColor.UNDERLINE + "PARKOUR FINISH",
							main.color("&7Click to claim reward!")));
			parkour.setLocation(new Location(main.getLobbyWorld(), 126.562, 115, 632.989, 179, -0));
			parkour.setSkin(skin);
			parkour.create();

			skywars = npcLib.createNPC(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "SKYWARS",
					main.color("&7Click to connect!"), "" + ChatColor.AQUA + "0 Players"));
			skywars.setLocation(new Location(main.getLobbyWorld(), 10.570, 51, 0.519, 90, -2));
			skywars.setSkin(skin);
			skywars.create();

			// FIX IT SO THIS UPDATES EVERY 10 SECONDS
		});

		update();
		// }
	}

	private void update() { // Updates NPCs
		BukkitRunnable runnable = new BukkitRunnable() {
			int skywarsCount = GameManager.playercount.getOrDefault("sw-1", 0);
			int scbCount = GameManager.playercount.getOrDefault("scb-1", 0)
					+ GameManager.playercount.getOrDefault("scb-2", 0);

			@Override
			public void run() {
				scb.setText(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "SUPER CRAFT BLOCKS",
						main.color("&7Click to connect!"), "" + ChatColor.AQUA + scbCount + " Players"));
				skywars.setText(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "SKYWARS",
						main.color("&7Click to connect!"), "" + ChatColor.AQUA + skywarsCount + " Players"));
			}

		};
		runnable.runTaskTimer(main, 0, 100);
	}

	@EventHandler
	public void onNPCInteract(NPCInteractEvent e) {
		Player player = e.getWhoClicked();
		// Detect when players hit NPC and do something
		if (e.getNPC() == scb) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(main, "BungeeCord");

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			try {
				out.writeUTF("Connect");
				out.writeUTF("scb-1");
				player.sendMessage(main.color("&e&l(!) &rConnecting to &escb-1"));
			} catch (Exception ex) {
				player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &escb-1"));
			}
			player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
		} else if (e.getNPC() == skywars) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(main, "BungeeCord");

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			try {
				out.writeUTF("Connect");
				out.writeUTF("sw-1");
				player.sendMessage(main.color("&e&l(!) &rConnecting to &esw-1"));
			} catch (Exception ex) {
				player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &esw-1"));
			}
			player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
		} else if (e.getNPC() == scbDuos) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(main, "BungeeCord");

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			try {
				out.writeUTF("Connect");
				out.writeUTF("scb-2");
				player.sendMessage(main.color("&e&l(!) &rConnecting to &escb-2"));
			} catch (Exception ex) {
				player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &escb-2"));
			}
			player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
		} else if (e.getNPC() == parkour) {
			if (main.p.hasPlayer(player)) {
				player.sendMessage(main.color("&r&l(!) &rHere is your reward: &e300 Tokens"));
				player.sendMessage(main.color("&r&l(!) &rTime Taken: &e" + main.p.time.get(player)));
				PlayerData data = main.getDataManager().getPlayerData(player);
				main.p.runnables.remove(player);
				main.p.time.remove(player);
				main.getParkour().players.remove(player);
				main.ResetPlayer(player);
				main.LobbyItems(player);
				player.setAllowFlight(true);

				if (data != null) {
					data.tokens += 300;
					main.LobbyBoard(player);
					main.SendPlayerToHub(player);
				}
			}
		} else if (e.getNPC() == scbModes) {
			new GameSelectorGUI(main).inv.open(player);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		// Spawn npcs when player joins server
		// if (main.lobbyWorld.getName().equals("lobbies")) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(main, () -> {
			Bukkit.getScheduler().runTask(main, () -> scb.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> skywars.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> scbDuos.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> parkour.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> scbModes.show(e.getPlayer()));
		}, 20L);
		// }
	}

	/*
	 * private Main main; private NPCLib npcLib; private List<MapNPC> npcs = new
	 * ArrayList<>(); private List<FrenzyNPC> npcs2 = new ArrayList<>(); private
	 * List<DuelNPC> npcs3 = new ArrayList<>(); public List<Maps> npcList = new
	 * ArrayList<>();
	 * 
	 * public NPCManager(Main main) { this.main = main; this.npcLib = new
	 * NPCLib(main); load(); Bukkit.getPluginManager().registerEvents(this, main);
	 * 
	 * }
	 * 
	 * public Main getMain() { return main; }
	 * 
	 * public NPCLib getNpcLib() { return npcLib; }
	 * 
	 * public void display(Player player) { for (MapNPC npc : npcs)
	 * npc.getNpc().show(player); }
	 * 
	 * public void addNpcs(MapNPC... npcs2) { for (MapNPC npc : npcs) {
	 * this.npcs.add(npc); } }
	 * 
	 * private void load() { Skin skin = new Skin(
	 * "ewogICJ0aW1lc3RhbXAiIDogMTYzOTc5ODIxNjM4OCwKICAicHJvZmlsZUlkIiA6ICI1NWEyZTcyZTAxNjE0Yzg1YjE5MWEwYjIyN2ZmNzU4YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXRoQmxpbmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWEzZTg3OTcxYTZmYjhlM2Q5NzZjN2RhYjYzMjU3MmVlMjAwZDI4MmE0NGVkYTVkMGEyYTNjNDE1MjQ2NGQyZiIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc5MTI3OTBmZjE2NGI5MzE5NmYwOGJhNzFkMGU2MjEyOTMwNDc3NmQwZjM0NzMzNGY4YTZlYWU1MDlmOGE1NiIKICAgIH0KICB9Cn0=",
	 * "tSB+tKhVQKKut1lm4VETD1l/ulDTI40F2f//L3d6pMZP2LGRp/B8BNbvqYjWdzY80aqw9t2zlvkAGQGFLOF1QPlCoA+OgG9kSS+OP7CZhM3fzTLjpIVyQQtKxCTJuh664Oj4JuzgfEYq/Yc7emUj7/asazPlNvgpY0BbS0pxXrhiN47UoNYjkxoBuyOX+uIkhmRzO+bQH1rRrMMdY6CYgubyz6QJXNJqg4zBgeRaUZHbtxlpy6FRcQPrvteacznCyfFa/cpkbbVLMPD9NQxCuB4bt29ygfVAgcO6+o5O/1JMJfA3yKpp0xuayZ6ILgIRzIBPP1943IAmegspjH8cs08Hhu7G9HuP7fyhvE3e8jMGzhhoeFmB4nZXDJxzT5ALsHjhzEzZFs3VazdiiaJy+7GwkqxQD6Y5NNUrMuHhvLo3nK9vs0kLjS/aQhBl4CkRIaqA3fJoE/ctpniJ5KVyaoSrF5YOn3QFp4qUg9JFr0KU7Ld3L2bcPwSFEIU7V3nIi7Kza9yx2JDOXOzmApfdDz1pYRB/g3D7WdVk1rRb/xPVUTlAjss4b9ijLhIHhcKGQyNl2dXPiIVnlx+FmJACnR40YZExLXEiFeutfUw1T8IP/Qqkg5158sKfqRPLwhvCqAjjqbDYtscPrNB1ApQCHTaL+UepPei+TFeHproVUHU="
	 * ); World lw = main.getLobbyWorld();
	 */

	/*
	 * addNpcs(new MapNPC(this, Maps.PileOfBodies, new Location(lw, -13.596, 162,
	 * -437.528, 50, 0), skin), new MapNPC(this, Maps.TheEnd, new Location(lw,
	 * -16.587, 162, -440.550, 50, 0), skin), new MapNPC(this, Maps.Stronghold, new
	 * Location(lw, -20.541, 162, -444.511, 50, 0), skin), new MapNPC(this,
	 * Maps.Village, new Location(lw, -21.653, 162, -449.526, 50, 0), skin), new
	 * MapNPC(this, Maps.NetherFortress, new Location(lw, -19.578, 162, -454.524,
	 * 50, 0), skin), new MapNPC(this, Maps.Mushroom, new Location(lw, -16.538, 162,
	 * -458.523, 50, 0), skin), new MapNPC(this, Maps.NightDragon, new Location(lw,
	 * -3.494, 162, -437.533, 50, 0), skin), new MapNPC(this, Maps.Candyland, new
	 * Location(lw, -0.500, 162, -440.518, 50, 0), skin), new MapNPC(this,
	 * Maps.Iceland, new Location(lw, 2.515, 162, -444.518, 50, 0), skin), new
	 * MapNPC(this, Maps.DragonsDescent, new Location(lw, 4.520, 162, -449.531, 50,
	 * 0), skin), new MapNPC(this, Maps.Clockwork, new Location(lw, 2.520, 162,
	 * -454.508, 50, 0), skin), new MapNPC(this, Maps.LostTemple, new Location(lw,
	 * -40.543, 162, -401.510, 50, 0), skin), new MapNPC(this, Maps.Marooned, new
	 * Location(lw, 22.468, 162, -411.552, 50, 0), skin), new MapNPC(this,
	 * Maps.Orbital, new Location(lw, 22.475, 162, -401.487, 50, 0), skin), new
	 * MapNPC(this, Maps.Apex, new Location(lw, 25.478, 162, -398.509, 50, 0),
	 * skin), new MapNPC(this, Maps.Revenge, new Location(lw, -43.521, 162,
	 * -398.475, 50, 0), skin), new MapNPC(this, Maps.Multiverse, new Location(lw,
	 * 25.469, 162, -414.578, 50, 0), skin), new MapNPC(this, Maps.Aperature, new
	 * Location(lw, 29.464, 162, -418.554, 50, 0), skin), new MapNPC(this,
	 * Maps.Atronach, new Location(lw, -40.529, 162, -411.578, 50, 0), skin), new
	 * MapNPC(this, Maps.Limbo, new Location(lw, -43.526, 162, -414.568, 50, 0),
	 * skin), new MapNPC(this, Maps.Treehouse, new Location(lw, -47.539, 162,
	 * -417.566, 50, 0), skin), new MapNPC(this, Maps.Gateway, new Location(lw,
	 * -3.507, 162, -375.505, 50, 0), skin), new MapNPC(this, Maps.Pokemob, new
	 * Location(lw, -13.575, 162, -375.539, 50, 0), skin), new MapNPC(this,
	 * Maps.Tropical, new Location(lw, 29.487, 162, -395.502, 50, 0), skin), new
	 * MapNPC(this, Maps.Frigid, new Location(lw, -47.526, 162, -394.467, 50, 0),
	 * skin), new MapNPC(this, Maps.JungleRiver, new Location(lw, 34.500, 162,
	 * -393.475, 50, 0), skin), new MapNPC(this, Maps.Mountain, new Location(lw,
	 * 34.541, 162, -419.569, 50, 0), skin), new MapNPC(this, Maps.Mansion, new
	 * Location(lw, -0.401, 162, -458.504, 50, 0), skin), new MapNPC(this,
	 * Maps.TempleOfMars, new Location(lw, 39.505, 162, -417.620, 50, 0), skin));
	 */

	// addNpcs(new MapNPC(this, new Location(main.getLobbyWorld(), -7.569, 143,
	// 19.495, 50, 0), skin));
	// addNpcs(new FrenzyNPC(this, new Location(lw, 1.5, 144, 19.5, 90, 10), skin));
	/*
	 * addNpcs(new MapNPC(this, new Location(main.getLobbyWorld(), 189.482, 112,
	 * 645.508), skin)); }
	 * 
	 * @EventHandler public void onPlayerJoin(PlayerJoinEvent e) { Player player =
	 * e.getPlayer(); display(player); }
	 * 
	 * public void updateRandomNpc() { for (MapNPC npc : npcs) { npc.update();
	 * return; } }
	 * 
	 * @EventHandler public void onInteract(NPCInteractEvent e) { Player player =
	 * e.getWhoClicked(); for (MapNPC npc : npcs) { if (npc.getNpc() == e.getNPC())
	 * { new ClassSelectorGUI(main).inv.open(player); return; } } }
	 */

}
