package anthony.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * A note block song read from a Note Block Studio file (.nbs, versions 0 to 5, the format Note Block Studio and
 * the NoteBlockAPI plugin use), ready to be played to a player's ears with {@link Player#playSound}.
 *
 * Only what 1.8 can play is used: the notes inside the note block's two octaves (F#3 to F#5, a note outside it is moved
 * a whole number of octaves into it), and the instruments 1.8 has (harp, bass, bass drum, snare, click, bass guitar and
 * pling; the newer ones are played as the closest of those). Layer volume and note velocity are used, panning and
 * fine pitch are not. The notes are read once, into small arrays, so a song costs next to nothing to keep.
 *
 * A song never changes after it is read, so one can be played to any number of players at once.
 */
public final class NoteBlockSong {

	private static final long MAX_FILE_BYTES = 4L * 1024 * 1024;
	private static final int MAX_NOTES = 300000;
	/** Anything later than this (20 minutes) is left out. */
	private static final int MAX_TICKS = 20 * 60 * 20;
	/** The note block's lowest note, F#3, is key 33 in the file. */
	private static final int FIRST_KEY = 33;
	/** The note block has 25 notes. */
	private static final int KEYS = 25;
	/** A thick song can have a dozen notes on one tick. A client plays about 28 sounds at once, so the rest are left out. */
	private static final int MAX_NOTES_PER_TICK = 6;
	/** Quiet time after the last note, so it can ring out. */
	private static final int TAIL_TICKS = 20;

	/** By the file's instrument number: harp, double bass, bass drum, snare, click, guitar, flute, bell, chime, xylophone... */
	private static final Sound[] INSTRUMENTS = {
			Sound.NOTE_PIANO, Sound.NOTE_BASS, Sound.NOTE_BASS_DRUM, Sound.NOTE_SNARE_DRUM, Sound.NOTE_STICKS,
			Sound.NOTE_BASS_GUITAR, Sound.NOTE_PIANO, Sound.NOTE_PLING, Sound.NOTE_PLING, Sound.NOTE_PLING,
			Sound.NOTE_PLING, Sound.NOTE_PLING, Sound.NOTE_BASS, Sound.NOTE_PLING, Sound.NOTE_PIANO, Sound.NOTE_PLING
	};

	/** The pitch of each of the 25 notes: 0.5 for the lowest, 1 for the middle one, 2 for the highest. */
	private static final float[] PITCH = new float[KEYS];

	static {
		for (int i = 0; i < KEYS; i++) PITCH[i] = (float) Math.pow(2.0, (i - 12) / 12.0);
	}

	private final String name;
	/** The game tick (20 a second) every note is played on, from 0, never going back. */
	private final int[] at;
	private final byte[] instrument;
	private final byte[] note;
	private final float[] volume;
	private final int length;

	private NoteBlockSong(String name, int[] at, byte[] instrument, byte[] note, float[] volume, int length) {
		this.name = name;
		this.at = at;
		this.instrument = instrument;
		this.note = note;
		this.volume = volume;
		this.length = length;
	}

	/**
	 * Reads a song from a file. Its name is the title in the file, or if it has none the file's name without ".nbs".
	 *
	 * @throws IOException if it isn't a note block song, has no notes or is too big. A file that is cut short in the
	 *                     middle of its notes is fine, the song just ends where the file does
	 */
	public static NoteBlockSong fromNbs(File file) throws IOException {
		if (file.length() > MAX_FILE_BYTES) throw new IOException("the file is over 4 MB");
		return fromNbs(Files.readAllBytes(file.toPath()), nameOf(file));
	}

	/**
	 * Reads a song from the bytes of a file (from inside the plugin's jar, say).
	 *
	 * @param fallbackName the name to use if the file has no title
	 * @throws IOException as {@link #fromNbs(File)}
	 */
	public static NoteBlockSong fromNbs(byte[] data, String fallbackName) throws IOException {
		if (data.length > MAX_FILE_BYTES) throw new IOException("the file is over 4 MB");
		ByteBuffer in = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

		int version = 0;
		int layerCount;
		int tempo;
		String title;
		try {
			int first = in.getShort() & 0xFFFF;
			if (first == 0) { // the newer files start with a 0, then the version
				version = in.get() & 0xFF;
				in.get(); // how many of the vanilla instruments there are
				if (version >= 3) in.getShort(); // the length, which is worked out from the notes instead
			}
			layerCount = in.getShort() & 0xFFFF;
			title = readString(in);
			readString(in); // author
			readString(in); // original author
			readString(in); // description
			tempo = in.getShort() & 0xFFFF; // ticks a second, times 100
			in.position(in.position() + 3 + 20); // auto save, its minutes, time signature, five statistics
			readString(in); // the file it was imported from
			if (version >= 4) in.position(in.position() + 4); // loop, loop count, loop start
		} catch (BufferUnderflowException | IllegalArgumentException e) {
			throw new IOException("not a note block song, it ends inside the header");
		}
		if (tempo <= 0) tempo = 1000;
		double gameTicksPerSongTick = 2000.0 / tempo;

		// The notes: a jump of ticks, then for that tick a jump of layers, an instrument, a key (and in version 4 and up
		// a velocity, a panning and a fine pitch), until a jump of 0
		List<int[]> rows = new ArrayList<int[]>();
		boolean cutShort = false;
		try {
			int tick = -1;
			while (true) {
				int jump = in.getShort() & 0xFFFF;
				if (jump == 0) break;
				tick += jump;
				if (tick * gameTicksPerSongTick > MAX_TICKS) break;

				int layer = -1;
				while (true) {
					jump = in.getShort() & 0xFFFF;
					if (jump == 0) break;
					layer += jump;
					int inst = in.get() & 0xFF;
					int key = in.get() & 0xFF;
					int velocity = 100;
					if (version >= 4) {
						velocity = in.get() & 0xFF;
						in.get(); // panning
						in.getShort(); // fine pitch
					}
					if (rows.size() >= MAX_NOTES) throw new IOException("more than " + MAX_NOTES + " notes");
					rows.add(new int[]{tick, layer, inst, key, Math.min(velocity, 100)});
				}
			}
		} catch (BufferUnderflowException e) {
			cutShort = true;
		}

		// How loud each layer is (the file has the layers after the notes). Full volume if they are missing.
		float[] layerVolume = new float[Math.max(layerCount, 1)];
		java.util.Arrays.fill(layerVolume, 1f);
		if (!cutShort) {
			try {
				for (int i = 0; i < layerCount; i++) {
					readString(in); // name
					if (version >= 4) in.get(); // locked
					layerVolume[i] = Math.min(in.get() & 0xFF, 100) / 100f;
					if (version >= 2) in.get(); // panning
				}
			} catch (BufferUnderflowException | IOException e) {
				// the rest stay at full volume
			}
		}

		int count = 0;
		int[] at = new int[rows.size()];
		byte[] instrument = new byte[rows.size()];
		byte[] note = new byte[rows.size()];
		float[] volume = new float[rows.size()];
		for (int[] row : rows) {
			float loudness = (row[1] < layerVolume.length ? layerVolume[row[1]] : 1f) * row[4] / 100f;
			if (loudness <= 0f) continue; // a muted layer

			int n = row[3] - FIRST_KEY;
			while (n < 0) n += 12;
			while (n >= KEYS) n -= 12;

			at[count] = (int) Math.round(row[0] * gameTicksPerSongTick);
			instrument[count] = (byte) (row[2] < INSTRUMENTS.length ? row[2] : 0);
			note[count] = (byte) n;
			volume[count] = loudness;
			count++;
		}
		if (count == 0) throw new IOException("the song has no notes");

		int end = at[count - 1] + TAIL_TICKS;
		String shown = title.trim().isEmpty() ? fallbackName : title.trim();
		return new NoteBlockSong(shown, java.util.Arrays.copyOf(at, count), java.util.Arrays.copyOf(instrument, count),
				java.util.Arrays.copyOf(note, count), java.util.Arrays.copyOf(volume, count), end);
	}

	/** The song's name from its file: the file's name without ".nbs", with spaces for underscores. */
	private static String nameOf(File file) {
		String name = file.getName();
		if (name.toLowerCase().endsWith(".nbs")) name = name.substring(0, name.length() - 4);
		return name.replace('_', ' ').trim();
	}

	private static String readString(ByteBuffer in) throws IOException {
		int length = in.getInt();
		if (length < 0 || length > in.remaining()) throw new IOException("bad text in the file");
		byte[] bytes = new byte[length];
		in.get(bytes);
		return new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
	}

	public String name() {
		return name;
	}

	/** How many game ticks (20 a second) the song lasts, with a short tail. */
	public int length() {
		return length;
	}

	public int noteCount() {
		return at.length;
	}

	/** The game tick the note with this number is played on. */
	int tickOf(int index) {
		return at[index];
	}

	/**
	 * Plays the notes that are due, up to and including the given tick (0 is the song's start), starting from the note
	 * with the given number, to this player only. Call it once a tick with the tick going up by one.
	 *
	 * @return the number of the next note, to be passed in the next time
	 */
	public int playDue(Player player, int index, int tick, float master) {
		return due(player, null, index, tick, master);
	}

	/** The same, but the notes go to the given output instead of a player (for trying a song out without a server). */
	public int playDue(Output output, int index, int tick, float master) {
		return due(null, output, index, tick, master);
	}

	/** Where the notes of {@link #playDue(Output, int, int, float)} go. */
	public interface Output {
		/** The sound to play, how loud (0 to 1) and at what pitch (0.5 to 2). */
		void play(Sound sound, float volume, float pitch);
	}

	private int due(Player player, Output output, int index, int tick, float master) {
		Location where = null;
		int played = 0;
		while (index < at.length && at[index] <= tick) {
			if (played < MAX_NOTES_PER_TICK) {
				Sound sound = INSTRUMENTS[instrument[index]];
				float loudness = volume[index] * master;
				if (output != null) {
					output.play(sound, loudness, PITCH[note[index]]);
				} else {
					if (where == null) where = player.getLocation();
					player.playSound(where, sound, loudness, PITCH[note[index]]);
				}
				played++;
			}
			index++;
		}
		return index;
	}
}
