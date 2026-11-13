package anthony.util;

/**
 * Centers a line of chat by measuring it in pixels (using the default Minecraft font) and padding it
 * with spaces. Pass text that already has real color codes (run it through Core#color first).
 * Only accurate for the default chat width and for plain letters, numbers and punctuation.
 */
public final class ChatCenter {

	/** Half of the default chat width, in pixels. */
	private static final int CENTER_PX = 154;
	/** A space is 3 pixels wide plus 1 pixel of spacing. */
	private static final int SPACE_PX = 4;

	private ChatCenter() {
	}

	/** The spaces to put in front of the text so it sits in the middle of the chat. */
	public static String padding(String text) {
		int toCompensate = CENTER_PX - pixelWidth(text) / 2;
		if (toCompensate <= 0)
			return "";

		StringBuilder padding = new StringBuilder();
		for (int px = 0; px < toCompensate; px += SPACE_PX)
			padding.append(' ');
		return padding.toString();
	}

	public static String center(String text) {
		return padding(text) + text;
	}

	/** Width of the text as the client draws it, ignoring color codes and counting bold's extra pixel. */
	public static int pixelWidth(String text) {
		int width = 0;
		boolean bold = false;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if (c == '§' && i + 1 < text.length()) {
				char code = Character.toLowerCase(text.charAt(++i));
				if (code == 'l')
					bold = true;
				else if (code == 'r' || (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f'))
					bold = false; // colors and reset also end bold
				continue;
			}

			width += glyphWidth(c) + 1 + (bold ? 1 : 0); // +1 spacing between characters
		}
		return width;
	}

	private static int glyphWidth(char c) {
		switch (c) {
			case 'i': case '!': case ':': case ';': case '|': case '.': case ',': case '\'':
				return 1;
			case 'l': case '`':
				return 2;
			case ' ': case 'I': case 't': case '[': case ']': case '"':
				return 3;
			case 'f': case 'k': case '(': case ')': case '*': case '<': case '>': case '{': case '}':
				return 4;
			case '@': case '~':
				return 6;
			default:
				return 5;
		}
	}
}
