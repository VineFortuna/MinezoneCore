package anthony.SuperCraftBrawl.christmas;

import anthony.SuperCraftBrawl.Core;
import anthony.util.NoteBlockSong;
import anthony.util.SongPlayback;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Jingle Bells in the main lobby for Christmas, once, every time a player joins the server. 1.8 has no Christmas
 * music (its discs are 13, cat, blocks, chirp, far, mall, mellohi, stal, strad, ward, 11 and wait), so it is a note
 * block song (a .nbs file inside the plugin) played to the player's ears with the note block sounds. Players hear it on
 * the "Jukebox/Note Blocks" volume slider of their game.
 *
 * <p>It starts a few seconds after they join and plays through once. It does not start again when they come back to the
 * lobby from a game, and it stops if they leave the lobby world while it is playing. /xmasmusic stops it for them.
 *
 * <p>The only state is one small object per player while their song plays, removed when it ends, when they leave the lobby
 * or when they quit, so there is nothing to leak. Turn the whole thing off after Christmas with {@link #ENABLED}.
 */
public final class LobbyMusic implements Listener, CommandExecutor {

	/** Master switch, like Halloween.ENABLED. Set to false after Christmas and the lobby is quiet again. */
	public static final boolean ENABLED = true;

	/** The song, inside the plugin. */
	private static final String SONG_FILE = "christmas-music/Jingle_Bells.nbs";
	private static final String SONG_NAME = "Jingle Bells";

	/** Quiet time before the song, so it doesn't start over the join messages. */
	private static final int JOIN_DELAY_TICKS = 60;
	/** How loud the song is, next to the game's other sounds. */
	private static final float VOLUME = 0.6f;

	private final Core main;
	private final World lobby;
	private final NoteBlockSong song;

	/** The players whose song is playing right now. Main thread only. */
	private final Map<UUID, SongPlayback> playing = new HashMap<UUID, SongPlayback>();
	private BukkitTask task;

	/** @throws IOException if the song can't be read, so the plugin starts without music (Core catches it) */
	public LobbyMusic(Core main, World lobby) throws IOException {
		this.main = main;
		this.lobby = lobby;
		this.song = readSong();

		Bukkit.getPluginManager().registerEvents(this, main);
		PluginCommand command = main.getCommand("xmasmusic");
		if (command != null) command.setExecutor(this);

		task = new BukkitRunnable() {
			@Override
			public void run() {
				tick();
			}
		}.runTaskTimer(main, 20L, 1L);
	}

	public void shutdown() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		playing.clear();
	}

	/** Reads the song out of the plugin. */
	private NoteBlockSong readSong() throws IOException {
		try (InputStream in = main.getResource(SONG_FILE)) {
			if (in == null) throw new IOException(SONG_FILE + " is missing from the plugin");
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int read;
			while ((read = in.read(buffer)) != -1) bytes.write(buffer, 0, read);
			return NoteBlockSong.fromNbs(bytes.toByteArray(), SONG_NAME);
		}
	}

	private void tick() {
		if (playing.isEmpty()) return;

		Iterator<Map.Entry<UUID, SongPlayback>> it = playing.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, SongPlayback> entry = it.next();
			Player player = Bukkit.getPlayer(entry.getKey());
			// Out when they have gone, or left the lobby, or the song has ended
			if (player == null || !player.isOnline() || player.getWorld() != lobby || !entry.getValue().advance(player)) {
				it.remove();
			}
		}
	}

	// ---------------------------------------------------------------- events

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld() == lobby) playing.put(player.getUniqueId(), new SongPlayback(song, VOLUME, JOIN_DELAY_TICKS));
	}

	@EventHandler
	public void onChangedWorld(PlayerChangedWorldEvent event) {
		// Leaving the lobby stops it. Coming back does not start it again: it is only for when they join the server.
		if (event.getFrom() == lobby) playing.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		playing.remove(event.getPlayer().getUniqueId());
	}

	// ---------------------------------------------------------------- /xmasmusic

	/** /xmasmusic stops the song for the player who is hearing it. */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can use this.");
			return true;
		}
		Player player = (Player) sender;
		if (playing.remove(player.getUniqueId()) != null) {
			player.sendMessage(main.color("&d&l(!) &rChristmas music stopped."));
		} else {
			player.sendMessage(main.color("&d&l(!) &rNothing is playing. &e" + SONG_NAME + " &rplays once when you join."));
		}
		return true;
	}
}
