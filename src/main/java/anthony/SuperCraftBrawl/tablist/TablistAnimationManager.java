package anthony.SuperCraftBrawl.tablist;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.ChatComponentText;

import java.lang.reflect.Field;

public class TablistAnimationManager {

    private final Core main;
    private BukkitTask task;

    private static final ChatColor GOLD   = ChatColor.GOLD;
    private static final ChatColor YELLOW = ChatColor.YELLOW;
    private static final ChatColor BOLD   = ChatColor.BOLD;

    private static final int PER_LETTER_DELAY_TICKS = 6;
    private static final int FLASH_DELAY_TICKS      = 4;
    private static final int FLASH_TOGGLES          = 5;
    private static final int HOLD_DURATION_TICKS    = 400;

    private static final String WORD = "MINEZONE NETWORK";
    private static final int N = WORD.length();

    private static final int CENTER_PX = 154;  // 1.8 default center width
    private static final int OFFSET_PX = 0;    // quick nudge if your RP shifts width (use -2, +2, etc.)


    private enum Phase { FILL, FLASH, HOLD }

    // cache last frame so joiners see it instantly
    private volatile String currentHeader = "\n" + GOLD + "" + BOLD + WORD + "\n";

    public TablistAnimationManager(Core main) { this.main = main; }

    public void start() {
        stop();

        task = new BukkitRunnable() {
            Phase phase = Phase.FILL;

            int k = 0;
            int fillDelay = 0;
            int flashDelay;
            int flashRemaining;
            ChatColor flashColor;
            int holdTicks = 0;

            @Override
            public void run() {
                switch (phase) {
                    case FILL: {
                        if (--fillDelay <= 0) {
                            currentHeader = "\n" + buildPartialBold(WORD, k) + "\n";
                            broadcastHeaderFooter(currentHeader, buildFooter());

                            k++;
                            fillDelay = PER_LETTER_DELAY_TICKS;

                            if (k > N) {
                                phase = Phase.FLASH;
                                flashRemaining = FLASH_TOGGLES;
                                flashDelay = FLASH_DELAY_TICKS;
                                flashColor = GOLD;
                            }
                        }
                        break;
                    }
                    case FLASH: {
                        if (--flashDelay <= 0) {
                            currentHeader = "\n" + flashColor + "" + BOLD + WORD + "\n";
                            broadcastHeaderFooter(currentHeader, buildFooter());

                            flashColor = (flashColor == YELLOW ? GOLD : YELLOW);
                            flashDelay = FLASH_DELAY_TICKS;
                            flashRemaining--;

                            if (flashRemaining <= 0) {
                                currentHeader = "\n" + GOLD + "" + BOLD + WORD + "\n";
                                broadcastHeaderFooter(currentHeader, buildFooter());
                                phase = Phase.HOLD;
                                holdTicks = HOLD_DURATION_TICKS;
                            }
                        }
                        break;
                    }
                    case HOLD: {
                        if (--holdTicks <= 0) {
                            phase = Phase.FILL;
                            k = 0;
                            fillDelay = PER_LETTER_DELAY_TICKS;
                        }
                        break;
                    }
                }
            }
        }.runTaskTimer(main, 0L, 1L);

        applyToAll();
    }

    public void stop() {
        if (task != null) {
            try { task.cancel(); } catch (Exception ignored) {}
            task = null;
        }
    }

    /** Call this on PlayerJoinEvent so the tablist shows instantly. */
    public void applyTo(Player p) {
        sendHeaderFooter(p, currentHeader, buildFooter());
        Bukkit.getScheduler().runTask(main, () -> sendHeaderFooter(p, currentHeader, buildFooter()));
    }

    public void applyToAll() {
        for (Player p : Bukkit.getOnlinePlayers()) sendHeaderFooter(p, currentHeader, buildFooter());
    }

    // ---------- Footer ----------
    private String buildFooter() {
        final String PAD = "   "; // 3 spaces left + right
        final String PAD2 = " ";

        String l1 = ChatColor.RESET + PAD + ChatColor.GRAY + "/help"
                + ChatColor.WHITE + " for a list of commands" + PAD;
        String l2 = ChatColor.RESET + PAD2 + ChatColor.GRAY + "/store"
                + ChatColor.WHITE + " to purchase a rank" + PAD;
        String l3 = ChatColor.RESET + PAD + ChatColor.GRAY + "/discord"
                + ChatColor.WHITE + " to join our Discord" + PAD;
        String l4 = ChatColor.RESET + "" + ChatColor.YELLOW + "minezone.club";

        return "\n" + l1 + "\n" + l2 + "\n" + l3 + "\n\n" + l4 + "\n";
    }


    /** Centers a colored string in the tablist using MC 1.8 glyph widths. */
    private static String centerTab(String message) {
        int px = 0;
        boolean bold = false;
        boolean prevColorChar = false;

        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);

            if (prevColorChar) {                 // handling § codes
                prevColorChar = false;
                if (ch == 'l' || ch == 'L')      bold = true;
                else if (ch == 'r' || ch == 'R') bold = false;
                continue;
            }
            if (ch == ChatColor.COLOR_CHAR) { prevColorChar = true; continue; }

            int w = FontWidth.len(ch);
            if (bold && ch != ' ') w += 1;       // bold adds 1px to non-space
            px += w;

            if (i + 1 < message.length()) px++;  // 1px inter-char spacing (not after last char)
        }

        int toCompensate = (CENTER_PX + OFFSET_PX) - (px / 2);
        int spaceLen = FontWidth.SPACE_LEN + 1;  // space glyph + spacing
        int spaces = Math.max(0, toCompensate / spaceLen);

        StringBuilder sb = new StringBuilder(spaces + message.length());
        for (int i = 0; i < spaces; i++) sb.append(' ');
        sb.append(message);
        return sb.toString();
    }

    // ---------- Animation helpers ----------

    private static String buildPartialBold(String text, int k) {
        if (k <= 0)             return GOLD + "" + BOLD + text;
        if (k >= text.length()) return YELLOW + "" + BOLD + text;

        StringBuilder sb = new StringBuilder(text.length() + 16);
        sb.append(YELLOW).append(BOLD).append(text, 0, k);
        sb.append(GOLD).append(BOLD).append(text, k, text.length());
        return sb.toString();
    }

    private static void setHeaderFooter(PacketPlayOutPlayerListHeaderFooter packet,
                                        IChatBaseComponent header, IChatBaseComponent footer) throws Exception {
        Field a = packet.getClass().getDeclaredField("a");
        Field b = packet.getClass().getDeclaredField("b");
        a.setAccessible(true);
        b.setAccessible(true);
        a.set(packet, header);
        b.set(packet, footer);
    }

    private void sendHeaderFooter(Player p, String headerRaw, String footerRaw) {
        try {
            IChatBaseComponent header = new ChatComponentText(headerRaw);
            IChatBaseComponent footer = new ChatComponentText(footerRaw);
            PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
            setHeaderFooter(packet, header, footer);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void broadcastHeaderFooter(String headerRaw, String footerRaw) {
        for (Player p : Bukkit.getOnlinePlayers()) sendHeaderFooter(p, headerRaw, footerRaw);
    }

    // ---------- 1.8 font widths (no enum → no compile weirdness) ----------
    private static final class FontWidth {
        static final int SPACE_LEN = 4;

        static int len(char c) {
            switch (c) {
                // Most letters/digits are 5px
                case 'A': case 'B': case 'C': case 'D': case 'E': case 'G': case 'H':
                case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P':
                case 'Q': case 'R': case 'S': case 'U': case 'V': case 'W': case 'X':
                case 'Y': case 'Z':
                case 'a': case 'b': case 'c': case 'd': case 'e': case 'g': case 'h':
                case 'j': case 'k': case 'm': case 'n': case 'o': case 'p': case 'q':
                case 'r': case 's': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                case '@': case '#': case '$': case '%': case '^': case '&':
                case '*': case '+': case '=': case '/': case '\\':
                case '?': case '~': case '-': case '_':
                    return 5;

                // Narrow letters
                case 'I': return 3;
                case 'i': return 1;
                case 'l': return 1;
                case 'f': case 't': return 4;

                // Punctuation / brackets
                case '[': case ']': return 3;
                case '(': case ')': return 4;
                case '{': case '}': return 4;
                case '<': case '>': return 4;
                case '!': case ',': case '.': case ':': case ';': case '\'': case '|':
                    return 1;
                case '"': return 3;

                case ' ': return SPACE_LEN;

                default: return 4; // sensible default width
            }
        }
    }
}