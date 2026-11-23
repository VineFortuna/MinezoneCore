package anthony.SuperCraftBrawl.npcs;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameManager;
import anthony.SuperCraftBrawl.gui.ClassicModeGUI;
import anthony.SuperCraftBrawl.gui.SCBModeSelectGUI;
import anthony.SuperCraftBrawl.gui.fishing.FishingGUI;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCSlot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;

public class NPCManager implements Listener {

	private Core main;
	private NPCLib npcLib;

	// NPCS HERE:
    private net.jitse.npclib.api.NPC fishing, scbClassic, flagwars, socialMedia, parkour, survey;
	private List<String> surveyFrameGreen, surveyFrameWhite;
	private BukkitTask surveyTask;

	private static final String SURVEY_URL = "https://docs.google.com/forms/d/e/1FAIpQLSfPqu6WDrVQW59YjzzVT0O5ZWa-8cHibHt3w7MBlHjInpOWyA/viewform?usp=dialog";

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
		Skin angelinoSkin = new Skin(
				"ewogICJ0aW1lc3RhbXAiIDogMTc5NTM4OTExMzYyOCwKICAicHJvZmlsZUlkIiA6ICI3OTUyYWI4MTM5NTE0MDViODdlYjAyMmMzYzZiYzkyNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmdlbGlubzExIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNjZDhmMDI1MDE3ODVkMTZiMDkzZjRkNjU5Njk4YTc5ZmNkNTBhMDA5NGY1MzM0OWQ0OGQ2NjMyZmM2ZTEzMmQiCiAgICB9CiAgfQp9",
				"NUFSw3okBHfms33ztfGV2WK2QYjNuZBOWxAYvAeoXQDBPT8bqycyn/fIyOUFnqOyh1ph63gzNnAfD4AmA+KDKbDaXiIkVGNvfTHiAuoe8pnWs7oY2+o09S/On0GsOj/DvppPX9oWNcggqGN6N5nW21BKN6WPsb/g8KE1kWoi/gfLLFcZEp5EJa8vUq2/6c+emefFvMQPr1PnqtJTZN1qCpqhjtr98oOM26TE5TLIUE657mWoLbK/UjXdbdIm6cnxcdulZJVIlWyM18v6Ade4f1KsOoFZ0EIyxtvYU9RZFQYK+/IydnANPbFIrvf1PvXAB4vTMho8102xAGHZezPxBeV80TkrertZ9IFE3XkV5qIJTVfTczeiM1gn5Z4cTx+agFEhvOhjTeIaiixKqei6Z719lTMgESMI7ZxMq/byTtrNP/759MqrLW6OOsa9PSUGoFSXx1tBn1wRLohSZyciAbQqlMNR7pRjUEeFt5Y7jsS4wnNS0QsBQyVjUNiwyY/ZR9u8ca+7fZ+w/hQXqhP46YoKm+/VrltDoufy2T02O7JwXB2g1CpZsv7dbcXRysB7xZD3mcaHoZdUVL7JUlaTfR13mJubZt5bbPblHGojVX4TodzROvAZ5ekwJ4/EN8rqkoTf4pjAH2HTdsO6Fq0vWFtHAti5fl7TUuwHJGrFmGc=");
		Skin drewpSkin = new Skin(
				"ewogICJ0aW1lc3RhbXAiIDogMTc5NTM4ODQ0NTgxNSwKICAicHJvZmlsZUlkIiA6ICIyMzYwZTc3MzU0Y2U0MjZmYmQ2OTc4Njg1ZDdhYmJkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJkcmV3cCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kODEwM2JlNWJmZGU2ODNjMTU5YzM3MzkzMWU4N2VkOTViM2Y3ZThiMDg2NDVmMWUzZTg1ZWM3NGZhOWI1NDciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83YzQxOGRmYmQzNzQxMmE1NWU5ZjE0MjVjOTgwN2E1OTFhZDEwY2Y3MWIxZWRiNTc2YTU4N2JlOTExNGMyNzdmIgogICAgfQogIH0KfQ==",
				"NnC5yUHd5BlzC3JkqCKBJ2tOvCiG9zaEUgrHvFXjTnSrg5EecK/kztXXtqEuh/LUOcc2hGdzADFJ95H4e4mWwgSmoM7RXTfsQiyEvvvErWwkzvxM++M6Ehmq+0MIJ9AWo3ZVjEAcpkZxs8Qhz/dFX5uKxn9pZzCkdeDg7F0uZnRp0PgLXqwToej9CoW2ZlXYAcQmN4eAp9J4DzLrOZszKUDiYDYAgo9bPGCW8Jv3L4vKLaBcTp67r7barVCRSEFq+bqOIgB2lz8dUNa95mDAgRYQBEAKMeosUBfLPDepFEGAD1dsHMqgTG4JgTvlRMXMyMWzziGTW4uNwLocEJS9JmQCBX2Dtw6Rrtcc9BqMFhij29XCGhuI9PBEOjVyxpS2yJHvN0dmKa7JtPnBxXnUhJcG+YUWdeuC9WcdrxNyzSJdhVVOyr7ilXDi3riQOrJQX3Hh6f8RY1a0xOZ67LdfHMGayvA4didT5Lc5ziRYxlfXAk0z5C9JuknhOICrmuN/CsqpID+qrxGag4o+/OC6E44qyF6vFa0Z4JYI/VtKdsr6fmnZC0nTurnXnsnfF4F8yX3bgbdqLJkjVfE3+UBHluRDk/Y+GW9U1CurlXav7XQXmY+O6up9FjEn9ZpAM6iRxMJpIgkArV5olHYTbsBEi4u3Wt3HYj3w0yvozL33WNw=");
		Skin flagWarsSkin = new Skin(
				"ewogICJ0aW1lc3RhbXAiIDogMTc4MjM0MzczNjcxNCwKICAicHJvZmlsZUlkIiA6ICI0N2Q1ZjY1ZWQyOWM0ZTU0OWRmNjlmNGNiZmExNTg3OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJQaWtlcnkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmIxMjc3Zjk5ZjJkZmU1MWQ0YjhkNTU4OTcwYmI1NTMyYTk1OWE3NGIzYmMyM2ZmMWRiZjA3ODljZGQ0NDVlOCIKICAgIH0KICB9Cn0=",
				"IFTvnqG3xZCYkQ6fe3U8IcVsbd+RkfFuHJv2uK21cnwIhaie0ABMGDbLgMTcutfZo+ujtxAMaAJYdljyp4Gg5b34AJhhGTo6dyuzZ0lmH7x73GAA5IstrfuVNK7vSIFnP/5I0C5zjK5joW3NqtkxXzRPwgxbP6NuuSDhiwOYQrTEGPoHCq5RFyaJ5LSpiqUMNU+i+vynlWNLnE7p90ZbSholPp4oB+DvP1lDy+n2xD8lQLrJeDHa/MtDr+C6oZZx5pkULOCV5iZfI/RDjhodA6RZAu6/Ij0o0YT2zPXJQBb6OT5oHr/Xl2ZgTlKVY4g6xYjmpZbPzkyGlK6meP6RMq9fKukMtjzTL9hAdMwWa7Wdll1MM6ynoxMF5nIhHeUWkRBocfYVGpsXQuGBCyWgoCYJRzkJT7O9uwKUUncn9mRG4ZbU+hwCQth30hUOfqZVNOmLiYeD0eWqqB0/XwOwK8rnYoNkhtPW3EedSMQqhvI9XcV/XZNTzC4c93waPt+yPtTqVMX/ReBtavaQMFKAqtSRWPBpA+fVMaBGkkdI+ZEU80QaSDQ3oQJLq3kza0kgJx3/UPwt+cIMwpi3g3nt5JJk1zMg7SEeiYPsriZ2VeeWmWylcmrlLz7yqhouTcHdVRKzd6JK6I+xzPxX7Nohmrk24d9Xo7cZQpvy7ItF0XI=");

		scbClassic = npcLib.createNPC(Arrays.asList(main.color("&6&lSUPER CRAFT BROS"), main.color("&fModes: &aClassic, Duels"),
				main.color("&eClick to Play!")));
		scbClassic.setLocation(new Location(main.getLobbyWorld(), 191.5, 106, 657.5, 174, 2));
		scbClassic.setSkin(sethblingSkin);
		scbClassic.setItem(NPCSlot.MAINHAND, ItemHelper.create(Material.COMPASS));
		scbClassic.create();

		flagwars = npcLib.createNPC(Arrays.asList(main.color("&6&lFLAG WARS &r&lNEW!"), main.color("&rMode: &aSolo"),
				main.color("&eClick to Play!")));
		flagwars.setLocation(new Location(main.getLobbyWorld(), 187.5, 106, 657.5, -174, 3));
		flagwars.setSkin(flagWarsSkin);
		flagwars.setItem(NPCSlot.MAINHAND, ItemHelper.createFlagWarsBanner());
		flagwars.create();

		parkour = npcLib.createNPC(Arrays.asList(main.color("&6&lPARKOUR"), main.color("&eClick to Play!")));
		parkour.setLocation(new Location(main.getLobbyWorld(), 165.5, 109, 670.5, -38f, 2.5f));
		parkour.setSkin(drewpSkin);
		parkour.create();

		socialMedia = npcLib.createNPC(Arrays.asList("" + ChatColor.AQUA + ChatColor.BOLD + "View Social Medias", "",
				main.color("&7Click to check the list!")));
		socialMedia.setLocation(new Location(main.getLobbyWorld(), 192.962, 115.5, 632.989, 137, 10));
		socialMedia.setSkin(sethblingSkin);
		socialMedia.create();

		fishing = npcLib.createNPC(Arrays.asList(main.color("&6&lFISHING"), main.color("&eClick to view!")));
		fishing.setLocation(new Location(main.getLobbyWorld(), 215.5, 106, 665.5, -0.3f, 0.5f));
		fishing.setSkin(fishermanSkin);
		fishing.setItem(NPCSlot.MAINHAND, ItemHelper.create(Material.FISHING_ROD));
		fishing.create();

		// Two shared immutable frames, the reward line flips between green and white
		surveyFrameGreen = Arrays.asList(main.color("&6&lSERVER SURVEY"), main.color("&a300 Token Reward!"),
				main.color("&eClick to view!"));
		surveyFrameWhite = Arrays.asList(main.color("&6&lSERVER SURVEY"), main.color("&f300 Token Reward!"),
				main.color("&eClick to view!"));
		survey = npcLib.createNPC(surveyFrameGreen);
		survey.setLocation(new Location(main.getLobbyWorld(), 184.5, 108, 640.5, -155.3f, -0.2f));
		survey.setSkin(angelinoSkin);
		survey.create();
		animateSurvey();
	}

	/** Every 0.5 seconds the survey reward line swaps color. Cancelled in {@link #shutdown()}. */
	private void animateSurvey() {
		surveyTask = new BukkitRunnable() {
			boolean white = false;

			@Override
			public void run() {
				white = !white;
				survey.setText(white ? surveyFrameWhite : surveyFrameGreen);
			}
		}.runTaskTimer(main, 10L, 10L);
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
			new SCBModeSelectGUI(main).inv.open(player);
		} else if (e.getNPC() == flagwars) {
			anthony.flagwars.FWModeSelectGUI.openOrJoin(main.getFwGameManager(), player);
		} else if (e.getNPC() == parkour) {
            SoundManager.playClickSound(player);
            mainParkour(player);
		} else if (e.getNPC() == survey) {
			TextComponent message = new TextComponent(main.color("&5&l(!) "));
			TextComponent link = new TextComponent(main.color("&e&l&nCLICK HERE"));
			link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, SURVEY_URL));
			message.addExtra(link);
			message.addExtra(new TextComponent(main.color("&f to fill out our &o5min&f survey and claim your &a300 Tokens&f, "
					+ "and your chance to win 1 month of &6&lLEGENDARY&r&f rank! Survey ends &7&nNovember 30th")));
			player.spigot().sendMessage(message);
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

    private void mainParkour(Player player) {
        Location loc = new Location(main.getLobbyWorld(), 189.5, 106, 571.5, -179, 1);
        player.teleport(loc);
        player.sendMessage(main.color("&r&l(!) &rYou have been sent to &e&lMain"));
    }

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		// Spawn npcs when player joins server
		// if (main.lobbyWorld.getName().equals("lobbies")) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(main, () -> {
			Bukkit.getScheduler().runTask(main, () -> fishing.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> scbClassic.show(e.getPlayer()));
			//Bukkit.getScheduler().runTask(main, () -> socialMedia.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> flagwars.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> parkour.show(e.getPlayer()));
			Bukkit.getScheduler().runTask(main, () -> survey.show(e.getPlayer()));
		}, 20L);
		// }
	}

    public NPCLib getNpcLib() {
        return npcLib;
    }

    public void shutdown() {
        if (surveyTask != null) {
            surveyTask.cancel();
            surveyTask = null;
        }

        net.jitse.npclib.api.NPC[] all = { fishing, scbClassic, flagwars, socialMedia, parkour, survey };

        for (net.jitse.npclib.api.NPC npc : all) {
            if (npc == null) continue;

            // Hide from all players first (no-op if already hidden)
            try {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    npc.hide(p);
                }
            } catch (Throwable ignored) {}

            // Then destroy the underlying NPC
            try {
                npc.destroy(); // <-- use destroy(), not despawn()
            } catch (Throwable ignored) {}
        }

        // If your NPCLib doesn’t have a disable() method, just don’t call it.
        // (Nothing else needed here.)
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
