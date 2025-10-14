package anthony.SuperCraftBrawl;

import net.md_5.bungee.api.ChatColor;

// Bars.java
public final class Bars { //
    private Bars() {}

    // ---------- Existing progress bar overloads (kept) ----------
    public static String progressBar(int current, int max, int width,
            org.bukkit.ChatColor fill, org.bukkit.ChatColor empty, char glyph, boolean showPercent) {
        return build(current, max, width, fill.toString(), empty.toString(), glyph, showPercent);
    }

    public static String progressBar(int current, int max, int width,
            net.md_5.bungee.api.ChatColor fill, net.md_5.bungee.api.ChatColor empty, char glyph, boolean showPercent) {
        return build(current, max, width, fill.toString(), empty.toString(), glyph, showPercent);
    }

    private static String build(int current, int max, int width,
            String fillCode, String emptyCode, char glyph, boolean showPercent) {

        if (max <= 0) max = 1;
        double pct = Math.max(0D, Math.min(1D, current / (double) max));
        int filled = (int) Math.round(width * pct);
        int empty  = Math.max(0, width - filled);

        StringBuilder sb = new StringBuilder(48);
        sb.append(net.md_5.bungee.api.ChatColor.DARK_GRAY).append('[');

        if (filled > 0) {
            sb.append(fillCode);
            for (int i = 0; i < filled; i++) sb.append(glyph);
        }
        if (empty > 0) {
            sb.append(emptyCode);
            for (int i = 0; i < empty; i++) sb.append(glyph);
        }

        sb.append(net.md_5.bungee.api.ChatColor.DARK_GRAY).append(']');
        if (showPercent) {
            sb.append(net.md_5.bungee.api.ChatColor.GRAY)
              .append((int) Math.round(pct * 100)).append('%'); // no space
        }
        return sb.toString();
    }

    // ---------- New: micro dots bar with brackets + optional percent ----------
    public static String dotsBar(int current, int max, int segments,
            net.md_5.bungee.api.ChatColor fill, net.md_5.bungee.api.ChatColor empty,
            char fillGlyph, char emptyGlyph,
            boolean showBrackets, boolean showPercent) {

        if (max <= 0) max = 1;
        double pct = Math.max(0D, Math.min(1D, current / (double) max));
        int filled = (int) Math.round(segments * pct);
        int unfilled = Math.max(0, segments - filled);

        StringBuilder sb = new StringBuilder(40);

        if (showBrackets) sb.append(net.md_5.bungee.api.ChatColor.DARK_GRAY).append('[');

        if (filled > 0) {
            sb.append(fill);
            for (int i = 0; i < filled; i++) sb.append(fillGlyph);
        }
        if (unfilled > 0) {
            sb.append(empty);
            for (int i = 0; i < unfilled; i++) sb.append(emptyGlyph);
        }

        if (showBrackets) sb.append(net.md_5.bungee.api.ChatColor.DARK_GRAY).append(']');
        sb.append(net.md_5.bungee.api.ChatColor.DARK_GRAY).append(' ');

        if (showPercent) {
            sb.append(ChatColor.GRAY)
              .append((int) Math.round(pct * 100)).append('%'); // tight "79%"
        }
        return sb.toString();
    }

    // Optional: 2.6k/5k formatter (kept for future)
    public static String abbrev(int n) {
        double v = n;
        String suf = "";
        if (Math.abs(v) >= 1_000_000) { v /= 1_000_000D; suf = "m"; }
        else if (Math.abs(v) >= 1_000) { v /= 1_000D; suf = "k"; }
        return (suf.isEmpty() ? Integer.toString(n)
                : (Math.round(v * 10.0) / 10.0) + suf);
    }
}