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

    // Colors / style (emit color THEN bold to match "&e&l" / "&b&l")
    private static final ChatColor GOLD  = ChatColor.GOLD;  // &e
    private static final ChatColor AQUA  = ChatColor.AQUA;  // &b
    private static final ChatColor BOLD  = ChatColor.BOLD;  // &l

    // Timing
    private static final int PER_LETTER_DELAY_TICKS = 6; // 0.3s per letter at 20 TPS
    private static final int FLASH_DELAY_TICKS      = 1; // rapid flash (1 tick between swaps)
    private static final int FLASH_TOGGLES          = 6; // number of color swaps during flash

    private static final String WORD = "MINEZONE";
    private static final int N = WORD.length();

    private enum Phase { FILL, FLASH }

    public TitleAnimationManager(Core main) {
        this.main = main;
    }

    public void start(Player player, FastBoard board) {
        stop(player); // never double-schedule

        BukkitTask task = new BukkitRunnable() {
            Phase phase = Phase.FILL;

            // FILL: k = how many letters are AQUA (0..N)
            int k = 0;
            int fillDelay = PER_LETTER_DELAY_TICKS;

            // FLASH state
            int flashDelay = FLASH_DELAY_TICKS;
            int flashRemaining = 0;
            ChatColor flashColor = AQUA; // start flashing from AQUA after fill

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
                            // first k letters AQUA &l, rest GOLD &l
                            String title = buildPartialBold(WORD, k);
                            board.updateTitle(safeTitle32(title));

                            k++;
                            fillDelay = PER_LETTER_DELAY_TICKS;

                            if (k > N) {
                                // all AQUA shown — enter rapid flash phase
                                phase = Phase.FLASH;
                                flashRemaining = FLASH_TOGGLES;
                                flashDelay = FLASH_DELAY_TICKS;
                                flashColor = AQUA; // start from aqua then toggle
                            }
                        }
                        break;
                    }
                    case FLASH: {
                        if (--flashDelay <= 0) {
                            // show full word in current flash color (always bold)
                            board.updateTitle(safeTitle32(flashColor.toString() + BOLD + WORD));

                            // prepare next
                            flashColor = (flashColor == AQUA ? GOLD : AQUA);
                            flashDelay = FLASH_DELAY_TICKS;
                            flashRemaining--;

                            if (flashRemaining <= 0) {
                                // reset to GOLD &l and restart the fill
                                board.updateTitle(safeTitle32(GOLD.toString() + BOLD + WORD));
                                phase = Phase.FILL;
                                k = 0;
                                fillDelay = PER_LETTER_DELAY_TICKS;
                            }
                        }
                        break;
                    }
                }
            }
        }.runTaskTimer(main, 0L, 1L); // tick every 1; internal delays control pacing

        tasks.put(player.getUniqueId(), task);
    }

    public void stop(Player player) {
        BukkitTask t = tasks.remove(player.getUniqueId());
        if (t != null) t.cancel();
    }

    public void stopAll() {
        for (BukkitTask t : tasks.values()) {
            try { t.cancel(); } catch (Exception ignored) {}
        }
        tasks.clear();
    }

    /**
     * Returns:
     *   k<=0  -> "&e&lMINEZONE" (all gold, bold)
     *   0<k<n -> "&b&l[first k]" + "&e&l[rest]" (both segments bold)
     *   k>=n  -> "&b&lMINEZONE" (all aqua, bold)
     *
     * Bold is reapplied after each color switch so everything stays bold.
     */
    private static String buildPartialBold(String text, int k) {
        if (k <= 0)  return GOLD.toString() + BOLD + text; // &e&lMINEZONE
        if (k >= text.length()) return AQUA.toString() + BOLD + text; // &b&lMINEZONE

        StringBuilder sb = new StringBuilder(32);
        sb.append(AQUA).append(BOLD).append(text, 0, k);
        sb.append(GOLD).append(BOLD).append(text, k, text.length());
        return sb.toString();
    }

    // Guard against FastBoard's 32-char raw limit
    private static String safeTitle32(String s) {
        if (s.length() <= 32) return s;
        String noBold = s.replace(BOLD.toString(), "");
        if (noBold.length() <= 32) return noBold;
        String plain = ChatColor.stripColor(noBold);
        if (plain.length() > 32) plain = plain.substring(0, 32);
        return plain;
    }
}
