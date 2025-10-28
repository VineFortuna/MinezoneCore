package anthony.SuperCraftBrawl.titles;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;


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

        // 1) Send the timing packet first
        PacketPlayOutTitle times = new PacketPlayOutTitle(
                EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
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

    /**
     * Clear the current title (instantly removes from screen).
     */
    public static void clear(Player p) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(
                new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
    }

    /**
     * Reset title state (clears + resets timings).
     */
    public static void reset(Player p) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(
                new PacketPlayOutTitle(EnumTitleAction.RESET, null));
    }

    /**
     * Simple action bar on 1.8 (position 2). (Not part of titles, but handy.)
     */
    public static void actionBar(Player p, String msg) {
        if (msg == null || msg.isEmpty()) return;
        IChatBaseComponent comp = new ChatComponentText(color(msg));
        // On 1.8, use PacketPlayOutChat with byte position 2 via the legacy constructor:
        PacketPlayOutChat packet = new PacketPlayOutChat(comp, (byte)2);
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
}

