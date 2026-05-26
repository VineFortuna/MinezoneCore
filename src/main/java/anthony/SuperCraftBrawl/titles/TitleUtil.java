package anthony.SuperCraftBrawl.titles;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class TitleUtil {

    private TitleUtil() {}

    /**
     * Send a title + optional subtitle with fade timings.
     *
     * @param p        player
     * @param title    main title (color with & codes). null to skip
     * @param subtitle subtitle (color with & codes). null to skip
     * @param fadeIn   ticks to fade in
     * @param stay     ticks to stay
     * @param fadeOut  ticks to fade out
     */
    public static void sendTitle(Player p, String title, String subtitle,
                                 int fadeIn, int stay, int fadeOut) {
        final CraftPlayer cp = (CraftPlayer) p;

        // 1) Send timing packet first
        PacketPlayOutTitle times = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
        cp.getHandle().playerConnection.sendPacket(times);

        // 2) Subtitle first (vanilla shows sub if you send before title)
        if (subtitle != null && !subtitle.isEmpty()) {
            IChatBaseComponent sub = new ChatComponentText(color(subtitle));
            PacketPlayOutTitle subPkt = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, sub);
            cp.getHandle().playerConnection.sendPacket(subPkt);
        }

        // 3) Main title
        if (title != null && !title.isEmpty()) {
            IChatBaseComponent tit = new ChatComponentText(color(title));
            PacketPlayOutTitle titlePkt = new PacketPlayOutTitle(EnumTitleAction.TITLE, tit);
            cp.getHandle().playerConnection.sendPacket(titlePkt);
        }
    }

    /** Clear the current title (instantly removes from screen). */
    public static void clear(Player p) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(
                new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
    }

    /** Reset title state (clears + resets timings). */
    public static void reset(Player p) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(
                new PacketPlayOutTitle(EnumTitleAction.RESET, null));
    }

    /** Simple action bar on 1.8 (position 2). */
    public static void actionBar(Player p, String msg) {
        if (msg == null || msg.isEmpty()) return;
        IChatBaseComponent comp = new ChatComponentText(color(msg));
        PacketPlayOutChat packet = new PacketPlayOutChat(comp, (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /* Convenience helpers */
    public static void broadcastTitle(Iterable<? extends Player> players,
                                      String title, String subtitle,
                                      int fadeIn, int stay, int fadeOut) {
        for (Player p : players) sendTitle(p, title, subtitle, fadeIn, stay, fadeOut);
    }

    public static void broadcastClear(Iterable<? extends Player> players) {
        for (Player p : players) clear(p);
    }

    /**
     * Send ONLY the timing packet (no text).
     * Use this once, then call setTitle()/setSubtitle() to update text smoothly.
     */
    public static void sendTimes(Player p, int fadeIn, int stay, int fadeOut) {
        PacketPlayOutTitle times = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(times);
    }

    /** Update ONLY the main title text (no timings). */
    public static void setTitle(Player p, String title) {
        if (title == null) title = "";
        IChatBaseComponent tit = new ChatComponentText(color(title));
        PacketPlayOutTitle titlePkt = new PacketPlayOutTitle(EnumTitleAction.TITLE, tit);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(titlePkt);
    }

    /** Update ONLY the subtitle text (no timings). */
    public static void setSubtitle(Player p, String subtitle) {
        if (subtitle == null) subtitle = "";
        IChatBaseComponent sub = new ChatComponentText(color(subtitle));
        PacketPlayOutTitle subPkt = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, sub);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(subPkt);
    }

    /**
     * Convenience: update title and/or subtitle text without touching timings.
     * Pass null to leave one unchanged.
     */
    public static void updateTitleText(Player p, String titleOrNull, String subtitleOrNull) {
        if (subtitleOrNull != null) setSubtitle(p, subtitleOrNull);
        if (titleOrNull != null) setTitle(p, titleOrNull);
    }

    // =========================
    // Smooth subtitle swap API
    // =========================

    /**
     * Show a title+subtitle, then (after delayTicks) swap ONLY the subtitle smoothly.
     * Uses UUID lookups so it survives relogs. Re-sends TIMES for the second phase so
     * you can control how long the swapped subtitle stays, without re-fading the title.
     */
    public static void sendTitleThenSwapSubtitle(Plugin plugin,
                                                 Player player,
                                                 String title,
                                                 String firstSubtitle,
                                                 int fadeIn, int stay, int fadeOut,
                                                 int delayTicks,
                                                 String secondSubtitle,
                                                 int secondStay, int secondFadeOut) {
        if (player == null) return;
        final UUID id = player.getUniqueId();

        // Phase 1: initial title + subtitle
        sendTitle(player, title, firstSubtitle, fadeIn, stay, fadeOut);

        // Phase 2: after delay, only change the subtitle (no title flicker)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) return;

            // Give the second phase its own timings (no fade-in to avoid flashing)
            sendTimes(p, 0, Math.max(0, secondStay), Math.max(0, secondFadeOut));

            // Clear any lingering subtitle text (e.g., "1") and apply the new one
            setSubtitle(p, "");
            setSubtitle(p, secondSubtitle == null ? "" : secondSubtitle);
        }, Math.max(0L, delayTicks));
    }

    /**
     * Overload: keep the same timings for both phases; just swap the subtitle after delay.
     */
    public static void sendTitleThenSwapSubtitle(Plugin plugin,
                                                 Player player,
                                                 String title,
                                                 String firstSubtitle,
                                                 int fadeIn, int stay, int fadeOut,
                                                 int delayTicks,
                                                 String secondSubtitle) {
        sendTitleThenSwapSubtitle(plugin, player, title, firstSubtitle,
                fadeIn, stay, fadeOut,
                delayTicks, secondSubtitle, stay, fadeOut);
    }
}