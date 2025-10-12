package anthony.SuperCraftBrawl.npcs;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameManager;
import anthony.SuperCraftBrawl.gui.GameSelectorGUI;
import anthony.SuperCraftBrawl.gui.fishing.FishingGUI;
import anthony.util.ItemHelper;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCSlot;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class NPCManager implements Listener {

	private Core main;
	private NPCLib npcLib;

	// NPCS HERE:
	private NPC fishing, scbClassic, scbDuels, socialMedia, parkour;

	public NPCManager(Core main) {
		this.main = main;
		this.npcLib = new NPCLib(main);
		load();
		Bukkit.getPluginManager().registerEvents(this, main);
	}

	private void load() {
		Skin sethblingSkin = new Skin(
				"ewogICJ0aW1lc3RhbXAiIDogMTYzOTc5ODIxNjM4OCwKICAicHJvZmlsZUlkIiA6ICI1NWEyZTcyZTAxNjE0Yzg1YjE5MWEwYjIyN2ZmNzU4YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXRoQmxpbmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWEzZTg3OTcxYTZmYjhlM2Q5NzZjN2RhYjYzMjU3MmVlMjAwZDI4MmE0NGVkYTVkMGEyYTNjNDE1MjQ2NGQyZiIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc5MTI3OTBmZjE2NGI5MzE5NmYwOGJhNzFkMGU2MjEyOTMwNDc3NmQwZjM0NzMzNGY4YTZlYWU1MDlmOGE1NiIKICAgIH0KICB9Cn0=",
				"tSB+tKhVQKKut1lm4VETD1l/ulDTI40F2f//L3d6pMZP2LGRp/B8BNbvqYjWdzY80aqw9t2zlvkAGQGFLOF1QPlCoA+OgG9kSS+OP7CZhM3fzTLjpIVyQQtKxCTJuh664Oj4JuzgfEYq/Yc7emUj7/asazPlNvgpY0BbS0pxXrhiN47UoNYjkxoBuyOX+uIkhmRzO+bQH1rRrMMdY6CYgubyz6QJXNJqg4zBgeRaUZHbtxlpy6FRcQPrvteacznCyfFa/cpkbbVLMPD9NQxCuB4bt29ygfVAgcO6+o5O/1JMJfA3yKpp0xuayZ6ILgIRzIBPP1943IAmegspjH8cs08Hhu7G9HuP7fyhvE3e8jMGzhhoeFmB4nZXDJxzT5ALsHjhzEzZFs3VazdiiaJy+7GwkqxQD6Y5NNUrMuHhvLo3nK9vs0kLjS/aQhBl4CkRIaqA3fJoE/ctpniJ5KVyaoSrF5YOn3QFp4qUg9JFr0KU7Ld3L2bcPwSFEIU7V3nIi7Kza9yx2JDOXOzmApfdDz1pYRB/g3D7WdVk1rRb/xPVUTlAjss4b9ijLhIHhcKGQyNl2dXPiIVnlx+FmJACnR40YZExLXEiFeutfUw1T8IP/Qqkg5158sKfqRPLwhvCqAjjqbDYtscPrNB1ApQCHTaL+UepPei+TFeHproVUHU=");
		Skin fishermanSkin = new Skin(
				"ewogICJ0aW1lc3RhbXAiIDogMTYxNDE3ODA2NzIzNywKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2MTY0YTNjM2JlYjFiYzAzMTAwMzBmMjgxNTU4OTE5ODEzZjBhMTVjMmQ3Y2I3NzIyNGMxZDk0ZmZmMjE0NDQiCiAgICB9CiAgfQp9",
				"GsrpTxzgihotEuasiq0cefZtiIzhyASNyAcXiHfFI+HqsC8aAZP/P6HYRa7zC3iQqMvDaVMZYFnmBui3B4KC49STqXy7UheGPT5xylV3dfjurinwpOUYJMNRcJYW9pLb98jVZ+bQW9LKHUdRbxgkGJxpJQQ+LQlROfpZ9DaM/fdVjImiMSUyUeCF5WyMxadRrEWeo+a1Cn25IrNWo+KWQy7OWLJONeZcuboZGqobuckZKfIRvW1Vtt2v45PXRI5ec/6YO6kr+yGVS51xlAZOzxjaaGACA+/5gHLeEu0Oka/hdGY0HdukqKcg3TXf1cZZ7qB/VCpnAuWOguTfXU09gh2BGRvOizsQpL5tRpg4lA/l5Q70t2jtj+79M2yRYC7LQs/SrUUIX6VJ3w8ylKKqTjLE+PVCmgwgW1bYX3tpt9xEhRJ5kPv47iX0DkVcGxE2QW3xd8gyJfE5BzeKhcXjEV+GJNa2NOiHnVIKJPuj+trJnOiKDainhcR00+Ncna5I9TomGm2r0TGUrO/bPqW7b2yvpBNUnAweUYjU/eG2E9uY5IzVPwO9M+PqulqD5Reqq3MW32WAcmLkO417Tn48tZnAmYjVoM5zb16ZN/J9LQgHMf9L0/ZgguBgKOx0UQeedJjwgkA9BZdL6LuTiLwfcEt2ZxSwvvAXfBWVmrSlVns=");

		scbClassic = npcLib.createNPC(Arrays.asList(main.color("&b&lSUPER CRAFT BROS"), main.color("&eMode: &bClassic"),
				main.color("&7Click to Play!")));
		scbClassic.setLocation(new Location(main.getLobbyWorld(), 191.5, 106, 657.5, 174, 2));
		scbClassic.setSkin(sethblingSkin);
		scbClassic.setItem(NPCSlot.MAINHAND, ItemHelper.create(Material.COMPASS));
		scbClassic.create();

		scbDuels = npcLib.createNPC(Arrays.asList(main.color("&b&lSUPER CRAFT BROS"), main.color("&eMode: &bDuels"),
				main.color("&7Click to Play!")));
		scbDuels.setLocation(new Location(main.getLobbyWorld(), 187.5, 106, 657.5, -174, 3));
		scbDuels.setSkin(sethblingSkin);
		scbDuels.setItem(NPCSlot.MAINHAND, ItemHelper.create(Material.COMPASS));
		scbDuels.create();

		parkour = npcLib.createNPC(Arrays.asList(main.color("&b&lPARKOUR"), main.color("&7Click to Play!")));
		parkour.setLocation(new Location(main.getLobbyWorld(), 183.5, 106, 655.5, -158, 2));
		parkour.setSkin(sethblingSkin);
		parkour.create();

		socialMedia = npcLib.createNPC(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "View Social Medias", "",
				main.color("&7Click to check the list!")));
		socialMedia.setLocation(new Location(main.getLobbyWorld(), 192.962, 115.5, 632.989, 137, 10));
		socialMedia.setSkin(sethblingSkin);
		socialMedia.create();

		fishing = npcLib.createNPC(Arrays.asList(main.color("&b&lFISHING"),
				main.color("&7Click to go fishing")));
		fishing.setLocation(new Location(main.getLobbyWorld(), 195.5, 106, 655.5, 162, 3));
		fishing.setSkin(fishermanSkin);
		fishing.setItem(NPCSlot.MAINHAND, ItemHelper.create(Material.FISHING_ROD));
		fishing.create();
	}

	private void update() { // Updates NPCs
		BukkitRunnable runnable = new BukkitRunnable() {
			int skywarsCount = GameManager.playercount.getOrDefault("sw-1", 0);
			int scbCount = GameManager.playercount.getOrDefault("scb-1", 0)
					+ GameManager.playercount.getOrDefault("scb-2", 0);

			@Override
			public void run() {
//				scb.setText(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "SUPER CRAFT BLOCKS",
//						main.color("&7Click to connect!"), "" + ChatColor.AQUA + scbCount + " Players"));
//				skywars.setText(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "SKYWARS",
//						main.color("&7Click to connect!"), "" + ChatColor.AQUA + skywarsCount + " Players"));
			}

		};
		runnable.runTaskTimer(main, 0, 100);
	}

	@EventHandler
	public void onNPCInteract(NPCInteractEvent e) {
		Player player = e.getWhoClicked();
		// Detect when players hit NPC and do something
//		if (e.getNPC() == scb) {
//			Bukkit.getMessenger().registerOutgoingPluginChannel(main, "BungeeCord");
//
//			ByteArrayOutputStream b = new ByteArrayOutputStream();
//			DataOutputStream out = new DataOutputStream(b);
//
//			try {
//				out.writeUTF("Connect");
//				out.writeUTF("scb-1");
//				player.sendMessage(main.color("&e&l(!) &rConnecting to &escb-1"));
//			} catch (Exception ex) {
//				player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &escb-1"));
//			}
//			player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
//		} else if (e.getNPC() == skywars) {
//			Bukkit.getMessenger().registerOutgoingPluginChannel(main, "BungeeCord");
//
//			ByteArrayOutputStream b = new ByteArrayOutputStream();
//			DataOutputStream out = new DataOutputStream(b);
//
//			try {
//				out.writeUTF("Connect");
//				out.writeUTF("sw-1");
//				player.sendMessage(main.color("&e&l(!) &rConnecting to &esw-1"));
//			} catch (Exception ex) {
//				player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &esw-1"));
//			}
//			player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
//		} else if (e.getNPC() == scbDuos) {
//			Bukkit.getMessenger().registerOutgoingPluginChannel(main, "BungeeCord");
//
//			ByteArrayOutputStream b = new ByteArrayOutputStream();
//			DataOutputStream out = new DataOutputStream(b);
//
//			try {
//				out.writeUTF("Connect");
//				out.writeUTF("scb-2");
//				player.sendMessage(main.color("&e&l(!) &rConnecting to &escb-2"));
//			} catch (Exception ex) {
//				player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &escb-2"));
//			}
//			player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
//		} else
		/*
		 * if (e.getNPC() == parkour) { if (main.p.hasPlayer(player)) {
		 * player.sendMessage(main.color("&r&l(!) &rHere is your reward: &e300 Tokens"))
		 * ; player.sendMessage(main.color("&r&l(!) &rTime Taken: &e" +
		 * main.p.time.get(player))); PlayerData data =
		 * main.getDataManager().getPlayerData(player); main.p.runnables.remove(player);
		 * main.p.time.remove(player); main.getParkour().players.remove(player);
		 * main.ResetPlayer(player); main.LobbyItems(player);
		 * player.setAllowFlight(true);
		 * 
		 * if (data != null) { data.tokens += 300;
		 * main.getScoreboardManager().lobbyBoard(player); main.SendPlayerToHub(player);
		 * } }
		 */
		if (e.getNPC() == fishing) {
			new FishingGUI(main, null).inv.open(player);
		} else if (e.getNPC() == scbClassic) {
			new GameSelectorGUI(main).inv.open(player);
		} else if (e.getNPC() == socialMedia) {
			player.sendMessage(main.color("&8&m-------&8[Social Media]&8&m-------"));
			player.sendMessage("");
			player.sendMessage(main.color("&eDiscord: &7https://discord.gg/FSZpmY9FZB"));
			player.sendMessage(main.color("&eStore: &7minezone.tebex.io"));
			player.sendMessage(main.color("&eYouTube: &7https://www.youtube.com/@minezone6480"));
			player.sendMessage(main.color("&eTwitter: &7https://twitter.com/MinezoneMC"));
			player.sendMessage(main.color("&eTikTok: &7https://www.tiktok.com/@minezonemc"));
			player.sendMessage("");
			player.sendMessage(main.color("&8&m----------------------------"));
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		// Spawn npcs when player joins server
		// if (main.lobbyWorld.getName().equals("lobbies")) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(main, () -> {
			Bukkit.getScheduler().runTask(main, () -> fishing.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> scbClassic.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> socialMedia.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> scbDuels.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> parkour.show(e.getPlayer()));
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
