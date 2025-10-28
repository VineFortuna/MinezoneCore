package anthony.SuperCraftBrawl.titles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class TitleSequence {
    private TitleSequence() {}

    public static final class TitleSpec {
        public final String title, subtitle;
        public final int fadeIn, stay, fadeOut;
        public TitleSpec(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            this.title = title; this.subtitle = subtitle;
            this.fadeIn = fadeIn; this.stay = stay; this.fadeOut = fadeOut;
        }
    }

    /** Send two titles back-to-back to a player. */
    public static void sendChained(Plugin plugin, Player p, TitleSpec first, TitleSpec second) {
        if (p == null || !p.isOnline()) return;

        // Send the first immediately
        TitleUtil.sendTitle(p, first.title, first.subtitle, first.fadeIn, first.stay, first.fadeOut);

        // Total time the first one occupies on-screen
        long delay = Math.max(0, first.fadeIn + first.stay + first.fadeOut) + 2L; // tiny buffer

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!p.isOnline()) return;
            TitleUtil.sendTitle(p, second.title, second.subtitle, second.fadeIn, second.stay, second.fadeOut);
        }, delay);
    }

    /** General version: play N titles in order. */
    public static void sendSequence(Plugin plugin, Player p, java.util.List<TitleSpec> specs) {
        if (p == null || !p.isOnline() || specs == null || specs.isEmpty()) return;

        long when = 0L;
        for (TitleSpec spec : specs) {
            final long fireAt = when;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline()) {
                    TitleUtil.sendTitle(p, spec.title, spec.subtitle, spec.fadeIn, spec.stay, spec.fadeOut);
                }
            }, fireAt);

            when += Math.max(0, spec.fadeIn + spec.stay + spec.fadeOut) + 2L; // buffer to avoid overlap
        }
    }
}

