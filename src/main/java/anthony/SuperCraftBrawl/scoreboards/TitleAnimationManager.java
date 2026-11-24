package anthony.SuperCraftBrawl.scoreboards;

import anthony.SuperCraftBrawl.Core;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TitleAnimationManager {
    private final Core main;
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    private static final ChatColor GOLD   = ChatColor.GOLD;
    private static final ChatColor YELLOW = ChatColor.YELLOW;
    private static final ChatColor BOLD   = ChatColor.BOLD;

    private static final int PER_LETTER_DELAY_TICKS = 6;
    private static final int FLASH_DELAY_TICKS      = 4;
    private static final int FLASH_TOGGLES          = 5;
    private static final int HOLD_DURATION_TICKS    = 400;

    private static final String WORD = "MINEZONE";
    private static final int N = WORD.length();

    private enum Phase { FILL, FLASH, HOLD }

    public TitleAnimationManager(Core main) { this.main = main; }

    // ===== Christmas animation (temporary) =====
    // Set CHRISTMAS to false when Christmas is over to bring back the normal animation (startClassic below is
    // the original, untouched). The Christmas one can then be deleted, or kept for next year.
    private static final boolean CHRISTMAS = true;

    private static final ChatColor RED   = ChatColor.RED;
    private static final ChatColor WHITE = ChatColor.WHITE;

    private static final String XMAS_TEXT = "MERRY CHRISTMAS";
    private static final int XMAS_LETTERS = XMAS_TEXT.replace(" ", "").length();
    private static final int XMAS_CYCLE_TICKS         = 200; // it plays every 10 seconds (start to start)
    private static final int XMAS_INITIAL_REST_TICKS  = 20;  // MINEZONE shows for a second after joining, then the first one
    private static final int XMAS_LETTER_DELAY_TICKS  = 4;   // one character at a time
    private static final int XMAS_FLICKER_DELAY_TICKS = 5;   // red <-> white
    private static final int XMAS_FLICKER_TICKS       = 60;  // flickers for 3 seconds
    // MINEZONE shows for whatever is left of the 10 seconds
    private static final int XMAS_REST_TICKS = XMAS_CYCLE_TICKS - XMAS_LETTERS * XMAS_LETTER_DELAY_TICKS - XMAS_FLICKER_TICKS;

    private enum XmasPhase { REST, WRITE, FLICKER }

    public void start(Player player, FastBoard board) {
        if (CHRISTMAS) startChristmas(player, board);
        else startClassic(player, board);
    }

    /*
     * MINEZONE for a bit, then "MERRY CHRISTMAS" is written out in red one character at a time, flickers
     * between red and white for 3 seconds, then goes back to MINEZONE. Repeats every 10 seconds.
     */
    private void startChristmas(Player player, FastBoard board) {
        stop(player);

        BukkitTask task = new BukkitRunnable() {
            XmasPhase phase = XmasPhase.REST;
            boolean first = true;
            int wait = XMAS_INITIAL_REST_TICKS;
            int shown = 0;        // how many characters of the text are written
            int flickerLeft = 0;  // ticks of flickering left
            boolean white = true; // the next flicker colour

            @Override
            public void run() {
                if (!player.isOnline() || !main.getScoreboardManager().hasLobbyBoard(player)) {
                    cancel();
                    tasks.remove(player.getUniqueId());
                    return;
                }

                if (first) { // the lobby board starts out yellow, the resting title is gold like the normal animation's
                    first = false;
                    board.updateTitle(safeTitle32(GOLD.toString() + BOLD + WORD));
                }

                if (phase == XmasPhase.REST) {
                    if (--wait > 0) return;
                    phase = XmasPhase.WRITE;
                    shown = 0;
                    wait = 0; // the first character goes up straight away
                }

                if (phase == XmasPhase.WRITE) {
                    if (--wait > 0) return;

                    shown++;
                    // The space isn't a step of its own, it goes up together with the letter after it (MERRY -> MERRY C)
                    if (XMAS_TEXT.charAt(shown - 1) == ' ' && shown < XMAS_TEXT.length()) shown++;
                    board.updateTitle(safeTitle32(RED.toString() + BOLD + XMAS_TEXT.substring(0, shown)));
                    wait = XMAS_LETTER_DELAY_TICKS;

                    if (shown >= XMAS_TEXT.length()) {
                        phase = XmasPhase.FLICKER;
                        flickerLeft = XMAS_FLICKER_TICKS;
                        white = true;
                    }
                    return;
                }

                // FLICKER
                if (--wait > 0) return;

                if (flickerLeft <= 0) {
                    // Done: back to MINEZONE until the next time
                    board.updateTitle(safeTitle32(GOLD.toString() + BOLD + WORD));
                    phase = XmasPhase.REST;
                    wait = XMAS_REST_TICKS;
                    return;
                }

                board.updateTitle(safeTitle32((white ? WHITE : RED).toString() + BOLD + XMAS_TEXT));
                white = !white;
                flickerLeft -= XMAS_FLICKER_DELAY_TICKS;
                wait = XMAS_FLICKER_DELAY_TICKS;
            }
        }.runTaskTimer(main, 0L, 1L);

        tasks.put(player.getUniqueId(), task);
    }

    // ===== The normal animation (what the title does when it isn't Christmas) =====

    private void startClassic(Player player, FastBoard board) {
        stop(player);

        BukkitTask task = new BukkitRunnable() {
            Phase phase = Phase.FILL;

            int k = 0; // how many letters are YELLOW
            int fillDelay = PER_LETTER_DELAY_TICKS;

            // flash state
            int flashDelay;
            int flashRemaining;
            ChatColor flashColor;

            // hold state
            int holdTicks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !main.getScoreboardManager().hasLobbyBoard(player)) {
                    cancel();
                    tasks.remove(player.getUniqueId());
                    return;
                }

                switch (phase) {
                    case FILL: {
                        if (--fillDelay <= 0) {
                            board.updateTitle(safeTitle32(buildPartialBold(WORD, k)));
                            k++;
                            fillDelay = PER_LETTER_DELAY_TICKS;

                            if (k > N) {
                                // fully YELLOW — enter flashing: GOLD ↔ YELLOW
                                phase = Phase.FLASH;
                                flashRemaining = FLASH_TOGGLES;
                                flashDelay = FLASH_DELAY_TICKS;
                                flashColor = GOLD; // first flash after fill is GOLD
                            }
                        }
                        break;
                    }
                    case FLASH: {
                        if (--flashDelay <= 0) {
                            board.updateTitle(safeTitle32(flashColor.toString() + BOLD + WORD));
                            flashColor = (flashColor == YELLOW ? GOLD : YELLOW);
                            flashDelay = FLASH_DELAY_TICKS;
                            flashRemaining--;

                            if (flashRemaining <= 0) {
                                // end on GOLD then HOLD for a few seconds
                                board.updateTitle(safeTitle32(GOLD.toString() + BOLD + WORD));
                                phase = Phase.HOLD;
                                holdTicks = HOLD_DURATION_TICKS;
                            }
                        }
                        break;
                    }
                    case HOLD: {
                        if (--holdTicks <= 0) {
                            // restart cycle
                            phase = Phase.FILL;
                            k = 0;
                            fillDelay = PER_LETTER_DELAY_TICKS;
                        }
                        break;
                    }
                }
            }
        }.runTaskTimer(main, 0L, 1L);

        tasks.put(player.getUniqueId(), task);
    }

    public void stop(Player player) {
        BukkitTask t = tasks.remove(player.getUniqueId());
        if (t != null) t.cancel();
    }

    public void stopAll() {
        for (BukkitTask t : tasks.values()) try { t.cancel(); } catch (Exception ignored) {}
        tasks.clear();
    }

    // k letters YELLOW (bold), rest GOLD (bold)
    private static String buildPartialBold(String text, int k) {
        if (k <= 0)                 return GOLD + "" + BOLD + text;
        if (k >= text.length())     return YELLOW + "" + BOLD + text;

        StringBuilder sb = new StringBuilder(32);
        sb.append(YELLOW).append(BOLD).append(text, 0, k);
        sb.append(GOLD).append(BOLD).append(text, k, text.length());
        return sb.toString();
    }

    // keep within FastBoard’s 32-char raw limit
    private static String safeTitle32(String s) {
        if (s.length() <= 32) return s;
        String noBold = s.replace(BOLD.toString(), "");
        if (noBold.length() <= 32) return noBold;
        String plain = ChatColor.stripColor(noBold);
        if (plain.length() > 32) plain = plain.substring(0, 32);
        return plain;
    }
}
