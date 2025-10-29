package anthony.SuperCraftBrawl.titles;

import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

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

    public static void sendChained(Plugin plugin, Player initialPlayer, TitleSpec first, TitleSpec second) {
        if (initialPlayer == null) return;
        final UUID uuid = initialPlayer.getUniqueId();

        Player p0 = Bukkit.getPlayer(uuid);
        if (p0 == null || !p0.isOnline()) return;
        TitleUtil.sendTitle(p0, first.title, first.subtitle, first.fadeIn, first.stay, first.fadeOut);

        // Slight overlap so there’s never a “no-title” frame
        long delay = Math.max(0, first.fadeIn + first.stay + Math.max(0, first.fadeOut - 2));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) return;

            boolean sameTitle =
                    (first.title == null ? "" : first.title).equals(second.title == null ? "" : second.title);

            if (sameTitle) {
                // Smooth path (no flicker):
                // 1) Set new timings with zero fade-in,
                // 2) Update subtitle,
                // 3) Re-send TITLE with the SAME text and zero fade-in (seeds state after relog).
                // All in the same tick -> appears as a subtitle swap.
                int stay  = Math.max(1, second.stay);
                int fout  = Math.max(0, second.fadeOut);

                TitleUtil.sendTimes(p, 0, stay, fout); // no fade-in
                TitleUtil.setSubtitle(p, second.subtitle == null ? "" : second.subtitle);
                TitleUtil.sendTitle(p, second.title, null, 0, stay, fout); // same main text, zero fade-in
            } else {
                // Different main title: normal send, but keep zero fade-in for snappiness if you prefer
                TitleUtil.sendTitle(p, second.title, second.subtitle,
                        Math.max(0, second.fadeIn), Math.max(1, second.stay), Math.max(0, second.fadeOut));
            }
        }, delay);
    }

    /** General version: play N titles in order, UUID-safe. */
    public static void sendSequence(Plugin plugin, Player initialPlayer, java.util.List<TitleSpec> specs) {
        if (initialPlayer == null || specs == null || specs.isEmpty()) return;
        final UUID uuid = initialPlayer.getUniqueId();

        long when = 0L;
        TitleSpec prev = null;
        for (TitleSpec spec : specs) {
            final long fireAt = when;
            final TitleSpec prior = prev;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) return;

                boolean sameTitle = prior != null &&
                        (prior.title == null ? "" : prior.title).equals(spec.title == null ? "" : spec.title);

                if (sameTitle) {
                    TitleUtil.sendTimes(p, Math.max(0, spec.fadeIn), Math.max(0, spec.stay), Math.max(0, spec.fadeOut));
                    TitleUtil.setSubtitle(p, spec.subtitle == null ? "" : spec.subtitle);
                } else {
                    TitleUtil.sendTitle(p, spec.title, spec.subtitle, spec.fadeIn, spec.stay, spec.fadeOut);
                }
            }, fireAt);

            when += Math.max(0, spec.fadeIn + spec.stay + spec.fadeOut) + 2L;
            prev = spec;
        }
    }
    public static void reset(Player p) {
        PacketPlayOutTitle clear = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.RESET, null);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(clear);
    }

}