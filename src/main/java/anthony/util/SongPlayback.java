package anthony.util;

import org.bukkit.entity.Player;

/**
 * One player listening to one song, once through: a wait, then the song from its first note to its last. Call
 * {@link #advance(Player)} once a tick until it says the song is over. Holds three numbers, so it costs nothing to keep.
 */
public final class SongPlayback {

	private final NoteBlockSong song;
	private final float volume;
	private int wait;
	private int tick;
	private int note;

	/**
	 * @param volume     how loud the song is next to the game's other sounds
	 * @param delayTicks how long to wait before the first note
	 */
	public SongPlayback(NoteBlockSong song, float volume, int delayTicks) {
		this.song = song;
		this.volume = volume;
		this.wait = delayTicks;
	}

	/** Plays what is due this tick to the player. Returns false once the song is over, true while there is more to come. */
	public boolean advance(Player player) {
		if (wait > 0) {
			wait--;
			return true;
		}
		note = song.playDue(player, note, tick, volume);
		return ++tick <= song.length();
	}

	/** The same, with the notes going to an output instead of a player. */
	public boolean advance(NoteBlockSong.Output output) {
		if (wait > 0) {
			wait--;
			return true;
		}
		note = song.playDue(output, note, tick, volume);
		return ++tick <= song.length();
	}
}
