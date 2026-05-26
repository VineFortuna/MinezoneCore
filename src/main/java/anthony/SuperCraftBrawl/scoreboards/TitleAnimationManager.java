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

    public void start(Player player, FastBoard board) {
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
